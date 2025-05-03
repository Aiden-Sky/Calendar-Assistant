package com.aiden.desine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aiden.desine.R;
import com.aiden.desine.adapters.ScheduleViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ScheduleFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ScheduleViewPagerAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        initViews(view);
        setupViewPager();

        return view;
    }

    private void initViews(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
    }

    private void setupViewPager() {
        adapter = new ScheduleViewPagerAdapter(requireActivity());

        // 添加子页面
        adapter.addFragment(new ScheduleMainFragment(), "日历视图");
        adapter.addFragment(new ScheduleWorksFragment(), "全部日程");

        viewPager.setAdapter(adapter);

        // 关联TabLayout和ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();
    }
}