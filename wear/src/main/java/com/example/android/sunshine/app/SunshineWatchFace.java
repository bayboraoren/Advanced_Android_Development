/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SunshineWatchFace extends CanvasWatchFaceService {

    public static final String LOG_TAG = SunshineWatchFace.class.getSimpleName();

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(60);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;


    final String fahrenheit = "\u2109";
    final String celcius = "\u2103";


    public static final String TAG = SunshineWatchFace.class.getSimpleName();

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


        public final String LOG_TAG = Engine.class.getSimpleName();

        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        private Paint mWeatherBackgroundPaint;
        private Paint mTimeBackgroundPaint;

        private Paint mTimeTextPaint;
        private Paint mDateTextPaint;
        private Paint mCityTextPaint;
        private Paint mHighTempTextPaint;
        private Paint mLowTempTextPaint;
        private GoogleApiClient mGoogleApiClient;
        private boolean mAmbient;
        private Time mTime;
        private boolean mLowBitAmbient;

        private SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d");

        private Bitmap mBackgroundScaledBitmap;
        private int scaleWidth = 70;
        private int scaleHeight = 70;
        private Bitmap mSunshineImage;

        private String mCityName;
        private String mSunshineHighTemp;
        private String mSunshineLowTemp;

        @Override
        public void onCreate(SurfaceHolder holder) {

            super.onCreate(holder);

            initWatchFaceStyle();
            initWeatherBackground(mAmbient);
            initTimeBackground(mAmbient);
            initViewComponents();
            initGoogleApiClient();

        }


        private void initWeatherBackground(boolean isAmbient) {

            mWeatherBackgroundPaint = new Paint();

            if(isAmbient) {
                mWeatherBackgroundPaint.setColor(getColor(R.color.background_black));
            }else{
                mWeatherBackgroundPaint.setColor(getColor(R.color.background_white));
            }

        }

        private void initTimeBackground(boolean isAmbient) {

            mTimeBackgroundPaint = new Paint();

            if(isAmbient) {
                mTimeBackgroundPaint.setColor(getColor(R.color.background_black));
            }else{
                mTimeBackgroundPaint.setColor(getColor(R.color.background_blue));
            }

        }

        private void initViewComponents() {

            mTimeTextPaint = initTimeTextPaint();
            mDateTextPaint = initDateTextPaint();
            mCityTextPaint = initCityTextPaint();
            mHighTempTextPaint = initHighTempTextPaint();
            mLowTempTextPaint = initLowTempTextPaint();
            mTime = new Time();

        }

        private void initGoogleApiClient() {

            mGoogleApiClient = new GoogleApiClient.Builder(SunshineWatchFace.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();

            mGoogleApiClient.connect();


        }

        private void initWatchFaceStyle() {

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

        }

        @Override
        public void onDestroy() {

            if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }

            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }


        private Paint initTextPaint(int color,Typeface typeFace,float textSize,boolean antiAlias){

            Paint paint = new Paint();
            paint.setColor(getColor(color));
            paint.setTypeface(typeFace);
            paint.setTextSize(textSize);
            paint.setAntiAlias(antiAlias);
            return paint;

        }

        private Paint initTimeTextPaint() {
            return initTextPaint(R.color.time_color,NORMAL_TYPEFACE,70f,true);
        }

        private Paint initDateTextPaint() {
            return initTextPaint(R.color.date_color,NORMAL_TYPEFACE,20f,true);
        }

        private Paint initCityTextPaint() {
            return initTextPaint(R.color.city_color,NORMAL_TYPEFACE,20f,true);
        }

        private Paint initHighTempTextPaint() {
            return initTextPaint(R.color.high_temp_color,Typeface.DEFAULT_BOLD,30f,true);
        }

        private Paint initLowTempTextPaint() {
            return initTextPaint(R.color.low_temp_color,Typeface.DEFAULT_BOLD,30f,true);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunshineWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTimeTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            int centerX = bounds.width() / 2;
            int centerY = bounds.height() / 2;
            int boundHeight = bounds.height()/2+50;
            int bottomYForWeatherPanel = 100;

            initTimeBackground(mAmbient);
            initWeatherBackground(mAmbient);

            if(!mAmbient) {
                //time, date, city background
                canvas.drawRect(0, 0, bounds.width(), boundHeight, mTimeBackgroundPaint);
                //weather background
                canvas.drawRect(0, boundHeight, bounds.width(), bounds.height(), mWeatherBackgroundPaint);
            }else{
                //time, date, city background
                canvas.drawRect(0, 0, bounds.width(), boundHeight, mTimeBackgroundPaint);
                //weather background
                canvas.drawRect(0, boundHeight, bounds.width(), bounds.height(), mWeatherBackgroundPaint);
            }

            //time
            mTime.setToNow();
            String time = String.format("%d:%02d", mTime.hour, mTime.minute);
            canvas.drawText(time, textCenterJustify(centerX, mTimeTextPaint, time), 140, mTimeTextPaint);

            //date
            if(!mAmbient) {
                String date = sdf.format(new Date());
                canvas.drawText(date, textCenterJustify(centerX, mDateTextPaint, date), 160, mDateTextPaint);
            }

            //city
            if (!mAmbient && mCityName != null) {
                canvas.drawText(mCityName, textCenterJustify(centerX, mCityTextPaint, mCityName), 180, mCityTextPaint);
            }



            //between gap high temp, low temp and weather icon
            int gapBetween = 50;

            //high temp
            if (!mAmbient && mSunshineHighTemp != null) {
                float highTempCenterXPosition = textCenterJustify(centerX, mHighTempTextPaint, mSunshineHighTemp);
                canvas.drawText(mSunshineHighTemp, highTempCenterXPosition, centerY + bottomYForWeatherPanel, mHighTempTextPaint);
            }

            //low temp
            if (!mAmbient && mSunshineLowTemp != null) {
                canvas.drawText(mSunshineLowTemp, textRightJustifyFor(centerX, mHighTempTextPaint, mSunshineHighTemp, mSunshineLowTemp, gapBetween), centerY + bottomYForWeatherPanel , mLowTempTextPaint);
            }

            //weather image
            if (!mAmbient && mBackgroundScaledBitmap != null) {
                canvas.drawBitmap(mBackgroundScaledBitmap, imageLeftJustifyFor(centerX, mHighTempTextPaint, mSunshineHighTemp, mBackgroundScaledBitmap, gapBetween),centerY +  bottomYForWeatherPanel - scaleHeight + scaleHeight/2, null);
            }else if(mAmbient && mBackgroundScaledBitmap != null){
                Bitmap bwBitmap = Utility.createContrast(mBackgroundScaledBitmap,50);
                canvas.drawBitmap(bwBitmap, weatherImageCenterJustify(centerX), 201, null);
            }


        }


        private float textRightJustifyFor(float centerX, Paint textTo, String textToString, String textFromString, int gapBetween) {
            float rightX = centerX + textTo.measureText(textToString) / 2;
            return centerX - (centerX - (rightX - mDateTextPaint.measureText(textFromString))) + gapBetween;
        }

        private float imageLeftJustifyFor(float centerX, Paint textTo, String textToString, Bitmap imageFrom, int gapBetween) {
            float leftX = centerX - textTo.measureText(textToString) / 2;
            return leftX - imageFrom.getWidth(); //centerX-(centerX-(leftX-imageFrom.getWidth()))+gapBetween;
        }

        private float textCenterJustify(float centerX, Paint text, String textValue) {
            return centerX - text.measureText(textValue) / 2;
        }

        private float weatherImageCenterJustify(float centerX) {
            return centerX - scaleWidth/2;
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }


        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "onConnected");
            Wearable.DataApi.addListener(mGoogleApiClient, this);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG, "onConnectionSuspended");

        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {

            final String fahrenheit = "\u2109";
            final String celcius = "\u2103";
            String metric = celcius;

            Log.d(LOG_TAG, "*** ON DATA CHANGED");

            for (DataEvent event : dataEvents) {

                Log.d(LOG_TAG, "Event received: ");

                String eventUri = event.getDataItem().getUri().toString();

                if (eventUri.contains("/sunshine/event")) {

                    DataMapItem dataItem = DataMapItem.fromDataItem(event.getDataItem());

                    String cityName = dataItem.getDataMap().getString("city-name");

                    mCityName = cityName;

                    boolean isMetric = dataItem.getDataMap().getBoolean("is-metric");

                    if (!isMetric) {
                        metric = fahrenheit;
                    }

                    String highTemp = dataItem.getDataMap().getString("high-temp") + metric;
                    mSunshineHighTemp = highTemp;
                    String lowTemp = dataItem.getDataMap().getString("low-temp") + metric;
                    mSunshineLowTemp = lowTemp;


                    int weatherResourceId = dataItem.getDataMap().getInt("weather-resource-id");

                    int resourceId = Utility.getArtResourceForWeatherCondition(weatherResourceId);
                    mSunshineImage = BitmapFactory.decodeResource(getBaseContext().getResources(), resourceId);
                    mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mSunshineImage, scaleWidth, scaleHeight, true);

                    invalidate();

                    Log.d(LOG_TAG, cityName + " " + highTemp + " " + lowTemp + " is metric : " + isMetric);


                }
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "onConnectionFailed");
        }


    }


    private static class EngineHandler extends Handler {
        private final WeakReference<SunshineWatchFace.Engine> mWeakReference;

        public EngineHandler(SunshineWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SunshineWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }


}