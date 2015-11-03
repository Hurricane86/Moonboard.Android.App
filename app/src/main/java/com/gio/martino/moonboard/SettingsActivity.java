package com.gio.martino.moonboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

public class SettingsActivity extends Activity {

    public static final int DEFAULT_NORMAL_HOLD_COLOR = 0xFF000000;
    public static final int DEFAULT_START_HOLD_COLOR  = 0x00FF0000;
    public static final int DEFAULT_TOP_HOLD_COLOR    = 0x0000FF00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Switch realtimeUpdateSwitch = (Switch)findViewById(R.id.realtimeUpdateSwitch);
        realtimeUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(compoundButton.getContext()).edit();
                edit.putBoolean("realtimeUpdate", b);
                edit.apply();
            }
        });

        boolean realtimeUpdate = settings.getBoolean("realtimeUpdate", false);
        realtimeUpdateSwitch.setChecked(realtimeUpdate);

        SurfaceView sv = (SurfaceView)findViewById(R.id.normalHoldSV);
        sv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), ColorPickerActivity.class);
                intent.putExtra("color_setting_id", "normal_hold_color");
                startActivityForResult(intent, 0);
            }
        });

        sv = (SurfaceView)findViewById(R.id.startHoldSV);
        sv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), ColorPickerActivity.class);
                intent.putExtra("color_setting_id", "start_hold_color");
                startActivityForResult(intent, 1);
            }
        });

        sv = (SurfaceView)findViewById(R.id.topHoldSV);
        sv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), ColorPickerActivity.class);
                intent.putExtra("color_setting_id", "top_hold_color");
                startActivityForResult(intent, 2);
            }
        });

        int color_0 = settings.getInt("normal_hold_color", DEFAULT_NORMAL_HOLD_COLOR);
        int color_1 = settings.getInt("start_hold_color",  DEFAULT_START_HOLD_COLOR);
        int color_2 = settings.getInt("top_hold_color",    DEFAULT_TOP_HOLD_COLOR);

        setHoldColor(0, color_0, false);
        setHoldColor(1, color_1, false);
        setHoldColor(2, color_2, false);

        ImageView connectionStatusImageView = (ImageView)findViewById(R.id.connectionStatusImageView);
        if(((MoonboardApplication) getApplication()).getMoonboardCommunicationService().isAlive())
            connectionStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.status_connected));
        else
            connectionStatusImageView.setImageDrawable(getResources().getDrawable(R.drawable.status_not_connected));
    }

    private void setHoldColor(int holdType, int color, boolean send)
    {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        SurfaceView sv;
        switch(holdType)
        {
            default:
            case 0: {
                sv = (SurfaceView) findViewById(R.id.normalHoldSV);
                break;
            }
            case 1: {
                sv = (SurfaceView) findViewById(R.id.startHoldSV);
                break;
            }
            case 2: {
                sv = (SurfaceView) findViewById(R.id.topHoldSV);
                break;
            }

        }

        sv.setBackgroundColor(Color.argb(alpha, red, green, blue));

        if(send)
        {
            byte[] data = new byte[4];
            data[0] = (byte) holdType;
            data[1] = (byte) red;
            data[2] = (byte) green;
            data[3] = (byte) blue;

            ((MoonboardApplication) getApplication()).getMoonboardCommunicationService().send(MoonboardCommunicationService.MESSAGE_TYPE_SET_COLORS, data);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_CANCELED)
            return;

        int color = data.getIntExtra("result", 0);
        setHoldColor(requestCode, color, true);
    }
}
