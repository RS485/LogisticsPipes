/*
 * Copyright (c) 2022  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2022  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.debug

import com.google.common.reflect.Reflection
import logisticspipes.LogisticsPipes
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL31
import org.lwjgl.opengl.GL32
import org.lwjgl.util.glu.GLU
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import kotlin.concurrent.withLock

private class ChangeTrackingSortedMap<K : Comparable<K>, V : Any> {
    private val orderedKeys = ArrayList<K>()
    private val values = ArrayList<V>()
    private val sessionUpdates = ArrayList<Int>()
    private val sessionInsertions = ArrayList<Int>()
    private var sessionStarted: Boolean = false

    val size: Int
        get() = orderedKeys.size

    fun startSession() {
        sessionUpdates.clear()
        sessionInsertions.clear()
        sessionStarted = true
    }

    fun put(key: K, value: V) {
        if (!sessionStarted) {
            throw IllegalStateException("Session not started. Only modify map after session start.")
        }
        val keyIndex = binarySearch(key)
        if (keyIndex < 0) {
            val addIndex = -(keyIndex + 1)
            orderedKeys.add(addIndex, key)
            values.add(addIndex, value)
            sessionInsertions.add(addIndex)
        } else if (values[keyIndex] != value) {
            orderedKeys[keyIndex] = key
            values[keyIndex] = value
            sessionUpdates.add(keyIndex)
        }
    }

    fun binarySearch(key: K): Int = Collections.binarySearch(orderedKeys, key)

    fun stopSession() {
        sessionStarted = false
    }

    fun getKeyOrNull(index: Int): K? = orderedKeys.getOrNull(index)
    fun getValueOrNull(index: Int): V? = values.getOrNull(index)
    fun wasUpdated(index: Int) = sessionUpdates.contains(index)
    fun wasAdded(index: Int) = sessionInsertions.contains(index)
}

private enum class GLType(val niceName: String, val getterFunction: (Int) -> Any?) {
    BOOLEAN("boolean", GL11::glGetBoolean),
    FLOAT("float", GL11::glGetFloat),
    DOUBLE("double", GL11::glGetDouble),
    INTEGER("int", GL11::glGetInteger),
    INTEGER64("long", GL32::glGetInteger64);
}

private data class GLVariable(val pname: Int, val type: GLType) : Comparable<GLVariable> {
    override fun compareTo(other: GLVariable): Int =
        pname.compareTo(other.pname).takeUnless { it == 0 } ?: type.compareTo(other.type)

    fun getName(nicePName: String?): String = (nicePName ?: pname.toString()) + " [${type.niceName}]"
}
interface IOpenGLDebugger {
    var printOnCycle: Int
    fun start()
    fun stop()
    fun cycle()
}

class OpenGLDebugger(override var printOnCycle: Int) : IOpenGLDebugger {
    private val probeGUIThread: Thread
    private var cycleCount = 0
    private var started = false
    private val glStuff = ChangeTrackingSortedMap<GLVariable, Any>()
    private val glVariablesToCheck = ArrayList<GLVariable>()
    private val debuggerLock = ReentrantLock()
    private val glVariablesCondition = debuggerLock.newCondition()
    private var glVariablesUpdated = false

    private inner class SpecialTableModel : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }

        override fun getColumnName(column: Int): String {
            return when (column) {
                0 -> "Key"
                1 -> "Value"
                else -> ""
            }
        }

        override fun getRowCount(): Int {
            return glStuff.size
        }

        override fun getColumnCount(): Int {
            return 2
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            return glStuff.getKeyOrNull(rowIndex)?.let { glVariable ->
                when (columnIndex) {
                    0 -> glVariable.getName(niceToHave[glVariable.pname])
                    1 -> glStuff.getValueOrNull(rowIndex) ?: "<NULL>"
                    else -> ""
                }
            } ?: "<NOT FOUND>"
        }
    }

    private inner class SpecialTableCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            if (table == null) {
                return this
            }
            background = null
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            if (glStuff.wasUpdated(row)) {
                background = Color.YELLOW
            } else if (glStuff.wasAdded(row)) {
                background = Color.GREEN
            }
            return this
        }
    }

    private inner class ProbeGUI : JDialog(), Runnable {
        private val mainPanel: JPanel
        private val variableMonitorTable: JTable
        private val closeButton: JButton

        init {
            for (info in UIManager.getInstalledLookAndFeels()) {
                if ("Windows" == info.name) {
                    try {
                        UIManager.setLookAndFeel(info.className)
                    } catch (e: ReflectiveOperationException) {
                        e.printStackTrace()
                    } catch (e: UnsupportedLookAndFeelException) {
                        e.printStackTrace()
                    }
                    break
                }
            }
            mainPanel = JPanel()
            closeButton = JButton().apply {
                text = "Close"
            }
            variableMonitorTable =
                JTable().apply {
                    isEnabled = false
                    model = SpecialTableModel()
                    columnModel.getColumn(0).cellRenderer = SpecialTableCellRenderer()
                }
            setupUI()
            type = Type.UTILITY
            contentPane = mainPanel
            getRootPane().defaultButton = closeButton
            defaultCloseOperation = DO_NOTHING_ON_CLOSE
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    stop()
                }
            })
            closeButton.addActionListener { stop() }
            mainPanel.registerKeyboardAction(
                { stop() },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
            )
        }

        override fun run() {
            glVariablesToCheck.clear()
            glVariablesToCheck.addAll(
                niceToHave.keys.flatMap { key ->
                    GLType.values().map { glType -> GLVariable(key, glType) }
                }
            )
            pack()
            isVisible = true
            while (started) {
                debuggerLock.withLock {
                    try {
                        while (!glVariablesUpdated) {
                            glVariablesCondition.await()
                        }
                        glVariablesUpdated = false
                        updateVariables()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        private fun updateVariables() {
            val dtm = variableMonitorTable.model as DefaultTableModel
            dtm.fireTableDataChanged()
        }

        private fun setupUI() = mainPanel.apply {
            layout = GridBagLayout()
            border = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                "null",
            )

            add(
                closeButton,
                GridBagConstraints().apply {
                    gridx = 2
                    gridy = 1
                    fill = GridBagConstraints.HORIZONTAL
                    insets = Insets(0, 5, 0, 0)
                },
            )

            add(
                JTextField().apply {
                    text = "Search…"
                    // TODO: add functionality
                },
                GridBagConstraints().apply {
                    gridx = 0
                    gridy = 1
                    weightx = 1.0
                    anchor = GridBagConstraints.WEST
                    fill = GridBagConstraints.HORIZONTAL
                },
            )

            add(
                JScrollPane().apply {
                    horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                    setViewportView(variableMonitorTable)
                },
                GridBagConstraints().apply {
                    gridx = 0
                    gridy = 0
                    gridwidth = 3
                    weightx = 1.0
                    weighty = 1.0
                    fill = GridBagConstraints.BOTH
                    insets = Insets(0, 0, 5, 0)
                },
            )
        }
    }

    init {
        require(printOnCycle >= 1) { "Print per cycle must be at least 1" }
        probeGUIThread = Thread(
            /* target = */ {
                synchronized(niceToHave) {
                    if (niceToHave.isEmpty()) {
                        updateNiceToHave()
                    }
                }
                ProbeGUI().run()
            },
            /* name = */ "LogisticsPipes GLDebug Probe #$probeID",
        )
        probeID++
    }

    override fun start() {
        if (!started) {
            started = true
            cycleCount = 0
            probeGUIThread.start()
        }
    }

    override fun stop() {
        if (started) {
            debuggerLock.withLock {
                started = false
                glVariablesUpdated = true
                glVariablesCondition.signal()
            }
        }
    }

    override fun cycle() {
        if (!started) {
            start()
        }
        ++cycleCount
        if (cycleCount % printOnCycle == 0) {
            saveOpenGLStuff()
            cycleCount = 0
        }
    }

    private fun saveOpenGLStuff() {
        debuggerLock.withLock {
            glStuff.startSession()
            val toRemove = glVariablesToCheck.filter { variable ->
                LogisticsPipes.log.warn("Getting ${variable.getName(niceToHave[variable.pname])}")
                val value = variable.type.getterFunction(variable.pname)
                val glError = GL11.glGetError()
                val isInvalid = glError == GL11.GL_INVALID_ENUM
                if (glError != GL11.GL_NO_ERROR) {
                    LogisticsPipes.log.warn("  got error ${GLU.gluErrorString(glError)}")
                }
                value?.takeUnless { isInvalid }?.also {
                    glStuff.put(variable, it)
                }
                return@filter isInvalid
            }.toSet()
            glVariablesToCheck.removeAll(toRemove)
            glStuff.stopSession()
            glVariablesUpdated = true
            glVariablesCondition.signal()
        }
    }

    companion object {
        private val niceToHave: HashMap<Int, String> = HashMap()
        private var probeID = 0

        private fun updateNiceToHave() {
            niceToHave.clear()
            var crawlerVersion = 11
            var almostEnd = false
            var end = false
            while (!end) {
                val packageGL = String.format("%s%d", "GL", crawlerVersion)
                val nextGL = String.format("%s.%s", "org.lwjgl.opengl", packageGL)
                try {
                    crawlerVersion++
                    val glClass = GL11::class.java.classLoader.loadClass(nextGL)
                    @Suppress("UnstableApiUsage")
                    Reflection.initialize(glClass)
                    almostEnd = false
                    for (f in glClass.declaredFields) {
                        try {
                            if (f.type != Int::class.javaPrimitiveType) {
                                continue
                            }
                            val id = f.getInt(null)
                            if (id == GL31.GL_INVALID_INDEX) {
                                continue
                            }
                            val nice = f.name
                            if (nice.endsWith("BIT")) {
                                continue
                            }

                            // All the things that are being replaced are not that bad
                            if (niceToHave.containsKey(id) && niceToHave[id] != nice) {
                                System.out.printf(
                                    "NiceToHave: ID %d exists. Replacing %s with %s!!%n",
                                    id,
                                    niceToHave.remove(id),
                                    nice
                                )
                            }
                            niceToHave[id] = String.format("%s.%s", packageGL, nice)
                        } catch (e: IllegalArgumentException) {
                            System.out.printf("NiceToHave: Illegal Argument!%nNiceToHave: %s%n", e)
                            e.printStackTrace()
                        } catch (e: IllegalAccessException) {
                            System.out.printf("NiceToHave: Illegal Access!%nNiceToHave: %s%n", e)
                            e.printStackTrace()
                        }
                    }
                } catch (e: ClassNotFoundException) {
                    if (almostEnd) {
                        end = true
                    } else {
                        almostEnd = true
                        crawlerVersion = (crawlerVersion / 10 + 1) * 10
                    }
                }
            }
        }
    }
}

@Suppress("unused")
object GlobalOpenGLDebugger : IOpenGLDebugger by OpenGLDebugger(60)
