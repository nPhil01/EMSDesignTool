package com.example.emsdesigntool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.util.ArrayList;

public class GestureActivity extends Activity {

    private final Context context = this;
    private String projectName;
    private File project;
    private File muscleFile;
    private String fileContent;
    private String muscleContent;
    private ArrayList<String> stringArrayList;
    private ArrayList<String> muscleArrayList;
    private String[] stringArray;
    private String[] stringArrayMuscle;
    private int[][][] intArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);

        // Get the chosen project
        Intent intent = getIntent();
        projectName = intent.getStringExtra(LoadGestureActivity.EXTRA_MESSAGE);
        project = getFile();
        muscleFile = getMuscleFile();
        try {
            fileContent = buttonString();
            muscleContent = muscleString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            stringArrayList = jsonStringToArray();
            muscleArrayList = jsonStringToArrayMuscle();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        stringArray = createStringArray();
        stringArrayMuscle = createStringArrayMuscle();
        intArray = createIntArray();
        loadToSreen();
    }

    // Get the project-file
    private File getFile(){
        String path = context.getFilesDir().toString();
        File directory = new File(path);
        File[] files = directory.listFiles();
        File file = files[0];

        for(File f: files){
            if(f.getName().equals(projectName)){
                file = f;
            }
        }
        return file;
    }

    // Get the project muscle file
    private File getMuscleFile(){
        String path = context.getFilesDir().toString();
        File directory = new File(path);
        File[] files = directory.listFiles();
        File file = files[0];
        String filename = projectName.substring(0, projectName.length() - 5) + "_desc.json";

        for(File f: files){
            if(f.getName().equals(filename)){
                file = f;
            }
        }
        return file;
    }

    // Get the String with the information
    private String buttonString() throws Exception{
        FileInputStream fileInputStream = new FileInputStream(project);
        String ret = convertStreamToString(fileInputStream);
        fileInputStream.close();
        return ret;
    }

    // Get the String with the information for the muscles
    private String muscleString() throws Exception{
        FileInputStream fileInputStream = new FileInputStream(muscleFile);
        String ret = convertStreamToString(fileInputStream);
        fileInputStream.close();
        return ret;
    }

    // Help-function for getting the string with the information
    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    // Cut the string into ArrayList
    private ArrayList<String> jsonStringToArray() throws JSONException {
        ArrayList<String> stringArray = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(fileContent);
        for(int i = 0; i < jsonArray.length(); i++){
            stringArray.add(jsonArray.getString(i));
        }
        return stringArray;
    }

    // Cut the string into ArrayList
    private ArrayList<String> jsonStringToArrayMuscle() throws JSONException {
        ArrayList<String> stringArray = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(muscleContent);
        for(int i = 0; i < jsonArray.length(); i++){
            stringArray.add(jsonArray.getString(i));
        }
        return stringArray;
    }

    // Cut the ArrayList into String
    private String[] createStringArray(){
        String[] strings = new String[stringArrayList.size()];
        for(int i = 0; i < stringArrayList.size(); i++){
            String[] helpString = stringArrayList.get(i).split(":");
            strings[i] = helpString[1];
            strings[i] = strings[i].substring(0, strings[i].length() - 1);
        }
        return strings;
    }

    // Cut the ArrayList into String
    private String[] createStringArrayMuscle(){
        String[] strings = new String[muscleArrayList.size()];
        for(int i = 0; i < muscleArrayList.size(); i++){
            String[] helpString = muscleArrayList.get(i).split(":");
            strings[i] = helpString[1];
            strings[i] = strings[i].substring(1, strings[i].length() - 2);
        }
        return strings;
    }

    // Create the int-array with the information
    private int[][][] createIntArray(){
        int secondDimensionLength = 0;
        for(int i = 0; i < stringArray.length; i++){
            String[] helpStringArray = stringArray[i].split("],");
            if(secondDimensionLength < helpStringArray.length){
                secondDimensionLength = helpStringArray.length;
            }
        }
        int[][][] intArray = new int[stringArray.length][secondDimensionLength][3];
        for(int i = 0; i < stringArray.length; i++){
            String[] helpStringArray = stringArray[i].split("],");
            helpStringArray[0] = helpStringArray[0].substring(1);
            if(!helpStringArray[helpStringArray.length - 1].equals("]")){
                helpStringArray[helpStringArray.length - 1] = helpStringArray[helpStringArray.length - 1].substring(0, helpStringArray[helpStringArray.length - 1].length() - 2);
                for(int j = 0; j < helpStringArray.length; j++){
                    helpStringArray[j] = helpStringArray[j].substring(1);
                    String[] helpStringArray2 = helpStringArray[j].split(",");
                    intArray[i][j][0] = Integer.parseInt(helpStringArray2[0]);
                    intArray[i][j][1] = Integer.parseInt(helpStringArray2[1]);
                    intArray[i][j][2] = Integer.parseInt(helpStringArray2[2]);
                }
            }
        }
        return intArray;
    }

    // Load the gesture
    private void loadToSreen(){
        Intent intent = new Intent(this, NewGestureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("int_array", intArray);
        intent.putExtras(bundle);
        intent.putExtra("string_array", stringArrayMuscle);
        intent.putExtra("projectName", projectName);
        startActivity(intent);
    }
}