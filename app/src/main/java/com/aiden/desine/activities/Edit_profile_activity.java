package com.aiden.desine.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aiden.desine.R;
import com.aiden.desine.dao.User_dao;
import com.aiden.desine.model.User;

public class Edit_profile_activity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private EditText editTextUsername, editTextPassword, editTextConfirmPassword, editTextPhone, editTextEmail;
    private Button buttonSave;
    private User_dao userDao;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 初始化控件
        editTextUsername = findViewById(R.id.editText_username);
        editTextPassword = findViewById(R.id.editText_password);
        editTextConfirmPassword = findViewById(R.id.editText_confirm_password);
        editTextPhone = findViewById(R.id.editText_phone);
        editTextEmail = findViewById(R.id.editText_email);
        buttonSave = findViewById(R.id.button_save);

        // 初始化 User_dao
        userDao = new User_dao(this);

        // 从 SharedPreferences 获取当前用户名
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUsername = prefs.getString("username", null);
        if (currentUsername == null) {
            Toast.makeText(this, getString(R.string.error_not_logged_in), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 加载当前用户信息
        loadUserInfo();

        // 设置保存按钮点击事件
        buttonSave.setOnClickListener(v -> saveUserInfo());
    }

    /** 加载用户信息到输入框 */
    private void loadUserInfo() {
        User user = userDao.getUser(currentUsername);
        if (user != null) {
            editTextUsername.setText(user.getUsername());
            editTextPhone.setText(user.getPhone());
            editTextEmail.setText(user.getEmail());
            // 不预填密码，保持为空
            Log.d(TAG, "用户信息加载成功: " + user.getUsername());
        } else {
            Toast.makeText(this, getString(R.string.error_load_user_info), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /** 保存用户信息 */
    private void saveUserInfo() {
        String newUsername = editTextUsername.getText().toString().trim();
        String newPassword = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String newPhone = editTextPhone.getText().toString().trim();
        String newEmail = editTextEmail.getText().toString().trim();

        // 输入验证
        if (!validateInputs(newUsername, newPhone, newEmail, newPassword, confirmPassword)) {
            return;
        }

        // 创建更新后的用户对象
        User updatedUser = new User();
        updatedUser.setUsername(newUsername);
        updatedUser.setPhone(newPhone);
        updatedUser.setEmail(newEmail);
        if (!newPassword.isEmpty()) {
            updatedUser.setPassword(newPassword);
        }

        // 更新数据库
        boolean success = userDao.updateUser(currentUsername, updatedUser);
        if (success) {
            Toast.makeText(this, getString(R.string.success_profile_update), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.error_profile_update), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "用户信息更新失败");
        }
    }

    /** 验证输入的有效性 */
    private boolean validateInputs(String username, String phone, String email, String password, String confirmPassword) {
        // 检查是否为空
        if (username.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_username_empty), Toast.LENGTH_SHORT).show();
            editTextUsername.requestFocus();
            return false;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_phone_empty), Toast.LENGTH_SHORT).show();
            editTextPhone.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_email_empty), Toast.LENGTH_SHORT).show();
            editTextEmail.requestFocus();
            return false;
        }

        // 电话格式验证
        if (!Patterns.PHONE.matcher(phone).matches()) {
            Toast.makeText(this, getString(R.string.error_invalid_phone), Toast.LENGTH_SHORT).show();
            editTextPhone.requestFocus();
            return false;
        }

        // 邮箱格式验证
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
            editTextEmail.requestFocus();
            return false;
        }

        // 密码一致性验证
        if (!password.isEmpty() || !confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show();
                editTextConfirmPassword.requestFocus();
                return false;
            }
            if (password.length() < 6) {
                Toast.makeText(this, getString(R.string.error_password_length), Toast.LENGTH_SHORT).show();
                editTextPassword.requestFocus();
                return false;
            }
        }

        return true;
    }

    /** 处理返回时的过渡动画 */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void finish() {
        super.finish();
        // 在保存成功或其他退出时添加返回动画
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
