package com.example.myapplication.overclass;


import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.helpers.ExeJsRlt;
import com.example.myapplication.helpers.Helper;
import com.example.myapplication.helpers.Page;
import com.example.myapplication.helpers.VC;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fi.iki.elonen.NanoHTTPD;


public class WebServer extends NanoHTTPD {
    private static NanoHTTPD SERVER_INSTANCE = null;

    ViewGroup root;
    AppCompatActivity activityContext;
    Map<String, Page> pageIdMap = new HashMap<>(16);
    public boolean singleWebViewMode = true;
    Lock lock = new ReentrantLock(true);
    String preRequestPageId = "";


    public WebServer(int port, AppCompatActivity activityContext, ViewGroup root) {
        super(port);
        this.activityContext = activityContext;
        this.root = root;
    }

    public static WebServer startWebServer(int port, AppCompatActivity activityContext, ViewGroup root) {
        try {
            SERVER_INSTANCE = new WebServer(port, activityContext, root);
            SERVER_INSTANCE.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (Exception e) {
            System.exit(-1);
        }
        return (WebServer) SERVER_INSTANCE;
    }

    public static void closeWebServer() {
        if (SERVER_INSTANCE != null) {
            SERVER_INSTANCE.stop();
            SERVER_INSTANCE = null;
        }
    }


    @Override
    public Response serve(IHTTPSession session) {
        if (session.getUri().startsWith("/spider")) {
            return spiderServe(session);
        }
        return newFixedLengthResponse(Response.Status.OK, "text/html", "<!doctype html>\n" + "\n" + "<head>\n" + "    <meta charset=\"utf-8\">\n" + "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" + "    <meta name=\"viewport\"\n" + "        content=\"width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no\">\n" + "    <meta name=\"applicable-device\" content=\"pc,mobile\">\n" + "    <meta http-equiv=\"Cache-Control\" content=\"no-transform\">\n" + "    <meta http-equiv=\"Cache-Control\" content=\"no-siteapp\">\n" + "</head>\n" + "<h1>Android-Spider</h1>");
    }


    public Response spiderServe(IHTTPSession session) {
        try {
            Map<String, String> files = new LinkedHashMap<>();
            session.parseBody(files);
            JSONObject jsonObject = loadJson(files);

            VC<String> pageId = new VC<>(getFormValue(jsonObject, "pageId"));
            String requestUrl = getFormValue(jsonObject, "requestUrl");
            String finishLoadTag = getFormValue(jsonObject, "finishLoadTag");
            String waitTimeAfterPageLoad = getFormValue(jsonObject, "waitTimeAfterPageLoad");
            String asyncWay = getFormValue(jsonObject, "asyncWay");
            VC<String> needRefresh = new VC<>(getFormValue(jsonObject, "needRefresh"));
            String retryTimes = getFormValue(jsonObject, "retryTimes");
            String function = getFormValue(jsonObject, "function");
            String waitTime = getFormValue(jsonObject, "waitTime");
            String loopInterval = getFormValue(jsonObject, "loopInterval");


            if (singleWebViewMode) {
                lock.lock();
                if (!preRequestPageId.equals(pageId.get())) {
                    needRefresh.set("1");
                }
                preRequestPageId = pageId.get();
                pageId.set("singleView");
            }

            if (!pageIdMap.containsKey(pageId.get())) {
                VC<Boolean> ismount = new VC<>(false);

                root.post(() -> {
                    MyWebView webView = Helper.createWebView(activityContext);
                    RelativeLayout relativeLayout = new RelativeLayout(activityContext);
                    relativeLayout.addView(webView);
                    root.addView(relativeLayout, 0);
                    pageIdMap.put(pageId.get(), new Page(webView, relativeLayout));
                    ismount.set(true);
                });

                if (!Helper.tillEq(ismount, true, 5000)) {
                    throw new RuntimeException("mount webView failed. ");
                }

                needRefresh.set("1");
            }

            if (!needRefresh.get().equals("")) {
                Helper.terminateWhenReturnTrue(() -> {
                    Helper.webViewLoadPage(getWebView(pageId), "http://localhost:8112");
                    return Helper.webViewExecuteJS(getWebView(pageId), "function(){if(location.hostname === 'localhost' || location.hostname === '127.0.0.1'){ return 'ok';};}", true).success;
                }, 3);

                boolean rlt = Helper.terminateWhenReturnTrue(() -> {
                    Helper.webViewLoadPage(getWebView(pageId), requestUrl);
                    Helper.sleep(1000);
                    return Helper.webViewExecuteJS(getWebView(pageId), "function(){if(location.hostname !== 'localhost' && location.hostname !== '127.0.0.1'){ return 'ok';};}", true).success;
                }, 3);
                if (!rlt) {
                    throw new RuntimeException("failed to load page. ");
                }
            }

            if (!singleWebViewMode) {
                getRelativeLayout(pageId).post(() -> {
                    getRelativeLayout(pageId).bringToFront();
                });
            }

            if (!finishLoadTag.equals("")) {
                boolean rlt = Helper.terminateWhenReturnTrue(() -> {
                    return Helper.webViewExecuteJS(getWebView(pageId), "function(){ return document.querySelector('" + finishLoadTag + "'); }", false).success;
                }, 5);
                if (!rlt) {
                    throw new RuntimeException("can't find finish load tag. ");
                }
            }

            if (!waitTimeAfterPageLoad.equals("")) {
                Helper.sleep(Long.parseLong(waitTimeAfterPageLoad));
            }

            int execTimes = 1;
            if (!"".equals(retryTimes)) {
                execTimes = Integer.parseInt(retryTimes);
            }
            ExeJsRlt rlt;
            do {
                if ("".equals(waitTime)) {
                    rlt = Helper.webViewExecuteJS(getWebView(pageId), function, !"".equals(asyncWay));
                } else {
                    if("".equals(loopInterval)){
                        loopInterval = "200";
                    }
                    rlt = Helper.webViewExecuteJS(getWebView(pageId), function, !"".equals(asyncWay), Long.parseLong(waitTime), Long.parseLong(loopInterval));
                }
                execTimes--;
            } while (execTimes > 0 && !rlt.success);

            if (rlt.success) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", rlt.rlt);
            } else {
                throw new RuntimeException("failed to execute js. ");
            }
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", Helper.getErrorInfoFromException(e));
        } finally {
            if (singleWebViewMode) {
                lock.unlock();
            }
        }
    }


    @SuppressWarnings("all")
    public MyWebView getWebView(VC<String> pageId) {
        return pageIdMap.get(pageId.get()).webView;
    }

    @SuppressWarnings("all")
    public RelativeLayout getRelativeLayout(VC<String> pageId) {
        return pageIdMap.get(pageId.get()).relativeLayout;
    }

    @SuppressWarnings("all")
    public JSONObject loadJson(Map<String, String> files) {
        try {
            return new JSONObject(files.get("postData"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getFormValue(JSONObject jsonObject, String k) {
        try {
            return jsonObject.getString(k).trim();
        } catch (Exception e) {
            return "";
        }
    }
}