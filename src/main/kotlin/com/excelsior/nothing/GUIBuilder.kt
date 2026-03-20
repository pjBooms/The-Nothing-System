package com.excelsior.nothing

import java.io.*

/**
 * Dynamic GUI panel building.
 * Translated from Java original by Nikita Lipsky, Excelsior LLC.
 */
object GUIBuilder {

    fun newPanel() {
        AppState.createFrameWindow()
    }

    fun addButton(name: String, text: String, cmd: String, x: Int, y: Int, w: Int, h: Int) {
        val panel = AppState.currentPanel ?: run {
            println("No current panel. Use GUIBuilder.newPanel first.")
            return
        }
        val button = DynamicComponent.ButtonComp(name, text, cmd, x, y, w, h)
        panel.components.add(button)
        AppState.addToRegistry(name, ButtonProxy(cmd))
    }

    fun addTextField(name: String, x: Int, y: Int, w: Int, h: Int) {
        val panel = AppState.currentPanel ?: run {
            println("No current panel. Use GUIBuilder.newPanel first.")
            return
        }
        val textField = DynamicComponent.TextFieldComp(name, x = x, y = y, w = w, h = h)
        panel.components.add(textField)
        // Register a proxy that returns the text field's value
        AppState.addToRegistry(name, TextFieldProxy(textField))
    }

    fun save(file: String) {
        val panel = AppState.currentPanel ?: run {
            println("No current panel.")
            return
        }
        try {
            FileOutputStream(file).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeInt(panel.components.size)
                    for (comp in panel.components) {
                        when (comp) {
                            is DynamicComponent.ButtonComp -> {
                                oos.writeUTF("button")
                                oos.writeUTF(comp.name)
                                oos.writeUTF(comp.text)
                                oos.writeUTF(comp.cmd)
                                oos.writeInt(comp.x)
                                oos.writeInt(comp.y)
                                oos.writeInt(comp.w)
                                oos.writeInt(comp.h)
                            }
                            is DynamicComponent.TextFieldComp -> {
                                oos.writeUTF("textfield")
                                oos.writeUTF(comp.name)
                                oos.writeInt(comp.x)
                                oos.writeInt(comp.y)
                                oos.writeInt(comp.w)
                                oos.writeInt(comp.h)
                            }
                        }
                    }
                }
            }
            println("File $file saved")
            panel.title = file
        } catch (io: IOException) {
            System.err.println("IOException: ${io.message}")
        }
    }

    fun open(file: String) {
        val resolvedFile = Kernel.checkThreeDots(file)
        val inp = Kernel.getInputStream(resolvedFile) ?: return
        try {
            ObjectInputStream(BufferedInputStream(inp)).use { ois ->
                val panel = AppState.createFrameWindow(resolvedFile)
                val count = ois.readInt()
                repeat(count) {
                    val type = ois.readUTF()
                    when (type) {
                        "button" -> {
                            val name = ois.readUTF()
                            val text = ois.readUTF()
                            val cmd = ois.readUTF()
                            val x = ois.readInt()
                            val y = ois.readInt()
                            val w = ois.readInt()
                            val h = ois.readInt()
                            panel.components.add(DynamicComponent.ButtonComp(name, text, cmd, x, y, w, h))
                            AppState.addToRegistry(name, ButtonProxy(cmd))
                        }
                        "textfield" -> {
                            val name = ois.readUTF()
                            val x = ois.readInt()
                            val y = ois.readInt()
                            val w = ois.readInt()
                            val h = ois.readInt()
                            val tf = DynamicComponent.TextFieldComp(name, x = x, y = y, w = w, h = h)
                            panel.components.add(tf)
                            AppState.addToRegistry(name, TextFieldProxy(tf))
                        }
                        else -> println("Unknown component type: $type")
                    }
                }
                panel.title = resolvedFile
            }
        } catch (io: IOException) {
            System.err.println("IOException: ${io.message}")
        } catch (cnf: ClassNotFoundException) {
            System.err.println("Class not found: ${cnf.message}")
        }
    }

    /**
     * Proxy that stands in for a button in the registry.
     * Can be used to execute the button's command via executeCommand.
     */
    class ButtonProxy(val cmd: String) {
        fun click() {
            Kernel.executeCommand(cmd)
        }

        override fun toString(): String = "ButtonProxy($cmd)"
    }

    /**
     * Proxy that wraps a TextFieldComp to expose a getText() method
     * compatible with reflection-based command system.
     */
    class TextFieldProxy(private val comp: DynamicComponent.TextFieldComp) {
        fun getText(): String = comp.value.value
        fun setText(text: String) { comp.value.value = text }
        override fun toString(): String = getText()
    }
}
