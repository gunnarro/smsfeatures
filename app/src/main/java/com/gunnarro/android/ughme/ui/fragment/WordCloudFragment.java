package com.gunnarro.android.ughme.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.BuildWordCloudTask;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.ui.view.WordCloudView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class WordCloudFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    private static final String TAG = WordCloudFragment.class.getSimpleName();

    private static final String ALL = "All";
    private static final String ALL_SEARCH = "(.*)";

    @Inject
    BuildWordCloudTask buildWordCloudTask;

    @Inject
    SmsBackupServiceImpl smsBackupService;

    private WordCloudView wordCloudView;

    private List<String> mobileNumbers;
    private String selectedMobileNumber = ALL_SEARCH;

    private Dialog progressDialog;

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
        setHasOptionsMenu(true);
        wordCloudView = view.findViewById(R.id.word_cloud_view);
        mobileNumbers = smsBackupService.getSmsBackupMobileNumbersTop10();
        RxBus.getInstance().listen().subscribe(getInputObserver());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.word_cloud_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu.menu: " + menu.getItem(0).getSubMenu());
        // mobileNumbers.forEach(s -> menu.findItem(menu.getItem(0).getGroupId()).getSubMenu().add(s));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu.menu: " + menu.getItem(0).getTitle());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.sms_select_mobile_number: {
                showPopupMenu();
                return true;
            }*/
            case R.id.sms_sent: {
                item.setChecked(!item.isChecked());
                updateWordCloudView(selectedMobileNumber, WordCloudEvent.MESSAGE_TYPE_OUTBOX);
                return true;
            }
            case R.id.sms_received: {
                item.setChecked(!item.isChecked());
                updateWordCloudView(selectedMobileNumber, WordCloudEvent.MESSAGE_TYPE_INBOX);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Save selected mobile number
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        this.selectedMobileNumber = item.getTitle().toString().replace(ALL, ALL_SEARCH);
        updateWordCloudView(selectedMobileNumber, WordCloudEvent.MESSAGE_TYPE_ALL);
        Log.d(TAG, "onMenuItemClick: selected mobile number: " + selectedMobileNumber);
        return true;
    }

    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                Log.d(TAG + ".getInputObserver.onSubscribe", "getInputObserver.onSubscribe");
            }

            @Override
            public void onNext(@NotNull Object obj) {
                //Log.d(buildTag("getInputObserver.onNext"), String.format("Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof WordCloudEvent) {
                    WordCloudEvent event = (WordCloudEvent) obj;
                    Log.d(TAG + ".getInputObserver.onNext", String.format("handle event: %s", event.toString()));
                    if (event.isUpdateEvent()) {
                        // hide progress dialog
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Log.e(TAG + ".getInputObserver.onError", String.format("%s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(TAG + ".getInputObserver.onComplete"
                        , "");
            }
        };
    }

    private void updateWordCloudView(String contactName, String smsType) {
        progressDialog = buildProgressDialog();
        progressDialog.show();
        // start background task for building word cloud, which may take som time, based on number of sms
        buildWordCloudTask.buildWordCloudEventBus(mapPreferences(), new Dimension(wordCloudView.getWidth(), wordCloudView.getHeight()), contactName, smsType);
    }

    private Settings mapPreferences() {
        Settings settings = new Settings();
        int words = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_number_of_words), settings.numberOfWords);
        int maxFontSize = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_word_max_font_size), settings.maxWordFontSize);
        int minFontSize = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_word_min_font_size), settings.minWordFontSize);
        int radiusStep = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_radius_step), settings.radiusStep);
        int offsetStep = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_offset_step), settings.offsetStep);

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
