package com.aiden.desine.activities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aiden.desine.R;
import com.aiden.desine.model.Habit_model;
import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit_model> habitList;
    private List<Habit_model> selectedHabits = new ArrayList<>();

    // 构造方法，接收习惯列表并处理 null 情况
    public HabitAdapter(List<Habit_model> habitList) {
        this.habitList = (habitList != null) ? habitList : new ArrayList<>();
    }

    // 创建视图持有者，使用 sub_schedule_item.xml
    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sub_schedule_item, parent, false);
        return new HabitViewHolder(view);
    }

    // 绑定数据到视图
    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit_model habit = habitList.get(position);

        // 设置习惯名称
        if (holder.textHabitName != null) {
            holder.textHabitName.setText(habit.getName());
        } else {
            Log.e("HabitAdapter", "text_habit_name is null at position " + position);
        }

        // 设置连续完成天数
        if (holder.textHabitStreak != null) {
            holder.textHabitStreak.setText("连续完成: " + habit.getStreak() + " 天");
        } else {
            Log.e("HabitAdapter", "text_habit_streak is null at position " + position);
        }

        // 隐藏完成率 TextView
        if (holder.textCompletionRate != null) {
            holder.textCompletionRate.setVisibility(View.GONE);
        } else {
            Log.e("HabitAdapter", "text_completion_rate is null at position " + position);
        }

        // 设置复选框状态
        if (holder.checkBox != null) {
            holder.checkBox.setChecked(selectedHabits.contains(habit));
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedHabits.add(habit);
                } else {
                    selectedHabits.remove(habit);
                }
            });
        } else {
            Log.e("HabitAdapter", "checkbox_habit is null at position " + position);
        }
    }

    // 返回习惯列表大小
    @Override
    public int getItemCount() {
        return habitList.size();
    }

    // 获取选中的习惯列表
    public List<Habit_model> getSelectedHabits() {
        return selectedHabits;
    }

    // 视图持有者类
    static class HabitViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textHabitName, textHabitStreak, textCompletionRate;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_habit);
            textHabitName = itemView.findViewById(R.id.text_habit_name);
            textHabitStreak = itemView.findViewById(R.id.text_habit_streak);
            textCompletionRate = itemView.findViewById(R.id.text_completion_rate);

            // 检查视图是否正确初始化
            if (checkBox == null) {
                Log.e("HabitViewHolder", "checkbox_habit not found");
            }
            if (textHabitName == null) {
                Log.e("HabitViewHolder", "text_habit_name not found");
            }
            if (textHabitStreak == null) {
                Log.e("HabitViewHolder", "text_habit_streak not found");
            }
            if (textCompletionRate == null) {
                Log.e("HabitViewHolder", "text_completion_rate not found");
            }
        }
    }
}
