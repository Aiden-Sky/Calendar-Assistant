package com.aiden.desine.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aiden.desine.R;
import com.aiden.desine.activities.add_habit_activity;
import com.aiden.desine.activities.HabitAdapter;
import com.aiden.desine.dao.Habit_dao;
import com.aiden.desine.model.Habit_model;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class HabitFragment extends Fragment {

    private FloatingActionButton fabAddHabit;
    private RecyclerView habitsRecyclerView;
    private Button buttonSubmit;
    private Button buttonDelete;
    private TextView totalHabitsTextView;
    private TextView todayCompletedTextView;
    private TextView streakDaysTextView;
    private Habit_dao habitDao;
    private HabitAdapter habitAdapter;
    private List<Habit_model> habitList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.fragment_habit, container, false);
            Log.d("HabitFragment", "Layout inflated: " + (view != null));
        } catch (Exception e) {
            Log.e("HabitFragment", "Error inflating layout: " + e.getMessage());
            Toast.makeText(getContext(), "布局加载失败", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (view == null) {
            return null;
        }

        habitDao = new Habit_dao(getContext());

        // 找到视图
        fabAddHabit = view.findViewById(R.id.fab_add_habit);
        habitsRecyclerView = view.findViewById(R.id.habits_recycler_view);
        buttonSubmit = view.findViewById(R.id.button_submit);
        buttonDelete = view.findViewById(R.id.button_delete);
        totalHabitsTextView = view.findViewById(R.id.total_habits);
        todayCompletedTextView = view.findViewById(R.id.today_completed);
        streakDaysTextView = view.findViewById(R.id.streak_days);

        // 检查视图是否正确初始化
        if (fabAddHabit == null || habitsRecyclerView == null || buttonSubmit == null ||
                buttonDelete == null || totalHabitsTextView == null ||
                todayCompletedTextView == null || streakDaysTextView == null) {
            Log.e("HabitFragment", "Failed to find views");
            Toast.makeText(getContext(), "视图加载失败", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 初始化 RecyclerView
        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        habitAdapter = new HabitAdapter(habitList);
        habitsRecyclerView.setAdapter(habitAdapter);
        loadHabits();

        // 设置 FAB 点击事件
        fabAddHabit.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), add_habit_activity.class);
                startActivityForResult(intent, 1);
            } else {
                Log.e("HabitFragment", "Activity is null");
            }
        });

        // 设置“提交”按钮点击事件
        buttonSubmit.setOnClickListener(v -> {
            List<Habit_model> selected = habitAdapter.getSelectedHabits();
            if (selected.isEmpty()) {
                Toast.makeText(getContext(), "请先选择习惯", Toast.LENGTH_SHORT).show();
                return;
            }
            for (Habit_model habit : selected) {
                habitDao.completeHabitForToday(habit); // 标记为今日完成
            }
            habitAdapter.getSelectedHabits().clear();
            habitAdapter.notifyDataSetChanged();
            loadHabits(); // 重新加载习惯列表
            updateStatistics(); // 更新统计信息
            Toast.makeText(getContext(), "已提交选中的习惯", Toast.LENGTH_SHORT).show();
        });

        // 设置“删除”按钮点击事件
        buttonDelete.setOnClickListener(v -> {
            List<Habit_model> selected = habitAdapter.getSelectedHabits();
            if (selected.isEmpty()) {
                Toast.makeText(getContext(), "请先选择习惯", Toast.LENGTH_SHORT).show();
                return;
            }
            for (Habit_model habit : selected) {
                habitDao.deleteHabit(habit); // 删除习惯
            }
            habitAdapter.getSelectedHabits().clear();
            loadHabits(); // 重新加载习惯列表
            updateStatistics(); // 更新统计信息
            Toast.makeText(getContext(), "已删除选中的习惯", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHabits();
        updateStatistics(); // 确保每次恢复时更新统计
    }

    private void loadHabits() {
        try {
            habitList.clear();
            habitList.addAll(habitDao.getAllHabits());
            habitAdapter.notifyDataSetChanged();
            updateStatistics(); // 加载习惯后更新统计
        } catch (Exception e) {
            Log.e("HabitFragment", "Error loading habits: " + e.getMessage());
            Toast.makeText(getContext(), "加载习惯失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatistics() {
        if (habitList == null || habitList.isEmpty()) {
            totalHabitsTextView.setText("总习惯\n0");
            todayCompletedTextView.setText("今日完成\n0");
            streakDaysTextView.setText("最长连续\n0天");
            return;
        }

        int totalHabits = habitList.size();
        int todayCompleted = 0;
        int maxStreak = 0;

        for (Habit_model habit : habitList) {
            if (habit.isCompletedToday()) { // 判断今日是否完成
                todayCompleted++;
            }
            if (habit.getStreak() > maxStreak) { // 获取最大连续天数
                maxStreak = habit.getStreak();
            }
        }

        totalHabitsTextView.setText("总习惯\n" + totalHabits);
        todayCompletedTextView.setText("今日完成\n" + todayCompleted);
        streakDaysTextView.setText("最长连续\n" + maxStreak + "天");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            loadHabits(); // 添加习惯后刷新列表和统计
        }
    }
}
