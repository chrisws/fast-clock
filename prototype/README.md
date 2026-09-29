# FastClock — Fasting Clock

A zero-dependency, single-file HTML prototype for tracking intermittent fasting on a 24-hour circular clock. **Target platform: Android (Kotlin + Jetpack Compose).**

## Quick Start

Open `index.html` in any modern browser. No build step, no server, no dependencies.

```
open index.html
```

## What's Built

### Clock
- 24-hour SVG ring with hour ticks and labels (12a / 6a / 12p / 6p)
- **Teal arc** — fasting window (default: 18 h)
- **Amber arc** — eating window (default: 12 PM – 6 PM)
- Glowing time-dot that tracks the current hour, updated every second

### EAT Button (center)
- Click to reveal the eating-window card:
  - Start / end times
  - Duration
  - Live countdown ("Opens in 04:32" / "Closes in 02:15")
- When you're inside the eating window the button flips to **FAST** (⏳) and the status text updates

### Status Line
- Above the button: `Fasting · 07:12 remaining` or `Eating · 03:45 until fast`

### Bottom Stats Bar
- Fasting hours · Eating hours · Day Streak (placeholder)

### Design
- Dark charcoal palette with teal + amber accents
- System font stack, CSS custom properties for the full token system
- Layered box-shadows, subtle radial-gradient background
- `prefers-reduced-motion` respected
- Responsive down to ~360 px
- No external requests: no CDN, no web fonts, no icon libraries

## File Structure

```
prototype/
├── index.html   ← everything lives here (HTML + CSS + JS)
└── README.md
```

## Configuration

All protocol values are at the top of the `<script>` block:

```js
var FAST_HOURS = 18;
var EAT_HOURS  = 6;
var EAT_START  = 12;  // 12:00 PM
var EAT_END    = 18;  // 6:00 PM
```

Change these to match your protocol.

## Path to Android

The HTML prototype is the **design and logic reference**. The production app will be built with **Kotlin + Jetpack Compose**.

- The clock ring becomes a custom `Canvas`-drawing composable (replaces the SVG)
- Clock math and state logic port directly from the JS
- CSS custom properties map to a Compose `ColorScheme`
- The EAT button, status line, and stats bar become standard Compose layouts
- All logic stays local and offline-first

### Android-specific considerations

- **Notifications** — use `NotificationManager` + `WorkManager` to fire alerts when the eating window opens/closes (replaces the browser `Notification` API)
- **Home-screen widget** — a small `AppWidgetProvider` showing the current status (fasting/eating) and countdown
- **State persistence** — `SharedPreferences` for protocol + streak; `Room` (SQLite) for fast history
- **Offline-first** — all logic is local; no network calls needed
- **Battery** — the 1-second tick is fine in-app; use `WorkManager` for background countdowns rather than a foreground service
- **Biometrics / lock** — optional `BiometricPrompt` to gate the "Break Fast" action
- **Theming** — map the CSS custom properties to Compose `ColorScheme` (dark by default, light optional)

## Next Steps

| # | Idea                        | Android note                                                                                                  |
|---|-----------------------------|---------------------------------------------------------------------------------------------------------------|
| 1 | **Protocol switching**      | Toggle 16:8, 18:6, 20:4, 23:1 or custom. Persist in `SharedPreferences`. Help text showing protocol objective |
| 2 | **Start / Break Fast flow** | "Start Fast" locks a timestamp; "Break Fast" logs completion. Optional biometric gate.                        |
| 3 | **State persistence**       | `localStorage` now → `SharedPreferences` / `Room` in the Android build.                                       |
| 4 | **12 hour clock not 24**    | subtle AM/PM labelling                                                                                        |
| 5 | Daily Score                 | When eat and stop eat button used outside the window the score is lowered                                     |
| 6 | Weigh-in                    | Track weight loss                                                                                             |
|   |                             |                                                                                                               |

## Next Steps ideas for later

| 1 | **Streak & history**        | Track completed fasts. 7-day / 30-day calendar strip. Store in `Room`.              |
| 2 | **Water / calorie logging** | Quick-tap counters during the eating window.                                        |
| 3 | **Notifications**           | Browser `Notification` API now → `NotificationManager` + `WorkManager` on Android.  |
| 4 | **Home-screen widget**      | Small widget showing status + countdown. Android-only feature.                      |
| 5 | **Body metrics**            | Optional weight / waist entry with an inline trend line.                            |
| 6 | **Share / export**          | Generate a summary card (canvas → PNG) or export fasts as CSV.                      |
| 7 | **Dark / light theme**      | Swap `:root` tokens now → Compose `ColorScheme` later.                              |
| 8 | **PWA / TWA**               | Service worker + manifest for an installable web version before going fully native. |
| 9 | **Multi-profile**           | Multiple users on one device (e.g., partner sharing a phone).                       |

## License

Prototype — no license attached.
