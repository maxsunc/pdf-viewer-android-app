# Architecture

## Overview
This is a single-module Android app using Jetpack Compose. The UI is split into a home screen (open + recents) and a viewer screen (rendered PDF). Navigation is state-driven: when `viewerState` is null the home screen shows; when it is non-null the viewer shows.

Key components:
- App-level state and persistence: `app/src/main/java/com/example/pdfviewer/PdfViewerViewModel.kt`
- Viewer rendering state and cache: `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfViewerViewModel.kt`
- Viewer UI: `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfViewerScreen.kt`
- Rendering + cache: `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfRendererRepository.kt`, `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfCache.kt`
- Recents persistence: `app/src/main/java/com/example/pdfviewer/data/RecentStore.kt`

## UI and state flow
1. `MainActivity` sets `PdfViewerApp` as the root Composable.
2. `PdfViewerApp` shows the home screen and launches the SAF picker.
3. On picker result, `PdfViewerViewModel` persists URI permission and loads the document.
4. `PdfViewerViewModel` emits `ViewerState`, which flips the UI to `PdfViewerScreen`.
5. `PdfViewerScreen` drives page rendering and progress updates.
6. Progress and mode changes are written back to DataStore through `RecentStore`.

## Data and persistence
Progress for each document is stored as `DocumentProgress` in a DataStore Preferences JSON array. Recents are kept as LRU (front of list is most recent) and trimmed to 10.

Relevant files:
- `app/src/main/java/com/example/pdfviewer/model/DocumentProgress.kt`
- `app/src/main/java/com/example/pdfviewer/data/RecentStore.kt`

## Rendering pipeline
`PdfRendererRepository` opens a `ParcelFileDescriptor` and `PdfRenderer`. Pages are rendered on background threads to a bitmap scaled to screen width. `PdfCache` stores bitmaps in an LRU keyed by `uri + page + width` and is cleared when the viewer closes.

Relevant files:
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfRendererRepository.kt`
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfCache.kt`

## Reading modes
- Vertical: `LazyColumn` with one item per page. Progress is the first visible page index.
- Paged: `HorizontalPager` with one item per page. Progress is the current pager page.

Relevant file:
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfViewerScreen.kt`

## Extension points
When adding a feature, start in these areas:
- Home screen or recents UI: `app/src/main/java/com/example/pdfviewer/ui/PdfViewerApp.kt`
- Viewer UI and gestures: `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfViewerScreen.kt`
- Rendering changes: `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfRendererRepository.kt`
- Persistence schema or recents behavior: `app/src/main/java/com/example/pdfviewer/data/RecentStore.kt`
- App flow and permissions: `app/src/main/java/com/example/pdfviewer/PdfViewerViewModel.kt`
