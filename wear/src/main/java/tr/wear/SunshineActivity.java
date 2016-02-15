package tr.wear;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SunshineActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("E, MMM dd yyyy", Locale.US);

    private static final SimpleDateFormat AMBIENT_HOUR_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);


    private TextView mClockView;
    private TextView mDate;
    private ImageView mDaysWeatherImage;
    private TextView higherDegree;
    private TextView lowerDegree;

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
        higherDegree = (TextView ) findViewById(R.id.higher_degree);
        lowerDegree = (TextView ) findViewById(R.id.lower_degree);

        updateDisplay();

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
}
