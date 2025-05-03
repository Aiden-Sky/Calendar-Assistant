package com.aiden.desine.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.aiden.desine.database.DBHelper;
import com.aiden.desine.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import android.util.Base64;

public class User_dao {
    private static final String TAG = "User_dao";  // 统一日志标签
    private DBHelper dbHelper;
    private Context context;

    // SharedPreferences 相关常量（与 PersonalFragment 保持一致）
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USERNAME = "username";

    public User_dao(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
    }

    // 获取当前登录用户的用户名
    public String getCurrentUsername() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, null);
        if (username == null) {
            Log.e(TAG, "未找到登录用户名");
        } else {
            Log.d(TAG, "获取到的用户名: " + username);
        }
        return username;
    }

    // 获取当前登录用户的信息
    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            Log.e(TAG, "无法获取当前用户，用户名为空");
            return null;
        }
        return getUser(username);
    }

    // 注册用户
    public boolean registerUser(User user) {
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
            values.put("profile_picture_path", user.getProfilePicturePath());
            values.put("remember_password", 0);
            values.put("auto_login", 0);

            long result = db.insert("users", null, values);
            if (result == -1) {
                Log.e(TAG, "注册用户失败: " + user.getUsername());
                return false;
            }
            Log.d(TAG, "注册用户成功: " + user.getUsername());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "注册用户异常: " + e.getMessage(), e);
            return false;
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
                boolean success = storedPasswordHash.equals(inputPasswordHash);
                if (success) {
                    Log.d(TAG, "登录成功: " + username);
                } else {
                    Log.w(TAG, "登录失败: 密码错误");
                }
                return success;
            } else {
                Log.w(TAG, "登录失败: 用户名不存在 - " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "登录异常: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    // 更新用户信息
    public boolean updateUser(String oldUsername, User updatedUser) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String newUsername = updatedUser.getUsername();
            String newPhone = updatedUser.getPhone();
            String newEmail = updatedUser.getEmail();
            String newPassword = updatedUser.getPassword(); // 如果不为空，则更新密码

            // 检查新用户名是否已存在（如果用户名改变）
            if (!oldUsername.equals(newUsername)) {
                cursor = db.query("users", new String[]{"username"}, "username = ?",
                        new String[]{newUsername}, null, null, null);
                if (cursor.moveToFirst()) {
                    Log.w(TAG, "新用户名已存在: " + newUsername);
                    return false; // 新用户名已存在
                }
                cursor.close();
                cursor = null;
            }

            // 获取当前用户的电话进行比较
            String currentPhone = null;
            cursor = db.query("users", new String[]{"phone"}, "username = ?",
                    new String[]{oldUsername}, null, null, null);
            if (cursor.moveToFirst()) {
                currentPhone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            } else {
                Log.w(TAG, "未找到用户: " + oldUsername);
                return false;
            }
            cursor.close();
            cursor = null;

            // 检查新电话是否已存在（如果电话改变）
            if (!newPhone.equals(currentPhone)) {
                cursor = db.query("users", new String[]{"phone"}, "phone = ?",
                        new String[]{newPhone}, null, null, null);
                if (cursor.moveToFirst()) {
                    Log.w(TAG, "新电话已存在: " + newPhone);
                    return false; // 新电话已存在
                }
                cursor.close();
                cursor = null;
            }

            // 准备更新的值
            ContentValues values = new ContentValues();
            values.put("username", newUsername);
            values.put("phone", newPhone);
            values.put("email", newEmail);

            // 如果提供了新密码，更新密码
            if (newPassword != null && !newPassword.isEmpty()) {
                String newSalt = generateSalt();
                String newPasswordHash = hashPassword(newPassword, newSalt);
                values.put("password_hash", newPasswordHash);
                values.put("salt", newSalt);
                Log.d(TAG, "将更新密码");
            }

            // 执行更新
            int rowsAffected = db.update("users", values, "username = ?", new String[]{oldUsername});
            if (rowsAffected > 0) {
                Log.d(TAG, "更新用户信息成功: oldUsername=" + oldUsername + ", newUsername=" + newUsername);
                // 如果用户名改变，更新 SharedPreferences
                if (!oldUsername.equals(newUsername)) {
                    updateCurrentUsername(newUsername);
                }
                return true;
            } else {
                Log.w(TAG, "更新用户信息失败: 未找到用户 " + oldUsername);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "更新用户信息异常: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    // 获取用户信息（指定用户名）
    public User getUser(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("users", null, "username = ?", new String[]{username}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                user.setProfilePicturePath(cursor.getString(cursor.getColumnIndexOrThrow("profile_picture_path")));
                Log.d(TAG, "获取用户成功: " + username);
                return user;
            } else {
                Log.w(TAG, "未找到用户: " + username);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取用户异常: " + e.getMessage(), e);
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
                boolean match = storedPhone.equals(phone);
                if (match) {
                    Log.d(TAG, "用户名和电话匹配: " + username);
                } else {
                    Log.w(TAG, "电话不匹配: " + username);
                }
                return match;
            } else {
                Log.w(TAG, "未找到用户: " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "验证用户名和电话异常: " + e.getMessage(), e);
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
            if (rowsAffected > 0) {
                Log.d(TAG, "更新密码成功: " + username);
                return true;
            } else {
                Log.w(TAG, "更新密码失败: 未找到用户 " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "更新密码异常: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // 更新用户头像路径
    public boolean updateProfilePicturePath(String username, String path) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("profile_picture_path", path);
            int rowsAffected = db.update("users", values, "username = ?", new String[]{username});
            if (rowsAffected > 0) {
                Log.d(TAG, "更新头像路径成功: username=" + username + ", path=" + path);
                return true;
            } else {
                Log.w(TAG, "更新头像路径失败: 未找到用户 " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "更新头像路径异常: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // 更新用户信息（用户名和邮箱）
    public boolean updateUserInfo(String oldUsername, String newUsername, String newEmail) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // 如果新用户名与旧用户名不同，检查新用户名是否已被占用
            if (!oldUsername.equals(newUsername)) {
                Cursor cursor = db.query("users", new String[]{"username"}, "username = ?",
                        new String[]{newUsername}, null, null, null);
                if (cursor.moveToFirst()) {
                    Log.w(TAG, "新用户名已存在: " + newUsername);
                    cursor.close();
                    return false; // 新用户名已存在，更新失败
                }
                cursor.close();
            }

            // 更新数据库中的用户名和邮箱
            ContentValues values = new ContentValues();
            values.put("username", newUsername);
            values.put("email", newEmail);

            int rowsAffected = db.update("users", values, "username = ?", new String[]{oldUsername});
            if (rowsAffected > 0) {
                Log.d(TAG, "更新用户信息成功: oldUsername=" + oldUsername + ", newUsername=" + newUsername);

                // 如果用户名发生变化，更新 SharedPreferences
                if (!oldUsername.equals(newUsername)) {
                    updateCurrentUsername(newUsername);
                }
                return true;
            } else {
                Log.w(TAG, "更新用户信息失败: 未找到用户 " + oldUsername);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "更新用户信息异常: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // 更新 SharedPreferences 中的当前用户名
    private void updateCurrentUsername(String newUsername) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, newUsername);
        editor.apply();
        Log.d(TAG, "更新 SharedPreferences 中的用户名为: " + newUsername);
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
