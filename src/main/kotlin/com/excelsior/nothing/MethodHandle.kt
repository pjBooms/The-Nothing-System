package com.excelsior.nothing

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.net.URL
import java.util.ArrayList
import java.util.HashMap

/**
 * Reflection-based method invocation handle.
 * Translated from Java original by Nikita Lipsky, Excelsior LLC.
 */
class MethodHandle(cmd: String) {

    var thisRef: Any? = null
    val methods = HashMap<Int, ArrayList<Method>>()

    private class OneClassClassLoader(
        private val logicalName: String,
        private val filePath: String = logicalName
    ) : ClassLoader(Thread.currentThread().contextClassLoader) {
        private var loaded = false
        private var clazz: Class<*>? = null

        override fun findClass(name: String): Class<*> {
            if (!name.replace('.', '/').equals(logicalName)) {
                throw ClassNotFoundException()
            }
            if (loaded) return clazz!!
            try {
                val fis = FileInputStream(filePath + ".class")
                return tryToLoadClass(fis)
            } catch (e: IOException) {
                // not found locally, try base URLs
            }
            for (url in baseURLs) {
                try {
                    val stream = URL(url.protocol, url.host, "$filePath.class").openStream()
                    return tryToLoadClass(stream)
                } catch (e: Exception) {
                    continue
                }
            }
            throw ClassNotFoundException()
        }

        private fun tryToLoadClass(inp: InputStream): Class<*> {
            val buf = inp.readBytes()
            inp.close()
            val c = defineClass(buf, 0, buf.size)
            clazz = c
            loaded = true
            return c
        }
    }

    private data class SplitString(val start: String, val lastPart: String)

    init {
        val ss = splitString(cmd) ?: throw IllegalArgumentException("No dot in command: $cmd")
        val c = findClass(ss.start) ?: throw IllegalArgumentException("Class not found: ${ss.start}")
        var cur: Class<*>? = c
        while (cur != null) {
            for (m in cur.declaredMethods) {
                if (m.name == ss.lastPart) {
                    val key = m.parameterTypes.size
                    val value = methods.getOrPut(key) { ArrayList() }
                    value.add(m)
                }
            }
            cur = cur.superclass
        }
        if (methods.isEmpty()) throw IllegalArgumentException("Method not found: ${ss.lastPart}")
    }

    private fun findClass(classRef: String): Class<*>? {
        if (classRef.startsWith("@")) {
            val o = Kernel.getFromRegistry(classRef.substring(1)) ?: return null
            thisRef = o
            return o.javaClass
        }
        return try {
            val cls = lookForClass(classRef)
            // Kotlin objects compile to classes with a static INSTANCE field.
            // Java static methods work with thisRef=null, but Kotlin object methods
            // are instance methods and need the singleton instance as receiver.
            try {
                val instanceField = cls.getDeclaredField("INSTANCE")
                instanceField.isAccessible = true
                thisRef = instanceField.get(null)
            } catch (_: NoSuchFieldException) {
                // Not a Kotlin object; thisRef stays null (correct for Java static methods)
            }
            cls
        } catch (e: ClassNotFoundException) {
            val ss = splitString(classRef) ?: return null
            try {
                val c = lookForClass(ss.start)
                val f: Field = c.getField(ss.lastPart)
                f.isAccessible = true
                val ref = f.get(null) ?: return null
                thisRef = ref
                ref.javaClass
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun exists(clazz: String): Boolean {
        if (File(clazz).exists()) return true
        for (dir in baseDirs) {
            if (File(dir, clazz).exists()) return true
        }
        for (url in baseURLs) {
            try {
                URL(url.protocol, url.host, clazz).openConnection()
                return true
            } catch (e: IOException) {
                continue
            }
        }
        return false
    }

    private fun lookForClass(clazz: String): Class<*> {
        for (imp in importPacks) {
            try {
                return Class.forName(if (imp.isEmpty()) clazz else "$imp.$clazz")
            } catch (e: ClassNotFoundException) {
                continue
            }
        }

        if (!classes.containsKey(clazz)) {
            if (File("$clazz.class").exists()) {
                addClass(clazz)
            } else {
                for (dir in baseDirs) {
                    if (File(dir, "$clazz.class").exists()) {
                        addClass(clazz, "$dir/$clazz")
                        break
                    }
                }
            }
        }

        val cl = classes[clazz]
        if (cl != null) {
            return Class.forName(clazz, true, cl)
        }
        throw ClassNotFoundException(clazz)
    }

    private fun findMethodByArgsLength(numOfArgs: Int): ArrayList<Method>? {
        for (i in numOfArgs downTo 0) {
            val candidate = methods[i]
            if (candidate != null) return candidate
        }
        return null
    }

    private fun canBeConvertedFromString(c: Class<*>): Boolean = true

    private fun convertFromString(arg: String, c: Class<*>): Any? {
        return when {
            arg.startsWith("&") && c == String::class.java -> arg.substring(1)
            arg.startsWith("@") && !c.isPrimitive -> {
                if (arg.indexOf('.') > 0) {
                    Kernel.executeCommand(arg.substring(1))
                } else {
                    val o = Kernel.getFromRegistry(arg.substring(1))
                        ?: return null
                    if (c.isAssignableFrom(o.javaClass)) o
                    else throw IllegalArgumentException("Wrong param type: $c")
                }
            }
            c == String::class.java -> arg
            c == Int::class.javaPrimitiveType -> arg.toInt()
            c == Long::class.javaPrimitiveType -> arg.toLong()
            else -> throw IllegalArgumentException("Wrong param type: $c")
        }
    }

    fun invoke(args: Array<String>): Any? {
        return try {
            val mths = findMethodByArgsLength(args.size)
                ?: throw IllegalArgumentException("Cannot find method for specified arguments")
            outer@ for (m in mths) {
                val paramTypes = m.parameterTypes
                val methArgs = arrayOfNulls<Any>(paramTypes.size)
                for (i in paramTypes.indices) {
                    val parT = paramTypes[i]
                    if (canBeConvertedFromString(parT)) {
                        try {
                            methArgs[i] = if (i < args.size) convertFromString(args[i], parT) else null
                        } catch (e: Exception) {
                            continue@outer
                        }
                    } else {
                        continue@outer
                    }
                }
                m.isAccessible = true
                return m.invoke(thisRef, *methArgs)
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private val baseURLs = ArrayList<URL>()
        private val baseDirs = ArrayList<String>()
        private val classes = HashMap<String, OneClassClassLoader>()
        private val importPacks = arrayOf("", "java.lang", "com.excelsior.nothing")

        fun addBaseURL(url: URL) {
            baseURLs.add(url)
        }

        fun addBaseDir(dir: String) {
            if (!baseDirs.contains(dir)) baseDirs.add(dir)
        }

        fun addClass(className: String, filePath: String = className) {
            classes[className] = OneClassClassLoader(className, filePath)
        }

        fun getMethodHandle(cmd: String): MethodHandle? {
            return try {
                MethodHandle(cmd)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        private fun splitString(s: String): SplitString? {
            val lastDot = s.lastIndexOf('.')
            if (lastDot == -1) return null
            return SplitString(s.substring(0, lastDot), s.substring(lastDot + 1))
        }
    }
}
