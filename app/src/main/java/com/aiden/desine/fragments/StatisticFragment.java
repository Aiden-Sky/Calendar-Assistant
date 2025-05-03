package com.aiden.desine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.aiden.desine.R;

public class StatisticFragment extends Fragment {

    public StatisticFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载并返回布局文件
        return inflater.inflate(R.layout.fragment_statistic, container, false);
    }
}
