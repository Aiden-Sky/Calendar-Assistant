package com.aiden.desine.model;

/**
 * 日程/任务数据模型
 */
public class Schedule {
    private long id;
    private long userId;
    private String title;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private String repeatType;
    private String reminderTime;
    private String category;
    private String priority;
    private boolean isCompleted;
    private String createdAt;

    public Schedule() {
        // 默认构造函数
    }

    public Schedule(long userId, String title, String description, String date,
                    String startTime, String endTime, String repeatType,
                    String reminderTime, String category, String priority) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeatType = repeatType;
        this.reminderTime = reminderTime;
        this.category = category;
        this.priority = priority;
        this.isCompleted = false;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getRepeatType() { return repeatType; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
} 