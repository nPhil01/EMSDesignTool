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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Observer;
import java.util.UUID;

/**
 * Created by Tim Dünte on 10.05.2016.
 */
public class EMSGattCallback extends BluetoothGattCallback {


    private boolean changed = false;
    private boolean connected = false;

    private boolean writeDone = true;

    public synchronized boolean isWriteDone(){
        return writeDone;
    }

    public synchronized void writeIsDone(){
        writeDone = false;
    }

    private ArrayList<Observer> observers = new ArrayList<Observer>();

    private void setChanged() {
        changed = true;
    }

    public void notifyObservers() {
        if (changed) {
            for (Observer observer : observers) {
                observer.update(null, this);
            }
            changed = false;
        }
    }


    public void addObserver(Observer observer) {
        observers.add(observer);
    }


    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    // Result of a characteristic read operation
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        //not used
    }

    @Override
    // Result of a characteristic write operation
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        Log.w("Bluetooth", "Write of  "
                + uuidToReadableString(characteristic.getUuid())
                + " was successful: "
                + (status == BluetoothGatt.GATT_SUCCESS)
                + " value was: " + characteristic.getStringValue(0));
        writeDone = true;
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.w("Bluetooth","Error: " + status);
        }
    }

    // Various callback methods defined by the BLE API.
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.w("Bluetooth","Connected!");
            gatt.discoverServices();
            connected = true;

            setChanged();
            notifyObservers();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.w("Bluetooth","Connection lost! Try to reconnect. ");
            connected = false;
            gatt.close();
            setChanged();
            notifyObservers();
        }
    }

    public static String uuidToReadableString(UUID uuid) {
        byte[] bytes1 = ByteBuffer.allocate(8)
                .putLong(uuid.getMostSignificantBits()).array();
        byte[] bytes2 = ByteBuffer.allocate(8)
                .putLong(uuid.getLeastSignificantBits()).array();
        return new String(bytes1) + new String(bytes2);
    }

    public boolean isConnected(){
        return connected;
    }

}
