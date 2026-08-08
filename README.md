# FitTrack (Android)

Capacitor-wrapped Android app for the FitTrack fitness tracker.
The web source (`www/`) is packaged into a native WebView shell — 100% code reuse, cloud sync unchanged.

## Structure
- `www/` — web app source (edit here). Chart.js & XLSX are vendored locally so the app works offline.
- `android/` — native Android project. Open this folder in Android Studio.
- `capacitor.config.json` — appId `com.fittrack.app`, appName `FitTrack`, webDir `www`.
- `.github/workflows/build.yml` — GitHub Actions CI that builds the debug APK in the cloud (no local toolchain needed).

## Prerequisites
| Tool | Version | Needed for |
|------|---------|-----------|
| Android Studio | latest (Hedgehog or newer) | local build / run / release |
| JDK | 17 | Gradle build (bundled with Android Studio) |
| Android SDK | Platform 36 + Build-Tools 36.0.0 | compilation |
| Node.js | 18+ (22 used here) | only when updating web assets (`npx cap sync`) |

> You do **not** need to install the Capacitor CLI locally — the native project under `android/` is already generated.

## Build & run (local, in Android Studio)
1. Open `android/` in Android Studio (`File → Open` → select the `android` folder).
2. Let Gradle sync and download dependencies (first launch needs network; a few minutes).
3. Connect a device (enable USB debugging) or start an emulator (`Device Manager`).
4. Click ▶ `Run 'app'`. It compiles and installs FitTrack on the device.

## Build a release APK / AAB (for distribution)
`Run` produces a debug build (not signable for stores). For release:
1. `Build → Generate Signed Bundle / APK` → choose **APK** (or **Bundle** for Play Store).
2. Create a keystore on first run — **keep the password and alias safe; losing them means you can no longer update the app**.
3. Output: `android/app/build/outputs/apk/release/app-release.apk`.
4. Bump `versionCode` / `versionName` in `android/app/build.gradle` before each release.

## Update web assets (after editing `www/`)
```bash
npm install            # first time only
npx cap sync android   # copies www/ into the native app assets
```
Then re-run / rebuild in Android Studio. (`cap sync` must run where Node is available — your own machine, not the sandbox.)

## Build the APK in the cloud (GitHub Actions — no local install)
A CI workflow (`.github/workflows/build.yml`) builds the debug APK on GitHub's servers on every push to `main`/`master`:
1. Push your changes.
2. Open the repo → **Actions** tab → `Build FitTrack APK` runs (≈3–8 min).
3. When green, open the run → download the **`fittrack-apk`** artifact (zip) → `app-debug.apk`.
4. Install on a phone (enable "Install unknown apps" on first install).

> Because CI runs `npm install` + `npx cap sync android` automatically, **after editing `www/` you only need to `git push`** — no local commands required to get a fresh APK.

## Build a versioned APK & share it (GitHub Release — anyone can download)
A second workflow (`.github/workflows/release.yml`) builds the APK and publishes it as a **GitHub Release asset** — a public, permanent download link that works for anyone (no GitHub login needed, unlike Actions artifacts which expire and require repo access).

**Trigger (pick one):**
- **Manual:** Actions → `Release FitTrack APK` → **Run workflow** → enter version e.g. `1.0.0` → run.
- **By tag:** `git tag v1.0.0 && git push origin v1.0.0`.

What happens:
1. CI builds `app-debug.apk` and stamps the app's `versionName` to the version you entered.
2. It creates a Release `v1.0.0` with asset `FitTrack-v1.0.0.apk`.

**Download / share:**
- Releases page: `https://github.com/meegaday/trainme/releases` (click the version → download `FitTrack-vX.X.X.apk`).
- Direct link (send this to anyone): `https://github.com/meegaday/trainme/releases/download/v1.0.0/FitTrack-v1.0.0.apk`

> **Public download requires a public repo.** If your repo is private, only collaborators can fetch the asset. Make it public at Settings → General → Change visibility → Public (this also exposes the source — your call).

## Notes
- Permissions: `INTERNET` only (cloud sync).
- `minSdk 24` (Android 7.0), `targetSdk 36`.
- Native splash screen included (no white flash on cold start).
- Cloud sync connects directly to 坚果云 (Jianguoyun) WebDAV via a native plugin (`WebDavPlugin.java`, `HttpURLConnection`) — no Cloudflare Worker needed. Credentials (email + app password) are stored on device.
- Detailed Chinese guides in this repo: `安卓出包指南.md` (local build & release) and `GitHub构建APK指南.md` (CI build & download).
