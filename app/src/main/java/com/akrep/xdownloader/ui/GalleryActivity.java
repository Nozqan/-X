package com.akrep.xdownloader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akrep.xdownloader.R;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView rvGallery;
    private final List<File> videoFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("İndirilen Videolar");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        rvGallery = findViewById(R.id.rvGallery);
        if (rvGallery != null) {
            rvGallery.setLayoutManager(new GridLayoutManager(this, 2));
            loadVideos();
        }
    }

    private void loadVideos() {
        videoFiles.clear();
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AkrepIndirici");
        if (dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".mp4"));
            if (files != null) {
                for (File f : files) {
                    if (f.length() > 0) {
                        videoFiles.add(f);
                    }
                }
            }
        }
        if (rvGallery != null) {
            rvGallery.setAdapter(new GalleryAdapter());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            File file = videoFiles.get(position);
            holder.tvName.setText(file.getName());
            
            // Glide ile video kapağını yükle (Çökme riskini azaltır)
            Glide.with(GalleryActivity.this)
                    .load(file)
                    .centerCrop()
                    .placeholder(R.drawable.bg_gradient)
                    .into(holder.ivThumb);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(GalleryActivity.this, VideoPlayerActivity.class);
                intent.putExtra("video_path", file.getAbsolutePath());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return videoFiles.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ImageView ivThumb;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvVideoName);
                ivThumb = v.findViewById(R.id.ivVideoThumb);
            }
        }
    }
}
