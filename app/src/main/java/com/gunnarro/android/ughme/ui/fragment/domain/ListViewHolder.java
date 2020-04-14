package com.gunnarro.android.ughme.ui.fragment.domain;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.gunnarro.android.ughme.R;

public class ListViewHolder extends RecyclerView.ViewHolder {

    public final View mView;
    public final TextView mIdView;
   // public final TextView mContentView;
   // public final TextView mDetailsView;
    public ListItem mItem;

    public ListViewHolder(View view) {
        super(view);
        mView = view;
        mIdView = view.findViewById(R.id.item_number);
      //  mContentView = view.findViewById(R.id.item_content);
      //  mDetailsView = view.findViewById(R.id.item_details);
    }

    @Override
    public String toString() {
        return mItem.toString();
    }
}

