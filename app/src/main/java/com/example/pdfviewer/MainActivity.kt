package com.example.pdfviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfviewer.model.ThemeMode
import com.example.pdfviewer.ui.PdfViewerApp
import com.example.pdfviewer.ui.theme.PdfViewerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: PdfViewerViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            PdfViewerTheme(darkTheme = themeMode == ThemeMode.DARK) {
                PdfViewerApp(viewModel)
            }
        }
    }
}
