package com.excelsior.nothing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor
import kotlin.math.roundToInt
import androidx.compose.material3.Text
import com.excelsior.nothing.AppState
import com.excelsior.nothing.FrameWindowState
import com.excelsior.nothing.TextWindowState

private val HANDLE = 6.dp
private const val MIN_W = 120f
private const val MIN_H = 60f

/**
 * Resize handle using positionChange() which Compose remaps to the current
 * coordinate system — correct even when the window moves between events.
 * Deltas are reported in pixels; callers must convert to dp as needed.
 */
@Composable
private fun ResizeHandle(
    modifier: Modifier,
    cursor: Cursor,
    onDrag: (dx: Float, dy: Float) -> Unit
) {
    Box(
        modifier
            .pointerHoverIcon(PointerIcon(cursor))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false).consume()
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            val delta = change.positionChange()
                            change.consume()
                            if (delta.x != 0f || delta.y != 0f) onDrag(delta.x, delta.y)
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    )
}

/**
 * Composable wrapper for text-based MDI windows.
 */
@Composable
fun InternalTextWindow(state: TextWindowState, onClose: () -> Unit) {
    InternalWindow(
        titleState = state.titleState,
        position = state.position,
        size = state.size,
        closable = state.closable,
        onClose = onClose,
        onActivate = {
            AppState.currentTextWindow = state
            AppState.bringTextWindowToFront(state)
        }
    ) {
        Stylepad(
            state = state,
            onFocusGain = {
                AppState.currentTextWindow = state
                AppState.bringTextWindowToFront(state)
            }
        )
    }
}

/**
 * Composable wrapper for panel-based MDI windows.
 */
@Composable
fun InternalFrameWindow(state: FrameWindowState, onClose: () -> Unit) {
    InternalWindow(
        titleState = state.titleState,
        position = state.position,
        size = state.size,
        closable = true,
        onClose = onClose,
        onActivate = {
            AppState.currentPanel = state
            AppState.bringFrameWindowToFront(state)
        }
    ) {
        DynamicPanel(
            state = state,
            onFocusGain = {
                AppState.currentPanel = state
                AppState.bringFrameWindowToFront(state)
            }
        )
    }
}

/**
 * Generic draggable, resizable, titled floating window composable.
 * Title bar: drag to move. Edges/corners: drag to resize.
 *
 * position is in pixels (used by IntOffset), size is in dp (used by .size(w.dp)).
 * Resize handlers divide pixel deltas by density to get dp deltas for size,
 * and multiply back to pixels when adjusting position (left-edge resize).
 */
@Composable
fun InternalWindow(
    titleState: MutableState<String>,
    position: MutableState<Offset>,
    size: MutableState<Size>,
    closable: Boolean,
    onClose: () -> Unit,
    onActivate: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val titleBarGradient = Brush.horizontalGradient(
        listOf(Color(0xFF000080), Color(0xFF1084D0))
    )
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .offset { IntOffset(position.value.x.roundToInt(), position.value.y.roundToInt()) }
            .size(size.value.width.dp, size.value.height.dp)
            .shadow(2.dp)
            .border(1.dp, Color(0xFF808080))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = { onActivate() })
    ) {
        Column(Modifier.fillMaxSize()) {
            // Title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(titleBarGradient)
                    .pointerInput(position) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = (position.value.x + dragAmount.x).coerceAtLeast(0f)
                            val newY = (position.value.y + dragAmount.y).coerceAtLeast(0f)
                            position.value = Offset(newX, newY)
                            onActivate()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = titleState.value,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp).weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (closable) {
                    Box(
                        modifier = Modifier.size(20.dp, 20.dp).clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "\u00D7", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // Content area
            Box(Modifier.fillMaxSize().background(Color.White)) {
                content()
            }
        }

        // Right edge — delta.x in px → dp for size
        ResizeHandle(
            modifier = Modifier.align(Alignment.CenterEnd).width(HANDLE).fillMaxHeight(),
            cursor = Cursor(Cursor.E_RESIZE_CURSOR)
        ) { dx, _ ->
            size.value = Size((size.value.width + dx / density).coerceAtLeast(MIN_W), size.value.height)
        }

        // Bottom edge
        ResizeHandle(
            modifier = Modifier.align(Alignment.BottomCenter).height(HANDLE).fillMaxWidth(),
            cursor = Cursor(Cursor.S_RESIZE_CURSOR)
        ) { _, dy ->
            size.value = Size(size.value.width, (size.value.height + dy / density).coerceAtLeast(MIN_H))
        }

        // Bottom-right corner
        ResizeHandle(
            modifier = Modifier.align(Alignment.BottomEnd).size(HANDLE),
            cursor = Cursor(Cursor.SE_RESIZE_CURSOR)
        ) { dx, dy ->
            size.value = Size(
                (size.value.width + dx / density).coerceAtLeast(MIN_W),
                (size.value.height + dy / density).coerceAtLeast(MIN_H)
            )
        }

        // Left edge — shrink width in dp, shift position in px so right edge stays fixed
        ResizeHandle(
            modifier = Modifier.align(Alignment.CenterStart).width(HANDLE).fillMaxHeight(),
            cursor = Cursor(Cursor.W_RESIZE_CURSOR)
        ) { dx, _ ->
            val newW = (size.value.width - dx / density).coerceAtLeast(MIN_W)
            val dxPx = (size.value.width - newW) * density
            position.value = Offset(position.value.x + dxPx, position.value.y)
            size.value = Size(newW, size.value.height)
        }

        // Bottom-left corner
        ResizeHandle(
            modifier = Modifier.align(Alignment.BottomStart).size(HANDLE),
            cursor = Cursor(Cursor.SW_RESIZE_CURSOR)
        ) { dx, dy ->
            val newW = (size.value.width - dx / density).coerceAtLeast(MIN_W)
            val dxPx = (size.value.width - newW) * density
            position.value = Offset(position.value.x + dxPx, position.value.y)
            size.value = Size(newW, (size.value.height + dy / density).coerceAtLeast(MIN_H))
        }
    }
}
