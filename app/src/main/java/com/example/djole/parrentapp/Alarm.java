package com.example.djole.parrentapp;

/**
 * Created by Djole on 26-Dec-17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.widget.Toast;


public class Alarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"Wake up",Toast.LENGTH_LONG).show();
        try {
            MapsActivity.mp.setLooping(true);
            MapsActivity.mp.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}