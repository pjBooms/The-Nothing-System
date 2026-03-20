package com.excelsior.nothing

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.excelsior.nothing.ui.App
import java.io.OutputStream
import java.io.PrintStream

fun main() {
    // All initialization runs ONCE here, before Compose starts.
    // If placed inside application {}, Compose re-executes it on every
    // recomposition, creating new TextWindowState objects while the UI
    // still holds references to the old ones.

    AppState.outputWindow = AppState.createTextWindow("Output", isOutput = true)
    AppState.commandsWindow = AppState.createTextWindow("System commands", isCommands = true)

    // Redirect stdout/stderr to the output pane
    val stream = PrintStream(object : OutputStream() {
        override fun write(b: ByteArray, off: Int, len: Int) {
            AppState.appendOutput(String(b, off, len))
        }
        override fun write(b: Int) {
            AppState.appendOutput(String(byteArrayOf(b.toByte())))
        }
    })
    System.setOut(stream)
    System.setErr(stream)

    // Load commands
    try {
        AppState.commandsWindow.setText(Sys.readTextFromFile("commands.txt"))
    } catch (e: Exception) {
        AppState.commandsWindow.setText(
            "Sys.newText\n" +
            "Sys.save\n" +
            "Sys.open\n" +
            "Sys.compile\n" +
            "Editor.bold\n" +
            "Editor.italic\n" +
            "Editor.underline\n" +
            "GUIBuilder.newPanel\n" +
            "System.out.println\n"
        )
    }

    // Create default user window
    val defaultWindow = AppState.createTextWindow("Frame 0")
    defaultWindow.setText("System.out.println hello\nAnother text")
    AppState.currentTextWindow = defaultWindow

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "The Nothing System",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            App()
        }
    }
}
