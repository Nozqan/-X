package com.akrep.xdownloader.ui;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.akrep.xdownloader.R;

import java.util.Locale;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private ImageButton btnPlayPause, btnRewind, btnForward, btnClose;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.videoView);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnRewind = findViewById(R.id.btnRewind);
        btnForward = findViewById(R.id.btnForward);
        btnClose = findViewById(R.id.btnClose);
        seekBar = findViewById(R.id.videoSeekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);

        String videoPath = getIntent().getStringExtra("video_path");
        if (videoPath != null) {
            videoView.setVideoURI(Uri.parse(videoPath));
        }

        btnPlayPause.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                videoView.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                updateSeekBar();
            }
        });

        // 10 Saniye Geri
        btnRewind.setOnClickListener(v -> {
            int pos = videoView.getCurrentPosition() - 10000;
            videoView.seekTo(Math.max(pos, 0));
            Toast.makeText(this, "-10sn", Toast.LENGTH_SHORT).show();
        });

        // 30 Saniye İleri
        btnForward.setOnClickListener(v -> {
            int pos = videoView.getCurrentPosition() + 30000;
            videoView.seekTo(Math.min(pos, videoView.getDuration()));
            Toast.makeText(this, "+30sn", Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> finish());

        videoView.setOnPreparedListener(mp -> {
            seekBar.setMax(videoView.getDuration());
            tvTotalTime.setText(formatTime(videoView.getDuration()));
            videoView.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            updateSeekBar();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) videoView.seekTo(progress);
                tvCurrentTime.setText(formatTime(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateSeekBar() {
        if (videoView != null && videoView.isPlaying()) {
            seekBar.setProgress(videoView.getCurrentPosition());
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    private String formatTime(int ms) {
        int seconds = (ms / 1000) % 60;
        int minutes = (ms / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
