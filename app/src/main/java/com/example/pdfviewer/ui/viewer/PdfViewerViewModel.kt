package com.example.pdfviewer.ui.viewer

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfviewer.model.DocumentProgress
import com.example.pdfviewer.model.ViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PdfViewerScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val renderer = PdfRendererRepository(application)
    private val cache = PdfCache(6)
    private var currentUri: Uri? = null

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    private val _bitmaps = MutableStateFlow<Map<Int, Bitmap>>(emptyMap())
    val bitmaps: StateFlow<Map<Int, Bitmap>> = _bitmaps.asStateFlow()

    fun load(uri: Uri) {
        viewModelScope.launch {
            if (currentUri != uri) {
                reset()
                currentUri = uri
            }
            renderer.open(uri)
            _pageCount.value = renderer.pageCount()
        }
    }

    fun renderPage(uri: Uri, pageIndex: Int, width: Int) {
        val key = "${uri}_${pageIndex}_${width}"
        val existing = cache.get(key)
        if (existing != null) {
            _bitmaps.value = _bitmaps.value + (pageIndex to existing)
            return
        }
        viewModelScope.launch {
            val bitmap = renderer.renderPage(uri, pageIndex, width)
            cache.put(key, bitmap)
            _bitmaps.value = _bitmaps.value + (pageIndex to bitmap)
        }
    }

    fun reset() {
        renderer.close()
        cache.clear()
        _bitmaps.value = emptyMap()
        _pageCount.value = 0
    }

    fun updateProgress(
        uri: Uri,
        displayName: String,
        page: Int,
        pageCount: Int,
        mode: ViewMode,
        onUpdate: (DocumentProgress) -> Unit
    ) {
        onUpdate(
            DocumentProgress(
                uri = uri.toString(),
                displayName = displayName,
                lastPage = page,
                pageCount = pageCount,
                lastMode = mode,
                lastAccessed = System.currentTimeMillis()
            )
        )
    }

    override fun onCleared() {
        reset()
        super.onCleared()
    }
}
