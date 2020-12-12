package com.gunnarro.android.ughme.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.sms.Sms;
import com.gunnarro.android.ughme.sms.SmsReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * view for query search in sms inbox
 */
public class SmsSearchFragment extends Fragment {
    private static final String LOG_TAG = SmsSearchFragment.class.getSimpleName();

    public static SmsSearchFragment newInstance() {
        SmsSearchFragment fragment = new SmsSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sms_search, container, false);
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
        List<Sms> smsList = getSmsInbox(null);
        Log.d(LOG_TAG, String.format("searchSms: number of sms: %s, search after: %s", smsList.size(), searchAfter));
        List<Sms> result = smsList.stream().filter(sms -> sms.getBody().toLowerCase().contains(searchAfter.toLowerCase())).collect(Collectors.toList());
        Snackbar.make(Objects.requireNonNull(getView()), String.format("Searched after: %s, hits: %s, time: %s ms", searchAfter, result.size(), (System.currentTimeMillis() - startTime)), Snackbar.LENGTH_LONG).show();
        return result;
    }


    private List<Sms> getSmsInbox(String mobileNumber) {
        SmsReader smsReader = new SmsReader(Objects.requireNonNull(getActivity()).getApplicationContext());
        List<Sms> inbox = smsReader.getSMSInbox(false, mobileNumber);
        Log.d(LOG_TAG, "getSmsInbox: sms for mobile number: " + mobileNumber + ", number of sms: " + inbox.size());
        return inbox;
    }

}
