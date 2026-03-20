package com.excelsior.nothing

import java.io.*

/**
 * File I/O and system operations.
 * Translated from Java original by Nikita Lipsky, Excelsior LLC.
 */
object Sys {

    fun newText() {
        AppState.createTextWindow()
    }

    fun newText(title: String) {
        val w = AppState.createTextWindow(title)
        AppState.currentTextWindow = w
    }

    fun createWindow() {
        AppState.createTextWindow()
    }

    fun writeText(file: String, text: String) {
        OutputStreamWriter(BufferedOutputStream(FileOutputStream(file))).use { os ->
            os.write(text)
        }
    }

    fun save(file: String) {
        val cur = AppState.currentTextWindow ?: return
        writeText(file, cur.text.value)
        cur.title = file
        println("File $file saved")
    }

    fun save() {
        val cur = AppState.currentTextWindow ?: return
        val file = cur.title
        if (!File(file).exists()) {
            println("Please specify file name")
            return
        }
        save(file)
    }

    fun readText(inp: InputStream): String {
        val reader = LineNumberReader(InputStreamReader(BufferedInputStream(inp)))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line).append('\n')
        }
        reader.close()
        return sb.toString()
    }

    fun readText(file: String): String {
        return readText(FileInputStream(file))
    }

    fun readTextFromFile(file: String): String {
        return readText(file)
    }

    fun open(file: String) {
        val resolvedFile = Kernel.checkThreeDots(file)
        val inp = Kernel.getInputStream(resolvedFile) ?: return
        val text = readText(inp)
        val w = AppState.createTextWindow(resolvedFile)
        w.setText(text)
        w.title = resolvedFile
        AppState.currentTextWindow = w
    }

    fun compile() {
        val cur = AppState.currentTextWindow ?: return
        val file = cur.title
        if (!File(file).exists()) {
            println("Please save file before compile")
            return
        }
        save(file)
        val compiler = javax.tools.ToolProvider.getSystemJavaCompiler()
        if (compiler == null) {
            println("Java compiler not available. Make sure you are running on a JDK.")
            return
        }
        val result = compiler.run(null, null, null, file)
        if (result == 0) {
            println("Successfully compiled")
            val filePath = file.substring(0, file.lastIndexOf('.'))
            val simpleClassName = File(filePath).name
            MethodHandle.addClass(simpleClassName, filePath)
        } else {
            println("Compilation failed with exit code $result")
        }
    }

    fun executeScript(fileName: String) {
        try {
            val dir = File(fileName).parent
            if (dir != null) MethodHandle.addBaseDir(dir)
            val br = BufferedReader(InputStreamReader(FileInputStream(fileName)))
            var command: String?
            while (br.readLine().also { command = it } != null) {
                Kernel.executeCommand(command!!)
            }
            br.close()
        } catch (io: IOException) {
            System.err.println("IOException: ${io.message}")
        }
    }
}
