# Project Overview

## What this app is
A minimal Android PDF reader built with Jetpack Compose. It opens local PDF files via the Android Storage Access Framework (SAF), renders pages using the platform PdfRenderer API, and remembers the last viewed page and reading mode per document. Recents are stored locally (max 10).

## What this app is not
- No annotations, highlighting, or text search.
- No cloud sync or accounts.
- No PDF editing or exporting.

## Target platform
- minSdk 21 (PdfRenderer requirement)
- targetSdk 35

## Tech stack
- Kotlin + Jetpack Compose
- Android PdfRenderer
- DataStore Preferences + kotlinx-serialization
