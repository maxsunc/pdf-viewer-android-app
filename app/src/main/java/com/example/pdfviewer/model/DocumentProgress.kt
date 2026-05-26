package com.example.pdfviewer.model

import kotlinx.serialization.Serializable

@Serializable
enum class ViewMode {
    VERTICAL,
    PAGED
}

@Serializable
data class DocumentProgress(
    val uri: String,
    val displayName: String,
    val lastPage: Int,
    val pageCount: Int,
    val lastMode: ViewMode,
    val lastAccessed: Long
)
