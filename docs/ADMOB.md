# AdMob configuration

Official setup guide: [Get started with AdMob on Android](https://developers.google.com/admob/android/quick-start).

## Debug builds

Debug builds use Google’s official sample identifiers only:

| Item | Value |
|------|--------|
| Test App ID | `ca-app-pub-3940256099942544~3347511713` |
| Test banner ad unit ID | `ca-app-pub-3940256099942544/6300978111` |

They are set in `app/build.gradle` for the `debug` build type:

- `manifestPlaceholders.admobAppId` is merged into `AndroidManifest.xml` as `com.google.android.gms.ads.APPLICATION_ID`
- `resValue` `admob_banner_unit_id` is what `BannerAdController` loads

The banner is an anchored adaptive banner. Its width is the playfield container width (the current window width if the container has not been laid out yet). It sits in `R.id.ad_view_container` below `GameView`, so it does not cover bubbles, the aim guide, the score, or the pause control. If the request fails or there is no network, the strip is hidden and the game keeps running.

The Mobile Ads SDK is initialized from `GameActivity` only after UMP consent finishes (`ConsentHelper`). A second request is not started while one banner is already attached. `onPause`, `onResume`, and `onDestroy` forward to the `AdView`.

Do not click test ads or generate artificial impressions.

## Production IDs

The publisher line in `app-ads.txt` is not an App ID and not a banner unit ID.

This repository does not contain a production App ID or banner unit ID. Do not invent them. Supply the IDs you create in AdMob, using either mechanism:

`local.properties` (gitignored):

```properties
ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
ADMOB_BANNER_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
```

Or the same names as environment variables or Gradle properties.

Release packaging (`packageRelease` and `bundleRelease`) fails if those values are missing or if they are Google sample IDs (`3940256099942544`). Until they are set, the release variant compiles with unset sentinels (`ca-app-pub-0000000000000000~0000000000` and `ca-app-pub-0000000000000000/0000000000`) so unit tests can run. Those sentinels are not production IDs and cannot be packaged.

## app-ads.txt

Both `app-ads.txt` and `website/app-ads.txt` contain only:

```text
google.com, pub-9933683934248674, DIRECT, f08c47fec0942fa0
```

Publish that file at the root of the developer website listed on the Play store listing:

`https://YOUR-DEVELOPER-DOMAIN/app-ads.txt`

GitHub repository URLs are not a developer website. This environment has not deployed the file and has not verified it. After the file is public, wait for Google’s crawler, then check the app’s app-ads.txt status in AdMob. Do not treat the listing as verified until AdMob says so.
