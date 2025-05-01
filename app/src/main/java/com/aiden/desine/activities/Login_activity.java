package com.aiden.desine.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.aiden.desine.databinding.ActivityLoginBinding;

public class Login_activity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private CheckBox rememberPasswordCheckbox;
    private CheckBox autoLoginCheckbox;
    private MaterialButton loginButton;
    private View forgotPasswordLink;
    private View registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initSharedPreferences();
        checkAutoLogin();
        restoreLoginInfo();
        setupListeners();
    }

    private void initViews() {
        usernameInput = binding.username;
        passwordInput = binding.password;
        rememberPasswordCheckbox = binding.rememberPassword;
        autoLoginCheckbox = binding.autoLogin;
        loginButton = binding.loginButton;
        forgotPasswordLink = binding.forgotPassword;
        registerLink = binding.registerLink;
    }

    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
    }

    private void checkAutoLogin() {
        if (sharedPreferences.getBoolean("auto_login", false)) {
            String savedUsername = sharedPreferences.getString("username", "");
            String savedPassword = sharedPreferences.getString("password", "");
            if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
                performLogin(savedUsername, savedPassword);
            }
        }
    }

    private void restoreLoginInfo() {
        if (sharedPreferences.getBoolean("remember_password", false)) {
            usernameInput.setText(sharedPreferences.getString("username", ""));
            passwordInput.setText(sharedPreferences.getString("password", ""));
            rememberPasswordCheckbox.setChecked(true);
        }
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> onLoginClick());

        registerLink.setOnClickListener(v -> {
            // TODO: 跳转到注册页面
            // startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        forgotPasswordLink.setOnClickListener(v -> {
            // TODO: 跳转到忘记密码页面
            // startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        rememberPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                autoLoginCheckbox.setChecked(false);
            }
        });

        autoLoginCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rememberPasswordCheckbox.setChecked(true);
            }
        });
    }

    private void onLoginClick() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (validateInput(username, password)) {
            performLogin(username, password);
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            showToast("请输入用户名");
            return false;
        }
        if (password.isEmpty()) {
            showToast("请输入密码");
            return false;
        }
        return true;
    }

    private void performLogin(String username, String password) {
        // TODO: 实现实际的登录逻辑，这里只是示例
        if ("admin".equals(username) && "123456".equals(password)) {
            saveLoginState(username, password);
            startActivity(new Intent(this, Home_activity.class));
            finish();
        } else {
            showToast("用户名或密码错误");
        }
    }

    private void saveLoginState(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("remember_password", rememberPasswordCheckbox.isChecked());
        editor.putBoolean("auto_login", autoLoginCheckbox.isChecked());
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
