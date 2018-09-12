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

import java.util.ArrayList;
import java.util.Observer;

public interface IEMSBluetoothLEService {

    public boolean isConnected();

    public EMSGattCallback connectTo(String deviceName);

    public void disconnect(String deviceName);

    public ArrayList<String> getListOfFoundEMSDevices();

    public void findDevices();

    public void stopFindingDevices();

    public void addObserver(Observer observer);
}
