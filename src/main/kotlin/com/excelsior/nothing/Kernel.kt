package com.excelsior.nothing

import com.excelsior.common.QuotedStringTokenizer
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList
import java.util.HashMap

/**
 * Command execution engine.
 * Translated from Java original by Nikita Lipsky, Excelsior LLC.
 */
object Kernel {

    private val cache = HashMap<String, Boolean>()

    fun isCommand(line: String): Boolean {
        val st = QuotedStringTokenizer(line)
        val cmd = st.nextToken() ?: return false
        cache[cmd]?.let { return it }
        val m = MethodHandle.getMethodHandle(cmd)
        if (m != null) cache[cmd] = true
        return m != null
    }

    fun executeCommand(line: String): Any? {
        cache.clear()
        val st = QuotedStringTokenizer(line)
        val cmd = st.nextToken() ?: return null
        val m = MethodHandle.getMethodHandle(cmd) ?: return null

        val firstArg = st.nextToken()
        val args = ArrayList<String>()

        if (firstArg != null) {
            val selText = AppState.currentSelectionText
            if (firstArg == "^" && selText != null) {
                val selSt = QuotedStringTokenizer(selText)
                while (true) {
                    val tok = selSt.nextToken() ?: break
                    args.add(tok)
                }
            } else {
                args.add(firstArg)
                while (true) {
                    val nextToken = st.nextToken() ?: break
                    args.add(nextToken)
                }
            }
        }

        return try {
            m.invoke(args.toTypedArray())
        } catch (e: Exception) {
            null
        }
    }

    fun addToRegistry(key: String, o: Any) {
        AppState.addToRegistry(key, o)
    }

    fun getFromRegistry(key: String): Any? {
        return AppState.getFromRegistry(key)
    }

    fun checkThreeDots(file: String): String {
        if (file == "...") {
            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Open")
            dialog.isVisible = true
            return if (dialog.file != null) "${dialog.directory}${dialog.file}" else file
        }
        return file
    }

    fun getInputStream(file: String): InputStream? {
        return try {
            val url = URL(file)
            MethodHandle.addBaseURL(getBaseUrlFrom(url))
            url.openStream()
        } catch (e: MalformedURLException) {
            if (file == "...") return null
            try {
                FileInputStream(file)
            } catch (ioe: IOException) {
                System.err.println("Cannot open file: $file - ${ioe.message}")
                null
            }
        }
    }

    private fun getBaseUrlFrom(url: URL): URL {
        return try {
            URL(url.protocol + "://" + url.host)
        } catch (e: MalformedURLException) {
            throw AssertionError("Bad base URL built")
        }
    }
}
