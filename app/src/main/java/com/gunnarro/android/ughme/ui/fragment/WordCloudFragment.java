package com.gunnarro.android.ughme.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
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

import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.BuildWordCloudTask;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.ui.view.WordCloudView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@AndroidEntryPoint
public class WordCloudFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = WordCloudFragment.class.getSimpleName();
    private Spinner mobileNumberSp;

    @Inject
    BuildWordCloudTask buildWordCloudTask;

    @Inject
    SmsBackupServiceImpl smsBackupService;

    private WordCloudView wordCloudView;


    /**
     * default constructor, needed when screen is rotated
     */
    @Inject
    public WordCloudFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_cloud, container, false);
        wordCloudView = view.findViewById(R.id.word_cloud_view);
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
                updateWordCloudView(mobileNumberSp.getSelectedItem().toString(), WordCloudEvent.MESSAGE_TYPE_INBOX);
                /*
                RxBus.getInstance().publish(WordCloudEvent.builder()
                        .eventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                        .smsTypeInbox()
                        .setValue(mobileNumberSp.getSelectedItem().toString())
                        .build());

                 */
            } else {
                updateWordCloudView(mobileNumberSp.getSelectedItem().toString(), WordCloudEvent.MESSAGE_TYPE_OUTBOX);
                /*
                RxBus.getInstance().publish(WordCloudEvent.builder()
                        .eventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                        .smsTypeOutbox()
                        .setValue(mobileNumberSp.getSelectedItem().toString())
                        .build());
                 */

                RxBus.getInstance()
                        .listen()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();
            }
        }
    }


    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                Log.d("getInputObserver.onSubscribe", "getInputObserver.onSubscribe");
            }

            @Override
            public void onNext(@NotNull Object obj) {
                //Log.d(buildTag("getInputObserver.onNext"), String.format("Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof WordCloudEvent) {
                    WordCloudEvent event = (WordCloudEvent) obj;
                    Log.d("getInputObserver.onNext", String.format("handle event: %s", event.toString()));
                    // rebuild word cloud based on selected sms type and mobile number
                    //buildWordCloud(getWidth(), getHeight(), event.getValue(), event.getSmsType());
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Log.e("getInputObserver.onError", String.format("%s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d("getInputObserver.onComplete"
                        , "");
            }
        };
    }

    // Listen to selections in the mobil number dropdown
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, String.format("selected viewId: %s", parent.getSelectedItem().getClass()));
        if (id == R.id.sms_mobile_spinner) {
            updateWordCloudView(mobileNumberSp.getSelectedItem().toString(), WordCloudEvent.MESSAGE_TYPE_ALL);
            /*
            RxBus.getInstance().publish(
                    WordCloudEvent.builder()
                            .eventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE)
                            .value(mobileNumberSp.getSelectedItem().toString())
                            .smsTypeAll()
                            .build());

 */
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void updateWordCloudView(String contactName, String smsType) {
        long startTime = System.currentTimeMillis();
        Dialog dlg = buildProgressDialog();
        dlg.show();

        Future<List<Word>> future = buildWordCloudTask.buildWordCloud(mapPreferences(), new Dimension(wordCloudView.getWidth(), wordCloudView.getHeight()), contactName, smsType);

        try {
            // will block main (also known as UI) thread here until task has finished
           // wordCloudView.updateWordList(future.get());
            List<Word> wordList = future.get();
            RxBus.getInstance().publish(
                    WordCloudEvent.builder()
                            .eventType(WordCloudEvent.WordCloudEventTypeEnum.UPDATE_MESSAGE)
                            .smsTypeAll()
                            .wordList(wordList)
                            .build());
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            // refresh view in order to show the new list of words, calls onCreate
            // wordCloudView.invalidate();
            // wordCloudView.requestLayout();
            // finally hide progress dialog
            dlg.dismiss();
        }
        Log.i(TAG, String.format("Finished! time=%s ms, thread: %s", (System.currentTimeMillis() - startTime), Thread.currentThread().getName()));
    }

    private Settings mapPreferences() {
        Settings settings = new Settings();
        int words = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(R.string.pref_number_of_words), settings.numberOfWords);
        int maxFontSize = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(R.string.pref_word_max_font_size), settings.maxWordFontSize);
        int minFontSize = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(R.string.pref_word_min_font_size), settings.minWordFontSize);
        int radiusStep = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(R.string.pref_radius_step), settings.radiusStep);
        int offsetStep = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(getResources().getString(R.string.pref_offset_step), settings.offsetStep);

        settings.numberOfWords = words;
        settings.maxWordFontSize = maxFontSize;
        settings.minWordFontSize = minFontSize;
        settings.radiusStep = radiusStep;
        settings.offsetStep = offsetStep;
        return settings;
    }

    private AlertDialog buildProgressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());
        alertDialog.setView(R.layout.dlg_progress);
        return alertDialog.setTitle("Build WordCloud").setCancelable(true).create();
    }
}
