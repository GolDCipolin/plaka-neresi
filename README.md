# Plaka Neresi

Android app that turns a Turkish licence-plate code into a province. Type `48`, get
Muğla. Type `mugla` (or `MUĞLA`, or `48`), get the same answer. The whole province table
is compiled into the APK, so the lookup itself works with no network at all — the only
thing that needs connectivity is the ad banner.

## Status

- Code ↔ province lookup, browsable list of all 81 provinces, Turkish UI
- A **Detaylar** action in the top bar opening a details sheet: version, province count,
  and a **light / dark / system** theme switch that persists
- Anchored adaptive AdMob banner — **currently using Google's test ad unit IDs**
- Release signing wired for Play; see [PLAY_STORE.md](PLAY_STORE.md)

Voice input ("48 neresi") and the map view are still not in — see
[Where voice would slot in](#where-voice-would-slot-in).

The app declares one permission, `INTERNET`, purely for ads. The ads SDK also merges in
`AD_ID`, which has to be declared on Play's Data safety form.

## Building

Verified on this machine: `gradlew test assembleDebug` → BUILD SUCCESSFUL, 27 unit tests
passing, `app-debug.apk` at ~9.3 MB.

Open the folder in Android Studio and press Run, or from a terminal:

```bash
gradlew assembleDebug
```

### Toolchain notes

There are three JDKs on this machine and only one of them can build the project:

| JDK | Where | Works? |
|---|---|---|
| 24 | on `PATH` | no — AGP does not support it |
| 25 | Android Studio's bundled JBR | no — Gradle 8.9 does not run on 25 |
| 21 | `C:\Users\ertur\.jdks\jbr-21.0.11` | **yes** |

Studio worked this out on its own and pinned Gradle to the JDK 21. For command-line
builds you have to set `JAVA_HOME` to it explicitly, or the JDK 24 on `PATH` wins and
the build fails:

```bash
JAVA_HOME='C:\Users\ertur\.jdks\jbr-21.0.11' gradlew test
```

- AGP 9.3.0 / Kotlin 2.3.21 / Gradle 9.5.0, compileSdk 37, targetSdk 36, minSdk 26.
- `minSdk 26` is what lets the launcher icon be pure XML (adaptive icon, no PNGs).
- `targetSdk 36` is not a preference — Google Play requires it for new submissions from
  **31 Aug 2026**, and AdMob 25.x needs compileSdk 36+. That is what forced the move off
  AGP 8.7.3.
- **AGP 9 has built-in Kotlin support**, so `org.jetbrains.kotlin.android` must *not* be
  in the `plugins` block — applying it is a hard build failure. The Compose compiler
  plugin is still applied separately. `android.kotlinOptions` is gone too; AGP aligns the
  Kotlin JVM target with `compileOptions` on its own.

## Running the tests

All logic is plain JVM code with no Android dependencies, so the unit tests run without
an emulator:

```bash
gradlew test
```

## Debug builds cannot validate a release

R8 only runs in the release variant, so a minified-only crash is invisible to every debug
run and to all 27 unit tests. This bit once already: the ads SDK pulls in WorkManager,
WorkManager stores state in Room, Room loads its generated `WorkDatabase_Impl` by name,
and R8 stripped it — the app died in `androidx.startup.InitializationProvider` before any
of our code ran. Keep rules are in [proguard-rules.pro](app/proguard-rules.pro).

To check a release build on an emulator without the real signing key, sign it with the
debug key:

```bash
gradlew assembleRelease
apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android --ks-key-alias androiddebugkey --key-pass pass:android --out release-test.apk app/build/outputs/apk/release/app-release-unsigned.apk
adb install -r release-test.apk
```

Do this before every Play upload.

Test with **3-button navigation**, not just gesture nav — the taller navigation bar is
what exposed a missing inset that had hidden the details row entirely on a Galaxy A57:

```bash
adb shell cmd overlay enable com.android.internal.systemui.navbar.threebutton
```

## Running it on a device

No emulator has been created and no phone is currently attached (`adb devices` is empty).
Either works:

- **A physical phone.** Settings → About phone → tap *Build number* seven times, then
  Developer options → USB debugging. Plug in, accept the prompt, pick it in Studio.
- **An emulator.** Device Manager → Add. Costs a ~1–1.5 GB system image download.

To install a built APK directly without Studio:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## How it is put together

```
plates/          the whole domain, no Android imports — directly unit-testable
  Province.kt      one province; caches its folded name for search
  Provinces.kt     the 81-row table + Turkish-collated alphabetical order
  TurkishText.kt   text folding and tokenising
  PlateSearch.kt   query -> ranked hits
ui/              Compose; knows nothing about how matching works
  PlateGraphics.kt  the plate visual, reused as list bullet and as the big answer
  HomeScreen.kt     search field, result card, browse list, ad slot
  DetailsSheet.kt   top-bar Detaylar action and the details bottom sheet
  AdBanner.kt       AdView wrapped for Compose
  PlateViewModel.kt query + sort state
  theme/            colour schemes and ThemeMode
settings/
  SettingsStore.kt  the one persisted preference (theme), on SharedPreferences
```

### The part that is actually tricky: Turkish text

`String.lowercase()` cannot be used anywhere near this data.

- Under a Turkish locale it maps `I` → `ı`, so `"ISPARTA"` stops matching `"Isparta"`.
- Under the root locale it maps `İ` → `i` + U+0307 (a combining dot), so `"İZMİR"`
  becomes a **7**-character string that never equals the 5-character `"izmir"`.

Either way the app silently fails for a tourist typing `izmir` on an English keyboard —
which is the main use case. `TurkishText.fold` maps every character explicitly instead,
so results are identical regardless of the phone's system language. `ç ğ ı i İ ö ş ü`
and the circumflex vowels all fold to their ASCII lookalikes.

Sorting has the mirror-image problem: plain `String` comparison files *Çanakkale* after
*Zonguldak*, because `Ç` is U+00C7. `Provinces.alphabetical` uses `java.text.Collator`
with a Turkish locale so the list reads correctly (`... c, ç, d ...`, `ı` before `i`).

### Matching rules

A query of only digits is treated as a code; anything else as a name. Results are ranked:

| Rank | Kind | Example |
|---|---|---|
| 1 | exact code | `48` → Muğla |
| 2 | code prefix | `4` → Ağrı (04) first, then 40–49 |
| 3 | name prefix | `mu` → Muğla before Gümüşhane |
| 4 | name contains | `hisar` → Afyonkarahisar |
| 5 | alias | `icel` → Mersin, `urfa` → Şanlıurfa |

Aliases cover old official names (İçel, Dersim) and everyday short forms (Antep, Urfa,
Maraş, İzmit, Adapazarı). They are searchable but never displayed.

Multi-word queries are matched piecewise, so `K. Maraş` and `kahraman maras` both reach
Kahramanmaraş even though the stored name is one unbroken run of letters.

## Where voice would slot in

Nothing in `plates/` assumes a keyboard — `PlateSearch.query` takes free text, and
`TurkishText.tokens` already discards filler. Adding "48 neresi" later means:

1. `SpeechRecognizer` with `tr-TR` for input. Note this is **online by default**; true
   offline needs `createOnDeviceSpeechRecognizer` (API 33+) and the user downloading the
   Turkish on-device pack, so typing has to stay the primary path regardless.
2. Strip the question words (`neresi`, `nere`, `plakası`) before searching — the tokeniser
   makes this a filter over `tokens`, not a parsing problem.
3. `TextToSpeech` with `Locale("tr", "TR")` to read the answer back.

The province table and the matching would not change.
