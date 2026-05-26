# File Map

## Root
- `build.gradle.kts`: top-level Gradle plugins.
- `settings.gradle.kts`: module list and repositories.
- `SPEC.md`: product specification and roadmap.

## App module
- `app/src/main/AndroidManifest.xml`: app manifest and launcher activity.
- `app/src/main/java/com/example/pdfviewer/MainActivity.kt`: entry activity.
- `app/src/main/java/com/example/pdfviewer/ui/PdfViewerApp.kt`: home screen + navigation gate.
- `app/src/main/java/com/example/pdfviewer/PdfViewerViewModel.kt`: app state, recents, permissions.
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfViewerScreen.kt`: viewer UI and interactions.
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfViewerViewModel.kt`: rendering state and cache.
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfRendererRepository.kt`: PdfRenderer operations.
- `app/src/main/java/com/example/pdfviewer/ui/viewer/PdfCache.kt`: LRU bitmap cache.
- `app/src/main/java/com/example/pdfviewer/ui/viewer/ViewerState.kt`: viewer screen state.
- `app/src/main/java/com/example/pdfviewer/data/RecentStore.kt`: DataStore persistence for recents.
- `app/src/main/java/com/example/pdfviewer/model/DocumentProgress.kt`: per-document progress model.
- `app/src/main/java/com/example/pdfviewer/util/UriUtils.kt`: SAF URI helper.
