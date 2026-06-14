package com.akrep.xdownloader.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akrep.xdownloader.R;
import com.akrep.xdownloader.model.VideoQuality;

import java.util.List;

public class QualityAdapter extends RecyclerView.Adapter<QualityAdapter.ViewHolder> {

    private final List<VideoQuality> qualities;
    private int selectedPosition = 0; // Varsayılan olarak ilk kalite seçili
    private final OnQualitySelectedListener listener;

    public interface OnQualitySelectedListener {
        void onQualitySelected(VideoQuality quality);
    }

    public QualityAdapter(List<VideoQuality> qualities, OnQualitySelectedListener listener) {
        this.qualities = qualities;
        this.listener = listener;
        if (qualities != null && !qualities.isEmpty() && listener != null) {
            listener.onQualitySelected(qualities.get(0));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quality, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoQuality quality = qualities.get(position);
        String label = quality.getLabel();
        
        // Görseldeki format: 1080p (FHD) / Mp4
        String displayLabel = label;
        if (label.contains("1080")) displayLabel = "1080p (FHD) / Mp4";
        else if (label.contains("720")) displayLabel = "720p (HD) / Mp4";
        else if (label.contains("480")) displayLabel = "480p / Mp4";
        else if (label.contains("360")) displayLabel = "360p / Mp4";
        
        holder.tvBadge.setText(displayLabel);
        holder.tvSize.setText(quality.getFileSizeFormatted());
        holder.tvLabel.setText(getQualityDescription(label));
        
        holder.rbSelected.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onQualitySelected(quality);
            }
        });
    }

    private String getQualityDescription(String label) {
        if (label.contains("1080")) return "Yüksek Kalite";
        if (label.contains("720")) return "İyi Kalite";
        if (label.contains("480")) return "Standart Kalite";
        return "Temel Kalite";
    }

    public VideoQuality getSelectedQuality() {
        if (qualities != null && selectedPosition < qualities.size()) {
            return qualities.get(selectedPosition);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return qualities != null ? qualities.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbSelected;
        TextView tvBadge, tvSize, tvLabel;
        
        ViewHolder(View v) {
            super(v);
            rbSelected = v.findViewById(R.id.rbSelected);
            tvBadge = v.findViewById(R.id.tvQualityBadge);
            tvSize = v.findViewById(R.id.tvQualitySize);
            tvLabel = v.findViewById(R.id.tvQualityLabel);
        }
    }
}
