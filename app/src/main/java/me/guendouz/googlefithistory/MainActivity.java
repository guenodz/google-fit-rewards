package me.guendouz.googlefithistory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static java.text.DateFormat.getDateInstance;

public class MainActivity extends AppCompatActivity {

    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    public static final String TAG = MainActivity.class.getSimpleName();

    private AppCompatTextView tvText, tvTimePeriod, tvRewardsOfThisWeeek;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RewardsAdapter rewardsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        tvText = findViewById(R.id.tvStepsCount);
        tvTimePeriod = findViewById(R.id.tvTimePeriod);
        tvRewardsOfThisWeeek = findViewById(R.id.tvRewardsTitle);
        recyclerView = findViewById(R.id.rvRewardList);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        setElementVisibility(false);

        // Create the FitnessOptions object
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        // Check if the user has already gave permission to access his data, otherwise, we must request permissions
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            // if we have user permission we can do our job!
            accessGoogleFit();
        }
    }

    private void setElementVisibility(boolean visible) {
        tvRewardsOfThisWeeek.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        recyclerView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    // Handle user's response to our permission request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit();
            }
        }
    }

    // Create a new data request and call the History API to get data
    private void accessGoogleFit() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(7, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();


        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(Task<DataReadResponse> task) {
                        progressBar.setIndeterminate(false);
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful())
                            setElementVisibility(true);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d(TAG, "onSuccess()");
                        showStepsCount(dataReadResponse);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure()");
            }
        });
    }

    private void showStepsCount(DataReadResponse dataReadResponse) {
        int stepsCountPerWeek = 0;
        DateFormat dateFormat = getDateInstance();
        if (dataReadResponse.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResponse.getBuckets().size());
            Bucket bucket = dataReadResponse.getBuckets().get(0);
            // Check if the returned bucket by the API is not empty
            if (bucket.getDataSets().size() > 0) {
                DataSet dataSet = bucket.getDataSets().get(0);
                // Check if dataset is not empty
                if (!dataSet.isEmpty()) {
                    // Since we are requesting only one data at a time, our dataset has one datapoint
                    // that contains the steps count during the week
                    DataPoint dataPoint = dataSet.getDataPoints().get(0);
                    stepsCountPerWeek = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                    Log.i(TAG, "Number of steps in the last week : " + stepsCountPerWeek);
                    tvText.setText(String.valueOf(stepsCountPerWeek));
                    tvTimePeriod.setText(String.format("%s - %s", dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)),
                            dateFormat.format(dataPoint.getEndTime(TimeUnit.MILLISECONDS))));
                    // create the adapter and attach it to the recycler view
                    rewardsAdapter = new RewardsAdapter(stepsCountPerWeek, Reward.generateSampleRewards());
                    recyclerView.setAdapter(rewardsAdapter);
                }
            }
        }

    }
}
