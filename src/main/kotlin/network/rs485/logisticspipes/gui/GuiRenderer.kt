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

import network.rs485.logisticspipes.gui.guidebook.Drawable
import network.rs485.logisticspipes.gui.widget.*
import network.rs485.logisticspipes.util.IRectangle

object GuiRenderer : WidgetRenderer<WidgetContainer> {

    private fun createWidget(container: WidgetContainer, component: GuiComponent): LPGuiWidget? = when (component) {
        is PropertyLabel<*, *> -> LabelWidget(
            parent = container,
            width = component.width,
            xPosition = component.horizontalAlignment,
            yPosition = component.verticalAlignment,
            xSize = component.horizontalSize,
            margin = component.margin,
            text = component.text,
            textColor = component.textColor,
            textAlignment = component.textAlignment,
            extendable = component.extendable,
            backgroundColor = component.backgroundColor,
        ).apply {
            component.onPropertyUpdate { newText ->
                updateText(newText)
            }
        }

        is PropertyButton<*, *> -> TextButton(
            parent = container,
            xPosition = component.horizontalAlignment,
            yPosition = component.verticalAlignment,
            xSize = Size.GROW,
            ySize = Size.FIXED,
            margin = component.margin,
            text = component.text,
            enabled = component.enabled,
            onClickAction = {
                component.action.invoke()
                return@TextButton true
            }
        ).apply {
            component.onPropertyUpdate { newText ->
                text = newText
            }
        }

        is Label -> LabelWidget(
            parent = container,
            width = component.width,
            xPosition = component.horizontalAlignment,
            yPosition = component.verticalAlignment,
            xSize = component.horizontalSize,
            margin = component.margin,
            text = component.text,
            textColor = component.textColor,
            textAlignment = component.textAlignment,
            extendable = component.extendable,
            backgroundColor = component.backgroundColor,
        )

        is Button -> TextButton(
            parent = container,
            xPosition = component.horizontalAlignment,
            yPosition = component.verticalAlignment,
            xSize = Size.GROW,
            ySize = Size.FIXED,
            margin = component.margin,
            text = component.text,
            enabled = component.enabled,
            onClickAction = {
                // FIXME: filter mouse button
                component.action.invoke()
                return@TextButton true
            }
        )

        is CustomSlots -> SlotGroup(
            parent = container,
            xPosition = component.horizontalAlignment,
            yPosition = component.verticalAlignment,
            margin = component.margin,
            slots = component.slots,
            columns = component.columns,
            rows = component.rows,
        )

        is PlayerSlots -> PlayerInventorySlotGroup(
            parent = container,
            xPosition = component.horizontalAlignment,
            yPosition = component.verticalAlignment,
            margin = component.margin,
            slots = component.slots,
        )

        is ComponentContainer -> createContainer(
            container = component,
            parent = container,
        )

        else -> println("[GuiRenderer.createWidget] Ignoring $component").let { null }
    }

    private fun createContainer(
        container: ComponentContainer,
        parent: Drawable? = null,
    ): WidgetContainer {
        val list = mutableListOf<LPGuiWidget>()
        val result = when (container) {
            is HContainer -> HorizontalWidgetContainer(list, parent, container.margin, container.gap)
            is VContainer -> VerticalWidgetContainer(list, parent, container.margin, container.gap)
            else -> throw IllegalArgumentException("")
        }
        container.children.forEach { child ->
            createWidget(result, child)?.also { list.add(it) }
        }
        return result
    }

    override fun render(componentContainer: ComponentContainer, body: IRectangle): WidgetContainer =
        createContainer(componentContainer)
}
