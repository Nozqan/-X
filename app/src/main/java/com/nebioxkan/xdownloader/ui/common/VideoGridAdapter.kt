package com.nebioxkan.xdownloader.ui.common

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.nebioxkan.xdownloader.R
import com.nebioxkan.xdownloader.data.VideoHistoryEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * VideoGridAdapter — TikTok ve Instagram geçmiş ızgarası için ortak adapter.
 * Her kart: thumbnail + indirme tarihi.
 */
class VideoGridAdapter(
    private val onItemClick: (VideoHistoryEntity) -> Unit,
    private val onItemLongClick: (VideoHistoryEntity) -> Unit
) : ListAdapter<VideoHistoryEntity, VideoGridAdapter.VideoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_grid, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val thumbnail: ImageView = itemView.findViewById(R.id.grid_item_thumbnail)
        private val dateText: TextView   = itemView.findViewById(R.id.grid_item_date)
        private val playIcon: ImageView  = itemView.findViewById(R.id.grid_item_play_icon)

        fun bind(item: VideoHistoryEntity) {
            // Thumbnail: önce thumbnailUrl, yoksa video dosyasından
            when {
                !item.thumbnailUrl.isNullOrEmpty() -> {
                    Glide.with(itemView.context)
                        .load(item.thumbnailUrl)
                        .transform(CenterCrop(), RoundedCorners(12))
                        .placeholder(R.drawable.ic_video_placeholder)
                        .into(thumbnail)
                }
                item.filePath.startsWith("content://") -> {
                    Glide.with(itemView.context)
                        .load(Uri.parse(item.filePath))
                        .transform(CenterCrop(), RoundedCorners(12))
                        .placeholder(R.drawable.ic_video_placeholder)
                        .into(thumbnail)
                }
                else -> {
                    Glide.with(itemView.context)
                        .load(File(item.filePath))
                        .transform(CenterCrop(), RoundedCorners(12))
                        .placeholder(R.drawable.ic_video_placeholder)
                        .into(thumbnail)
                }
            }

            // Tarih
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            dateText.text = sdf.format(Date(item.downloadedAt))

            // Play icon her zaman görünür
            playIcon.visibility = View.VISIBLE

            // Tıklama
            itemView.setOnClickListener { onItemClick(item) }
            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<VideoHistoryEntity>() {
            override fun areItemsTheSame(old: VideoHistoryEntity, new: VideoHistoryEntity) =
                old.id == new.id
            override fun areContentsTheSame(old: VideoHistoryEntity, new: VideoHistoryEntity) =
                old == new
        }
    }
}
