package com.example.pdfviewer.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfviewer.PdfViewerViewModel
import com.example.pdfviewer.model.DocumentProgress
import com.example.pdfviewer.ui.viewer.PdfViewerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerApp(viewModel: PdfViewerViewModel = viewModel()) {
    val recents by viewModel.recents.collectAsState()
    val viewerState by viewModel.viewerState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.persistPermission(uri)
                viewModel.loadDocument(uri)
            }
        }
    )

    if (viewerState != null) {
        PdfViewerScreen(
            state = viewerState!!,
            onExit = { viewModel.closeViewer() },
            onUpdateProgress = { viewModel.updateProgress(it) },
            onModeChange = { viewModel.updateMode(it) },
            themeMode = themeMode,
            onThemeChange = viewModel::updateTheme
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Viewer") },
                actions = {
                    ThemeMenuAction(
                        themeMode = themeMode,
                        onThemeChange = viewModel::updateTheme
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                picker.launch(arrayOf("application/pdf"))
            }) {
                Text("Open PDF")
            }

            if (recents.isNotEmpty()) {
                Text("Recents")
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recents, key = { it.uri }) { item ->
                        RecentItem(item = item, onOpen = { viewModel.openRecent(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentItem(item: DocumentProgress, onOpen: () -> Unit) {
    Card(onClick = onOpen) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.displayName)
            Text("Page ${item.lastPage + 1} of ${item.pageCount}")
        }
    }
}
