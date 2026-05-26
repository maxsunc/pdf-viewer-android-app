package com.example.pdfviewer.model

enum class ThemeMode(val storageValue: String) {
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return when (value) {
                DARK.storageValue -> DARK
                LIGHT.storageValue -> LIGHT
                else -> LIGHT
            }
        }
    }
}
