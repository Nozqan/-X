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
        binding.rvQualities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentVideos.setLayoutManager(new GridLayoutManager(this, 2));
        
        // İndir butonu (Analiz başlatır)
        binding.btnAnalyze.setOnClickListener(v -> {
            String url = binding.etUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                viewModel.analyzeUrl(url);
            } else {
                Toast.makeText(this, "Lütfen bir link girin", Toast.LENGTH_SHORT).show();
            }
        });

        // Yapıştır butonu
        binding.btnPaste.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                binding.etUrl.setText(clipboard.getPrimaryClip().getItemAt(0).getText());
            }
        });

        // Seçileni İndir butonu
        binding.btnDownloadSelected.setOnClickListener(v -> {
            if (selectedQuality != null) {
                viewModel.downloadVideo(selectedQuality);
                Toast.makeText(this, selectedQuality.getLabel() + " indirme başlatıldı", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lütfen önce bir kalite seçin", Toast.LENGTH_SHORT).show();
            }
        });

        // Alt Menü - Home
        binding.btnNavHome.setOnClickListener(v -> {
            // Zaten Home'dayız, belki bir scroll up yapılabilir
            binding.etUrl.requestFocus();
        });

        // Alt Menü - İndirilenler (Video Player Butonu)
        binding.btnNavDownloads.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
            overridePendingTransition(com.akrep.xdownloader.R.anim.slide_in_right, com.akrep.xdownloader.R.anim.slide_out_left);
        });

        // Giriş butonu (Twitter Kuşu)
        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(com.akrep.xdownloader.R.anim.scale_up, android.R.anim.fade_out);
        });
        
        // Ayarlar butonu
        binding.btnNavSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(com.akrep.xdownloader.R.anim.fade_in, com.akrep.xdownloader.R.anim.slide_out_left);
        });

        // Geçmiş Butonu (Aynı zamanda Galeriye gider)
        binding.btnNavHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
            overridePendingTransition(com.akrep.xdownloader.R.anim.slide_in_right, com.akrep.xdownloader.R.anim.slide_out_left);
        });
        
        // Twitter/X Giriş Butonu - Eski Kuş İkonu (Simülasyon)
        binding.btnLogin.setImageResource(android.R.drawable.ic_menu_send);
        binding.btnLogin.setPadding(12, 12, 12, 12);
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
        if (info.getThumbnailUrl() != null && !info.getThumbnailUrl().isEmpty()) {
            Glide.with(this).load(info.getThumbnailUrl()).into(binding.ivThumbnail);
        } else {
            binding.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
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
