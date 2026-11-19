package com.familyframe.dashboard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import com.familyframe.radio.RelayServer;
import java.io.File;
import java.io.IOException;

public final class FrameService extends Service {
    static final int PORT = 8768;
    private static final String CHANNEL = "family_frame";
    private volatile RelayServer server;
    private SettingsReturnButton returnButton;
    private static final String SHOW_RETURN = "com.familyframe.dashboard.SHOW_RETURN";
    private static final String HIDE_RETURN = "com.familyframe.dashboard.HIDE_RETURN";

    static void showReturn(Context context, ResultReceiver callback) {
        Intent intent = new Intent(context, FrameService.class).setAction(SHOW_RETURN);
        intent.putExtra("callback", callback);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
        else context.startService(intent);
    }

    static void hideReturn(Context context) {
        Intent intent = new Intent(context, FrameService.class).setAction(HIDE_RETURN);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
        else context.startService(intent);
    }

    static void start(Context context) {
        Intent intent = new Intent(context, FrameService.class);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent);
        else context.startService(intent);
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL,
                    "Family Frame", NotificationManager.IMPORTANCE_LOW));
        }
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder notice = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        startForeground(PORT, notice.setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle("Family Frame").setContentText("Dashboard audio is ready")
                .setContentIntent(open).setOngoing(true).build());
        if (server == null) startServer();
        if (returnButton == null) returnButton = new SettingsReturnButton(this);
        if (intent != null && SHOW_RETURN.equals(intent.getAction())) {
            boolean ready = returnButton.show();
            ResultReceiver callback = intent.getParcelableExtra("callback");
            if (callback != null) callback.send(ready ? 1 : 0, null);
        } else if (intent != null && HIDE_RETURN.equals(intent.getAction())) {
            returnButton.hide();
        }
        return START_STICKY;
    }

    private void startServer() {
        try {
            RelayServer relay = new RelayServer((message, error) -> {
                if (error == null) Log.i("FamilyFrame", message);
                else Log.e("FamilyFrame", message, error);
            }, PORT, new File(getFilesDir(), "adhan.mp3"));
            server = relay;
            new Thread(() -> {
                try {
                    relay.serve();
                } catch (IOException error) {
                    if (server == relay) {
                        Log.e("FamilyFrame", "Audio listener stopped", error);
                        stopSelf();
                    }
                }
            }, "family-frame-audio").start();
        } catch (IOException error) {
            Log.e("FamilyFrame", "Could not start built-in audio relay", error);
            stopSelf();
        }
    }

    @Override public void onDestroy() {
        if (returnButton != null) returnButton.hide();
        RelayServer relay = server;
        server = null;
        if (relay != null) relay.close();
        stopForeground(true);
        super.onDestroy();
    }
}
