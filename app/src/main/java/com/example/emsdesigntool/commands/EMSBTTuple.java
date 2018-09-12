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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;


/**
 * Created by Tim Dünte on 10.05.2016.
 */
public class EMSBTTuple {

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public EMSGattCallback getEmsGattCallback() {
        return emsGattCallback;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    private BluetoothDevice bluetoothDevice;
    private EMSGattCallback emsGattCallback;
    private BluetoothGatt bluetoothGatt;

    public EMSBTTuple(BluetoothDevice bluetoothDevice, EMSGattCallback emsGattCallback, BluetoothGatt bluetoothGatt){
        this.emsGattCallback = emsGattCallback;
        this.bluetoothDevice = bluetoothDevice;
        this.bluetoothGatt = bluetoothGatt;
    }


}
