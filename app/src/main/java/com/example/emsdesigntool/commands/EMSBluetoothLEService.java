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

package com.example.emsdesigntool.commands;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

/**
 * Created by Max Pfeiffer on 12.08.2015.
 * Edit by Max Pfeiffer
 */
public class EMSBluetoothLEService extends Observable implements IEMSBluetoothLEService{


    private Handler mHandler = new Handler();
    private ArrayList<BluetoothDevice> moduleBTList = new ArrayList<BluetoothDevice>();

    private static final long UNREADABLE_UUID_EMS_SERVICE_MSB = Long.parseLong("454d532d53657276", 16);
    private static final long UNREADABLE_UUID_EMS_SERVICE_LSB = Long.parseLong("6963652d424c4531", 16);

    private Hashtable<String, EMSBTTuple> emsBtTuple = new Hashtable<String, EMSBTTuple>();

    private static EMSBluetoothLEService myEMSBluetoothLEService = null;

    private BluetoothAdapter blAdapter;

    public static EMSBluetoothLEService getInstance(BluetoothAdapter blAdapter){
        if(myEMSBluetoothLEService == null){
            myEMSBluetoothLEService = new EMSBluetoothLEService(blAdapter);
        }
        return myEMSBluetoothLEService;
    }

    private EMSBluetoothLEService(BluetoothAdapter blAdapter) {
        this.blAdapter = blAdapter;
        myEMSBluetoothLEService = this;
    }

    public boolean isConnected(String deviceName) {
        if(emsBtTuple.get(deviceName) == null){
            return false;
        }

        EMSGattCallback emsGattCallback = emsBtTuple.get(deviceName).getEmsGattCallback();

        if ( emsGattCallback!= null ) {
            return emsGattCallback.isConnected();
        }

        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public EMSGattCallback connectTo(String deviceName) {
        EMSGattCallback gattCallback;
        BluetoothDevice dev;

        EMSBTTuple tuple = emsBtTuple.get(deviceName);
        if(tuple != null){
            dev = tuple.getBluetoothDevice();
            gattCallback = tuple.getEmsGattCallback();
            emsBtTuple.remove(deviceName);
        }else{
            dev = getDeviceByName(deviceName);
            gattCallback = new EMSGattCallback();
        }

        if (dev != null ){
            BluetoothGatt gatt = dev.connectGatt(null, false, gattCallback);
            if (gatt != null) {
                emsBtTuple.put(deviceName, new EMSBTTuple(dev, gattCallback, gatt));
                return gattCallback;
            }
            return null;
        }
        else{
            Log.w("BLE-Service", "Device with name: " + deviceName + " not found!");
            return null;
        }
    }

    public BluetoothDevice getDeviceByName (String name){
        for ( BluetoothDevice dev: moduleBTList){
           if(dev.getName().equals(name)){
                return dev;
            }
        }
        return null;
    }


    public synchronized void sendMessageToEMSDevice(String deviceName, String message) {

        //waits until last message is written
        long start = System.currentTimeMillis();
        Log.w("Bluetooth", "Send message " + message);

        EMSBTTuple tuple = emsBtTuple.get(deviceName);

        if(tuple == null){
            return;
        }

        EMSGattCallback gattCallback = tuple.getEmsGattCallback();

        while (!gattCallback.isWriteDone() && start + 500 > System.currentTimeMillis()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        BluetoothGatt gatt  = tuple.getBluetoothGatt();

        if(gatt != null ) {

            Log.w("Bluetooth", "Send message Gatt " + gatt.getDevice().getName());
            List<BluetoothGattService> serviceList = gatt.getServices();

            BluetoothGattCharacteristic characteristic = null;
            Log.w("Bluetooth", "Send message Services " + serviceList.size() + " list" + serviceList);
            UUID uuid = new UUID(UNREADABLE_UUID_EMS_SERVICE_MSB, UNREADABLE_UUID_EMS_SERVICE_LSB);
            characteristic = gatt.getService(uuid).getCharacteristics().get(0);

            if (characteristic != null) {
                gattCallback.writeIsDone();
                characteristic.setValue(message);
                gatt.writeCharacteristic(characteristic);
            } else {
                System.err.println("Missing characteristic on EMS Device.");
            }
        }
    }

    public void disconnect(String deviceName) {

        BluetoothGatt gatt = emsBtTuple.get(deviceName).getBluetoothGatt();
        Log.w("Bluetooth", "try to disconnect... " );
        if (gatt != null) {
            Log.w("Bluetooth","try to disconnect...");
            gatt.disconnect();

            Log.w("Bluetooth", " Gatt " + gatt.getDevice().getName());

            setChanged();
            notifyObservers(this);

            Log.w("Bluetooth", "done");

        }
        Log.w("Bluetooth", "try to disconnect... ");
    }


    // Device scan callback. Finds EMS Devices and fills the list.
    private BluetoothAdapter.LeScanCallback mScanCallback2 = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            Log.w("ONLESCAN", "in onLeScan");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (device != null && device.getName() != null && device.getName().contains("EMS")) {
                        Log.w("ONLESCAN", "in first if");
                        //Log.i("Bluetooth", "EMS Device found: " + device.getName());
                        if(!moduleBTList.contains(device)){
                            Log.w("ONLESCAN", "in second if");
                            moduleBTList.add(device);
                            setChanged();
                            notifyObservers( myEMSBluetoothLEService);
                        }
                        //Log.i("Bluetooth", "Size of list: " + moduleBTList.size());
                    }
                }
            });
        }
    };

    public ArrayList<String> getListOfFoundEMSDevices(){
        ArrayList<String> deviceNames = new ArrayList<String>();

        for(BluetoothDevice device : moduleBTList){
            deviceNames.add(device.getName());
        }

        return deviceNames;
    }

    public void findDevices() {
        Log.w("FIND DEVICES", "FIND DEVICES");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i("OHO", "begin device scan");
                moduleBTList.clear();

                UUID[] uuids = {new UUID(UNREADABLE_UUID_EMS_SERVICE_MSB, UNREADABLE_UUID_EMS_SERVICE_LSB)};
                blAdapter.startLeScan(uuids, mScanCallback2);
                // Log.i("OHOÞ2222222", blAdapt);
            }
        });

    }
    public void stopFindingDevices() {
        blAdapter.stopLeScan(mScanCallback2);
    }
}
