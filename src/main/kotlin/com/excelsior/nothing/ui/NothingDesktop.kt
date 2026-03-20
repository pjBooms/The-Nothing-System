package com.excelsior.nothing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.excelsior.nothing.AppState
import java.awt.Cursor

/**
 * Main layout composable.
 * Left: user MDI pane. Right: system panes (output on top, commands on bottom).
 * Both splits are draggable.
 */
@Composable
fun NothingDesktop() {
    var totalWidth by remember { mutableStateOf(0) }
    var totalHeight by remember { mutableStateOf(0) }
    var hSplit by remember { mutableStateOf(0.8f) }
    var vSplit by remember { mutableStateOf(0.5f) }

    Row(
        Modifier
            .fillMaxSize()
            .onSizeChanged { totalWidth = it.width; totalHeight = it.height }
    ) {
        // User MDI pane
        Box(
            Modifier
                .weight(hSplit)
                .fillMaxHeight()
                .background(Color(0xFFD4D0C8))
        ) {
            MdiPane()
        }

        // Vertical divider (draggable)
        Box(
            Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(Color(0xFF808080))
                .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        if (totalWidth > 0) {
                            hSplit = (hSplit + dragAmount.x / totalWidth).coerceIn(0.1f, 0.9f)
                        }
                    }
                }
        )

        // System panes
        Column(Modifier.weight(1f - hSplit).fillMaxHeight()) {
            // Output pane
            Box(Modifier.weight(vSplit).fillMaxWidth()) {
                SystemPane(AppState.outputWindow)
            }

            // Horizontal divider (draggable)
            Box(
                Modifier
                    .height(5.dp)
                    .fillMaxWidth()
                    .background(Color(0xFF808080))
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            if (totalHeight > 0) {
                                vSplit = (vSplit + dragAmount.y / totalHeight).coerceIn(0.1f, 0.9f)
                            }
                        }
                    }
            )

            // Commands pane
            Box(Modifier.weight(1f - vSplit).fillMaxWidth()) {
                SystemPane(AppState.commandsWindow)
            }
        }
    }
}
