#!/usr/bin/env python3
"""Pass Cairo Quran Radio to an Android frame over an ADB USB reverse port."""

from http.client import HTTPException, parse_headers
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from shutil import which
from subprocess import PIPE, Popen, TimeoutExpired


HOST = "127.0.0.1"
PORT = 8766
RADIO_PATH = "/cairo.mp3"
UPSTREAM = "https://radio.xecod.com/station/quran-cairo"


class RadioHandler(BaseHTTPRequestHandler):
    def valid_request(self):
        host = self.headers.get("Host")
        if host and host not in (f"{HOST}:{PORT}", f"localhost:{PORT}"):
            self.send_error(400, "Use the loopback address")
            return False
        if self.path != RADIO_PATH:
            self.send_error(404, "Unknown radio path")
            return False
        return True

    def send_audio_headers(self):
        self.send_response(200)
        self.send_header("Content-Type", "audio/mpeg")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Connection", "close")
        self.end_headers()

    def do_HEAD(self):
        if self.valid_request():
            self.send_audio_headers()

    def do_GET(self):
        if not self.valid_request():
            return
        self.connection.settimeout(15)
        try:
            source = Popen([
                "curl", "--disable", "--silent", "--show-error", "--fail",
                "--no-buffer", "--include", "--suppress-connect-headers",
                "--proto", "=https", "--connect-timeout", "8",
                "--speed-limit", "100", "--speed-time", "12", UPSTREAM,
            ], stdout=PIPE)
        except OSError as error:
            self.log_error("Upstream HTTPS connection failed: %s", error)
            self.send_error(502, "Could not reach Cairo Quran Radio")
            return
        try:
            try:
                status = source.stdout.readline(512).split()
                headers = parse_headers(source.stdout)
                if status[1:2] != [b"200"] or headers.get_content_type() != "audio/mpeg":
                    self.send_error(502, "Unexpected response from Cairo Quran Radio")
                    return
            except (OSError, HTTPException) as error:
                self.log_error("Upstream HTTPS connection failed: %s", error)
                self.send_error(502, "Could not reach Cairo Quran Radio")
                return
            try:
                self.send_audio_headers()
                while True:
                    chunk = source.stdout.read1(4096)
                    if not chunk:
                        break
                    self.wfile.write(chunk)
            except (BrokenPipeError, ConnectionResetError):
                pass  # The frame stopped playback or switched feeds.
            except OSError as error:
                self.log_error("Radio connection interrupted: %s", error)
        finally:
            source.stdout.close()
            source.terminate()
            try:
                source.wait(timeout=2)
            except TimeoutExpired:
                source.kill()
                source.wait()


if __name__ == "__main__":
    if not which("curl"):
        raise SystemExit("curl is required to connect to the live radio feed.")
    with ThreadingHTTPServer((HOST, PORT), RadioHandler) as server:
        print(f"Cairo Quran Radio relay ready at http://{HOST}:{PORT}{RADIO_PATH}", flush=True)
        print("Keep this window open while listening. Press Ctrl+C to stop.", flush=True)
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            pass
