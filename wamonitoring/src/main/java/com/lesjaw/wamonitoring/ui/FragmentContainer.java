package com.lesjaw.wamonitoring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.ui.fragment.LogAbsence;
import com.lesjaw.wamonitoring.ui.fragment.LogChecklist;
import com.lesjaw.wamonitoring.ui.fragment.LogDaily;


public class FragmentContainer extends AppCompatActivity {

    private String email;
    private String nama;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        Intent i = getIntent();
        String id_fragement = i.getStringExtra("id_fragement");
        if (id_fragement.equals("Tags Employee")|| id_fragement.equals("Checklist Employee")){
            email = i.getStringExtra("Memail");
            nama = i.getStringExtra("nama");
        } else {
            email = "none";
        }

        if (!email.equals("none")) {
            getSupportActionBar().setTitle(id_fragement + " by " + nama);
        } else {
            getSupportActionBar().setTitle(id_fragement);
        }

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        InitLoad(id_fragement);

    }

    private void InitLoad (String idFragement) {

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        String myMessage = email;
        bundle.putString("message", myMessage );

        switch (idFragement) {
            case "Checklist":
                LogChecklist data2 = new LogChecklist();
                bundle.putString("message", "none");
                data2.setArguments(bundle);
                ft.replace(R.id.fragment_container, data2);
                break;
            case "LogDaily": {
                LogDaily data = new LogDaily();
                data.setArguments(bundle);
                ft.replace(R.id.fragment_container, data);
                break;
            }
            case "Absence":
                ft.replace(R.id.fragment_container, new LogAbsence());
                break;
            case "Tags Employee": {
                LogDaily data = new LogDaily();
                data.setArguments(bundle);
                ft.replace(R.id.fragment_container, data);
                break;
            }
            case "Checklist Employee":
                LogChecklist data1 = new LogChecklist();
                data1.setArguments(bundle);
                ft.replace(R.id.fragment_container, data1);
                break;
        }
        ft.commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}