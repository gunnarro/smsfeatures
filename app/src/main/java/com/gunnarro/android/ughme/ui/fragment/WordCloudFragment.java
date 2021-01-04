package com.gunnarro.android.ughme.ui.fragment;

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

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.mordred.wordcloud.WordCloud;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordCloudFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = WordCloud.class.getSimpleName();
    private Spinner mobileNumberSp;

    private final SmsBackupServiceImpl smsBackupService;

    @Inject
    public WordCloudFragment(SmsBackupServiceImpl smsBackupService) {
        this.smsBackupService = smsBackupService;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_word_cloud, container, false);
        view.findViewById(R.id.sms_in_out_box_checkbox).setOnClickListener(this);
        List<String> mobileNumbers = smsBackupService.getSmsBackupMobileNumbersTop10();
        mobileNumbers.add(0, "(.*)");
        mobileNumberSp = view.findViewById(R.id.sms_mobile_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, mobileNumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mobileNumberSp.setAdapter(adapter);
        mobileNumberSp.setOnItemSelectedListener(this);
        return view;
    }

    // listen to check box selection
    @Override
    public void onClick(View view) {
        boolean checked = false;
        if (view instanceof RadioButton) {
            checked = ((RadioButton) view).isChecked();
        } else if (view instanceof CheckBox) {
            checked = ((CheckBox) view).isChecked();
        }
        // Check which radio button was clicked
        if (view.getId() == R.id.sms_in_out_box_checkbox) {
            Log.d(TAG, String.format("checkbox in-outbox: %s", checked));
            if (checked) {
                RxBus.getInstance().publish(WordCloudEvent.builder()
                        .eventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                        .smsTypeInbox()
                        .setValue(mobileNumberSp.getSelectedItem().toString())
                        .build());
            } else {
                RxBus.getInstance().publish(WordCloudEvent.builder()
                        .eventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                        .smsTypeOutbox()
                        .setValue(mobileNumberSp.getSelectedItem().toString())
                        .build());
            }
        }
    }

    // Listen to selections in the mobil number dropdown
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        RxBus.getInstance().publish(
                WordCloudEvent.builder()
                        .eventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                        .setValue(mobileNumberSp.getSelectedItem().toString())
                        .smsTypeAll()
                        .build());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
