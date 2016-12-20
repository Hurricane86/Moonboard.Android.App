package com.gio.martino.moonboard;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TestActivity extends Activity {

    byte[] data = new byte[11*18*2];
    int tickCounter = 0;
    boolean alive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        int index = 0;
        for(int col = 0; col < 11; ++col)
            for(int row = 0; row < 18; ++row)
            {
                int new_row = (col % 2 == 0) ? row : 17 - row;

                data[index] = (byte) (col + new_row * 11);
                data[index+1] = 0;

                index += 2;
            }


        /*for(int i = 0; i < data.length / 2; ++i) {
            data[i*2] = (byte) (i & 0xFF);
            data[i*2+1] = 0;
        }*/

        final Button playButton = (Button)findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alive = !alive;
            }
        });

        final Handler handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {

                handler.postDelayed(this, 500);

                if(!alive)
                    return;

                if(tickCounter >= data.length)
                    tickCounter = 0;

                byte[] holds = java.util.Arrays.copyOfRange(data, 0, tickCounter);

                ((MoonboardApplication)getApplication()).getMoonboardCommunicationService().send(MoonboardCommunicationService.MESSAGE_TYPE_SET_PROBLEM, holds);

                final TextView progressLabel = (TextView)findViewById(R.id.progressLabel);
                progressLabel.setText(Float.toString(((float)tickCounter/(float)data.length) * 100) + " %");

                tickCounter += 2;
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        alive = false;
        super.onDestroy();
    }
}
