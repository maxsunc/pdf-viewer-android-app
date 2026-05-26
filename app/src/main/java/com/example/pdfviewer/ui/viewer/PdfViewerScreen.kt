@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.pdfviewer.ui.viewer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdfviewer.model.DocumentProgress
import com.example.pdfviewer.model.ThemeMode
import com.example.pdfviewer.model.ViewMode
import com.example.pdfviewer.ui.ThemeMenuAction
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    state: ViewerState,
    onExit: () -> Unit,
    onUpdateProgress: (DocumentProgress) -> Unit,
    onModeChange: (ViewMode) -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    viewModel: PdfViewerScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val pageCount by viewModel.pageCount.collectAsState()
    val bitmaps by viewModel.bitmaps.collectAsState()
    val listState = rememberLazyListState()
    val widthPx = context.resources.displayMetrics.widthPixels
    val scope = rememberCoroutineScope()
    var pagedCurrentPage by remember { mutableStateOf(state.initialPage) }

    LaunchedEffect(state.uri) {
        viewModel.load(state.uri)
    }

    LaunchedEffect(state.uri, pageCount, state.mode) {
        if (pageCount > 0 && state.mode == ViewMode.VERTICAL) {
            scope.launch {
                listState.scrollToItem(state.initialPage.coerceIn(0, pageCount - 1))
            }
        }
    }

    LaunchedEffect(state.uri, state.mode, state.initialPage) {
        pagedCurrentPage = state.initialPage
    }

    LaunchedEffect(listState.firstVisibleItemIndex, pageCount, state.mode) {
        if (pageCount > 0 && state.mode == ViewMode.VERTICAL) {
            val page = listState.firstVisibleItemIndex.coerceAtLeast(0)
            viewModel.updateProgress(
                uri = state.uri,
                displayName = state.displayName,
                page = page,
                pageCount = pageCount,
                mode = state.mode,
                onUpdate = onUpdateProgress
            )
        }
    }

    DisposableEffect(state.uri) {
        onDispose {
            viewModel.reset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.displayName) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Text("Back")
                    }
                },
                actions = {
                    Text(if (state.mode == ViewMode.VERTICAL) "Vertical" else "Paged")
                    Switch(
                        checked = state.mode == ViewMode.PAGED,
                        onCheckedChange = { checked ->
                            onModeChange(if (checked) ViewMode.PAGED else ViewMode.VERTICAL)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ThemeMenuAction(
                        themeMode = themeMode,
                        onThemeChange = onThemeChange
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (pageCount > 0) {
                val currentPage = if (state.mode == ViewMode.VERTICAL) {
                    listState.firstVisibleItemIndex
                } else {
                    pagedCurrentPage
                }.coerceIn(0, pageCount - 1)
                Text(
                    "${currentPage + 1} / $pageCount",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (state.mode == ViewMode.VERTICAL) {
                VerticalViewer(
                    pageCount = pageCount,
                    listState = listState,
                    bitmaps = bitmaps,
                    onRequest = { index -> viewModel.renderPage(state.uri, index, widthPx) }
                )
            } else {
                PagedViewer(
                    pageCount = pageCount,
                    bitmaps = bitmaps,
                    initialPage = state.initialPage,
                    uri = state.uri,
                    onPageChange = { page ->
                        pagedCurrentPage = page
                        viewModel.updateProgress(
                            uri = state.uri,
                            displayName = state.displayName,
                            page = page,
                            pageCount = pageCount,
                            mode = state.mode,
                            onUpdate = onUpdateProgress
                        )
                    },
                    onRequest = { index -> viewModel.renderPage(state.uri, index, widthPx) }
                )
            }
        }
    }
}

@Composable
private fun VerticalViewer(
    pageCount: Int,
    listState: LazyListState,
    bitmaps: Map<Int, Bitmap>,
    onRequest: (Int) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = (0 until pageCount).toList(),
            key = { it }
        ) { page ->
            val bitmap = bitmaps[page]
            if (bitmap == null) {
                onRequest(page)
                PlaceholderPage()
            } else {
                PdfPageImage(bitmap)
            }
        }
    }
}

@Composable
private fun PagedViewer(
    pageCount: Int,
    bitmaps: Map<Int, Bitmap>,
    initialPage: Int,
    uri: android.net.Uri,
    onPageChange: (Int) -> Unit,
    onRequest: (Int) -> Unit
) {
    val pagerState = rememberPagerStateSafe(initialPage, pageCount)
    LaunchedEffect(uri, pageCount, initialPage) {
        pagerState.scrollToPage(initialPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0)))
    }
    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }

    androidx.compose.foundation.pager.HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val bitmap = bitmaps[page]
        if (bitmap == null) {
            onRequest(page)
            PlaceholderPage()
        } else {
            PdfPageImage(bitmap)
        }
    }
}

@Composable
private fun PdfPageImage(bitmap: Bitmap) {
    var scale by remember { mutableStateOf(MinZoom) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                containerSize = size
                if (scale > MinZoom) {
                    offset = clampOffset(offset, scale, size)
                }
            }
            .clipToBounds()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var gestureActive = true
                    while (gestureActive) {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        val shouldConsume = event.changes.size > 1 || scale > MinZoom
                        if (shouldConsume) {
                            val newScale = (scale * zoomChange).coerceIn(MinZoom, MaxZoom)
                            val newOffset = clampOffset(offset + panChange, newScale, containerSize)
                            scale = newScale
                            offset = if (newScale == MinZoom) Offset.Zero else newOffset
                            event.changes.forEach { change ->
                                if (change.positionChanged()) {
                                    change.consumePositionChange()
                                }
                            }
                        }
                        gestureActive = event.changes.any { it.pressed }
                    }
                }
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
private fun PlaceholderPage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Loading...")
    }
}

private const val MinZoom = 1f
private const val MaxZoom = 4f

private fun clampOffset(offset: Offset, scale: Float, size: IntSize): Offset {
    if (scale <= MinZoom || size.width == 0 || size.height == 0) {
        return Offset.Zero
    }
    val maxX = (size.width * (scale - 1f) / 2f).coerceAtLeast(0f)
    val maxY = (size.height * (scale - 1f) / 2f).coerceAtLeast(0f)
    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY)
    )
}

@Composable
private fun rememberPagerStateSafe(initialPage: Int, pageCount: Int): androidx.compose.foundation.pager.PagerState {
    val safeInitial = initialPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
    return androidx.compose.foundation.pager.rememberPagerState(
        initialPage = safeInitial,
        pageCount = { pageCount }
    )
}
