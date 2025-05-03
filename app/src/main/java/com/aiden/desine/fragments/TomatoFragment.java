package com.aiden.desine.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aiden.desine.R;
import com.google.android.material.button.MaterialButton;

public class TomatoFragment extends Fragment {
    private TextView timerText;
    private MaterialButton startPauseButton;
    private MaterialButton stopButton;
    private ImageButton increaseTime;
    private ImageButton decreaseTime;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 25 * 60 * 1000; // 默认25分钟
    private long defaultTime = 25 * 60 * 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tomato, container, false);
        initViews(view);
        setupListeners();
        updateTimerText();
        return view;
    }

    private void initViews(View view) {
        timerText = view.findViewById(R.id.timer_text);
        startPauseButton = view.findViewById(R.id.btn_start_pause);
        stopButton = view.findViewById(R.id.btn_stop);
        increaseTime = view.findViewById(R.id.increase_time);
        decreaseTime = view.findViewById(R.id.decrease_time);
    }

    private void setupListeners() {
        startPauseButton.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        stopButton.setOnClickListener(v -> resetTimer());

        increaseTime.setOnClickListener(v -> adjustTime(5 * 60 * 1000)); // 增加5分钟
        decreaseTime.setOnClickListener(v -> adjustTime(-5 * 60 * 1000)); // 减少5分钟
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                startPauseButton.setText("开始");
                timeLeftInMillis = defaultTime;
                updateTimerText();
                // TODO: 添加提示音或通知
                showCompletionNotification();
            }
        }.start();

        isTimerRunning = true;
        startPauseButton.setText("暂停");
        increaseTime.setVisibility(View.INVISIBLE);
        decreaseTime.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        startPauseButton.setText("继续");
        increaseTime.setVisibility(View.VISIBLE);
        decreaseTime.setVisibility(View.VISIBLE);
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeftInMillis = defaultTime;
        updateTimerText();
        isTimerRunning = false;
        startPauseButton.setText("开始");
        increaseTime.setVisibility(View.VISIBLE);
        decreaseTime.setVisibility(View.VISIBLE);
    }

    private void adjustTime(long timeChange) {
        if (!isTimerRunning) {
            long newTime = timeLeftInMillis + timeChange;
            if (newTime >= 5 * 60 * 1000 && newTime <= 60 * 60 * 1000) { // 限制在5分钟到60分钟之间
                timeLeftInMillis = newTime;
                defaultTime = newTime;
                updateTimerText();
            }
        }
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(timeLeftFormatted);
    }

    private void showCompletionNotification() {
        // TODO: 实现完成提醒，可以是声音或系统通知
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timeLeftInMillis", timeLeftInMillis);
        outState.putLong("defaultTime", defaultTime);
        outState.putBoolean("isTimerRunning", isTimerRunning);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis");
            defaultTime = savedInstanceState.getLong("defaultTime");
            isTimerRunning = savedInstanceState.getBoolean("isTimerRunning");

            updateTimerText();
            if (isTimerRunning) {
                startTimer();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            // 暂停时保存当前状态，但不取消计时器
            isTimerRunning = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isTimerRunning) {
            // 恢复时重新启动计时器
            startTimer();
        }
    }



}
