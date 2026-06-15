package com.nebioxkan.xdownloader.ui.settings

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.nebioxkan.xdownloader.R
import com.nebioxkan.xdownloader.theme.AppThemeType
import com.nebioxkan.xdownloader.theme.ThemeApplier
import com.nebioxkan.xdownloader.theme.ThemeManager

/**
 * SettingsFragment — Güncellenmiş ayarlar ekranı.
 *
 * Değişenler:
 *  • Ayarlar ikonu: yeni modern çizgi ikon (XML drawable)
 *  • "2K Arka Plan" → "Temalar" (5 tema + tam UI rengi değişimi)
 *  • Uygulama amacı metni güncellendi
 *  • Kurucu adı: Nebi Özkan
 *
 * Dokunulmayan:
 *  • Karanlık/Aydınlık toggle — aynı
 *  • Parmak izi toggle — aynı
 */
class SettingsFragment : Fragment() {

    // ─── Views ───────────────────────────────────────────────────────────────

    private lateinit var rootLayout: LinearLayout

    // Uygulama bilgisi
    private lateinit var creatorNameText: TextView
    private lateinit var appPurposeText: TextView

    // Mevcut (dokunulmayan) togglelar
    private lateinit var darkModeSwitch: Switch
    private lateinit var fingerprintSwitch: Switch

    // Tema bölümü (yenilendi)
    private lateinit var themeSectionCard: CardView
    private lateinit var themeTitle: TextView

    private lateinit var btnBlackPearl: MaterialButton
    private lateinit var btnMidnightBlue: MaterialButton
    private lateinit var btnEmerald: MaterialButton
    private lateinit var btnRuby: MaterialButton
    private lateinit var btnNebiOzkan: MaterialButton

    private lateinit var currentThemeLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setStaticTexts()
        setupThemeButtons()
        applyCurrentTheme()
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
    }

    // ─── Binding ─────────────────────────────────────────────────────────────

    private fun bindViews(view: View) {
        rootLayout        = view.findViewById(R.id.settings_root)
        creatorNameText   = view.findViewById(R.id.settings_creator_name)
        appPurposeText    = view.findViewById(R.id.settings_app_purpose)
        darkModeSwitch    = view.findViewById(R.id.settings_dark_mode_switch)
        fingerprintSwitch = view.findViewById(R.id.settings_fingerprint_switch)
        themeSectionCard  = view.findViewById(R.id.settings_theme_card)
        themeTitle        = view.findViewById(R.id.settings_theme_title)
        btnBlackPearl     = view.findViewById(R.id.theme_btn_black_pearl)
        btnMidnightBlue   = view.findViewById(R.id.theme_btn_midnight_blue)
        btnEmerald        = view.findViewById(R.id.theme_btn_emerald)
        btnRuby           = view.findViewById(R.id.theme_btn_ruby)
        btnNebiOzkan      = view.findViewById(R.id.theme_btn_nebi_ozkan)
        currentThemeLabel = view.findViewById(R.id.settings_current_theme_label)
    }

    // ─── Statik metinler ─────────────────────────────────────────────────────

    private fun setStaticTexts() {
        // Kurucu adı — her zaman en üstte
        creatorNameText.text = "Nebi Özkan"

        // Uygulama amacı (eski: "Twitter'dan ücretsiz video indirmek için yapıldı")
        appPurposeText.text = "Ücretsiz ve reklamsız video indirici"

        // Tema bölüm başlığı (eski: "2K Arka Plan Resimleri Teması")
        themeTitle.text = "Temalar"

        // Buton etiketleri
        btnBlackPearl.text   = "Siyah İnci"
        btnMidnightBlue.text = "Gece Mavisi"
        btnEmerald.text      = "Zümrüt"
        btnRuby.text         = "Yakut"
        btnNebiOzkan.text    = "Nebi Özkan Özel"
    }

    // ─── Tema butonları ───────────────────────────────────────────────────────

    private fun setupThemeButtons() {
        btnBlackPearl.setOnClickListener   { selectTheme(AppThemeType.BLACK_PEARL) }
        btnMidnightBlue.setOnClickListener { selectTheme(AppThemeType.MIDNIGHT_BLUE) }
        btnEmerald.setOnClickListener      { selectTheme(AppThemeType.EMERALD) }
        btnRuby.setOnClickListener         { selectTheme(AppThemeType.RUBY) }
        btnNebiOzkan.setOnClickListener    { selectTheme(AppThemeType.NEBI_OZKAN_SPECIAL) }

        updateCurrentThemeLabel()
    }

    private fun selectTheme(theme: AppThemeType) {
        ThemeManager.saveTheme(requireContext(), theme)
        applyCurrentTheme()
        updateCurrentThemeLabel()

        // Activity'yi yenile — tüm fragmentlar yeni tema ile boyansın
        requireActivity().recreate()
    }

    private fun updateCurrentThemeLabel() {
        val current = ThemeManager.loadTheme(requireContext())
        currentThemeLabel.text = "Aktif tema: ${current.displayName}"
    }

    // ─── Tema uygulama ────────────────────────────────────────────────────────

    private fun applyCurrentTheme() {
        val colors = ThemeManager.getCurrentColors(requireContext())

        ThemeApplier.applyToRoot(rootLayout, colors)
        ThemeApplier.applyToCardView(themeSectionCard, colors)
        ThemeApplier.applyToToggle(darkModeSwitch, colors)
        ThemeApplier.applyToToggle(fingerprintSwitch, colors)

        creatorNameText.setTextColor(colors.primaryAccent)
        appPurposeText.setTextColor(colors.textSecondary)
        themeTitle.setTextColor(colors.textPrimary)
        currentThemeLabel.setTextColor(colors.textSecondary)

        // Tema butonlarını kendi renkleriyle boya
        applyThemeButtonColor(btnBlackPearl,   AppThemeType.BLACK_PEARL)
        applyThemeButtonColor(btnMidnightBlue, AppThemeType.MIDNIGHT_BLUE)
        applyThemeButtonColor(btnEmerald,      AppThemeType.EMERALD)
        applyThemeButtonColor(btnRuby,         AppThemeType.RUBY)
        applyThemeButtonColor(btnNebiOzkan,    AppThemeType.NEBI_OZKAN_SPECIAL)
    }

    private fun applyThemeButtonColor(button: MaterialButton, theme: AppThemeType) {
        val themeColors  = ThemeManager.getColors(theme)
        val activeTheme  = ThemeManager.loadTheme(requireContext())
        val isActive     = theme == activeTheme

        button.setBackgroundColor(themeColors.primaryAccent)
        button.setTextColor(themeColors.background)
        button.strokeWidth = if (isActive) 4 else 0
        if (isActive) button.strokeColor =
            android.content.res.ColorStateList.valueOf(0xFFFFFFFF.toInt())
    }
}
