package com.example.emsdesigntool;

import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;

public class Timeline {
    private int deviceIndex;
    private int channel;
    private float iMin;
    private float iMax;
    private ArrayList<Button> buttonlist;
    private static ArrayList<EMSModule> emsModules = new ArrayList<>();
    private long startTime = 0;
    private int[][] buttonArray;

    Timeline(int deviceIndexNew, int channelNew, float iMinNew, float iMaxNew, ArrayList<Button> buttonlistNew, ArrayList<EMSModule> emsModulesNew){
        this.deviceIndex = deviceIndexNew;
        this.channel = channelNew;
        this.iMin = iMinNew;
        this.iMax = iMaxNew;
        this.buttonlist = buttonlistNew;
        emsModules = emsModulesNew;

        // Create array of button-information
        buttonArray = new int [buttonlist.size()][3];
        for(int i = 0; i < buttonlist.size(); i++){
            buttonArray[i][0] = buttonlist.get(i).getWidth();
            buttonArray[i][1] = buttonlist.get(i).getHeight();
            buttonArray[i][2] = (int)buttonlist.get(i).getX();
        }
    }

    public int[][] getButtonArray(){
        return buttonArray;
    }

    public void setButtonArray(int[][] buttonArray){
        this.buttonArray = buttonArray;
    }

    void playElements(){
        if(startTime == 0){
            startTime = System.currentTimeMillis();
        }
        float intensityDif = (iMax - iMin) / 100;
        for(int i = 0; i < buttonlist.size(); i++){
            long currentTime;
            long buttonX = (long)buttonlist.get(i).getX() * 2;
            boolean done = false;
            while(!done){
                currentTime = System.currentTimeMillis();
                if(currentTime - startTime >= buttonX){
                    float intensityF = (((float)buttonlist.get(i).getHeight() - 100) * intensityDif) + iMin;
                    int intensity = (int)intensityF;
                    int duration = buttonlist.get(i).getWidth() * 2;
                    Log.w("START PLAYING", intensity + "    " + duration);
                    emsModules.get(deviceIndex).startCommandCIT(channel, intensity, duration);
                    Log.w("END PLAYING", buttonlist.get(i).toString());
                    done = true;
                }
            }
        }
    }
}
