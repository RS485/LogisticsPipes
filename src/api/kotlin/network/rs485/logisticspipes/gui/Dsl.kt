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
import network.rs485.logisticspipes.property.layer.PropertyLayer

@DslMarker
annotation class GuiComponentMarker

@GuiComponentMarker
abstract class GuiComponent {
    var width: Int = -1
    var height: Int = -1
    val children = arrayListOf<GuiComponent>()
    var margin: Margin = Margin.NONE
    var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.LEFT
    var verticalAlignment: VerticalAlignment = VerticalAlignment.TOP
    var horizontalSize: Size = Size.GROW
    var verticalSize: Size = Size.GROW
    var enabled: Boolean = true

    open fun <T : GuiComponent> initComponent(component: T, init: T.() -> Unit): T {
        component.apply(init)
        children.add(component)
        return component
    }
}


abstract class ComponentContainer : GuiComponent() {

    var gap: Int = 1

    /**
     * Arranges contained widgets left-to-right horizontally
     */
    fun horizontal(init: HContainer.() -> Unit) = initComponent(HContainer(), init)

    /**
     * Arranges contained widgets top-to-bottom vertically
     */
    fun vertical(init: VContainer.() -> Unit) = initComponent(VContainer(), init)

    fun optionalComponent(init: OptionalComponent.() -> Unit) = initComponent(OptionalComponent(), init)

    /**
     * Represents a label with the text value based on a property.
     */
    fun <V : Any, P : Property<V>> label(init: PropertyLabel<V, P>.() -> Unit) = initComponent(PropertyLabel(), init)

    /**
     * Represents a button with the text value based on a property.
     */
    fun <V : Any, P : Property<V>> propertyButton(init: PropertyButton<V, P>.() -> Unit) =
        initComponent(PropertyButton(), init)

    /**
     * Represents a static button.
     */
    fun button(init: Button.() -> Unit) = initComponent(Button(), init)

    /**
     * Represents a static piece of text.
     */
    fun staticLabel(init: Label.() -> Unit) = initComponent(Label(), init)
}

/**
 * Used to construct a base container component.
 */
fun widgetContainer(init: VContainer.() -> Unit): VContainer =
    VContainer().apply(init)

class HContainer : ComponentContainer() {
    var alignment: HorizontalAlignment = HorizontalAlignment.LEFT
}

class VContainer : ComponentContainer() {
    var alignment: VerticalAlignment = VerticalAlignment.TOP
}

/**
 * Adds component to hierarchy if the predicate returns true.
 */
class OptionalComponent : ComponentContainer() {
    var predicate: () -> Boolean = { false }
    var vertical: Boolean = true
    private var addComponents: Boolean = false

    override fun <T : GuiComponent> initComponent(component: T, init: T.() -> Unit): T {
        if (addComponents) {
            component.apply(init)
            children.add(component)
        }
        return component
    }

    fun activeComponents(init: OptionalComponent.() -> Unit): OptionalComponent {
        if (predicate.invoke()) {
            addComponents = true
        }
        init(this)
        addComponents = false
        return this
    }

    fun inactiveComponents(init: OptionalComponent.() -> Unit): OptionalComponent {
        if (!predicate.invoke()) {
            addComponents = true
        }
        init(this)
        addComponents = false
        return this
    }
}

open class Label : GuiComponent() {
    var text: String = ""
    var textAlignment: HorizontalAlignment = HorizontalAlignment.LEFT
    var textColor: Int = 0
    var extendable: Boolean = false
    var backgroundColor: Int = 0
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
