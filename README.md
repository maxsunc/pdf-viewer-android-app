# PDF Viewer Android App

## What this app is
A minimal Android PDF reader built with Jetpack Compose. It opens local PDF files via the Android Storage Access Framework (SAF), renders pages using the platform PdfRenderer API, and remembers the last viewed page and reading mode per document. Recents are stored locally.

## Setup
- Install Android Studio with the Android SDK, JDK 17, and an emulator or device on API 21+.
- Open the project in Android Studio, sync Gradle, and run the `app` configuration.
- Command line build: `./gradlew assembleDebug` (use `gradlew.bat` on Windows).
