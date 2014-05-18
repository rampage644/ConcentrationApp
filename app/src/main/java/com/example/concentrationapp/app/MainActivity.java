package com.example.concentrationapp.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends Activity {
    private ImageView mView = null;
    private Boolean mState = true;
    private RotateAnimation mRotation = null;

    private Boolean mStopAutomatically = false;
    private int mStopAutomaticallyTimeout = 300;
    private Uri mStopAutomaticallySound = null;

    private Handler handler = new Handler();
    private Runnable r = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = (ImageView) findViewById(R.id.dotView);
        mRotation = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotation.setInterpolator(new LinearInterpolator());
        mRotation.setRepeatCount(Animation.INFINITE);
        mRotation.setDuration(1000);

        mView.startAnimation(mRotation);

        updatePreferences();

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mState) {
                    stop();
                } else {
                    start();
                }
            }
        });
    }

    private void updatePreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mStopAutomatically = sharedPref.getBoolean("pref_auto_stop", mStopAutomatically);
        mStopAutomaticallyTimeout = Integer.parseInt(sharedPref.getString("pref_auto_stop_timeout", Integer.toString(mStopAutomaticallyTimeout)));
        mStopAutomaticallySound = Uri.parse(sharedPref.getString("pref_auto_stop_sound", ""));
        Log.d("capp", mStopAutomaticallySound.toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void start() {
        mRotation.start();
        if (mStopAutomatically) {
            r = new Runnable() {
                @Override
                public void run() {
                    if (mStopAutomaticallySound != null) {
                        Log.d("capp", "Ring!");
                        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.bell_ring);
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mp.release();
                            }
                        });
                        mp.setLooping(false);
                        mp.start();
                        mp.setLooping(false);
                    }
                    stop();
                }
            };
            handler.postDelayed(r, mStopAutomaticallyTimeout*1000);

        }
        mState = true;
    }

    private void stop() {
        mRotation.cancel();
        if (handler != null) {
            handler.removeCallbacks(r);
        }
        mState = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            updatePreferences();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, UserSettingsActivity.class);
            startActivityForResult(i, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
