package com.aiden.desine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiden.desine.R;
import com.aiden.desine.adapters.ScheduleAdapter;
import com.aiden.desine.dao.Schedule_dao;
import com.aiden.desine.model.Schedule;
import com.aiden.desine.presenters.SchedulePresenter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 日程主界面，实现MVP架构中的View层
 */
public class ScheduleMainFragment extends Fragment implements SchedulePresenter.ScheduleView {
    private CalendarView calendarView;
    private RecyclerView taskListView;
    private TextView emptyView;
    private ScheduleAdapter adapter;
    private SchedulePresenter presenter;
    private SimpleDateFormat dateFormat;
    private String currentDate;
    private long currentUserId = 1; // 默认用户ID，实际应从登录会话获取

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_main, container, false);
        
        calendarView = view.findViewById(R.id.calendarView);
        taskListView = view.findViewById(R.id.habit_list);
        
        // 添加空视图
        addEmptyView(view);
        
        // 初始化日期格式化工具
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = dateFormat.format(new Date());
        
        // 初始化Presenter
        presenter = new SchedulePresenter(requireContext(), this);
        presenter.setCurrentUserId(currentUserId);
        
        setupCalendarView();
        setupTaskList();
        setupFab(view);
        
        // 加载当天任务
        presenter.loadSchedulesByDate(currentDate);
        
        return view;
    }

    /**
     * 添加空视图，当没有任务时显示
     */
    private void addEmptyView(View rootView) {
        ViewGroup container = (ViewGroup) taskListView.getParent();
        
        emptyView = new TextView(requireContext());
        emptyView.setText("今日暂无任务");
        emptyView.setTextSize(16);
        emptyView.setPadding(0, 30, 0, 0);
        emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        
        // 将空视图添加到卡片的容器中
        container.addView(emptyView);
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // 处理日期选择事件
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            currentDate = dateFormat.format(selectedDate.getTime());
            
            // 加载选定日期的任务
            presenter.loadSchedulesByDate(currentDate);
        });
    }

    private void setupTaskList() {
        // 设置RecyclerView的布局管理器
        taskListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // 初始化适配器
        adapter = new ScheduleAdapter(requireContext(), new ArrayList<>());
        taskListView.setAdapter(adapter);
        
        // 设置任务点击事件
        adapter.setOnScheduleClickListener(this::showScheduleDetails);
        
        // 设置任务状态变更事件
        adapter.setOnScheduleStatusChangedListener(this::updateScheduleStatus);
    }

    /**
     * 显示日程详情
     */
    private void showScheduleDetails(Schedule schedule) {
        // 打开日程详情对话框
        ScheduleDetailDialogFragment dialog = ScheduleDetailDialogFragment.newInstance(schedule.getId());
        dialog.setOnScheduleUpdatedListener(new ScheduleDetailDialogFragment.OnScheduleUpdatedListener() {
            @Override
            public void onScheduleUpdated(Schedule schedule) {
                // 更新日程列表
                presenter.loadSchedulesByDate(currentDate);
            }

            @Override
            public void onScheduleDeleted(long scheduleId) {
                // 更新日程列表
                presenter.loadSchedulesByDate(currentDate);
            }
        });
        dialog.show(getChildFragmentManager(), "schedule_detail");
    }

    /**
     * 更新日程完成状态
     */
    private void updateScheduleStatus(Schedule schedule, boolean isCompleted) {
        Schedule_dao dao = new Schedule_dao(requireContext());
        schedule.setCompleted(isCompleted);
        dao.updateSchedule(schedule);
    }

    private void loadTasksForDate(int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, dayOfMonth);
        String dateString = dateFormat.format(cal.getTime());
        presenter.loadSchedulesByDate(dateString);
    }

    private void setupFab(View view) {
        FloatingActionButton fab = view.findViewById(R.id.fab_add_task);
        fab.setOnClickListener(v -> {
            AddScheduleDialogFragment dialog = new AddScheduleDialogFragment();
            
            // 设置日程保存回调
            dialog.setOnScheduleSavedListener(schedule -> {
                // 如果新添加的日程是当前显示的日期，则刷新列表
                if (currentDate.equals(schedule.getDate())) {
                    presenter.loadSchedulesByDate(currentDate);
                }
            });
            
            dialog.show(getChildFragmentManager(), "add_schedule");
        });
    }
    
    // 实现ScheduleView接口方法
    
    @Override
    public void showLoading() {
        // 可以添加加载指示器
    }

    @Override
    public void hideLoading() {
        // 隐藏加载指示器
    }

    @Override
    public void showScheduleSaved() {
        // 在添加对话框中处理
    }

    @Override
    public void showScheduleSaveFailed() {
        // 在添加对话框中处理
    }

    @Override
    public void showSchedules(List<Schedule> schedules) {
        if (schedules.isEmpty()) {
            taskListView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            taskListView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.updateData(schedules);
        }
    }

    @Override
    public void showValidationError(String message) {
        // 在对话框中处理
    }

    @Override
    public void clearFields() {
        // 在对话框中处理
    }

    @Override
    public void closeDialog() {
        // 在对话框中处理
    }
}