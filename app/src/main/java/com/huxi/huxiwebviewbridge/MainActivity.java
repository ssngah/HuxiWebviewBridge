package com.huxi.huxiwebviewbridge;

import android.os.Bundle;

import com.xg.webview.activity.BaseWebViewActivity;

public class MainActivity extends BaseWebViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected String getLoadUrl() {
        return null;
    }
}
