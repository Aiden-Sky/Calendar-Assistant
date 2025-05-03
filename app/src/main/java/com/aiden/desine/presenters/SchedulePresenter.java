package com.aiden.desine.presenters;

import android.content.Context;

import com.aiden.desine.model.Schedule;
import com.aiden.desine.dao.Schedule_dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 日程Presenter，处理业务逻辑，连接View和Model
 */
public class SchedulePresenter {
    private ScheduleView view;
    private Schedule_dao repository;
    private long currentUserId = 1; // 默认用户ID
    
    /**
     * View接口，定义UI交互方法
     */
    public interface ScheduleView {
        void showLoading();
        void hideLoading();
        void showScheduleSaved();
        void showScheduleSaveFailed();
        void showSchedules(List<Schedule> schedules);
        void showValidationError(String message);
        void clearFields();
        void closeDialog();
    }

    public SchedulePresenter(Context context, ScheduleView view) {
        this.view = view;
        this.repository = new Schedule_dao(context);
    }
    
    /**
     * 设置当前用户ID
     */
    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }
    
    /**
     * 获取当前用户ID
     */
    public long getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * 保存日程
     */
    public void saveSchedule(String title, String description, Calendar dateTime, 
                           Calendar endDateTime, String repeatType, String reminderTime, 
                           String category, String priority) {
        // 执行输入验证
        if (!validateInputs(title, dateTime, endDateTime)) {
            return;
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        // 创建日程对象
        Schedule schedule = new Schedule(
            currentUserId,
            title.trim(),
            description != null ? description.trim() : "",
            dateFormat.format(dateTime.getTime()),
            timeFormat.format(dateTime.getTime()),
            timeFormat.format(endDateTime.getTime()),
            repeatType,
            reminderTime,
            category,
            priority
        );
        
        view.showLoading();
        
        // 保存到数据库
        long newId = repository.addSchedule(schedule);
        
        view.hideLoading();
        
        if (newId > 0) {
            schedule.setId(newId);
            view.showScheduleSaved();
            view.clearFields();
            view.closeDialog();
        } else {
            view.showScheduleSaveFailed();
        }
    }
    
    /**
     * 加载特定日期的日程
     */
    public void loadSchedulesByDate(String date) {
        view.showLoading();
        List<Schedule> schedules = repository.getSchedulesByDate(currentUserId, date);
        view.hideLoading();
        view.showSchedules(schedules);
    }
    
    /**
     * 加载所有日程
     */
    public void loadAllSchedules() {
        view.showLoading();
        List<Schedule> schedules = repository.getAllSchedules(currentUserId);
        view.hideLoading();
        view.showSchedules(schedules);
    }
    
    /**
     * 删除日程
     */
    public void deleteSchedule(long scheduleId) {
        repository.deleteSchedule(scheduleId);
        // 刷新日程列表
        loadAllSchedules();
    }
    
    /**
     * 验证输入
     */
    private boolean validateInputs(String title, Calendar startTime, Calendar endTime) {
        if (title == null || title.trim().isEmpty()) {
            view.showValidationError("请输入任务标题");
            return false;
        }
        
        if (endTime.before(startTime)) {
            view.showValidationError("结束时间不能早于开始时间");
            return false;
        }
        
        return true;
    }
} 