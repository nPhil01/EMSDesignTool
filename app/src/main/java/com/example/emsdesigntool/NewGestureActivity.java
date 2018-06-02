package com.example.emsdesigntool;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

public class NewGestureActivity extends AppCompatActivity {

    private LinearLayout ll;
    private LinearLayout llInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_gesture);
        ll = (LinearLayout) findViewById(R.id.ll_timeline);
        llInput = (LinearLayout) findViewById(R.id.inputTimelineName);
    }

    // Prepare the new Timeline
    public void prepareTimeline(View view){
        // Create the input field
        EditText editText = new EditText(this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 3f));
        editText.setId(R.id.edit_text_timeline_info);

        // Create the "create"-button
        Button button = new Button(this);
        button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        button.setText("Create");
        button.setId(R.id.button_timeline_info);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTimeline();
            }
        });

        // Add them to the view
        llInput.addView(editText);
        llInput.addView(button);
    }

    // Add a Timeline
    private void addTimeline(){
        // Get the input
        EditText mEdit = (EditText)findViewById(R.id.edit_text_timeline_info);
        Button inputButton = (Button)findViewById(R.id.button_timeline_info);
        String muscle = mEdit.getText().toString();

        // Remove the input and the button
        ((LinearLayout)mEdit.getParent()).removeView(mEdit);
        ((LinearLayout)inputButton.getParent()).removeView(inputButton);

        // Get index of add-button
        Button button = (Button)findViewById(R.id.button4);
        int indexButton = ((LinearLayout)button.getParent()).indexOfChild(button);

        // Load the timeline-layout
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout childLayout = (LinearLayout) inflater.inflate(R.layout.timeline, null, false);

        // Add a text/info to the layout
        TextView textView = new TextView(this);
        textView.setText(muscle);
        childLayout.addView(textView);

        // Add the timeline-layout above the button
        ll.addView(childLayout, indexButton);
        ll.invalidate();

    }
}
