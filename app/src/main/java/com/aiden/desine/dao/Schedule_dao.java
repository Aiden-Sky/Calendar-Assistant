package com.aiden.desine.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.aiden.desine.database.DBHelper;
import com.aiden.desine.model.Schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * 日程数据访问对象，负责与数据库交互
 */
public class Schedule_dao {
    private static final String TAG = "Schedule_dao";
    private DBHelper dbHelper;

    public Schedule_dao(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 添加新日程到数据库
     * @param schedule 日程对象
     * @return 新插入记录的ID，失败返回-1
     */
    public long addSchedule(Schedule schedule) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long newRowId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.COL_USER_ID, schedule.getUserId());
            values.put(DBHelper.COL_TITLE, schedule.getTitle());
            values.put(DBHelper.COL_DESCRIPTION, schedule.getDescription());
            values.put(DBHelper.COL_DATE, schedule.getDate());
            values.put(DBHelper.COL_START_TIME, schedule.getStartTime());
            values.put(DBHelper.COL_END_TIME, schedule.getEndTime());
            values.put(DBHelper.COL_REPEAT_TYPE, schedule.getRepeatType());
            values.put(DBHelper.COL_REMINDER_TIME, schedule.getReminderTime());
            values.put(DBHelper.COL_CATEGORY, schedule.getCategory());
            values.put(DBHelper.COL_PRIORITY, schedule.getPriority());
            values.put(DBHelper.COL_IS_COMPLETED, schedule.isCompleted() ? 1 : 0);

            newRowId = db.insert(DBHelper.TABLE_SCHEDULES, null, values);
            Log.d(TAG, "添加日程成功，ID: " + newRowId);
        } catch (Exception e) {
            Log.e(TAG, "添加日程失败: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return newRowId;
    }

    /**
     * 更新已有日程
     * @param schedule 日程对象
     * @return 是否成功更新
     */
    public boolean updateSchedule(Schedule schedule) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(DBHelper.COL_TITLE, schedule.getTitle());
            values.put(DBHelper.COL_DESCRIPTION, schedule.getDescription());
            values.put(DBHelper.COL_DATE, schedule.getDate());
            values.put(DBHelper.COL_START_TIME, schedule.getStartTime());
            values.put(DBHelper.COL_END_TIME, schedule.getEndTime());
            values.put(DBHelper.COL_REPEAT_TYPE, schedule.getRepeatType());
            values.put(DBHelper.COL_REMINDER_TIME, schedule.getReminderTime());
            values.put(DBHelper.COL_CATEGORY, schedule.getCategory());
            values.put(DBHelper.COL_PRIORITY, schedule.getPriority());
            values.put(DBHelper.COL_IS_COMPLETED, schedule.isCompleted() ? 1 : 0);

            String selection = DBHelper.COL_ID + " = ?";
            String[] selectionArgs = { String.valueOf(schedule.getId()) };

            int count = db.update(DBHelper.TABLE_SCHEDULES, values, selection, selectionArgs);
            success = count > 0;
            Log.d(TAG, "更新日程" + (success ? "成功" : "失败") + "，ID: " + schedule.getId());
        } catch (Exception e) {
            Log.e(TAG, "更新日程失败: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return success;
    }

    /**
     * 删除日程
     * @param scheduleId 日程ID
     * @return 是否成功删除
     */
    public boolean deleteSchedule(long scheduleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            String selection = DBHelper.COL_ID + " = ?";
            String[] selectionArgs = { String.valueOf(scheduleId) };

            int count = db.delete(DBHelper.TABLE_SCHEDULES, selection, selectionArgs);
            success = count > 0;
            Log.d(TAG, "删除日程" + (success ? "成功" : "失败") + "，ID: " + scheduleId);
        } catch (Exception e) {
            Log.e(TAG, "删除日程失败: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return success;
    }

    /**
     * 获取特定日期的所有日程
     * @param userId 用户ID
     * @param date 日期字符串，格式为yyyy-MM-dd
     * @return 日程列表
     */
    public List<Schedule> getSchedulesByDate(long userId, String date) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            String selection = DBHelper.COL_USER_ID + " = ? AND " + DBHelper.COL_DATE + " = ?";
            String[] selectionArgs = { String.valueOf(userId), date };

            Cursor cursor = db.query(
                    DBHelper.TABLE_SCHEDULES,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    DBHelper.COL_START_TIME + " ASC"
            );

            while (cursor.moveToNext()) {
                schedules.add(cursorToSchedule(cursor));
            }
            cursor.close();
            Log.d(TAG, "获取日期 " + date + " 的日程，共 " + schedules.size() + " 条");
        } catch (Exception e) {
            Log.e(TAG, "获取日程失败: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return schedules;
    }

    /**
     * 获取所有日程
     * @param userId 用户ID
     * @return 日程列表
     */
    public List<Schedule> getAllSchedules(long userId) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            String selection = DBHelper.COL_USER_ID + " = ?";
            String[] selectionArgs = { String.valueOf(userId) };

            Cursor cursor = db.query(
                    DBHelper.TABLE_SCHEDULES,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    DBHelper.COL_DATE + " ASC, " + DBHelper.COL_START_TIME + " ASC"
            );

            while (cursor.moveToNext()) {
                schedules.add(cursorToSchedule(cursor));
            }
            cursor.close();
            Log.d(TAG, "获取所有日程，共 " + schedules.size() + " 条");
        } catch (Exception e) {
            Log.e(TAG, "获取日程失败: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return schedules;
    }

    /**
     * 获取单个日程详情
     * @param scheduleId 日程ID
     * @return 日程对象，如果未找到则返回null
     */
    public Schedule getScheduleById(long scheduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Schedule schedule = null;

        try {
            String selection = DBHelper.COL_ID + " = ?";
            String[] selectionArgs = { String.valueOf(scheduleId) };

            Cursor cursor = db.query(
                    DBHelper.TABLE_SCHEDULES,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                schedule = cursorToSchedule(cursor);
            }
            cursor.close();
            Log.d(TAG, "获取日程详情，ID: " + scheduleId + (schedule != null ? "，成功" : "，未找到"));
        } catch (Exception e) {
            Log.e(TAG, "获取日程详情失败: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return schedule;
    }

    /**
     * 将Cursor转换为Schedule对象
     */
    private Schedule cursorToSchedule(Cursor cursor) {
        Schedule schedule = new Schedule();
        schedule.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ID)));
        schedule.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_USER_ID)));
        schedule.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TITLE)));
        schedule.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_DESCRIPTION)));
        schedule.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_DATE)));
        schedule.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_START_TIME)));
        schedule.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_END_TIME)));
        schedule.setRepeatType(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_REPEAT_TYPE)));
        schedule.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_REMINDER_TIME)));
        schedule.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CATEGORY)));
        schedule.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PRIORITY)));
        schedule.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_IS_COMPLETED)) == 1);
        
        try {
            schedule.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CREATED_AT)));
        } catch (IllegalArgumentException e) {
            // 处理旧版本数据库可能不存在该字段的情况
            schedule.setCreatedAt("");
        }
        
        return schedule;
    }
} 