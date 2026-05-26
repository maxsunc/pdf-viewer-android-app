# Feature Guide

This document maps common feature types to the files you likely need to touch.

## Add or change home screen UI
- `app/src/main/java/com/example/pdfviewer/ui/PdfViewerApp.kt`

Examples: new buttons, recents layout changes, empty states.

## Add or change viewer UI
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfViewerScreen.kt`

Examples: toolbar actions, page overlays, gestures, jump-to-page dialogs.

## Add rendering behavior
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfRendererRepository.kt`
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfCache.kt`

Examples: scaling rules, prefetching, cache size changes.

## Add or change persistence
- `app/src/main/java/com/example/pdfviewer/data/RecentStore.kt`
- `app/src/main/java/com/example/pdfviewer/model/DocumentProgress.kt`

Examples: new fields for per-document state, recents ordering, retention changes.

## Add flow or permission changes
- `app/src/main/java/com/example/pdfviewer/PdfViewerViewModel.kt`

Examples: permission validation, error handling on missing files.
