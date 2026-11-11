package com.familyframe.radio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(24 * getResources().getDisplayMetrics().density);
        panel.setPadding(padding, padding, padding, padding);
        TextView title = new TextView(this);
        title.setText("Frame Radio Relay");
        title.setTextSize(26);
        panel.addView(title);
        TextView instructions = new TextView(this);
        instructions.setText("Cairo Quran Radio over the frame's Wi-Fi.\n"
                + "Start the relay, then choose Feed → On-frame relay in the dashboard.\n"
                + "Starting also enables the relay after reboot.");
        instructions.setTextSize(16);
        panel.addView(instructions);
        TextView status = new TextView(this);
        status.setText(RadioService.enabled(this) ? "Start on boot is enabled." : "Relay is off.");
        panel.addView(status);
        Button start = new Button(this);
        start.setText("Start relay");
        start.setOnClickListener(view -> {
            RadioService.setEnabled(this, true);
            RadioService.start(this);
            status.setText("Start requested. Open the dashboard and tap Play radio.");
        });
        panel.addView(start);
        Button stop = new Button(this);
        stop.setText("Stop relay");
        stop.setOnClickListener(view -> {
            RadioService.setEnabled(this, false);
            stopService(new Intent(this, RadioService.class));
            status.setText("Relay stopped. Start on boot is off.");
        });
        panel.addView(stop);
        Button dashboard = new Button(this);
        dashboard.setText("Open dashboard");
        dashboard.setOnClickListener(view -> {
            Intent fully = getPackageManager().getLaunchIntentForPackage("de.ozerov.fully");
            if (fully != null) startActivity(fully);
            else status.setText("Open Fully Kiosk Browser to return to the dashboard.");
        });
        panel.addView(dashboard);
        setContentView(panel);
    }
}
