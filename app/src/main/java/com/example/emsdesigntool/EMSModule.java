/**
 * Multiple Devices
 *
 *  Copyright 2016 by Tim Dünte <tim.duente@hci.uni-hannover.de>
 *  Copyright 2016 by Max Pfeiffer <max.pfeiffer@hci.uni-hannover.de>
 *
 *  Licensed under "The MIT License (MIT) – military use of this product is forbidden – V 0.2".
 *  Some rights reserved. See LICENSE.
 *
 * @license "The MIT License (MIT) – military use of this product is forbidden – V 0.2"
 * <https://bitbucket.org/MaxPfeiffer/letyourbodymove/wiki/Home/License>
 */

package com.example.emsdesigntool;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.example.emsdesigntool.commands.EMSBluetoothLEService;
import com.example.emsdesigntool.commands.EMSGattCallback;

import java.util.ArrayList;
import java.util.Observer;

/**
 * Created by pfeiffer on 16.06.15.
 *
 */
public class EMSModule implements IEMSModule {
    public String getDeviceName() {
        return deviceName;
    }

    private String deviceName = "";
    private EMSBluetoothLEService bluetoothLEService;

    private int[] MAXINTENSITY = {100, 100};
    private int[] intensities = {MAXINTENSITY[0], MAXINTENSITY[1]};
    private int[] signalLenghtes = {1000, 1000};
    private int[] currentPattern = {0,0};
    private int [] patterns = {0,1,2,3,4};
    private int [] increasTime = {0,0};
    private int [] decreasTime = {0,0};


    private int minResendTime = 50; //ms
    private int [] maxResenTime = {(int)( (double) signalLenghtes[0] * 0.75), (int)( (double) signalLenghtes[1] * 0.75) };
    private boolean [] updateDevice ={true,true};

    private EMSGattCallback emsGattCallback;

    private ArrayList<Observer> toAdd = new ArrayList<Observer>();

    public EMSModule(EMSBluetoothLEService bluetoothLEService, String deviceName) {

        this.deviceName = deviceName;
        this.bluetoothLEService = bluetoothLEService;
   }


