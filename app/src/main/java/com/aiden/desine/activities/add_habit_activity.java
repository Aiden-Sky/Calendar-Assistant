package com.aiden.desine.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.aiden.desine.R;
import com.aiden.desine.dao.Habit_dao;
import com.aiden.desine.model.Habit_model;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class add_habit_activity extends AppCompatActivity {

    private TextInputEditText habitName, habitUnit, habitGoalValue, habitFrequency, habitReminderTime, habitNotes;
    private Button saveButton;
    private Habit_dao habitDao;
    private TextInputLayout habitReminderTimeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_habit_item);

        habitDao = new Habit_dao(this);

        // 初始化控件
        habitName = findViewById(R.id.habit_name);
        habitUnit = findViewById(R.id.habit_unit);
        habitGoalValue = findViewById(R.id.habit_goal_value);
        habitFrequency = findViewById(R.id.habit_frequency);
        habitReminderTime = findViewById(R.id.habit_reminder_time);
        habitNotes = findViewById(R.id.habit_notes);
        saveButton = findViewById(R.id.save_button);
        habitReminderTimeLayout = findViewById(R.id.habit_reminder_time_layout);

        // 验证控件是否正确加载
        Log.d("add_habit_activity", getString(R.string.debug_habit_name_found, 
              habitName != null ? "found" : "null"));

        // 设置末尾图标点击事件以选择时间
        habitReminderTimeLayout.setEndIconOnClickListener(v -> showTimePickerDialog());

        // 设置保存按钮点击事件
        saveButton.setOnClickListener(v -> saveHabit());
    }

    /** 显示时间选择对话框 */
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    habitReminderTime.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    /** 保存习惯 */
    private void saveHabit() {
        String name = habitName.getText().toString().trim();
        String unit = habitUnit.getText().toString().trim();
        String goalValueStr = habitGoalValue.getText().toString().trim();
        String frequency = habitFrequency.getText().toString().trim();
        String reminderTime = habitReminderTime.getText().toString().trim();
        String notes = habitNotes.getText().toString().trim();

        if (name.isEmpty() || unit.isEmpty() || goalValueStr.isEmpty() || frequency.isEmpty() || reminderTime.isEmpty()) {
            Toast.makeText(this, R.string.error_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int goalValue = Integer.parseInt(goalValueStr);
            if (goalValue <= 0) {
                Toast.makeText(this, R.string.error_goal_positive, Toast.LENGTH_SHORT).show();
                return;
            }

            Habit_model newHabit = new Habit_model(0, name, unit, goalValue, frequency, reminderTime,
                    notes, 0, 0.0, false);
            long id = habitDao.insertHabit(newHabit);
            if (id != -1) {
                Toast.makeText(this, R.string.habit_save_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.habit_save_failed, Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_goal_numeric, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("add_habit_activity", getString(R.string.habit_save_failed_with_error, e.getMessage()));
            Toast.makeText(this, getString(R.string.habit_save_failed_with_error, e.getMessage()), 
                         Toast.LENGTH_LONG).show();
        }
    }
}
