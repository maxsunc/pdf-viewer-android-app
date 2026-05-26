package com.example.pdfviewer.ui.viewer

import android.graphics.Bitmap
import android.util.LruCache

class PdfCache(maxEntries: Int) {
    private val cache = object : LruCache<String, Bitmap>(maxEntries) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun clear() {
        cache.evictAll()
    }
}
