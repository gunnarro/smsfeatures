package com.gunnarro.android.ughme.ui.fragment;

import com.gunnarro.android.ughme.ui.fragment.domain.ListItem;

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 */
public interface ListFragmentInteractionListener {

    void onListFragmentInteraction(ListItem item);
}
