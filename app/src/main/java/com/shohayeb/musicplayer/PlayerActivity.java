package com.shohayeb.musicplayer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    private final String CURRENT_SONG_INDEX = "index";
    private final String CURRENT_PLAYER_POSITION = "currentProgress";
    private int index;
    private Animation animBlink;
    private ImageButton playButton;
    private ImageButton stopButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private SeekBar seekBar;
    private ArrayList<Songs> list;
    private MediaPlayer player;
    private ImageView imageView;
    private TextView remainingTimeTextView;
    private TextView songDurationTextView;
    private AudioManager audioManager;

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                player.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                player.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                releaseMediaPlayer();
            }
        }
    };
    private MusicCompletionListener onComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        imageView = findViewById(R.id.imageView);
        playButton = findViewById(R.id.play);
        stopButton = findViewById(R.id.stop);
        nextButton = findViewById(R.id.next);
        previousButton = findViewById(R.id.previous);
        seekBar = findViewById(R.id.seekBar);
        remainingTimeTextView = findViewById(R.id.remaining_time);
        songDurationTextView = findViewById(R.id.song_duration);
        onComplete = new MusicCompletionListener();
        animBlink = AnimationUtils.loadAnimation(this, R.anim.blink);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (getIntent().getExtras().get(LoadSongs.SONGS_LIST) != null) {
            list = (ArrayList<Songs>) getIntent().getExtras().get(LoadSongs.SONGS_LIST);
            index = getIntent().getIntExtra(LoadSongs.INDEX, 0);
        }
        if (list != null) {
            if (savedInstanceState != null)
                createMedia(savedInstanceState.getInt(CURRENT_PLAYER_POSITION, 0));
            else
                createMedia(0);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                Handler h = new Handler();
                boolean onChange = false;
                Runnable r = new Runnable() {
                    public void run() {
                        if (player != null && onChange) {
                            remainingTimeTextView.setText(getTimeString(String.valueOf(player.getDuration() / 1000 - seekBar.getProgress())));
                            h.postDelayed(this, 100);
                        }
                    }
                };

                @Override
                public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                    if (fromUser) {
                        seekBar.setProgress(progress);
                        if (player != null)
                            player.seekTo(progress * 1000);

                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (player != null) {
                        onChange = true;
                        PlayerActivity.this.runOnUiThread(new Thread(r));
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (player != null && !player.isPlaying())
                        player.seekTo(seekBar.getProgress() * 1000);
                    h.removeCallbacksAndMessages(r);
                    onChange = false;


                }
            });
            playButton.setOnClickListener(v -> {
                if (player == null) {
                    createMedia(0);
                    playButton.setImageResource(R.drawable.ic_pause_light_green_a700_36dp);
                    remainingTimeTextView.clearAnimation();
                } else {
                    if (player.isPlaying()) {
                        player.pause();
                        playButton.setImageResource(R.drawable.ic_play_arrow_light_green_a700_36dp);
                        remainingTimeTextView.startAnimation(animBlink);
                    } else {
                        player.start();
                        playButton.setImageResource(R.drawable.ic_pause_light_green_a700_36dp);
                        remainingTimeTextView.clearAnimation();
                    }
                }
            });
            stopButton.setOnClickListener(v -> releaseMediaPlayer());
            nextButton.setOnClickListener(v -> playNext());
            previousButton.setOnClickListener(v -> playPrevious());
        }
    }

    // play the previous song if there is one else it will do nothing
    private void playPrevious() {
        if (index > 0) {
            index -= 1;
            releaseMediaPlayer();
            createMedia(0);
        }
    }

    // play the next song if there is one else it will do nothing
    private void playNext() {
        if (index < list.size() - 1) {
            index += 1;
            releaseMediaPlayer();
            createMedia(0);
        }
    }

    // Creates the media player object
    // start playing the media
    // update the text view with remaining seconds of the media
    // update the seek bar with the current media position
    private void createMedia(int resume) {
        int result = audioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        player = MediaPlayer.create(getApplicationContext(), Uri.parse(list.get(index).getPath()));
        remainingTimeTextView.setText(getTimeString(String.valueOf(player.getDuration() / 1000)));
        if (resume != 0)
            player.seekTo(resume);
        songDurationTextView.setText(getTimeString(String.valueOf(player.getDuration() / 1000)));
        remainingTimeTextView.clearAnimation();
        Handler handler = new Handler();
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    remainingTimeTextView.setText(getTimeString(String.valueOf(player.getDuration() / 1000 - player.getCurrentPosition() / 1000)));
                    handler.postDelayed(this, 1010);
                }
            }
        });

        player.start();
        byte[] image = list.get(index).getBitmap();
        if (image != null) {
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
        }
        player.setOnCompletionListener(onComplete);
        playButton.setImageResource(R.drawable.ic_pause_light_green_a700_36dp);
        seekBar.setMax(player.getDuration() / 1000);
        Handler mHandler = new Handler();
        int r;
        if ((player.getDuration() / 1000) > 10)
            r = 1000;
        else r = 200;
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    int mCurrentPosition = player.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    mHandler.postDelayed(this, r);
                } else if (player != null && !player.isPlaying())
                    mHandler.postDelayed(this, r);
            }
        });
    }

    // stop the media and set the media player to null
    // reset the seek bar position
    private void releaseMediaPlayer() {
        remainingTimeTextView.clearAnimation();
        // If the media player is not null, then it may be currently playing a sound.
        if (player != null) {
            player.release();
            player = null;
            playButton.setImageResource(R.drawable.ic_play_arrow_light_green_a700_36dp);
            seekBar.setProgress(0);
            audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    // stop the media when back button got pressed
    @Override
    public void finish() {
        releaseMediaPlayer();
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_righ);
    }

    /**
     * convert String of milli seconds to hours , minutes and seconds
     *
     * @param milliSecond the String to be converted
     * @return converted String hh:mm:ss
     */
    private String getTimeString(String milliSecond) {
        long second = Long.parseLong(milliSecond);
        StringBuffer buf = new StringBuffer();
        int hours = (int) (second / (60 * 60));
        int minutes = (int) ((second % (60 * 60)) / (60));
        int seconds = (int) (((second % (60 * 60)) % (60)));
        if (hours != 0) {
            buf.append(String.format("%02d", hours)).append(":");
        }
        buf.append(String.format("%02d", minutes)).append(":").append(String.format("%02d", seconds));
        return buf.toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (player != null) {
            outState.putInt(CURRENT_PLAYER_POSITION, player.getCurrentPosition());
            outState.putInt(CURRENT_SONG_INDEX, index);
            player.pause();
            player.release();
            player = null;
            audioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        index = savedInstanceState.getInt(CURRENT_SONG_INDEX);
        player.seekTo(savedInstanceState.getInt(CURRENT_PLAYER_POSITION));
    }

    private class MusicCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            remainingTimeTextView.setText(R.string.zero);
            seekBar.setProgress(seekBar.getMax());
            releaseMediaPlayer();
        }
    }
}
