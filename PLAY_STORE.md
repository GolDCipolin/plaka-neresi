# Publishing to Google Play

What the project already satisfies, and what is left for you. Everything in the
**"You must do this"** sections needs your account or your credentials — I can't do them
for you.

---

## Already handled in the project

| Requirement | Status |
|---|---|
| Target API 36 (mandatory for new apps from **31 Aug 2026**) | `targetSdk = 36` |
| App Bundle (`.aab`) — APKs are not accepted for new apps | `gradlew bundleRelease` |
| Minification + resource shrinking | enabled on `release` |
| Signing wired without committing secrets | reads `keystore.properties` |
| 64-bit support | Kotlin/Compose only, no native code |
| Permissions kept minimal | `INTERNET` only |

---

## 1. You must do this: create the upload keystore

I will not generate this — it needs a password, and signing credentials should only ever
exist on your machine. Run it yourself:

```bash
keytool -genkeypair -v -keystore plakaneresi-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

> **Back this file up somewhere safe, today.** If you lose it you cannot ship updates to
> your own app ever again. Play Signing lets you request a reset, but it is a slow manual
> appeal and not something to rely on. Keep a copy off this machine.

Then create `keystore.properties` in the project root — it is gitignored:

```properties
storeFile=C:/Users/ertur/keys/plakaneresi-upload.jks
storePassword=...
keyAlias=upload
keyPassword=...
```

Verify it signs:

```bash
gradlew bundleRelease
```

Output lands at `app/build/outputs/bundle/release/app-release.aab`.

---

## 2. You must do this: swap the AdMob IDs

[app/src/main/res/values/ads.xml](app/src/main/res/values/ads.xml) currently holds
Google's **test** IDs. Replace both with your own from the AdMob console before you ship.

Do **not** ship with test IDs (no revenue), and do **not** run your real IDs on your own
device during development — Google counts that as invalid traffic and account
suspensions are effectively final. Register your phone as a test device instead.

---

## 3. You must do this: a privacy policy

Non-negotiable once an ads SDK is present. It needs to be a public URL, live before you
submit, and reachable from the store listing.

It has to disclose that Google AdMob collects the advertising ID and device information
for ad personalisation. A generated policy from a reputable generator is acceptable —
just make sure it actually names AdMob.

---

## 4. You must do this: Data safety form

In Play Console. With AdMob in the app you declare, at minimum:

- **Device or other IDs — collected**, shared with third parties, used for Advertising.
- Data is **encrypted in transit** (AdMob uses HTTPS).
- Users **cannot** request deletion of ad IDs through the app.

Do not declare "no data collected". That is the single most common cause of a rejected
first submission for an ad-supported app.

---

## 5. Consider: EEA consent (UMP)

Turkey is not in the EEA, but Play judges by **where the user is**, not where you are.
If anyone in the EU/UK installs it, you need a consent flow before serving personalised
ads, via Google's User Messaging Platform SDK.

Two honest options:

1. Add the UMP SDK and show a consent form. Correct, more work.
2. Restrict distribution to Turkey in Play Console, sidestepping it for now.

Not wired up yet — tell me which and I'll do it.

---

## 6. Store listing assets

I can't produce binary images. Sizes required:

| Asset | Spec |
|---|---|
| App icon | 512 × 512 PNG, 32-bit, no transparency |
| Feature graphic | 1024 × 500 PNG/JPG |
| Phone screenshots | 2–8, min 320 px on the short side |
| Short description | ≤ 80 characters |
| Full description | ≤ 4000 characters |

The icon can be exported from
[ic_launcher_foreground.xml](app/src/main/res/drawable/ic_launcher_foreground.xml) —
in Studio, right-click `res` → *New* → *Image Asset*, or open the vector and export at
512 px. Remember the 512 icon must be **square with no transparency**, so put the plate
on the `#DCE5FA` background rather than exporting the foreground alone.

Draft listing text, if useful:

> **Short:** 48 neresi? Türkiye'nin 81 ilinin plaka kodu, çevrimdışı.
>
> **Full:** Gördüğünüz plakanın hangi ilden olduğunu anında öğrenin. 48 yazın, Muğla
> çıksın. İl adından da arayabilirsiniz — "Muğla" ya da Türkçe karakter olmadan "mugla".
> Tüm veriler uygulamanın içinde: internet bağlantısı gerekmez.

---

## 7. Release checklist

- [ ] `versionCode` incremented (Play rejects a reused one — it is currently `1`)
- [ ] `versionName` updated
- [ ] Real AdMob IDs in `ads.xml`
- [ ] `gradlew bundleRelease` succeeds and is signed
- [ ] **Installed and launched the *release* build**, not a debug one. R8 runs only in
      release, and it has already broken this app once by stripping a class WorkManager
      loads reflectively. Debug runs and unit tests cannot catch that class of bug — see
      "Debug builds cannot validate a release" in the README for the one-liner that
      signs a release APK with the debug key so you can test it on an emulator.
- [ ] Privacy policy URL live
- [ ] Data safety form completed
- [ ] Content rating questionnaire done
- [ ] Countries selected

---

## A note on the first submission

New personal developer accounts created after Nov 2023 must run a **closed test with 12
testers for 14 continuous days** before production access is granted. Plan for that — it
is the step that surprises people, and it means your first public release is at least two
weeks after you are otherwise ready.
