package com.aiden.desine.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.aiden.desine.R;

public class login_rigist_activity extends AppCompatActivity {
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputEditText phoneInput;
    private TextInputEditText emailInput;
    private MaterialButton registerButton;
    private MaterialButton exitButton; // 新增退出按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_subpage_regist);

        initViews();
        setupListeners();
    }

    private void initViews() {
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        confirmPasswordInput = findViewById(R.id.confirm_password);
        phoneInput = findViewById(R.id.phone);
        emailInput = findViewById(R.id.email);
        registerButton = findViewById(R.id.register_button);
        exitButton = findViewById(R.id.exit_button); // 初始化退出按钮
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> onRegisterClick());
        exitButton.setOnClickListener(v -> finish()); // 点击退出按钮，返回上一个页面
    }

    private void onRegisterClick() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String phone = phoneInput.getText().toString();
        String email = emailInput.getText().toString();

        if (validateInput(username, password, confirmPassword, phone, email)) {
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            if (sharedPreferences.contains(username)) {
                showToast("用户名已存在");
                return;
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(username, password);
            editor.putString(username + "_phone", phone);
            editor.putString(username + "_email", email);
            editor.apply();

            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            finish(); // 注册成功后返回登录页面
        }
    }

    private boolean validateInput(String username, String password, String confirmPassword, String phone, String email) {
        if (username.isEmpty()) {
            showToast("请输入用户名");
            return false;
        }
        if (password.isEmpty()) {
            showToast("请输入密码");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast("两次输入的密码不一致");
            return false;
        }
        if (phone.isEmpty()) {
            showToast("请输入电话");
            return false;
        }
        if (email.isEmpty()) {
            showToast("请输入邮箱");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
