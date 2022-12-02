package com.example.myapplication.helpers;

import android.widget.RelativeLayout;

import com.example.myapplication.overclass.MyWebView;

public class Page {
    public Page(MyWebView webView, RelativeLayout relativeLayout) {
        this.webView = webView;
        this.relativeLayout = relativeLayout;
    }

    public MyWebView webView = null;
    public RelativeLayout relativeLayout = null;
}
