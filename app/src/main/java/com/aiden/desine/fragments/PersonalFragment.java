package com.aiden.desine.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.aiden.desine.R;
import com.aiden.desine.activities.Edit_profile_activity;
import com.aiden.desine.activities.Login_activity;
import com.aiden.desine.dao.User_dao;
import com.aiden.desine.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersonalFragment extends Fragment {
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final String TAG = "PersonalFragment";
    private ImageView avatarImageView;
    private User_dao userDao;
    private String currentUsername;
    private String profilePictureDir;
    private ExecutorService executorService;
    private String currentLanguage; // 用于追踪当前语言

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView 开始");
        View view = inflater.inflate(R.layout.fragment_personal, container, false);

        // 初始化控件
        avatarImageView = view.findViewById(R.id.avatar_image_view);
        setupNightMode(view);
        Spinner languageSpinner = view.findViewById(R.id.language_spinner);
        Button editProfileButton = view.findViewById(R.id.edit_profile_button);

        // 设置头像点击事件
        avatarImageView.setOnClickListener(v -> pickImage());

        // 检查控件初始化
        if (avatarImageView == null || languageSpinner == null || editProfileButton == null) {
            Log.e(TAG, "控件初始化失败");
            Toast.makeText(getContext(), "页面加载失败", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 初始化逻辑
        userDao = new User_dao(getContext());
        profilePictureDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        executorService = Executors.newSingleThreadExecutor();

        currentUsername = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("username", null);
        Log.d(TAG, "获取到的用户名: " + currentUsername);
        if (currentUsername == null) {
            Toast.makeText(getContext(), "未登录用户，请重新登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), Login_activity.class));
            return view;
        }

        // 异步加载头像
        executorService.execute(this::loadUserAvatar);

        // 设置语言选择下拉框
        String[] languages = {"中文", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // 初始化当前语言
        currentLanguage = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("language", Locale.getDefault().getLanguage().equals("zh") ? "中文" : "English");
        languageSpinner.setSelection(currentLanguage.equals("中文") ? 0 : 1);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = languages[position];
                // 仅当选择的新语言与当前语言不同时切换
                if (!selectedLanguage.equals(currentLanguage)) {
                    Log.d(TAG, "用户选择切换语言: " + selectedLanguage);
                    changeLanguage(selectedLanguage);
                } else {
                    Log.d(TAG, "语言未改变，无需切换: " + selectedLanguage);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 设置修改个人信息按钮
        editProfileButton.setOnClickListener(v -> {
            Log.d(TAG, "点击修改个人信息");
            startActivity(new Intent(getActivity(), Edit_profile_activity.class));
        });

        Log.d(TAG, "onCreateView 结束");
        return view;
    }

    private void setupNightMode(View view) {
        SwitchMaterial nightModeSwitch = view.findViewById(R.id.night_mode_switch);
        
        // 获取当前夜间模式状态
        boolean isNightMode = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getBoolean("night_mode", false);
        
        // 设置开关初始状态
        nightModeSwitch.setChecked(isNightMode);
        
        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存状态到 SharedPreferences
            getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("night_mode", isChecked)
                    .apply();

            // 在重新创建活动之前保存要显示的页面信息
            getActivity().getSharedPreferences("navigation_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("default_page", R.id.nav_mine)
                    .apply();
            
            // 应用夜间模式
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            
            // 重新创建activity以应用新主题
            getActivity().recreate();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 确保开关状态与实际主题同步
        if (getView() != null) {
            SwitchMaterial nightModeSwitch = getView().findViewById(R.id.night_mode_switch);
            boolean isNightMode = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .getBoolean("night_mode", false);
            nightModeSwitch.setChecked(isNightMode);
        }
    }

    private void changeLanguage(String language) {
        try {
            Log.d(TAG, "开始切换语言: " + language);
            Locale locale = language.equals("中文") ? Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH;
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

            // 保存新语言并更新 currentLanguage
            getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("language", language)
                    .apply();
            currentLanguage = language;

            // 使用新的方式重启Activity以保留导航状态
            Intent intent = getActivity().getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            getActivity().finish();
            getActivity().startActivity(intent);
            
            Log.d(TAG, "语言切换完成");
        } catch (Exception e) {
            Log.e(TAG, "语言切换异常: " + e.getMessage(), e);
            Toast.makeText(getContext(), "语言切换失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserAvatar() {
        Log.d(TAG, "loadUserAvatar 开始");
        try {
            User user = userDao.getUser(currentUsername);
            if (user != null && user.getProfilePicturePath() != null) {
                String path = user.getProfilePicturePath();
                Log.d(TAG, "加载头像路径: " + path);
                File file = new File(path);
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (bitmap != null && getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            avatarImageView.setImageBitmap(bitmap);
                            Log.d(TAG, "头像加载成功");
                        });
                    } else {
                        Log.e(TAG, "图片解码失败，路径: " + path);
                        setDefaultAvatar();
                    }
                } else {
                    Log.e(TAG, "头像文件不存在: " + path);
                    setDefaultAvatar();
                }
            } else {
                Log.d(TAG, "用户无头像路径或用户信息为空");
                setDefaultAvatar();
            }
        } catch (Exception e) {
            Log.e(TAG, "加载头像异常: " + e.getMessage(), e);
            setDefaultAvatar();
        }
        Log.d(TAG, "loadUserAvatar 结束");
    }

    private void setDefaultAvatar() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() -> avatarImageView.setImageResource(R.drawable.ic_launcher_foreground));
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Log.d(TAG, "选择的图片URI: " + imageUri);
            try {
                String fileName = "user_" + currentUsername + "_" + System.currentTimeMillis() + ".jpg";
                String filePath = profilePictureDir + "/" + fileName;
                Log.d(TAG, "保存图片路径: " + filePath);

                saveImageToFile(imageUri, filePath);

                File file = new File(filePath);
                if (!file.exists()) {
                    Log.e(TAG, "图片文件不存在: " + filePath);
                    Toast.makeText(getContext(), "图片保存失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "图片保存成功: " + filePath);

                boolean success = userDao.updateProfilePicturePath(currentUsername, filePath);
                if (success) {
                    Log.d(TAG, "数据库更新成功");
                    executorService.execute(this::loadUserAvatar);
                    Toast.makeText(getContext(), "头像上传成功", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "数据库更新失败");
                    Toast.makeText(getContext(), "头像上传失败，请重试", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e(TAG, "保存图片失败: " + e.getMessage(), e);
                Toast.makeText(getContext(), "保存图片失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "onActivityResult: 无效的请求或结果");
        }
    }

    private void saveImageToFile(Uri imageUri, String filePath) throws IOException {
        InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            Log.e(TAG, "无法打开输入流，URI: " + imageUri);
            throw new IOException("无法打开输入流");
        }
        FileOutputStream outputStream = new FileOutputStream(filePath);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        Log.d(TAG, "onDestroy 执行");
    }
}
