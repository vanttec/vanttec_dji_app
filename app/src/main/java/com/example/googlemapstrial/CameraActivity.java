package com.example.googlemapstrial;

import android.os.Bundle;


import androidx.fragment.app.FragmentActivity;

public class CameraActivity extends FragmentActivity {
    private static final String TAG = CameraActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }
}
