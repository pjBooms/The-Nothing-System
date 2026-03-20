package com.excelsior.nothing.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun App() {
    MaterialTheme {
        Surface(color = Color(0xFFD4D0C8)) {
            NothingDesktop()
        }
    }
}
