# MUJ Student Portal

## Overview

A fully offline university Android project inspired by Manipal University Jaipur's student portal. This is an academic prototype, not an official MUJ application. It uses Kotlin, XML layouts and ordinary Activities rather than Compose or a backend.

## Features

- Demo login with field validation, masked password and optional remembered session.
- Dashboard with seven explicit-Intent destinations, academic summaries and quick-access cards.
- Calculated attendance, internal marks, registered courses and weekday timetables.
- Notice detail dialogs.
- Validated service request form with Spinner, RadioGroup and CheckBox; incrementing local request IDs and persistent latest request.
- Editable phone and alternate email, local saving and logout that clears the navigation task.
- No internet permission, remote images, Firebase, university APIs or network services.

## Screens

| Activity | Purpose |
| --- | --- |
| SplashActivity | One-second introduction and session-based routing |
| LoginActivity | Demo credentials, errors and Remember me |
| DashboardActivity | Student overview and portal navigation |
| AttendanceActivity | RecyclerView with calculated percentages and progress indicators |
| MarksActivity | Internal marks, grades and GPA summaries |
| TimetableActivity | Weekday Spinner dynamically updates RecyclerView |
| CoursesActivity | Course codes, faculty, credits and registration status |
| NoticesActivity | Notice list and full-detail AlertDialogs |
| ServiceRequestActivity | Validated form and persistent latest request |
| ProfileActivity | Student details, editable contacts and logout |

All seven secondary screens have toolbar Up navigation. System Back returns through the Activity stack. Successful login finishes LoginActivity; logout uses NEW_TASK and CLEAR_TASK so Back cannot reopen the dashboard.

## Android Concepts Demonstrated

- Standard Android Studio/Gradle app module, manifest and resources.
- Kotlin data classes and a simple mock-data object.
- Activity lifecycle: onCreate inflates and binds views; SplashActivity schedules in onStart and cancels in onStop; timetable saves selected day in onSaveInstanceState.
- Explicit Intent navigation between registered Activities.
- XML TextView, ImageView, TextInputEditText, MaterialButton, CheckBox, Spinner, RadioGroup and RadioButton.
- RecyclerView, ViewHolder, Adapter, LinearLayoutManager and recycled binding.
- Material cards, vector drawables, progress indicators, Toasts and dialogs.
- Input validation before persistence; nullable text and incorrectly typed preference values have safe defaults.
- ViewBinding gives type-safe view references without deprecated Kotlin synthetics.
- SharedPreferences for small local key/value records.

## Tech Stack

- Kotlin 2.0.21
- XML
- Android SDK (minimum API 23; compile/target API 35)
- RecyclerView
- SharedPreferences
- Material Components
- Android Gradle Plugin 8.7.3 / Gradle 8.9
- JDK 17 or 21

## Build and Install

Open this directory in Android Studio and let Gradle sync. Install Android SDK Platform 35 and Build Tools 35.0.0 through SDK Manager. Android Studio normally writes your SDK path into the ignored `local.properties` file; update that file if building on another machine.

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The APK is `app/build/outputs/apk/debug/app-debug.apk`. Build dependency downloads require internet on the first build; the installed application does not. Once dependencies and the Gradle distribution are cached, build with `./gradlew --offline :app:assembleDebug`.

## Demo Credentials

Registration Number: **229301234**  
Password: **muj123**

These credentials are also visible on the login screen. They are intentionally embedded for a classroom demo, not secure authentication. Passwords are not saved to preferences.

## Application Flow

```text
Splash
  -> Login (unless an active or remembered session exists)
  -> Dashboard
      -> Attendance / Marks / Timetable / Courses / Notices
      -> Service Request
      -> Profile -> Logout -> Login (cleared back stack)
```

Remember me persists login across process termination. Without it, login lasts only for the current app process. Logout clears both session flags but deliberately retains profile edits and the latest request. Android can keep a process alive after Back, so removing an Activity is not the same as ending a session process.

## Local Data

Academic information lives in `data/MockData.kt`. The model and adapter folders contain one model and RecyclerView adapter per academic module. There is no network/repository framework: Activities read the small immutable mock lists directly.

`PortalPreferences` wraps the `muj_portal` SharedPreferences file for remembered-session state, phone and alternate email. `ServiceRequestActivity` uses a separate `muj_requests` file for the counter and latest full request. User data survives screen reopening and process restarts, but uninstalling/clearing app data removes it. Automatic backup is disabled. Request submission is local recording only, not delivery to a university office. The small request record and counter use one checked `commit()` before the success dialog; profile/session updates use `apply()`.

