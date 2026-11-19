import com.familyframe.radio.RelayServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class RelayServerTest {
    public static void main(String[] args) throws Exception {
        int port;
        try (ServerSocket probe = new ServerSocket(0)) { port = probe.getLocalPort(); }
        File audio = File.createTempFile("family-frame-audio", ".mp3");
        byte[] expected = "ID3test recording".getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream output = new FileOutputStream(audio)) { output.write(expected); }
        try (RelayServer server = new RelayServer((message, error) -> {}, port, audio)) {
            Thread thread = new Thread(() -> {
                try { server.serve(); } catch (java.io.IOException closed) { }
            });
            thread.setDaemon(true);
            thread.start();
            String base = "http://127.0.0.1:" + port;
            HttpURLConnection request = (HttpURLConnection) new URL(base + "/adhan.mp3").openConnection();
            request.setReadTimeout(2000);
            if (request.getResponseCode() != 200) throw new AssertionError("Adhan response failed");
            try (InputStream input = request.getInputStream()) {
                byte[] actual = input.readAllBytes();
                if (!Arrays.equals(actual, expected)) throw new AssertionError("Recording changed");
            }
            request.disconnect();
            request = (HttpURLConnection) new URL(base + "/../private.mp3").openConnection();
            if (request.getResponseCode() != 404) throw new AssertionError("Unexpected file access");
            request.disconnect();
            if (!audio.delete()) throw new AssertionError("Could not remove fixture");
            request = (HttpURLConnection) new URL(base + "/adhan.mp3").openConnection();
            if (request.getResponseCode() != 404) throw new AssertionError("Missing recording status");
            request.disconnect();
            request = (HttpURLConnection) new URL(base + "/cairo.mp3").openConnection();
            request.setRequestMethod("HEAD");
            if (request.getResponseCode() != 200) throw new AssertionError("Radio endpoint failed");
            request.disconnect();
        } finally { audio.delete(); }
        System.out.println("Passed local adhan, radio probe, missing file and path restrictions");
    }
}
