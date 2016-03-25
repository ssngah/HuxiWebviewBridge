package com.xg.webview.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.xg.webview.WebViewJavascriptBridge.JSMethodCallback;
import com.xg.webview.WebViewJavascriptBridge.NativeHandler;
import com.xg.webview.XGBridgeWebView;
import com.xiaogu.webviewbridge.R;

public abstract class BaseWebViewFragment extends Fragment{
    protected XGBridgeWebView mWebView;
    protected View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if(view==null){
            view = inflater.inflate(R.layout.activity_base_webview, container, false);
            mWebView = (XGBridgeWebView) view.findViewById(R.id.webview);
            setUpWebView();
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        return view;
    }
    protected void setUpWebView(){
        mWebView.loadUrl(getLoadUrl());
        mWebView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        mWebView.addJavascriptInterface(getActivity());
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
