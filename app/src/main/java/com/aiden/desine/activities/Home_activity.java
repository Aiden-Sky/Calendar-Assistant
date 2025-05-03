package com.aiden.desine.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aiden.desine.fragments.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.aiden.desine.R;



public class Home_activity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Fragment habitFragment;
    private Fragment tomatoFragment;
    private Fragment currentFragment;
    private Fragment scheduleFragment;
    private Fragment mineFragment;
    private Fragment statisticFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化所有Fragment
        if (savedInstanceState == null) {
            habitFragment = new HabitFragment();
            tomatoFragment = new TomatoFragment();
            scheduleFragment = new ScheduleFragment();
//            mineFragment = new MineFragment();
            statisticFragment = new StatisticFragment();

            // 添加第一个Fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, habitFragment)
                    .add(R.id.fragment_container, tomatoFragment).hide(tomatoFragment)
                    .add(R.id.fragment_container, scheduleFragment).hide(scheduleFragment)
//                    .add(R.id.fragment_container, mineFragment).hide(mineFragment)
                    .add(R.id.fragment_container, statisticFragment).hide(statisticFragment)
                    .commit();

            currentFragment = habitFragment;
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment showFragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_habit) {
                showFragment = habitFragment;
            } else if (itemId == R.id.nav_tomato) {
                showFragment = tomatoFragment;
            }else if (itemId == R.id.nav_schedule) {
                showFragment = scheduleFragment;
            } else if (itemId == R.id.nav_mine) {
                showFragment = mineFragment;
            }else if (itemId == R.id.nav_statistic) {
                showFragment = statisticFragment;
            }



            if (showFragment != null && showFragment != currentFragment) {
                // 使用hide/show切换Fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .show(showFragment)
                        .commit();
                currentFragment = showFragment;
            }
            return true;
        });

        // 设置默认选中项
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_habit);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前Fragment的状态
        getSupportFragmentManager().putFragment(outState, "currentFragment", currentFragment);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 恢复Fragment状态
        currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");
    }
}