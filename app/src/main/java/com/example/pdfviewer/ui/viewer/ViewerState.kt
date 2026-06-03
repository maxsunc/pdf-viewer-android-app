package com.example.pdfviewer.ui.viewer

import android.net.Uri
import com.example.pdfviewer.model.ViewMode

data class ViewerState(
    val uri: Uri,
    val displayName: String,
    val initialPage: Int,
    val mode: ViewMode,
    val bookmarks: Set<Int> = emptySet()
)
