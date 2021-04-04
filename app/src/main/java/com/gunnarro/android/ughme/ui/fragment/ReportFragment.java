package com.gunnarro.android.ughme.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.analyze.AnalyzeReport;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * view for word cloud build analyze
 */
@AndroidEntryPoint
public class ReportFragment extends Fragment {

    @Inject
    SmsBackupServiceImpl smsBackupService;

    @Inject
    public ReportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Utility.buildTag(getClass(), "onCreate"), "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        Log.d(Utility.buildTag(getClass(), "onCreateView"), "");
        return view;
    }

    /**
     * Update backup info after view is successfully create
     */
    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTextView(view, smsBackupService.readAnalyzeReport());
    }

    private void updateTextView(View view, AnalyzeReport analyseReport) {
        TextView reportView = view.findViewById(R.id.report_view);
        Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
        StringWriter sw = new StringWriter();
        gson.toJson(analyseReport, sw);
        reportView.setText(sw.toString());
    }

}
