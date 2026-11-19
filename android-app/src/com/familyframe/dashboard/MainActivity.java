package com.familyframe.dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONException;
import org.json.JSONObject;

public final class MainActivity extends Activity {
    private static final String ORIGIN = "https://familyframe.local/";
    private static final int PICK_FILE = 10;
    private static final int PICK_ADHAN = 11;
    private static final int BACKUP_LIMIT = 6500000;
    private static final int AUDIO_LIMIT = 32 * 1024 * 1024;
    private final ExecutorService files = Executors.newSingleThreadExecutor();
    private WebView web;
    private ValueCallback<Uri[]> fileSelection;
    private AlertDialog options;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        FrameService.start(this);
        web = new WebView(this);
        web.setBackgroundColor(0xff123d35);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        web.setWebViewClient(new DashboardClient());
        web.setWebChromeClient(new DashboardChrome());
        web.addJavascriptInterface(new NativeActions(), "FrameNative");
        setContentView(web);
        loadDashboard();
        // A predictable app-specific directory also supports frames without a document picker.
        File imports = getExternalFilesDir("import");
        if (imports == null) Log.w("FamilyFrame", "External import directory is unavailable");
    }

    private void loadDashboard() {
        try (InputStream input = getAssets().open("family-calendar.html")) {
            String html = new String(readLimited(input, 2 * 1024 * 1024), StandardCharsets.UTF_8);
            web.loadDataWithBaseURL(ORIGIN, html, "text/html", "UTF-8", ORIGIN);
        } catch (IOException error) {
            new AlertDialog.Builder(this).setTitle("Dashboard could not open")
                    .setMessage("Restart Family Frame or reinstall the APK with adb install -r.")
                    .setPositiveButton("Retry", (dialog, which) -> loadDashboard()).show();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (web != null) {
            web.onResume();
            FrameService.start(this);
            web.postDelayed(this::fullscreen, 250);
        }
    }

    private void fullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override public void onBackPressed() { showOptions(); }

    private void showOptions() {
        if (isFinishing() || (options != null && options.isShowing())) return;
        String[] actions = {"Return to dashboard", "Wi-Fi settings", "Date and time settings",
                "Media volume", "Choose adhan MP3", "Import copied backup",
                "Import copied adhan", "Set Family Frame as Home", "Restart dashboard"};
        options = new AlertDialog.Builder(this).setTitle("Family Frame")
                .setItems(actions, (dialog, index) -> {
                    switch (index) {
                        case 0: web.evaluateJavascript("window.go('Dashboard')", null); break;
                        case 1: openSettings(Settings.ACTION_WIFI_SETTINGS); break;
                        case 2: openSettings(Settings.ACTION_DATE_SETTINGS); break;
                        case 3: showVolume(); break;
                        case 4: chooseAdhan(); break;
                        case 5: importCopied("our-family-backup.json", false); break;
                        case 6: importCopied("adhan.mp3", true); break;
                        case 7: openSettings(Settings.ACTION_HOME_SETTINGS); break;
                        case 8: loadDashboard(); break;
                        default: break;
                    }
                }).setNegativeButton("Close", null).create();
        options.show();
    }

    private void openSettings(String action) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        try {
            startActivity(new Intent(action));
        } catch (ActivityNotFoundException error) {
            message("This settings screen is unavailable on this frame.");
        }
    }

    private void showVolume() {
        AudioManager audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        SeekBar slider = new SeekBar(this);
        slider.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        slider.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean user) {
                if (user) audio.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(32, 24, 32, 24);
        layout.addView(slider, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(this).setTitle("Media volume").setView(layout)
                .setPositiveButton("Done", null).show();
    }

    private void chooseAdhan() {
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("audio/*")
                .addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(pick, PICK_ADHAN);
        } catch (ActivityNotFoundException error) {
            message("Copy adhan.mp3 into the app's import folder, then use Import copied adhan.");
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == PICK_FILE) {
            if (fileSelection != null) {
                fileSelection.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result, data));
                fileSelection = null;
            }
        } else if (request == PICK_ADHAN && result == RESULT_OK && data != null && data.getData() != null) {
            Uri selected = data.getData();
            files.execute(() -> {
                try (InputStream input = getContentResolver().openInputStream(selected)) {
                    saveAdhan(input);
                } catch (IOException | SecurityException error) {
                    message("Could not read that recording. Choose a readable MP3 under 32 MB.");
                }
            });
        }
    }

    private void importCopied(String name, boolean audio) {
        File folder = getExternalFilesDir("import");
        if (folder == null) {
            message("The import folder is unavailable. Check device storage.");
            return;
        }
        files.execute(() -> {
            try (InputStream input = new FileInputStream(new File(folder, name))) {
                if (audio) saveAdhan(input);
                else {
                    String json = new String(readLimited(input, BACKUP_LIMIT), StandardCharsets.UTF_8);
                    runOnUiThread(() -> web.evaluateJavascript(
                            "window.importFrameBackup(" + JSONObject.quote(json) + ")", null));
                }
            } catch (IOException error) {
                message("Copy " + name + " to " + folder.getAbsolutePath() + " first.");
            }
        });
    }

    private void saveAdhan(InputStream input) throws IOException {
        if (input == null) throw new IOException("No recording selected");
        byte[] recording = readLimited(input, AUDIO_LIMIT);
        if (!isMp3(recording)) throw new IOException("Expected MP3 audio");
        File temporary = new File(getFilesDir(), "adhan.tmp");
        try (OutputStream output = new FileOutputStream(temporary)) {
            output.write(recording);
        }
        if (!temporary.renameTo(new File(getFilesDir(), "adhan.mp3"))) {
            temporary.delete();
            throw new IOException("Could not save recording");
        }
        runOnUiThread(() -> {
            web.evaluateJavascript("window.frameAdhanReady()", null);
            message("Adhan saved. Use Test adhan now in Prayer settings.");
        });
    }

    private static boolean isMp3(byte[] bytes) {
        if (bytes.length < 3) return false;
        return (bytes[0] == 'I' && bytes[1] == 'D' && bytes[2] == '3')
                || ((bytes[0] & 255) == 255 && (bytes[1] & 224) == 224);
    }

    private void exportBackup(String json) {
        files.execute(() -> {
            try {
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                if (bytes.length > BACKUP_LIMIT) throw new IOException("Backup is too large");
                JSONObject parsed = new JSONObject(json);
                if (!parsed.has("events") || !parsed.has("tasks")) throw new IOException("Invalid backup");
                File directory = getExternalFilesDir("backups");
                if (directory == null) throw new IOException("No backup storage");
                File destination = new File(directory, "our-family-backup-"
                        + System.currentTimeMillis() + ".json");
                try (OutputStream output = new FileOutputStream(destination)) { output.write(bytes); }
                message("Backup saved to " + destination.getAbsolutePath());
            } catch (IOException | JSONException error) {
                message("Could not save backup. Check free storage.");
            }
        });
    }

    private void message(String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
    }

    private static byte[] readLimited(InputStream input, int limit) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (output.size() + count > limit) throw new IOException("File is too large");
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }

    private static WebResourceResponse response(String type, int code, String reason, byte[] bytes) {
        return new WebResourceResponse(type, "UTF-8", code, reason,
                Collections.singletonMap("Access-Control-Allow-Origin", ORIGIN.substring(0, ORIGIN.length() - 1)),
                new ByteArrayInputStream(bytes));
    }

    private final class DashboardClient extends WebViewClient {
        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            message("Use the Frame button to open device settings.");
            return true;
        }

        @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url.equals("http://127.0.0.1:8768/cairo.mp3")
                    || url.equals("http://127.0.0.1:8768/adhan.mp3")
                    || url.startsWith("data:image/") || url.startsWith("data:audio/")) return null;
            if (!request.getMethod().equals("GET") || !NetworkPolicy.permits(url)) {
                return response("text/plain", 403, "Forbidden", new byte[0]);
            }
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Accept", "application/json");
                if (connection.getResponseCode() != 200) {
                    return response("text/plain", 502, "Bad Gateway", new byte[0]);
                }
                try (InputStream input = connection.getInputStream()) {
                    return response("application/json", 200, "OK", readLimited(input, 2 * 1024 * 1024));
                }
            } catch (IOException error) {
                Log.w("FamilyFrame", "API request failed for " + request.getUrl().getHost(), error);
                return response("text/plain", 502, "Bad Gateway", new byte[0]);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
        // TLS errors use WebView's default cancellation. Certificate validation stays enabled.
    }

    private final class DashboardChrome extends WebChromeClient {
        @Override public boolean onConsoleMessage(ConsoleMessage message) {
            Log.d("FamilyFrame", "Dashboard " + message.messageLevel() + ": " + message.message());
            return true;
        }
        @Override public boolean onJsAlert(WebView view, String url, String text, JsResult result) {
            new AlertDialog.Builder(MainActivity.this).setMessage(text)
                    .setPositiveButton("OK", (dialog, which) -> result.confirm())
                    .setOnCancelListener(dialog -> result.cancel()).show();
            return true;
        }
        @Override public boolean onJsConfirm(WebView view, String url, String text, JsResult result) {
            new AlertDialog.Builder(MainActivity.this).setMessage(text)
                    .setPositiveButton("Continue", (dialog, which) -> result.confirm())
                    .setNegativeButton("Cancel", (dialog, which) -> result.cancel())
                    .setOnCancelListener(dialog -> result.cancel()).show();
            return true;
        }
        @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                FileChooserParams parameters) {
            if (fileSelection != null) fileSelection.onReceiveValue(null);
            fileSelection = callback;
            try {
                Intent intent = parameters.createIntent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, PICK_FILE);
            } catch (ActivityNotFoundException error) {
                fileSelection.onReceiveValue(null);
                fileSelection = null;
                message("No file picker is installed. For a backup, use Import copied backup in Frame options.");
            }
            return true;
        }
    }

    private final class NativeActions {
        @JavascriptInterface public void showSettings() { runOnUiThread(() -> showOptions()); }
        @JavascriptInterface public void chooseAdhan() { runOnUiThread(() -> MainActivity.this.chooseAdhan()); }
        @JavascriptInterface public void exportBackup(String json) {
            if (json != null && json.length() <= BACKUP_LIMIT) MainActivity.this.exportBackup(json);
        }
    }

    @Override protected void onDestroy() {
        if (fileSelection != null) fileSelection.onReceiveValue(null);
        if (web != null) {
            web.removeJavascriptInterface("FrameNative");
            web.destroy();
        }
        files.shutdown();
        super.onDestroy();
    }
}