    public EMSBluetoothLEService getBluetoothLEConnector() {
        return bluetoothLEService;
    }



    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        if (bluetoothLEService.isConnected(deviceName)) {
            disconnect();
        }
    }

    public boolean isConnected(){
        return bluetoothLEService.isConnected(deviceName);
    }

    public void connect() {
        if (bluetoothLEService.isConnected(deviceName)) {
            disconnect();
        }
        this.emsGattCallback = bluetoothLEService.connectTo(deviceName);
        if(toAdd.size()> 0){
            for(Observer observer: toAdd){
                emsGattCallback.addObserver(observer);
            }
        }
    }

    public void disconnect() {
        // Clear command list. reset stuff. send stop command to Device.
        stopCommand(0);
        stopCommand(1);
        bluetoothLEService.disconnect(this.deviceName);
    }

    public void sendMessageToBoard(String msg){

        bluetoothLEService.sendMessageToEMSDevice(deviceName, msg);
    }


    @Override
    public void setIntensity(int intensity, int channel) {
        this.intensities[channel]= intensity;

        Log.w("setIntensity", " Channel: " + channel + " Intensity: " + intensity);
        updateDevice[channel] = true;
        //bluetoothLEService.sendMessageToEMSDevice("C" + channel + "I" + intensity);

    }

    @Override
    public void setMAXINTENSITY(int intensity, int channel) {

        MAXINTENSITY[channel] = intensity;
    }

    @Override
    public void setSignalLength(int time, int channel) {
        this.signalLenghtes[channel] = time;
        maxResenTime[channel] = (int)( (double) signalLenghtes[channel] * 0.75);
        updateDevice[channel] = true;
        //bluetoothLEService.sendMessageToEMSDevice("C"+channel+"T"+time);
    }


    @Override
    public void stopCommand(int channel) {

        stopHandlerC(channel);
        setIntensity(0, channel);
        updateDevice[channel] = false;
        bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I0T0G");

    }

    @Override
    public void startCommandC(int channel) {
        stopHandlerC(channel);
        updateDevice[channel] = true;
        tickChannel(channel);

    }

    @Override
    public void startCommandCT(int channel, int ontTime) {
        stopHandlerC(channel);
        signalLenghtes[channel] = ontTime;
        bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I" + intensities[channel] + "T" + signalLenghtes[channel] + "G");



    }

    @Override
    public void startCommandCI(int channel, int intensity) {
        stopHandlerC(channel);
        intensities[channel] = intensity;
        Log.w("INTENSITY", intensity + "");
        intensity += 1;
        bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I" + intensity + "T" + signalLenghtes[channel] + "G");
        bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I" + intensity + "T" + signalLenghtes[channel] + "G");



    }

    @Override
    public void startCommandCIT(int channel, int intensity, int ontTime) {
        stopHandlerC(channel);
        signalLenghtes[channel] = ontTime;
        intensities[channel] = intensity;
        increasTime[channel]=0;
        decreasTime[channel]=0;

        bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I" + intensity + "T" + ontTime + "G");

    }

    @Override
    public void startCommandCincTT(int channel, int increaseTime, int ontTime) {

        stopHandlerC(channel);
        signalLenghtes[channel] = ontTime;
        increasTime[channel]=increaseTime;
        decreasTime[channel]=0;
        bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I" + intensities[channel] + "T" + signalLenghtes[channel] + "G");

    }

    @Override
    public void startCommandCdecTT(int channel, int decreaseTime, int ontTime) {
        stopHandlerC(channel);
        signalLenghtes[channel] = ontTime;
        increasTime[channel]=0;
        decreasTime[channel]=decreaseTime;
        bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I" + intensities[channel] + "T" + signalLenghtes[channel] + "G");


    }

    @Override
    public void startCommandCincTdecTT(int channel, int increaseTime, int decreaseTime, int ontTime, int intensy) {
        stopHandlerC(channel);
        signalLenghtes[channel] = ontTime;
        increasTime[channel]=increaseTime;
        decreasTime[channel]=decreaseTime;
     /*!!!!!!!!!!!!!!!!!!!!!!!*/   intensities[channel]=intensy;
        bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I" + intensities[channel] + "T" + signalLenghtes[channel] + "G");
    }

    @Override
    public void setIntensityOnChannelForTime(int intensity, int channel, long time) {

    }

    @Override
    public void addObserver(Observer observer) {
        if(emsGattCallback == null){
            toAdd.add(observer);
        }else {
            emsGattCallback.addObserver(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        if(emsGattCallback == null){
            toAdd.remove(observer);
        }else {
            emsGattCallback.removeObserver(observer);
        }
    }

    public void setPattern(int pattern, int channel){
        currentPattern[channel] = pattern;
        switch (currentPattern[channel]) {

            case 0:
                intensities[channel] = MAXINTENSITY[channel];
                break;
            case 1:
                intensities[channel] = 0;
                break;
            case 2:
                intensities[channel] = 0;
                break;
            case 3:
                intensities[channel] = 0;
                break;
            case 4:
                intensities[channel] = 0;
                break;
            default:
                break;
        }
    }

    public int getPattern( int channel) {
       return currentPattern[channel];

    }

    /*
    Pappterns
     */
    // Patern 1:
    //Inreases the current from 0 to maxint in 1000 ms then start form 0
    int scheduleNextTickPatternIncreseT100Than0(int channel){
        int increasingSteps = 8;
        int increasingSpeed = minResendTime + 70;
        if (intensities[channel] < MAXINTENSITY[channel]) {
            if (intensities[channel] >= MAXINTENSITY[channel]) {
                intensities[channel] = 1;
            }

            setIntensity(intensities[channel] + increasingSteps, channel);
        }else{

            setIntensity(1, channel);
        }
        return increasingSpeed;
    }

    // Patern 2:
    // Toggle between high and low every second
    int scheduleNextTickPatternHighAndLow(int channel){

        Log.w("scheduleNextTickPattern", " "+channel);

        int increasingSteps = MAXINTENSITY[channel];
        int increasingSpeed = minResendTime + 1000;
        if (intensities[channel] < MAXINTENSITY[channel]) {
            setIntensity(increasingSteps, channel);
        }else{

            setIntensity(0, channel);
        }
        return increasingSpeed;

    }

    //Pattern 3
    // Linear increasing and decreasing
    boolean turnFromUmToDown = false;
    int scheduleNextTickPatternSinus(int channel){
        int increasingSteps = 8;
        int increasingSpeed = minResendTime + 70;
        if (intensities[channel] < MAXINTENSITY[channel] && !turnFromUmToDown) {
            if (intensities[channel] > MAXINTENSITY[channel]) {
                setIntensity(MAXINTENSITY[channel], channel);
            } else {
            setIntensity(intensities[channel] + increasingSteps, channel);
            }



        }else{

            turnFromUmToDown =  true;
        }

        if (intensities[channel] - increasingSteps > 0 && turnFromUmToDown) {

            setIntensity(intensities[channel] - increasingSteps, channel);
        }else{

            turnFromUmToDown =  false;
        }

        if ( intensities[channel] <0 ) intensities[channel] =0;
        return increasingSpeed;
    }

    //Pattern 4
    // Inreases the current from 0 to 100 in 3000 ms
    int scheduleNextTickPatternIncrese(int channel){
        int increasingSteps = 4;
        int increasingSpeed = minResendTime + 70;
        if (intensities[channel] + increasingSteps <= MAXINTENSITY[channel]) {

            setIntensity(intensities[channel] + increasingSteps, channel);
        }else{

            setIntensity(intensities[channel], channel);
            increasingSpeed = maxResenTime[channel];
        }
        return increasingSpeed;
    }


    private static final int TICKC0 = 0;
    private static final int TICKC1 = 1;

    private void tickChannel(int channel) {


        int nextTick = minResendTime;
        switch(currentPattern[channel]) {

            case 0:
                intensities[channel] = MAXINTENSITY[channel];
                nextTick = maxResenTime[channel];
                break;
            case 1:
                nextTick = scheduleNextTickPatternIncreseT100Than0(channel);
                break;
            case 2:
                nextTick =  scheduleNextTickPatternHighAndLow(channel);
                break;
            case 3:
                nextTick = scheduleNextTickPatternSinus(channel);
                break;
            case 4:
                nextTick = scheduleNextTickPatternIncrese(channel);
                break;
            default:
                break;
        }
        scheduleNextTickC(nextTick, channel);

    }
    private void stopHandlerC(int channel) {
        if (channel == 0)
            tickHandler.removeMessages(TICKC0);

        if (channel == 1)
            tickHandler.removeMessages(TICKC1);
    }


    private void scheduleNextTickC(int delay, int channel) {
        if (channel == 0)
            tickHandler.sendMessageDelayed(tickHandler.obtainMessage(TICKC0), delay);
        if (channel == 1 )
            tickHandler.sendMessageDelayed(tickHandler.obtainMessage(TICKC1), delay);


        //if (updateDevice[channel]) {
            Log.w("UPDATE","Device Name "+ deviceName+ " Channle" + channel + " intensities[channel] " + intensities[channel] + " signalLenghtes[channel]  " + signalLenghtes[channel] + " delay " + delay);
            bluetoothLEService.sendMessageToEMSDevice(deviceName, "C" + channel + "I" + intensities[channel] + "T" + signalLenghtes[channel] + "G");
        //   updateDevice[channel] = false;
        // }
    }

    private Handler tickHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TICKC0:
                    tickChannel(0);
                    break;
                case TICKC1:
                    tickChannel(1);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

}
