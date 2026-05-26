package com.example.pdfviewer.ui

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.pdfviewer.R
import com.example.pdfviewer.model.ThemeMode

@Composable
fun ThemeMenuAction(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val themeLabel = when (themeMode) {
        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
        ThemeMode.DARK -> stringResource(R.string.theme_dark)
    }

    TextButton(onClick = { menuOpen = true }) {
        Text(stringResource(R.string.theme_current, themeLabel))
    }
    DropdownMenu(
        expanded = menuOpen,
        onDismissRequest = { menuOpen = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.theme_light)) },
            onClick = {
                onThemeChange(ThemeMode.LIGHT)
                menuOpen = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.theme_dark)) },
            onClick = {
                onThemeChange(ThemeMode.DARK)
                menuOpen = false
            }
        )
    }
}
