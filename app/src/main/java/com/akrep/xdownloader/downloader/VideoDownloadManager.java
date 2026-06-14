package com.akrep.xdownloader.downloader;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Android DownloadManager kullanarak video indirme işlemini yönetir.
 * Sistem DownloadManager kullanıldığı için indirme arka planda devam eder,
 * bildirim gösterilir ve dosya Galeri'ye eklenir.
 */
public class VideoDownloadManager {

    private static VideoDownloadManager instance;
    private final Context context;
    private final DownloadManager downloadManager;

    public static synchronized VideoDownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new VideoDownloadManager(context);
        }
        return instance;
    }

    public interface DownloadCallback {
        void onDownloadStarted(long downloadId, String fileName);
        void onDownloadProgress(long downloadId, int progress, long downloadedBytes, long totalBytes);
        void onDownloadCompleted(long downloadId, String filePath);
        void onDownloadFailed(long downloadId, String reason);
    }

    public VideoDownloadManager(Context context) {
        this.context = context.getApplicationContext();
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /**
     * Video indirmeyi başlat
     *
     * @param videoUrl   İndirilecek video URL'si
     * @param tweetId    Tweet ID (dosya adı için)
     * @param quality    Kalite etiketi (örn. "1080p")
     * @param callback   İndirme durumu callback'i
     * @return downloadId
     */
    public long startDownload(String videoUrl, String tweetId, String quality, DownloadCallback callback) {
        String fileName = generateFileName(tweetId, quality);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));

        // İndirme ayarları
        request.setTitle("Akrep İndirici - " + quality);
        request.setDescription("Twitter/X videosu indiriliyor...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AkrepIndirici/" + fileName);
        request.setMimeType("video/mp4");
        request.allowScanningByMediaScanner();

        // HTTP başlıkları - bot engeli aşmak için
        request.addRequestHeader("User-Agent",
                "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
        request.addRequestHeader("Referer", "https://twitter.com/");
        request.addRequestHeader("Accept", "video/mp4,video/*;q=0.9,*/*;q=0.8");
        request.addRequestHeader("Accept-Language", "tr-TR,tr;q=0.9,en;q=0.8");

        long downloadId = downloadManager.enqueue(request);

        if (callback != null) {
            callback.onDownloadStarted(downloadId, fileName);
            // İlerleme takibi için arka plan thread'i başlat
            startProgressTracking(downloadId, callback);
        }

        return downloadId;
    }

    /**
     * İndirme ilerlemesini takip et
     */
    private void startProgressTracking(long downloadId, DownloadCallback callback) {
        new Thread(() -> {
            boolean downloading = true;
            while (downloading) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(query);

                if (cursor != null && cursor.moveToFirst()) {
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                    int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

                    int status = statusIndex >= 0 ? cursor.getInt(statusIndex) : -1;
                    long downloaded = downloadedIndex >= 0 ? cursor.getLong(downloadedIndex) : 0;
                    long total = totalIndex >= 0 ? cursor.getLong(totalIndex) : -1;

                    switch (status) {
                        case DownloadManager.STATUS_RUNNING:
                        case DownloadManager.STATUS_PENDING:
                            if (total > 0) {
                                int progress = (int) ((downloaded * 100L) / total);
                                callback.onDownloadProgress(downloadId, progress, downloaded, total);
                            }
                            break;

                        case DownloadManager.STATUS_SUCCESSFUL:
                            String localUri = localUriIndex >= 0 ? cursor.getString(localUriIndex) : "";
                            callback.onDownloadCompleted(downloadId, localUri != null ? localUri : "");
                            downloading = false;
                            break;

                        case DownloadManager.STATUS_FAILED:
                            int reason = reasonIndex >= 0 ? cursor.getInt(reasonIndex) : -1;
                            callback.onDownloadFailed(downloadId, getFailureReason(reason));
                            downloading = false;
                            break;
                    }
                    cursor.close();
                } else {
                    if (cursor != null) cursor.close();
                    downloading = false;
                }
            }
        }).start();
    }

    /**
     * Dosya adı oluştur
     */
    private String generateFileName(String tweetId, String quality) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "X_" + tweetId + "_" + quality + "_" + timestamp + ".mp4";
    }

    /**
     * Hata nedenini Türkçe açıkla
     */
    private String getFailureReason(int reason) {
        switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME: return "İndirme devam ettirilemedi";
            case DownloadManager.ERROR_DEVICE_NOT_FOUND: return "Depolama alanı bulunamadı";
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS: return "Dosya zaten mevcut";
            case DownloadManager.ERROR_FILE_ERROR: return "Dosya hatası";
            case DownloadManager.ERROR_HTTP_DATA_ERROR: return "HTTP veri hatası";
            case DownloadManager.ERROR_INSUFFICIENT_SPACE: return "Yetersiz depolama alanı";
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS: return "Çok fazla yönlendirme";
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE: return "Bilinmeyen HTTP hatası";
            case DownloadManager.ERROR_UNKNOWN: return "Bilinmeyen hata";
            default: return "Hata kodu: " + reason;
        }
    }

    /**
     * İndirmeyi iptal et
     */
    public void cancelDownload(long downloadId) {
        downloadManager.remove(downloadId);
    }

    public void enqueueDownload(com.akrep.xdownloader.model.VideoQuality quality) {
        startDownload(quality.getUrl(), "Video", quality.getLabel(), null);
    }

    /**
     * İndirme klasörünü al
     */
    public static File getDownloadDirectory() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "AkrepIndirici");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }
}
