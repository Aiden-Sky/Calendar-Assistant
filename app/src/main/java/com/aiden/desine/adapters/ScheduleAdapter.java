package com.aiden.desine.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiden.desine.R;
import com.aiden.desine.model.Schedule;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private List<Schedule> scheduleList;
    private Context context;
    private OnScheduleClickListener listener;
    private OnScheduleStatusChangedListener statusListener;

    // 点击事件回调接口
    public interface OnScheduleClickListener {
        void onScheduleClick(Schedule schedule);
    }

    // 状态变更回调接口
    public interface OnScheduleStatusChangedListener {
        void onStatusChanged(Schedule schedule, boolean isCompleted);
    }

    public ScheduleAdapter(Context context, List<Schedule> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;
    }

    public void setOnScheduleClickListener(OnScheduleClickListener listener) {
        this.listener = listener;
    }

    public void setOnScheduleStatusChangedListener(OnScheduleStatusChangedListener listener) {
        this.statusListener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule schedule = scheduleList.get(position);
        holder.bind(schedule);
    }

    @Override
    public int getItemCount() {
        return scheduleList == null ? 0 : scheduleList.size();
    }

    // 更新数据
    public void updateData(List<Schedule> newData) {
        this.scheduleList = newData;
        notifyDataSetChanged();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView priorityTextView;
        private TextView timeTextView;
        private TextView categoryTextView;
        private CheckBox completedCheckBox;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.task_title);
            priorityTextView = itemView.findViewById(R.id.task_priority);
            timeTextView = itemView.findViewById(R.id.task_time);
            categoryTextView = itemView.findViewById(R.id.task_category);
            completedCheckBox = itemView.findViewById(R.id.task_completed);

            // 设置点击事件
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onScheduleClick(scheduleList.get(position));
                }
            });

            // 设置复选框状态变更监听
            completedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && statusListener != null && buttonView.isPressed()) {
                    Schedule schedule = scheduleList.get(position);
                    schedule.setCompleted(isChecked);
                    statusListener.onStatusChanged(schedule, isChecked);
                }
            });
        }

        public void bind(Schedule schedule) {
            titleTextView.setText(schedule.getTitle());
            
            // 设置优先级标签样式
            priorityTextView.setText(schedule.getPriority());
            switch (schedule.getPriority()) {
                case "高":
                    priorityTextView.setBackgroundColor(Color.parseColor("#F44336")); // 红色
                    break;
                case "中":
                    priorityTextView.setBackgroundColor(Color.parseColor("#FF9800")); // 橙色
                    break;
                case "低":
                    priorityTextView.setBackgroundColor(Color.parseColor("#4CAF50")); // 绿色
                    break;
            }
            
            // 设置时间信息
            String timeText = schedule.getStartTime();
            if (schedule.getEndTime() != null && !schedule.getEndTime().isEmpty()) {
                timeText += " - " + schedule.getEndTime();
            }
            timeTextView.setText(timeText);
            
            // 设置分类标签
            categoryTextView.setText(schedule.getCategory());
            
            // 设置完成状态
            completedCheckBox.setChecked(schedule.isCompleted());

            // 如果任务已完成，则标题显示为灰色
            if (schedule.isCompleted()) {
                titleTextView.setTextColor(Color.GRAY);
            } else {
                titleTextView.setTextColor(Color.parseColor("#333333"));
            }
        }
    }
} 