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

package network.rs485.logisticspipes.gui

import network.rs485.logisticspipes.property.Property
import network.rs485.logisticspipes.property.PropertyLayer

@DslMarker
annotation class GuiComponentMarker

@GuiComponentMarker
sealed class GuiComponent {
    val children = arrayListOf<GuiComponent>()

    protected fun <T : GuiComponent> initComponent(component: T, init: T.() -> Unit): T {
        component.apply(init)
        children.add(component)
        return component
    }
}

/**
 * Base container component
 */
abstract class WidgetContainer : GuiComponent() {

    fun row(init: Row.() -> Unit) = initComponent(Row(), init)
    fun col(init: Col.() -> Unit) = initComponent(Col(), init)
    fun <V : Any, P : Property<V>> label(init: PropertyLabel<V, P>.() -> Unit) = initComponent(PropertyLabel(), init)
    fun <V : Any, P : Property<V>> button(init: PropertyButton<V, P>.() -> Unit) = initComponent(PropertyButton(), init)
    fun button(init: Button.() -> Unit) = initComponent(Button(), init)

}

/**
 * Arranges contained widgets side-by-side horizontally
 */
class Row : WidgetContainer()

/**
 * Arranges contained widgets side-by-side vertically
 */
class Col : WidgetContainer()

/**
 * Represents a static piece of text
 */
open class Label : GuiComponent() {
    var text: String = ""
}

interface PropertyAware {
    fun onPropertyUpdate(callback: (String) -> Unit)
}

class PropertyLabel<V : Any, P : Property<V>> : Label(), PropertyAware {
    lateinit var propertyLayer: PropertyLayer
    lateinit var property: P
    var propertyToText: (V) -> String = Any::toString

    override fun onPropertyUpdate(callback: (String) -> Unit) {
        propertyLayer.addObserver(property) {
            callback.invoke(propertyToText.invoke(it.copyValue()))
        }
    }
}

open class Button : GuiComponent() {
    var text: String = ""
    var action: () -> Unit = {}
}

class PropertyButton<V : Any, P : Property<V>> : Button(), PropertyAware {
    lateinit var propertyLayer: PropertyLayer
    lateinit var property: P
    var propertyToText: (V) -> String = Any::toString
    override fun onPropertyUpdate(callback: (String) -> Unit) {
        propertyLayer.addObserver(property) {
            callback.invoke(propertyToText.invoke(it.copyValue()))
        }
    }
}

fun widgetContainer(init: WidgetContainer.() -> Unit): WidgetContainer =
    object : WidgetContainer() {}.apply(init)
