package com.aiden.desine.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aiden.desine.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScheduleWorksFragment extends Fragment  {
    private RecyclerView recyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                           @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_works, container, false);
        initViews(view);


        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.schedule_recycler_view);
    }



}