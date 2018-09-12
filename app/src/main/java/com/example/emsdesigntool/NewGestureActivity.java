package com.example.emsdesigntool;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.example.emsdesigntool.commands.EMSBluetoothLEService;
import com.example.emsdesigntool.searchandshow.SearchAndShowEMSDevicesView;
import com.example.emsdesigntool.searchandshow.SearchAndShowEMSChannels;
import com.edmodo.rangebar.RangeBar;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewGestureActivity extends AppCompatActivity {

    private LinearLayout ll;
    private LinearLayout llInput;
    private final Context context = this;
    private android.app.AlertDialog.Builder alert;
    private SearchAndShowEMSDevicesView searchAndShowEMSDevicesView;
    private SearchAndShowEMSChannels searchAndShowEMSChannels;
    private EMSBluetoothLEService bleConnector;
    private static final ArrayList<EMSModule> emsModules = new ArrayList<>();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private ArrayList<String> connectedChannels = new ArrayList<>();
    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private int[][][] loadIntArray;
    private String[] loadStringArray;
    private String projectNameLoaded;
    private static final String EXTRA_MESSAGE = "com.example.emsdesigntool.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_gesture);
        ll = findViewById(R.id.ll_timeline);
        llInput = findViewById(R.id.inputTimelineName);

        Bundle bundle = getIntent().getExtras();
        Intent intent = getIntent();

        projectNameLoaded = null;

        // Check if a project is loaded and create it
        if(bundle != null){
            loadIntArray = (int[][][])bundle.getSerializable("int_array");
            loadStringArray = intent.getStringArrayExtra("string_array");
            projectNameLoaded = intent.getStringExtra("projectName");
            projectNameLoaded = projectNameLoaded.substring(0, projectNameLoaded.length() - 5);

            createLoadedProject();
        } else {
            getSavedFile();
        }

        //Android M Permission check
        if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect EMS-modules");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
        else {
            gotPermission();
        }

        Button scrollLeftButton1 = findViewById(R.id.buttonScrollLeft1);
        Button scrollLeftButton2 = findViewById(R.id.buttonScrollLeft2);
        Button scrollRightButton1 = findViewById(R.id.buttonScrollRight1);
        Button scrollRightButton2 = findViewById(R.id.buttonScrollRight2);

        scrollLeftButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollLeft();
                return false;
            }
        });

        scrollLeftButton2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollLeft();
                return false;
            }
        });

        scrollRightButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollRight();
                return false;
            }
        });

        scrollRightButton2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollRight();
                return false;
            }
        });

        /*
        // Check always if modules disconnected
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int timelinesCount = ll.getChildCount() - 5;
                    if (timelinesCount >= 1) {
                        for (int i = 1; i <= timelinesCount; i++) {
                            if(ll.getChildAt(i) instanceof LinearLayout){
                                LinearLayout llTimelineParent = (LinearLayout) ll.getChildAt(i);
                                LinearLayout llTimeline = (LinearLayout) llTimelineParent.getChildAt(0);
                                LockableScrollView LSV = (LockableScrollView) llTimeline.getChildAt(1);
                                LinearLayout llLSVChild = (LinearLayout) LSV.getChildAt(0);
                                LinearLayout llButton = (LinearLayout) llLSVChild.getChildAt(1);
                                LinearLayout llTimelineButtons = (LinearLayout) llLSVChild.getChildAt(0);

                                if (!(llButton.getChildAt(0) instanceof Button)) {
                                    String[] deviceChannel = ((String) ((TextView)llButton.getChildAt(0)).getText()).split(",");
                                    for(EMSModule emsModule : emsModules){
                                        if(emsModule.getDeviceName().equals(deviceChannel[0]) && !emsModule.isConnected()){
                                            ImageButton plusButton = (ImageButton) llTimelineButtons.getChildAt(llTimelineButtons.getChildCount() - 1);
                                            if(plusButton.getTag() == "connected"){
                                                timelineConnectionLost(llTimeline);
                                            }
                                            emsModule.connect();
                                        } else if(emsModule.getDeviceName().equals(deviceChannel[0]) && emsModule.isConnected()){
                                            ImageButton plusButton = (ImageButton) llTimelineButtons.getChildAt(llTimelineButtons.getChildCount() - 1);
                                            if(plusButton.getTag() != "connected"){
                                                timelineConnected(llTimeline);
                                            }
                                        }
                                    }
                                } else {
                                    timelineDisonnected(llTimeline);
                                }
                            }
                        }
                    }
                }
            }
        });
        */
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle("Leaving Project")
                .setMessage("Are you sure you want to leave the project?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        navigateBack();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void navigateBack(){
        NavUtils.navigateUpFromSameTask(this);
    }

    // Get the project-file
    private void getSavedFile(){
        String path = context.getFilesDir().toString();
        File directory = new File(path);
        File[] files = directory.listFiles();

        for(File f: files){
            if(f.getName().equals("lastSaved.json")){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("Old project found!");

                // set dialog message
                alertDialogBuilder.setMessage("Do you want to load your old project?");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadOldProject();
                        dialog.cancel();
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        }
    }

    private void loadOldProject(){
        Intent intent = new Intent(this, GestureActivity.class);
        String projectName = "lastSaved.json";
        intent.putExtra(EXTRA_MESSAGE, projectName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_gesture, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.connectEMSDevices){
            alert = getNewAlertDialog();
            alert.show();
            return true;
        }
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Load the chosen project
    private void createLoadedProject(){
        for(int i = 0; i < loadIntArray.length; i++){
            Button buttonNewTimeline = findViewById(R.id.button4);
            final int indexButton = ((LinearLayout)buttonNewTimeline.getParent()).indexOfChild(buttonNewTimeline);
            addTimeline(loadStringArray[i]);
            // Get index of add-button
            for(int j = 0; j < loadIntArray[i].length; j++){
                if(loadIntArray[i][j][0] != 0){
                    createNewElement(indexButton);
                }
            }
            for(int j = loadIntArray[i].length - 1; j >=0; j--){
                int widthSum = 0;
                for(int k = 0; k < j; k++){
                    widthSum += loadIntArray[i][k][0];
                }
                if(loadIntArray[i][j][0] != 0){
                    setValuesLoaded(loadIntArray[i][j][0], loadIntArray[i][j][1], loadIntArray[i][j][2], j, widthSum);
                }
            }
        }
    }

    // Set the values of the loaded buttons
    private void setValuesLoaded(int width, int height, int xPos, int buttonIndex, int xDif){
        int timelinesCount = ll.getChildCount() - 5;
        LinearLayout llTimelineParent = (LinearLayout) ll.getChildAt(timelinesCount);
        LinearLayout llTimeline = (LinearLayout) llTimelineParent.getChildAt(0);
        LockableScrollView HSV = (LockableScrollView)llTimeline.getChildAt(1);
        LinearLayout HSVChild = (LinearLayout)HSV.getChildAt(0);
        LinearLayout llTimelineButtons = (LinearLayout)HSVChild.getChildAt(1);
        llTimelineButtons.invalidate();
        Button button  = (Button) llTimelineButtons.getChildAt(buttonIndex);

        if(llTimelineButtons.getChildCount() - 2 == buttonIndex){
            ImageButton imageButton = (ImageButton)llTimelineButtons.getChildAt(llTimelineButtons.getChildCount() - 1);
            int buttonWidth = button.getWidth();
            imageButton.setX(xPos - xDif + buttonWidth);
        }

        // Set new width and height of the button and return to the create-screen
        LinearLayout.LayoutParams elementParams = new LinearLayout.LayoutParams(width, height);
        // elementParams.gravity = Gravity.BOTTOM;
        button.setLayoutParams(elementParams);
        button.setX(xPos - xDif);


    }

    // Prepare the new Timeline
    public void prepareTimeline(View view){
        // Set max Timeslines = 6
        if(ll.getChildCount() - 5 >= 6){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set title
            alertDialogBuilder.setTitle("No more timelines possible");

            // set dialog message
            alertDialogBuilder.setMessage("You reached the limit of timelines, which is 6");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            return;
        }

        // Disable createButton
        final Button buttonNewTimeline = findViewById(R.id.button4);
        buttonNewTimeline.setEnabled(false);

        final TextView textView = new TextView(this);
        textView.setText("Timeline description:");

        // Create the input field
        final EditText editText = new EditText(this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 3f));
        editText.setId(R.id.edit_text_timeline_info);

        // Create the "create"-button
        Button button = new Button(this);
        button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        button.setBackgroundResource(android.R.drawable.btn_default);
        button.setText("Create");
        button.setId(R.id.button_timeline_info);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String muscle = editText.getText().toString();
                buttonNewTimeline.setEnabled(true);
                // Remove the input and the button
                ((LinearLayout)editText.getParent()).removeView(editText);
                ((LinearLayout)v.getParent()).removeView(v);
                ((LinearLayout)textView.getParent()).removeView(textView);
                addTimeline(muscle);
            }
        });

        // Add them to the view
        llInput.addView(textView);
        llInput.addView(editText);
        llInput.addView(button);
    }

    // Add a Timeline
    private void addTimeline(String muscleInput){
        Button buttonNewTimeline = findViewById(R.id.button4);
        // Get index of add-button
        final int indexButton = ((LinearLayout)buttonNewTimeline.getParent()).indexOfChild(buttonNewTimeline);

        // Load the timeline-layout
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout childLayout = (LinearLayout) inflater.inflate(R.layout.timeline, null, false);
        childLayout.setId(indexButton);

        // Get the childs of the view, call the "createNewElement" Method with the right ID
        final LinearLayout child1 = (LinearLayout) childLayout.getChildAt(0);
        LockableScrollView child2 = (LockableScrollView) child1.getChildAt(1);
        child2.setScrollingEnabled(false);
        LinearLayout child3 = (LinearLayout) child2.getChildAt(0);
        LinearLayout child4 = (LinearLayout) child3.getChildAt(1);
        final ImageButton plusButton = (ImageButton) child4.getChildAt(0);
        plusButton.setTag("disconnected");
        plusButton.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHHOLD = 200;
            private long lastTouchDown;
            int lastAction = 0;
            float dX = 0;
            @Override
            public boolean onTouch( View v, MotionEvent e){
                int index = ((LinearLayout)plusButton.getParent()).indexOfChild(plusButton);
                switch (e.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - e.getRawX();
                        lastAction = MotionEvent.ACTION_DOWN;
                        lastTouchDown = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        lastAction = MotionEvent.ACTION_MOVE;
                        moveButton(e, v, dX, index);
                        break;

                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHHOLD) {
                            createNewElement(indexButton);
                        }
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });

        // Set function for play-button
        final ImageButton playButton = (ImageButton)child1.getChildAt(0);
        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                playTimeline(playButton);
            }
        });

        // Set function for Calibrate-Button
        final ImageButton calibrateButton = (ImageButton) child1.getChildAt(2);
        calibrateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                calibrate(calibrateButton);
            }
        });

        // Add a text/info to the layout
        TextView textView = new TextView(this);
        textView.setText(muscleInput);
        childLayout.addView(textView);

        // Add the timeline-layout above the button
        ll.addView(childLayout, indexButton);
        ll.invalidate();

        // Prepare the timescale
        createTimescale(child3);
    }

    private void createTimescale(@NonNull LinearLayout LSVChild){
        LinearLayout timer = (LinearLayout) LSVChild.getChildAt(0);
        Button sec1 = new Button(this);
        int start = 1;
        sec1.setText(start + "");
        sec1.setBackgroundResource(R.drawable.button_invisible);
        sec1.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.MATCH_PARENT));
        timer.addView(sec1);
        for(int i = 2; i <= 10; i++){
            addTimeButton(i, timer);
        }
    }

    private void addTimeButton(int i, @NonNull LinearLayout timer){
        Button space = new Button(this);
        space.setBackgroundResource(R.drawable.button_invisible);
        space.setText(i - 1 + ",5");
        space.setLayoutParams(new LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.MATCH_PARENT));
        space.setEnabled(false);

        Button sec = new Button(this);
        sec.setText(i + "");
        sec.setBackgroundResource(R.drawable.button_invisible);
        sec.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.MATCH_PARENT));
        sec.setEnabled(false);

        timer.addView(space);
        timer.addView(sec);
    }

    // Create a new Element
    public void createNewElement(int timelineID){
        // Get the childs, need to do this to add the button at the right place
        final LinearLayout parentLL = findViewById(timelineID);
        LinearLayout childLL = (LinearLayout)parentLL.getChildAt(0);
        LockableScrollView childHSV = (LockableScrollView)childLL.getChildAt(1);
        LinearLayout HSVChild = (LinearLayout)childHSV.getChildAt(0);
        final LinearLayout timeline = (LinearLayout)HSVChild.getChildAt(1);
        LinearLayout llButton = (LinearLayout) HSVChild.getChildAt(2);
        int plusButtonIndex = timeline.getChildCount() - 1;

        ImageButton plusButton = (ImageButton) timeline.getChildAt(plusButtonIndex);
        float newX = plusButton.getX();
        int difX = 0;
        if(plusButtonIndex > 0){
            for(int i = 0; i < plusButtonIndex; i++){
                difX += timeline.getChildAt(i).getWidth();
            }
        }

        // Create the new Element in front of the plus button
        final Button newElement = new Button(this);

        LinearLayout.LayoutParams newElementParams;
        newElementParams = new LinearLayout.LayoutParams(500, 200);
        newElementParams.gravity = Gravity.BOTTOM;
        newElement.setLayoutParams(newElementParams);
        if(!(llButton.getChildAt(0) instanceof Button)){
            Log.w("LLBUTTON", llButton.getChildAt(0).toString());
            newElement.setBackgroundResource(R.drawable.button_connected);
        } else {
            newElement.setBackgroundResource(android.R.drawable.btn_default);
        }
        // Make the button draggable
        newElement.setOnTouchListener(new View.OnTouchListener(){
            float dX = 0;
            int lastAction = 0;
            private long lastTouchDown;
            private int CLICK_ACTION_THRESHHOLD = 200;

            @Override
            public boolean onTouch( View v, MotionEvent e){
                int index = ((LinearLayout)v.getParent()).indexOfChild(v);
                switch (e.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - e.getRawX();
                        lastAction = MotionEvent.ACTION_DOWN;
                        lastTouchDown = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        lastAction = MotionEvent.ACTION_MOVE;
                        moveButton(e, v, dX, index);
                        break;

                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHHOLD) {
                            setValues(newElement);
                        }
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });
        newElement.setX(newX - difX);
        timeline.addView(newElement, plusButtonIndex);
    }

    private boolean moveButton(MotionEvent e, View v, float dX, int index){
        float beforeChange = v.getX();
        v.setX(e.getRawX() + dX);
        float afterChange = v.getX();
        LinearLayout llParent = (LinearLayout)v.getParent();
        boolean borderNotReached;
        if(v instanceof ImageButton){
            if(index > 0){
                if(beforeChange > afterChange){
                    float xDif = afterChange - beforeChange;
                    boolean[] nextReached = new boolean[llParent.getChildCount()];
                    for(int i = 0; i < nextReached.length; i++){
                        nextReached[i] = false;
                    }
                    for(int i = index; i >= 1; i--){
                        if(llParent.getChildAt(i) instanceof ImageButton && !nextReached[i]){
                            ImageButton element1 = (ImageButton) llParent.getChildAt(i);
                            Button element0 = (Button)llParent.getChildAt(i - 1);
                            if(element1.getX() <= element0.getX() + element0.getWidth() + 1){
                                nextReached[i] = true;
                            }
                        } else {
                            checkNextReached(llParent, nextReached, i);
                        }

                        if (nextReachedButton(index, llParent, xDif, nextReached, i)) return false;
                    }
                }
            } else {
                if(afterChange <= 0.0){
                    v.setX(1);
                }
            }
        } else {
            if(beforeChange > afterChange){
                if(index > 0){
                    float xDif = afterChange - beforeChange;
                    boolean[] nextReached = new boolean[index + 1];
                    for(int i = 0; i < nextReached.length; i++){
                        nextReached[i] = false;
                    }
                    for(int i = index; i >= 1; i--){
                        if(i == index && !nextReached[i]) {
                            Button element1 = (Button) llParent.getChildAt(i);
                            Button element0 = (Button) llParent.getChildAt(i - 1);
                            if (element1.getX() <= element0.getX() + element0.getWidth() + 1) {
                                nextReached[i] = true;
                            }
                        } else checkNextReached(llParent, nextReached, i);
                        if (nextReachedButton(index, llParent, xDif, nextReached, i)) return false;
                    }
                } else {
                    if(afterChange <= 0.0){
                        v.setX(1);
                    }
                }
            } else if(beforeChange < afterChange){
                if(index < llParent.getChildCount() - 1){
                    float xDif = afterChange - beforeChange;
                    int booleanLength = llParent.getChildCount() - index;
                    boolean[] nextReached = new boolean[booleanLength];
                    for(int i = 0; i < nextReached.length; i++){
                        nextReached[i] = false;
                    }
                    for(int i = index; i < llParent.getChildCount() - 1; i++){
                        if(i == index && !nextReached[i - index]){
                            checkNextReachedMovingRight(index, llParent, nextReached, i);
                        } else if(!nextReached[i - index] && nextReached[i - index - 1]){
                            checkNextReachedMovingRight(index, llParent, nextReached, i);
                        }
                        if(nextReached[i - index]){
                            borderNotReached = moveOtherButtons(i + 1, xDif, llParent);
                            if(!borderNotReached){
                                Log.w("RETURN FALSE", "RETURN FALSE");
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean nextReachedButton(int index, LinearLayout llParent, float xDif, boolean[] nextReached, int i) {
        boolean borderNotReached;
        if(nextReached[i]){
            borderNotReached = moveOtherButtons(i - 1, xDif, llParent);
            if(!borderNotReached){
                for(int j = index; j >= 0; j--){
                    float newX = 0;
                    if(j == 0){
                        newX = 0;
                    } else {
                        for(int k = 0; k < j; k++){
                            Button button = (Button)llParent.getChildAt(k);
                            newX += button.getWidth();
                        }
                    }
                    if(llParent.getChildAt(j) instanceof ImageButton){
                        ImageButton imageButton = (ImageButton)llParent.getChildAt(j);
                        imageButton.setX(newX);
                    } else {
                        Button button = (Button)llParent.getChildAt(j);
                        button.setX(newX);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void checkNextReachedMovingRight(int index, LinearLayout llParent, boolean[] nextReached, int i) {
        if(llParent.getChildAt(i + 1) instanceof ImageButton){
            Button element0 = (Button) llParent.getChildAt(i);
            ImageButton element1 = (ImageButton) llParent.getChildAt(i + 1);
            if(element0.getX() + element0.getWidth() - 1 >= element1.getX()){
                nextReached[i - index] = true;
            }
        } else {
            Button element0 = (Button) llParent.getChildAt(i);
            Button element1 = (Button) llParent.getChildAt(i + 1);
            if(element0.getX() + element0.getWidth() - 1 >= element1.getX()){
                nextReached[i - index] = true;
            }
        }
    }

    private void checkNextReached(LinearLayout llParent, boolean[] nextReached, int i) {
        if(!nextReached[i] && nextReached[i + 1]){
            Button element1 = (Button) llParent.getChildAt(i);
            Button element0 = (Button) llParent.getChildAt(i - 1);
            if(element1.getX() <= element0.getX() + element0.getWidth() + 1){
                nextReached[i] = true;
            }
        }
    }

    private boolean moveOtherButtons(int index, float xDif, LinearLayout llParent){
        if(llParent.getChildAt(index) instanceof ImageButton){
            ImageButton element = (ImageButton)llParent.getChildAt(index);
            float newX = element.getX() + xDif;
            element.setX(newX);
            if(index == 0 && newX <= 0.0){
                element.setX(1);
                return false;
            }
        } else {
            Button element = (Button)llParent.getChildAt(index);
            float newX = element.getX() + xDif;
            element.setX(newX);
            if(index == 0 && newX <= 0.0){
                return false;
            }
        }
        return true;
    }


    // Open/Create the set-values-window
    public void setValues(final Button element){
        final FrameLayout frameLayout = findViewById(R.id.frameNewGesture);

        // Create the linear layout
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setClickable(true);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));


        // Get values for the button
        int buttonH = element.getHeight();
        int buttonw = element.getWidth();

        int duration = buttonw * 2;

        // Create the input views
        LinearLayout llWidth = new LinearLayout(this);
        TextView tvWidth = new TextView(this);
        final EditText etWidth = new EditText(this);

        LinearLayout llHeight = new LinearLayout(this);
        TextView tvHeight = new TextView(this);
        final SeekBar sbHeight = new SeekBar(this);

        llWidth.setOrientation(LinearLayout.HORIZONTAL);
        llHeight.setOrientation(LinearLayout.HORIZONTAL);

        tvWidth.setText("Duration in ms");
        tvHeight.setText("Intensity in %");

        etWidth.setInputType(InputType.TYPE_CLASS_NUMBER);
        etWidth.setText("" + duration);

        sbHeight.setMax(100);
        sbHeight.setMin(0);
        sbHeight.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        int buttonHOld = buttonH - 100;
        sbHeight.setProgress(buttonHOld);

        // Create the submit button
        Button submit = new Button(this);
        submit.setBackgroundResource(R.drawable.button_connected);
        submit.setText("Save");
        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String newDurationSek = etWidth.getText().toString();
                int newDurationInt = Integer.parseInt(newDurationSek);
                // Check if duration is not > 5000
                if(newDurationInt > 5000){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                    // set title
                    alertDialogBuilder.setTitle("Duration too long!");

                    // set dialog message
                    alertDialogBuilder.setMessage("The maximum duration is 5000!");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                } else {
                    // Create new width of the button
                    int newDuration = newDurationInt / 2;

                    // Create new height of the button
                    int buttonHNew = sbHeight.getProgress();
                    int buttonHNewH = buttonHNew + 100;

                    // Set new width and height of the button and return to the create-screen
                    LinearLayout.LayoutParams elementParams = new LinearLayout.LayoutParams(newDuration, buttonHNewH);
                    // elementParams.gravity = Gravity.BOTTOM;
                    element.setLayoutParams(elementParams);
                    frameLayout.removeView(linearLayout);
                }
            }
        });

        // Create cancel button
        Button cancelButton = new Button(this);
        cancelButton.setBackgroundResource(android.R.drawable.btn_default);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                frameLayout.removeView(linearLayout);
            }
        });

        // Create the delete button
        Button delete = new Button(this);
        delete.setBackgroundResource(R.drawable.button_red);
        delete.setText("Delete this element");
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("Delete element?");

                // set dialog message
                alertDialogBuilder.setMessage("Are you sure you want to delete this element?");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        frameLayout.removeView(linearLayout);
                        ((LinearLayout)element.getParent()).removeView(element);
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        // Add the views to the FrameLayout
        llWidth.addView(tvWidth);
        llWidth.addView(etWidth);
        llHeight.addView(tvHeight);
        llHeight.addView(sbHeight);
        linearLayout.addView(llWidth);
        linearLayout.addView(llHeight);
        linearLayout.addView(submit);
        linearLayout.addView(delete);
        linearLayout.addView(cancelButton);
        frameLayout.addView(linearLayout);
    }

    // Calibrate the EMS / Option to connect to EMS-Modul
    private void calibrate(View v){
        final FrameLayout frameLayout = findViewById(R.id.frameNewGesture);

        LinearLayout llFrame = new LinearLayout(this);
        llFrame.setOrientation(LinearLayout.HORIZONTAL);
        llFrame.setBackgroundColor(Color.WHITE);
        llFrame.setClickable(true);
        llFrame.setLayoutParams(new LinearLayout.LayoutParams(frameLayout.getWidth(), frameLayout.getHeight()));

        // Create the ScrollView
        final ScrollView HSV = new ScrollView(this);
        HSV.setFillViewport(true);

        LinearLayout llPicture = new LinearLayout(this);
        llPicture.setOrientation(LinearLayout.VERTICAL);

        LinearLayout vParent = (LinearLayout)v.getParent();
        LinearLayout timelineViewV = (LinearLayout) vParent.getParent();
        final TextView muscleName = (TextView)timelineViewV.getChildAt(timelineViewV.getChildCount() - 1);
        String muscleNameStringV = (String) muscleName.getText();
        TextView muscleNameV = new TextView(this);
        muscleNameV.setText(muscleNameStringV);
        llPicture.addView(muscleNameV);

        // Create the linear layout
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        // Create button to connect to ems module
        Button emsDeviceButton = new Button(this);
        emsDeviceButton.setBackgroundResource(android.R.drawable.btn_default);
        emsDeviceButton.setText("Add EMS-Modules");
        emsDeviceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                frameLayout.removeView(HSV);
                alert = getNewAlertDialog();
                alert.show();
            }
        });

        // Create button to connect to ems moduleChannel
        Button connectButton = new Button(this);
        connectButton.setBackgroundResource(android.R.drawable.btn_default);
        connectButton.setText("Connect to EMS-Channel");
        final ImageButton IB = (ImageButton)v;
        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                alert = getNewAlertDialogChannel(IB);
                alert.show();
                frameLayout.removeView(HSV);
            }
        });
        Button okButton = new Button(this);
        okButton.setBackgroundResource(R.drawable.button_connected);
        okButton.setText("OK");
        okButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                frameLayout.removeView(HSV);
            }
        });

        // Create disconnect button
        Button disconnectButton = new Button(this);
        disconnectButton.setBackgroundResource(android.R.drawable.btn_default);
        disconnectButton.setText("Disconnect");
        disconnectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //  Get the device name from textview
                LinearLayout llParent = (LinearLayout)IB.getParent();
                LockableScrollView LSV = (LockableScrollView) llParent.getChildAt(1);
                LinearLayout llLSVChild = (LinearLayout) LSV.getChildAt(0);
                LinearLayout llButton = (LinearLayout) llLSVChild.getChildAt(2);
                // If textview is set
                String device = (String) ((TextView)llButton.getChildAt(0)).getText();
                String channelToRemove = "";
                for(String connectedChannel : connectedChannels){
                    if(connectedChannel.equals(device)){
                        TextView deviceView = (TextView)llButton.getChildAt(0);
                        llButton.removeView(deviceView);
                        channelToRemove = connectedChannel;
                        frameLayout.removeView(HSV);
                        calibrate(IB);
                        timelineDisonnected(llParent);
                    }
                }
                connectedChannels.remove(channelToRemove);
            }
        });

        // Create the button to delete the whole timeline
        Button deleteTimeline = new Button(this);
        deleteTimeline.setText("Delete the timeline");
        deleteTimeline.setBackgroundResource(R.drawable.button_red);
        deleteTimeline.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("Delete timeline?");

                // set dialog message
                alertDialogBuilder.setMessage("Are you sure you want to delete the whole timeline?");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        frameLayout.removeView(HSV);
                        LinearLayout IBParent = (LinearLayout)IB.getParent();
                        LinearLayout timelineView = (LinearLayout) IBParent.getParent();
                        ll.removeView(timelineView);
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        // Rename the timeline
        Button renameButton = new Button(this);
        renameButton.setBackgroundResource(android.R.drawable.btn_default);
        renameButton.setText("Rename the timeline");
        renameButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                LinearLayout IBParent = (LinearLayout)IB.getParent();
                LinearLayout timelineView = (LinearLayout) IBParent.getParent();
                final TextView muscleName = (TextView)timelineView.getChildAt(timelineView.getChildCount() - 1);
                String muscleNameString = (String) muscleName.getText();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("Rename the timeline");
                final RenameTimelineTextview renameTimelineTextview = new RenameTimelineTextview(alertDialogBuilder.getContext(), null, muscleNameString);
                alertDialogBuilder.setView(renameTimelineTextview);
                // set dialog message
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String changedName = renameTimelineTextview.getInput();
                        Log.w("CHANGED NAME", changedName);
                        muscleName.setText(changedName);
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });

        final LinearLayout llParent = (LinearLayout)IB.getParent();
        LockableScrollView LSV = (LockableScrollView) llParent.getChildAt(1);
        LinearLayout llLSVChild = (LinearLayout) LSV.getChildAt(0);
        LinearLayout llButton = (LinearLayout) llLSVChild.getChildAt(2);
        final int childCount = llParent.getChildCount();



        // Create the TextView to label the rangeBar
        TextView rangeBarText = new TextView(this);
        rangeBarText.setText("Set the minimum and maximum Power");

        // Get the Values of min and max power
        String[] minMax= ((String)((TextView) llParent.getChildAt(childCount - 1)).getText()).split(",");
        int minPower = Integer.parseInt(minMax[0]);
        int maxPower = Integer.parseInt(minMax[1]);
        Log.w("minPower", minMax[0]);
        Log.w("maxPower", minMax[1]);

        // Create the RangeBar to calibrate the EMS-Module
        RangeBar rangeBar = new RangeBar(this);
        rangeBar.setTickCount(100);
        rangeBar.setThumbIndices(minPower, maxPower);
        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int i, int i1) {
                TextView minMaxText = (TextView) llParent.getChildAt(childCount - 1);
                minMaxText.setText(i + "," + i1);
            }
        });

        LinearLayout llButtons = new LinearLayout(this);
        llButtons.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button testButton = new Button(this);
        Button buttonMin = new Button(this);
        Button buttonMax = new Button(this);
        testButton.setBackgroundResource(android.R.drawable.btn_default);
        buttonMin.setBackgroundResource(android.R.drawable.btn_default);
        buttonMax.setBackgroundResource(android.R.drawable.btn_default);
        buttonMin.setText("Calibrate minimum");
        buttonMax.setText("Calibrate maximum");
        testButton.setText("Test the channel");
        buttonMin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                String [] minMax;
                int min;
                minMax= ((String)((TextView) llParent.getChildAt(childCount - 1)).getText()).split(",");
                min = Integer.parseInt(minMax[0]);
                calibrateMinMax(llParent, min);
                switch (e.getActionMasked()){
                    case MotionEvent.ACTION_UP:
                        minMax= ((String)((TextView) llParent.getChildAt(childCount - 1)).getText()).split(",");
                        min = Integer.parseInt(minMax[0]);
                        calibrateMinMax(llParent, min);
                        break;

                    default:
                        return false;
                }
                return false;
            }
        });

        buttonMax.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                String [] minMax;
                int max;
                minMax= ((String)((TextView) llParent.getChildAt(childCount - 1)).getText()).split(",");
                max = Integer.parseInt(minMax[1]);
                calibrateMinMax(llParent, max);
                switch (e.getActionMasked()){
                    case MotionEvent.ACTION_UP:
                        minMax= ((String)((TextView) llParent.getChildAt(childCount - 1)).getText()).split(",");
                        max = Integer.parseInt(minMax[1]);
                        calibrateMinMax(llParent, max);
                        break;

                    default:
                        return false;
                }
                return false;
            }
        });

        testButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int max = 99;
                calibrateMinMax(llParent, max);
                return false;
            }
        });

        if(llButton.getChildAt(0) instanceof Button){
            rangeBar.setEnabled(false);
            testButton.setEnabled(false);
            buttonMax.setEnabled(false);
            buttonMin.setEnabled(false);
            disconnectButton.setEnabled(false);
        } else {
            connectButton.setEnabled(false);
        }

        TextView channelChosenView;
        String channels;
        String channelChosen[] = new String[2];
        if(!(llButton.getChildAt(0) instanceof Button)){
            channelChosenView = (TextView)llButton.getChildAt(0);
            channels = (channelChosenView.getText().toString());
            channelChosen = channels.split(",");
            channelChosen[1] = channelChosen[1].substring(channelChosen[1].length() - 1);
        }

        if(emsModules.size() == 0){
            Log.w("LLPICTURE", "EMSMODULES == 0");
            TextView connectionStat = new TextView(this);
            connectionStat.setText("No EMS-Modules found connected to the process!");
            llPicture.addView(connectionStat);

            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.notconnectedems_notconnectedchannel);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(frameLayout.getWidth() / 2, LinearLayout.LayoutParams.WRAP_CONTENT));
            llPicture.addView(imageView);
        } else if(llButton.getChildAt(0) instanceof Button){
            Log.w("LLPICTURE", "EMSMODULES > 0; NO CHANNEL");
            TextView connectionStat = new TextView(this);
            connectionStat.setText("EMS-Modules found, but no channel connected to the muscle!");
            llPicture.addView(connectionStat);

            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.connectedems_notconnectedchannels);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(frameLayout.getWidth() / 2, LinearLayout.LayoutParams.WRAP_CONTENT));
            llPicture.addView(imageView);
        } else if(channelChosen[1].equals("0")){
            Log.w("LLPICTURE", "EMSMODULES > 0; CHANNEL 0");
            TextView connectionStat = new TextView(this);
            connectionStat.setText("Muscle connected to channel 0!");
            llPicture.addView(connectionStat);

            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.connectedems_channel1connnected);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(frameLayout.getWidth() / 2, LinearLayout.LayoutParams.WRAP_CONTENT));
            llPicture.addView(imageView);
        } else if(channelChosen[1].equals("1")){
            Log.w("LLPICTURE", "EMSMODULES > 0; CHANNEL 1");
            TextView connectionStat = new TextView(this);
            connectionStat.setText("Muscle connected to channel 1!");
            llPicture.addView(connectionStat);

            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.connectedems_channel2connected);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(frameLayout.getWidth() / 2, LinearLayout.LayoutParams.WRAP_CONTENT));
            llPicture.addView(imageView);
        }

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(frameLayout.getWidth() / 2, LinearLayout.LayoutParams.WRAP_CONTENT);
        testButton.setLayoutParams(llParams);
        buttonMin.setLayoutParams(llParams);
        buttonMax.setLayoutParams(llParams);

        llButtons.addView(testButton);
        llButtons.addView(buttonMin);
        llButtons.addView(buttonMax);

        renameButton.setLayoutParams(llParams);
        emsDeviceButton.setLayoutParams(llParams);
        connectButton.setLayoutParams(llParams);
        disconnectButton.setLayoutParams(llParams);
        rangeBarText.setLayoutParams(llParams);
        rangeBar.setLayoutParams(llParams);
        okButton.setLayoutParams(llParams);
        deleteTimeline.setLayoutParams(llParams);

        // Add views
        linearLayout.addView(renameButton);
        linearLayout.addView(emsDeviceButton);
        linearLayout.addView(connectButton);
        linearLayout.addView(disconnectButton);
        linearLayout.addView(rangeBarText);
        linearLayout.addView(rangeBar);
        linearLayout.addView(llButtons);
        linearLayout.addView(okButton);
        linearLayout.addView(deleteTimeline);

        llFrame.addView(linearLayout);
        llFrame.addView(llPicture);

        HSV.addView(llFrame);

        frameLayout.addView(HSV);

    }

    private void calibrateMinMax(LinearLayout llParent, int intensity){
        boolean channelEMS = true;
        LockableScrollView LSV = (LockableScrollView) llParent.getChildAt(1);
        LinearLayout llLSVChild = (LinearLayout) LSV.getChildAt(0);
        LinearLayout llButton = (LinearLayout) llLSVChild.getChildAt(2);
        if(llButton.getChildAt(0) instanceof Button){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set title
            alertDialogBuilder.setTitle("No EMS Device and channel chosen");

            // set dialog message
            alertDialogBuilder.setMessage("You need to choose a device and a channel before you can calibrate the EMS module");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            channelEMS = false;
        }
        if(channelEMS){
            String[] deviceChannel = ((String) ((TextView)llButton.getChildAt(0)).getText()).split(",");
            int deviceIndex = getModuleIndexByName(deviceChannel[0]);
            deviceChannel[1] = deviceChannel[1].substring(deviceChannel[1].length() - 1);
            int channel = Integer.parseInt(deviceChannel[1]);
            Log.w("minmax", "" + intensity);
            emsModules.get(deviceIndex).startCommandCI(channel, intensity);

        }
    }

    // Got the Bluetooth permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gotPermission();

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover EMS-modules.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    // Got Bluetooth permission, start the search
    private void gotPermission(){
        Log.d("PERMISSION", "coarse location permission granted");

        // Bluetooth connection
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        BluetoothAdapter mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If
        // not displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        bleConnector = EMSBluetoothLEService.getInstance(mBluetoothAdapter);
    }

    // Show list of EMS-Moduls and connect them
    private android.app.AlertDialog.Builder getNewAlertDialog(){
        //Creation of an AlertDialog
        alert = new android.app.AlertDialog.Builder(this);

        // Set Title and message
        alert.setTitle("Select the devices");
        // No message because, if the list in the view is too long, the buttons disapper if the message is set
        // alert.setMessage("Select the name of the devices you wish to connect.");

        // Prepare the list of modules
        searchAndShowEMSDevicesView = new SearchAndShowEMSDevicesView(alert.getContext(), null, true);
        searchAndShowEMSDevicesView.setBLEService(bleConnector);
        Log.w("SearchAndShowBLEService", searchAndShowEMSDevicesView.toString());
        alert.setView(searchAndShowEMSDevicesView);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                for (String deviceName : searchAndShowEMSDevicesView.getNamesOfSelectedItems()) {
                    boolean alreadyInList = false;
                    // Check if the module is already connected
                    for(EMSModule emsModule : emsModules){
                        if(emsModule.getDeviceName().equals(deviceName)){
                            alreadyInList = true;
                        }
                    }
                    // Add the not-connected modules to the list
                    if(!alreadyInList) {
                        EMSModule emsModule = new EMSModule(bleConnector, deviceName);
                        emsModule.connect();

                        emsModules.add(emsModule);
                    }
                }

                dialog.dismiss();
                searchAndShowEMSDevicesView.stopScanning();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.cancel();
                searchAndShowEMSDevicesView.stopScanning();
            }
        });

        return alert;
    }

    // Show list of EMS-Moduls and connect to one
    private android.app.AlertDialog.Builder getNewAlertDialogChannel(final View v){
        final ImageButton IB = (ImageButton)v;
        android.app.AlertDialog.Builder alert;
        //Creation of an AlertDialog
        alert = new android.app.AlertDialog.Builder(this);

        // Set Title and message
        alert.setTitle("Select the device and channel");
        // No message, because, if the list in the view is too long, the buttons disapper if the message is set
        // alert.setMessage("Select the name of the device and channel you wish to connect.");

        // Prepare the list of channels
        if(emsModules.size() == 0){
            alert.setMessage("You need to add Modules to the process first");
        }

        searchAndShowEMSChannels = new SearchAndShowEMSChannels(alert.getContext(), null, emsModules, connectedChannels);
        alert.setView(searchAndShowEMSChannels);


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                for (String deviceNameChannel : searchAndShowEMSChannels.getNamesOfSelectedItems()) {
                    boolean alreadyInList = false;
                    // Check if the module is already connected
                    for(String connectedChannel : connectedChannels){
                        if(connectedChannel.equals(deviceNameChannel)){
                            alreadyInList = true;
                        }
                    }
                    // Add the not-connected channel to the list
                    if(!alreadyInList) {

                        connectedChannels.add(deviceNameChannel);

                        LinearLayout llParent = (LinearLayout)IB.getParent();
                        LockableScrollView LSV = (LockableScrollView) llParent.getChildAt(1);
                        LinearLayout llLSVChild = (LinearLayout) LSV.getChildAt(0);
                        LinearLayout llButton = (LinearLayout) llLSVChild.getChildAt(2);
                        TextView deviceText = new TextView(NewGestureActivity.this);
                        deviceText.setText(deviceNameChannel);
                        // deviceText.setHeight(10);
                        llButton.addView(deviceText, 0);
                        timelineConnected(llParent);
                    }
                }
                calibrate(IB);
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                calibrate(IB);
                dialog.cancel();
            }
        });

        return alert;
    }

    private void timelineConnected(LinearLayout llParent){
        LockableScrollView LSV = (LockableScrollView) llParent.getChildAt(1);
        ImageButton playButton = (ImageButton) llParent.getChildAt(0);
        ImageButton caliButton = (ImageButton) llParent.getChildAt(2);
        playButton.setBackgroundResource(R.drawable.button_connected);
        caliButton.setBackgroundResource(R.drawable.button_connected);
        LinearLayout llVert = (LinearLayout) LSV.getChildAt(0);
        LinearLayout llTimeline = (LinearLayout) llVert.getChildAt(1);
        int buttonsCount = llTimeline.getChildCount();
        for(int i = 0; i < buttonsCount; i++){
            if(llTimeline.getChildAt(i) instanceof Button){
                Button element = (Button) llTimeline.getChildAt(i);
                element.setBackgroundResource(R.drawable.button_connected);
            }
            if(llTimeline.getChildAt(i) instanceof ImageButton){
                ImageButton element = (ImageButton) llTimeline.getChildAt(i);
                element.setTag("connected");
                element.setBackgroundResource(R.drawable.button_connected);
            }
        }
    }

    private void timelineDisonnected(LinearLayout llParent){
        LockableScrollView LSV = (LockableScrollView) llParent.getChildAt(1);
        ImageButton playButton = (ImageButton) llParent.getChildAt(0);
        ImageButton caliButton = (ImageButton) llParent.getChildAt(2);
        playButton.setBackgroundResource(android.R.drawable.btn_default);
        caliButton.setBackgroundResource(android.R.drawable.btn_default);
        LinearLayout llVert = (LinearLayout) LSV.getChildAt(0);
        LinearLayout llTimeline = (LinearLayout) llVert.getChildAt(1);
        int buttonsCount = llTimeline.getChildCount();
        for(int i = 0; i < buttonsCount; i++){
            if(llTimeline.getChildAt(i) instanceof Button){
                Button element = (Button) llTimeline.getChildAt(i);
                element.setBackgroundResource(android.R.drawable.btn_default);
            }
            if(llTimeline.getChildAt(i) instanceof ImageButton){
                ImageButton element = (ImageButton) llTimeline.getChildAt(i);
                element.setTag("disconnected");
                element.setBackgroundResource(android.R.drawable.btn_default);
            }
        }
    }

    private void timelineConnectionLost(LinearLayout llParent){
        LockableScrollView LSV = (LockableScrollView) llParent.getChildAt(1);
        ImageButton playButton = (ImageButton) llParent.getChildAt(0);
        ImageButton caliButton = (ImageButton) llParent.getChildAt(2);
        playButton.setBackgroundResource(R.drawable.button_disconnected);
        caliButton.setBackgroundResource(R.drawable.button_disconnected);
        LinearLayout llVert = (LinearLayout) LSV.getChildAt(0);
        LinearLayout llTimeline = (LinearLayout) llVert.getChildAt(1);
        int buttonsCount = llTimeline.getChildCount();
        for(int i = 0; i < buttonsCount; i++){
            if(llTimeline.getChildAt(i) instanceof Button){
                Button element = (Button) llTimeline.getChildAt(i);
                element.setBackgroundResource(R.drawable.button_disconnected);
            }
            if(llTimeline.getChildAt(i) instanceof ImageButton){
                ImageButton element = (ImageButton) llTimeline.getChildAt(i);
                element.setTag("connectionLost");
                element.setBackgroundResource(R.drawable.button_disconnected);
            }
        }
    }

    // Get the module index
    private int getModuleIndexByName(String deviceName){
        int index =-1;
        int i=0;

        for ( EMSModule m : emsModules){
            if(m.getDeviceName().equalsIgnoreCase(deviceName)){
                index = i;
                break;
            }
            i++;
        }
        return index;
    }

    // Plays the timeline
    private void playTimeline(View v){
        ImageButton playButton = (ImageButton)v;
        LinearLayout llParent = (LinearLayout)playButton.getParent();
        LockableScrollView LSV = (LockableScrollView) llParent.getChildAt(1);
        LinearLayout llLSVChild = (LinearLayout) LSV.getChildAt(0);
        LinearLayout llButton = (LinearLayout) llLSVChild.getChildAt(2);
        if(llButton.getChildAt(0) instanceof Button){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set title
            alertDialogBuilder.setTitle("No EMS Device and channel chosen");

            // set dialog message
            alertDialogBuilder.setMessage("You need to choose a device and a channel before you can play the timeline");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            return;
        }
        String[] deviceChannel = ((String) ((TextView)llButton.getChildAt(0)).getText()).split(",");
        int deviceIndex = getModuleIndexByName(deviceChannel[0]);
        deviceChannel[1] = deviceChannel[1].substring(deviceChannel[1].length() - 1);

        String[] intensities = ((String) ((TextView)llParent.getChildAt(llParent.getChildCount() - 1)).getText()).split(",");
        Log.w("DEVICE CHANNEL INTENSITY", "device: " + deviceIndex + ";   channel: " + deviceChannel[1] + ";   Intensites: " + intensities[0] + ", " + intensities[1]);

        LinearLayout llTimelime = (LinearLayout)llLSVChild.getChildAt(1);
        int elementsIndex = llTimelime.getChildCount() - 2;
        ArrayList<Button> buttonlist = new ArrayList<>();
        if(elementsIndex < 0){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set title
            alertDialogBuilder.setTitle("No Elements in the timeline");

            // set dialog message
            alertDialogBuilder.setMessage("You need to add elements to your timeline before you can play it.");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            return;
        } else if(elementsIndex == 0){
            buttonlist.add((Button) llTimelime.getChildAt(0));
        } else {
            for(int i = 0; i <= elementsIndex; i++){
                buttonlist.add((Button) llTimelime.getChildAt(i));
            }
        }
        float iMin = Float.parseFloat(intensities[0]);
        float iMax = Float.parseFloat(intensities[1]);
        int channel = Integer.parseInt(deviceChannel[1]);

        Timeline timeline = new Timeline(deviceIndex, channel, iMin, iMax, buttonlist, emsModules);
        timeline.playElements();
    }

    public void playAll(View v){
        int timelinesCount = ll.getChildCount() - 5;
        ArrayList<Timeline> timelineList = createTimelineArray();
        boolean emsExists = true;
        for(int i = 2; i <= timelinesCount; i++) {
            LinearLayout llTimelineParent = (LinearLayout) ll.getChildAt(i);
            LinearLayout llTimeline = (LinearLayout) llTimelineParent.getChildAt(0);
            LockableScrollView LSV = (LockableScrollView) llTimeline.getChildAt(1);
            LinearLayout llLSVChild = (LinearLayout) LSV.getChildAt(0);
            LinearLayout llButton = (LinearLayout) llLSVChild.getChildAt(2);
            if (llButton.getChildAt(0) instanceof Button && emsExists) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("No EMS Device and channel chosen");

                // set dialog message
                alertDialogBuilder.setMessage("You need to choose a device and a channel for every timeline before you can play the timeline");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                emsExists = false;
            }
        }
        if(emsExists){
            for(final Timeline t: timelineList){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        t.playElements();
                    }
                });
            }
        }
    }

    // Create a ArrayList with the descriptions
    private ArrayList<String> createDescList(){
        int timelinesCount = ll.getChildCount() - 5;
        ArrayList<String> descList = new ArrayList<>();

        for(int i = 2; i <= timelinesCount; i++){
            LinearLayout llTimelineParent = (LinearLayout)ll.getChildAt(i);
            TextView textView = (TextView) llTimelineParent.getChildAt(llTimelineParent.getChildCount() - 1);
            String muscle = textView.getText().toString();

            descList.add(muscle);
        }
        return descList;
    }

    // Create the timeline-array
    private ArrayList<Timeline> createTimelineArray(){
        int timelinesCount = ll.getChildCount() - 5;
        ArrayList<Timeline> timelineList = new ArrayList<>();
        boolean timelinesExists = true;
        if(timelinesCount < 2){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set title
            alertDialogBuilder.setTitle("No timelines found");

            // set dialog message
            alertDialogBuilder.setMessage("You need to add timelines first.");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            timelinesExists = false;
        }
        if(timelinesExists){
            for(int i = 2; i <= timelinesCount; i++){
                LinearLayout llTimelineParent = (LinearLayout)ll.getChildAt(i);
                LinearLayout llTimeline = (LinearLayout)llTimelineParent.getChildAt(0);
                LockableScrollView LSV = (LockableScrollView) llTimeline.getChildAt(1);
                LinearLayout llLSVChild = (LinearLayout) LSV.getChildAt(0);
                LinearLayout llButton = (LinearLayout) llLSVChild.getChildAt(2);

                String[] deviceChannel;
                int deviceIndex;
                if(!(llButton.getChildAt(0) instanceof Button)){
                    deviceChannel = ((String) ((TextView)llButton.getChildAt(0)).getText()).split(",");
                    deviceIndex = getModuleIndexByName(deviceChannel[0]);
                    deviceChannel[1] = deviceChannel[1].substring(deviceChannel[1].length() - 1);
                } else {
                    deviceChannel = new String[2];
                    deviceIndex = getModuleIndexByName(deviceChannel[0]);
                }

                String[] intensities = ((String) ((TextView)llTimeline.getChildAt(llTimeline.getChildCount() - 1)).getText()).split(",");
                Log.w("DEVICE CHANNEL INTENSITY", "device: " + deviceIndex + ";   channel: " + deviceChannel[1] + ";   Intensites: " + intensities[0] + ", " + intensities[1]);

                LinearLayout HSVChild = (LinearLayout)LSV.getChildAt(0);
                LinearLayout llTimeline1 = (LinearLayout)HSVChild.getChildAt(1);
                int elementsIndex = llTimeline1.getChildCount() - 2;
                ArrayList<Button> buttonlist = new ArrayList<>();
                if(elementsIndex == 0){
                    buttonlist.add((Button) llTimeline1.getChildAt(0));
                } else {
                    for(int j = 0; j <= elementsIndex; j++){
                        buttonlist.add((Button) llTimeline1.getChildAt(j));
                    }
                }
                float iMin = Float.parseFloat(intensities[0]);
                float iMax = Float.parseFloat(intensities[1]);
                int channel;
                if(!(llButton.getChildAt(0) instanceof Button)){
                    channel = Integer.parseInt(deviceChannel[1]);
                } else {
                    channel = 0;
                }


                Timeline timeline = new Timeline(deviceIndex, channel, iMin, iMax, buttonlist, emsModules);
                timelineList.add(timeline);
            }
        }
        return timelineList;
    }

    // Scroll with all scollbars to the left
    private void scrollLeft(){
        int timelinesCount = ll.getChildCount() - 5;
        switch (timelinesCount){
            case 1:
                break;

            default:
                LinearLayout llTimelineParent1 = (LinearLayout) ll.getChildAt(2);
                LinearLayout llTimeline1 = (LinearLayout) llTimelineParent1.getChildAt(0);
                LockableScrollView HSV1 = (LockableScrollView)llTimeline1.getChildAt(1);
                int scrollTo = HSV1.getScrollX();
                LinearLayout HSVChild1 = (LinearLayout)HSV1.getChildAt(0);
                LinearLayout buttonParent1 = (LinearLayout)HSVChild1.getChildAt(2);
                Button extraButton1;
                if(buttonParent1.getChildAt(0) instanceof Button){
                    extraButton1 = (Button)buttonParent1.getChildAt(0);
                } else {
                    extraButton1 = (Button)buttonParent1.getChildAt(1);
                }
                int width1 = extraButton1.getWidth();
                for(int i = 2; i <= timelinesCount; i++) {
                    LinearLayout llTimelineParent = (LinearLayout) ll.getChildAt(i);
                    LinearLayout llTimeline = (LinearLayout) llTimelineParent.getChildAt(0);
                    LockableScrollView HSV = (LockableScrollView)llTimeline.getChildAt(1);
                    LinearLayout HSVChild = (LinearLayout)HSV.getChildAt(0);
                    LinearLayout buttonParent = (LinearLayout)HSVChild.getChildAt(2);
                    Button extraButton;
                    if(buttonParent1.getChildAt(0) instanceof Button){
                        extraButton = (Button)buttonParent.getChildAt(0);
                    } else {
                        extraButton = (Button)buttonParent.getChildAt(1);
                    }
                    extraButton.setWidth(width1);

                    HSV.scrollTo(scrollTo - 50, HSV.getScrollY());
                }
        }
    }

    // Scroll with all scollbars to the left
    private void scrollRight(){
        int timelinesCount = ll.getChildCount() - 5;
        switch (timelinesCount){
            case 1:
                break;

            default:
                LinearLayout llTimelineParent1 = (LinearLayout) ll.getChildAt(2);
                LinearLayout llTimeline1 = (LinearLayout) llTimelineParent1.getChildAt(0);
                LockableScrollView HSV1 = (LockableScrollView)llTimeline1.getChildAt(1);
                int scrollTo = HSV1.getScrollX();
                LinearLayout HSVChild1 = (LinearLayout)HSV1.getChildAt(0);
                LinearLayout buttonParent1 = (LinearLayout)HSVChild1.getChildAt(2);
                Button extraButton1;
                if(buttonParent1.getChildAt(0) instanceof Button){
                    extraButton1 = (Button)buttonParent1.getChildAt(0);
                } else {
                    extraButton1 = (Button)buttonParent1.getChildAt(1);
                }
                int width1 = extraButton1.getWidth();
                for(int i = 2; i <= timelinesCount; i++) {
                    LinearLayout llTimelineParent = (LinearLayout) ll.getChildAt(i);
                    LinearLayout llTimeline = (LinearLayout) llTimelineParent.getChildAt(0);
                    LockableScrollView HSV = (LockableScrollView)llTimeline.getChildAt(1);
                    LinearLayout HSVChild = (LinearLayout)HSV.getChildAt(0);
                    LinearLayout timeScale = (LinearLayout) HSVChild.getChildAt(0);
                    int secCount = timeScale.getChildCount() / 2;
                    LinearLayout buttonParent = (LinearLayout)HSVChild.getChildAt(2);
                    Button extraButton;
                    if(buttonParent.getChildAt(0) instanceof Button){
                        extraButton = (Button)buttonParent.getChildAt(0);
                    } else {
                        extraButton = (Button)buttonParent.getChildAt(1);
                    }
                    extraButton.setWidth(width1);

                    int beforescroll = HSV.getScrollX();
                    HSV.scrollTo(scrollTo + 50, HSV.getScrollY());
                    int afterscroll = HSV.getScrollX();
                    if(beforescroll == afterscroll){
                        int width = extraButton.getWidth();
                        extraButton.setWidth(width + 50);
                        int newTimeCount = extraButton.getWidth() / 500;
                        if(secCount < newTimeCount){
                            for(int k = secCount + 1; k <= newTimeCount; k++){
                                addTimeButton(k, timeScale);
                            }
                        }
                        HSV.scrollTo(scrollTo + 50, HSV.getScrollY());
                    }
                }
        }
    }

    // Save the project
    public void saveProject(View v){
        final FrameLayout frameLayout = findViewById(R.id.frameNewGesture);

        // Create the linear layout
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setClickable(true);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        TextView nameLabel = new TextView(this);
        nameLabel.setText("Name your project");

        final EditText nameInput = new EditText(this);
        if(projectNameLoaded != null && !projectNameLoaded.equals("lastSaved")){
            nameInput.setText(projectNameLoaded);
        }

        Button cancelButton = new Button(this);
        cancelButton.setBackgroundResource(android.R.drawable.btn_default);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                frameLayout.removeView(linearLayout);
            }
        });

        Button saveButton = new Button(this);
        saveButton.setBackgroundResource(android.R.drawable.btn_default);
        saveButton.setText("SAVE");
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final ArrayList<Timeline> timelineList = createTimelineArray();
                final ArrayList<String> descList = createDescList();
                String fileName = nameInput.getText().toString();
                fileName = fileName + ".json";
                final File gpxfile = new File(context.getFilesDir(), fileName);

                String fileNameDesc = fileName.substring(0, fileName.length() - 5) + "_desc.json";
                final File descFile = new File(context.getFilesDir(), fileNameDesc);
                boolean fileExists = false;
                // Check if project already exists
                if(gpxfile.exists()){
                    fileExists = true;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);

                    // set title
                    alertDialogBuilder.setTitle("Project already exists");

                    // set dialog message
                    alertDialogBuilder.setMessage("Do you want to overwrite the project?");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            saveProjectFinal(gpxfile, descFile, timelineList, descList, frameLayout, linearLayout, true);
                            dialog.cancel();
                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            frameLayout.removeView(linearLayout);
                        }
                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
                if(!fileExists){
                    saveProjectFinal(gpxfile, descFile, timelineList, descList, frameLayout, linearLayout, false);
                }
            }
        });
        linearLayout.addView(nameLabel);
        linearLayout.addView(nameInput);
        linearLayout.addView(saveButton);
        linearLayout.addView(cancelButton);
        frameLayout.addView(linearLayout);
    }

    private void saveProjectFinal(File gpxfile, File descFile, ArrayList<Timeline> timelineList, ArrayList<String> descList, FrameLayout frameLayout, LinearLayout linearLayout, boolean overwrite){
        if(overwrite){
            try {
                PrintWriter printWriter = new PrintWriter(gpxfile);
                printWriter.print("");
                printWriter.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Create the json
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            FileWriter writer = new FileWriter(gpxfile);
            FileWriter writerDesc = new FileWriter(descFile);
            writerDesc.append("[");
            writer.append("[");
            for(int i = 0; i < timelineList.size(); i++){
                String fileInputJson = "";
                try {
                    fileInputJson = objectMapper.writeValueAsString(timelineList.get(i));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                writer.append(fileInputJson);
                writerDesc.append("{\"muscle\":\"" + descList.get(i) + "\"}");
                if(i < timelineList.size() - 1){
                    writer.append(",");
                    writerDesc.append(",");
                }
            }
            writer.append("]");
            writerDesc.append("]");
            writer.flush();
            writerDesc.flush();
            writer.close();
            writerDesc.close();
            if(frameLayout != null){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("Saved successfully");

                // set dialog message
                alertDialogBuilder.setMessage("Your project was saved successfully");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                frameLayout.removeView(linearLayout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        int timelinesCount = ll.getChildCount() - 5;
        if(timelinesCount >= 2){
            final ArrayList<Timeline> timelineList = createTimelineArray();
            final ArrayList<String> descList = createDescList();
            String fileName = "lastSaved.json";
            final File gpxfile = new File(context.getFilesDir(), fileName);

            String fileNameDesc = "lastSaved_desc.json";
            final File descFile = new File(context.getFilesDir(), fileNameDesc);
            saveProjectFinal(gpxfile, descFile, timelineList, descList, null, null, true);
        }
    }
}
