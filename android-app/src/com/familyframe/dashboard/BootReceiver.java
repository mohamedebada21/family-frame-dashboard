package com.familyframe.dashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        FrameService.start(context);
        // The reference Android 6 frame permits this. Newer Android uses the Home role.
        if (Build.VERSION.SDK_INT <= 28) {
            try {
                context.startActivity(new Intent(context, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (RuntimeException error) {
                Log.w("FamilyFrame", "Select Family Frame as the Home app", error);
            }
        }
    }
}
