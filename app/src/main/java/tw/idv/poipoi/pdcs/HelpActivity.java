package tw.idv.poipoi.pdcs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static tw.idv.poipoi.pdcs.Core.CORE;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        if (!CORE.isHelpServiceOn()){
            CORE.startHelpService();
        }

        findViewById(R.id.button_Exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitHelpMode();
            }
        });

    }

    private void exitHelpMode(){
        CORE.exitHelpMode();
        finish();
    }

}
