package com.gunnarro.android.ughme.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.sms.Sms;
import com.gunnarro.android.ughme.ui.fragment.domain.ListItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ListFragmentInteractionListener}
 * interface.
 */
public class ListItemFragment extends Fragment {

    private static final String LOG_TAG = ListItemFragment.class.getSimpleName();

    private static final String ARG_TITLE = "fragment-title";
    private ListFragmentInteractionListener mListener;
    private String mTitle;

    private MyItemRecyclerViewAdapter listItemAdapter;
    private List<ListItem> itemList = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ListItemFragment newInstance(String name) {
        ListItemFragment fragment = new ListItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            listItemAdapter = new MyItemRecyclerViewAdapter(itemList, mListener);
            recyclerView.setAdapter(listItemAdapter);
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ListFragmentInteractionListener) {
            mListener = (ListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
        //  listen RxJava event here
        RxBus.getInstance().listen().subscribe(getInputObserver());
        Log.d(LOG_TAG, "onAttach: : Registerer RxBus listener");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(LOG_TAG, "onSubscribe:");
            }

            @Override
            public void onNext(Object obj) {
                Log.d(LOG_TAG, String.format("onNext: Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof List<?>) {
                    Log.d(LOG_TAG, "onNext: update list");
                    List<Sms> smsList = Collections.unmodifiableList((List<Sms>) obj);
                    itemList.clear();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    smsList.forEach(s -> itemList.add(new ListItem(dateFormat.format(new Date(s.getTimeMs())) + " from " + s.getAddress() + "\n" + s.getBody())));
                    listItemAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, String.format("onError: %s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(LOG_TAG, "onComplete");
            }
        };
    }
}
