package com.example.android.sunshine.app.wearable;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by baybora on 2/16/16.
 */
public class WearableUtility {

    public static final String LOG_TAG = WearableUtility.class.getSimpleName();

    public static GoogleApiClient initWear(GoogleApiClient mGoogleApiClient,
                          Context context,
                          GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                          GoogleApiClient.OnConnectionFailedListener failedListener) {

        if (null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(connectionCallbacks)
                    .addOnConnectionFailedListener(failedListener)
                    .build();
            Log.v(LOG_TAG, "GoogleApiClient created");
        }

        if (!mGoogleApiClient.isConnected()) {

            int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            if (ConnectionResult.SUCCESS == result) {
                mGoogleApiClient.connect();
            } else {
                Toast.makeText(context, "Not connected to wear...", Toast.LENGTH_SHORT).show();
            }
            Log.v(LOG_TAG, "Connecting to GoogleApiClient..");
        }

        return mGoogleApiClient;

    }


}
