package com.akrep.xdownloader.model;

import java.util.List;

public class VideoInfo {
    private String tweetId;
    private String tweetUrl;
    private String authorName;
    private String authorHandle;
    private String tweetText;
    private String thumbnailUrl;
    private String authorAvatarUrl;
    private String title;
    private List<VideoQuality> qualities;
    private long duration; // milisaniye

    public VideoInfo(String tweetId, String tweetUrl) {
        this.tweetId = tweetId;
        this.tweetUrl = tweetUrl;
    }

    public String getTweetId() { return tweetId; }
    public String getTweetUrl() { return tweetUrl; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorHandle() { return authorHandle; }
    public void setAuthorHandle(String authorHandle) { this.authorHandle = authorHandle; }
    public String getTweetText() { return tweetText; }
    public void setTweetText(String tweetText) { this.tweetText = tweetText; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }
    public String getTitle() { return title != null ? title : tweetText; }
    public void setTitle(String title) { this.title = title; }
    public List<VideoQuality> getQualities() { return qualities; }
    public void setQualities(List<VideoQuality> qualities) { this.qualities = qualities; }
    public String getDuration() { 
        if (duration <= 0) return "00:00 min";
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d min", minutes, seconds);
    }
    public void setDuration(long duration) { this.duration = duration; }

    public VideoQuality getBestQuality() {
        if (qualities == null || qualities.isEmpty()) return null;
        VideoQuality best = qualities.get(0);
        for (VideoQuality q : qualities) {
            if (q.getBitrate() > best.getBitrate()) best = q;
        }
        return best;
    }
}
