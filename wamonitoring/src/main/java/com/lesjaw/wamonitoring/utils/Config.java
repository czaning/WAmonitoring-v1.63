package com.lesjaw.wamonitoring.utils;

/**
 * Created by lesjaw@gmail.com on 25/09/2017.
 */

public class Config {

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    public static final String SEND_NOTICE = "sendNotice";
    public static final String SEND_MESSAGE = "sendMessage";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";

    public static final String DOMAIN = "https://olmatix.com/";
    public static final String APPLICATION_ID = "com.lesjaw.wamonitoring";


}
