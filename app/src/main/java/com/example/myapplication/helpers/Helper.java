package com.example.myapplication.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.example.myapplication.overclass.MyWebView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Helper {

    public static MyWebView createWebView(Context context) {
        MyWebView webView = new MyWebView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MyWebView.setWebContentsDebuggingEnabled(true);
        }
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setAllowFileAccess(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setSupportZoom(true);//是否可以缩放，默认true
        webSettings.setBuiltInZoomControls(true);//是否显示缩放按钮，默认false
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        webSettings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
        webSettings.setAppCacheEnabled(true);//是否使用缓存
        webSettings.setDomStorageEnabled(true);//DOM Storage

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webView.isPageLoad.set(true);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();// 接受所有网站的证书
            }

        });
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void send(String msg) {
                webView.receivedValue.set(msg);
            }

            @JavascriptInterface
            public void isok(String msg) {
                webView.runokcheck.set(msg);
            }

        }, "RltReceiver");

        return webView;
    }

    public static String getRequest(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            InputStream inputStream = conn.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder buffer = new StringBuilder();
            String temp = null;

            while ((temp = bufferedReader.readLine()) != null) {
                buffer.append(temp);
            }
            bufferedReader.close();//记得关闭
            reader.close();
            inputStream.close();
            return buffer.toString();
        } catch (IOException e) {
            // pass
        }
        return "";
    }


    public static String postRequest(String requrl, String body) {
        String html = "";
        try {
            URL url = new URL(requrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(50000);//超时时间
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
//      conn.setRequestProperty("User-Agent", Other.getUserAgent(context));
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(body);
            out.flush();
            out.close();

            InputStream inputStream = conn.getInputStream();

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inputStream.close();

            byte[] data = outStream.toByteArray();


            html = new String(data, "utf-8");

        } catch (Exception e) {

        }
        return html;
    }


    public static void log(String msg) {
        System.out.println("==============\n您有一条新消息：\n" + msg);
    }

    public static <T> boolean tillEq(VC<T> v, T finalValue) {
        return tillEq(v, finalValue, 1000, () -> {
        });
    }

    public static <T> boolean tillEq(VC<T> v, T finalValue, long waitTime) {
        return tillEq(v, finalValue, waitTime, () -> {
        });
    }

    public static <T> boolean tillEq(VC<T> v, T finalValue, Runnable loopcallback) {
        return tillEq(v, finalValue, 1000, loopcallback);
    }

    public static <T> boolean tillEq(VC<T> v, T finalValue, long waitTime, Runnable loopcallback) {
        long interval = 100;
        long aculat = 0;
        while (true) {
            if (finalValue instanceof String) {
                if (finalValue.equals(v.get())) break;
            } else if (v.get() == finalValue) {
                break;
            }

            if (aculat >= waitTime) {
                return false;
            }
            Helper.sleep(interval);
            aculat += interval;
            if (loopcallback != null) {
                loopcallback.run();
            }
        }
        return true;
    }

    public static <T> boolean tillNe(VC<T> v, T startValue) {
        return tillNe(v, startValue, 1000);
    }

    public static <T> boolean tillNe(VC<T> v, T startValue, long waitTime) {
        long interval = 100;
        long aculat = 0;
        while (true) {
            if (startValue instanceof String) {
                if (!startValue.equals(v.get())) break;
            } else if (v.get() != startValue) {
                break;
            }

            if (aculat >= waitTime) {
                return false;
            }
            Helper.sleep(interval);
            aculat += interval;
        }
        return true;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            // who care?
        }
    }

    public static String getErrorInfoFromException(Exception e) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
            e.printStackTrace(pw);
            return "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            return "ErrorInfoFromException";
        }
    }

    public static ExeJsRlt webViewExecuteJS(MyWebView webView, String function, boolean asyncWay) {
        return webViewExecuteJS(webView, function, asyncWay, 5000, 200);
    }

    public static ExeJsRlt webViewExecuteJS(MyWebView webView, String function, boolean asyncWay, long waitTime, long loopInterval) {
        VC<String> rltReceiver = new VC<>();
        VC<String> isok = new VC<>("");
        webView.setRunokcheck(isok);
        webView.setReceivedValue(rltReceiver);
        VC<Boolean> meetException = new VC<>(false);
        Runnable submit = () -> {
            meetException.set(false);
            webView.post(() -> {
                try {
                    if (asyncWay) {
                        webView.loadUrl("javascript:(function(){var count = 0; window.loop = function () {if (count > 50) {throw new Error();}; var userfunctionexexrlt = null; try{ userfunctionexexrlt = (" + function + ")();}catch(e){};if(userfunctionexexrlt){RltReceiver.send(userfunctionexexrlt);RltReceiver.isok('ok');}else{window.setTimeout(loop, " + loopInterval + ");}; count++; };window.setTimeout(loop, 10);})();");
                    } else {
                        webView.loadUrl("javascript:(function(){RltReceiver.send((" + function + ")());RltReceiver.isok('ok');})();");
                    }
                } catch (Exception e) {
                    meetException.set(true);
                }
            });
        };
        submit.run();
        Helper.tillEq(isok, "ok", waitTime, () -> {
            if (meetException.get()) {
                submit.run();
            }
        });

        ExeJsRlt rlt = new ExeJsRlt();
        rlt.success = isok.get().equals("ok");
        rlt.rlt = rltReceiver.get();
        return rlt;
    }

    public static boolean webViewLoadPage(MyWebView webView, String url) {
        VC<Boolean> isload = new VC<>(false);
        webView.setIsPageLoad(isload);
        VC<Boolean> meetException = new VC<>(false);
        Runnable submit = () -> {
            meetException.set(false);
            webView.post(() -> {
                try {
                    webView.loadUrl(url);
                } catch (Exception e) {
                    meetException.set(true);
                }
            });
        };
        submit.run();
        Helper.tillEq(isload, true, 5000, () -> {
            if (meetException.get()) {
                submit.run();
            }
        });
        return isload.get();
    }


    public static boolean terminateWhenReturnTrue(Supplier<Boolean> supplier, int maxExecTimes) {
        return terminateWhenReturnTrue(supplier, maxExecTimes, 200);
    }


    public static boolean terminateWhenReturnTrue(Supplier<Boolean> supplier, int maxExecTimes, long interval) {
        VC<Boolean> latestRlt = new VC<>(false);
        do {
            try {
                Boolean rlt = supplier.get();
                latestRlt.set(rlt);
                if (null != rlt && rlt) {
                    break;
                }
            } catch (Exception e) {
                // pass
            }
            sleep(interval);
            maxExecTimes--;
        } while (maxExecTimes > 0);
        return latestRlt.get();
    }
}