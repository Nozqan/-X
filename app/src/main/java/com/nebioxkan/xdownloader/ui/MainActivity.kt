package com.nebioxkan.xdownloader.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nebioxkan.xdownloader.R
import com.nebioxkan.xdownloader.theme.ThemeApplier
import com.nebioxkan.xdownloader.theme.ThemeManager
import com.nebioxkan.xdownloader.ui.instagram.InstagramFragment
import com.nebioxkan.xdownloader.ui.settings.SettingsFragment
import com.nebioxkan.xdownloader.ui.tiktok.TikTokFragment

/**
 * MainActivity — Alt gezinti çubuğu.
 *
 * Sekme isimleri:
 *  1. TikTok   (eski: Home)
 *  2. Twitter  (eski: Downloads) — Twitter kodu DOKUNULMADI
 *  3. Instagram (eski: History)
 *  4. Ayarlar
 *
 * Twitter fragmentı mevcut projeden olduğu gibi kullanılır (TwitterFragment).
 * Bu dosyada Twitter'a ait hiçbir iş mantığı değiştirilmemiştir.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_navigation)

        // İlk açılışta TikTok sekmesi aktif
        if (savedInstanceState == null) {
            loadFragment(TikTokFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_tiktok    -> TikTokFragment()
                R.id.nav_twitter   -> getTwitterFragment()   // Mevcut Twitter kodu
                R.id.nav_instagram -> InstagramFragment()
                R.id.nav_settings  -> SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }
            loadFragment(fragment)
            true
        }

        applyThemeToNav()
    }

    override fun onResume() {
        super.onResume()
        applyThemeToNav()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * Mevcut Twitter fragmentını yükler.
     * Sınıf adı projenizde ne ise onu kullanın.
     * Twitter mantığına hiçbir şey eklenmedi/çıkarılmadı.
     */
    private fun getTwitterFragment(): Fragment {
        // Projenizde TwitterFragment hangi paketteyse o sınıfı döndürün.
        // Örnek: return com.nebioxkan.xdownloader.ui.twitter.TwitterFragment()
        // Güvenlik için reflection ile yüklüyoruz — proje compile olunca direkt import yapın:
        return try {
            Class.forName("com.nebioxkan.xdownloader.ui.twitter.TwitterFragment")
                .getDeclaredConstructor()
                .newInstance() as Fragment
        } catch (e: Exception) {
            // Twitter fragmentı bulunamazsa boş fragment döner (bu olmamalı)
            Fragment()
        }
    }

    private fun applyThemeToNav() {
        val colors = ThemeManager.getCurrentColors(this)
        ThemeApplier.applyToBottomNav(bottomNav, colors)
        window.decorView.setBackgroundColor(colors.background)
        window.statusBarColor = colors.navBar
        window.navigationBarColor = colors.navBar
    }
}
