# AdMob configuration

## Development (current defaults)

This project uses **Google’s official sample / test IDs** so you can develop safely:

| Item | Value |
|------|--------|
| Test App ID | `ca-app-pub-3940256099942544~3347511713` |
| Test anchored adaptive banner unit | `ca-app-pub-3940256099942544/9214589741` |

Configured in:

- `app/build.gradle` → `manifestPlaceholders.admobAppId` and `resValue` for `admob_banner_unit_id`
- `AndroidManifest.xml` → `com.google.android.gms.ads.APPLICATION_ID`

Do **not** click test ads repeatedly or generate artificial traffic.

## Production setup (manual)

1. Create an Android app in [AdMob](https://admob.google.com/) and copy the real **App ID**.
2. Create an **Anchored adaptive banner** ad unit and copy the real **ad unit ID**.
3. In `app/build.gradle`:
   - Set `manifestPlaceholders.admobAppId` to your App ID.
   - Set the **release** `resValue "string", "admob_banner_unit_id", "..."` to your banner unit ID.
4. Keep debug builds on test IDs.
5. Never invent App IDs or unit IDs.

Publisher record supplied for app-ads.txt (not an App ID / unit ID):

```text
google.com, pub-9933683934248674, DIRECT, f08c47fec0942fa0
```

## Consent / privacy

`ConsentHelper` uses Google’s User Messaging Platform (UMP) before ads initialize.
If you target children or a Families audience, configure child-directed treatment and Play policies before release.

## Banner placement

Adaptive banners load into `R.id.ad_view_container` **below** the playable `GameView`, so ads never cover bubbles, aim guides, score, or controls. Load failures hide the ad strip; gameplay continues offline.
