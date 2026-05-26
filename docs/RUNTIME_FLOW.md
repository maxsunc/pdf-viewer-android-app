# Runtime Flow

## App start
1. `MainActivity` sets the root Composable to `PdfViewerApp`.
2. `PdfViewerApp` collects recents and viewer state from `PdfViewerViewModel`.

## Open document
1. User taps Open PDF.
2. SAF picker returns a Uri.
3. `PdfViewerViewModel.persistPermission` stores read permission.
4. `PdfViewerViewModel.loadDocument` reads stored progress (if any) and emits `ViewerState`.

## Viewing
1. `PdfViewerScreen` calls `PdfViewerScreenViewModel.load` which opens PdfRenderer.
2. Pages are rendered on demand via `renderPage` and cached.
3. Page changes call `updateProgress`, which writes to DataStore.

## Exit
1. User hits Back in the top app bar.
2. `PdfViewerViewModel.closeViewer` sets `viewerState` to null.
3. Viewer resources are released via `PdfViewerScreenViewModel.reset`.
