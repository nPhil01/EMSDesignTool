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

package com.example.emsdesigntool.searchandshow;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.emsdesigntool.commands.IEMSBluetoothLEService;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;


/**
 * Created by Tim Dünte on 18.03.2016.
 */
public class SearchAndShowEMSDevicesView extends LinearLayout implements Observer {
    private ArrayList<String> checkedItems = new ArrayList<String>();

    private ArrayAdapter<String> listData;
    private ArrayList<String> testData = new ArrayList<String>();
    private ListView listView;

    private String selectedItem = null;

    private IEMSBluetoothLEService ble;

    public void setBLEService(IEMSBluetoothLEService emsBluetoothLEService) {
        ble = emsBluetoothLEService;
        Log.w("setBLEServiceStringValueOf", ble.toString());
        refreshList();
    }

    public SearchAndShowEMSDevicesView(Context context, AttributeSet attrs){
        this(context, attrs, false);
    }

    public SearchAndShowEMSDevicesView(Context context, AttributeSet attrs, boolean multipleSelection) {

        super(context, attrs);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        ListView listView = new ListView(context);
        if(multipleSelection){
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listData = new ArrayAdapter<String>(context,
                    android.R.layout.simple_list_item_multiple_choice, testData);
        }else {
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listData = new ArrayAdapter<String>(context,
                    android.R.layout.select_dialog_singlechoice, testData);
        }
        listView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        listView.setAdapter(listData);

        Log.w("LIST DATA", listData.toString());

        this.addView(listView);

        listView.setFocusable(true);
        listView.setSelection(0);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               selectedItem = listData.getItem(position);
               view.setActivated(true);
                if(checkedItems.contains(selectedItem)){
                    checkedItems.remove(selectedItem);
                }else{
                    checkedItems.add(selectedItem);
                }
            }

        });
    }
    private void refreshList(){
        Log.w("REFRESH LIST BLE", ble.toString());
        ble.addObserver(this);
        ble.findDevices();
        Log.w("REFRESH LIST", ble.toString());
    }

    @Override
    public void update(Observable observable, Object data) {
        IEMSBluetoothLEService emsService = (IEMSBluetoothLEService) data;
        Log.w("UPDATE DATA", data.toString());

        for(String str: emsService.getListOfFoundEMSDevices()){
            boolean found = false;
            for(int i = 0; i< listData.getCount(); i++){
                if(listData.getItem(i).equals(str)){
                    found = true;
                }
                Log.w("ListDataItem", str);
            }
            if(!found){
                listData.add(str);
            }
        }

    }

    public String getNameOfSelectedItem(){
       return selectedItem;
    }

    public ArrayList<String> getNamesOfSelectedItems() {
        return checkedItems;
    }

    public void stopScanning(){
        ble.stopFindingDevices();
    }
}
