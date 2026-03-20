package com.excelsior.nothing.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.runtime.key
import com.excelsior.nothing.AppState
import com.excelsior.nothing.FrameWindowState
import com.excelsior.nothing.TextWindowState

/**
 * MDI desktop pane - hosts all user text and frame windows.
 */
@Composable
fun MdiPane() {
    Box(Modifier.fillMaxSize().clipToBounds()) {
        for (window in AppState.allWindows) {
            when (window) {
                is TextWindowState -> key(window.id) {
                    InternalTextWindow(
                        state = window,
                        onClose = { AppState.removeTextWindow(window) }
                    )
                }
                is FrameWindowState -> key(window.id) {
                    InternalFrameWindow(
                        state = window,
                        onClose = { AppState.removeFrameWindow(window) }
                    )
                }
            }
        }
    }
}
