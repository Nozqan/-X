package com.nebioxkan.xdownloader.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

/**
 * Tema Sistemi — Nebi Özkan X Downloader
 *
 * 5 tema:
 *  1. Black Pearl     (Siyah İnci)
 *  2. Midnight Blue   (Gece Mavisi)
 *  3. Emerald         (Zümrüt)
 *  4. Ruby            (Yakut)
 *  5. Nebi Özkan Special
 *
 * Tema seçildiğinde etkilenen alanlar:
 *  - Ana arka plan
 *  - Kart / panel arka planı
 *  - Birincil aksan rengi (butonlar, toggle, indirme butonu)
 *  - İkincil aksan rengi
 *  - Metin rengi
 *  - Alt gezinti çubuğu arka planı
 */

enum class AppThemeType(val id: String, val displayName: String) {
    BLACK_PEARL("black_pearl", "Siyah İnci"),
    MIDNIGHT_BLUE("midnight_blue", "Gece Mavisi"),
    EMERALD("emerald", "Zümrüt"),
    RUBY("ruby", "Yakut"),
    NEBI_OZKAN_SPECIAL("nebi_ozkan_special", "Nebi Özkan Özel")
}

data class ThemeColors(
    @ColorInt val background: Int,
    @ColorInt val surface: Int,
    @ColorInt val primaryAccent: Int,
    @ColorInt val secondaryAccent: Int,
    @ColorInt val textPrimary: Int,
    @ColorInt val textSecondary: Int,
    @ColorInt val navBar: Int,
    @ColorInt val downloadButton: Int,
    @ColorInt val toggleActive: Int,
    @ColorInt val cardBorder: Int
)

object ThemeManager {

    private const val PREF_NAME = "xdownloader_prefs"
    private const val KEY_THEME = "selected_theme"

    // ─── Renk paleti tanımları (ARGB) ────────────────────────────────────────

    private val BLACK_PEARL_COLORS = ThemeColors(
        background      = 0xFF0A0A0A.toInt(),
        surface         = 0xFF1A1A1A.toInt(),
        primaryAccent   = 0xFFE8E8E8.toInt(),
        secondaryAccent = 0xFF888888.toInt(),
        textPrimary     = 0xFFFFFFFF.toInt(),
        textSecondary   = 0xFFAAAAAA.toInt(),
        navBar          = 0xFF111111.toInt(),
        downloadButton  = 0xFFE8E8E8.toInt(),
        toggleActive    = 0xFFCCCCCC.toInt(),
        cardBorder      = 0xFF2A2A2A.toInt()
    )

    private val MIDNIGHT_BLUE_COLORS = ThemeColors(
        background      = 0xFF0D1117.toInt(),
        surface         = 0xFF161B22.toInt(),
        primaryAccent   = 0xFF58A6FF.toInt(),
        secondaryAccent = 0xFF1F6FEB.toInt(),
        textPrimary     = 0xFFE6EDF3.toInt(),
        textSecondary   = 0xFF8B949E.toInt(),
        navBar          = 0xFF0D1117.toInt(),
        downloadButton  = 0xFF1F6FEB.toInt(),
        toggleActive    = 0xFF58A6FF.toInt(),
        cardBorder      = 0xFF30363D.toInt()
    )

    private val EMERALD_COLORS = ThemeColors(
        background      = 0xFF0A1A0F.toInt(),
        surface         = 0xFF0F2518.toInt(),
        primaryAccent   = 0xFF2ECC71.toInt(),
        secondaryAccent = 0xFF1A7A43.toInt(),
        textPrimary     = 0xFFE8F5E9.toInt(),
        textSecondary   = 0xFF81C784.toInt(),
        navBar          = 0xFF0A1A0F.toInt(),
        downloadButton  = 0xFF27AE60.toInt(),
        toggleActive    = 0xFF2ECC71.toInt(),
        cardBorder      = 0xFF1B5E20.toInt()
    )

    private val RUBY_COLORS = ThemeColors(
        background      = 0xFF1A0A0A.toInt(),
        surface         = 0xFF260F0F.toInt(),
        primaryAccent   = 0xFFE74C3C.toInt(),
        secondaryAccent = 0xFF96281B.toInt(),
        textPrimary     = 0xFFFCE4E4.toInt(),
        textSecondary   = 0xFFEF9A9A.toInt(),
        navBar          = 0xFF1A0A0A.toInt(),
        downloadButton  = 0xFFC0392B.toInt(),
        toggleActive    = 0xFFE74C3C.toInt(),
        cardBorder      = 0xFF4E1010.toInt()
    )

    private val NEBI_OZKAN_SPECIAL_COLORS = ThemeColors(
        background      = 0xFF0A0014.toInt(),
        surface         = 0xFF12001F.toInt(),
        primaryAccent   = 0xFFBB86FC.toInt(),
        secondaryAccent = 0xFF6200EE.toInt(),
        textPrimary     = 0xFFEDE7F6.toInt(),
        textSecondary   = 0xFFCE93D8.toInt(),
        navBar          = 0xFF0A0014.toInt(),
        downloadButton  = 0xFF7C4DFF.toInt(),
        toggleActive    = 0xFFBB86FC.toInt(),
        cardBorder      = 0xFF4A148C.toInt()
    )

    // ─── Public API ──────────────────────────────────────────────────────────

    fun getColors(theme: AppThemeType): ThemeColors = when (theme) {
        AppThemeType.BLACK_PEARL       -> BLACK_PEARL_COLORS
        AppThemeType.MIDNIGHT_BLUE     -> MIDNIGHT_BLUE_COLORS
        AppThemeType.EMERALD           -> EMERALD_COLORS
        AppThemeType.RUBY              -> RUBY_COLORS
        AppThemeType.NEBI_OZKAN_SPECIAL -> NEBI_OZKAN_SPECIAL_COLORS
    }

    fun saveTheme(context: Context, theme: AppThemeType) {
        prefs(context).edit().putString(KEY_THEME, theme.id).apply()
    }

    fun loadTheme(context: Context): AppThemeType {
        val id = prefs(context).getString(KEY_THEME, AppThemeType.BLACK_PEARL.id)
        return AppThemeType.values().firstOrNull { it.id == id } ?: AppThemeType.BLACK_PEARL
    }

    fun getCurrentColors(context: Context): ThemeColors = getColors(loadTheme(context))

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
