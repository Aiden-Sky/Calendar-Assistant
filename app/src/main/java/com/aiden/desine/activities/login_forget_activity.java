package com.aiden.desine.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.aiden.desine.R;

public class login_forget_activity extends AppCompatActivity {
    private TextInputEditText usernameInput;
    private TextInputEditText phoneInput;
    private MaterialButton submitUserPhoneButton;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmNewPasswordInput;
    private MaterialButton submitNewPasswordButton;
    private MaterialButton exitButton; // 新增退出按钮
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_subpage_forget);

        initViews();
        setupListeners();
    }

    private void initViews() {
        usernameInput = findViewById(R.id.username);
        phoneInput = findViewById(R.id.phone);
        submitUserPhoneButton = findViewById(R.id.submit_user_phone_button);
        newPasswordInput = findViewById(R.id.new_password);
        confirmNewPasswordInput = findViewById(R.id.confirm_new_password);
        submitNewPasswordButton = findViewById(R.id.submit_new_password_button);
        exitButton = findViewById(R.id.exit_button); // 初始化退出按钮
    }

    private void setupListeners() {
        submitUserPhoneButton.setOnClickListener(v -> onSubmitUserPhoneClick());
        submitNewPasswordButton.setOnClickListener(v -> onSubmitNewPasswordClick());
        exitButton.setOnClickListener(v -> finish()); // 点击退出，返回 Login_activity
    }

    private void onSubmitUserPhoneClick() {
        String username = usernameInput.getText().toString();
        String phone = phoneInput.getText().toString();

        if (validateUserPhone(username, phone)) {
            if (checkUsernameAndPhone(username, phone)) {
                this.username = username;
                findViewById(R.id.new_password_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.confirm_new_password_layout).setVisibility(View.VISIBLE);
                submitNewPasswordButton.setVisibility(View.VISIBLE);
                findViewById(R.id.username_layout).setVisibility(View.GONE);
                findViewById(R.id.phone_layout).setVisibility(View.GONE);
                submitUserPhoneButton.setVisibility(View.GONE);
            } else {
                showToast("用户名或电话错误");
            }
        }
    }

    private void onSubmitNewPasswordClick() {
        String newPassword = newPasswordInput.getText().toString();
        String confirmNewPassword = confirmNewPasswordInput.getText().toString();

        if (validatePassword(newPassword, confirmNewPassword)) {
            updatePassword(username, newPassword);
            Toast.makeText(this, "密码已成功重置", Toast.LENGTH_SHORT).show();
            finish(); // 返回登录界面
        }
    }

    private boolean validateUserPhone(String username, String phone) {
        if (username.isEmpty()) {
            showToast("请输入用户名");
            return false;
        }
        if (phone.isEmpty()) {
            showToast("请输入电话");
            return false;
        }
        return true;
    }

    private boolean checkUsernameAndPhone(String username, String phone) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String storedPhone = sharedPreferences.getString(username + "_phone", null);
        return storedPhone != null && storedPhone.equals(phone);
    }

    private boolean validatePassword(String newPassword, String confirmNewPassword) {
        if (newPassword.isEmpty()) {
            showToast("请输入新密码");
            return false;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            showToast("两次输入的密码不一致");
            return false;
        }
        return true;
    }

    private void updatePassword(String username, String newPassword) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(username, newPassword);
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
