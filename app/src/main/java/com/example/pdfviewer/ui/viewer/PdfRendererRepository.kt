package com.example.pdfviewer.ui.viewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfRendererRepository(private val context: Context) {
    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var currentUri: Uri? = null

    suspend fun open(uri: Uri): PdfRenderer = withContext(Dispatchers.IO) {
        if (currentUri == uri && renderer != null) {
            return@withContext renderer!!
        }
        close()
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            ?: error("Unable to open PDF")
        fileDescriptor = pfd
        renderer = PdfRenderer(pfd)
        currentUri = uri
        renderer!!
    }

    suspend fun renderPage(uri: Uri, pageIndex: Int, width: Int): Bitmap = withContext(Dispatchers.IO) {
        val pdf = open(uri)
        if (pageIndex < 0 || pageIndex >= pdf.pageCount) error("Page out of range")

        val page = pdf.openPage(pageIndex)
        val scale = width.toFloat() / page.width.toFloat()
        val height = (page.height * scale).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap
    }

    fun pageCount(): Int {
        return renderer?.pageCount ?: 0
    }

    fun close() {
        renderer?.close()
        fileDescriptor?.close()
        renderer = null
        fileDescriptor = null
        currentUri = null
    }
}
