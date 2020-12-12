package com.gunnarro.android.ughme;

import com.gunnarro.android.ughme.sms.Sms;

import org.junit.Assert;
import org.junit.Test;

public class SmsTest {

    @Test
    public void matchSmsType() {
        String value = "+4723545400";
        Sms sms = Sms.builder().setType(".*").setBody("msg").setAddress("+4723545400").build();
        Assert.assertEquals(Boolean.TRUE, sms.getAddress().matches(value.replace("+", "\\+")));
    }
}
