package com.aiden.desine.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.aiden.desine.R;
import com.aiden.desine.dao.Habit_dao;
import com.aiden.desine.model.Habit_model;

public class add_habit_activity extends AppCompatActivity {

    private EditText habitName, habitUnit, habitGoalValue, habitFrequency, habitReminderTime, habitNotes;
    private Button saveButton;
    private Habit_dao habitDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_habit_item);

        habitDao = new Habit_dao(this);

        habitName = findViewById(R.id.habit_name);
        habitUnit = findViewById(R.id.habit_unit);
        habitGoalValue = findViewById(R.id.habit_goal_value);
        habitFrequency = findViewById(R.id.habit_frequency);
        habitReminderTime = findViewById(R.id.habit_reminder_time);
        habitNotes = findViewById(R.id.habit_notes);
        saveButton = findViewById(R.id.save_button);

        // 验证控件是否正确加载
        Log.d("add_habit_activity", "habitName: " + (habitName != null ? "found" : "null"));

        saveButton.setOnClickListener(v -> saveHabit());
    }

    private void saveHabit() {
        String name = habitName.getText().toString().trim();
        String unit = habitUnit.getText().toString().trim();
        String goalValueStr = habitGoalValue.getText().toString().trim();
        String frequency = habitFrequency.getText().toString().trim();
        String reminderTime = habitReminderTime.getText().toString().trim();
        String notes = habitNotes.getText().toString().trim();

        if (name.isEmpty() || unit.isEmpty() || goalValueStr.isEmpty() || frequency.isEmpty() || reminderTime.isEmpty()) {
            Toast.makeText(this, "请填写所有必填字段", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int goalValue = Integer.parseInt(goalValueStr);
            if (goalValue <= 0) {
                Toast.makeText(this, "目标值必须为正数", Toast.LENGTH_SHORT).show();
                return;
            }

            Habit_model newHabit = new Habit_model(0, name, unit, goalValue, frequency, reminderTime,
                    notes, 0, 0.0, false);
            long id = habitDao.insertHabit(newHabit);
            if (id != -1) {
                Toast.makeText(this, "习惯保存成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "目标值必须为数字", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("add_habit_activity", "保存失败: " + e.getMessage());
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