Overall attendance is weighted by class count: `156 / 182 × 100 = 85.71%`, rounded to **86%**. Individual values and progress are calculated. Safe status uses the exact fraction against 75%, not a rounded percentage. GPA figures are illustrative academic summaries. The dashboard's **18 / 24** earned-credit progress and Courses' **19 registered credits** intentionally describe different demo figures, as specified in the assignment.

## 60-Second Demo

1. **0–10 s:** Launch, show visible demo credentials, tick Remember me and log in.
2. **10–20 s:** Show dashboard, open Attendance and explain calculated progress; Back, open Marks.
3. **20–30 s:** Open Timetable, change Monday to Tuesday; open Courses and show faculty/credits.
4. **30–40 s:** Open Notices and tap an announcement to show its full dialog.
5. **40–52 s:** Open Service Request. Demonstrate an invalid submission, then select Academic, enter `Attendance correction` and `Please review my attendance for the last lab.`, select Normal, confirm and submit. Show generated ID.
6. **52–60 s:** Reopen the request to show persistence; open Profile, save a phone edit, then log out and demonstrate that Back does not restore Dashboard.

For a slower viva, restart the app with Remember me enabled and disabled to explain persistent versus process-local sessions.

## Likely Viva Questions

**Why Activities and explicit Intents?** Each module is an independent Activity. An explicit Intent names the exact destination class; Android manages the back stack.

**What is onCreate?** It runs when an Activity instance is created. We inflate the XML binding, setContentView and attach listeners there. Rotation can create a new instance.

**Why XML and ViewBinding?** XML separates view structure from event-handling Kotlin. ViewBinding generates typed references to XML IDs and avoids unsafe casts and synthetic properties.

**How does RecyclerView work?** A LayoutManager positions items. The Adapter inflates a card into a ViewHolder and binds the record for a position. ViewHolders are reused as the list scrolls.

**How does the Spinner update the timetable?** onItemSelected looks up the chosen weekday's local list, passes it to the adapter and scrolls to the first class. Selection is saved across recreation.

**RadioButton versus CheckBox?** A RadioGroup permits one priority at a time. The CheckBox is an independent true/false confirmation.

**How is attendance calculated?** Attended classes divided by total classes times 100. Overall attendance divides summed attended classes by summed total classes, not the average of rounded subject percentages. A zero total is guarded.

**Why SharedPreferences rather than a database?** This prototype stores only a few key/value fields and one latest request. A database would be appropriate for a larger searchable request history.

**apply versus commit?** apply updates memory immediately and writes asynchronously without returning a success result. commit writes synchronously and returns whether persistence succeeded. The tiny request record checks that result before confirming submission.

**How is invalid input handled?** Validation returns before saving. TextInputLayout errors identify text fields; Toasts explain missing Spinner, priority or confirmation choices.

**How does logout prevent going back?** It clears session state and starts LoginActivity with FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_CLEAR_TASK. The prior task Activities are removed.

**Is authentication secure?** No. This is an offline academic prototype with visible hardcoded demo credentials. A production app needs a trusted server, secure authentication and protected tokens.

## Verification Performed

The debug APK was built and installed on an Android 15 / API 35 Pixel 5 emulator. Actual UI interactions were exercised with airplane mode enabled:

- Blank registration/password and invalid credentials are rejected.
- All seven dashboard destinations and both quick-access cards open their Activities; system Back and toolbar Up return correctly.
- Academic RecyclerViews render records, calculated attendance and registration status.
- The timetable changes for all five weekdays and retains selection through rotation.
- Notice dialogs display full descriptions, dates and categories.
- Service requests reject missing type, blank subject, short description, missing priority and unchecked confirmation.
- Successful requests produced `SR-2026-001` and `SR-2026-002`; the latest record survived Activity reopening, process restart and logout/login.
- Request draft text, priority and confirmation survived rotation.
- Profile rejects invalid phone/email; saved edits survived screen reopening and process restart.
- Remembered login survives force-stop/relaunch; unchecked Remember me returns to Login after process termination.
- Logout removes the dashboard from the back stack.
- Login, dashboard, attendance, request dialog and profile were visually inspected.

Verification used external throwaway UI automation, not a production dependency or a mocked backend. Runtime checks cover API 35; older supported Android versions were not separately exercised.

Final verification command: `./gradlew --offline :app:assembleDebug :app:lintDebug` — **BUILD SUCCESSFUL**. Android lint reports **0 errors and 27 non-blocking warnings**, including dependency-update suggestions, the explicitly requested custom SplashActivity, and presentation/localization recommendations. The report is generated at `app/build/reports/lint-results-debug.html`. No lint errors or warnings were suppressed.

## Future Scope

- Real university API integration
- Secure authentication
- Push notifications
- Online fee payments
- Live attendance synchronization
