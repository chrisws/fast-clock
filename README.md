# FastClock

[![made with hali](android/.hali/badge.svg)](https://github.com/chrisws/fast-clock)
[![unit tests](android/.hali/test_badge.svg)](https://github.com/chrisws/fast-clock)

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
    │   └── src/
    │       ├── main/
    │       │   ├── AndroidManifest.xml
    │       │   └── java/com/fastclock/
    │       │       ├── MainActivity.kt
    │       │       ├── model/
    │       │       │   ├── Protocol.kt
    │       │       │   └── FastState.kt        (includes WeighIn)
    │       │       └── ui/
    │       │           ├── theme/Theme.kt
    │       │           ├── clock/ClockCanvas.kt
    │       │           ├── ClockLogic.kt        (pure logic, unit-tested)
    │       │           ├── ProtocolPanel.kt     (protocol selection + guidance)
    │       │           ├── WeighInDialog.kt     (weigh-in entry dialog)
    │       │           └── FastClockScreen.kt   (main screen composition)
    │       └── test/
    │           └── java/com/fastclock/ui/
    │               └── ClockLogicTest.kt        (unit tests)
    ├── build.gradle
    ├── settings.gradle
    └── gradle.properties
```

## Prototype

Open `prototype/index.html` in any browser. No build step, no dependencies.

See [prototype/README.md](prototype/README.md) for full details.

## Android App

Built with **Kotlin + Jetpack Compose**. The HTML prototype serves as the design and logic reference.

### Features

- **24-hour circular clock** with eating-window arc and real-time hand
- **Digital clock display** showing current time below the clock face
- **Protocol presets** (16:8, 18:6, 20:4) plus a **Custom** mode with editable eat start/end hours
- **Manual setup mode** — drag the eating window directly on the clock face
- **Fasting benefit summary** — contextual health notes based on current fast duration
- **Streak tracking** — consecutive-day fast counter
- **Weigh-in logging** — record weight over time via a dialog
- **Eating window card** — expandable detail showing window times and countdowns
- **Guidance panel** — dietary tips for maintaining a fast (coffee, tea, water)

### Key decisions

| Concern | Approach |
|---|---|
| Clock rendering | Custom `Canvas` composable (replaces SVG) |
| State persistence | `SharedPreferences` for protocol/streak, `Room` for fast history |
| Notifications | `NotificationManager` + `WorkManager` for eating-window alerts |
| Theming | Compose `ColorScheme` mapped from CSS custom properties |
| Testability | Pure logic extracted to `ClockLogic.kt`, covered by unit tests |
| Offline-first | All logic local, no network calls |

### Unit Tests

Clock and time logic is isolated in `ClockLogic.kt` and tested via JUnit:

```bash
cd android
./gradlew testDebugUnitTest
```

### Build

```bash
cd android
./gradlew assembleDebug
```

Requires Android Studio (Hedgehog or later) and JDK 17.

## License

See [LICENSE](LICENSE).