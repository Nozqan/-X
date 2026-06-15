package com.akrep.xdownloader.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.akrep.xdownloader.databinding.ActivityMainBinding;
import com.akrep.xdownloader.model.VideoInfo;
import com.akrep.xdownloader.model.VideoQuality;
import com.akrep.xdownloader.viewmodel.MainViewModel;
import com.bumptech.glide.Glide;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private QualityAdapter qualityAdapter;
    private VideoQuality selectedQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Parmak İzi Kontrolü (Başlangıçta)
        if (getSharedPreferences("AkrepPrefs", MODE_PRIVATE).getBoolean("fingerprint_enabled", false)) {
            showBiometricPrompt();
        }

        setupUI();
        observeViewModel();
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, "Hata: " + errString, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "Tanınmadı!", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Giriş Yap")
                .setSubtitle("Parmak izinizi kullanın")
                .setNegativeButtonText("İptal")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void setupUI() {
        // Liste Yapılandırmaları
        binding.rvQualities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentVideos.setLayoutManager(new GridLayoutManager(this, 2));

        // 1. ANALİZ ET BUTONU
        binding.btnAnalyze.setOnClickListener(v -> {
            String url = binding.etUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                viewModel.analyzeUrl(url);
            } else {
                Toast.makeText(this, "Lütfen link girin", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. YAPIŞTIR BUTONU
        binding.btnPaste.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                binding.etUrl.setText(clipboard.getPrimaryClip().getItemAt(0).getText());
            }
        });

        // 3. SEÇİLENİ İNDİR BUTONU
        binding.btnDownloadSelected.setOnClickListener(v -> {
            if (selectedQuality != null) {
                viewModel.downloadVideo(selectedQuality);
                Toast.makeText(this, "İndirme başlatıldı", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Kalite seçin", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. ALT NAV - HOME
        binding.btnNavHome.setOnClickListener(v -> {
            binding.etUrl.requestFocus();
            Toast.makeText(this, "Ana Ekran", Toast.LENGTH_SHORT).show();
        });

        // 5. ALT NAV - İNDİRİLENLER (GALERİ)
        binding.btnNavDownloads.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, GalleryActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Galeri Hatası!", Toast.LENGTH_SHORT).show();
            }
        });

        // 6. ALT NAV - GEÇMİŞ (GALERİ)
        binding.btnNavHistory.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, GalleryActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Geçmiş Hatası!", Toast.LENGTH_SHORT).show();
            }
        });

        // 7. ALT NAV - AYARLAR
        binding.btnNavSettings.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, SettingsActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Ayarlar Hatası!", Toast.LENGTH_SHORT).show();
            }
        });

        // 8. TWITTER KUŞU (LOGIN)
        binding.btnLogin.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, LoginActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Giriş Hatası!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getVideoInfo().observe(this, info -> {
            if (info != null) {
                showVideoResult(info);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            // Loading göstergesi eklenebilir
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showVideoResult(VideoInfo info) {
        binding.layoutResult.setVisibility(View.VISIBLE);
        
        // Thumbnail
        if (info.getThumbnailUrl() != null && !info.getThumbnailUrl().isEmpty()) {
            Glide.with(this).load(info.getThumbnailUrl()).into(binding.ivThumbnail);
        }

        // Başlık ve Bilgiler
        binding.tvVideoTitle.setText(info.getTitle());
        binding.tvUserHandle.setText(info.getAuthorName() + " • X Downloader");
        binding.tvDuration.setText(info.getDuration());

        // Kalite Listesi
        qualityAdapter = new QualityAdapter(info.getQualities(), quality -> {
            selectedQuality = quality;
            binding.btnDownloadSelected.setText("📥 SEÇİLENİ İNDİR (" + quality.getFileSizeFormatted() + ")");
        });
        binding.rvQualities.setAdapter(qualityAdapter);
    }
}
