package com.lesjaw.wamonitoring.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by lesjaw@gmail.com on 11/09/2017.
 */

public class NFCManager {

    private Activity activity;
    private NfcAdapter nfcAdpt;
    Context ctx;

    public NFCManager(Activity activity) {
        this.activity = activity;
    }

    public void verifyNFC() throws NFCNotSupported, NFCNotEnabled {

        nfcAdpt = NfcAdapter.getDefaultAdapter(activity);

        if (nfcAdpt == null)
            throw new NFCNotSupported();

        if (!nfcAdpt.isEnabled())
            throw new NFCNotEnabled();

    }

    public void enableDispatch() {
        Intent nfcIntent = new Intent(activity, getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, nfcIntent, 0);
        IntentFilter[] intentFiltersArray = new IntentFilter[] {};
        String[][] techList = new String[][] { { android.nfc.tech.Ndef.class.getName() }, { android.nfc.tech.NdefFormatable.class.getName() } };


        nfcAdpt.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techList);
    }

    public void disableDispatch() {
        if (nfcAdpt != null) {
            nfcAdpt.disableForegroundDispatch(activity);
        }
    }

    public static class NFCNotSupported extends Exception {

        public NFCNotSupported() {
            super();
        }
    }

    public static class NFCNotEnabled extends Exception {

        public NFCNotEnabled() {
            super();
        }
    }


    public void writeTag(Tag tag, NdefMessage message)  {
        if (tag != null) {
            try {
                Ndef ndefTag = Ndef.get(tag);

                if (ndefTag == null) {
                    // Let's try to format the Tag in NDEF
                    NdefFormatable nForm = NdefFormatable.get(tag);
                    if (nForm != null) {
                        nForm.connect();
                        nForm.format(message);
                        nForm.close();
                    }
                }
                else {
                    ndefTag.connect();
                    ndefTag.writeNdefMessage(message);
                    ndefTag.close();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public NdefMessage createUriMessage(String content, String type) {
        NdefRecord record = NdefRecord.createUri(type + content);
        NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
        return msg;

    }

    public void createWrite(Tag tag, NdefMessage message,  Context ctx)  {

        (new Thread(new Runnable() {
            // Password has to be 4 characters
            // Password Acknowledge has to be 2 characters
            byte[] pwd      = "-_bA".getBytes();
            byte[] pack     = "cC".getBytes();
            boolean auth = false;

            @Override
            public void run() {
                // Store tag object for use in MifareUltralight and Ndef
                MifareUltralight mifare = null;
                int debugCounter = 0;
                byte[] response;

                // Whole process is put into a big try-catch trying to catch the transceive's IOException
                try {
                    mifare = MifareUltralight.get(tag);
                    mifare.connect();
                    while (!mifare.isConnected()) ;

                    // Authenticate with the tag first
                    // In case it's already been locked
                    response = mifare.transceive(new byte[]{
                            (byte) 0x1B, // PWD_AUTH
                            pwd[0], pwd[1], pwd[2], pwd[3]
                    });
                    // Check if PACK is matching expected PACK
                    // This is a (not that) secure method to check if tag is genuine
                    byte[] packResponse;

                    if ((response != null) && (response.length >= 2)) {
                        packResponse = Arrays.copyOf(response, 2);
                        if (!(pack[0] == packResponse[0] && pack[1] == packResponse[1])) {
                            Log.d("DEBUG", "Tag could not authenticated: ");
                            auth = false;
                            sendMessageDetail("Tag could not authenticated");

                        } else {
                            Log.d("DEBUG", "Tag authenticated: " + pack[0] + ":" + pack[1] + " " + packResponse[0] + ":" + packResponse[1]);
                            //
                            sendMessageDetail("Tag authenticated");
                            auth = true;

                            // Get page 29h
                            response = mifare.transceive(new byte[]{
                                    (byte) 0x30, // READ
                                    (byte) 0x29  // page address
                            });
                            // Configure tag to protect entire storage (page 0 and above)
                            if ((response != null) && (response.length >= 16)) {  // read always returns 4 pages
                                int auth0 = 255;                                    // first page to be protected
                                mifare.transceive(new byte[]{
                                        (byte) 0xA2, // WRITE
                                        (byte) 0x29, // page address
                                        response[0], 0, response[2],              // Keep old mirror values and write 0 in RFUI byte as stated in datasheet
                                        (byte) (auth0 & 0x0ff)
                                });
                            }

                            mifare.close();

                                try {
                                    Ndef ndefTag = Ndef.get(tag);

                                    if (ndefTag == null) {
                                        // Let's try to format the Tag in NDEF
                                        NdefFormatable nForm = NdefFormatable.get(tag);
                                        if (nForm != null) {
                                            nForm.connect();
                                            nForm.format(message);
                                            nForm.close();
                                            Log.d("DEBUG", "NFC formatted: ");
                                            sendMessageDetail("Tag has been formated & written");
                                        }
                                    } else {
                                        ndefTag.connect();
                                        ndefTag.writeNdefMessage(message);
                                        ndefTag.close();
                                        Log.d("DEBUG", "NFC written: ");
                                        mifare.connect();
                                        sendMessageDetail("Tag has been written, it's now can be use by your employee, press back to write another tags");

                                        // Get page 29h
                                        response = mifare.transceive(new byte[]{
                                                (byte) 0x30, // READ
                                                (byte) 0x29  // page address
                                        });
                                        // Configure tag to protect entire storage (page 0 and above)
                                        if ((response != null) && (response.length >= 16)) {  // read always returns 4 pages
                                            int auth0 = 0;                                    // first page to be protected
                                            mifare.transceive(new byte[]{
                                                    (byte) 0xA2, // WRITE
                                                    (byte) 0x29, // page address
                                                    response[0], 0, response[2],              // Keep old mirror values and write 0 in RFUI byte as stated in datasheet
                                                    (byte) (auth0 & 0x0ff)
                                            });
                                        }
                                        mifare.close();

                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                    Log.d("DEBUG", "NFC is not empty, please write again to unlock and update NFC");
                                    sendMessageDetail("NFC is not empty, please write again to unlock and update NFC");

                                }
                        }
                    }

                    if (!auth) {
                        // Get Page 2Ah
                        response = mifare.transceive(new byte[]{
                                (byte) 0x30, // READ
                                (byte) 0x2A  // page address
                        });
                        // configure tag as write-protected with unlimited authentication tries
                        if ((response != null) && (response.length >= 16)) {    // read always returns 4 pages
                            boolean prot = false;                               // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                            int authlim = 0;                                    // 0 = unlimited tries
                            mifare.transceive(new byte[]{
                                    (byte) 0xA2, // WRITE
                                    (byte) 0x2A, // page address
                                    (byte) ((response[0] & 0x078) | (prot ? 0x080 : 0x000) | (authlim & 0x007)),    // set ACCESS byte according to our settings
                                    0, 0, 0                                                                         // fill rest as zeros as stated in datasheet (RFUI must be set as 0b)
                            });
                        }
                        // Get page 29h
                        response = mifare.transceive(new byte[]{
                                (byte) 0x30, // READ
                                (byte) 0x29  // page address
                        });
                        // Configure tag to protect entire storage (page 0 and above)
                        if ((response != null) && (response.length >= 16)) {  // read always returns 4 pages
                            int auth0 = 0;                                    // first page to be protected
                            mifare.transceive(new byte[]{
                                    (byte) 0xA2, // WRITE
                                    (byte) 0x29, // page address
                                    response[0], 0, response[2],              // Keep old mirror values and write 0 in RFUI byte as stated in datasheet
                                    (byte) (auth0 & 0x0ff)
                            });
                        }

                        // Send PACK and PWD
                        // set PACK:
                        mifare.transceive(new byte[]{
                                (byte) 0xA2,
                                (byte) 0x2C,
                                pack[0], pack[1], 0, 0  // Write PACK into first 2 Bytes and 0 in RFUI bytes
                        });
                        // set PWD:
                        mifare.transceive(new byte[]{
                                (byte) 0xA2,
                                (byte) 0x2B,
                                pwd[0], pwd[1], pwd[2], pwd[3] // Write all 4 PWD bytes into Page 43
                        });
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                } catch (Exception e) {
                    //Trying to catch any exception that may be thrown
                    e.printStackTrace();
                }

                try {
                    mifare.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Generate Ndef object from tag object
                // Connect NDEF, write message and close connection
                if (tag != null && !auth) {
                    try {
                        Ndef ndefTag = Ndef.get(tag);

                        if (ndefTag == null) {
                            // Let's try to format the Tag in NDEF
                            NdefFormatable nForm = NdefFormatable.get(tag);
                            if (nForm != null) {
                                nForm.connect();
                                nForm.format(message);
                                nForm.close();
                                Log.d("DEBUG", "NFC formatted: ");
                                sendMessageDetail("Tag has been formated & written");
                            }
                        } else {
                            ndefTag.connect();
                            ndefTag.writeNdefMessage(message);
                            ndefTag.close();
                            sendMessageDetail("Tag has been written, it's now can be use by your employee, press back to write another tags");

                            mifare.connect();
                            // Get Page 2Ah
                            response = mifare.transceive(new byte[]{
                                    (byte) 0x30, // READ
                                    (byte) 0x2A  // page address
                            });
                            // configure tag as write-protected with unlimited authentication tries
                            if ((response != null) && (response.length >= 16)) {    // read always returns 4 pages
                                boolean prot = false;                               // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                                int authlim = 0;                                    // 0 = unlimited tries
                                mifare.transceive(new byte[]{
                                        (byte) 0xA2, // WRITE
                                        (byte) 0x2A, // page address
                                        (byte) ((response[0] & 0x078) | (prot ? 0x080 : 0x000) | (authlim & 0x007)),    // set ACCESS byte according to our settings
                                        0, 0, 0                                                                         // fill rest as zeros as stated in datasheet (RFUI must be set as 0b)
                                });
                            }
                            // Get page 29h
                            response = mifare.transceive(new byte[]{
                                    (byte) 0x30, // READ
                                    (byte) 0x29  // page address
                            });
                            // Configure tag to protect entire storage (page 0 and above)
                            if ((response != null) && (response.length >= 16)) {  // read always returns 4 pages
                                int auth0 = 0;                                    // first page to be protected
                                mifare.transceive(new byte[]{
                                        (byte) 0xA2, // WRITE
                                        (byte) 0x29, // page address
                                        response[0], 0, response[2],              // Keep old mirror values and write 0 in RFUI byte as stated in datasheet
                                        (byte) (auth0 & 0x0ff)
                                });
                            }

                            // Send PACK and PWD
                            // set PACK:
                            mifare.transceive(new byte[]{
                                    (byte) 0xA2,
                                    (byte) 0x2C,
                                    pack[0], pack[1], 0, 0  // Write PACK into first 2 Bytes and 0 in RFUI bytes
                            });
                            // set PWD:
                            mifare.transceive(new byte[]{
                                    (byte) 0xA2,
                                    (byte) 0x2B,
                                    pwd[0], pwd[1], pwd[2], pwd[3] // Write all 4 PWD bytes into Page 43
                            });

                            mifare.close();
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        })).start();

    }

    private void sendMessageDetail(String Notify) {
        Intent intent = new Intent("tags");
        intent.putExtra("notify", Notify);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }

    public NdefMessage createTextMessage(String content) {
        try {
            // Get UTF-8 byte
            byte[] lang = Locale.getDefault().getLanguage().getBytes("UTF-8");
            byte[] text = content.getBytes("UTF-8"); // Content in UTF-8

            int langSize = lang.length;
            int textLength = text.length;

            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
            payload.write((byte) (langSize & 0x1F));
            payload.write(lang, 0, langSize);
            payload.write(text, 0, textLength);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
            return new NdefMessage(new NdefRecord[]{record});
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public NdefMessage createExternalMessage(String content) {
        NdefRecord externalRecord = NdefRecord.createExternal("com.survivingwithandroid", "data", content.getBytes());

        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] { externalRecord });

        return ndefMessage;
    }
}