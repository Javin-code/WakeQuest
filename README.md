# WakeQuest – The Alarm You Must Solve

## Overview

WakeQuest is an interactive Android alarm application designed to make waking up more engaging. Instead of dismissing an alarm immediately, users must successfully complete a selected challenge before the alarm stops.

## Features

- Create, edit and delete alarms.
- Enable and disable individual alarms.
- Persistent local alarm storage.
- Full-screen alarm interface.
- Continuous alarm sound and vibration.
- Three interactive challenges:
  - Moving Buttons
  - CAPTCHA
  - Mathematics
- Adjustable challenge difficulty.
- Repeat alarm scheduling.
- Input validation and error handling.

## Technology Stack

- Kotlin
- Jetpack Compose
- Android SDK
- Room Database
- Android AlarmManager
- BroadcastReceiver
- Android audio and vibration APIs

## Project Structure

- `ui/` – Application screens and user interface.
- `data/` – Database, entities and repository.
- `alarm/` – Alarm scheduling and audio management.
- `challenge/` – Challenge generation and logic.
- `receiver/` – Alarm and device boot receivers.
- `service/` – Background alarm service.

## How to Run

1. Clone this repository.
2. Open the project in Android Studio.
3. Allow Gradle to synchronise.
4. Connect an Android device or start an emulator.
5. Build and run the application.
6. Grant the required Android permissions.

## Testing

The application should be tested for alarm creation, persistence, scheduling, audio playback, vibration, challenge validation and error handling.

Actual test results and platform limitations should be documented separately.

## Future Improvements

- Additional challenge types.
- Improved accessibility.
- More alarm sound options.
- Enhanced scheduling and reliability.

## Repository

https://github.com/Javin-code/WakeQuest
