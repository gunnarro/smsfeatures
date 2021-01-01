package com.gunnarro.android.ughme.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.ui.adapter.SmsAdapter;
import com.gunnarro.android.ughme.utility.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * view for query search in sms inbox
 */
@AndroidEntryPoint
public class SmsSearchFragment extends Fragment {
    private static final String LOG_TAG = SmsSearchFragment.class.getSimpleName();
    private List<Sms> smsList = new ArrayList<>();

    private final SmsBackupServiceImpl smsBackupService;

    @Inject
    public SmsSearchFragment(@NonNull SmsBackupServiceImpl smsBackupService) {
        this.smsBackupService = smsBackupService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        SearchView smsSearchView = view.findViewById(R.id.view_sms_search);
        smsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Sms> smsList = searchSms(query);
                ListView listView = view.findViewById(R.id.view_sms_search_result);
                SmsAdapter adapter = new SmsAdapter(view.getContext(), new ArrayList<>(smsList));
                listView.setAdapter(adapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        Log.d("smsSearchFragment", "onCreateView");
        return view;
    }

    private List<Sms> searchSms(String searchAfter) {
        long startTime = System.currentTimeMillis();
        updateSmsList();
        Log.d(LOG_TAG, String.format("searchSms: number of sms: %s, search after: %s", smsList.size(), searchAfter));
        List<Sms> result = smsList.stream().filter(sms -> sms.getBody().toLowerCase().contains(searchAfter.toLowerCase())).collect(Collectors.toList());
        Snackbar.make(Objects.requireNonNull(getView()), String.format("Searched after: %s, hits: %s, time: %s ms", searchAfter, result.size(), (System.currentTimeMillis() - startTime)), Snackbar.LENGTH_LONG).show();
        return result;
    }

    private void updateSmsList() {
        smsList = smsBackupService.getSmsBackup();
        Long lastBackupSmsTimeMs = !smsList.isEmpty() ? smsList.get(0).getTimeMs() : null;
        List<Sms> inbox = smsBackupService.getSmsInbox(null, lastBackupSmsTimeMs);
        Log.d(LOG_TAG, String.format("updateSmsList: sms inbox: %s", inbox));
        Log.d(LOG_TAG, String.format("updateSmsList: sms backup: %s", smsList));
        List<Sms> newSmsList = Utility.diffLists(inbox, smsList);
        Utility.mergeList(smsList, newSmsList);
        Log.d(LOG_TAG, String.format("updateSmsList: sms after merge: %s", smsList.size()));
    }

}
