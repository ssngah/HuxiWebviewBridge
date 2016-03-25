package com.xg.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.xg.webview.WebViewJavascriptBridge.JSMethodCallback;
import com.xg.webview.WebViewJavascriptBridge.NativeHandler;
import com.xiaogu.webviewbridge.R;

import java.io.InputStream;

@SuppressLint("SetJavaScriptEnabled")
public class XGBridgeWebView extends WebView{
    private WebViewJavascriptBridge mBridge;
    private BridgeWebViewClient bridgeClient = null;
    public XGBridgeWebView(Context context,AttributeSet attrs){
        this(context, attrs, 0);
    }

    public XGBridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initWebView();
    }
    public XGBridgeWebView(Context context) {
        this(context, null, 0);
    }
	private void initWebView(){
        getSettings().setJavaScriptEnabled(true);
        //getSettings().setPluginState(PluginState.ON);
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowContentAccess(true);
        if(Build.VERSION.SDK_INT>=16) {
            getSettings().setAllowFileAccessFromFileURLs(true);
            getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        getSettings().setAppCacheEnabled(false);
        //TODO should be set outside
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            setWebContentsDebuggingEnabled(true);
        }
        bridgeClient = new BridgeWebViewClient(getContext());
        super.setWebViewClient(bridgeClient);
        setWebChromeClient(new WebChromeClient(){
        	@Override
        	public boolean onJsAlert(WebView view, String url, String message,
        			final JsResult result) {
//        		return super.onJsAlert(view, url, message, result);
        		new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int wicht)
                    {
                        result.cancel();
                    }
                }).setCancelable(false)
                .create()
                .show();
        		return true;
        	}
        	
        	@Override
        	public boolean onJsConfirm(WebView view, String url,
        			String message, final JsResult result) {
        		new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int wicht)
                    {
                        result.confirm();
                    }
                }).setNegativeButton(android.R.string.cancel, new AlertDialog.OnClickListener(){
                	public void onClick(DialogInterface dialog, int wicht)
                    {
                        result.cancel();
                    }
                }).setCancelable(false)
                .create()
                .show();
        		return true;
        	}
        });
    }
    @Override
    public void setWebViewClient(WebViewClient client) {
        throw new RuntimeException("use setBridgeWebViewClient instead and" +
        		" please call super method at first when you need to override onPageFinished");
    }
    public void setBridgeWebViewClient(BridgeWebViewClient client){
        super.setWebViewClient(client);
        bridgeClient = client;
    }
    @Override
    public void addJavascriptInterface(Object obj, String interfaceName) {
       throw new RuntimeException("Use addJavascriptInterface(Activity)" +
       		" instead of the old addjavainteerface");
    }
    public void addJavascriptInterface(Activity context){
        mBridge = new WebViewJavascriptBridge(context, this);
        super.addJavascriptInterface(mBridge, "_WebViewJavascriptBridge");
        
    }
    public void callJsMethod(final String methodName){
    	if (!bridgeClient.bridgeScriptLoaded) {
    		new Handler(getContext().getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					callJsMethod(methodName);
				}
			}, 100);
			return;
		}
        tryToNotifyUser();
        mBridge.callHandler(methodName);
    }
    public void callJsMethod(final String methodName,final String data){
    	if (!bridgeClient.bridgeScriptLoaded) {
    		new Handler(getContext().getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					callJsMethod(methodName,data);
				}
			}, 100);
			return;
		}
        tryToNotifyUser();
        mBridge.callHandler(methodName, data);
    }
    public void callJsMethod(final String methodName,final String data,final JSMethodCallback callback){
        String dataStr = data;
        if(dataStr!=null){
            dataStr = dataStr.replace("\\","\\\\");
            dataStr = dataStr.replace("\"","\\\"");
            dataStr = dataStr.replace("\'","\\\'");
            dataStr = dataStr.replace("\n","\\n");
            dataStr = dataStr.replace("\r","\\r");
            dataStr = dataStr.replace("\f","\\f");
        }
        final String formatData = dataStr;
    	if (!bridgeClient.bridgeScriptLoaded) {
    		new Handler(getContext().getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					callJsMethod(methodName,formatData,callback);
				}
			}, 100);
			return;
		}
        tryToNotifyUser();
        mBridge.callHandler(methodName, data, callback);
    }
    private void tryToNotifyUser(){
        if(mBridge==null){
            throw new RuntimeException("you should call addJavacriptInterface(NativeHandler,Activity) first");
        }
    }
    
    public void registerHandler(String handlerName,NativeHandler handler){
    	if (mBridge != null) {
    		mBridge.registerHandler(handlerName, handler);
		}
    }
    ////////////////////////////
    
    @SuppressLint("NewApi")
	public static class BridgeWebViewClient extends WebViewClient{
        private String mScript;
        private  Context context;
        public boolean bridgeScriptLoaded = false;
        public BridgeWebViewClient(Context context) {
            InputStream is = context.getResources().openRawResource(R.raw.webviewjavascriptbridge);
            mScript = WebViewJavascriptBridge.convertStreamToString(is);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            loadWebViewJavascriptBridgeJs(view);
        }


        private void loadWebViewJavascriptBridgeJs(WebView webView) {
            if(android.os.Build.VERSION.SDK_INT >= 19)
            	webView.evaluateJavascript(mScript,new ValueCallback<String>(){
					@Override
					public void onReceiveValue(String arg0) {
			            bridgeScriptLoaded = true;
					}
            	});
            else {
                webView.loadUrl("javascript:" + mScript);
                bridgeScriptLoaded = true;
			}
        }
        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e("webview error","we've got an error");
        }
    }
}
