package com.gunnarro.android.ughme.ui.fragment;

import android.app.AlertDialog;
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
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class WordCloudFragment extends Fragment {

    private static final String ALL = "All";
    public static final String ALL_SEARCH = "(.*)";
    private String selectedMobileNumber = ALL_SEARCH;
    @Inject
    BuildWordCloudTask buildWordCloudTask;
    @Inject
    SmsBackupServiceImpl smsBackupService;
    @Inject
    PreferencesFragment preferencesFragment;

    private WordCloudView wordCloudView;
    private Menu optionsMenu;
    private List<String> mobileNumbers;
    private AlertDialog progressDialog;

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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, mobileNumbers);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMobileNumber = mobileNumbers.get(position);
                Log.d(Utility.buildTag(getClass(), "onCreateOptionsMenu"), "spinner.onItemSelected mobile:" + selectedMobileNumber);
                handleOptionsMenuSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Log.d(Utility.buildTag(getClass(), "onCreateOptionsMenu"), "menu: " + menu);
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
     * <p>
     * menu.setHeaderTitle("Context Menu");
     * menu.add(0, v.getId(), 0, "Upload");
     * menu.add(0, v.getId(), 0, "Search");
     * }
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(Utility.buildTag(getClass(), "onOptionsItemSelected"), "selected: " + item.getTitle());
        if (item.getItemId() == R.id.sms_inbox_menu || item.getItemId() == R.id.sms_outbox_menu) {
            // must save the checkbox selection
            item.setChecked(!item.isChecked());
            handleOptionsMenuSelection();
        }
        return true;
    }

    private void handleOptionsMenuSelection() {
        boolean isInbox = optionsMenu.findItem(R.id.sms_inbox_menu).isChecked();
        boolean isOutbox = optionsMenu.findItem(R.id.sms_outbox_menu).isChecked();
        String mobileNumber = selectedMobileNumber.equals(ALL) ? ALL_SEARCH : selectedMobileNumber;
        Log.d(Utility.buildTag(getClass(), "handleOptionsMenuSelection"), String.format("mobile=%s, inbox=%s, outbox=%s", mobileNumber, isInbox, isOutbox));
        if (isInbox && isOutbox) {
            updateWordCloudView(mobileNumber, WordCloudEvent.MESSAGE_TYPE_ALL);
        } else if (isOutbox) {
            updateWordCloudView(mobileNumber, WordCloudEvent.MESSAGE_TYPE_OUTBOX);
            //viewFragment(preferencesFragment);only for testing
        } else if (isInbox) {
            updateWordCloudView(mobileNumber, WordCloudEvent.MESSAGE_TYPE_INBOX);
        } else {
            // clear current view
            Log.d(Utility.buildTag(getClass(), "handleOptionsMenuSelection"), "none is selected");
        }
    }

    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                Log.d(Utility.buildTag(getClass(), "getInputObserver.onSubscribe"), "");
            }

            @Override
            public void onNext(@NotNull Object obj) {
                if (obj instanceof WordCloudEvent) {
                    WordCloudEvent event = (WordCloudEvent) obj;
                    Log.d(Utility.buildTag(getClass(), "getInputObserver.onNext"), String.format("handle event: %s", event.toString()));
                    if (event.isUpdateEvent()) {
                        // hide progress dialog
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    } else if (event.isProgressEvent()) {
                        // update progress
                        Log.i(Utility.buildTag(getClass(), "onNext"), "progress: " + event.getProgressMsg());
                        if (progressDialog != null) {
                            // progressDialog.setMessage(event.getProgressMsg());
                            // progressDialog.incrementProgressBy(progressDialog.getProgress() + event.getProgressStep());
                        }
                    }
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Log.e(Utility.buildTag(getClass(), "getInputObserver.onError"), String.format("%s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(Utility.buildTag(getClass(), "getInputObserver.onComplete"), "");
            }
        };
    }

    private void updateWordCloudView(String contactName, String smsType) {
        Log.d(Utility.buildTag(getClass(), "updateWordCloudView"), String.format("mobile=%s, smsType=%s", contactName, smsType));
        progressDialog = buildAlertDialog();
        //progressDialog.show();
        // start background task for building word cloud, which may take som time, based on number of sms
        buildWordCloudTask.buildWordCloudEventBus(mapPreferences(), Dimension.builder().width(wordCloudView.getWidth()).height(wordCloudView.getHeight()).build(), contactName, smsType);
    }

    private void viewFragment(@NonNull Fragment fragment) {
        Log.d(Utility.buildTag(getClass(), "viewFragment"), "fragment: " + fragment.getTag());
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private Settings mapPreferences() {
        Settings settings = new Settings();
        // read selected preference values, and use settings default values if not set
        settings.numberOfWords = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_number_of_words), settings.numberOfWords);
        settings.minCharsInWord = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_word_min_chars), settings.minCharsInWord);
        settings.maxWordFontSize = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_word_max_font_size), settings.maxWordFontSize);
        settings.minWordFontSize = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_word_min_font_size), settings.minWordFontSize);
        settings.radiusStep = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_radius_step), settings.radiusStep);
        settings.offsetStep = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt(getResources().getString(R.string.pref_offset_step), settings.offsetStep);
        settings.wordRotation = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("pref_word_rotate", true);
        settings.colorSchema = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("pref_color_schema", "");
        settings.fontType = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("pref_font_type", "");
        // set regexp for word selection
        settings.wordMatchRegex = String.format(settings.wordMatchRegexFormat, settings.minCharsInWord);
        Log.d(Utility.buildTag(getClass(), "mapPreferences"), settings.toString());
        return settings;
    }

    /**
     * A ProgressDialog is used when we want to prevent the user from interacting with the application while waiting. The Dialog aspect freezes the user from doing anything until it is dismissed
     */
    private AlertDialog buildAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getActivity());
        alertDialog.setView(R.layout.dlg_progress);
        return alertDialog.setTitle("Build WordCloud").setCancelable(true).create();
    }

/*
    private ProgressDialog buildProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMax(100);
        progressDialog.setMessage("Start...");
        progressDialog.setTitle("Build word cloud");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        return progressDialog;
    }

 */
}
