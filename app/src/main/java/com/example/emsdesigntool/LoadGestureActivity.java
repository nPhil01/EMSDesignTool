package com.example.emsdesigntool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;

public class LoadGestureActivity extends Activity implements View.OnClickListener {

    private final Context context = this;
    static final String EXTRA_MESSAGE = "com.example.emsdesigntool.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_gesture);
        LinearLayout ll_List = findViewById(R.id.ll_listFiles);

        File[] files = getFiles();
        boolean notJson;
        boolean descFile;
        String name;
        String nameWEnding;
        String ending;
        String _desc;
        for(int i = 0; i < files.length; i++){
            notJson = false;
            descFile = false;
            nameWEnding = files[i].getName();
            if(nameWEnding.length() < 4){
                notJson = true;
                name = "";
            } else {
                name = nameWEnding.substring(0, nameWEnding.length() - 5);
                ending = nameWEnding.substring(nameWEnding.length() - 5, nameWEnding.length());
                if(name.length() >= 5){
                    _desc = name.substring(name.length() - 5, name.length());
                } else {
                    _desc = "";
                }

                if(!ending.equals(".json")){
                    notJson = true;
                }
                if(_desc.equals("_desc")){
                    descFile = true;
                }
            }
            if(!notJson && !descFile){
                Button button = new Button(this);
                button.setBackgroundResource(android.R.drawable.btn_default);
                button.setText(name);
                button.setOnClickListener(this);
                ll_List.addView(button);
            }
        }
    }

    // Get all saved project-files
    private File[] getFiles(){
        String path = context.getFilesDir().toString();
        File directory = new File(path);
        return directory.listFiles();
    }

    @Override
    public void onClick(View v) {
        Button button = (Button)v;
        Intent intent = new Intent(this, GestureActivity.class);
        String projectName = button.getText().toString();
        projectName = projectName + ".json";
        intent.putExtra(EXTRA_MESSAGE, projectName);
        startActivity(intent);
    }
}
