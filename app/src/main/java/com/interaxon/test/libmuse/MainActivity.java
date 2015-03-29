/**
 * Example of using libmuse library on android.
 * Interaxon, Inc. 2015
 */

package com.interaxon.test.libmuse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.media.SoundPool;
import android.media.AudioManager;
import android.media.AudioAttributes;

import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.LibMuseVersion;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;


/**
 * In this simple example MainActivity implements 2 MuseHeadband listeners
 * and updates UI when data from Muse is received. Similarly you can implement
 * listers for other data or register same listener to listen for different type
 * of data.
 * For simplicity we create Listeners as inner classes of MainActivity. We pass
 * reference to MainActivity as we want listeners to update UI thread in this
 * example app.
 * You can also connect multiple muses to the same phone and register same
 * listener to listen for data from different muses. In this case you will
 * have to provide synchronization for data members you are using inside
 * your listener.
 *
 * Usage instructions:
 * 1. Enable bluetooth on your device
 * 2. Pair your device with muse
 * 3. Run this project
 * 4. Press Refresh. It should display all paired Muses in Spinner
 * 5. Make sure Muse headband is waiting for connection and press connect.
 * It may take up to 10 sec in some cases.
 * 6. You should see EEG and accelerometer data as well as connection status,
 * Version information and MuseElements (alpha, beta, theta, delta, gamma waves)
 * on the screen.
 */
public class MainActivity extends FragmentActivity {
    public Muse getMuse() {
        return muse;
    }

    public void setMuse(Muse muse) {
        this.muse = muse;
    }

    public boolean isDataTransmission() {
        return dataTransmission;
    }

    public void setDataTransmission(boolean dataTransmission) {
        this.dataTransmission = dataTransmission;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isPlay_alpha() {
        return play_alpha;
    }

    public void setPlay_alpha(boolean play_alpha) {
        this.play_alpha = play_alpha;
    }

    public boolean isPlay_beta() {
        return play_beta;
    }

    public void setPlay_beta(boolean play_beta) {
        this.play_beta = play_beta;
    }

    public boolean isPlay_delta() {
        return play_delta;
    }

    public void setPlay_delta(boolean play_delta) {
        this.play_delta = play_delta;
    }

    public boolean isPlay_gamma() {
        return play_gamma;
    }

    public void setPlay_gamma(boolean play_gamma) {
        this.play_gamma = play_gamma;
    }

    public boolean isPlay_theta() {
        return play_theta;
    }

    public void setPlay_theta(boolean play_theta) {
        this.play_theta = play_theta;
    }

    /**
     * Connection listener updates UI with new connection status and logs it.
     */
    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getPreviousConnectionState().toString() +
                         " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                                " " + status;
            Log.i("Muse Headband", full);
            Activity activity = activityRef.get();
            // UI thread is used here only because we need to update
            // TextView values. You don't have to use another thread, unless
            // you want to run disconnect() or connect() from connection packet
            // handler. In this case creating another thread is required.
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView statusText =
                                (TextView) findViewById(R.id.con_status);
                        statusText.setText(status);
                        TextView museVersionText =
                                (TextView) findViewById(R.id.version);
                        if (current == ConnectionState.CONNECTED) {
                            MuseVersion museVersion = muse.getMuseVersion();
                            String version = museVersion.getFirmwareType() +
                                 " - " + museVersion.getFirmwareVersion() +
                                 " - " + Integer.toString(
                                    museVersion.getProtocolVersion());
                            museVersionText.setText(version);
                        } else {
                            museVersionText.setText(R.string.undefined);
                        }
                    }
                });
            }
        }
    }

    /**
     * Data listener will be registered to listen for: Accelerometer,
     * Eeg and Relative Alpha bandpower packets. In all cases we will
     * update UI with new values.
     * We also will log message if Artifact packets contains "blink" flag.
     * DataListener methods will be called from execution thread. If you are
     * implementing "serious" processing algorithms inside those listeners,
     * consider to create another thread.
     */
    class DataListener extends MuseDataListener {

        final WeakReference<Activity> activityRef;

        DataListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            switch (p.getPacketType()) {
//                case EEG:
//                    updateEeg(p.getValues());
//                    break;
//                case ACCELEROMETER:
//                    updateAccelerometer(p.getValues());
//                    break;
                case ALPHA_RELATIVE:
                    updateAlphaRelative(p.getValues());
                case BETA_RELATIVE:
                    updateBetaRelative(p.getValues());
                    break;
                case GAMMA_RELATIVE:
                    updateGammaRelative(p.getValues());
                    break;
                case DELTA_RELATIVE:
                    updateDeltaRelative(p.getValues());
                    break;
                case THETA_RELATIVE:
                    updateThetaRelative(p.getValues());
                    break;
                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn() && p.getBlink()) {
                Log.i("Artifacts", "blink");
            }
        }

