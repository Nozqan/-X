package com.akrep.xdownloader.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.akrep.xdownloader.R;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchBiometric, switchDarkTheme;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        switchBiometric = findViewById(R.id.switchBiometric);
        switchDarkTheme = findViewById(R.id.switchDarkTheme);

        switchBiometric.setChecked(prefs.getBoolean("biometric_lock", false));
        switchDarkTheme.setChecked(prefs.getBoolean("dark_theme", true));

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("biometric_lock", isChecked).apply();
            String msg = isChecked ? "Parmak izi kilidi aktif" : "Parmak izi kilidi devre dışı";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_theme", isChecked).apply();
            Toast.makeText(this, "Tema değişikliği bir sonraki açılışta uygulanacak", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layoutChangeBackground).setOnClickListener(v -> {
            Toast.makeText(this, "Arka plan değiştirme özelliği 2050 güncellemesiyle gelecek!", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
