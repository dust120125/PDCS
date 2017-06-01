package tw.idv.poipoi.pdcs;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.dust.capApi.CAP;
import org.dust.capApi.Info;

public class AlertActivity extends AppCompatActivity {

    private boolean finished = false;
    private SoundPool soundPool;
    private int dangerSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Core.onAlertActivity = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Info info = (Info) bundle.getSerializable("Info");

        ((TextView) findViewById(R.id.textView_alertMessage)).setText(info.description);

        findViewById(R.id.button_sos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finished = true;
                finish();
            }
        });

        findViewById(R.id.button_fine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finished = true;
                finish();
            }
        });

        SoundPool.Builder builder = new SoundPool.Builder();
        AudioAttributes attributes = new AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_MUSIC).build();
        builder.setMaxStreams(2).setAudioAttributes(attributes);
        soundPool = builder.build();
        dangerSoundId = soundPool.load(this, R.raw.danger_alert_sound, 1);
        Log.i("AlertActivity", "SoundPool, ID: " + dangerSoundId);
        //soundPool.play(dangerSoundId, 1, 1, 0, -1, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(sampleId, 1, 1, 0, -1, 1);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("AlertActivity", "KeyDown:" + event.toString());
        return true;
    }

    @Override
    protected void onPause() {
        Log.i("AlertActivity", "onPause");
        soundPool.stop(dangerSoundId);
        soundPool.release();
        super.onPause();
        if (!finished) {
            Intent intent = new Intent(this, AlertActivity.class);
            finish();
            Log.i("AlertActivity", "restart");
            startActivity(intent);
        } else {
            Core.onAlertActivity = false;
        }
    }
}