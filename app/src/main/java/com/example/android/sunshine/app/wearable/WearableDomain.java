package com.example.android.sunshine.app.wearable;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by baybora on 2/16/16.
 */
public class WearableDomain {

    private Context context;
    private String cityName;
    private String highTemp;
    private String lowTemp;
    private boolean isMetric;
    private int weatherResourceId;
    private GoogleApiClient googleApiClient;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(String lowTemp) {
        this.lowTemp = lowTemp;
    }

    public boolean isMetric() {
        return isMetric;
    }

    public void setIsMetric(boolean isMetric) {
        this.isMetric = isMetric;
    }

    public int getWeatherResourceId() {
        return weatherResourceId;
    }

    /***
     * weather id
     * @param weatherResourceId
     */
    public void setWeatherResourceId(int weatherResourceId) {
        this.weatherResourceId = weatherResourceId;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }
}
