package com.nebioxkan.xdownloader.ui.instagram

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nebioxkan.xdownloader.R
import com.nebioxkan.xdownloader.data.Platform
import com.nebioxkan.xdownloader.data.VideoDatabase
import com.nebioxkan.xdownloader.data.VideoDownloadRepository
import com.nebioxkan.xdownloader.data.VideoHistoryEntity
import com.nebioxkan.xdownloader.theme.ThemeApplier
import com.nebioxkan.xdownloader.theme.ThemeManager
import com.nebioxkan.xdownloader.ui.common.VideoGridAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * InstagramFragment — History butonunun yerini aldı.
 *
 * TikTok ile birebir aynı mantık:
 *  • Otomatik URL yapıştırma
 *  • Watermarksız + en yüksek kalite
 *  • Grid geçmiş
 *  • Tam tema uyumu
 */
class InstagramFragment : Fragment() {

    private lateinit var repo: VideoDownloadRepository
    private lateinit var dao: com.nebioxkan.xdownloader.data.VideoHistoryDao
    private lateinit var adapter: VideoGridAdapter

    private lateinit var rootLayout: android.widget.LinearLayout
    private lateinit var urlInput: EditText
    private lateinit var downloadBtn: MaterialButton
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryBtn: ImageButton

    private var isDownloading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_instagram, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repo = VideoDownloadRepository(requireContext())
        dao  = VideoDatabase.getInstance(requireContext()).videoHistoryDao()

        bindViews(view)
        setupRecycler()
        observeHistory()
        setupDownloadButton()
        applyCurrentTheme()
    }

    override fun onResume() {
        super.onResume()
        autopasteFromClipboard()
        applyCurrentTheme()
    }

    private fun bindViews(view: View) {
        rootLayout     = view.findViewById(R.id.instagram_root)
        urlInput       = view.findViewById(R.id.instagram_url_input)
        downloadBtn    = view.findViewById(R.id.instagram_download_btn)
        statusText     = view.findViewById(R.id.instagram_status_text)
        progressBar    = view.findViewById(R.id.instagram_progress)
        recyclerView   = view.findViewById(R.id.instagram_history_grid)
        historyTitle   = view.findViewById(R.id.instagram_history_title)
        clearHistoryBtn = view.findViewById(R.id.instagram_clear_history)
    }

    private fun setupRecycler() {
        adapter = VideoGridAdapter(
            onItemClick     = { item -> openVideo(item) },
            onItemLongClick = { item -> confirmDelete(item) }
        )
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = adapter

        clearHistoryBtn.setOnClickListener {
            lifecycleScope.launch { dao.clearPlatform(Platform.INSTAGRAM.name) }
        }
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            dao.getByPlatform(Platform.INSTAGRAM.name).collectLatest { list ->
                adapter.submitList(list)
                historyTitle.visibility  = if (list.isEmpty()) View.GONE else View.VISIBLE
                clearHistoryBtn.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun autopasteFromClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip ?: return
        if (clip.itemCount == 0) return
        val text = clip.getItemAt(0).coerceToText(requireContext()).toString()
        if (isInstagramUrl(text) && urlInput.text.toString() != text) {
            urlInput.setText(text)
            urlInput.setSelection(text.length)
            showStatus("Instagram linki yapıştırıldı ✓", success = true)
        }
    }

    private fun isInstagramUrl(text: String): Boolean =
        text.contains("instagram.com") || text.contains("instagr.am")

    private fun setupDownloadButton() {
        downloadBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                showStatus("Lütfen bir Instagram linki girin.", success = false)
                return@setOnClickListener
            }
            if (!isInstagramUrl(url)) {
                showStatus("Geçersiz Instagram linki.", success = false)
                return@setOnClickListener
            }
            if (isDownloading) return@setOnClickListener
            startDownload(url)
        }
    }

    private fun startDownload(url: String) {
        isDownloading = true
        progressBar.visibility = View.VISIBLE
        downloadBtn.isEnabled  = false
        showStatus("İndiriliyor — en yüksek kalite, watermark kaldırılıyor…", success = true)

        lifecycleScope.launch {
            val result = repo.download(url, Platform.INSTAGRAM)
            progressBar.visibility = View.GONE
            downloadBtn.isEnabled  = true
            isDownloading = false

            if (result.success && result.filePath != null) {
                dao.insert(
                    VideoHistoryEntity(
                        platform     = Platform.INSTAGRAM.name,
                        filePath     = result.filePath,
                        thumbnailUrl = result.thumbnailUrl
                    )
                )
                urlInput.setText("")
                showStatus("✓ Başarıyla indirildi!", success = true)
            } else {
                showStatus("Hata: ${result.errorMessage}", success = false)
            }
        }
    }

    private fun showStatus(message: String, success: Boolean) {
        statusText.text       = message
        statusText.visibility = View.VISIBLE
        val colors = ThemeManager.getCurrentColors(requireContext())
        statusText.setTextColor(if (success) colors.primaryAccent else 0xFFFF5252.toInt())
    }

    private fun openVideo(item: VideoHistoryEntity) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(android.net.Uri.parse(item.filePath), "video/mp4")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    private fun confirmDelete(item: VideoHistoryEntity) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Geçmişten sil")
            .setMessage("Bu video geçmişten kaldırılsın mı?")
            .setPositiveButton("Sil") { _, _ ->
                lifecycleScope.launch { dao.delete(item) }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun applyCurrentTheme() {
        val colors = ThemeManager.getCurrentColors(requireContext())
        ThemeApplier.applyToRoot(rootLayout, colors)
        ThemeApplier.applyToDownloadButton(downloadBtn, colors)
        urlInput.setBackgroundColor(colors.surface)
        urlInput.setTextColor(colors.textPrimary)
        urlInput.setHintTextColor(colors.textSecondary)
        historyTitle.setTextColor(colors.textPrimary)
    }
}
