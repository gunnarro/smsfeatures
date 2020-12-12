package com.gunnarro.android.ughme.ui.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.sms.Sms;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SmsAdapter extends ArrayAdapter<Sms> {

    public SmsAdapter(Context context, ArrayList<Sms> smsList) {
        super(context, 0, smsList);
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_sms, parent, false);
        }
        // Get the data item for this position
        Sms sms = getItem(position);
        if (sms != null) {
            TextView header = convertView.findViewById(R.id.sms_header);
            TextView body = convertView.findViewById(R.id.sms_body);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.UK);
            header.setText(String.format("[%s] %s", dateFormat.format(new Date(sms.getTimeMs())), sms.getAddress()));
            body.setText(sms.getBody());
        }
        return convertView;
    }
}
