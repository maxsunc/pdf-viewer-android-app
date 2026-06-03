package com.example.pdfviewer

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfviewer.data.RecentStore
import com.example.pdfviewer.data.ThemeStore
import com.example.pdfviewer.model.DocumentProgress
import com.example.pdfviewer.model.ThemeMode
import com.example.pdfviewer.model.ViewMode
import com.example.pdfviewer.ui.viewer.ViewerState
import com.example.pdfviewer.util.getDisplayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PdfViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val store = RecentStore(application)
    private val themeStore = ThemeStore(application)

    private val recentsState = store.recentsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )
    val recents = recentsState

    private val _viewerState = MutableStateFlow<ViewerState?>(null)
    val viewerState: StateFlow<ViewerState?> = _viewerState.asStateFlow()
    val themeMode: StateFlow<ThemeMode> = themeStore.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.LIGHT
    )
    private var lastPageInSession: Int = 0
    private var lastPageCountInSession: Int = 0
    private var lastBookmarksInSession: Set<Int> = emptySet()

    fun persistPermission(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        runCatching {
            getApplication<Application>().contentResolver.takePersistableUriPermission(uri, flags)
        }
    }

    fun loadDocument(uri: Uri) {
        viewModelScope.launch {
            val existing = recentsState.value.firstOrNull { it.uri == uri.toString() }
            val displayName = existing?.displayName ?: getDisplayName(getApplication(), uri)
            val initial = existing ?: DocumentProgress(
                uri = uri.toString(),
                displayName = displayName,
                lastPage = 0,
                pageCount = 0,
                lastMode = ViewMode.VERTICAL,
                lastAccessed = System.currentTimeMillis(),
                bookmarks = emptySet()
            )
            lastPageInSession = initial.lastPage
            lastPageCountInSession = initial.pageCount
            lastBookmarksInSession = initial.bookmarks
            _viewerState.value = ViewerState(
                uri = uri,
                displayName = displayName,
                initialPage = initial.lastPage,
                mode = initial.lastMode,
                bookmarks = initial.bookmarks
            )
        }
    }

    fun openRecent(item: DocumentProgress) {
        loadDocument(Uri.parse(item.uri))
    }

    fun closeViewer() {
        _viewerState.value = null
    }

    fun updateProgress(progress: DocumentProgress) {
        lastPageInSession = progress.lastPage
        lastPageCountInSession = progress.pageCount
        lastBookmarksInSession = progress.bookmarks
        viewModelScope.launch { store.upsert(progress) }
    }

    fun updateMode(mode: ViewMode) {
        val current = _viewerState.value ?: return
        _viewerState.value = current.copy(
            mode = mode, 
            initialPage = lastPageInSession,
            bookmarks = lastBookmarksInSession
        )
        viewModelScope.launch {
            store.upsert(
                DocumentProgress(
                    uri = current.uri.toString(),
                    displayName = current.displayName,
                    lastPage = lastPageInSession,
                    pageCount = lastPageCountInSession,
                    lastMode = mode,
                    lastAccessed = System.currentTimeMillis(),
                    bookmarks = lastBookmarksInSession
                )
            )
        }
    }

    fun toggleBookmark(page: Int) {
        val current = _viewerState.value ?: return
        val newBookmarks = if (current.bookmarks.contains(page)) {
            current.bookmarks - page
        } else {
            current.bookmarks + page
        }
        lastBookmarksInSession = newBookmarks
        _viewerState.value = current.copy(bookmarks = newBookmarks)
        viewModelScope.launch {
            store.upsert(
                DocumentProgress(
                    uri = current.uri.toString(),
                    displayName = current.displayName,
                    lastPage = lastPageInSession,
                    pageCount = lastPageCountInSession,
                    lastMode = current.mode,
                    lastAccessed = System.currentTimeMillis(),
                    bookmarks = newBookmarks
                )
            )
        }
    }

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            themeStore.setThemeMode(mode)
        }
    }

    
}
