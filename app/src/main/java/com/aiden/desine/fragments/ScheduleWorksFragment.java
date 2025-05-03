package com.aiden.desine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 全部日程列表界面，实现MVP架构中的View层
 */
public class ScheduleWorksFragment extends Fragment implements SchedulePresenter.ScheduleView {
    private RecyclerView recyclerView;
    private LinearLayout emptyStateContainer;
    private Spinner filterSpinner;
    private FloatingActionButton fabAddSchedule;
    
    private ScheduleAdapter adapter;
    private SchedulePresenter presenter;
    private List<Schedule> allSchedules = new ArrayList<>();
    
    // 过滤选项
    private static final int FILTER_ALL = 0;
    private static final int FILTER_TODAY = 1;
    private static final int FILTER_COMPLETED = 2;
    private static final int FILTER_UNCOMPLETED = 3;
    private static final int FILTER_HIGH_PRIORITY = 4;
    
    private int currentFilter = FILTER_ALL;
    private long currentUserId = 1; // 默认用户ID，实际应从登录会话获取
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String todayDate = dateFormat.format(new Date());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                           @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_works, container, false);
        
        // 初始化视图
        initViews(view);
        
        // 初始化Presenter
        presenter = new SchedulePresenter(requireContext(), this);
        presenter.setCurrentUserId(currentUserId);
        
        // 设置RecyclerView
        setupRecyclerView();
        
        // 设置过滤器
        setupFilterSpinner();
        
        // 设置添加按钮
        setupAddButton();
        
        // 加载所有日程
        presenter.loadAllSchedules();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.schedule_recycler_view);
        emptyStateContainer = view.findViewById(R.id.empty_state_container);
        filterSpinner = view.findViewById(R.id.filter_spinner);
        fabAddSchedule = view.findViewById(R.id.fab_add_schedule);
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ScheduleAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        
        // 设置点击事件
        adapter.setOnScheduleClickListener(this::showScheduleDetails);
        
        // 设置状态变更事件
        adapter.setOnScheduleStatusChangedListener(this::updateScheduleStatus);
    }
    
    private void setupFilterSpinner() {
        String[] filterOptions = new String[]{"全部日程", "今日日程", "已完成", "未完成", "高优先级"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);
        
        // 设置选择监听
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = position;
                applyFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不做任何处理
            }
        });
    }
    
    private void setupAddButton() {
        fabAddSchedule.setOnClickListener(v -> {
            // 打开添加日程对话框
            AddScheduleDialogFragment dialog = new AddScheduleDialogFragment();
            dialog.setOnScheduleSavedListener(schedule -> {
                // 刷新日程列表
                presenter.loadAllSchedules();
            });
            dialog.show(getChildFragmentManager(), "add_schedule");
        });
    }
    
    /**
     * 应用过滤器，根据当前选择的过滤条件显示日程
     */
    private void applyFilter() {
        if (allSchedules.isEmpty()) {
            return;
        }
        
        List<Schedule> filteredList;
        
        switch (currentFilter) {
            case FILTER_TODAY:
                // 今日日程
                filteredList = allSchedules.stream()
                        .filter(s -> s.getDate().equals(todayDate))
                        .collect(Collectors.toList());
                break;
            case FILTER_COMPLETED:
                // 已完成
                filteredList = allSchedules.stream()
                        .filter(Schedule::isCompleted)
                        .collect(Collectors.toList());
                break;
            case FILTER_UNCOMPLETED:
                // 未完成
                filteredList = allSchedules.stream()
                        .filter(s -> !s.isCompleted())
                        .collect(Collectors.toList());
                break;
            case FILTER_HIGH_PRIORITY:
                // 高优先级
                filteredList = allSchedules.stream()
                        .filter(s -> "高".equals(s.getPriority()))
                        .collect(Collectors.toList());
                break;
            case FILTER_ALL:
            default:
                // 全部日程
                filteredList = new ArrayList<>(allSchedules);
                break;
        }
        
        updateUI(filteredList);
    }
    
    /**
     * 更新UI显示
     */
    private void updateUI(List<Schedule> schedules) {
        if (schedules.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.updateData(schedules);
        }
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
                // 刷新日程列表
                presenter.loadAllSchedules();
            }

            @Override
            public void onScheduleDeleted(long scheduleId) {
                // 刷新日程列表
                presenter.loadAllSchedules();
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
    
    // 实现ScheduleView接口方法
    
    @Override
    public void showLoading() {
        // 可以添加加载进度条
    }

    @Override
    public void hideLoading() {
        // 隐藏加载进度条
    }

    @Override
    public void showScheduleSaved() {
        // 不需要实现
    }

    @Override
    public void showScheduleSaveFailed() {
        // 不需要实现
    }

    @Override
    public void showSchedules(List<Schedule> schedules) {
        // 保存所有日程，以便后续过滤
        this.allSchedules = new ArrayList<>(schedules);
        // 应用当前选择的过滤器
        applyFilter();
    }

    @Override
    public void showValidationError(String message) {
        // 不需要实现
    }

    @Override
    public void clearFields() {
        // 不需要实现
    }

    @Override
    public void closeDialog() {
        // 不需要实现
    }
}