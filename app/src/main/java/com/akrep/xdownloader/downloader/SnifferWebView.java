package com.akrep.xdownloader.downloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.akrep.xdownloader.model.VideoQuality;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SnifferWebView {
    private final WebView webView;
    private final Set<String> capturedUrls = new HashSet<>();
    private SnifferListener listener;

    public interface SnifferListener {
        void onVideosFound(List<VideoQuality> qualities);
        void onError(String error);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public SnifferWebView(Context context) {
        webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains(".mp4") && !capturedUrls.contains(url)) {
                    capturedUrls.add(url);
                    Log.d("Sniffer", "Video yakalandı: " + url);
                    notifyListener();
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
    }

    public void startSniffing(String url, SnifferListener listener) {
        this.listener = listener;
        capturedUrls.clear();
        webView.loadUrl(url);
    }

    private void notifyListener() {
        if (listener != null && !capturedUrls.isEmpty()) {
            List<VideoQuality> qualities = new ArrayList<>();
            for (String url : capturedUrls) {
                int height = url.contains("720") ? 720 : (url.contains("480") ? 480 : 360);
                qualities.add(new VideoQuality(height + "p", url, height * 1000, 0, height));
            }
            listener.onVideosFound(qualities);
        }
    }
}
