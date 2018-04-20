package com.lesjaw.wamonitoring.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFrag extends Fragment {

    private ViewPager viewPager;
    private String mLevelUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_main_chat, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        //tabLayout = (TabLayout) view.findViewById(R.id.tab);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PreferenceHelper mPrefHelper = new PreferenceHelper(getActivity());
        mLevelUser = mPrefHelper.getLevelUser();

        setupViewPager(viewPager);
        /*tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        switch (mLevelUser) {
            case "0":
                viewPagerAdapter.addFragment(new ChatCompany(), "Company");
                viewPagerAdapter.addFragment(new ChatAM(), "AM");
                viewPager.setOffscreenPageLimit(2);
                break;
            case "1":
                viewPagerAdapter.addFragment(new ChatCompany(), "Company");
                viewPagerAdapter.addFragment(new ChatUsers(), "Users");
                viewPagerAdapter.addFragment(new ChatAM(), "AM");
                viewPager.setOffscreenPageLimit(3);
                break;
            case "2":
                viewPagerAdapter.addFragment(new ChatCompany(), "Company");
                viewPagerAdapter.addFragment(new ChatUsers(), "Users");
                viewPager.setOffscreenPageLimit(2);
                break;
            case "4":
                viewPagerAdapter.addFragment(new ChatCompany(), "Company");
//                viewPagerAdapter.addFragment(new ChatUsers(), "Users");
                viewPagerAdapter.addFragment(new ChatAM(), "AM");
                viewPager.setOffscreenPageLimit(2);
                break;
        }

        viewPager.setAdapter(viewPagerAdapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList = new ArrayList<>();
        List<String> fragmentTitles = new ArrayList<>();

        ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

        void addFragment(Fragment fragment, String name) {
            fragmentList.add(fragment);
            fragmentTitles.add(name);
        }
    }
}
