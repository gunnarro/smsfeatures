package com.gunnarro.android.ughme.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.gunnarro.android.ughme.R;

public class ConfirmDialogFragment extends DialogFragment {

    private final static String TITLE_KEY = "title";
    private final static String MESSAGE_KEY = "message";

    /**
     * // Default constructor required for DialogFragment
     */
    public ConfirmDialogFragment() {
    }

    public static ConfirmDialogFragment newInstance(String title, String message) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getArguments().getString(TITLE_KEY));
        alertDialogBuilder.setMessage(getArguments().getString(MESSAGE_KEY));
        alertDialogBuilder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((DialogActionListener)getParentFragment()).onDialogAction(DialogActionListener.OK_ACTION);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((DialogActionListener)getParentFragment()).onDialogAction(DialogActionListener.CANCEL_ACTION);
            }
        });
        return alertDialogBuilder.create();
    }
}
