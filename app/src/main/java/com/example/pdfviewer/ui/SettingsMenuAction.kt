package com.example.pdfviewer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pdfviewer.R
import com.example.pdfviewer.model.ThemeMode
import com.example.pdfviewer.model.ViewMode

@Composable
fun SettingsMenuAction(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    viewMode: ViewMode? = null,
    onViewModeChange: ((ViewMode) -> Unit)? = null,
    bookmarks: Set<Int> = emptySet(),
    onNavigateToBookmark: (Int) -> Unit = {},
    enabled: Boolean = true
) {
    var menuOpen by remember { mutableStateOf(false) }
    var bookmarksDialogOpen by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { menuOpen = true },
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings_label)
            )
        }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            // Theme options
            DropdownMenuItem(
                text = { Text(stringResource(R.string.theme_light)) },
                onClick = {
                    onThemeChange(ThemeMode.LIGHT)
                    menuOpen = false
                },
                trailingIcon = {
                    if (themeMode == ThemeMode.LIGHT) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.theme_dark)) },
                onClick = {
                    onThemeChange(ThemeMode.DARK)
                    menuOpen = false
                },
                trailingIcon = {
                    if (themeMode == ThemeMode.DARK) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )

            if (viewMode != null && onViewModeChange != null) {
                HorizontalDivider()

                // View Mode options
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.view_mode_vertical)) },
                    onClick = {
                        onViewModeChange(ViewMode.VERTICAL)
                        menuOpen = false
                    },
                    trailingIcon = {
                        if (viewMode == ViewMode.VERTICAL) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.view_mode_paged)) },
                    onClick = {
                        onViewModeChange(ViewMode.PAGED)
                        menuOpen = false
                    },
                    trailingIcon = {
                        if (viewMode == ViewMode.PAGED) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }

            if (viewMode != null) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.bookmarks_title)) },
                    onClick = {
                        bookmarksDialogOpen = true
                        menuOpen = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Bookmark, contentDescription = null)
                    }
                )
            }
        }
    }

    if (bookmarksDialogOpen) {
        AlertDialog(
            onDismissRequest = { bookmarksDialogOpen = false },
            title = { Text(stringResource(R.string.bookmarks_title)) },
            text = {
                if (bookmarks.isEmpty()) {
                    Text(stringResource(R.string.bookmarks_empty))
                } else {
                    val sortedBookmarks = bookmarks.toList().sortedDescending()
                    LazyColumn {
                        items(sortedBookmarks) { page ->
                            TextButton(
                                onClick = {
                                    onNavigateToBookmark(page)
                                    bookmarksDialogOpen = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.bookmark_page_item, page + 1),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { bookmarksDialogOpen = false }) {
                    Text("Close")
                }
            }
        )
    }
}
