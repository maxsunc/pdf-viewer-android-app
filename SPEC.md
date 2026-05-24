# PDF Viewer Android App - Specification

## 1. Overview
Build a simple Android PDF viewer that lets users open local PDF files, read them, and resume from the last viewed page. The app should support two reading modes: vertical continuous scroll (default, implemented first) and page-by-page swipe. The last page and last mode are saved per document, with a limit of 10 recent documents.

## 2. Goals
- Open PDFs from device storage using the Android Storage Access Framework (SAF).
- Render and display PDFs in a Compose UI.
- Save and restore the last viewed page per document.
- Support vertical scroll and page-by-page swipe, with a toggle.
- Keep last-viewed state for up to 10 recent PDFs.

## 3. Non-Goals
- Annotations, highlighting, or text search.
- Cloud sync or account features.
- Editing or exporting PDFs.

## 4. Target Platform
- Device: OnePlus 10T (Android 15).
- minSdk: 21 (required for Android PdfRenderer).
- targetSdk: 35 (Android 15).

## 5. Tech Stack and Dependencies
- Language: Kotlin
- UI: Jetpack Compose
- PDF Rendering: Android `PdfRenderer` (no third-party library).
- Persistence: DataStore Preferences + Kotlinx Serialization
- Coroutines for background rendering

Recommended libraries (via Gradle):
- `androidx.activity:activity-compose`
- `androidx.compose.ui:ui`
- `androidx.compose.material3:material3`
- `androidx.compose.foundation:foundation` (Pager)
- `androidx.datastore:datastore-preferences`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`

## 6. Data Model
### DocumentProgress
Stored per document in DataStore.

Fields:
- `uri: String` (SAF content URI)
- `displayName: String` (from OpenableColumns)
- `lastPage: Int` (0-based)
- `pageCount: Int` (optional, for UI)
- `lastMode: ViewMode` (`VERTICAL` or `PAGED`)
- `lastAccessed: Long` (epoch millis, used for LRU)

### Recent List
- Stored as a JSON array in DataStore under a single key.
- When adding/updating a document, move it to the front and trim to 10 items.

## 7. Storage and Permissions
- Use SAF `ACTION_OPEN_DOCUMENT` to pick PDFs.
- Call `takePersistableUriPermission` with read permission.
- Store the URI string in DataStore.
- If permission is missing or file is deleted, show an error and remove the entry from recents.

## 8. UI and Navigation

### Start Screen
- Primary action: "Open PDF" (launches SAF picker).
- Secondary list: Recent PDFs (up to 10), showing display name and last page.

### Viewer Screen
- Top app bar with:
  - Back button
  - Document title
  - Mode toggle (Vertical / Paged)
- Page indicator (e.g., "12 / 320")
- Jump-to-page dialog or input

#### Reading Modes
1. **Vertical (default, first implementation)**
   - Use `LazyColumn` with one item per page.
   - Render pages on-demand when visible.
   - Save last page based on the first visible page index.

2. **Paged (secondary)**
   - Use `HorizontalPager` (Compose Foundation Pager).
   - One page per position.
   - Save last page when the pager settles.

## 9. PDF Rendering
- Open `ParcelFileDescriptor` from URI.
- Create `PdfRenderer` and get `pageCount`.
- For each page:
  - Render to bitmap sized to fit the screen width.
  - Use background dispatcher (IO/Default) to avoid blocking UI.
- Cache bitmaps in an `LruCache` keyed by `uri + pageIndex + width`.
- Release resources on screen exit and when bitmaps are evicted.

## 10. State Persistence
- Save last page and last mode:
  - On page change (throttle or debounce if needed).
  - On `onStop` as a final sync.
- Restore on viewer start:
  - Read DataStore entry for URI.
  - Scroll/jump to `lastPage` and set mode.

## 11. Error Handling
- Missing permission or unreadable URI:
  - Show a dialog or snackbar and remove from recents.
- Corrupt PDF:
  - Show a simple error and return to the start screen.
- Out-of-memory while rendering:
  - Reduce cache size and retry rendering at a lower scale if needed.

## 12. Performance and Memory
- Use a fixed-size `LruCache` for bitmaps (e.g., 3-6 pages).
- Render only visible pages and prefetch 1 page ahead.
- Avoid holding PdfRenderer pages open longer than needed.

## 13. Testing Plan
- Manual tests:
  - Open a PDF, scroll, close app, reopen, resume at last page.
  - Switch modes, close app, reopen, ensure mode and page restore.
  - Open more than 10 PDFs and confirm older ones drop off.
  - Revoke permission or delete a PDF and verify graceful handling.

## 14. Milestones
1. Project setup with Compose and SAF picker.
2. Vertical mode viewer with PdfRenderer rendering.
3. DataStore persistence for last page.
4. Recent list on start screen (limit 10).
5. Mode toggle and paged mode viewer.
6. Polish: jump-to-page, better error states, caching tweaks.

## 15. Open Decisions (defaults)
- Use PdfRenderer (built-in) rather than a third-party library.
- Store progress for up to 10 documents.
- Save per-document view mode (not just global).
