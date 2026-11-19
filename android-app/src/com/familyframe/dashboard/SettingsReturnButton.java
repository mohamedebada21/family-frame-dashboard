package com.familyframe.dashboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;

/** A small return control while Android settings or a file picker is open. */
final class SettingsReturnButton {
    private final Context context;
    private final WindowManager windows;
    private Button button;

    SettingsReturnButton(Context context) {
        this.context = context;
        windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    boolean show() {
        if (button != null) return true;
        if (!Settings.canDrawOverlays(context)) return false;
        Button control = new Button(context);
        control.setText("← Back to Family Frame");
        control.setTextSize(17);
        control.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        control.setTextColor(Color.rgb(18, 61, 53));
        control.setAllCaps(false);
        control.setPadding(dp(18), dp(8), dp(18), dp(8));
        control.setMinHeight(dp(52));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(243, 238, 223));
        background.setCornerRadius(dp(26));
        background.setStroke(dp(2), Color.rgb(18, 61, 53));
        control.setBackground(background);
        control.setContentDescription("Back to Family Frame dashboard");
        control.setOnClickListener(view -> {
            try {
                context.startActivity(new Intent(context, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            } catch (RuntimeException error) {
                Log.e("FamilyFrame", "Could not return to dashboard", error);
            }
        });
        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams layout = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        layout.gravity = Gravity.BOTTOM | Gravity.LEFT;
        layout.x = dp(14);
        layout.y = dp(14);
        try {
            windows.addView(control, layout);
            button = control;
            return true;
        } catch (RuntimeException error) {
            Log.w("FamilyFrame", "Return button could not be displayed", error);
            return false;
        }
    }

    void hide() {
        if (button == null) return;
        try {
            windows.removeView(button);
        } catch (IllegalArgumentException error) {
            Log.d("FamilyFrame", "Return button was already removed");
        }
        button = null;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
