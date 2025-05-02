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
import com.aiden.desine.R;
import com.aiden.desine.dao.User_dao;
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
    private User_dao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDao = new User_dao(this);
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
            if (!savedUsername.isEmpty() && userDao.getUser(savedUsername) != null) {
                Toast.makeText(this, "自动登录成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Home_activity.class));
                finish();
            }
        }
    }

    private void restoreLoginInfo() {
        if (sharedPreferences.getBoolean("remember_password", false)) {
            String savedUsername = sharedPreferences.getString("username", "");
            String savedPassword = sharedPreferences.getString("password", "");
            usernameInput.setText(savedUsername);
            passwordInput.setText(savedPassword); // 自动填充密码
            rememberPasswordCheckbox.setChecked(true);
        }
    }


    private void setupListeners() {
        loginButton.setOnClickListener(v -> onLoginClick());

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(Login_activity.this, login_rigist_activity.class));
        });

        forgotPasswordLink.setOnClickListener(v -> {
            startActivity(new Intent(Login_activity.this, login_forget_activity.class));
        });

        rememberPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                autoLoginCheckbox.setChecked(false); // 如果取消记住密码，自动登录也取消
            }
        });

        autoLoginCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rememberPasswordCheckbox.setChecked(true); // 自动登录必须记住密码
            }
        });
    }

    private void onLoginClick() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (validateInput(username, password)) {
            if (userDao.loginUser(username, password)) {
                saveLoginState(username, password); // 保存用户名和密码
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Home_activity.class));
                finish();
            } else {
                showToast("用户名或密码错误");
            }
        }
    }


    private void saveLoginState(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (rememberPasswordCheckbox.isChecked()) {
            editor.putString("username", username);
            editor.putString("password", password); // 保存密码
        } else {
            editor.remove("username");
            editor.remove("password"); // 未勾选时清除数据
        }
        editor.putBoolean("remember_password", rememberPasswordCheckbox.isChecked());
        editor.apply();
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




    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}