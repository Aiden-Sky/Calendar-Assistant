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
import com.aiden.desine.model.Schedule;
import com.aiden.desine.presenters.SchedulePresenter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 添加日程对话框，实现MVP架构中的View层
 */
public class AddScheduleDialogFragment extends DialogFragment implements SchedulePresenter.ScheduleView {
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
    
    private Calendar selectedDateTime;
    private Calendar selectedEndDateTime;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    
    private SchedulePresenter presenter;
    private OnScheduleSavedListener onScheduleSavedListener;
    
    /**
     * 定义回调接口，用于通知外部组件日程已保存
     */
    public interface OnScheduleSavedListener {
        void onScheduleSaved(Schedule schedule);
    }
    
    public void setOnScheduleSavedListener(OnScheduleSavedListener listener) {
        this.onScheduleSavedListener = listener;
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
        
        selectedDateTime = Calendar.getInstance();
        selectedEndDateTime = Calendar.getInstance();
        selectedEndDateTime.add(Calendar.HOUR_OF_DAY, 1); // 默认结束时间比开始时间晚1小时
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
            // 使用Presenter保存日程
            presenter.saveSchedule(
                titleInput.getText() != null ? titleInput.getText().toString() : "",
                descriptionInput.getText() != null ? descriptionInput.getText().toString() : "",
                selectedDateTime,
                selectedEndDateTime,
                repeatInput.getText().toString(),
                reminderInput.getText().toString(),
                categoryInput.getText().toString(),
                priorityInput.getText().toString()
            );
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
        repeatInput.setText(repeatOptions[0], false);
        
        // 提醒时间下拉选项
        String[] reminderOptions = new String[]{"无提醒", "提前5分钟", "提前15分钟", "提前30分钟", "提前1小时", "提前1天"};
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            reminderOptions
        );
        reminderInput.setAdapter(reminderAdapter);
        reminderInput.setText(reminderOptions[0], false);
        
        // 标签分类下拉选项
        String[] categoryOptions = new String[]{"工作", "学习", "生活", "健康", "购物", "娱乐", "其他"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            categoryOptions
        );
        categoryInput.setAdapter(categoryAdapter);
        categoryInput.setText(categoryOptions[0], false);
    }

    private void setupPriorityDropdown() {
        String[] priorities = new String[]{"高", "中", "低"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            priorities
        );
        priorityInput.setAdapter(adapter);
        priorityInput.setText(priorities[1], false); // 默认设为"中"优先级
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
    
    // 实现ScheduleView接口的方法
    
    @Override
    public void showLoading() {
        // 可以实现加载指示器，如进度条
        btnSave.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        btnSave.setEnabled(true);
    }

    @Override
    public void showScheduleSaved() {
        Toast.makeText(requireContext(), "日程保存成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showScheduleSaveFailed() {
        Toast.makeText(requireContext(), "保存失败，请重试", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSchedules(List<Schedule> schedules) {
        // 此方法在当前对话框中不需要实现，因为它不显示日程列表
    }

    @Override
    public void showValidationError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void clearFields() {
        titleInput.setText("");
        descriptionInput.setText("");
        selectedDateTime = Calendar.getInstance();
        selectedEndDateTime = Calendar.getInstance();
        selectedEndDateTime.add(Calendar.HOUR_OF_DAY, 1);
        updateDateTimeDisplay();
        repeatInput.setText("不重复", false);
        reminderInput.setText("无提醒", false);
        categoryInput.setText("工作", false);
        priorityInput.setText("中", false);
    }

    @Override
    public void closeDialog() {
        dismiss();
    }
}