package com.aiden.desine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.aiden.desine.R;
import com.aiden.desine.dao.User_dao;
import com.aiden.desine.model.User;

public class login_rigist_activity extends AppCompatActivity {
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputEditText phoneInput;
    private TextInputEditText emailInput;
    private MaterialButton registerButton;
    private MaterialButton exitButton;
    private User_dao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_subpage_regist);

        userDao = new User_dao(this);
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
        exitButton = findViewById(R.id.exit_button);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> onRegisterClick());
        exitButton.setOnClickListener(v -> finish());
    }

    private void onRegisterClick() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String phone = phoneInput.getText().toString();
        String email = emailInput.getText().toString();

        if (validateInput(username, password, confirmPassword, phone, email)) {
            User user = new User(username, password, phone, email);
            if (userDao.registerUser(user)) {
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, Login_activity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "注册失败，用户名或电话已存在", Toast.LENGTH_SHORT).show();
            }
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
