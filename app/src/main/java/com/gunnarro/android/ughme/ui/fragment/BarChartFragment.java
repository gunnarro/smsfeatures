package com.gunnarro.android.ughme.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BarChartFragment extends Fragment implements OnChartGestureListener {
    private BarChart chart;

    @NonNull
    public static BarChartFragment newInstance() {
        return new BarChartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("BarChartFragment", "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barchart_simple, container, false);
        // create a new chart object
        Log.d("BarChartFragment", "create chart");
        chart = new BarChart(getActivity());
        chart.getDescription().setEnabled(false);
        chart.setOnChartGestureListener(this);
        // Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),"OpenSans-Light.ttf");
        CustomeMarkerView mv = new CustomeMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        //xAxis.setValueFormatter(new ValueFormatter());

        YAxis leftAxis = chart.getAxisLeft();
        //leftAxis.setTypeface(tf);
        leftAxis.setSpaceTop(30f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaximum(100f);
        leftAxis.setValueFormatter(new DefaultValueFormatter(0));

        chart.getAxisRight().setEnabled(false);

        Legend l = chart.getLegend();
        //l.setTypeface(tf);

        // programmatically add the chart
        FrameLayout parent = view.findViewById(R.id.parentLayout);
        parent.addView(chart);
        chart.setFitBars(true);
        // chart.animateXY(5000, 5000);
        updateChartData(getSmsBackup("45465500"));
        Log.d("BarChartFragment", "onCreateView: finished");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("BarChartFragment", "onViewCreated");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //  listen RxJava event here
        RxBus.getInstance().listen().subscribe(getInputObserver());
        Log.d("BarChartFragment", "onAttach: : Registerer RxBus listener");
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("BarChartFragment", "onChartGestureStart: START");
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("BarChartFragment", "onChartGestureEnd: END");
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

    private List<IBarDataSet> mapToBarDataSets(List<Sms> smsList) {
        // accumulate sms per mobile number
        Map<String, Integer> smsMap = smsList
                .stream()
                .collect(Collectors.groupingBy(Sms::getAddress
                        , Collectors.summingInt(Sms::getCount)));

        Log.d("BarChartFragment", "mapToBarDataSets: Map: " + smsMap);
        List<IBarDataSet> barDataSets = new ArrayList<>();
        smsMap.entrySet()
                .forEach(e -> barDataSets.add(buildBarDataSet(e.getKey(), e.getValue())));
        return barDataSets;
    }

    private BarDataSet buildBarDataSet(String name, Integer value) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(value, value));
        BarDataSet barDataSet = new BarDataSet(entries, name);
        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        barDataSet.setBarBorderWidth(0.9f);
        barDataSet.setValueTextColor(Color.DKGRAY);
        barDataSet.setValueTextSize(12f);
        return barDataSet;
    }

    private void updateChartData(List<Sms> smsList) {
        List<IBarDataSet> barDataSets = mapToBarDataSets(smsList);
        Log.d("BarChartFragment", String.format("updateChartData: Update chart data, %s", barDataSets.size()));
        if (chart.getData() != null && chart.getBarData().getDataSetCount() > 0) {
            Log.d("BarChartFragment", "updateChartData: chart data not set");
            // BarDataSet currentBarDataSet = (BarDataSet) chart.getBarData().getDataSetByIndex(0);
            // currentBarDataSet.setValues(barDataSets.get(0).getEntriesForXValue(0));
        } else {
            BarData data = new BarData(barDataSets);
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
            Log.d("BarChartFragment", String.format("sms backup file not found! error: %s", e.getMessage()));
            return new ArrayList<>();
        }
    }

    private String getSmsBackupFilePath(String mobileNumber) throws FileNotFoundException {
        File appDir = getActivity().getFilesDir();
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new FileNotFoundException("No sms backup file for mobile number found!");
        }
        return String.format("%s/sms-backup-%s.json", appDir.getPath(), mobileNumber);
    }

    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d("BarChartFragment", "onSubscribe:");
            }

            @Override
            public void onNext(Object obj) {
                Log.d("BarChartFragment", String.format("onNext: Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof List<?>) {
                    Log.d("BarChartFragment", "onNext: update bar chart data");
                    updateChartData((List<Sms>) obj);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e("BarChartFragment", String.format("onError: %s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d("BarChartFragment", "onComplete:");
            }
        };
    }
}
