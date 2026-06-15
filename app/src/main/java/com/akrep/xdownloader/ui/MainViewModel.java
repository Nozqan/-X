package com.akrep.xdownloader.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.akrep.xdownloader.downloader.XVideoExtractor;
import com.akrep.xdownloader.model.VideoInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    public enum State {
        IDLE, LOADING, SUCCESS, ERROR, ANALYZING
    }

    private final MutableLiveData<State> state = new MutableLiveData<>(State.IDLE);
    private final MutableLiveData<VideoInfo> videoInfo = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> loadingMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final XVideoExtractor extractor;
    private final ExecutorService executor;

    public MainViewModel(@NonNull Application application) {
        super(application);
        extractor = new XVideoExtractor(application);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<State> getState() { return state; }
    public LiveData<VideoInfo> getVideoInfo() { return videoInfo; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getLoadingMessage() { return loadingMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return errorMessage; }

    /**
     * Tweet URL'sini analiz et
     */
    public void analyzeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            errorMessage.setValue("URL boş olamaz");
            isLoading.setValue(false);
            return;
        }

        String tweetId = XVideoExtractor.extractTweetId(url.trim());
        if (tweetId == null) {
            errorMessage.setValue("Geçersiz Twitter/X URL'si.\nÖrnek: https://x.com/kullanici/status/1234567890");
            isLoading.setValue(false);
            return;
        }

        isLoading.setValue(true);
        state.setValue(State.ANALYZING);
        loadingMessage.setValue("Tweet analiz ediliyor...");

        executor.execute(() -> {
            try {
                loadingMessage.postValue("Video bilgileri alınıyor...");
                VideoInfo info = extractor.extract(url.trim());

                loadingMessage.postValue("Kalite seçenekleri hazırlanıyor...");

                if (info.getQualities() == null || info.getQualities().isEmpty()) {
                    errorMessage.postValue("Bu tweet'te video bulunamadı.");
                    state.postValue(State.ERROR);
                } else {
                    videoInfo.postValue(info);
                    state.postValue(State.SUCCESS);
                }
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage() != null ? e.getMessage() : "Bilinmeyen hata oluştu");
                state.postValue(State.ERROR);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void downloadVideo(com.akrep.xdownloader.model.VideoQuality quality) {
        com.akrep.xdownloader.downloader.VideoDownloadManager.getInstance(getApplication()).enqueueDownload(quality);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
