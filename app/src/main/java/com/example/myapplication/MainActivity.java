package com.example.myapplication;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.overclass.WebServer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatActivity context = this;
        setContentView(ActivityMainBinding.inflate(getLayoutInflater()).getRoot());
        ViewGroup root = findViewById(R.id.linearL);

        WebServer webServer = WebServer.startWebServer(8112, context, root);

        Button mButton = findViewById(R.id.mbutton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webServer.singleWebViewMode) {
                    mButton.post(() -> {
                        mButton.setText("切换到单WebView模式");
                    });
                } else {
                    mButton.post(() -> {
                        mButton.setText("切换到多WebView模式");
                    });
                }
                webServer.singleWebViewMode = !webServer.singleWebViewMode;
            }
        });
    }
}