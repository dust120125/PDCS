package tw.idv.poipoi.pdcs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import static tw.idv.poipoi.pdcs.Core.CORE;

/**
 * Created by DuST on 2017/12/11.
 */

public class HelpService extends Service {

    private static final int PLAY_TONE_ID = 22;
    private static final int PLAY_TONE_INTERVAL = 60000;
    private static final int TONE_LOOP_TIMES = 5;
    private static final String PLAY_TONE = "playTone";

    private ToneGenerator toneGenerator;
    private AlarmReceiver alarmReceiver;
    private AsyncTask<Void, Void, Void> tonePlayTask;
    private boolean finished;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("HelpService", "onCreate");
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
        setAlarm();

        CORE.setHelpServiceOn(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("HelpService", "onDestroy");
        finished = true;
        cancelAlarm();
        CORE.setHelpServiceOn(false);
    }

    private void setAlarm() {
        alarmReceiver = new AlarmReceiver();
        registerReceiver(alarmReceiver, new IntentFilter(PLAY_TONE));
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent it = new Intent(PLAY_TONE);
        PendingIntent pi = PendingIntent.getBroadcast(this, PLAY_TONE_ID,
                it, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), PLAY_TONE_INTERVAL, pi);
    }

    private void cancelAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent it = new Intent(PLAY_TONE);
        PendingIntent pi = PendingIntent.getBroadcast(this, PLAY_TONE_ID,
                it, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
        unregisterReceiver(alarmReceiver);
    }

    private void playTone(){
        tonePlayTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for(int i = 0; !finished && i < TONE_LOOP_TIMES; i++){
                    toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 1000);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        tonePlayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("HelpService", "Received broadcast: " + intent.getAction());
            if (intent.getAction().equals(PLAY_TONE)) {
                playTone();
            }
        }
    }
}
