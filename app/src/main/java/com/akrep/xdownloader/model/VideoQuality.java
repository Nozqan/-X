package com.akrep.xdownloader.model;

public class VideoQuality {
    private String label;       // "1080p", "720p", "480p", "360p"
    private String url;         // Gerçek video URL'si
    private long fileSize;      // Tahmini dosya boyutu (byte)
    private int bitrate;        // Bit hızı
    private int width;
    private int height;

    public VideoQuality(String label, String url, int bitrate, int width, int height) {
        this.label = label;
        this.url = url;
        this.bitrate = bitrate;
        this.width = width;
        this.height = height;
        this.fileSize = -1;
    }

    public String getLabel() { return label; }
    public String getUrl() { return url; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public int getBitrate() { return bitrate; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public String getResolutionTag() {
        if (height >= 1080) return "1080p";
        if (height >= 720) return "720p";
        if (height >= 480) return "480p";
        if (height >= 360) return "360p";
        return label;
    }

    public String getFileSizeFormatted() {
        if (fileSize <= 0) return "Bilinmiyor";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }

    @Override
    public String toString() {
        return label + " (" + getFileSizeFormatted() + ")";
    }
}
