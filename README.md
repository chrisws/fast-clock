# FastClock

A fasting clock app for tracking intermittent fasting on a 24-hour circular clock.

## Project Structure

```
fast-clock/
├── README.md              ← you are here
├── LICENSE
├── prototype/             ← single-file HTML/CSS/JS prototype (design + logic reference)
│   ├── index.html
│   └── README.md
└── android/               ← Kotlin + Jetpack Compose production app
    ├── app/
    │   ├── build.gradle
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       └── java/com/fastclock/
    │           ├── MainActivity.kt
    │           ├── model/
    │           │   ├── Protocol.kt
    │           │   └── FastState.kt
    │           └── ui/
    │               ├── theme/
    │               │   └── Theme.kt
    │               ├── clock/
    │               │   └── ClockCanvas.kt
    │               └── FastClockScreen.kt
    ├── build.gradle
    ├── settings.gradle
    └── gradle.properties
```

## Prototype

Open `prototype/index.html` in any browser. No build step, no dependencies.

See [prototype/README.md](prototype/README.md) for full details.

## Android App

Built with **Kotlin + Jetpack Compose**. The HTML prototype serves as the design and logic reference.

### Key decisions

| Concern | Approach |
|---|---|
| Clock rendering | Custom `Canvas` composable (replaces SVG) |
| State persistence | `SharedPreferences` for protocol/streak, `Room` for fast history |
| Notifications | `NotificationManager` + `WorkManager` for eating-window alerts |
| Theming | Compose `ColorScheme` mapped from CSS custom properties |
| Offline-first | All logic local, no network calls |

### Build

```bash
cd android
./gradlew assembleDebug
```

Requires Android Studio (Hedgehog or later) and JDK 17.

## License

See [LICENSE](LICENSE).