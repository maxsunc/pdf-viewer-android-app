@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.pdfviewer.ui.viewer

import android.annotation.SuppressLint
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.view.View
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.example.pdfviewer.model.DocumentProgress
import com.example.pdfviewer.model.ThemeMode
import com.example.pdfviewer.model.ViewMode
import com.example.pdfviewer.ui.ThemeMenuAction
import kotlinx.coroutines.launch

@SuppressLint("NewApi")
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
    val widthPx = context.resources.displayMetrics.widthPixels
    var pagedCurrentPage by remember { mutableStateOf(state.initialPage) }
    var zoomScale by remember(state.uri) { mutableStateOf(MinZoom) }
    val fragmentContainerId = remember { View.generateViewId() }

    LaunchedEffect(state.uri) {
        viewModel.load(state.uri)
    }

    LaunchedEffect(state.uri, state.mode, state.initialPage) {
        pagedCurrentPage = state.initialPage
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
            if (pageCount > 0 && state.mode == ViewMode.PAGED) {
                val currentPage = pagedCurrentPage.coerceIn(0, pageCount - 1)
                Text(
                    "${currentPage + 1} / $pageCount",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (state.mode == ViewMode.VERTICAL) {
                val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager
                if (fragmentManager != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            FragmentContainerView(ctx).apply {
                                id = fragmentContainerId
                            }
                        },
                        update = { view ->
                            val existingFragment = fragmentManager.findFragmentById(fragmentContainerId) as? PdfViewerFragment
                            if (existingFragment == null) {
                                val newFragment = PdfViewerFragment().apply {
                                    documentUri = state.uri
                                }
                                fragmentManager.beginTransaction()
                                    .replace(fragmentContainerId, newFragment)
                                    .commit()
                            } else {
                                existingFragment.documentUri = state.uri
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("Error: Context is not a FragmentActivity")
                    }
                }
            } else {
                PagedViewer(
                    pageCount = pageCount,
                    bitmaps = bitmaps,
                    initialPage = state.initialPage,
                    uri = state.uri,
                    scale = zoomScale,
                    onScaleChange = { zoomScale = it },
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
private fun PagedViewer(
    pageCount: Int,
    bitmaps: Map<Int, Bitmap>,
    initialPage: Int,
    uri: android.net.Uri,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onPageChange: (Int) -> Unit,
    onRequest: (Int) -> Unit
) {
    val pagerState = rememberPagerStateSafe(initialPage, pageCount)
    val scope = rememberCoroutineScope()
    val edgeWidthPx = with(LocalDensity.current) { EdgeTapWidth.toPx() }
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
            PdfPageImage(
                bitmap = bitmap,
                scale = scale,
                onScaleChange = onScaleChange,
                edgeTapWidthPx = edgeWidthPx,
                onEdgeTap = { direction ->
                    if (pageCount < 2) return@PdfPageImage
                    val target = when (direction) {
                        EdgeTapDirection.Previous -> (page - 1).coerceAtLeast(0)
                        EdgeTapDirection.Next -> (page + 1).coerceAtMost(pageCount - 1)
                    }
                    if (target != page) {
                        scope.launch { pagerState.animateScrollToPage(target) }
                    }
                }
            )
        }
    }
}

@Composable
private fun PdfPageImage(
    bitmap: Bitmap,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    edgeTapWidthPx: Float? = null,
    onEdgeTap: ((EdgeTapDirection) -> Unit)? = null
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val scaleState = rememberUpdatedState(scale)
    val onScaleChangeState = rememberUpdatedState(onScaleChange)
    val edgeTapWidthState = rememberUpdatedState(edgeTapWidthPx)
    val onEdgeTapState = rememberUpdatedState(onEdgeTap)

    LaunchedEffect(scale, containerSize) {
        if (scale <= MinZoom || containerSize == IntSize.Zero) {
            offset = Offset.Zero
        } else {
            offset = clampOffset(offset, scale, containerSize)
        }
    }

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
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val edgeTapDirection = resolveEdgeTapDirection(
                        position = down.position,
                        containerSize = containerSize,
                        edgeTapWidthPx = edgeTapWidthState.value
                    )
                    var pastTouchSlop = false
                    val startPosition = down.position
                    var gestureActive = true
                    while (gestureActive) {
                        val event = awaitPointerEvent()
                        if (event.changes.size > 1) {
                            pastTouchSlop = true
                        }
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        val currentScale = scaleState.value
                        val shouldConsume = event.changes.size > 1 || currentScale > MinZoom
                        val positionDelta = event.changes.firstOrNull()?.position?.minus(startPosition)
                        if (!pastTouchSlop && positionDelta != null) {
                            if (positionDelta.getDistance() > viewConfiguration.touchSlop) {
                                pastTouchSlop = true
                            }
                        }
                        if (shouldConsume) {
                            val newScale = (currentScale * zoomChange).coerceIn(MinZoom, MaxZoom)
                            val newOffset = clampOffset(offset + panChange, newScale, containerSize)
                            offset = if (newScale == MinZoom) Offset.Zero else newOffset
                            onScaleChangeState.value(newScale)
                            event.changes.forEach { change ->
                                if (change.positionChanged()) {
                                    change.consume()
                                }
                            }
                        }
                        gestureActive = event.changes.any { it.pressed }
                    }
                    if (!pastTouchSlop && edgeTapDirection != null) {
                        onEdgeTapState.value?.invoke(edgeTapDirection)
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
private val EdgeTapWidth = 32.dp

private enum class EdgeTapDirection {
    Previous,
    Next
}

private fun resolveEdgeTapDirection(
    position: Offset,
    containerSize: IntSize,
    edgeTapWidthPx: Float?
): EdgeTapDirection? {
    val widthPx = containerSize.width.toFloat()
    val edgeWidth = edgeTapWidthPx ?: return null
    if (widthPx <= 0f || edgeWidth <= 0f) {
        return null
    }
    return when {
        position.x <= edgeWidth -> EdgeTapDirection.Previous
        position.x >= widthPx - edgeWidth -> EdgeTapDirection.Next
        else -> null
    }
}

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
