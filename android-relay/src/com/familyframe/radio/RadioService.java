package com.familyframe.radio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;

public final class RadioService extends Service {
    private static final String TAG = "FrameRadioRelay";
    private static final String CHANNEL = "radio_relay";
    private static final int NOTICE = 8767;
    private volatile RelayServer relay;

    static boolean enabled(Context context) {
        return context.getSharedPreferences("relay", MODE_PRIVATE).getBoolean("enabled", false);
    }

    static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences("relay", MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply();
    }

    static void start(Context context) {
        Intent intent = new Intent(context, RadioService.class);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
        else context.startService(intent);
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (!enabled(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        NotificationManager notices = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            notices.createNotificationChannel(new NotificationChannel(
                    CHANNEL, "Frame radio relay", NotificationManager.IMPORTANCE_LOW));
        }
        startForeground(NOTICE, notification("Ready for On-frame relay"));
        if (relay == null) {
            try {
                RelayServer server = new RelayServer((message, error) -> {
                    if (error == null) Log.i(TAG, message);
                    else Log.e(TAG, message, error);
                });
                relay = server;
                new Thread(() -> {
                    try {
                        server.serve();
                    } catch (IOException error) {
                        if (relay == server) {
                            Log.e(TAG, "Loopback listener stopped", error);
                            stopSelf();
                        }
                    }
                }, "frame-radio-listener").start();
                Log.i(TAG, "Ready on 127.0.0.1:" + RelayServer.PORT);
            } catch (IOException error) {
                Log.e(TAG, "Could not bind the loopback port", error);
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        return START_STICKY;
    }

    private Notification notification(String message) {
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        return builder.setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Frame Radio Relay").setContentText(message)
                .setContentIntent(open).setOngoing(true).build();
    }

    @Override public void onDestroy() {
        RelayServer server = relay;
        relay = null;
        if (server != null) server.close();
        stopForeground(true);
        super.onDestroy();
    }
}
