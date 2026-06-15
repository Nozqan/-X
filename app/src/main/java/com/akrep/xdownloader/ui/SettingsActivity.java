package com.akrep.xdownloader.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.akrep.xdownloader.R;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("AkrepPrefs", MODE_PRIVATE);

        setupSettings();
    }

    private void setupSettings() {
        // 1. TEMA SEÇİMİ (SİYAH / BEYAZ)
        Switch switchTheme = findViewById(R.id.switchTheme);
        if (switchTheme != null) {
            switchTheme.setChecked(prefs.getBoolean("dark_mode", true));
            switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("dark_mode", isChecked).apply();
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                Toast.makeText(this, "Tema Değiştirildi", Toast.LENGTH_SHORT).show();
            });
        }

        // 2. PARMAK İZİ
        Switch switchFingerprint = findViewById(R.id.switchFingerprint);
        if (switchFingerprint != null) {
            switchFingerprint.setChecked(prefs.getBoolean("fingerprint_enabled", false));
            switchFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("fingerprint_enabled", isChecked).apply();
                Toast.makeText(this, "Güvenlik Ayarı Güncellendi", Toast.LENGTH_SHORT).show();
            });
        }

        // 3. ARKA PLAN SEÇİCİ (BASİT LİSTE)
        findViewById(R.id.layoutChangeBackground).setOnClickListener(v -> {
            String[] bgs = {"Siyah İnci", "Gece Mavisi", "Zümrüt Yeşili", "Yakut Kırmızısı", "Nebi Özkan Özel"};
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Arka Plan Seçin")
                .setItems(bgs, (dialog, which) -> {
                    prefs.edit().putString("selected_bg", bgs[which]).apply();
                    Toast.makeText(this, bgs[which] + " Uygulandı", Toast.LENGTH_SHORT).show();
                })
                .show();
        });

        // 4. GERİ BUTONU
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
