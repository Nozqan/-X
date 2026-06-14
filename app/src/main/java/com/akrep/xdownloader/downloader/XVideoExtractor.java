package com.akrep.xdownloader.downloader;

import android.content.Context;
import android.util.Log;

import com.akrep.xdownloader.model.VideoInfo;
import com.akrep.xdownloader.model.VideoQuality;
import com.akrep.xdownloader.utils.CookieUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class XVideoExtractor {

    private static final String TAG = "XVideoExtractor";
    private final OkHttpClient client;
    private final Context context;

    public XVideoExtractor(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();
    }

    public VideoInfo extract(String tweetUrl) throws Exception {
        String tweetId = extractTweetId(tweetUrl);
        if (tweetId == null) throw new IllegalArgumentException("Geçersiz URL");

        VideoInfo info = new VideoInfo(tweetId, tweetUrl);

        // --- YÖNTEM 1: SSSBypass (En hızlı bypass) ---
        try {
            return extractViaSSSBypass(info, tweetUrl);
        } catch (Exception e) {
            Log.e(TAG, "SSSBypass başarısız");
        }

        // --- YÖNTEM 2: FxTwitter API (Alternatif Proxy) ---
        try {
            return extractViaFxTwitter(info, tweetId);
        } catch (Exception e) {
            Log.e(TAG, "FxTwitter başarısız");
        }

        // --- YÖNTEM 3: Resmi CDN (Oturumla) ---
        if (CookieUtils.isLoggedIn(context)) {
            try {
                return extractViaOfficialAPI(info, tweetId);
            } catch (Exception e) {
                Log.e(TAG, "Resmi CDN başarısız");
            }
        }

        throw new Exception("Twitter bot koruması aşılamadı. Lütfen uygulamayı kapatıp açın veya linki paylaş butonu ile uygulamaya gönderin.");
    }

    private VideoInfo extractViaSSSBypass(VideoInfo info, String tweetUrl) throws Exception {
        FormBody formBody = new FormBody.Builder()
                .add("id", tweetUrl)
                .add("locale", "tr")
                .add("tt", "0")
                .build();

        Request request = new Request.Builder()
                .url("https://ssstwitter.com/fetch")
                .post(formBody)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .header("X-Requested-With", "XMLHttpRequest")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String html = response.body().string();
            Pattern pattern = Pattern.compile("href=\"(https://video\\.twimg\\.com/[^\"]+)\"");
            Matcher matcher = pattern.matcher(html);
            List<VideoQuality> qualities = new ArrayList<>();
            while (matcher.find()) {
                String vUrl = matcher.group(1).replace("&amp;", "&");
                if (vUrl.contains(".mp4")) {
                    int height = extractHeight(vUrl);
                    qualities.add(new VideoQuality(height + "p", vUrl, height * 1000, 0, height));
                }
            }
            if (qualities.isEmpty()) throw new Exception("Video yok");
            info.setQualities(qualities);
            return info;
        }
    }

    private VideoInfo extractViaFxTwitter(VideoInfo info, String tweetId) throws Exception {
        // fxtwitter.com API'si Twitter'ı bypass etmek için harikadır
        Request request = new Request.Builder()
                .url("https://api.fxtwitter.com/i/status/" + tweetId)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            JsonObject root = JsonParser.parseString(response.body().string()).getAsJsonObject();
            if (root.has("tweet")) {
                JsonObject tweet = root.getAsJsonObject("tweet");
                if (tweet.has("media") && tweet.getAsJsonObject("media").has("videos")) {
                    JsonArray videos = tweet.getAsJsonObject("media").getAsJsonArray("videos");
                    List<VideoQuality> qualities = new ArrayList<>();
                    for (JsonElement v : videos) {
                        JsonObject obj = v.getAsJsonObject();
                        String vUrl = obj.get("url").getAsString();
                        int height = extractHeight(vUrl);
                        qualities.add(new VideoQuality(height + "p", vUrl, height * 1000, 0, height));
                    }
                    info.setQualities(qualities);
                    return info;
                }
            }
        }
        throw new Exception("FxTwitter başarısız");
    }

    private VideoInfo extractViaOfficialAPI(VideoInfo info, String tweetId) throws Exception {
        String url = "https://cdn.syndication.twimg.com/tweet-result?id=" + tweetId + "&lang=tr";
        Request.Builder builder = new Request.Builder().url(url);
        String cookies = CookieUtils.getCookies(context);
        builder.header("Cookie", cookies);
        String ct0 = extractCookieValue(cookies, "ct0");
        if (ct0 != null) builder.header("x-csrf-token", ct0);

        try (Response response = client.newCall(builder.build()).execute()) {
            JsonObject root = JsonParser.parseString(response.body().string()).getAsJsonObject();
            if (root.has("mediaDetails")) {
                JsonArray media = root.getAsJsonArray("mediaDetails");
                for (JsonElement m : media) {
                    JsonObject obj = m.getAsJsonObject();
                    if (obj.has("video_info")) {
                        if (obj.has("media_url_https")) {
                            info.setThumbnailUrl(obj.get("media_url_https").getAsString());
                        }
                        info.setQualities(parseVariants(obj.getAsJsonObject("video_info")));
                        return info;
                    }
                }
            }
        }
        throw new Exception("Resmi API başarısız");
    }

    private List<VideoQuality> parseVariants(JsonObject videoInfo) {
        List<VideoQuality> list = new ArrayList<>();
        JsonArray variants = videoInfo.getAsJsonArray("variants");
        for (JsonElement v : variants) {
            JsonObject obj = v.getAsJsonObject();
            if ("video/mp4".equals(obj.get("content_type").getAsString())) {
                String url = obj.get("url").getAsString();
                int height = extractHeight(url);
                list.add(new VideoQuality(height + "p", url, height * 1000, 0, height));
            }
        }
        list.sort((a, b) -> b.getBitrate() - a.getBitrate());
        return list;
    }

    private int extractHeight(String url) {
        if (url.contains("1080")) return 1080;
        if (url.contains("720")) return 720;
        if (url.contains("480")) return 480;
        if (url.contains("360")) return 360;
        return 0;
    }

    private String extractCookieValue(String cookies, String key) {
        Pattern pattern = Pattern.compile(key + "=([^;]+)");
        Matcher matcher = pattern.matcher(cookies);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static String extractTweetId(String url) {
        if (url == null) return null;
        Matcher m = Pattern.compile("status(?:es)?/(\\d+)").matcher(url);
        return m.find() ? m.group(1) : null;
    }
}
