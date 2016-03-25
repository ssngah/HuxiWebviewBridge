package com.xg.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Implements Serializable in case of javascript
 * interface will be removed in obfuscated code.
 * reference author: jack_fang Date: 13-8-15
 * updated by:Phyllis
 */
public class WebViewJavascriptBridge implements Serializable {

    private static final long serialVersionUID = -3448613567779349620L;
    WebView mWebView;
    Activity mContext;
    Map<String, NativeHandler> _messageHandlers;
    Map<String, JSMethodCallback> _responseCallbacks;
    long _uniqueId;

    public WebViewJavascriptBridge(Activity context, WebView webview) {
        this.mContext = context;
        this.mWebView = webview;
        _messageHandlers = new HashMap<>();
        _responseCallbacks = new HashMap<>();
        _uniqueId = 0;
    }

    public static String convertStreamToString(java.io.InputStream is) {
        String s = "";
        try {
            Scanner scanner = new Scanner(is, "UTF-8");
            scanner.useDelimiter("\\A");
            if (scanner.hasNext())
                s = scanner.next();
            is.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public interface NativeHandler {
        public void handle(String data, JSMethodCallback jsCallback);
    }

    public interface JSMethodCallback {
        public void callback(String data);
    }

    public void registerHandler(String handlerName, NativeHandler handler) {
        _messageHandlers.put(handlerName, handler);
    }

    private class CallbackJs implements JSMethodCallback {
        private final String callbackIdJs;

        public CallbackJs(String callbackIdJs) {
            this.callbackIdJs = callbackIdJs;
        }

        @Override
        public void callback(String data) {
            _callbackJs(callbackIdJs, data);
        }
    }

    private void _callbackJs(String callbackIdJs, String data) {
        Map<String, String> message = new HashMap<String, String>();
        message.put("responseId", callbackIdJs);
        message.put("responseData", data);
        _dispatchMessage(message);
    }

    @JavascriptInterface
    public void _handleMessageFromJs(final String data, String responseId, String responseData,
                                     String callbackId, String handlerName) {
        if (null != responseId) {
            JSMethodCallback responseCallback = _responseCallbacks.get(responseId);
            responseCallback.callback(responseData);
            _responseCallbacks.remove(responseId);
        } else {
            JSMethodCallback responseCallback = null;
            if (null != callbackId) {
                responseCallback = new CallbackJs(callbackId);
            }
            final JSMethodCallback finalResponseCallback = responseCallback;
            NativeHandler handler = null;
            if (null == handlerName) {
                return;
            }
            handler = _messageHandlers.get(handlerName);
            if (null == handler) {
                Log.e("test", "WVJB Warning: No handler for " + handlerName);
                return;
            }
            final NativeHandler finalHandler = handler;
            try {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finalHandler.handle(data, finalResponseCallback);
                    }
                });
            } catch (Exception exception) {
                Log.e("WebViewBridge",
                        "WebViewJavascriptBridge: WARNING: java handler threw. "
                                + exception.getMessage());
            }
        }
    }

    public void send(String data) {
        send(data, null);
    }

    public void send(String data, JSMethodCallback responseCallback) {
        _sendData(data, responseCallback, null);
    }

    private void _sendData(String data, JSMethodCallback responseCallback, String handlerName) {
        Map<String, String> message = new HashMap<String, String>();
        if (!TextUtils.isEmpty(data)) {
            message.put("data", data);
        }
        if (null != responseCallback) {
            String callbackId = "java_cb_" + (++_uniqueId);
            _responseCallbacks.put(callbackId, responseCallback);
            message.put("callbackId", callbackId);
        }
        if (null != handlerName) {
            message.put("handlerName", handlerName);
        }
        _dispatchMessage(message);
    }

    @SuppressLint("NewApi")
    private void _dispatchMessage(Map<String, String> message) {
        String messageJSON = new JSONObject(message).toString();
        Log.d("test", "sending:" + messageJSON);
        final String javascriptCommand = String.format(
                "javascript:WebViewJavascriptBridge._handleMessageFromJava('%s');",
                doubleEscapeString(messageJSON));
        final String javascriptCommandForSdk19 = String.format(
                "WebViewJavascriptBridge._handleMessageFromJava('%s');",
                doubleEscapeString(messageJSON));
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= 19)
                    mWebView.evaluateJavascript(javascriptCommandForSdk19, null);
                else {
                    mWebView.loadUrl(javascriptCommand);
                }
            }
        });
    }

    public void callHandler(String handlerName) {
        callHandler(handlerName, null, null);
    }

    public void callHandler(String handlerName, String data) {
        callHandler(handlerName, data, null);
    }

    public void callHandler(String handlerName, String data, JSMethodCallback responseCallback) {
        _sendData(data, responseCallback, handlerName);
    }

    /*
     * You must escape the char \ and char ", or you will not recevie a correct
     * json object in your javascript which will cause a exception in chrome.
     * 
     * Reference:
     * http://stackoverflow.com/questions
     * /5569794/escape-nsstring-for-javascript-input http://www.json.org/
     */
    private String doubleEscapeString(String javascript) {
        String result;
        result = javascript.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        result = result.replace("\'", "\\\'");
        result = result.replace("\n", "\\n");
        result = result.replace("\r", "\\r");
        result = result.replace("\f", "\\f");
        return result;
    }

}
