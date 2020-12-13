package com.gunnarro.android.ughme.ui.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.Utility;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.sms.Sms;
import com.mordred.wordcloud.WordCloud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class WordCloudFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = WordCloud.class.getSimpleName();

    private RadioButton dateRadioBtn;
    private RadioButton mobileRadioBtn;
    private Spinner mobileNumberSp;


    public WordCloudFragment() {
        // Required empty public constructor
    }

    public static WordCloudFragment newInstance() {
        return new WordCloudFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_word_cloud, container, false);
        dateRadioBtn = view.findViewById(R.id.sms_number_radio_btn);
        dateRadioBtn.setOnClickListener(this);
        mobileRadioBtn = view.findViewById(R.id.sms_datetime_radio_btn);
        mobileRadioBtn.setOnClickListener(this);
        view.findViewById(R.id.sms_in_out_box_checkbox).setOnClickListener(this);
        List<String> mobileNumbers = getSmsBackupMobileNumbersTop10();
        mobileNumbers.add(0, "(.*)");
        mobileNumberSp = view.findViewById(R.id.sms_mobile_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_spinner_item, mobileNumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mobileNumberSp.setAdapter(adapter);
        mobileNumberSp.setOnItemSelectedListener(this);
        return view;
    }

    private List<String> getSmsBackupMobileNumbersTop10() {
        try {
            List<Sms> smsList = Utility.getSmsBackup(getSmsBackupFilePath());
            Map<String, Integer> smsMap = smsList.stream().collect(Collectors.groupingBy(Sms::getAddress, Collectors.summingInt(Sms::getCount)));
            return Utility.getTop10ValuesFromMap(smsMap);
        } catch (Exception e) {
            Log.d(TAG, String.format("sms backup file not found! error: %s", e.getMessage()));
            return new ArrayList<>();
        }
    }

    private String getSmsBackupFilePath() {
        return String.format("%s/sms-backup-all.json", Objects.requireNonNull(getContext()).getFilesDir().getPath());
    }

    @Override
    public void onClick(View view) {
        boolean checked = false;
        if (view instanceof RadioButton) {
            checked = ((RadioButton) view).isChecked();
        } else if (view instanceof CheckBox) {
            checked = ((CheckBox) view).isChecked();
        }
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.sms_number_radio_btn:
                if (checked) {
                    RxBus.getInstance().publish(WordCloudEvent.builder().setEventType(WordCloudEvent.WordCloudEventTypeEnum.NUMBER).smsTypeAll().build());
                    break;
                }
            case R.id.sms_datetime_radio_btn:
                if (checked) {
                    RxBus.getInstance().publish(WordCloudEvent.builder().setEventType(WordCloudEvent.WordCloudEventTypeEnum.DATE).smsTypeAll().build());
                    break;
                }
            case R.id.sms_in_out_box_checkbox:
                Log.d(TAG, String.format("radiobtn in-outbox: %s", checked));
                if (checked) {
                    RxBus.getInstance().publish(WordCloudEvent.builder()
                            .setEventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                            .smsTypeInbox()
                            .setValue(mobileNumberSp.getSelectedItem().toString())
                            .build());
                } else {
                    RxBus.getInstance().publish(WordCloudEvent.builder()
                            .setEventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                            .smsTypeOutbox()
                            .setValue(mobileNumberSp.getSelectedItem().toString())
                            .build());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mobileRadioBtn.setChecked(false);
        dateRadioBtn.setChecked(false);
        RxBus.getInstance().publish(
                WordCloudEvent.builder()
                        .setEventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                        .setValue(mobileNumberSp.getSelectedItem().toString())
                        .smsTypeAll()
                        .build());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