//        private void updateAccelerometer(final ArrayList<Double> data) {
//            Activity activity = activityRef.get();
//            if (activity != null) {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        TextView acc_x = (TextView) findViewById(R.id.acc_x);
//                        TextView acc_y = (TextView) findViewById(R.id.acc_y);
//                        TextView acc_z = (TextView) findViewById(R.id.acc_z);
//                        acc_x.setText(String.format(
//                            "%6.2f", data.get(Accelerometer.FORWARD_BACKWARD.ordinal())));
//                        acc_y.setText(String.format(
//                            "%6.2f", data.get(Accelerometer.UP_DOWN.ordinal())));
//                        acc_z.setText(String.format(
//                            "%6.2f", data.get(Accelerometer.LEFT_RIGHT.ordinal())));
//                    }
//                });
//            }
//        }

//        private void updateEeg(final ArrayList<Double> data) {
//            Activity activity = activityRef.get();
//            if (activity != null) {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                         TextView tp9 = (TextView) findViewById(R.id.eeg_tp9);
//                         TextView fp1 = (TextView) findViewById(R.id.eeg_fp1);
//                         TextView fp2 = (TextView) findViewById(R.id.eeg_fp2);
//                         TextView tp10 = (TextView) findViewById(R.id.eeg_tp10);
//                         tp9.setText(String.format(
//                            "%6.2f", data.get(Eeg.TP9.ordinal())));
//                         fp1.setText(String.format(
//                            "%6.2f", data.get(Eeg.FP1.ordinal())));
//                         fp2.setText(String.format(
//                            "%6.2f", data.get(Eeg.FP2.ordinal())));
//                         tp10.setText(String.format(
//                            "%6.2f", data.get(Eeg.TP10.ordinal())));
//                    }
//                });
//            }
//        }

        private void updateAlphaRelative(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double tp9 = data.get(Eeg.TP9.ordinal());
                        double fp1 = data.get(Eeg.FP1.ordinal());
                        double fp2 = data.get(Eeg.FP2.ordinal());
                        double tp10 = data.get(Eeg.TP10.ordinal());

                        TextView elem1 = (TextView) findViewById(R.id.elem1);
                        TextView elem2 = (TextView) findViewById(R.id.elem2);
                        TextView elem3 = (TextView) findViewById(R.id.elem3);
                        TextView elem4 = (TextView) findViewById(R.id.elem4);
                        elem1.setText(String.format(
                        "%6.2f", data.get(Eeg.TP9.ordinal())));
                        elem2.setText(String.format(
                        "%6.2f", data.get(Eeg.FP1.ordinal())));
                        elem3.setText(String.format(
                        "%6.2f", data.get(Eeg.FP2.ordinal())));
                        elem4.setText(String.format(
                        "%6.2f", data.get(Eeg.TP10.ordinal())));

                        int count = 0;
                        double sum = 0;
                        if(!Double.isNaN(tp9))
                        {
                            sum += tp9;
                            count++;
                        }
                        if(!Double.isNaN(fp1))
                        {
                            sum += fp1;
                            count++;
                        }
                        if(!Double.isNaN(fp2))
                        {
                            sum += fp2;
                            count++;
                        }
                        if(!Double.isNaN(tp10))
                        {
                            sum += tp10;
                            count++;
                        }

                        double avgA = sum/count;

                        long curTime = System.currentTimeMillis();
                        long delta = curTime-prevTimeA;
                        prevTimeA = curTime;
                        timerA += delta;
                        if (timerA > Atime && avgA > 0 && play_alpha)
                        {
                            playPianoMajorC(avgA, 4);
                            timerA = 0;
                        }
                    }
                });
            }
        }

        private void updateBetaRelative(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double tp9 = data.get(Eeg.TP9.ordinal());
                        double fp1 = data.get(Eeg.FP1.ordinal());
                        double fp2 = data.get(Eeg.FP2.ordinal());
                        double tp10 = data.get(Eeg.TP10.ordinal());

                        TextView beta1 = (TextView) findViewById(R.id.beta1);
                        TextView beta2 = (TextView) findViewById(R.id.beta2);
                        TextView beta3 = (TextView) findViewById(R.id.beta3);
                        TextView beta4 = (TextView) findViewById(R.id.beta4);
                        beta1.setText(String.format(
                                "%6.2f", data.get(Eeg.TP9.ordinal())));
                        beta2.setText(String.format(
                                "%6.2f", data.get(Eeg.FP1.ordinal())));
                        beta3.setText(String.format(
                                "%6.2f", data.get(Eeg.FP2.ordinal())));
                        beta4.setText(String.format(
                                "%6.2f", data.get(Eeg.TP10.ordinal())));

                        int count = 0;
                        double sum = 0;
                        if(!Double.isNaN(tp9))
                        {
                            sum += tp9;
                            count++;
                        }
                        if(!Double.isNaN(fp1))
                        {
                            sum += fp1;
                            count++;
                        }
                        if(!Double.isNaN(fp2))
                        {
                            sum += fp2;
                            count++;
                        }
                        if(!Double.isNaN(tp10))
                        {
                            sum += tp10;
                            count++;
                        }

                        double avgA = sum/count;

                        long curTime = System.currentTimeMillis();
                        long delta = curTime-prevTimeB;
                        prevTimeB = curTime;
                        timerB += delta;
                        if (timerB > Btime && avgA > 0 && play_beta)
                        {
                            playPianoMajorC(avgA, 5);
                            timerB = 0;
                        }
                    }
                });
            }
        }

        private void updateDeltaRelative(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double tp9 = data.get(Eeg.TP9.ordinal());
                        double fp1 = data.get(Eeg.FP1.ordinal());
                        double fp2 = data.get(Eeg.FP2.ordinal());
                        double tp10 = data.get(Eeg.TP10.ordinal());

                        TextView delta1 = (TextView) findViewById(R.id.delta1);
                        TextView delta2 = (TextView) findViewById(R.id.delta2);
                        TextView delta3 = (TextView) findViewById(R.id.delta3);
                        TextView delta4 = (TextView) findViewById(R.id.delta4);
                        delta1.setText(String.format(
                                "%6.2f", data.get(Eeg.TP9.ordinal())));
                        delta2.setText(String.format(
                                "%6.2f", data.get(Eeg.FP1.ordinal())));
                        delta3.setText(String.format(
                                "%6.2f", data.get(Eeg.FP2.ordinal())));
                        delta4.setText(String.format(
                                "%6.2f", data.get(Eeg.TP10.ordinal())));

                        int count = 0;
                        double sum = 0;
                        if(!Double.isNaN(tp9))
                        {
                            sum += tp9;
                            count++;
                        }
                        if(!Double.isNaN(fp1))
                        {
                            sum += fp1;
                            count++;
                        }
                        if(!Double.isNaN(fp2))
                        {
                            sum += fp2;
                            count++;
                        }
                        if(!Double.isNaN(tp10))
                        {
                            sum += tp10;
                            count++;
                        }

                        double avgA = sum/count;

                        long curTime = System.currentTimeMillis();
                        long delta = curTime-prevTimeD;
                        prevTimeD = curTime;
                        timerD += delta;
                        if (timerD > Dtime && avgA > 0 && play_delta)
                        {
                            playPianoMajorC(avgA, 2);
                            timerD = 0;
                        }
                    }
                });
            }
        }

        private void updateGammaRelative(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double tp9 = data.get(Eeg.TP9.ordinal());
                        double fp1 = data.get(Eeg.FP1.ordinal());
                        double fp2 = data.get(Eeg.FP2.ordinal());
                        double tp10 = data.get(Eeg.TP10.ordinal());

                        TextView gamma1 = (TextView) findViewById(R.id.gamma1);
                        TextView gamma2 = (TextView) findViewById(R.id.gamma2);
                        TextView gamma3 = (TextView) findViewById(R.id.gamma3);
                        TextView gamma4 = (TextView) findViewById(R.id.gamma4);
                        gamma1.setText(String.format(
                                "%6.2f", data.get(Eeg.TP9.ordinal())));
                        gamma2.setText(String.format(
                                "%6.2f", data.get(Eeg.FP1.ordinal())));
                        gamma3.setText(String.format(
                                "%6.2f", data.get(Eeg.FP2.ordinal())));
                        gamma4.setText(String.format(
                                "%6.2f", data.get(Eeg.TP10.ordinal())));

                        int count = 0;
                        double sum = 0;
                        if(!Double.isNaN(tp9))
                        {
                            sum += tp9;
                            count++;
                        }
                        if(!Double.isNaN(fp1))
                        {
                            sum += fp1;
                            count++;
                        }
                        if(!Double.isNaN(fp2))
                        {
                            sum += fp2;
                            count++;
                        }
                        if(!Double.isNaN(tp10))
                        {
                            sum += tp10;
                            count++;
                        }

                        double avgA = sum/count;

                        long curTime = System.currentTimeMillis();
                        long delta = curTime-prevTimeG;
                        prevTimeG = curTime;
                        timerG += delta;
                        if (timerG > Gtime && avgA > 0 && play_gamma)
                        {
                            playPianoMajorC(avgA, 6);
                            timerG = 0;
                        }
                    }
                });
            }
        }

        private void updateThetaRelative(final ArrayList<Double> data) {
            Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double tp9 = data.get(Eeg.TP9.ordinal());
                        double fp1 = data.get(Eeg.FP1.ordinal());
                        double fp2 = data.get(Eeg.FP2.ordinal());
                        double tp10 = data.get(Eeg.TP10.ordinal());

                        TextView theta1 = (TextView) findViewById(R.id.theta1);
                        TextView theta2 = (TextView) findViewById(R.id.theta2);
                        TextView theta3 = (TextView) findViewById(R.id.theta3);
                        TextView theta4 = (TextView) findViewById(R.id.theta4);
                        theta1.setText(String.format(
                                "%6.2f", data.get(Eeg.TP9.ordinal())));
                        theta2.setText(String.format(
                                "%6.2f", data.get(Eeg.FP1.ordinal())));
                        theta3.setText(String.format(
                                "%6.2f", data.get(Eeg.FP2.ordinal())));
                        theta4.setText(String.format(
                                "%6.2f", data.get(Eeg.TP10.ordinal())));

                        int count = 0;
                        double sum = 0;
                        if(!Double.isNaN(tp9))
                        {
                            sum += tp9;
                            count++;
                        }
                        if(!Double.isNaN(fp1))
                        {
                            sum += fp1;
                            count++;
                        }
                        if(!Double.isNaN(fp2))
                        {
                            sum += fp2;
                            count++;
                        }
                        if(!Double.isNaN(tp10))
                        {
                            sum += tp10;
                            count++;
                        }

                        double avgA = sum/count;

                        long curTime = System.currentTimeMillis();
                        long delta = curTime-prevTimeT;
                        prevTimeT = curTime;
                        timerT += delta;
                        if (timerT > Ttime && avgA > 0 && play_theta)
                        {
                            playPianoMajorC(avgA, 3);
                            timerT = 0;
                        }
                    }
                });
            }
        }

    }

    // Declarations
    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private DataListener dataListener = null;
    private boolean dataTransmission = true;
    // Holds sounds to play
    private SoundPool pool;
    // Manages audio
    AudioManager audioManager;
    // Different soundID declarations, for playing notes
    private int a2, a3, a4, a5, a6, ab2, ab3, ab4, ab5, ab6, b2, b3, b4, b5, b6;
    private int bb2, bb3, bb4, bb5, bb6, c2, c3, c4, c5, c6, d2, d3, d4, d5, d6;
    private int db2, db3, db4, db5, db6, e2, e3, e4, e5, e6, eb2, eb3, eb4, eb5, eb6;
    private int f2, f3, f4, f5, f6, g2, g3, g4, g5, g6, gb2, gb3, gb4, gb5, gb6;
    private boolean loaded = false;
    private float actVolume, maxVolume, volume;
    // Timer, determines when to play sounds - kinda crappy solution
    private long prevTimeA = 0;
    private long timerA = 0;
    private long prevTimeB = 0;
    private long timerB = 0;
    private long prevTimeD = 0;
    private long timerD = 0;
    private long prevTimeG = 0;
    private long timerG = 0;
    private long prevTimeT = 0;
    private long timerT = 0;
    // Time between notes
    private long Atime = 1250;
    private long Btime = 1000;
    private long Dtime = 2000;
    private long Gtime = 750;
    private long Ttime = 1500;

    private boolean play_alpha = false;
    private boolean play_beta = false;
    private boolean play_delta = false;
    private boolean play_gamma = false;
    private boolean play_theta = false;

    public MainActivity() {
        // Create listeners and pass reference to activity to them
        WeakReference<Activity> weakActivity =
                                new WeakReference<Activity>(this);
        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Make buttons
        super.onCreate(savedInstanceState);

        // Get the view from activity_main.xml
        setContentView(R.layout.activity_main);

        // Locate the viewpager in activity_main.xml
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        // Set the ViewPagerAdapter into ViewPager
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;
        //Hardware buttons setting to adjust the media sound
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Audio settings
        AudioAttributes attributes = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build();
        pool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(10).build();

        // Pause app until sounds loaded
        pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        // Loading sounds
        a2 = pool.load(this, R.raw.pianoa2, 1);
        a3 = pool.load(this, R.raw.pianoa3, 1);
        a4 = pool.load(this, R.raw.pianoa4, 1);
        a5 = pool.load(this, R.raw.pianoa5, 1);
        a6 = pool.load(this, R.raw.pianoa6, 1);

        ab2 = pool.load(this, R.raw.pianoab2, 1);
        ab3 = pool.load(this, R.raw.pianoab3, 1);
        ab4 = pool.load(this, R.raw.pianoab4, 1);
        ab5 = pool.load(this, R.raw.pianoab5, 1);
        ab6 = pool.load(this, R.raw.pianoab6, 1);

        b2 = pool.load(this, R.raw.pianob2, 1);
        b3 = pool.load(this, R.raw.pianob3, 1);
        b4 = pool.load(this, R.raw.pianob4, 1);
        b5 = pool.load(this, R.raw.pianob5, 1);
        b6 = pool.load(this, R.raw.pianob6, 1);

        bb2 = pool.load(this, R.raw.pianobb2, 1);
        bb3 = pool.load(this, R.raw.pianobb3, 1);
        bb4 = pool.load(this, R.raw.pianobb4, 1);
        bb5 = pool.load(this, R.raw.pianobb5, 1);
        bb6 = pool.load(this, R.raw.pianobb6, 1);

        c2 = pool.load(this, R.raw.pianob2, 1);
        c3 = pool.load(this, R.raw.pianoc3, 1);
        c4 = pool.load(this, R.raw.pianoc4, 1);
        c5 = pool.load(this, R.raw.pianoc5, 1);
        c6 = pool.load(this, R.raw.pianoc6, 1);

        d2 = pool.load(this, R.raw.pianod2, 1);
        d3 = pool.load(this, R.raw.pianod3, 1);
        d4 = pool.load(this, R.raw.pianod4, 1);
        d5 = pool.load(this, R.raw.pianod5, 1);
        d6 = pool.load(this, R.raw.pianod6, 1);

        db2 = pool.load(this, R.raw.pianodb2, 1);
        db3 = pool.load(this, R.raw.pianodb3, 1);
        db4 = pool.load(this, R.raw.pianodb4, 1);
        db5 = pool.load(this, R.raw.pianodb5, 1);
        db6 = pool.load(this, R.raw.pianodb6, 1);

        e2 = pool.load(this, R.raw.pianoe2, 1);
        e3 = pool.load(this, R.raw.pianoe3, 1);
        e4 = pool.load(this, R.raw.pianoe4, 1);
        e5 = pool.load(this, R.raw.pianoe5, 1);
        e6 = pool.load(this, R.raw.pianoe6, 1);

        eb2 = pool.load(this, R.raw.pianoeb2, 1);
        eb3 = pool.load(this, R.raw.pianoeb3, 1);
        eb4 = pool.load(this, R.raw.pianoeb4, 1);
        eb5 = pool.load(this, R.raw.pianoeb5, 1);
        eb6 = pool.load(this, R.raw.pianoeb6, 1);

        f2 = pool.load(this, R.raw.pianof2, 1);
        f3 = pool.load(this, R.raw.pianof3, 1);
        f4 = pool.load(this, R.raw.pianof4, 1);
        f5 = pool.load(this, R.raw.pianof5, 1);
        f6 = pool.load(this, R.raw.pianof6, 1);

        g2 = pool.load(this, R.raw.pianog2, 1);
        g3 = pool.load(this, R.raw.pianog3, 1);
        g4 = pool.load(this, R.raw.pianog4, 1);
        g5 = pool.load(this, R.raw.pianog5, 1);
        g6 = pool.load(this, R.raw.pianog6, 1);

        gb2 = pool.load(this, R.raw.pianogb2, 1);
        gb3 = pool.load(this, R.raw.pianogb3, 1);
        gb4 = pool.load(this, R.raw.pianogb4, 1);
        gb5 = pool.load(this, R.raw.pianogb5, 1);
        gb6 = pool.load(this, R.raw.pianogb6, 1);

        Log.i("Muse Headband", "libmuse version=" + LibMuseVersion.SDK_VERSION);
    }

    /**
     * Plays a piano note, based on a value (val) between 0 and 1 to determine the key, and an
     * octave between 2 and 6
     *
     */
    private void playPiano(double val, int octave)
    {
        if(octave == 2)
        {
            if (val < .06699 )
            {
                pool.play(c2, volume, volume, 1, 0, 1f);
            }
            else if (val < .13795)
            {
                pool.play(db2, volume, volume, 1, 0, 1f);
            }
            else if (val < .21313)
            {
                pool.play(d2, volume, volume, 1, 0, 1f);
            }
            else if (val < .29279)
            {
                pool.play(eb2, volume, volume, 1, 0, 1f);
            }
            else if (val < .37718)
            {
                pool.play(e2, volume, volume, 1, 0, 1f);
            }
            else if (val < .46659)
            {
                pool.play(f2, volume, volume, 1, 0, 1f);
            }
            else if (val < .56132)
            {
                pool.play(gb2, volume, volume, 1, 0, 1f);
            }
            else if (val < .66167)
            {
                pool.play(g2, volume, volume, 1, 0, 1f);
            }
            else if (val < .768)
            {
                pool.play(ab2, volume, volume, 1, 0, 1f);
            }
            else if (val < .88065)
            {
                pool.play(a2, volume, volume, 1, 0, 1f);
            }
            else if (val < .95)
            {
                pool.play(bb2, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b2, volume, volume, 1, 0, 1f);
            }
        }
        else if(octave == 3)
        {
            if (val < .06699 )
            {
                pool.play(c3, volume, volume, 1, 0, 1f);
            }
            else if (val < .13795)
            {
                pool.play(db3, volume, volume, 1, 0, 1f);
            }
            else if (val < .21313)
            {
                pool.play(d3, volume, volume, 1, 0, 1f);
            }
            else if (val < .29279)
            {
                pool.play(eb3, volume, volume, 1, 0, 1f);
            }
            else if (val < .37718)
            {
                pool.play(e3, volume, volume, 1, 0, 1f);
            }
            else if (val < .46659)
            {
                pool.play(f3, volume, volume, 1, 0, 1f);
            }
            else if (val < .56132)
            {
                pool.play(gb3, volume, volume, 1, 0, 1f);
            }
            else if (val < .66167)
            {
                pool.play(g3, volume, volume, 1, 0, 1f);
            }
            else if (val < .768)
            {
                pool.play(ab3, volume, volume, 1, 0, 1f);
            }
            else if (val < .88065)
            {
                pool.play(a3, volume, volume, 1, 0, 1f);
            }
            else if (val < .95)
            {
                pool.play(bb3, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b3, volume, volume, 1, 0, 1f);
            }
        }
        else if(octave == 4)
        {
            if (val < .06699 )
            {
                pool.play(c4, volume, volume, 1, 0, 1f);
            }
            else if (val < .13795)
            {
                pool.play(db4, volume, volume, 1, 0, 1f);
            }
            else if (val < .21313)
            {
                pool.play(d4, volume, volume, 1, 0, 1f);
            }
            else if (val < .29279)
            {
                pool.play(eb4, volume, volume, 1, 0, 1f);
            }
            else if (val < .37718)
            {
                pool.play(e4, volume, volume, 1, 0, 1f);
            }
            else if (val < .46659)
            {
                pool.play(f4, volume, volume, 1, 0, 1f);
            }
            else if (val < .56132)
            {
                pool.play(gb4, volume, volume, 1, 0, 1f);
            }
            else if (val < .66167)
            {
                pool.play(g4, volume, volume, 1, 0, 1f);
            }
            else if (val < .768)
            {
                pool.play(ab4, volume, volume, 1, 0, 1f);
            }
            else if (val < .88065)
            {
                pool.play(a4, volume, volume, 1, 0, 1f);
            }
            else if (val < .95)
            {
                pool.play(bb4, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b4, volume, volume, 1, 0, 1f);
            }
        }
        else if(octave == 5)
        {
            if (val < .06699 )
            {
                pool.play(c5, volume, volume, 1, 0, 1f);
            }
            else if (val < .13795)
            {
                pool.play(db5, volume, volume, 1, 0, 1f);
            }
            else if (val < .21313)
            {
                pool.play(d5, volume, volume, 1, 0, 1f);
            }
            else if (val < .29279)
            {
                pool.play(eb5, volume, volume, 1, 0, 1f);
            }
            else if (val < .37718)
            {
                pool.play(e5, volume, volume, 1, 0, 1f);
            }
            else if (val < .46659)
            {
                pool.play(f5, volume, volume, 1, 0, 1f);
            }
            else if (val < .56132)
            {
                pool.play(gb5, volume, volume, 1, 0, 1f);
            }
            else if (val < .66167)
            {
                pool.play(g5, volume, volume, 1, 0, 1f);
            }
            else if (val < .768)
            {
                pool.play(ab5, volume, volume, 1, 0, 1f);
            }
            else if (val < .88065)
            {
                pool.play(a5, volume, volume, 1, 0, 1f);
            }
            else if (val < .95)
            {
                pool.play(bb5, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b5, volume, volume, 1, 0, 1f);
            }
        }
        else if(octave == 6)
        {
            if (val < .06699 )
            {
                pool.play(c6, volume, volume, 1, 0, 1f);
            }
            else if (val < .13795)
            {
                pool.play(db6, volume, volume, 1, 0, 1f);
            }
            else if (val < .21313)
            {
                pool.play(d6, volume, volume, 1, 0, 1f);
            }
            else if (val < .29279)
            {
                pool.play(eb6, volume, volume, 1, 0, 1f);
            }
            else if (val < .37718)
            {
                pool.play(e6, volume, volume, 1, 0, 1f);
            }
            else if (val < .46659)
            {
                pool.play(f6, volume, volume, 1, 0, 1f);
            }
            else if (val < .56132)
            {
                pool.play(gb6, volume, volume, 1, 0, 1f);
            }
            else if (val < .66167)
            {
                pool.play(g6, volume, volume, 1, 0, 1f);
            }
            else if (val < .768)
            {
                pool.play(ab6, volume, volume, 1, 0, 1f);
            }
            else if (val < .88065)
            {
                pool.play(a6, volume, volume, 1, 0, 1f);
            }
            else if (val < .95)
            {
                pool.play(bb6, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b6, volume, volume, 1, 0, 1f);
            }
        }
    }

    /**
     * Plays a piano note, based on a value (val) between 0 and 1 to determine the key, and an
     * octave between 2 and 6 - Major C version
     *
     */
    private void playPianoMajorC(double val, int octave)
    {
        if(octave == 2)
        {
            if (val < .14286 )
            {
                pool.play(c2, volume, volume, 1, 0, 1f);
            }
            else if (val < .21313)
            {
                pool.play(d2, volume, volume, 1, 0, 1f);
            }
            else if (val < .42857)
            {
                pool.play(e2, volume, volume, 1, 0, 1f);
            }
            else if (val < .57143)
            {
                pool.play(f2, volume, volume, 1, 0, 1f);
            }
            else if (val < .71429)
            {
                pool.play(g2, volume, volume, 1, 0, 1f);
            }
            else if (val < .86)
            {
                pool.play(a2, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b2, volume, volume, 1, 0, 1f);
            }
        }
        else if(octave == 3)
        {
            if (val < .14286 )
            {
                pool.play(c3, volume, volume, 1, 0, 1f);
            }
            else if (val < .28571)
            {
                pool.play(d3, volume, volume, 1, 0, 1f);
            }
            else if (val < .42857)
            {
                pool.play(e3, volume, volume, 1, 0, 1f);
            }
            else if (val < .57143)
            {
                pool.play(f3, volume, volume, 1, 0, 1f);
            }
            else if (val < .71429)
            {
                pool.play(g3, volume, volume, 1, 0, 1f);
            }
            else if (val < .86)
            {
                pool.play(a3, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b3, volume, volume, 1, 0, 1f);
            }
        }
        else if(octave == 4)
        {
            if (val < .14286 )
            {
                pool.play(c4, volume, volume, 1, 0, 1f);
            }
            else if (val < .28571)
            {
                pool.play(d4, volume, volume, 1, 0, 1f);
            }
            else if (val < .42857)
            {
                pool.play(e4, volume, volume, 1, 0, 1f);
            }
            else if (val < .57143)
            {
                pool.play(f4, volume, volume, 1, 0, 1f);
            }
            else if (val < .71429)
            {
                pool.play(g4, volume, volume, 1, 0, 1f);
            }
            else if (val < .86)
            {
                pool.play(a4, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b4, volume, volume, 1, 0, 1f);
            }
        }
        else if(octave == 5)
        {
            if (val < .14286 )
            {
                pool.play(c5, volume, volume, 1, 0, 1f);
            }
            else if (val < .28571)
            {
                pool.play(d5, volume, volume, 1, 0, 1f);
            }
            else if (val < .42857)
            {
                pool.play(e5, volume, volume, 1, 0, 1f);
            }
            else if (val < .57143)
            {
                pool.play(f5, volume, volume, 1, 0, 1f);
            }
            else if (val < .71429)
            {
                pool.play(g5, volume, volume, 1, 0, 1f);
            }
            else if (val < .86)
            {
                pool.play(a5, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b5, volume, volume, 1, 0, 1f);
            }
        }
        else if(octave == 6)
        {
            if (val < .14286 )
            {
                pool.play(c6, volume, volume, 1, 0, 1f);
            }
            else if (val < .28571)
            {
                pool.play(d6, volume, volume, 1, 0, 1f);
            }
            else if (val < .42857)
            {
                pool.play(e6, volume, volume, 1, 0, 1f);
            }
            else if (val < .57143)
            {
                pool.play(f6, volume, volume, 1, 0, 1f);
            }
            else if (val < .71429)
            {
                pool.play(g6, volume, volume, 1, 0, 1f);
            }
            else if (val < .86)
            {
                pool.play(a6, volume, volume, 1, 0, 1f);
            }
            else if (val < 1)
            {
                pool.play(b6, volume, volume, 1, 0, 1f);
            }
        }
    }

    public void configure_library() {
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener,
                                  MuseDataPacketType.ACCELEROMETER);
        muse.registerDataListener(dataListener,
                                  MuseDataPacketType.EEG);
        muse.registerDataListener(dataListener,
                                  MuseDataPacketType.ALPHA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.BETA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.THETA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.GAMMA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.DELTA_RELATIVE);
        muse.registerDataListener(dataListener,
                                  MuseDataPacketType.ARTIFACTS);
        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
