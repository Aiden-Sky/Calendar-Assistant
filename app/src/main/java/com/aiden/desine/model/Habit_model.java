package com.aiden.desine.model;

public class Habit_model {
    private int id;              // 习惯ID
    private String name;         // 习惯名称
    private String unit;         // 单位
    private int goalValue;       // 目标值
    private String frequency;    // 频率
    private String reminderTime; // 提醒时间
    private String notes;        // 备注
    private int streak;          // 连续天数
    private double completionRate; // 完成率
    private boolean completedToday; // 今日是否完成

    // 构造函数
    public Habit_model(int id, String name, String unit, int goalValue, String frequency,
                       String reminderTime, String notes, int streak, double completionRate,
                       boolean completedToday) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.goalValue = goalValue;
        this.frequency = frequency;
        this.reminderTime = reminderTime;
        this.notes = notes;
        this.streak = streak;
        this.completionRate = completionRate;
        this.completedToday = completedToday;
    }

    // Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getGoalValue() { return goalValue; }
    public void setGoalValue(int goalValue) { this.goalValue = goalValue; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    public boolean isCompletedToday() { return completedToday; }
    public void setCompletedToday(boolean completedToday) { this.completedToday = completedToday; }
}
