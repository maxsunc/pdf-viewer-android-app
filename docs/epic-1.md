# Epic 1: Hybrid PDF Viewer with Text Selection

## Background & Context
The codebase is a PDF viewer Android application built with Jetpack Compose. 

Originally, the app rendered PDFs using Android's native `android.graphics.pdf.PdfRenderer`. This API parses a PDF page and outputs a flat image (`Bitmap`). While this approach is fast and works well for simple viewing, it completely drops all structural data from the PDF (text, coordinates, etc.). As a result, **text selection is impossible** using only the native `PdfRenderer`.

The app supports two viewing modes:
1. **Vertical Mode:** A continuous vertical scroll of pages.
2. **Paged Mode:** A horizontal pager that snaps to individual pages.

## Objective
The primary goal is to **enable text selection** for the user without relying on expensive 3rd-party commercial SDKs (like PSPDFKit) or restrictively licensed open-source alternatives (like MuPDF).

To achieve this, we are adopting a **Hybrid Approach**:
1. **Vertical View (New):** Integrate the new official Jetpack library `androidx.pdf:pdf-viewer-fragment` (which is backed by Pdfium). This Fragment natively supports rendering, zooming, and **text selection** out of the box.
2. **Paged View (Legacy):** Retain the custom Jetpack Compose `HorizontalPager` and `PdfRenderer` implementation. This preserves the snappy, custom horizontal viewing experience, acknowledging the trade-off that text selection will only be available in Vertical mode.

## Technical Requirements
To implement this Hybrid Approach, the following architectural shifts must be implemented and maintained:

1. **SDK Requirements:** `androidx.pdf:pdf-viewer-fragment` strictly requires the app's `compileSdk` and `minSdk` to be at least `35`.
2. **Fragment Hosting:** Because the new PDF viewer is a traditional Android Fragment, the `MainActivity` must extend `FragmentActivity` instead of `ComponentActivity`.
3. **Compose Integration:** Use the `androidx.fragment:fragment-compose` library (specifically the `<AndroidFragment>` composable) to embed the `PdfViewerFragment` directly inside the Compose layout (`PdfViewerScreen.kt`).
4. **State Management:** The `PdfViewerScreen` must dynamically switch between rendering the `AndroidFragment` (when in Vertical mode) and the custom `PagedViewer` (when in Paged mode).

## Current Status & Next Steps (Hand-off)
Basic implementation of the Hybrid Approach has been committed to the codebase. The build dependencies, `FragmentActivity` shift, and `<AndroidFragment>` compose wrapper are in place.

**CRITICAL BLOCKER:** 
Currently, the application **crashes** when attempting to render the `PdfViewerFragment` in Vertical mode on a device/emulator. 

**Tasks for the assigned agent:**
1. Investigate the crash occurring when the `AndroidFragment<PdfViewerFragment>` is instantiated or rendered in `PdfViewerScreen.kt`. (Potential culprits: Theme mismatches between Compose Material 3 and Fragment expectations, lifecycle initialization issues within `AndroidFragment`, or missing manifest declarations required by the alpha PDF library).
2. Fix the crash so that the `PdfViewerFragment` correctly loads and displays the document URI.
3. Ensure text selection works in the Vertical mode.
4. Verify that toggling back to Paged mode smoothly transitions back to the custom Compose implementation without memory leaks.