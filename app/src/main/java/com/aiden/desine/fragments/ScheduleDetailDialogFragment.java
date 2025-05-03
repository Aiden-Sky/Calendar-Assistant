package com.aiden.desine.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aiden.desine.R;
import com.aiden.desine.dao.Schedule_dao;
import com.aiden.desine.model.Schedule;
import com.aiden.desine.presenters.SchedulePresenter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 日程详情对话框，用于查看和编辑日程
 */
public class ScheduleDetailDialogFragment extends DialogFragment implements SchedulePresenter.ScheduleView {
    private TextInputEditText titleInput;
    private TextInputEditText dateInput;
    private TextInputEditText timeInput;
    private TextInputEditText endTimeInput;
    private AutoCompleteTextView priorityInput;
    private AutoCompleteTextView repeatInput;
    private AutoCompleteTextView reminderInput;
    private AutoCompleteTextView categoryInput;
    private TextInputEditText descriptionInput;
    private MaterialButton btnCancel;
    private MaterialButton btnSave;
    private MaterialButton btnDelete;
    
    private Calendar selectedDateTime;
    private Calendar selectedEndDateTime;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    
    private SchedulePresenter presenter;
    private Schedule currentSchedule;
    private OnScheduleUpdatedListener onScheduleUpdatedListener;
    
    // 传入的日程ID参数
    private static final String ARG_SCHEDULE_ID = "schedule_id";
    
