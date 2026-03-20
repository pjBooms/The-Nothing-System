package com.excelsior.nothing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import java.io.*

/**
 * Rich text formatting operations.
 * Translated from Java original by Nikita Lipsky, Excelsior LLC.
 */
object Editor {

    private fun applyStyle(style: SpanStyle) {
        val state = AppState.currentTextWindow ?: return
        val sel = state.selection.value
        if (sel.length == 0) return
        state.spans.add(TextWindowState.StyledSpan(style, sel.min, sel.max))
    }

    fun bold() {
        applyStyle(SpanStyle(fontWeight = FontWeight.Bold))
    }

    fun italic() {
        applyStyle(SpanStyle(fontStyle = FontStyle.Italic))
    }

    fun underline() {
        applyStyle(SpanStyle(textDecoration = TextDecoration.Underline))
    }

    fun setColor(color: String) {
        val c = resolveColor(color) ?: run {
            println("Unknown color: $color")
            return
        }
        applyStyle(SpanStyle(color = c))
    }

    private fun resolveColor(color: String): Color? {
        return when (color.lowercase()) {
            "red" -> Color.Red
            "green" -> Color.Green
            "blue" -> Color.Blue
            "black" -> Color.Black
            "white" -> Color.White
            "yellow" -> Color.Yellow
            "cyan" -> Color.Cyan
            "magenta" -> Color.Magenta
            "gray", "grey" -> Color.Gray
            "lightgray", "lightgrey" -> Color.LightGray
            "darkgray", "darkgrey" -> Color.DarkGray
            "orange" -> Color(0xFFFF6600)
            "pink" -> Color(0xFFFF69B4)
            "purple" -> Color(0xFF800080)
            "brown" -> Color(0xFF8B4513)
            else -> {
                // Try hex color like #RRGGBB
                if (color.startsWith("#") && (color.length == 7 || color.length == 9)) {
                    try {
                        val hex = color.removePrefix("#")
                        val argb = if (hex.length == 6) {
                            "FF$hex".toLong(16)
                        } else {
                            hex.toLong(16)
                        }
                        Color(argb.toInt())
                    } catch (e: NumberFormatException) {
                        null
                    }
                } else {
                    // Try java.awt.Color fields for backwards compatibility
                    try {
                        val f = java.awt.Color::class.java.getDeclaredField(color.uppercase())
                        f.isAccessible = true
                        val awtColor = f.get(null) as java.awt.Color
                        Color(awtColor.red, awtColor.green, awtColor.blue)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }

    fun setFontSize(size: Int) {
        applyStyle(SpanStyle(fontSize = size.sp))
    }

    fun addButton(name: String, text: String, cmd: String) {
        // In the Compose version, adding a button inline to a text window isn't directly supported
        // We append a text marker and print info
        val state = AppState.currentTextWindow ?: return
        state.appendText("\n[Button:$name text=\"$text\" cmd=\"$cmd\"]\n")
        println("Button '$name' added to text window. Use GUIBuilder.newPanel + GUIBuilder.addButton for panel buttons.")
    }

    /**
     * Writes document (text + spans) to a file.
     * Format: first line is the text (base64 encoded), subsequent lines are spans as:
     *   start end style_description
     * For simplicity we write text to .txt and spans separately.
     */
    fun writeDocument(fileName: String, state: TextWindowState) {
        try {
            val file = File(fileName)
            // Write text
            FileOutputStream(file).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(state.text.value)
                    oos.writeInt(state.spans.size)
                    for (span in state.spans) {
                        oos.writeInt(span.start)
                        oos.writeInt(span.end)
                        oos.writeObject(serializeSpanStyle(span.style))
                    }
                }
            }
            AppState.currentTextWindow?.title = file.name
        } catch (io: IOException) {
            System.err.println("IOException: ${io.message}")
        }
    }

    private fun serializeSpanStyle(style: SpanStyle): HashMap<String, Any?> {
        val map = HashMap<String, Any?>()
        style.fontWeight?.let { map["fontWeight"] = it.weight }
        style.fontStyle?.let { map["fontStyle"] = it.value }
        style.textDecoration?.let { map["textDecoration"] = it.toString() }
        style.color.let { if (it != Color.Unspecified) map["color"] = it.value.toLong() }
        style.fontSize.let { if (it.isSp || it.isEm) map["fontSize"] = it.value }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    fun readDocument(inp: InputStream): Pair<String, List<TextWindowState.StyledSpan>> {
        return try {
            ObjectInputStream(BufferedInputStream(inp)).use { ois ->
                val text = ois.readObject() as String
                val spanCount = ois.readInt()
                val spans = mutableListOf<TextWindowState.StyledSpan>()
                repeat(spanCount) {
                    val start = ois.readInt()
                    val end = ois.readInt()
                    val styleMap = ois.readObject() as HashMap<String, Any?>
                    val style = deserializeSpanStyle(styleMap)
                    spans.add(TextWindowState.StyledSpan(style, start, end))
                }
                println("Document successfully loaded.")
                Pair(text, spans)
            }
        } catch (io: IOException) {
            System.err.println("IOException: ${io.message}")
            Pair("", emptyList())
        } catch (cnf: ClassNotFoundException) {
            System.err.println("ClassNotFoundException: ${cnf.message}")
            Pair("", emptyList())
        }
    }

    private fun deserializeSpanStyle(map: HashMap<String, Any?>): SpanStyle {
        var style = SpanStyle()
        (map["fontWeight"] as? Int)?.let { style = style.copy(fontWeight = FontWeight(it)) }
        (map["fontStyle"] as? Int)?.let {
            style = style.copy(fontStyle = if (it == FontStyle.Italic.value) FontStyle.Italic else FontStyle.Normal)
        }
        (map["textDecoration"] as? String)?.let { td ->
            style = style.copy(textDecoration = when {
                "Underline" in td -> TextDecoration.Underline
                "LineThrough" in td -> TextDecoration.LineThrough
                else -> TextDecoration.None
            })
        }
        (map["color"] as? Long)?.let { style = style.copy(color = Color(it.toULong())) }
        (map["fontSize"] as? Float)?.let { style = style.copy(fontSize = it.sp) }
        return style
    }

    fun open(file: String) {
        val resolvedFile = Kernel.checkThreeDots(file)
        val inp = Kernel.getInputStream(resolvedFile) ?: return
        val w = AppState.createTextWindow(resolvedFile)
        val (text, spans) = readDocument(inp)
        w.setText(text)
        w.spans.addAll(spans)
        w.title = resolvedFile
        AppState.currentTextWindow = w
    }

    fun save(file: String) {
        val cur = AppState.currentTextWindow ?: return
        writeDocument(file, cur)
        cur.title = file
        println("File $file saved")
    }
}
