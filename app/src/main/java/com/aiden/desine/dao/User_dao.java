package com.aiden.desine.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.aiden.desine.database.DBHelper;
import com.aiden.desine.model.User_model;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import android.util.Base64;

public class User_dao {
    private DBHelper dbHelper;

    public User_dao(Context context) {
        dbHelper = new DBHelper(context);
    }

    // 注册用户
    public boolean registerUser(User_model user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String salt = generateSalt();
            String passwordHash = hashPassword(user.getPassword(), salt);

            ContentValues values = new ContentValues();
            values.put("username", user.getUsername());
            values.put("password_hash", passwordHash);
            values.put("salt", salt);
            values.put("phone", user.getPhone());
            values.put("email", user.getEmail());
            values.put("remember_password", 0);
            values.put("auto_login", 0);

            long result = db.insert("users", null, values);
            return result != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 用户名或电话重复时会返回 false
        } finally {
            db.close();
        }
    }

    // 登录用户
    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("users", new String[]{"password_hash", "salt"}, "username = ?",
                    new String[]{username}, null, null, null);
            if (cursor.moveToFirst()) {
                String storedPasswordHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
                String salt = cursor.getString(cursor.getColumnIndexOrThrow("salt"));
                String inputPasswordHash = hashPassword(password, salt);
                return storedPasswordHash.equals(inputPasswordHash);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    // 获取用户信息
    public User_model getUser(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("users", null, "username = ?", new String[]{username}, null, null, null);
            if (cursor.moveToFirst()) {
                User_model user = new User_model();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                return user;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    // 验证用户名和电话（用于忘记密码）
    public boolean checkUsernameAndPhone(String username, String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("users", new String[]{"phone"}, "username = ?",
                    new String[]{username}, null, null, null);
            if (cursor.moveToFirst()) {
                String storedPhone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                return storedPhone.equals(phone);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    // 更新用户密码（用于忘记密码）
    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String newSalt = generateSalt();
            String newPasswordHash = hashPassword(newPassword, newSalt);

            ContentValues values = new ContentValues();
            values.put("password_hash", newPasswordHash);
            values.put("salt", newSalt);

            int rowsAffected = db.update("users", values, "username = ?", new String[]{username});
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    // 生成盐
    private String generateSalt() {
        return UUID.randomUUID().toString();
    }

    // 哈希密码
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
