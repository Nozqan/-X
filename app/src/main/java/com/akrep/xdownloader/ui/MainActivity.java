package com.akrep.xdownloader.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.concurrent.Executor;

import com.akrep.xdownloader.databinding.ActivityMainBinding;
import com.akrep.xdownloader.model.VideoInfo;
import com.akrep.xdownloader.model.VideoQuality;
import com.akrep.xdownloader.utils.CookieUtils;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private QualityAdapter qualityAdapter;
    private VideoQuality selectedQuality;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        
        // Tema Ayarı Uygula
        if (prefs.getBoolean("dark_theme", true)) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar);
        } else {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
        }
        
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        if (prefs.getBoolean("biometric_lock", false)) {
            // binding.layoutMainContent.setVisibility(View.GONE); // Eğer layout'ta bu ID yoksa hata verir, layout'u kontrol etmeliyim
            checkBiometric();
        } else {
            initApp();
        }
    }

    private void checkBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(() -> {
                    initApp();
                });
            }
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, "Giriş Reddedildi: " + errString, Toast.LENGTH_LONG).show();
                finish();
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Nebi Özkan - Güvenli Giriş")
                .setSubtitle("Lütfen parmak izinizi okutun")
                .setNegativeButtonText("Çıkış")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void initApp() {
        setupUI();
        observeViewModel();
        requestPermissions();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                String url = extractUrl(sharedText);
                if (url != null) {
                    binding.etUrl.setText(url);
                    viewModel.analyzeUrl(url);
                }
            }
        }
    }

    private String extractUrl(String text) {
        String[] parts = text.split("\\s+");
        for (String part : parts) {
            if (part.startsWith("http") && (part.contains("twitter.com") || part.contains("x.com"))) {
                return part;
            }
        }
        return null;
    }

    private void setupUI() {
        if (binding == null) return;

        if (binding.rvQualities != null) binding.rvQualities.setLayoutManager(new LinearLayoutManager(this));
        if (binding.rvRecentVideos != null) binding.rvRecentVideos.setLayoutManager(new GridLayoutManager(this, 2));
        
        // İndir butonu (Analiz başlatır)
        if (binding.btnAnalyze != null) {
            binding.btnAnalyze.setOnClickListener(v -> {
                String url = binding.etUrl.getText().toString().trim();
                if (!url.isEmpty()) {
                    viewModel.analyzeUrl(url);
                } else {
                    Toast.makeText(this, "Lütfen bir link girin", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Yapıştır butonu
        if (binding.btnPaste != null) {
            binding.btnPaste.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    binding.etUrl.setText(clipboard.getPrimaryClip().getItemAt(0).getText());
                }
            });
        }

        // Seçileni İndir butonu
        if (binding.btnDownloadSelected != null) {
            binding.btnDownloadSelected.setOnClickListener(v -> {
                if (selectedQuality != null) {
                    viewModel.downloadVideo(selectedQuality);
                    Toast.makeText(this, "İndirme başlatıldı", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lütfen önce bir kalite seçin", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Alt Menü - Home
        if (binding.btnNavHome != null) {
            binding.btnNavHome.setOnClickListener(v -> {
                binding.etUrl.requestFocus();
            });
        }

        // Alt Menü - İndirilenler (Video Player Butonu)
        if (binding.btnNavDownloads != null) {
            binding.btnNavDownloads.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, GalleryActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Galeri açılamadı!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Giriş butonu (Twitter Kuşu)
        if (binding.btnLogin != null) {
            binding.btnLogin.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Giriş ekranı açılamadı!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Ayarlar butonu
        if (binding.btnNavSettings != null) {
            binding.btnNavSettings.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Ayarlar açılamadı!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Geçmiş Butonu
        if (binding.btnNavHistory != null) {
            binding.btnNavHistory.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, GalleryActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Geçmiş açılamadı!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void observeViewModel() {
        viewModel.getState().observe(this, state -> {
            binding.layoutLoading.setVisibility(state == MainViewModel.State.ANALYZING ? View.VISIBLE : View.GONE);
            if (state == MainViewModel.State.ERROR) {
                Toast.makeText(this, "Hata: Video analiz edilemedi!", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getVideoInfo().observe(this, info -> {
            if (info != null) {
                showVideoResult(info);
            }
        });
    }

    private void showVideoResult(VideoInfo info) {
        binding.layoutResult.setVisibility(View.VISIBLE);
        
        // Thumbnail
        if (info.getThumbnailUrl() != null && !info.getThumbnailUrl().isEmpty()) {
            Glide.with(this).load(info.getThumbnailUrl()).into(binding.ivThumbnail);
        } else {
            binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Title & Handle
        binding.tvVideoTitle.setText(info.getTitle() != null ? info.getTitle() : "X Video Downloader");
        binding.tvUserHandle.setText(info.getAuthorName() != null ? info.getAuthorName() + " • Upload Dünyası" : "X Video • Upload Dünyası");
        
        // Duration (Varsayılan veya varsa)
        binding.tvDuration.setText(info.getDuration() != null ? info.getDuration() : "12:45 min");

        // User Avatar (Varsayılan veya varsa)
        if (info.getAuthorAvatarUrl() != null && !info.getAuthorAvatarUrl().isEmpty()) {
            Glide.with(this).load(info.getAuthorAvatarUrl()).circleCrop().into(binding.ivUserAvatar);
        }

        qualityAdapter = new QualityAdapter(info.getQualities(), quality -> {
            selectedQuality = quality;
            binding.btnDownloadSelected.setText("📥 SEÇİLENİ İNDİR (" + quality.getFileSizeFormatted() + ")");
        });
        binding.rvQualities.setAdapter(qualityAdapter);
    }

    private void checkClipboard() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            android.content.ClipData clipData = clipboard.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence text = clipData.getItemAt(0).getText();
                if (text != null) {
                    String url = text.toString();
                    if (url.contains("twitter.com") || url.contains("x.com")) {
                        binding.etUrl.setText(url);
                        viewModel.analyzeUrl(url);
                    }
                }
            }
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.POST_NOTIFICATIONS}, 100);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkClipboard();
    }
}
