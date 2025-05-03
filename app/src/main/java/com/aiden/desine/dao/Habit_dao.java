package com.aiden.desine.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.aiden.desine.model.Habit_model;
import com.aiden.desine.database.DBHelper;
import java.util.ArrayList;
import java.util.List;

public class Habit_dao {
    private DBHelper dbHelper;

    public Habit_dao(Context context) {
        dbHelper = new DBHelper(context);
    }

    /** 插入新习惯 */
    public long insertHabit(Habit_model habit) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", habit.getName());
            values.put("unit", habit.getUnit());
            values.put("goal_value", habit.getGoalValue());
            values.put("frequency", habit.getFrequency());
            values.put("reminder_time", habit.getReminderTime());
            values.put("notes", habit.getNotes());
            values.put("streak", habit.getStreak());
            values.put("completion_rate", habit.getCompletionRate());
            values.put("completed_today", habit.isCompletedToday() ? 1 : 0);

            long newRowId = db.insertOrThrow("habits", null, values);
            return newRowId;
        } catch (Exception e) {
            Log.e("Habit_dao", "插入失败: " + e.getMessage());
            return -1;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /** 获取所有习惯 */
    public List<Habit_model> getAllHabits() {
        List<Habit_model> habitList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM habits", null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
                int goalValue = cursor.getInt(cursor.getColumnIndexOrThrow("goal_value"));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));
                String reminderTime = cursor.getString(cursor.getColumnIndexOrThrow("reminder_time"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                int streak = cursor.getInt(cursor.getColumnIndexOrThrow("streak"));
                double completionRate = cursor.getDouble(cursor.getColumnIndexOrThrow("completion_rate"));
                int completedTodayInt = cursor.getInt(cursor.getColumnIndexOrThrow("completed_today"));
                boolean completedToday = completedTodayInt == 1;

                Habit_model habit = new Habit_model(id, name, unit, goalValue, frequency,
                        reminderTime, notes, streak, completionRate, completedToday);
                habitList.add(habit);
            }
        } catch (Exception e) {
            Log.e("Habit_dao", "查询失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return habitList;
    }

    /** 根据 ID 获取单个习惯 */
    public Habit_model getHabitById(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM habits WHERE id = ?", new String[]{String.valueOf(id)});

            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
                int goalValue = cursor.getInt(cursor.getColumnIndexOrThrow("goal_value"));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));
                String reminderTime = cursor.getString(cursor.getColumnIndexOrThrow("reminder_time"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"));
                int streak = cursor.getInt(cursor.getColumnIndexOrThrow("streak"));
                double completionRate = cursor.getDouble(cursor.getColumnIndexOrThrow("completion_rate"));
                int completedTodayInt = cursor.getInt(cursor.getColumnIndexOrThrow("completed_today"));
                boolean completedToday = completedTodayInt == 1;

                return new Habit_model(id, name, unit, goalValue, frequency, reminderTime,
                        notes, streak, completionRate, completedToday);
            }
        } catch (Exception e) {
            Log.e("Habit_dao", "查询失败: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    /** 更新习惯 */
    public int updateHabit(Habit_model habit) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", habit.getName());
            values.put("unit", habit.getUnit());
            values.put("goal_value", habit.getGoalValue());
            values.put("frequency", habit.getFrequency());
            values.put("reminder_time", habit.getReminderTime());
            values.put("notes", habit.getNotes());
            values.put("streak", habit.getStreak());
            values.put("completion_rate", habit.getCompletionRate());
            values.put("completed_today", habit.isCompletedToday() ? 1 : 0);

            int rowsAffected = db.update("habits", values, "id = ?", new String[]{String.valueOf(habit.getId())});
            return rowsAffected;
        } catch (Exception e) {
            Log.e("Habit_dao", "更新失败: " + e.getMessage());
            return 0;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /** 删除习惯 */
    public int deleteHabit(Habit_model habit) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            int rowsAffected = db.delete("habits", "id = ?", new String[]{String.valueOf(habit.getId())});
            return rowsAffected;
        } catch (Exception e) {
            Log.e("Habit_dao", "删除失败: " + e.getMessage());
            return 0;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /** 标记习惯为今日完成 */
    public void completeHabitForToday(Habit_model habit) {
        habit.setCompletedToday(true);
        habit.setStreak(habit.getStreak() + 1); // 简单递增 streak
        updateHabit(habit); // 更新数据库
    }
}
