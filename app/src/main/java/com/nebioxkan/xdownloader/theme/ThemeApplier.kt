package com.nebioxkan.xdownloader.theme

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * ThemeApplier — Seçili temayı tüm UI elementlerine uygular.
 * Her Activity/Fragment onResume()'da applyToRoot() çağırmalı.
 */
object ThemeApplier {

    fun applyToRoot(root: View, colors: ThemeColors) {
        root.setBackgroundColor(colors.background)
        applyToChildren(root, colors)
    }

    fun applyToBottomNav(nav: BottomNavigationView, colors: ThemeColors) {
        nav.setBackgroundColor(colors.navBar)
        nav.itemIconTintList = android.content.res.ColorStateList.valueOf(colors.primaryAccent)
        nav.itemTextColor   = android.content.res.ColorStateList.valueOf(colors.primaryAccent)
    }

    fun applyToDownloadButton(button: MaterialButton, colors: ThemeColors) {
        button.setBackgroundColor(colors.downloadButton)
        button.setTextColor(colors.background)
    }

    fun applyToCardView(card: CardView, colors: ThemeColors) {
        card.setCardBackgroundColor(colors.surface)
        card.radius = 16f
    }

    fun applyToToggle(switch: Switch, colors: ThemeColors) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val thumbColors = intArrayOf(colors.toggleActive, colors.secondaryAccent)
        val trackColors = intArrayOf(
            blendColors(colors.toggleActive, colors.background, 0.4f),
            blendColors(colors.secondaryAccent, colors.background, 0.4f)
        )
        switch.thumbTintList = android.content.res.ColorStateList(states, thumbColors)
        switch.trackTintList = android.content.res.ColorStateList(states, trackColors)
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private fun applyToChildren(view: View, colors: ThemeColors) {
        when (view) {
            is ConstraintLayout -> {
                view.setBackgroundColor(colors.background)
                for (i in 0 until view.childCount) applyToChildren(view.getChildAt(i), colors)
            }
            is CardView -> applyToCardView(view, colors)
            is MaterialButton -> applyToDownloadButton(view, colors)
            is Button -> {
                view.setBackgroundColor(colors.primaryAccent)
                view.setTextColor(colors.background)
            }
            is Switch -> applyToToggle(view, colors)
            is TextView -> {
                if (view.currentTextColor == Color.WHITE || view.currentTextColor == Color.BLACK) {
                    view.setTextColor(colors.textPrimary)
                }
            }
            is android.view.ViewGroup -> {
                for (i in 0 until view.childCount) applyToChildren(view.getChildAt(i), colors)
            }
        }
    }

    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val r = Color.red(color1)   * ratio + Color.red(color2)   * (1 - ratio)
        val g = Color.green(color1) * ratio + Color.green(color2) * (1 - ratio)
        val b = Color.blue(color1)  * ratio + Color.blue(color2)  * (1 - ratio)
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }
}
