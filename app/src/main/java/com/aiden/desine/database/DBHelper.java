package com.aiden.desine.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "GrowthApp.db";
    public static final int DATABASE_VERSION = 3; // 增加版本号以触发升级
    private static final String TAG = "DBHelper";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "phone TEXT NOT NULL UNIQUE, " +
                "email TEXT NOT NULL, " +
                "profile_picture_path TEXT, " +
                "remember_password INTEGER DEFAULT 0, " +
                "auto_login INTEGER DEFAULT 0)");
        Log.d(TAG, "创建 users 表成功，包含 profile_picture_path 列");

        // 创建日程表 (更新后的结构)
        db.execSQL("CREATE TABLE schedules (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "date TEXT NOT NULL, " +
                "start_time TEXT NOT NULL, " +
                "end_time TEXT, " +
                "repeat_type TEXT DEFAULT '不重复', " +
                "reminder_time TEXT DEFAULT '无提醒', " +
                "category TEXT DEFAULT '工作', " +
                "priority TEXT DEFAULT '中', " +
                "is_completed INTEGER DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
        Log.d(TAG, "创建 schedules 表成功，包含扩展字段");

        // 创建习惯表
        db.execSQL("CREATE TABLE habits (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "name TEXT, " +
                "checked_dates TEXT, " + // 用JSON字符串存储已打卡日期
                "FOREIGN KEY(user_id) REFERENCES users(id))");
        Log.d(TAG, "创建 habits 表成功");

        // 创建备忘录表（可选）
        db.execSQL("CREATE TABLE memos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "content TEXT, " +
                "created_at TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
        Log.d(TAG, "创建 memos 表成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "数据库升级：从版本 " + oldVersion + " 到版本 " + newVersion);
        
        if (oldVersion < 2) {
            // 检查并添加 profile_picture_path 列
            try {
                db.execSQL("ALTER TABLE users ADD COLUMN profile_picture_path TEXT");
                Log.d(TAG, "升级成功：为 users 表添加 profile_picture_path 列");
            } catch (Exception e) {
                Log.e(TAG, "升级失败：添加 profile_picture_path 列时出错: " + e.getMessage(), e);
            }
        }
        
        if (oldVersion < 3) {
            // 更新schedules表结构，添加新字段
            try {
                // 创建临时表
                db.execSQL("CREATE TABLE schedules_temp (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT, " +
                        "date TEXT NOT NULL, " +
                        "start_time TEXT NOT NULL, " +
                        "end_time TEXT, " +
                        "repeat_type TEXT DEFAULT '不重复', " +
                        "reminder_time TEXT DEFAULT '无提醒', " +
                        "category TEXT DEFAULT '工作', " +
                        "priority TEXT DEFAULT '中', " +
                        "is_completed INTEGER DEFAULT 0, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(user_id) REFERENCES users(id))");
                
                // 复制旧数据到新表
                db.execSQL("INSERT INTO schedules_temp (id, user_id, title, description, date) " +
                        "SELECT id, user_id, title, content, date FROM schedules");
                
                // 删除旧表
                db.execSQL("DROP TABLE schedules");
                
                // 重命名新表
                db.execSQL("ALTER TABLE schedules_temp RENAME TO schedules");
                
                Log.d(TAG, "升级成功：更新 schedules 表结构");
            } catch (Exception e) {
                Log.e(TAG, "升级失败：更新 schedules 表结构时出错: " + e.getMessage(), e);
            }
        }
    }
    
    // 添加一个新日程
    public static final String TABLE_SCHEDULES = "schedules";
    public static final String COL_ID = "id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";
    public static final String COL_START_TIME = "start_time";
    public static final String COL_END_TIME = "end_time";
    public static final String COL_REPEAT_TYPE = "repeat_type";
    public static final String COL_REMINDER_TIME = "reminder_time";
    public static final String COL_CATEGORY = "category";
    public static final String COL_PRIORITY = "priority";
    public static final String COL_IS_COMPLETED = "is_completed";
    public static final String COL_CREATED_AT = "created_at";
}
