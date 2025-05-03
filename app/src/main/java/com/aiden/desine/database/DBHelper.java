package com.aiden.desine.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "GrowthApp.db";
    public static final int DATABASE_VERSION = 2; // 增加版本号以触发升级
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

        // 创建日程表
        db.execSQL("CREATE TABLE schedules (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT, " +
                "content TEXT, " +
                "date TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
        Log.d(TAG, "创建 schedules 表成功");

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
        // 可根据需要添加更多版本升级逻辑
    }
}
