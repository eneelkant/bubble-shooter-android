# Google Play release checklist

This app is not published and is not production-ready. Nothing in this repository uploads to Google Play.

Checked against Google Play’s target API policy (Play Console Help, “Target API level requirements,” in force 31 August 2026):

- New apps and updates must target Android 16 (API 36) or higher to be submitted.
- An extension, if Play Console offers one for this app, runs to 1 November 2026.
- This project sets `compileSdk 36` and `targetSdk 36`.

## Application identity

| Field | Value |
|-------|--------|
| Application ID | `com.eneelkant.bubbleshooter` |
| versionCode | `1` |
| versionName | `1.0.0` |
| minSdk | `24` |
| targetSdk / compileSdk | `36` |
| Language | Java 17 |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.13 |

Android 16 ignores fixed portrait orientation on large screens (smallest width of about 600dp). Phone layouts stay portrait because each activity requests `screenOrientation="portrait"`. Check a tablet or foldable before release.

## What this environment verified

- `./gradlew test`, `./gradlew assembleDebug`, and `./gradlew lint` on the debug variant.
- Debug builds keep Google’s sample App ID and banner unit ID.
- `assembleRelease` / `bundleRelease` stop before packaging when production AdMob IDs or an upload keystore are missing.

No signed Android App Bundle was generated here. `ADMOB_APP_ID`, `ADMOB_BANNER_UNIT_ID`, `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD` were not supplied. Do not commit a keystore, passwords, or production ad IDs.

## Signing an upload key

Create the keystore outside the repository:

```bash
keytool -genkeypair -v \
  -keystore upload-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

Point the release build at it without committing the file:

```bash
export ANDROID_KEYSTORE_PATH=/absolute/path/upload-keystore.jks
export ANDROID_KEYSTORE_PASSWORD='...'
export ANDROID_KEY_ALIAS=upload
export ANDROID_KEY_PASSWORD='...'
export ADMOB_APP_ID='ca-app-pub-...'
export ADMOB_BANNER_UNIT_ID='ca-app-pub-...'
./gradlew bundleRelease
```

The bundle is written to `app/build/outputs/bundle/release/app-release.aab` only after those checks pass. Enroll in Play App Signing and keep the upload key offline.

## Still required before a Play submission

- Production AdMob App ID and anchored adaptive banner unit ID, created in AdMob and passed in as above. Confirm the packaged release manifest does not contain `3940256099942544`.
- Deploy `app-ads.txt` to `https://YOUR-DEVELOPER-DOMAIN/app-ads.txt` and wait until AdMob shows the file as verified. The line to publish is `google.com, pub-9933683934248674, DIRECT, f08c47fec0942fa0`.
- Play Console app, store listing, icon, feature graphic, phone screenshots, and a short description.
- Privacy policy URL hosted on a site you control. The app uses AdMob and UMP and stores high score and best level in app-private SharedPreferences.
- Data safety form: advertising identifiers and on-device game progress. Declare that ads are present.
- Content rating questionnaire and target audience. If the app is for children or in the Families program, configure child-directed treatment and UMP before release. That is not configured now.
- Ads declaration in Play Console, and link the AdMob app to the Play listing.
- Physical-device QA for touch aiming, cutouts, pause/resume, and offline play. An emulator run does not replace that.
- Internal testing track before production. Do not submit until you explicitly choose to upload.
