package com.excelsior.nothing

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange

object AppState {
    val textWindows = mutableStateListOf<TextWindowState>()
    val frameWindows = mutableStateListOf<FrameWindowState>()
    /** Unified z-order list: windows rendered in this order (last = topmost). */
    val allWindows = mutableStateListOf<Any>()
    var currentTextWindow by mutableStateOf<TextWindowState?>(null)
    var currentPanel by mutableStateOf<FrameWindowState?>(null)
    var currentSelectionText by mutableStateOf<String?>(null)

    lateinit var outputWindow: TextWindowState
    lateinit var commandsWindow: TextWindowState

    var windowCount = 0

    private val registry = HashMap<String, Any>()

    fun addToRegistry(key: String, obj: Any) {
        registry[key] = obj
    }

    fun getFromRegistry(key: String): Any? = registry[key]

    fun appendOutput(text: String) {
        if (::outputWindow.isInitialized) {
            outputWindow.appendText(text)
        }
    }

    fun createTextWindow(
        title: String = "Frame $windowCount",
        isOutput: Boolean = false,
        isCommands: Boolean = false
    ): TextWindowState {
        val id = windowCount
        val offsetX = (20 * (windowCount % 10)).toFloat()
        val offsetY = (20 * (windowCount % 10)).toFloat()
        windowCount++
        val state = TextWindowState(
            id = id,
            titleState = mutableStateOf(title),
            text = mutableStateOf(""),
            spans = mutableStateListOf(),
            selection = mutableStateOf(TextRange.Zero),
            position = mutableStateOf(Offset(offsetX, offsetY)),
            size = mutableStateOf(Size(600f, 450f)),
            isOutput = isOutput,
            isCommands = isCommands,
            closable = !isOutput && !isCommands
        )
        if (!isOutput && !isCommands) {
            textWindows.add(state)
            allWindows.add(state)
        }
        return state
    }

    fun createFrameWindow(title: String = "Frame $windowCount"): FrameWindowState {
        val id = windowCount
        val offsetX = (20 * (windowCount % 10)).toFloat()
        val offsetY = (20 * (windowCount % 10)).toFloat()
        windowCount++
        val state = FrameWindowState(
            id = id,
            titleState = mutableStateOf(title),
            components = mutableStateListOf(),
            position = mutableStateOf(Offset(offsetX, offsetY)),
            size = mutableStateOf(Size(600f, 450f))
        )
        frameWindows.add(state)
        allWindows.add(state)
        currentPanel = state
        return state
    }

    fun removeTextWindow(window: TextWindowState) {
        textWindows.remove(window)
        allWindows.remove(window)
        if (currentTextWindow == window) {
            currentTextWindow = textWindows.lastOrNull()
        }
    }

    fun removeFrameWindow(window: FrameWindowState) {
        frameWindows.remove(window)
        allWindows.remove(window)
        if (currentPanel == window) {
            currentPanel = frameWindows.lastOrNull()
        }
    }

    fun bringTextWindowToFront(window: TextWindowState) {
        if (textWindows.remove(window)) {
            textWindows.add(window)
        }
        if (allWindows.remove(window)) {
            allWindows.add(window)
        }
    }

    fun bringFrameWindowToFront(window: FrameWindowState) {
        if (frameWindows.remove(window)) {
            frameWindows.add(window)
        }
        if (allWindows.remove(window)) {
            allWindows.add(window)
        }
    }
}

class TextWindowState(
    val id: Int,
    val titleState: MutableState<String>,
    val text: MutableState<String>,
    val spans: SnapshotStateList<StyledSpan>,
    val selection: MutableState<TextRange>,
    val position: MutableState<Offset>,
    val size: MutableState<Size>,
    val isOutput: Boolean = false,
    val isCommands: Boolean = false,
    val closable: Boolean = true
) {
    data class StyledSpan(val style: SpanStyle, val start: Int, val end: Int)

    var title: String
        get() = titleState.value
        set(v) { titleState.value = v }

    fun setText(newText: String) {
        text.value = newText
        spans.clear()
        selection.value = TextRange.Zero
    }

    fun appendText(t: String) {
        text.value += t
    }

    fun getSelectionText(): String? {
        val sel = selection.value
        return if (sel.length > 0) text.value.substring(sel.min, sel.max) else null
    }
}

class FrameWindowState(
    val id: Int,
    val titleState: MutableState<String>,
    val components: SnapshotStateList<DynamicComponent>,
    val position: MutableState<Offset>,
    val size: MutableState<Size>
) {
    var title: String
        get() = titleState.value
        set(v) { titleState.value = v }
}

sealed class DynamicComponent {
    abstract val name: String

    data class ButtonComp(
        override val name: String,
        val text: String,
        val cmd: String,
        val x: Int,
        val y: Int,
        val w: Int,
        val h: Int
    ) : DynamicComponent()

    class TextFieldComp(
        override val name: String,
        val value: MutableState<String> = mutableStateOf(""),
        val x: Int,
        val y: Int,
        val w: Int,
        val h: Int
    ) : DynamicComponent()
}
