package com.aiden.desine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.aiden.desine.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class HabitFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView habitList;
    private FloatingActionButton fabAddHabit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_habit, container, false);

        // 绑定控件
        calendarView = view.findViewById(R.id.calendarView);
        habitList = view.findViewById(R.id.habit_list);
        fabAddHabit = view.findViewById(R.id.fab_add_habit);

        // 示例点击事件
        fabAddHabit.setOnClickListener(v -> {
            // 打开添加习惯界面（你可以跳转到新Activity或弹出Dialog）
        });

        // 这里你可以设置 RecyclerView 的 Adapter 等逻辑

        return view;
    }
}
