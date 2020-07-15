package com.gunnarro.android.ughme.ui.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.fragment.app.Fragment;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.observable.RxBus;

public class WordCloudFragment extends Fragment implements View.OnClickListener {

    public enum WordCloudTypeEnum {
        MESSAGE, NUMBER, DATE
    }

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
        view.findViewById(R.id.sms_msg_radio_btn).setOnClickListener(this);
        view.findViewById(R.id.sms_number_radio_btn).setOnClickListener(this);
        view.findViewById(R.id.sms_datetime_radio_btn).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.sms_msg_radio_btn:
                if (checked) {
                    RxBus.getInstance().publish(WordCloudTypeEnum.MESSAGE);
                    break;
                }
            case R.id.sms_number_radio_btn:
                if (checked) {
                    RxBus.getInstance().publish(WordCloudTypeEnum.NUMBER);
                    break;
                }
            case R.id.sms_datetime_radio_btn:
                if (checked) {
                    RxBus.getInstance().publish(WordCloudTypeEnum.DATE);
                    break;
                }
            default:
                break;
        }
    }
}
