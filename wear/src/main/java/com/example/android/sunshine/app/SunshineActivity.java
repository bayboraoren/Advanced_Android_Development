package com.example.android.sunshine.app;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SunshineActivity extends WearableActivity  implements
        DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = SunshineActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("E, MMM dd yyyy", Locale.US);

    private static final SimpleDateFormat AMBIENT_HOUR_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private TextView mClockView;
    private TextView mDate;
    private ImageView mDaysWeatherImage;
    private TextView mHighTemp;
    private TextView mLowTemp;
    private TextView mCityName;

    private BoxInsetLayout mContainerView;
    //private TextView mTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunshine);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        //mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);
        mDate= (TextView) findViewById(R.id.date);
        mDaysWeatherImage = (ImageView) findViewById(R.id.days_weather_image);
        mHighTemp = (TextView ) findViewById(R.id.higher_degree);
        mLowTemp = (TextView ) findViewById(R.id.lower_degree);
        mCityName = (TextView ) findViewById(R.id.city_name);

        initGoogleApiClient();

        updateDisplay();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }



    private void initGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {

        Date date = new Date();

        if (isAmbient()) {
            //mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            //mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);
            mDate.setText(AMBIENT_DATE_FORMAT.format(date));
            mClockView.setText(AMBIENT_HOUR_FORMAT.format(date));
        } else {
            //mContainerView.setBackground(null);
            //mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }


    //GOOGLE CLIENT API

    @Override
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "Connected to Google Api Service");
        }
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }



    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG, " GOOGLE API CONNECTION SUSPENDED : " + i);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG," GOOGLE API CONNECTION FAILED : " + connectionResult.getErrorMessage());
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        final String fahrenheit = "\u2109";
        final String celcius = "\u2103";
        String metric = celcius;

        Log.d(LOG_TAG, "*** ON DATA CHANGED");

        for (DataEvent event: dataEvents) {

            Log.d(LOG_TAG,"Event received: ");

            String eventUri = event.getDataItem().getUri().toString();

            if (eventUri.contains ("/sunshine/event")) {

                DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());

                String cityName = dataItem.getDataMap().getString("city-name");
                mCityName.setText(cityName);

                boolean isMetric = dataItem.getDataMap().getBoolean("is-metric");

                if(!isMetric){
                    metric = fahrenheit;
                }

                String highTemp = dataItem.getDataMap().getString("high-temp") + metric;
                mHighTemp.setText(highTemp);
                String lowTemp = dataItem.getDataMap().getString("low-temp") + metric;
                mLowTemp.setText(lowTemp);


                int weatherResourceId = dataItem.getDataMap().getInt("weather-resource-id");

                int resourceId = Utility.getArtResourceForWeatherCondition(weatherResourceId);
                mDaysWeatherImage.setImageResource(resourceId);


                Log.d(LOG_TAG, cityName + " " + highTemp + " " + lowTemp + " is metric : " + isMetric);

            }
        }
    }


}
