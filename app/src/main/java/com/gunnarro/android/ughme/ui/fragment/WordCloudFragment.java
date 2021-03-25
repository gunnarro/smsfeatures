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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
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
public class WordCloudFragment extends Fragment {

    private static final String TAG = WordCloudFragment.class.getSimpleName();

    private static final String ALL = "All";
    private static final String ALL_SEARCH = "(.*)";
    private final String selectedMobileNumber = ALL_SEARCH;
    @Inject
    BuildWordCloudTask buildWordCloudTask;
    @Inject
    SmsBackupServiceImpl smsBackupService;
    private WordCloudView wordCloudView;
    private Menu optionsMenu;
    private List<String> mobileNumbers;
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
        mobileNumbers.add(0, ALL);
        RxBus.getInstance().listen().subscribe(getInputObserver());
        return view;
    }

    /**
     * It's where you should place actions that have a global impact on the app
     * Only create the initial menu state and not make changes during the activity lifecycle.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // keep a reference to options menu
        optionsMenu = menu;
        // clear current menu items
        menu.clear();
        // set fragment specific menu items
        inflater.inflate(R.menu.word_cloud_menu, menu);
        MenuItem m = menu.findItem(R.id.mobile_dropdown_menu);
        Spinner spinner = (Spinner) m.getActionView();
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, mobileNumbers);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "spinner.onItemSelected mobile:" + mobileNumbers.get(position));
                //updateWordCloudView(mobileNumbers.get(position), WordCloudEvent.MESSAGE_TYPE_ALL);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Log.d(TAG, "onCreateOptionsMenu.menu: " + menu);
    }

    /**
     * Modify the options menu based on events that occur during the fragment lifecycle here,
     */
    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
    }

    /**
     * The contextual action mode is the preferred technique for displaying contextual actions when available.
     *
     * @Override public void onCreateContextMenu(@NotNull ContextMenu menu, @NotNull View v, ContextMenu.ContextMenuInfo menuInfo) {
     * super.onCreateContextMenu(menu, v, menuInfo);
     * getActivity().getMenuInflater().inflate(R.menu.word_cloud_context_menu, menu);
     * <p>
     * menu.setHeaderTitle("Context Menu");
     * menu.add(0, v.getId(), 0, "Upload");
     * menu.add(0, v.getId(), 0, "Search");
     * }
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG + ".onOptionsItemSelected", "selected: " + item.getTitle());
        // save the checkbox selection
        item.setChecked(!item.isChecked());
        handleOptionsMenuSelection(selectedMobileNumber, optionsMenu.findItem(R.id.sms_inbox_menu).isChecked(), optionsMenu.findItem(R.id.sms_outbox_menu).isChecked());
        return true;
    }

    private void handleOptionsMenuSelection(String mobileNumber, boolean isInbox, boolean isOutbox) {
        Log.d(TAG + ".handleOptionsMenuSelection", String.format("mobile=%s, inbox=%s, outbox=%s", mobileNumber, isInbox, isOutbox));
        if (isInbox && isOutbox) {
            updateWordCloudView(mobileNumber, WordCloudEvent.MESSAGE_TYPE_ALL);
        } else if (isOutbox) {
            updateWordCloudView(mobileNumber, WordCloudEvent.MESSAGE_TYPE_OUTBOX);
        } else if (isInbox) {
            updateWordCloudView(mobileNumber, WordCloudEvent.MESSAGE_TYPE_INBOX);
        } else {
            // do nothing
           // updateWordCloudView(mobileNumber, null);
        }
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
        Log.d(TAG, String.format("updateWordCloudView mobile: %s, smsType=%s", contactName, smsType));
        progressDialog = buildProgressDialog();
        progressDialog.show();
        // start background task for building word cloud, which may take som time, based on number of sms
        buildWordCloudTask.buildWordCloudEventBus(mapPreferences(), Dimension.builder().width(wordCloudView.getWidth()).height(wordCloudView.getHeight()).build(), contactName, smsType);
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
