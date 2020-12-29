package com.gunnarro.android.ughme.ui.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SmsAdapter extends ArrayAdapter<Sms> {

    public SmsAdapter(Context context, ArrayList<Sms> smsList) {
        super(context, 0, smsList);
    }

    @NotNull
    @Override
    public View getView(int position, View itemView, @NotNull ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_sms, parent, false);
        }
        // Get the data item for this position
        Sms sms = getItem(position);
        if (sms != null) {
            TextView header = itemView.findViewById(R.id.sms_header);
            TextView body = itemView.findViewById(R.id.sms_body);
            TextView footer = itemView.findViewById(R.id.sms_footer);
            header.setText(Utility.formatTime(sms.getTimeMs()));
            body.setText(sms.getBody());
            footer.setText(String.format("from: %s", sms.getContactName()));

            if (sms.getType().equals("1")) {
                itemView.setBackgroundResource(R.color.colorSmsReceived);
                body.setGravity(Gravity.START);
                header.setGravity(Gravity.START);
            } else {
                itemView.setBackgroundResource(R.color.colorSmsSent);
                body.setGravity(Gravity.END);
                header.setGravity(Gravity.END);
            }
        }
        return itemView;
    }
}
