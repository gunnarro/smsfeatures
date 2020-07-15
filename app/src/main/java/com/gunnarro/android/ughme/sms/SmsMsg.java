package com.gunnarro.android.ughme.sms;

import androidx.annotation.NonNull;

public class SmsMsg {

    private final String toMobilePhoneNumber;
    private final String msg;
    public SmsMsg(String toMobilePhoneNumber, String msg) {
        this.toMobilePhoneNumber = toMobilePhoneNumber;
        this.msg = msg;
    }

    public String getToMobilePhoneNumber() {
        return toMobilePhoneNumber;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isTraceSMS() {
        return msg != null && msg.trim().equalsIgnoreCase(ActionEnum.TRACE.name());
    }

    public boolean isForwardSMS() {
        return msg != null && msg.trim().equalsIgnoreCase(ActionEnum.FORWARD.name());
    }

    @Override
    @NonNull
    public String toString() {
        return toMobilePhoneNumber + ": " + msg;
    }

    private enum ActionEnum {
        TRACE, ALARM, WAKEUP, FORWARD
    }
}
