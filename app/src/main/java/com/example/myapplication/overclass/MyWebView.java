package com.example.myapplication.overclass;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.helpers.VC;

public class MyWebView extends WebView {

    public VC<Boolean> isPageLoad = new VC<>(false);
    public VC<String> receivedValue = new VC<>();

    public VC<String> runokcheck = new VC<>();

    public MyWebView(@NonNull Context context) {
        super(context);
    }

    public MyWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setIsPageLoad(VC<Boolean> isPageLoad) {
        this.isPageLoad = isPageLoad;
    }

    public void setReceivedValue(VC<String> receivedValue) {
        this.receivedValue = receivedValue;
    }

    public void setRunokcheck(VC<String> runokcheck) {
        this.runokcheck = runokcheck;
    }
}
