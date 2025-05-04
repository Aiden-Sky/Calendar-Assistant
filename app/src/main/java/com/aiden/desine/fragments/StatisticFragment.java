package com.aiden.desine.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.aiden.desine.R;
import com.aiden.desine.dao.Habit_dao;
import com.aiden.desine.dao.Schedule_dao;
import com.aiden.desine.model.Habit_model;
import com.aiden.desine.model.Schedule;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StatisticFragment extends Fragment {

    // UI 组件
    private TextView habitCompletionRateView;
    private TextView taskCompletionRateView;
    private TextView habitCountView;
    private TextView taskCountView;
    private TextView productivityScoreView;
    private CircularProgressIndicator productivityScoreIndicator;
    private BarChart habitChart;
    private LineChart taskChart;
    private PieChart priorityChart;
    private ChipGroup dateRangeChipGroup;
    private Chip chipWeek, chipMonth, chipThreeMonths, chipYear, chipCustom;
    private MaterialButton habitChartTypeButton, taskChartTypeButton;
    
    // 数据访问对象
    private Habit_dao habitDao;
    private Schedule_dao scheduleDao;
    private long currentUserId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    // 日期范围
    private Date startDate;
    private Date endDate;
    
    // 图表类型
    private enum HabitChartType { STREAK, COMPLETION_RATE }
    private enum TaskChartType { COMPLETION_RATE, TASK_COUNT }
    private HabitChartType currentHabitChartType = HabitChartType.STREAK;
    private TaskChartType currentTaskChartType = TaskChartType.COMPLETION_RATE;

    public StatisticFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载布局文件
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);
        
        // 初始化 DAO
        habitDao = new Habit_dao(getContext());
        scheduleDao = new Schedule_dao(getContext());
        
        // 获取当前用户ID
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("current_user_id", 1);
        
        // 初始化视图
        initViews(view);
        
        // 设置日期范围为本周（默认）
        setDateRangeToCurrentWeek();
        
        // 设置监听器
        setupListeners();
        
        // 加载数据并更新UI
        updateStatistics();
        
        return view;
    }
    
    private void initViews(View view) {
        // 绑定视图
        habitCompletionRateView = view.findViewById(R.id.habit_completion_rate);
        taskCompletionRateView = view.findViewById(R.id.task_completion_rate);
        habitCountView = view.findViewById(R.id.habit_count);
        taskCountView = view.findViewById(R.id.task_count);
        productivityScoreView = view.findViewById(R.id.productivity_score);
        productivityScoreIndicator = view.findViewById(R.id.productivity_score_indicator);
        habitChart = view.findViewById(R.id.habit_chart);
        taskChart = view.findViewById(R.id.task_chart);
        priorityChart = view.findViewById(R.id.priority_chart);
        
        // 日期范围选择器
        dateRangeChipGroup = view.findViewById(R.id.date_range_chip_group);
        chipWeek = view.findViewById(R.id.chip_week);
        chipMonth = view.findViewById(R.id.chip_month);
        chipThreeMonths = view.findViewById(R.id.chip_three_months);
        chipYear = view.findViewById(R.id.chip_year);
        chipCustom = view.findViewById(R.id.chip_custom);
        
        // 图表类型按钮
        habitChartTypeButton = view.findViewById(R.id.habit_chart_type_button);
        taskChartTypeButton = view.findViewById(R.id.task_chart_type_button);
    }
    
    private void setupListeners() {
        // 日期范围选择器监听
        chipWeek.setOnClickListener(v -> {
            setDateRangeToCurrentWeek();
            updateStatistics();
        });
        
        chipMonth.setOnClickListener(v -> {
            setDateRangeToCurrentMonth();
            updateStatistics();
        });
        
        chipThreeMonths.setOnClickListener(v -> {
            setDateRangeToLast3Months();
            updateStatistics();
        });
        
        chipYear.setOnClickListener(v -> {
            setDateRangeToCurrentYear();
            updateStatistics();
        });
        
        chipCustom.setOnClickListener(v -> {
            showDateRangePicker();
        });
        
        // 图表类型切换按钮监听
        habitChartTypeButton.setOnClickListener(v -> {
            toggleHabitChartType();
        });
        
        taskChartTypeButton.setOnClickListener(v -> {
            toggleTaskChartType();
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次页面恢复时更新数据
        updateStatistics();
    }
    
    private void updateStatistics() {
        // 更新统计数据
        updateOverviewStats();
        
        // 更新习惯图表
        setupHabitChart();
        
        // 更新日程图表
        setupTaskChart();
        
        // 更新优先级分布图
        setupPriorityChart();
        
        // 计算并更新效率评分
        calculateProductivityScore();
    }
    
    private void updateOverviewStats() {
        // 获取指定日期范围内的数据
        List<Habit_model> allHabits = habitDao.getAllHabits();
        List<Schedule> schedules = getSchedulesInDateRange();
        
        // 计算习惯完成率
        int completedHabits = 0;
        for (Habit_model habit : allHabits) {
            if (habit.isCompletedToday()) {
                completedHabits++;
            }
        }
        float habitCompletionRate = allHabits.size() > 0 ? 
                (float) completedHabits / allHabits.size() * 100 : 0;
        
        // 计算日程完成率
        int completedTasks = 0;
        for (Schedule schedule : schedules) {
            if (schedule.isCompleted()) {
                completedTasks++;
            }
        }
        float taskCompletionRate = schedules.size() > 0 ? 
                (float) completedTasks / schedules.size() * 100 : 0;
        
        // 更新UI
        habitCompletionRateView.setText(String.format(Locale.getDefault(), "%.1f%%", habitCompletionRate));
        taskCompletionRateView.setText(String.format(Locale.getDefault(), "%.1f%%", taskCompletionRate));
        habitCountView.setText(String.valueOf(allHabits.size()));
        taskCountView.setText(String.valueOf(schedules.size()));
    }
    
    private void setupHabitChart() {
        List<Habit_model> allHabits = habitDao.getAllHabits();
        
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        if (currentHabitChartType == HabitChartType.STREAK) {
            // 显示连续打卡天数
            habitChartTypeButton.setText(getString(R.string.chart_streak));
            
            for (int i = 0; i < allHabits.size() && i < 7; i++) {
                Habit_model habit = allHabits.get(i);
                entries.add(new BarEntry(i, habit.getStreak()));
                labels.add(habit.getName());
            }
        } else {
            // 显示习惯完成率
            habitChartTypeButton.setText(getString(R.string.chart_completion_rate));
            
            for (int i = 0; i < allHabits.size() && i < 7; i++) {
                Habit_model habit = allHabits.get(i);
                entries.add(new BarEntry(i, (float) habit.getCompletionRate()));
                labels.add(habit.getName());
            }
        }
        
        // 创建数据集
        BarDataSet dataSet = new BarDataSet(entries, currentHabitChartType == HabitChartType.STREAK ? 
                getString(R.string.chart_habit_streak_days) : getString(R.string.chart_habit_completion_rate));
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);
        
        // 创建BarData对象
        BarData barData = new BarData(dataSet);
        
        // 配置X轴
        XAxis xAxis = habitChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        
        // 应用数据到图表
        habitChart.setData(barData);
        habitChart.getDescription().setEnabled(false);
        habitChart.setFitBars(true);
        habitChart.animateY(1000);
        habitChart.invalidate();
    }
    
    private void setupTaskChart() {
        // 获取日期范围内的所有日期
        ArrayList<String> dates = getDatesInRange();
        
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        if (currentTaskChartType == TaskChartType.COMPLETION_RATE) {
            // 显示完成率
            taskChartTypeButton.setText(getString(R.string.chart_completion_rate));
            
            // 计算每天的完成率
            for (int i = 0; i < dates.size(); i++) {
                String date = dates.get(i);
                List<Schedule> schedules = scheduleDao.getSchedulesByDate(currentUserId, date);
                int completedTasks = 0;
                
                for (Schedule schedule : schedules) {
                    if (schedule.isCompleted()) {
                        completedTasks++;
                    }
                }
                
                float completionRate = schedules.size() > 0 ? 
                        (float) completedTasks / schedules.size() * 100 : 0;
                
                entries.add(new Entry(i, completionRate));
            }
        } else {
            // 显示任务数量
            taskChartTypeButton.setText(getString(R.string.chart_task_count));
            
            // 计算每天的任务数量
            for (int i = 0; i < dates.size(); i++) {
                String date = dates.get(i);
                List<Schedule> schedules = scheduleDao.getSchedulesByDate(currentUserId, date);
                entries.add(new Entry(i, schedules.size()));
            }
        }
        
        // 格式化日期标签
        for (String date : dates) {
            try {
                Date dateObj = dateFormat.parse(date);
                SimpleDateFormat shortFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
                labels.add(shortFormat.format(dateObj));
            } catch (ParseException e) {
                labels.add(date);
            }
        }
        
        // 创建数据集
        LineDataSet dataSet = new LineDataSet(entries, currentTaskChartType == TaskChartType.COMPLETION_RATE ? 
                getString(R.string.chart_task_completion_rate) : getString(R.string.chart_task_count_title));
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        
        // 创建LineData对象
        LineData lineData = new LineData(dataSet);
        
        // 配置X轴
        XAxis xAxis = taskChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        // 设置最大可见的标签数量
        xAxis.setLabelCount(Math.min(7, dates.size()), true);
        
        // 应用数据到图表
        taskChart.setData(lineData);
        taskChart.getDescription().setEnabled(false);
        taskChart.animateX(1000);
        taskChart.invalidate();
    }
    
    private void setupPriorityChart() {
        // 获取日期范围内的所有日程
        List<Schedule> schedules = getSchedulesInDateRange();
        
        // 统计不同优先级的任务数量
        Map<String, Integer> priorityCounts = new HashMap<>();
        priorityCounts.put(getString(R.string.priority_high_value), 0);
        priorityCounts.put(getString(R.string.priority_medium_value), 0);
        priorityCounts.put(getString(R.string.priority_low_value), 0);
        
        for (Schedule schedule : schedules) {
            String priority = schedule.getPriority();
            priorityCounts.put(priority, priorityCounts.getOrDefault(priority, 0) + 1);
        }
        
        // 准备饼图数据
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (priorityCounts.get(getString(R.string.priority_high_value)) > 0) {
            entries.add(new PieEntry(priorityCounts.get(getString(R.string.priority_high_value)), 
                getString(R.string.priority_high)));
        }
        if (priorityCounts.get(getString(R.string.priority_medium_value)) > 0) {
            entries.add(new PieEntry(priorityCounts.get(getString(R.string.priority_medium_value)), 
                getString(R.string.priority_medium)));
        }
        if (priorityCounts.get(getString(R.string.priority_low_value)) > 0) {
            entries.add(new PieEntry(priorityCounts.get(getString(R.string.priority_low_value)), 
                getString(R.string.priority_low)));
        }
        
        // 创建数据集
        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.priority_distribution_title));
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(priorityChart));
        
        // 创建PieData对象
        PieData pieData = new PieData(dataSet);
        
        // 配置饼图
        priorityChart.setData(pieData);
        priorityChart.getDescription().setEnabled(false);
        priorityChart.setUsePercentValues(true);
        priorityChart.setCenterText(getString(R.string.chart_priority_center));
        priorityChart.setCenterTextSize(14f);
        priorityChart.setHoleRadius(45f);
        priorityChart.setTransparentCircleRadius(50f);
        priorityChart.animateY(1000);
        priorityChart.invalidate();
    }
    
    private void calculateProductivityScore() {
        // 获取数据
        List<Habit_model> allHabits = habitDao.getAllHabits();
        List<Schedule> schedules = getSchedulesInDateRange();
        
        // 计算习惯完成率
        int completedHabits = 0;
        for (Habit_model habit : allHabits) {
            if (habit.isCompletedToday()) {
                completedHabits++;
            }
        }
        float habitCompletionRate = allHabits.size() > 0 ? 
                (float) completedHabits / allHabits.size() * 100 : 0;
        
        // 计算日程完成率
        int completedTasks = 0;
        for (Schedule schedule : schedules) {
            if (schedule.isCompleted()) {
                completedTasks++;
            }
        }
        float taskCompletionRate = schedules.size() > 0 ? 
                (float) completedTasks / schedules.size() * 100 : 0;
        
        // 计算综合得分（习惯占40%，日程占60%）
        int productivityScore = (int) (habitCompletionRate * 0.4f + taskCompletionRate * 0.6f);
        
        // 更新UI
        productivityScoreView.setText(String.valueOf(productivityScore));
        productivityScoreIndicator.setProgress(productivityScore);
        
        // 根据得分显示评价
        TextView evaluationText = (TextView) ((ViewGroup) productivityScoreView.getParent()).getChildAt(1);
        if (productivityScore >= 90) {
            evaluationText.setText(getString(R.string.perfect));
        } else if (productivityScore >= 75) {
            evaluationText.setText(getString(R.string.good));
        } else if (productivityScore >= 60) {
            evaluationText.setText(getString(R.string.comman));
        } else {
            evaluationText.setText(getString(R.string.stress));
        }
    }
    
    // 切换习惯图表类型
    private void toggleHabitChartType() {
        currentHabitChartType = (currentHabitChartType == HabitChartType.STREAK) ? 
                HabitChartType.COMPLETION_RATE : HabitChartType.STREAK;
        setupHabitChart();
    }
    
    // 切换日程图表类型
    private void toggleTaskChartType() {
        currentTaskChartType = (currentTaskChartType == TaskChartType.COMPLETION_RATE) ? 
                TaskChartType.TASK_COUNT : TaskChartType.COMPLETION_RATE;
        setupTaskChart();
    }
    
    // 设置日期范围为当前周
    private void setDateRangeToCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        startDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        endDate = calendar.getTime();
    }
    
    // 设置日期范围为当前月
    private void setDateRangeToCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDate = calendar.getTime();
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = calendar.getTime();
    }
    
    // 设置日期范围为过去3个月
    private void setDateRangeToLast3Months() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDate = calendar.getTime();
        
        calendar = Calendar.getInstance();
        endDate = calendar.getTime();
    }
    
    // 设置日期范围为当年
    private void setDateRangeToCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        startDate = calendar.getTime();
        
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        endDate = calendar.getTime();
    }
    
    // 显示日期范围选择器
    private void showDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText(getString(R.string.date_range_picker_title))
                        .build();
        
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            startDate = new Date(selection.first);
            endDate = new Date(selection.second);
            updateStatistics();
        });
        
        dateRangePicker.show(getChildFragmentManager(), "DATE_RANGE_PICKER");
    }
    
    // 获取日期范围内的所有日程
    private List<Schedule> getSchedulesInDateRange() {
        List<Schedule> schedules = new ArrayList<>();
        
        // 遍历日期范围内的每一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        Date currentDate = startDate;
        
        while (!currentDate.after(endDate)) {
            String dateStr = dateFormat.format(currentDate);
            schedules.addAll(scheduleDao.getSchedulesByDate(currentUserId, dateStr));
            
            // 日期加1
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            currentDate = calendar.getTime();
        }
        
        return schedules;
    }
    
    // 获取日期范围内的所有日期字符串
    private ArrayList<String> getDatesInRange() {
        ArrayList<String> dates = new ArrayList<>();
        
        // 遍历日期范围内的每一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        Date currentDate = startDate;
        
        while (!currentDate.after(endDate)) {
            dates.add(dateFormat.format(currentDate));
            
            // 日期加1
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            currentDate = calendar.getTime();
        }
        
        return dates;
    }
}
