package com.example.pdfviewer

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfviewer.data.RecentStore
import com.example.pdfviewer.model.DocumentProgress
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

    private val recentsState = store.recentsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )
    val recents = recentsState

    private val _viewerState = MutableStateFlow<ViewerState?>(null)
    val viewerState: StateFlow<ViewerState?> = _viewerState.asStateFlow()
    private var lastPageInSession: Int = 0
    private var lastPageCountInSession: Int = 0

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
                lastAccessed = System.currentTimeMillis()
            )
            lastPageInSession = initial.lastPage
            lastPageCountInSession = initial.pageCount
            _viewerState.value = ViewerState(
                uri = uri,
                displayName = displayName,
                initialPage = initial.lastPage,
                mode = initial.lastMode
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
        viewModelScope.launch { store.upsert(progress) }
    }

    fun updateMode(mode: ViewMode) {
        val current = _viewerState.value ?: return
        _viewerState.value = current.copy(mode = mode, initialPage = lastPageInSession)
        viewModelScope.launch {
            store.upsert(
                DocumentProgress(
                    uri = current.uri.toString(),
                    displayName = current.displayName,
                    lastPage = lastPageInSession,
                    pageCount = lastPageCountInSession,
                    lastMode = mode,
                    lastAccessed = System.currentTimeMillis()
                )
            )
        }
    }

    
}
