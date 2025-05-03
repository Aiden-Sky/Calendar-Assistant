package com.aiden.desine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.aiden.desine.R;

public class ScheduleMainFragment extends Fragment {
    private CalendarView calendarView;
    private RecyclerView taskList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_main, container, false);
        
        calendarView = view.findViewById(R.id.calendarView);
        taskList = view.findViewById(R.id.habit_list);
        
        setupCalendarView();
        setupTaskList();
        
        return view;
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // TODO: 处理日期选择事件
            loadTasksForDate(year, month, dayOfMonth);
        });
    }

    private void setupTaskList() {
        // TODO: 设置任务列表适配器
    }

    private void loadTasksForDate(int year, int month, int dayOfMonth) {
        // TODO: 加载选定日期的任务
    }
}