package com.aiden.desine.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
        // 在设置布局之前应用主题
        boolean isNightMode = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getBoolean("night_mode", false);
        AppCompatDelegate.setDefaultNightMode(
            isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化所有Fragment
        if (savedInstanceState == null) {
            habitFragment = new HabitFragment();
            tomatoFragment = new TomatoFragment();
            scheduleFragment = new ScheduleFragment();
            mineFragment = new PersonalFragment();
            statisticFragment = new StatisticFragment();

            // 修改Fragment的添加逻辑，默认全部隐藏
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, habitFragment, "habitFragment").hide(habitFragment)
                    .add(R.id.fragment_container, tomatoFragment, "tomatoFragment").hide(tomatoFragment)
                    .add(R.id.fragment_container, scheduleFragment, "scheduleFragment").hide(scheduleFragment)
                    .add(R.id.fragment_container, mineFragment, "mineFragment").hide(mineFragment)
                    .add(R.id.fragment_container, statisticFragment, "statisticFragment").hide(statisticFragment)
                    .commit();

            // 根据夜间模式设置当前Fragment并显示
            currentFragment = isNightMode ? mineFragment : habitFragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .show(currentFragment)
                    .commit();
        } else {
            // 如果状态已保存，从FragmentManager中恢复Fragment
            habitFragment = getSupportFragmentManager().findFragmentByTag("habitFragment");
            tomatoFragment = getSupportFragmentManager().findFragmentByTag("tomatoFragment");
            scheduleFragment = getSupportFragmentManager().findFragmentByTag("scheduleFragment");
            mineFragment = getSupportFragmentManager().findFragmentByTag("mineFragment");
            statisticFragment = getSupportFragmentManager().findFragmentByTag("statisticFragment");
            
            // 尝试从保存状态恢复当前Fragment
            if (savedInstanceState.containsKey("currentFragment")) {
                try {
                    currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");
                } catch (Exception e) {
                    currentFragment = habitFragment; // 默认回到习惯Fragment
                }
            } else {
                currentFragment = habitFragment;
            }
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
            // 根据夜间模式设置默认选中的导航项
            bottomNavigationView.setSelectedItemId(isNightMode ? R.id.nav_mine : R.id.nav_habit);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前Fragment的状态
        getSupportFragmentManager().putFragment(outState, "currentFragment", currentFragment);
        // 保存当前选中的导航项
        outState.putInt("selectedItemId", bottomNavigationView.getSelectedItemId());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 恢复Fragment状态
        if (savedInstanceState.containsKey("currentFragment")) {
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "currentFragment");
            
            // 恢复底部导航选中状态
            int selectedItemId = savedInstanceState.getInt("selectedItemId", R.id.nav_habit);
            bottomNavigationView.setSelectedItemId(selectedItemId);
            
            // 确保所有Fragment加载并设置当前Fragment可见
            if (habitFragment == null) habitFragment = getSupportFragmentManager().findFragmentByTag("habitFragment");
            if (tomatoFragment == null) tomatoFragment = getSupportFragmentManager().findFragmentByTag("tomatoFragment");
            if (scheduleFragment == null) scheduleFragment = getSupportFragmentManager().findFragmentByTag("scheduleFragment");
            if (mineFragment == null) mineFragment = getSupportFragmentManager().findFragmentByTag("mineFragment");
            if (statisticFragment == null) statisticFragment = getSupportFragmentManager().findFragmentByTag("statisticFragment");
        }
    }
}