    /**
     * 创建实例，传入日程ID
     */
    public static ScheduleDetailDialogFragment newInstance(long scheduleId) {
        ScheduleDetailDialogFragment fragment = new ScheduleDetailDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SCHEDULE_ID, scheduleId);
        fragment.setArguments(args);
        return fragment;
    }
    
    /**
     * 定义回调接口，用于通知外部组件日程已更新或删除
     */
    public interface OnScheduleUpdatedListener {
        void onScheduleUpdated(Schedule schedule);
        void onScheduleDeleted(long scheduleId);
    }
    
    public void setOnScheduleUpdatedListener(OnScheduleUpdatedListener listener) {
        this.onScheduleUpdatedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialogStyle);
        
        // 初始化Presenter
        presenter = new SchedulePresenter(requireContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_main_add, container, false);
        
        initViews(view);
        setupDateTimeFormatters();
        setupListeners();
        setupDropdowns();
        
        // 加载日程详情
        if (getArguments() != null) {
            long scheduleId = getArguments().getLong(ARG_SCHEDULE_ID, -1);
            if (scheduleId != -1) {
                loadScheduleDetails(scheduleId);
            }
        }
        
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            // 设置对话框宽度为屏幕宽度的90%
            Window window = getDialog().getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                window.setAttributes(params);
                
                // 设置背景透明以显示圆角
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }

    private void initViews(View view) {
        titleInput = view.findViewById(R.id.edit_task_title);
        dateInput = view.findViewById(R.id.edit_task_date);
        timeInput = view.findViewById(R.id.edit_task_time);
        endTimeInput = view.findViewById(R.id.edit_task_end_time);
        priorityInput = view.findViewById(R.id.priority_dropdown);
        repeatInput = view.findViewById(R.id.repeat_dropdown);
        reminderInput = view.findViewById(R.id.reminder_dropdown);
        categoryInput = view.findViewById(R.id.category_dropdown);
        descriptionInput = view.findViewById(R.id.edit_task_description);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);
        
        // 添加删除按钮
        btnDelete = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnDelete.setText("删除");
        btnDelete.setTextColor(requireContext().getResources().getColor(android.R.color.holo_red_dark));
        
        // 将删除按钮添加到按钮容器中
        ViewGroup buttonContainer = (ViewGroup) btnSave.getParent();
        buttonContainer.addView(btnDelete, 0); // 添加到第一个位置
        
        selectedDateTime = Calendar.getInstance();
        selectedEndDateTime = Calendar.getInstance();
        selectedEndDateTime.add(Calendar.HOUR_OF_DAY, 1);
    }

    private void setupDateTimeFormatters() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        updateDateTimeDisplay();
    }

    private void setupListeners() {
        dateInput.setOnClickListener(v -> showDatePicker());
        timeInput.setOnClickListener(v -> showTimePicker());
        endTimeInput.setOnClickListener(v -> showEndTimePicker());
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnSave.setOnClickListener(v -> {
            updateSchedule();
        });
        
        btnDelete.setOnClickListener(v -> {
            if (currentSchedule != null) {
                deleteSchedule(currentSchedule.getId());
            }
        });
    }

    private void setupDropdowns() {
        // 优先级下拉选项
        setupPriorityDropdown();
        
        // 重复周期下拉选项
        String[] repeatOptions = new String[]{"不重复", "每日", "每周一", "每周二", "每周三", "每周四", "每周五", "每周六", "每周日"};
        ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            repeatOptions
        );
        repeatInput.setAdapter(repeatAdapter);
        
        // 提醒时间下拉选项
        String[] reminderOptions = new String[]{"无提醒", "提前5分钟", "提前15分钟", "提前30分钟", "提前1小时", "提前1天"};
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            reminderOptions
        );
        reminderInput.setAdapter(reminderAdapter);
        
        // 标签分类下拉选项
        String[] categoryOptions = new String[]{"工作", "学习", "生活", "健康", "购物", "娱乐", "其他"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            categoryOptions
        );
        categoryInput.setAdapter(categoryAdapter);
    }

    private void setupPriorityDropdown() {
        String[] priorities = new String[]{"高", "中", "低"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            priorities
        );
        priorityInput.setAdapter(adapter);
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            // 更新结束时间的日期与开始日期一致
            selectedEndDateTime.set(Calendar.YEAR, year);
            selectedEndDateTime.set(Calendar.MONTH, month);
            selectedEndDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            updateDateTimeDisplay();
        }, 
        selectedDateTime.get(Calendar.YEAR),
        selectedDateTime.get(Calendar.MONTH),
        selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            
            // 如果结束时间早于开始时间，则自动将结束时间设置为开始时间后1小时
            if (selectedEndDateTime.before(selectedDateTime)) {
                selectedEndDateTime.setTimeInMillis(selectedDateTime.getTimeInMillis());
                selectedEndDateTime.add(Calendar.HOUR_OF_DAY, 1);
            }
            
            updateDateTimeDisplay();
        },
        selectedDateTime.get(Calendar.HOUR_OF_DAY),
        selectedDateTime.get(Calendar.MINUTE),
        true
        ).show();
    }
    
    private void showEndTimePicker() {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            selectedEndDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedEndDateTime.set(Calendar.MINUTE, minute);
            updateDateTimeDisplay();
        },
        selectedEndDateTime.get(Calendar.HOUR_OF_DAY),
        selectedEndDateTime.get(Calendar.MINUTE),
        true
        ).show();
    }

    private void updateDateTimeDisplay() {
        dateInput.setText(dateFormat.format(selectedDateTime.getTime()));
        timeInput.setText(timeFormat.format(selectedDateTime.getTime()));
        endTimeInput.setText(timeFormat.format(selectedEndDateTime.getTime()));
    }
    
    /**
     * 加载日程详情
     */
    private void loadScheduleDetails(long scheduleId) {
        // 从数据库获取日程详情
        Schedule_dao dao = new Schedule_dao(requireContext());
        currentSchedule = dao.getScheduleById(scheduleId);
        
        if (currentSchedule != null) {
            // 填充表单
            titleInput.setText(currentSchedule.getTitle());
            descriptionInput.setText(currentSchedule.getDescription());
            
            // 解析日期和时间
            try {
                Date date = dateFormat.parse(currentSchedule.getDate());
                Date startTime = timeFormat.parse(currentSchedule.getStartTime());
                
                if (date != null && startTime != null) {
                    selectedDateTime.setTime(date);
                    // 设置时间部分
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startTime);
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, startCal.get(Calendar.HOUR_OF_DAY));
                    selectedDateTime.set(Calendar.MINUTE, startCal.get(Calendar.MINUTE));
                }
                
                // 解析结束时间
                Date endTime = timeFormat.parse(currentSchedule.getEndTime());
                if (endTime != null) {
                    selectedEndDateTime.setTime(date); // 同一天
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(endTime);
                    selectedEndDateTime.set(Calendar.HOUR_OF_DAY, endCal.get(Calendar.HOUR_OF_DAY));
                    selectedEndDateTime.set(Calendar.MINUTE, endCal.get(Calendar.MINUTE));
                }
                
                updateDateTimeDisplay();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            
            // 设置下拉选项
            repeatInput.setText(currentSchedule.getRepeatType(), false);
            reminderInput.setText(currentSchedule.getReminderTime(), false);
            categoryInput.setText(currentSchedule.getCategory(), false);
            priorityInput.setText(currentSchedule.getPriority(), false);
        }
    }
    
    /**
     * 更新日程
     */
    private void updateSchedule() {
        if (currentSchedule == null) {
            return;
        }
        
        // 验证输入
        if (titleInput.getText() == null || titleInput.getText().toString().trim().isEmpty()) {
            showValidationError("请输入任务标题");
            return;
        }
        
        if (selectedEndDateTime.before(selectedDateTime)) {
            showValidationError("结束时间不能早于开始时间");
            return;
        }
        
        // 更新日程对象
        currentSchedule.setTitle(titleInput.getText().toString().trim());
        currentSchedule.setDescription(descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "");
        currentSchedule.setDate(dateFormat.format(selectedDateTime.getTime()));
        currentSchedule.setStartTime(timeFormat.format(selectedDateTime.getTime()));
        currentSchedule.setEndTime(timeFormat.format(selectedEndDateTime.getTime()));
        currentSchedule.setRepeatType(repeatInput.getText().toString());
        currentSchedule.setReminderTime(reminderInput.getText().toString());
        currentSchedule.setCategory(categoryInput.getText().toString());
        currentSchedule.setPriority(priorityInput.getText().toString());
        
        // 保存到数据库
        Schedule_dao dao = new Schedule_dao(requireContext());
        boolean success = dao.updateSchedule(currentSchedule);
        
        if (success) {
            Toast.makeText(requireContext(), "日程更新成功", Toast.LENGTH_SHORT).show();
            
            // 通知监听器
            if (onScheduleUpdatedListener != null) {
                onScheduleUpdatedListener.onScheduleUpdated(currentSchedule);
            }
            
            dismiss();
        } else {
            Toast.makeText(requireContext(), "更新失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 删除日程
     */
    private void deleteSchedule(long scheduleId) {
        Schedule_dao dao = new Schedule_dao(requireContext());
        boolean success = dao.deleteSchedule(scheduleId);
        
        if (success) {
            Toast.makeText(requireContext(), "日程已删除", Toast.LENGTH_SHORT).show();
            
            // 通知监听器
            if (onScheduleUpdatedListener != null) {
                onScheduleUpdatedListener.onScheduleDeleted(scheduleId);
            }
            
            dismiss();
        } else {
            Toast.makeText(requireContext(), "删除失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
    
    // 实现ScheduleView接口的方法
    
    @Override
    public void showLoading() {
        // 实现加载指示器
    }

    @Override
    public void hideLoading() {
        // 隐藏加载指示器
    }

    @Override
    public void showScheduleSaved() {
        // 不需要实现，因为使用直接方法更新
    }

    @Override
    public void showScheduleSaveFailed() {
        // 不需要实现，因为使用直接方法更新
    }

    @Override
    public void showSchedules(List<Schedule> schedules) {
        // 不需要实现
    }

    @Override
    public void showValidationError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clearFields() {
        // 不需要实现
    }

    @Override
    public void closeDialog() {
        dismiss();
    }
} 