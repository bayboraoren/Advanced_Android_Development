package com.example.android.sunshine.app.wearable;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

/**
 * Created by baybora on 2/16/16.
 */
public class WearableTask extends AsyncTask<Node, Void, Void> {


    public static final String LOG_TAG = WearableTask.class.getSimpleName();
    private final String mCityName;
    private final String mHighTemp;
    private final String mLowTemp;
    private final boolean mIsMetric;
    private final int mWeatherResourceId;
    private final GoogleApiClient mGoogleApiClient;


    public WearableTask(WearableDomain wearableDomain) {
        mCityName = wearableDomain.getCityName();
        mHighTemp = wearableDomain.getHighTemp();
        mLowTemp = wearableDomain.getLowTemp();
        mIsMetric = wearableDomain.isMetric();
        mWeatherResourceId = wearableDomain.getWeatherResourceId();
        mGoogleApiClient = wearableDomain.getGoogleApiClient();
    }

    @Override
    protected Void doInBackground(Node... nodes) {

        PutDataMapRequest dataMap = PutDataMapRequest.create("/sunshine/event");

        dataMap.getDataMap().putInt("weather-resource-id", mWeatherResourceId);
        dataMap.getDataMap().putString("city-name", mCityName);
        dataMap.getDataMap().putString("high-temp", String.format("%.0f", new Double(mHighTemp)));
        dataMap.getDataMap().putString("low-temp", String.format("%.0f", new Double(mLowTemp)));
        dataMap.getDataMap().putBoolean("is-metric", mIsMetric);
        dataMap.getDataMap().putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Wearable.DataApi
                .putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {

            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(LOG_TAG, "Google Api Client Connection OK : " + dataItemResult.getStatus() + ", " + dataItemResult.getDataItem().getUri());
            }

        });


        Log.d(LOG_TAG, "/sunshine/event status " + getStatus());

        return null;
    }



}
