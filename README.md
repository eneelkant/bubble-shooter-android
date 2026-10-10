# Bubble Shooter Android

A customizable Java Bubble Shooter for Android with a modern portrait UI, level progression, local high scores, and Google Mobile Ads (AdMob) test integration.

## Overview

- **Platform:** Android (minSdk 24, targetSdk 36)
- **Language:** Java
- **Build:** Android Gradle Plugin 8.13.2 / Gradle 8.13
- **Gameplay:** Aim and shoot bubbles, match 3+ of the same color, drop floating groups, clear the board before shots run out
- **Monetization:** Anchored adaptive banner via Google Mobile Ads SDK (test IDs in development)

## License and attribution

This repository is licensed under the **Apache License 2.0** (see [LICENSE](LICENSE) and [NOTICE](NOTICE)).

The destination repo was seeded for a Bubble Shooter project. The open-source reference game [panthitech/BubbleShooter](https://github.com/panthitech/BubbleShooter) is also Apache 2.0 and permits commercial modification and redistribution under that license. After reviewing that project’s license, code structure, and branded art (animal/panda theme), this codebase ships an **original** playable implementation with original canvas graphics and UI so third-party branding and proprietary look-and-feel are not copied.

## Android Studio setup

1. Install Android Studio (a current stable) with Android SDK 36 and Build-Tools 35.
2. `git clone https://github.com/eneelkant/bubble-shooter-android.git`
3. Checkout `main`.
4. Open the project root in Android Studio and let Gradle sync.
5. Run on an emulator or device (portrait phone recommended).

### Command-line build

```bash
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew test
./gradlew assembleDebug
./gradlew lint
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Testing

Unit tests cover grid matching, floating-bubble drops, shooting/pause rules, and level scaling:

```bash
./gradlew test
```

Manual device checks (emulator or phone) still required for:

- Touch aiming and animation smoothness
- System insets / display cutouts
- AdMob banner load, failure, and offline play
- Activity pause/resume lifecycle with ads

## Gameplay controls

- **Drag** on the playfield to aim (dotted guide)
- **Release** to shoot
- **Pause** button: resume, restart level, or quit
- Clear all bubbles to advance; running out of shots or filling the bottom row ends the run
- High score and best level are stored in SharedPreferences

## AdMob (development)

Debug builds use Google’s sample App ID `ca-app-pub-3940256099942544~3347511713` and sample banner unit `ca-app-pub-3940256099942544/6300978111`. See [docs/ADMOB.md](docs/ADMOB.md).

Production App ID and banner unit ID are not in this repository. Set `ADMOB_APP_ID` and `ADMOB_BANNER_UNIT_ID` for release builds. Do not invent IDs. Release packaging rejects sample IDs.

Privacy: UMP consent is requested before ads initialize.

## app-ads.txt

Exact publisher line (also in repo root and `website/app-ads.txt`):

```text
google.com, pub-9933683934248674, DIRECT, f08c47fec0942fa0
```

**Important:** GitHub repository URLs are **not** an AdMob developer-website domain. No Play Console developer website was configured in this project environment.

### Deploy manually

1. Host a site you control (the domain listed as the developer website in Google Play / AdMob).
2. Publish the file at the site root:

   `https://YOUR-DEVELOPER-DOMAIN/app-ads.txt`

3. Wait for Google’s crawler and verify status in AdMob.
4. Do not assume verification until AdMob shows it.

## Google Play release prerequisites (remaining)

The app targets API 36, which is what Play requires for new apps and updates as of 31 August 2026. It is not production-ready. See [docs/PLAY_RELEASE.md](docs/PLAY_RELEASE.md).

- Provide production AdMob App ID and banner unit ID (`ADMOB_APP_ID`, `ADMOB_BANNER_UNIT_ID`)
- Deploy `app-ads.txt` to your developer domain and confirm AdMob has crawled it
- Create a Play upload keystore and supply it via `ANDROID_KEYSTORE_*` (never commit it)
- No signed AAB is produced until those values exist
- Set privacy policy URL, store listing, screenshots, content rating, Data safety, and the ads declaration
- If children/Families: configure child-directed ads / UMP accordingly
- Manual QA on physical devices across screen sizes
- Confirm the AdMob app is linked to Play

## Project layout

```text
app/src/main/java/com/eneelkant/bubbleshooter/
  game/     # Pure Java engine (unit-tested)
  ui/       # Menu, GameActivity, GameView
  ads/      # Banner, consent, high-score prefs
app-ads.txt
docs/ADMOB.md
docs/PLAY_RELEASE.md
website/app-ads.txt   # copy for developer-site deployment
```
