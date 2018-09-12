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

import java.util.Observer;

/**
 * Created by pfeiffer on 30.07.15.
 */
public interface IEMSModule {



    public void setIntensity(int intensity, int channel);

    public void setMAXINTENSITY(int progress, int channel);

    public void setSignalLength(int time, int channel);

    public void stopCommand(int channel);

    public void startCommandC(int channel);

    public void startCommandCT(int channel, int ontTime);

    public void startCommandCI(int channel, int intensity);

    public void startCommandCIT(int channel, int intensity, int ontTime);

    public void startCommandCincTT(int channel, int increaseTime, int ontTime);

    public void startCommandCdecTT(int channel, int decreaseTime, int ontTime);

    public void startCommandCincTdecTT(int channel, int increaseTime, int decreaseTime, int ontTime, int inensity);

    public void setIntensityOnChannelForTime(int intensity, int channel, long time);

    public void addObserver(Observer observer);

    public void removeObserver(Observer observer);

}
