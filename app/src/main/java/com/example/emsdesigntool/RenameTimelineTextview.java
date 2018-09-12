package com.example.emsdesigntool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

@SuppressLint("ViewConstructor")
public class RenameTimelineTextview extends android.support.v7.widget.AppCompatEditText {

    RenameTimelineTextview(Context context, AttributeSet attrs, String input) {
        super(context, attrs);

        this.setText(input);
    }

    public String getInput(){
        return String.valueOf(this.getText());
    }
}
