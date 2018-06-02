package com.example.emsdesigntool;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Start new Gesture Activity
    public void newGestureView (View view){
        Intent intent = new Intent(this, NewGestureActivity.class);
        startActivity(intent);
    }

    // Start new Gesture Activity
    public void loadGestureView (View view){
        Intent intent = new Intent(this, LoadGestureActivity.class);
        startActivity(intent);
    }

    // Start new Gesture Activity
    public void settingsView (View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
