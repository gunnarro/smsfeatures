package com.gunnarro.android.ughme.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.gunnarro.android.ughme.R;

import org.jetbrains.annotations.NotNull;

public class ConfirmDialogFragment extends DialogFragment {

    private final static String TITLE_KEY = "title";
    private final static String MESSAGE_KEY = "message";

    /**
     * Default constructor required for DialogFragment
     */
    public ConfirmDialogFragment() {
    }

    static ConfirmDialogFragment newInstance(String title, String message) {
        ConfirmDialogFragment frag = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);
        frag.setArguments(args);
        return frag;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        assert getArguments() != null;
        alertDialogBuilder.setTitle(getArguments().getString(TITLE_KEY));
        alertDialogBuilder.setMessage(getArguments().getString(MESSAGE_KEY));
        alertDialogBuilder.setPositiveButton(R.string.btn_ok, (dialog, whichButton) -> {
            assert getParentFragment() != null;
            ((DialogActionListener) getParentFragment()).onDialogAction(DialogActionListener.OK_ACTION);
        });
        alertDialogBuilder.setNegativeButton(R.string.btn_cancel, (dialog, whichButton) -> {
            assert getParentFragment() != null;
            ((DialogActionListener) getParentFragment()).onDialogAction(DialogActionListener.CANCEL_ACTION);
        });
        return alertDialogBuilder.create();
    }
}
