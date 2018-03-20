package com.lesjaw.wamonitoring.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.ui.fragment.LogAbsence;
import com.lesjaw.wamonitoring.ui.fragment.LogAbsenceDate;
import com.lesjaw.wamonitoring.ui.fragment.LogAbsencePulang;
import com.lesjaw.wamonitoring.utils.DataPagerAdapter;


public class MainAbsenActivity extends AppCompatActivity {
    private DataPagerAdapter mDataPagerAdaper;
    int tab_position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_data);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mDataPagerAdaper = new DataPagerAdapter(getSupportFragmentManager());

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Absence");
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        tabLayout.setupWithViewPager(mViewPager);
        setupViewPager(mViewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab_position = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {

       /* Bundle bundle = new Bundle();
        String myMessage = "none";
        bundle.putString("message", myMessage );

        LogDaily data = new LogDaily();
        data.setArguments(bundle);

        LogChecklist data1 = new LogChecklist();
        data1.setArguments(bundle);*/

        mDataPagerAdaper.addFrag(new LogAbsence(), "CLOCK IN");
        mDataPagerAdaper.addFrag(new LogAbsencePulang(), "CLOCK OUT");
        mDataPagerAdaper.addFrag(new LogAbsenceDate(), "BY DATE");
        viewPager.setOffscreenPageLimit(2);

        viewPager.setAdapter(mDataPagerAdaper);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        // super.onSaveInstanceState(outState);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }


}
