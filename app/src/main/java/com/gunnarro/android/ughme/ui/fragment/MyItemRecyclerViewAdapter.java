package com.gunnarro.android.ughme.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.ui.fragment.domain.ListItem;
import com.gunnarro.android.ughme.ui.fragment.domain.ListViewHolder;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.gunnarro.android.ughme.ui.fragment.domain.ListItem} and makes a call to the
 * specified {@link ListFragmentInteractionListener}.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private final List<ListItem> mValues;
    private final ListFragmentInteractionListener mListener;

    MyItemRecyclerViewAdapter(List<ListItem> items, ListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getId());
        //holder.mContentView.setText(mValues.get(position).getContent());
        //holder.mDetailsView.setText(mValues.get(position).getDetails());

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.getmItem());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}
