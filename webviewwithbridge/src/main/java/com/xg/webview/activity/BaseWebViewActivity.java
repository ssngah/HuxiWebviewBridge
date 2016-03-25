package com.xg.webview.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

import com.xg.webview.WebViewJavascriptBridge.JSMethodCallback;
import com.xg.webview.WebViewJavascriptBridge.NativeHandler;
import com.xg.webview.XGBridgeWebView;
import com.xiaogu.webviewbridge.R;

public abstract class BaseWebViewActivity extends AppCompatActivity{
    protected XGBridgeWebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_webview);
        setUpWebView();
    }
    private void setUpWebView(){
        mWebView = (XGBridgeWebView) findViewById(R.id.webview);
        mWebView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        mWebView.addJavascriptInterface( this);
        mWebView.loadUrl(getLoadUrl());

        mWebView.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                
                    switch (event.getAction()) {  
                    case MotionEvent.ACTION_DOWN:
                    	v.requestFocusFromTouch();
                    	break;
                    case MotionEvent.ACTION_UP:

                    	break; 
                    case MotionEvent.ACTION_MOVE:
                        break;  
                    case MotionEvent.ACTION_CANCEL:   
                        break;  
                    }  
                      
                    return false;  
             }  
            
        });
    }

    protected abstract String getLoadUrl();

}
