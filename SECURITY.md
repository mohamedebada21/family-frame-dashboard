# Security policy

This is a local household dashboard, not an authenticated multi-user service. It has not had an independent security audit. Version 7.1 adds schema validation to dashboard import and startup loading to prevent saved identifiers, colors and numeric fields from becoming executable markup.

## Reporting a vulnerability

If enabled on the repository, use GitHub's **Security → Report a vulnerability** to report privately. Do not put real family backups, credentials or exploit details in public issues. If private reporting is unavailable, open an issue asking the maintainer to enable it without disclosing vulnerability details.

## Data and network boundaries

- Events, tasks, photos and settings are kept in browser localStorage, not encrypted by this app. Anyone with access to the unlocked frame can view or change them.
- Weather/geocoding requests send selected coordinates or a search query to Open-Meteo. Prayer requests send the fixed Queens coordinates and calculation parameters to AlAdhan. These are HTTPS requests.
- An optional HTTPS adhan URL contacts its chosen host. Prefer a local MP3; do not put credentials or private tokens in audio URLs.
- Tapping Play radio connects over HTTPS to either the Cairo Quran Radio MP3 relay at `radio.xecod.com` or the broadcaster's HLS feed at `webvideocore.net`. The official station link opens `misrquran.gov.eg`.
- The optional USB relay binds to the Mac's loopback interface and serves only `/cairo.mp3`. The frame uses HTTP to its own loopback port over USB. The Mac fetches the fixed radio URL with normal HTTPS certificate checks and rejects redirects.
- The Android companion listens on `127.0.0.1:8767` on the frame and serves the same fixed audio path. It opens the fixed upstream URL with Android's HTTPS stack and default certificate and hostname verification. The local HTTP audio endpoint is available to apps on the frame and is bound to loopback only.
- There is no account login, cloud data store, analytics or automatic calendar synchronization in this source.
- Browser/device backups may contain personal information. Keep those out of this repository even if `.gitignore` is present.
- The code retains inline handlers for its old WebView target. It does not claim strict Content Security Policy enforcement. Schema validation and output escaping reduce injection risk but are not a substitute for an updated browser.

## Device precautions

Use a currently supported Android device for new installations where possible. Updating this HTML or installing a root certificate does not patch Android or WebView vulnerabilities. Security updates depend on the device vendor.

For the legacy frame, use an isolated guest/IoT Wi-Fi network where available, keep sensitive accounts off the device, keep certificate validation enabled, and disable USB debugging after setup when you no longer need it. Do not expose ADB or Fully remote administration to the internet. Keep Fully's JavaScript/native integration and remote administration disabled unless a separately reviewed feature needs them; this app does not require them.

## Maintainer checklist (must be configured on GitHub)

Creating these files does not enable repository settings. After repository creation:

1. Enable private vulnerability reporting in repository Security settings.
2. Enable available secret scanning/push protection. Never commit account tokens or private backups.
3. Add a rule for `main` blocking force pushes/deletion and requiring pull requests before merges. Require a second review when a second maintainer is available; do not accidentally make a solo project impossible to update.
4. Keep collaborator permissions minimal. Enable two-factor authentication on the owner account.
5. Review changes and run both test files before merging. If adding CI later, use read-only default token permissions, pinned action commits and no secrets on untrusted pull requests.

Sources: [GitHub private reporting](https://docs.github.com/en/code-security/how-tos/report-and-fix-vulnerabilities/configure-vulnerability-reporting/configure-for-a-repository), [GitHub branch protection](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches), [Android security updates](https://source.android.com/docs/security/overview/updates-resources).
