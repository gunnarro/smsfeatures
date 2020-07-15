package com.gunnarro.android.ughme.location;

import org.jetbrains.annotations.NotNull;

public class Position {
    public static final String URL_GOOGLE_MAPS = "http://maps.google.com/maps?q=%s,%s";

    private long time;
    private String mobileNumber;
    private String address;
    private double latitude;
    private double longitude;
    private int altitude;

    public Position(String mobileNumber, double latitude, double longitude, long time) {
        this.mobileNumber = mobileNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public String createGoogleMapUrl() {
        // Latitude and longitude
        return String.format(URL_GOOGLE_MAPS, latitude, longitude);
    }


    @Override
    @NotNull
    public String toString() {
        final StringBuilder sb = new StringBuilder("Position{");
        sb.append("time=").append(time);
        sb.append(", mobileNumber='").append(mobileNumber);
        sb.append(", address='").append(address);
        sb.append(", latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append(", altitude=").append(altitude);
        sb.append('}');
        return sb.toString();
    }
}
