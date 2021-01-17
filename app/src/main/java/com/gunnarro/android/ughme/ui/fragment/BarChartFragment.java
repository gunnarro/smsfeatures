package com.gunnarro.android.ughme.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

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
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.chart.CustomeMarkerView;
import com.gunnarro.android.ughme.model.chart.StackedBarEntry;
import com.gunnarro.android.ughme.model.chart.formatter.DayAxisValueFormatter;
import com.gunnarro.android.ughme.model.chart.formatter.MonthXAxisFormatter;
import com.gunnarro.android.ughme.model.chart.formatter.SimpleAxisValueFormatter;
import com.gunnarro.android.ughme.model.chart.formatter.YearXAxisFormatter;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class BarChartFragment extends Fragment implements OnChartGestureListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = BarChartFragment.class.getName();
    private BarChart chart;
    private Spinner mobileNumberSp;
    private List<String> mobileNumbers;

    @Inject
    SmsBackupServiceImpl smsBackupService;

    @Inject
    public BarChartFragment() {
    }

    private enum StatTypeEnum {
        NUMBER, DAY, MONTH, YEAR
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barchart, container, false);
        view.findViewById(R.id.day_radio_btn).setOnClickListener(this);
        view.findViewById(R.id.month_radio_btn).setOnClickListener(this);
        view.findViewById(R.id.year_radio_btn).setOnClickListener(this);
        view.findViewById(R.id.number_radio_btn).setOnClickListener(this);
        mobileNumbers = new ArrayList<>();
        mobileNumbers.add(BackupFragment.ALL);
        mobileNumberSp = view.findViewById(R.id.number_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, mobileNumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mobileNumberSp.setAdapter(adapter);
        mobileNumberSp.setOnItemSelectedListener(this);

        // create a new chart object
        Log.d(TAG, "create chart");
        chart = view.findViewById(R.id.barchart);
        chart.getDescription().setEnabled(false);
        chart.setOnChartGestureListener(this);
        CustomeMarkerView mv = new CustomeMarkerView(getActivity(), R.layout.custom_marker_view);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new YearXAxisFormatter());

        chart.getAxisRight().setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setSpaceTop(30f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        //leftAxis.setAxisMaximum(1000f);
        leftAxis.setValueFormatter(new DefaultValueFormatter(0));

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);

        chart.setFitBars(true);
        updateChartData(getSmsBackup(mobileNumberSp.getSelectedItem().toString().toLowerCase()), StatTypeEnum.NUMBER);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // don not show options menu
        menu.setGroupVisible(0, false);
        menu.clear();
        menu.close();
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

    private String formatAsDateStr(Long timeMs, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(timeMs);
    }

    @NonNull()
    private List<IBarDataSet> mapToBarDataSets(List<Sms> smsList, StatTypeEnum type) {
        // accumulate sms per selected type
        Map<String, Integer> smsMap;
        switch (type) {
            case DAY:
                smsMap = smsList.stream().collect(Collectors.groupingBy(s -> formatAsDateStr(s.getTimeMs(), "dd-MM-YY"), Collectors.summingInt(Sms::getCount)));
                break;
            case MONTH:
                smsMap = smsList.stream().collect(Collectors.groupingBy(s -> formatAsDateStr(s.getTimeMs(), "MM-YY"), Collectors.summingInt(Sms::getNumberOfReceived)));
                break;
            case YEAR:
                smsMap = smsList.stream().collect(Collectors.groupingBy(s -> formatAsDateStr(s.getTimeMs(), "YYYY"), Collectors.summingInt(Sms::getNumberOfSent)));
                break;
            default:
                smsMap = smsList.stream().collect(Collectors.groupingBy(Sms::getContactName, Collectors.summingInt(Sms::getCount)));
                break;
        }
        // sort and limit to top ten entries with highest count
        Map<String, Integer> sortedSmsMap = Utility.getTop10Values(smsMap);

        Log.d(TAG, "mapToBarDataSets: Map: " + sortedSmsMap);
        List<IBarDataSet> barDataSets = new ArrayList<>();
        boolean isStacked = false;
        if (isStacked) {
            StackedBarEntry stackedBarEntry = buildStackedBarEntries(sortedSmsMap);
            barDataSets.add(buildBarDataSetStacked(stackedBarEntry.getLabels(), stackedBarEntry.getValues()));
        } else {
            sortedSmsMap.entrySet().forEach(e -> barDataSets.add(buildBarDataSet(e.getKey(), e.getValue(), barDataSets.size())));
        }
        mobileNumbers = Utility.getTop10ValuesFromMap(sortedSmsMap);
        Log.d(TAG, "mapToBarDataSets: Set: " + barDataSets);
        return barDataSets;
    }

    @NonNull
    private StackedBarEntry buildStackedBarEntries(Map<String, Integer> smsMap) {
        float[] values = new float[smsMap.size()];
        String[] labels = new String[smsMap.size()];
        int i = 0;
        for (Map.Entry<String, Integer> entry : smsMap.entrySet()) {
            labels[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        return new StackedBarEntry(labels, values);
    }

    private BarDataSet buildBarDataSetStacked(String[] labels, float[] values) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, values));
        BarDataSet barDataSet = new BarDataSet(entries, null);
        barDataSet.setStackLabels(labels);
        // set different colors within a data set
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.DKGRAY);
        barDataSet.setValueTextSize(12f);
        return barDataSet;
    }

    @NonNull
    private BarDataSet buildBarDataSet(String name, Integer value, Integer index) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(index, Float.valueOf(value)));
        BarDataSet barDataSet = new BarDataSet(entries, name);
        // set color for a full data set
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        barDataSet.setColor(color);
        // set different colors within a data set
        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        // barDataSet.setBarBorderWidth(0.9f);
        barDataSet.setValueTextColor(Color.DKGRAY);
        barDataSet.setValueTextSize(12f);
        // barDataSet.setStackLabels(null);
        return barDataSet;
    }

    private void updateChartData(List<Sms> smsList, StatTypeEnum type) {
        List<IBarDataSet> barDataSets = mapToBarDataSets(smsList, type);
        Log.d(TAG, String.format("updateChartData: Update chart data, data sets: %s", barDataSets.size()));
        if (chart.getData() != null && chart.getBarData().getDataSetCount() > 0) {
            Log.d(TAG, "updateChartData: chart data set!");
            // BarDataSet currentBarDataSet = (BarDataSet) chart.getBarData().getDataSetByIndex(0);
            // currentBarDataSet.setValues(barDataSets.get(0).getEntriesForXValue(0));
        } else {
            Log.d(TAG, "data not set, set new data");
            chart.setData(new BarData(barDataSets));
            //chart.setFitBars(true);
        }
        // always, override current data
        chart.setData(new BarData(barDataSets));
        //
        if (type == StatTypeEnum.DAY) {
            chart.getXAxis().setValueFormatter(new DayAxisValueFormatter());
        } else if (type == StatTypeEnum.MONTH) {
            chart.getXAxis().setValueFormatter(new MonthXAxisFormatter());
        } else if (type == StatTypeEnum.YEAR) {
            chart.getXAxis().setValueFormatter(new SimpleAxisValueFormatter(new String[]{"2016", "2017", "2018", "2019", "2020", "2021", "2022"}));
        } else if (type == StatTypeEnum.NUMBER) {
            chart.getXAxis().setValueFormatter(null);
        }

        // let the chart know it's data changed
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        // refresh
        chart.invalidate();
        Log.d(TAG, "updated chart data! type: " + type);
    }

    private List<Sms> getSmsBackup(String filterBy) {
        try {
            List<Sms> smsList = smsBackupService.getSmsBackup();
            Log.d(TAG, "getSmsBackup: " + smsList);
            if (filterBy != null && !filterBy.equalsIgnoreCase(BackupFragment.ALL)) {
                return smsList.stream().filter(s -> s.getContactName().contains(filterBy)).collect(Collectors.toList());
            }
            return smsList;
        } catch (Exception e) {
            Log.d(TAG, String.format("sms backup file not found! error: %s", e.getMessage()));
            return new ArrayList<>();
        }
    }

    /**
     *
     */
    @Override
    public void onClick(View view) {
        String selectedMobileNumber = mobileNumberSp.getSelectedItem().toString();
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        int id = view.getId();
        if (id == R.id.day_radio_btn) {
            if (checked) {
                updateChartData(getSmsBackup(selectedMobileNumber), StatTypeEnum.DAY);
            }
        } else if (id == R.id.month_radio_btn) {
            if (checked) {
                updateChartData(getSmsBackup(selectedMobileNumber), StatTypeEnum.MONTH);
            }
        } else if (id == R.id.year_radio_btn) {
            if (checked) {
                updateChartData(getSmsBackup(selectedMobileNumber), StatTypeEnum.YEAR);
            }
        } else if (id == R.id.number_radio_btn) {
            if (checked) {
                updateChartData(getSmsBackup(selectedMobileNumber), StatTypeEnum.NUMBER);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        updateChartData(getSmsBackup(mobileNumberSp.getSelectedItem().toString().toLowerCase()), StatTypeEnum.NUMBER);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                Log.d(TAG, "getInputObserver.onSubscribe:");
            }

            @Override
            public void onNext(@NotNull Object obj) {
                Log.d(TAG, String.format("getInputObserver.onNext: Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof List<?>) {
                    Log.d(TAG, "getInputObserver.onNext: update bar chart data");
                    updateChartData(Collections.unmodifiableList((List<Sms>) obj), StatTypeEnum.NUMBER);
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Log.e(TAG, String.format("getInputObserver.onError: %s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "getInputObserver.onComplete:");
            }
        };
    }
}
