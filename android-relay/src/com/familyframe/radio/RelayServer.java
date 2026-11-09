package com.familyframe.radio;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;

/** One fixed station, exposed only on the frame's loopback interface. */
final class RelayServer implements Closeable {
    static final int PORT = 8767;
    private static final String HOST = "127.0.0.1";
    private static final String PATH = "/cairo.mp3";
    private static final String UPSTREAM = "https://radio.xecod.com/station/quran-cairo";
    private final ServerSocket listener = new ServerSocket();
    private final Set<Socket> clients = new HashSet<>();
    private final ThreadPoolExecutor workers = new ThreadPoolExecutor(
            0, 4, 15, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    private final Status status;
    private boolean closed;

    interface Status {
        void update(String message, IOException error);
    }

    RelayServer(Status status) throws IOException {
        this.status = status;
        try {
            listener.setReuseAddress(true);
            listener.bind(new InetSocketAddress(InetAddress.getByName(HOST), PORT));
        } catch (IOException error) {
            listener.close();
            throw error;
        }
    }

    void serve() throws IOException {
        while (!listener.isClosed()) {
            Socket client = listener.accept();
            synchronized (clients) {
                if (closed) {
                    client.close();
                    return;
                }
                clients.add(client);
            }
            try {
                workers.execute(() -> handle(client));
            } catch (RejectedExecutionException busy) {
                release(client);
            }
        }
    }

    private void handle(Socket client) {
        try {
            client.setSoTimeout(8000);
            InputStream input = new BufferedInputStream(client.getInputStream());
            OutputStream output = client.getOutputStream();
            String request = readLine(input, 512);
            if (request == null) return;
            String[] parts = request.split(" ");
            if (parts.length != 3 || !parts[2].startsWith("HTTP/1.")) {
                reply(output, "400 Bad Request", "text/plain", true);
                return;
            }
            boolean head = parts[0].equals("HEAD");
            if (!head && !parts[0].equals("GET")) {
                reply(output, "405 Method Not Allowed", "text/plain", true);
                return;
            }
            if (!parts[1].equals(PATH)) {
                reply(output, "404 Not Found", "text/plain", true);
                return;
            }
            boolean endOfHeaders = false;
            for (int i = 0; i < 32; i++) {
                String header = readLine(input, 2048);
                if (header == null) return;
                if (header.isEmpty()) {
                    endOfHeaders = true;
                    break;
                }
                if (header.regionMatches(true, 0, "Host:", 0, 5)) {
                    String host = header.substring(5).trim();
                    if (!host.equals(HOST + ":" + PORT) && !host.equals("localhost:" + PORT)) {
                        reply(output, "400 Bad Request", "text/plain", true);
                        return;
                    }
                }
            }
            if (!endOfHeaders) {
                reply(output, "431 Request Header Fields Too Large", "text/plain", true);
                return;
            }
            if (head) reply(output, "200 OK", "audio/mpeg", false);
            else stream(output);
        } catch (IOException ignored) {
            // The browser may close a probe or stop playback at any point.
        } finally {
            release(client);
        }
    }

    private void stream(OutputStream output) throws IOException {
        HttpsURLConnection source = (HttpsURLConnection) new URL(UPSTREAM).openConnection();
        source.setInstanceFollowRedirects(false);
        source.setConnectTimeout(8000);
        source.setReadTimeout(12000);
        source.setRequestProperty("User-Agent", "FamilyFrameRadio/1.0");
        byte[] buffer = new byte[4096];
        boolean sentHeaders = false;
        try {
            int code = source.getResponseCode();
            String type = source.getContentType();
            if (code != 200 || type == null || !type.split(";", 2)[0].trim()
                    .toLowerCase(Locale.ROOT).equals("audio/mpeg")) {
                throw new IOException("Unexpected station response (HTTP " + code + ")");
            }
            try (InputStream audio = source.getInputStream()) {
                int length = audio.read(buffer);
                if (length < 0) throw new IOException("The station sent no audio");
                status.update("Receiving Cairo Quran Radio", null);
                reply(output, "200 OK", "audio/mpeg", false);
                sentHeaders = true;
                while (length >= 0) {
                    try {
                        output.write(buffer, 0, length);
                    } catch (IOException disconnected) {
                        return;
                    }
                    length = audio.read(buffer);
                }
            }
        } catch (IOException error) {
            status.update("Station connection failed. See the relay log.", error);
            if (!sentHeaders) reply(output, "502 Bad Gateway", "text/plain", true);
        } finally {
            source.disconnect();
        }
    }

    private static String readLine(InputStream input, int limit) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            int next = input.read();
            if (next == -1) return null;
            if (next == '\n') return line.toString();
            if (next != '\r') line.append((char) next);
        }
        throw new IOException("HTTP line too long");
    }

    private static void reply(OutputStream output, String status, String type, boolean empty)
            throws IOException {
        String headers = "HTTP/1.0 " + status + "\r\nContent-Type: " + type
                + "\r\nCache-Control: no-store\r\nConnection: close\r\n"
                + (empty ? "Content-Length: 0\r\n" : "") + "\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
    }

    private void release(Socket client) {
        synchronized (clients) { clients.remove(client); }
        try { client.close(); } catch (IOException ignored) { }
    }

    @Override public void close() {
        synchronized (clients) {
            closed = true;
            try { listener.close(); } catch (IOException ignored) { }
            for (Socket client : clients) {
                try { client.close(); } catch (IOException ignored) { }
            }
            clients.clear();
        }
        workers.shutdownNow();
    }
}
