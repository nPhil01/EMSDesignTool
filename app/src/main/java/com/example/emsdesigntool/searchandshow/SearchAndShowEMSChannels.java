package com.example.emsdesigntool.searchandshow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.example.emsdesigntool.EMSModule;

import java.util.ArrayList;

public class SearchAndShowEMSChannels extends LinearLayout {

    private ArrayAdapter<String> channelsToConnect;
    private String selectedItem = null;
    private ArrayList<String> checkedItems = new ArrayList<String>();
    private ArrayList<String> testData = new ArrayList<String>();

    public SearchAndShowEMSChannels(Context context, AttributeSet attrs, ArrayList<EMSModule> emsModules, ArrayList<String> connectedChannels){
        super(context, attrs);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        ListView listView = new ListView(context);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        channelsToConnect = new ArrayAdapter<String>(context,
                android.R.layout.select_dialog_singlechoice, testData);

        listView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        for(EMSModule emsModule : emsModules){
            boolean foundCH1 = false;
            boolean foundCH2 = false;
            if(emsModule.isConnected()){
                for(int i = 0; i < connectedChannels.size(); i++){
                    if(connectedChannels.get(i).equals(emsModule.getDeviceName() + ",CH0")){
                        foundCH1 = true;
                    }
                    if(connectedChannels.get(i).equals(emsModule.getDeviceName() + ",CH1")){
                        foundCH2 = true;
                    }
                }
                if(!foundCH1){
                    channelsToConnect.add(emsModule.getDeviceName() + ",CH0");
                }
                if(!foundCH2){
                    channelsToConnect.add(emsModule.getDeviceName() + ",CH1");
                }
            }
        }

        listView.setAdapter(channelsToConnect);
        this.addView(listView);

        listView.setFocusable(true);
        listView.setSelection(0);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = channelsToConnect.getItem(position);
                view.setActivated(true);
                if(checkedItems.contains(selectedItem)){
                    checkedItems.remove(selectedItem);
                }else{
                    checkedItems.add(selectedItem);
                }
            }

        });
    }

    public ArrayList<String> getNamesOfSelectedItems() {
        return checkedItems;
    }
}
