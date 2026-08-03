# FitTrack (Android)

Capacitor-wrapped Android app for the FitTrack fitness tracker.
The web source (`www/`) is packaged into a native WebView shell — 100% code reuse, cloud sync unchanged.

## Structure
- `www/` — web app source (edit here). Chart.js & XLSX are vendored locally so the app works offline.
- `android/` — native Android project. Open this folder in Android Studio.
- `capacitor.config.json` — appId `com.fittrack.app`, appName `FitTrack`, webDir `www`.

## Build & run (on your machine)
1. Install Android Studio + JDK 17 + Android SDK 36.
2. Open `android/` in Android Studio, let it sync Gradle.
3. Connect a device (USB debugging) or start an emulator.
4. Click ▶ Run 'app'.

## Release
Build → Generate Signed Bundle / APK → APK. Version via `versionCode`/`versionName` in `android/app/build.gradle`.

## Update web assets after editing `www/`
```bash
npm install
npx cap sync android
```

## Notes
- Permissions: INTERNET only (cloud sync).
- minSdk 24 (Android 7.0), targetSdk 36.
- Native splash screen included (no white flash on cold start).
- Cloud sync connects directly to 坚果云 WebDAV via a native plugin (`WebDavPlugin.java`, `HttpURLConnection`) — no Cloudflare Worker needed. Credentials (email + app password) are stored on device.
