package com.excelsior.nothing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.excelsior.nothing.AppState
import com.excelsior.nothing.Kernel
import com.excelsior.nothing.TextWindowState

/**
 * The core styled text editor composable.
 *
 * Lines that resolve as valid commands (via Kernel.isCommand) get underlined
 * on hover and executed on click. Implements the Oberon-style command detection.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Stylepad(state: TextWindowState, onFocusGain: () -> Unit = {}) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var hoveredRange by remember { mutableStateOf<IntRange?>(null) }

    val displayAnnotatedString = remember(state.text.value, state.spans.toList(), hoveredRange) {
        buildAnnotatedString {
            append(state.text.value)
            for (span in state.spans) {
                val safeEnd = minOf(span.end, state.text.value.length)
                if (span.start < safeEnd) {
                    addStyle(span.style, span.start, safeEnd)
                }
            }
            hoveredRange?.let { r ->
                val safeEnd = minOf(r.last + 1, state.text.value.length)
                if (r.first < safeEnd) {
                    addStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = Color(0xFF0000AA)
                        ),
                        r.first,
                        safeEnd
                    )
                }
            }
        }
    }

    val textFieldValue: TextFieldValue = remember(displayAnnotatedString, state.selection.value) {
        TextFieldValue(
            annotatedString = displayAnnotatedString,
            selection = state.selection.value
        )
    }

    val pointerIcon = if (hoveredRange != null) PointerIcon.Hand else PointerIcon.Default

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerHoverIcon(pointerIcon, overrideDescendants = true)
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue: TextFieldValue ->
                if (newValue.text != state.text.value) {
                    val newText = newValue.text
                    state.text.value = newText
                    // Remove spans that are fully out of bounds after edit
                    val adjustedSpans = state.spans.filter { span ->
                        span.start < newText.length && span.end <= newText.length
                    }
                    state.spans.clear()
                    state.spans.addAll(adjustedSpans)
                }
                state.selection.value = newValue.selection
                val sel = newValue.selection
                if (sel.length > 0) {
                    AppState.currentSelectionText = newValue.text.substring(sel.min, sel.max)
                }
                hoveredRange = null
            },
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { focusState -> if (focusState.isFocused) onFocusGain() }
                .onPointerEvent(PointerEventType.Move) { event ->
                    val pos = event.changes.firstOrNull()?.position ?: return@onPointerEvent
                    val layout = textLayoutResult ?: return@onPointerEvent
                    val text = state.text.value
                    if (text.isEmpty()) {
                        hoveredRange = null
                        return@onPointerEvent
                    }
                    val charOffset = layout.getOffsetForPosition(pos).coerceIn(0, text.length)
                    val lineStart = (text.lastIndexOf('\n', charOffset - 1) + 1).coerceAtLeast(0)
                    val lineEnd = text.indexOf('\n', charOffset).let { idx -> if (idx == -1) text.length else idx }
                    if (lineStart > lineEnd) {
                        hoveredRange = null
                        return@onPointerEvent
                    }
                    val line = text.substring(lineStart, lineEnd)
                    val firstSpaceInLine = line.indexOf(' ')
                    val cursorPosInLine = charOffset - lineStart
                    val isOnCommandPart = if (firstSpaceInLine < 0) cursorPosInLine < line.length
                                         else cursorPosInLine <= firstSpaceInLine
                    if (isOnCommandPart && line.isNotBlank() && Kernel.isCommand(line)) {
                        val cmdEnd = if (firstSpaceInLine >= 0) lineStart + firstSpaceInLine else lineEnd
                        hoveredRange = lineStart until cmdEnd
                    } else {
                        hoveredRange = null
                    }
                }
                .onPointerEvent(PointerEventType.Exit) { hoveredRange = null }
                // Use Initial pass so we intercept clicks BEFORE BasicTextField
                // consumes them for cursor placement (Main pass).
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press) {
                                val change = event.changes.firstOrNull() ?: continue
                                val layout = textLayoutResult ?: continue
                                val pos = change.position
                                val text = state.text.value
                                if (text.isEmpty()) continue
                                val charOffset = layout.getOffsetForPosition(pos).coerceIn(0, text.length)
                                val lineStart = (text.lastIndexOf('\n', charOffset - 1) + 1).coerceAtLeast(0)
                                val lineEnd = text.indexOf('\n', charOffset).let { idx -> if (idx == -1) text.length else idx }
                                if (lineStart > lineEnd) continue
                                val line = text.substring(lineStart, lineEnd)
                                val firstSpaceInLine = line.indexOf(' ')
                                val cursorPosInLine = charOffset - lineStart
                                val isOnCommandPart = if (firstSpaceInLine < 0) cursorPosInLine < line.length
                                                     else cursorPosInLine <= firstSpaceInLine
                                if (isOnCommandPart && line.isNotBlank() && Kernel.isCommand(line)) {
                                    // Execute the command but do NOT consume the event.
                                    // Consuming would prevent BasicTextField from gaining
                                    // keyboard focus, making typing impossible.
                                    Kernel.executeCommand(line)
                                }
                            }
                        }
                    }
                },
            onTextLayout = { result: TextLayoutResult -> textLayoutResult = result },
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Color.Black
            ),
            decorationBox = @Composable { innerTextField: @Composable () -> Unit ->
                Box(Modifier.fillMaxSize().padding(4.dp)) {
                    innerTextField()
                }
            }
        )
    }
}

/**
 * A system pane (output or commands) - a non-draggable Stylepad with a title bar.
 */
@Composable
fun SystemPane(state: TextWindowState) {
    Column(
        Modifier
            .fillMaxSize()
            .border(1.dp, Color.Gray)
    ) {
        // Title bar (not draggable - fixed system pane)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .background(Color(0xFF808080)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.titleState.value,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp).weight(1f)
            )
            if (state.isOutput) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear output",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { state.setText("") }
                        .padding(end = 4.dp)
                )
            }
        }
        Stylepad(state = state)
    }
}
