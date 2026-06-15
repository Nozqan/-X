package com.nebioxkan.xdownloader.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * VideoDownloadRepository
 *
 * TikTok ve Instagram için ortak indirme mantığı.
 *  - Watermark yok (no-watermark API endpoint kullanılır)
 *  - Kullanıcı adı / paylaşan kişi bilgisi kaldırılır
 *  - Her zaman en yüksek kalite
 *  - Twitter koduna DOKUNULMAZ — o ayrı repository'de
 */

enum class Platform { TIKTOK, INSTAGRAM }

data class DownloadResult(
    val success: Boolean,
    val filePath: String? = null,
    val errorMessage: String? = null,
    val thumbnailUrl: String? = null
)

data class VideoHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val platform: Platform,
    val filePath: String,
    val thumbnailUrl: String?,
    val downloadedAt: Long = System.currentTimeMillis()
)

class VideoDownloadRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // ─── Ana indirme fonksiyonu ───────────────────────────────────────────────

    suspend fun download(url: String, platform: Platform): DownloadResult =
        withContext(Dispatchers.IO) {
            try {
                val resolved = resolveDirectUrl(url, platform)
                    ?: return@withContext DownloadResult(false, errorMessage = "Video URL çözümlenemedi.")

                val savedPath = saveVideoToGallery(resolved.videoUrl, platform, resolved.thumbnailUrl)
                DownloadResult(
                    success = true,
                    filePath = savedPath,
                    thumbnailUrl = resolved.thumbnailUrl
                )
            } catch (e: Exception) {
                DownloadResult(false, errorMessage = e.message ?: "Bilinmeyen hata")
            }
        }

    // ─── Platform bazlı URL çözümleme ────────────────────────────────────────

    private data class ResolvedVideo(val videoUrl: String, val thumbnailUrl: String?)

    private fun resolveDirectUrl(url: String, platform: Platform): ResolvedVideo? {
        return when (platform) {
            Platform.TIKTOK    -> resolveTikTok(url)
            Platform.INSTAGRAM -> resolveInstagram(url)
        }
    }

    /**
     * TikTok — watermarksız en yüksek kalite.
     * tikwm.com API (ücretsiz, watermark kaldırır, HD döner).
     * Fallback: ssstik.io API
     */
    private fun resolveTikTok(url: String): ResolvedVideo? {
        // Yöntem 1: tikwm API
        val apiUrl = "https://api.tikwm.com/video/info?url=${encodeUrl(url)}&hd=1"
        val req = Request.Builder().url(apiUrl).build()
        val resp = client.newCall(req).execute()
        if (resp.isSuccessful) {
            val body = resp.body?.string() ?: return null
            val json = JSONObject(body)
            if (json.optInt("code") == 0) {
                val data = json.getJSONObject("data")
                // play_addr_h264 → no watermark HD, hdplay → 1080p
                val hdUrl = data.optString("hdplay")
                    .ifEmpty { data.optString("play") }  // fallback to SD no-watermark
                val thumb = data.optString("cover")
                if (hdUrl.isNotEmpty()) return ResolvedVideo(hdUrl, thumb.ifEmpty { null })
            }
        }

        // Yöntem 2: ssstik fallback (POST form)
        val sssUrl = "https://ssstik.io/abc?url=dl"
        val formBody = okhttp3.FormBody.Builder()
            .add("id", url)
            .add("locale", "en")
            .add("tt", "")
            .build()
        val sssReq = Request.Builder().url(sssUrl).post(formBody).build()
        val sssResp = client.newCall(sssReq).execute()
        if (sssResp.isSuccessful) {
            val html = sssResp.body?.string() ?: return null
            val videoRegex = Regex("""href="(https://[^"]+\.mp4[^"]*)" class="without_watermark""")
            val match = videoRegex.find(html)
            if (match != null) return ResolvedVideo(match.groupValues[1], null)
        }

        return null
    }

    /**
     * Instagram — watermarksız en yüksek kalite.
     * Instagram oEmbed + SnapSave API zinciri.
     */
    private fun resolveInstagram(url: String): ResolvedVideo? {
        // Yöntem 1: snapinsta API
        val apiUrl = "https://api.snapinsta.app/v2/info?url=${encodeUrl(url)}"
        val req = Request.Builder()
            .url(apiUrl)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        val resp = client.newCall(req).execute()
        if (resp.isSuccessful) {
            val body = resp.body?.string() ?: return null
            runCatching {
                val json = JSONObject(body)
                val medias = json.getJSONArray("data")
                // En yüksek kaliteyi seç (son index genellikle en yüksek)
                var bestUrl = ""
                var bestSize = 0L
                var thumb: String? = null
                for (i in 0 until medias.length()) {
                    val item = medias.getJSONObject(i)
                    val size = item.optLong("size", 0)
                    val vUrl = item.optString("url", "")
                    if (size > bestSize && vUrl.isNotEmpty()) {
                        bestSize = size
                        bestUrl  = vUrl
                    }
                    if (thumb == null) thumb = item.optString("thumbnail").ifEmpty { null }
                }
                if (bestUrl.isNotEmpty()) return ResolvedVideo(bestUrl, thumb)
            }
        }

        // Yöntem 2: instasave fallback
        val fb2 = okhttp3.FormBody.Builder()
            .add("url", url)
            .build()
        val req2 = Request.Builder()
            .url("https://v3.saveinsta.app/api/ajaxSearch")
            .post(fb2)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        val resp2 = client.newCall(req2).execute()
        if (resp2.isSuccessful) {
            val body2 = resp2.body?.string() ?: return null
            runCatching {
                val json2 = JSONObject(body2)
                val html = json2.optString("data", "")
                val regex = Regex("""href="(https://[^"]+\.mp4[^"]*)"""")
                val m = regex.find(html)
                if (m != null) return ResolvedVideo(m.groupValues[1], null)
            }
        }

        return null
    }

    // ─── Galeriye kaydetme (MediaStore) ──────────────────────────────────────

    private fun saveVideoToGallery(videoUrl: String, platform: Platform, thumbnailUrl: String?): String {
        val request = Request.Builder().url(videoUrl).build()
        val response = client.newCall(request).execute()
        val bytes = response.body?.bytes() ?: throw Exception("Video indirilemedi.")

        val fileName = "${platform.name.lowercase()}_${System.currentTimeMillis()}.mp4"
        val folderName = "X Downloader/${platform.name}"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/$folderName")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw Exception("MediaStore URI oluşturulamadı.")
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            uri.toString()
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), folderName)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        }
    }

    private fun encodeUrl(url: String): String = java.net.URLEncoder.encode(url, "UTF-8")
}
