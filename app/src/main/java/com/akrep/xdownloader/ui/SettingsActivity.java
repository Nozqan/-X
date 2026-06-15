package com.akrep.xdownloader.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.akrep.xdownloader.R;
import java.util.concurrent.Executor;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchBiometric, switchDarkTheme;
    private SharedPreferences prefs;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Kurucu Bilgileri
        TextView tvFounder = findViewById(R.id.tvFounderName);
        if (tvFounder != null) tvFounder.setText("Nebi Özkan");
        
        TextView tvAppPurpose = findViewById(R.id.tvAppPurpose);
        if (tvAppPurpose != null) tvAppPurpose.setText("Twitter videolarını ücretsiz ve tamamen reklamsız indirmek için tasarlanmıştır.");

        switchBiometric = findViewById(R.id.switchBiometric);
        switchDarkTheme = findViewById(R.id.switchDarkTheme);

        switchBiometric.setChecked(prefs.getBoolean("biometric_lock", false));
        switchDarkTheme.setChecked(prefs.getBoolean("dark_theme", true));

        // Biyometrik Hazırlık
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(SettingsActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                switchBiometric.setChecked(false);
                Toast.makeText(getApplicationContext(), "Hata: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                prefs.edit().putBoolean("biometric_lock", true).apply();
                Toast.makeText(getApplicationContext(), "Parmak izi kilidi aktif edildi!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Kimlik doğrulanamadı", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biyometrik Doğrulama")
                .setSubtitle("Parmak izinizi kullanarak kilidi aktifleştirin")
                .setNegativeButtonText("İptal")
                .build();

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !prefs.getBoolean("biometric_lock", false)) {
                biometricPrompt.authenticate(promptInfo);
            } else {
                prefs.edit().putBoolean("biometric_lock", isChecked).apply();
            }
        });

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_theme", isChecked).apply();
            String themeName = isChecked ? "AMOLED Siyah" : "Kar Beyazı";
            Toast.makeText(this, themeName + " teması seçildi. Lütfen uygulamayı yeniden başlatın.", Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.layoutChangeBackground).setOnClickListener(v -> {
            String[] backgrounds = {"Lüks Altın 2K", "Mat Gece 2K", "Okyanus Derinliği 2K", "Nebi Özkan Özel 2K", "2050 Vizyonu 2K"};
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("2K Lüks Arka Plan Seçin");
            builder.setItems(backgrounds, (dialog, which) -> {
                prefs.edit().putString("custom_bg", backgrounds[which]).apply();
                Toast.makeText(this, backgrounds[which] + " uygulandı! (100MB+ Paket İçeriği)", Toast.LENGTH_LONG).show();
            });
            builder.show();
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
