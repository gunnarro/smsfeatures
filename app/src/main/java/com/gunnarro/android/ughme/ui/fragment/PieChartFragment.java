package com.gunnarro.android.ughme.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.chart.CustomeMarkerView;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.sms.Sms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class PieChartFragment extends Fragment implements OnChartGestureListener {
    private static final String TAG = PieChartFragment.class.getName();

    private PieChart chart;

    @NonNull
    public static PieChartFragment newInstance() {
        return new PieChartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barchart_simple, container, false);
        // create a new chart object
        Log.d(TAG, "create chart");
        chart = new PieChart(getActivity());
        chart.getDescription().setEnabled(false);
        chart.setOnChartGestureListener(this);
        CustomeMarkerView mv = new CustomeMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(chart);
        chart.setMarker(mv);
        Log.d(TAG, "onCreateView: finished");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //  listen RxJava event here
        RxBus.getInstance().listen().subscribe(getInputObserver());
        Log.d(TAG, "onAttach: : Registerer RxBus listener");
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i(TAG, "onChartGestureStart: START");
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i(TAG, "onChartGestureEnd: END");
        chart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("BarChartFragment", "onChartLongPressed: Chart long pressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("BarChartFragment", "onChartDoubleTapped: Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("BarChartFragment", "onChartSingleTapped: Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("BarChartFragment", "onChartFling: Chart fling. VelocityX: " + velocityX + ", VelocityY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("BarChartFragment", "onChartScale: ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("BarChartFragment", "onChartTranslate: dX: " + dX + ", dY: " + dY);
    }

    private void updateChartData(List<Sms> smsList) {
        Map<String, Integer> smsMap = smsList.stream().collect(Collectors.groupingBy(Sms::getAddress, Collectors.summingInt(Sms::getCount)));
        List<PieEntry> numberOfSmsList = smsMap.entrySet().stream().map(e -> new PieEntry(1, e.getValue())).collect(Collectors.toList());
        PieDataSet dataSet = new PieDataSet(numberOfSmsList, "Sms");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        Log.d(TAG, String.format("updateChartData: Update chart data, data sets: %s", dataSet.getFormSize()));
        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
            Log.d(TAG, "updateChartData: chart data not set");
            // BarDataSet currentBarDataSet = (BarDataSet) chart.getBarData().getDataSetByIndex(0);
            // currentBarDataSet.setValues(barDataSets.get(0).getEntriesForXValue(0));
        } else {
            PieData data = new PieData(dataSet);
            chart.setData(data);
            //chart.setFitBars(true);
        }
        // let the chart know it's data changed
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        // refresh
        chart.invalidate();
    }

    private List<Sms> getSmsBackup(String mobileNumber) {
        Gson gson = new GsonBuilder().setLenient().create();
        Type smsListType = new TypeToken<ArrayList<Sms>>() {
        }.getType();

        try {
            File f = new File(getSmsBackupFilePath(mobileNumber));
            return gson.fromJson(new FileReader(f.getPath()), smsListType);
        } catch (FileNotFoundException e) {
            Log.d(TAG, String.format("sms backup file not found! error: %s", e.getMessage()));
            return new ArrayList<>();
        }
    }

    private String getSmsBackupFilePath(String mobileNumber) {
        File appDir = Objects.requireNonNull(getActivity()).getFilesDir();
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            mobileNumber = "all";
        }
        return String.format("%s/sms-backup-%s.json", appDir.getPath(), mobileNumber);
    }

    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "getInputObserver.onSubscribe:");
            }

            @Override
            public void onNext(Object obj) {
                Log.d(TAG, String.format("getInputObserver.onNext: Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof List<?>) {
                    Log.d(TAG, "getInputObserver.onNext: update bar chart data");
                    updateChartData(Collections.unmodifiableList((List<Sms>) obj));
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, String.format("getInputObserver.onError: %s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "getInputObserver.onComplete:");
            }
        };
    }
}
