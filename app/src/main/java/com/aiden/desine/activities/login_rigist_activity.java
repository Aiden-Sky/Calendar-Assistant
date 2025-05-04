package com.aiden.desine.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
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
    private ProgressDialog progressDialog;

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
            usernameInput.setError(getString(R.string.error_username_empty));
            return false;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameInput.setError(getString(R.string.error_invalid_username));
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.error_field_required));
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError(getString(R.string.error_password_length));
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError(getString(R.string.error_password_mismatch));
            return false;
        }

        if (phone.isEmpty()) {
            phoneInput.setError(getString(R.string.error_phone_empty));
            return false;
        }

        if (!Patterns.PHONE.matcher(phone).matches()) {
            phoneInput.setError(getString(R.string.error_invalid_phone));
            return false;
        }

        if (email.isEmpty()) {
            emailInput.setError(getString(R.string.error_email_empty));
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.error_invalid_email));
            return false;
        }

        return true;
    }
}
