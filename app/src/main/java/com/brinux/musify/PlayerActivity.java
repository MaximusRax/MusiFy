package com.brinux.musify;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.brinux.musify.databinding.ActivityPlayerBinding;
import com.bumptech.glide.Glide;
import com.frolo.waveformseekbar.WaveformSeekBar;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private Handler handler = new Handler();
    private List<Song> songList = new ArrayList<>();
    private List<Song> shuffleList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ActivityPlayerBinding binding;
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                long currentPosition = player.getCurrentPosition();
                long duration = player.getDuration();
                if (duration > 0) {
                    float progressPercent = ((float) currentPosition / duration);
                    binding.waveformSeekBar.setProgressInPercentage(progressPercent);
                    binding.textElapsed.setText(formateTime((int) (currentPosition / 1000)));
                    binding.textDuration.setText(formateTime((int) (duration / 1000)));
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        songList = getIntent().getParcelableArrayListExtra("songList");
        currentIndex = getIntent().getIntExtra("position", 0);

        if (songList == null || songList.isEmpty()) {
            Toast.makeText(this, "!No Songs Found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        shuffleList = new ArrayList<>(songList);
        binding.waveformSeekBar.setWaveform(createWaveForm());
        initPlayerWithSong(currentIndex);
        setupControl();
        binding.backBtn.setOnClickListener(View -> finish());
    }

    private void setupControl() {
        binding.buttonPlayPause.setOnClickListener(View -> togglePlayPause());
        binding.buttonNext.setOnClickListener(View -> playNext());
        binding.buttonPrev.setOnClickListener(View -> playPrev());
        binding.buttonShuffle.setOnClickListener(View -> toggleShuffle());
        binding.buttonRepeat.setOnClickListener(View -> toggleRepeat());
        binding.waveformSeekBar.setCallback(new WaveformSeekBar.Callback() {
            @Override
            public void onProgressChanged(WaveformSeekBar seekBar, float percent, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    long seekPos = (long) (percent * duration);
                    player.seekTo(seekPos);
                    binding.textElapsed.setText(formateTime((int) (seekPos / 1000)));
                }
            }

            @Override
            public void onStartTrackingTouch(WaveformSeekBar seekBar) {
                handler.removeCallbacks(updateRunnable);
            }

            @Override
            public void onStopTrackingTouch(WaveformSeekBar seekBar) {
                handler.postDelayed(updateRunnable, 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        if (isRepeat)
            binding.buttonRepeat.setColorFilter(getResources().getColor(R.color.purple));
        else binding.buttonRepeat.setColorFilter(null);
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        if (isShuffle) {
            Collections.shuffle(shuffleList);
            binding.buttonShuffle.setColorFilter(getResources().getColor(R.color.purple));
        } else {
            shuffleList = new ArrayList<>(songList);
            binding.buttonShuffle.clearColorFilter();
        }
        initPlayerWithSong(currentIndex);
    }

    private void playPrev() {
        int listSize = (currentIndex - 1) % (isShuffle ? shuffleList.size() : songList.size());
        currentIndex = (currentIndex - 1 + listSize) % listSize;
        initPlayerWithSong(currentIndex);
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
            handler.removeCallbacks(updateRunnable);
        } else {
            player.play();
            handler.postDelayed(updateRunnable, 0);
        }
        updatePlayPauseButton();
    }

    private void initPlayerWithSong(int index) {
        Song song = isShuffle ? shuffleList.get(index) : songList.get(index);

        if (player != null) player.release();
        player = new ExoPlayer.Builder(this).build();
        player.setRepeatMode(isRepeat ? player.REPEAT_MODE_ONE : player.REPEAT_MODE_OFF);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
//                Player.Listener.super.onPlaybackStateChanged(playbackState);
                updatePlayPauseButton();
                if (playbackState == Player.STATE_READY) {
                    binding.textDuration.setText(formateTime((int) (player.getDuration() / 1000)));
                    handler.postDelayed(updateRunnable, 0);
                } else if (playbackState == Player.STATE_ENDED) {
                    playNext();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(PlayerActivity.this, "Error Playing media" + error.getMessage(), Toast.LENGTH_SHORT);
            }
        });
        player.setMediaItem(MediaItem.fromUri(song.data));
        player.prepare();
        player.play();
        updatePlayPauseButton();
        updateUI(song);
    }

    private void updateUI(Song song) {
        binding.textTitle.setText(song.title != null ? song.title : "");
        binding.textArtist.setText(song.artist != null ? song.artist : "");
        setTitle(song.title);

        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), song.albumId);
        if (hasAlbumArt(albumArtUri)) {
            Glide.with(this)
                    .asBitmap()
                    .load(albumArtUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_music_note_24)
                    .error(R.drawable.ic_music_note_24)
                    .into(binding.imageAlbumArtPlayer);
            Glide.with(this)
                    .asBitmap()
                    .load(albumArtUri)
                    .apply(bitmapTransform(new BlurTransformation(25, 3)))
//                    .circleCrop()
                    .placeholder(R.drawable.ic_music_note_24)
                    .error(R.drawable.ic_music_note_24)
                    .into(binding.bgAlbumArt);
        } else {
            binding.imageAlbumArtPlayer.setImageResource(R.drawable.ic_music_note_24);
            binding.bgAlbumArt.setImageResource(R.drawable.ic_music_note_24);
        }
    }

    public boolean hasAlbumArt(Uri albumArtUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(albumArtUri)) {
            return inputStream != null;
        } catch (Exception ex) {
        }
        return false;
    }

    private String formateTime(int seconds) {
        return String.format("%02d%02d", seconds / 60, seconds % 60);
    }

    private int[] createWaveForm() {
        Random random = new Random(System.currentTimeMillis());
        int[] values = new int[50];
        for (int i = 0; i < values.length; ++i) {
            values[i] = 5 + random.nextInt(50);
        }
        return values;
    }

    private void updatePlayPauseButton() {
        binding.buttonPlayPause.setImageResource(
                player != null && player.isPlaying() ?
                        R.drawable.ic_pause_24 :
                        R.drawable.ic_play_arrow_24
        );
    }

    private void playNext() {
        currentIndex = (currentIndex + 1) % (isShuffle ? shuffleList.size() : songList.size());
        initPlayerWithSong(currentIndex);
    }
}