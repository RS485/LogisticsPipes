/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
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

import logisticspipes.modules.ModuleProvider
import logisticspipes.network.packets.module.PropertyModuleUpdate
import logisticspipes.proxy.MainProxy
import logisticspipes.utils.string.StringUtils
import net.minecraft.inventory.IInventory
import network.rs485.logisticspipes.gui.widget.*
import network.rs485.logisticspipes.inventory.ProviderMode
import network.rs485.logisticspipes.property.*

// TODO create different buttons.
class ProviderGui(private val playerInventory: IInventory, private val providerModule: ModuleProvider) : LPBaseGuiContainer(ProviderPipeContainer(playerInventory, providerModule), 192, 186) {
    override var z = 1.0f

    private val prefix: String = "gui.providerpipe."

    private val propertyLayer: PropertyLayer = object : PropertyLayer(providerModule.propertyList) {
        override fun onChange(property: Property<*>) {}
    }

    private val providerMode: EnumProperty<ProviderMode> = propertyLayer.getWritableProperty(providerModule.providerMode)
    private val isExclusionFilter: BooleanProperty = propertyLayer.getWritableProperty(providerModule.isExclusionFilter)

    private val title: LPGuiLabel = LPGuiLabel(
            parent = this,
            text = providerModule.filterInventory.name,
            xPosition = Center,
            yPosition = Top(6),
            xSize = FullSize(6),
            textColor = helper.TEXT_DARK)
            .setAlignment(HorizontalAlignment.CENTER)
    private val extractionModeLabel: LPGuiLabel = LPGuiLabel(
            parent = this,
            xPosition = Left(6),
            yPosition = Top(80),
            xSize = FullSize(6),
            textColor = helper.TEXT_DARK)
            .setExtendable(true, helper.BACKGROUND_LIGHT)
    private val extractionModeButton: TextButton = TextButton(
            parent = this,
            xPosition = Left(6),
            yPosition = Top(35),
            xSize = AbsoluteSize(50),
            ySize = AbsoluteSize(20),
            onClickAction = { mouseButton ->
                if (mouseButton == 0) {
                    providerMode.next()
                    extractionModeLabel.updateText("${StringUtils.translate("${prefix}ExcessInventory")} ${providerMode.value.extractionModeString}")
                    return@TextButton true
                }
                return@TextButton false
            })
    private val providerModeButton: BiStateButton = BiStateButton(
            parent = this,
            xPosition = Right(6),
            yPosition = Top(35),
            xSize = AbsoluteSize(50),
            ySize = AbsoluteSize(20),
            initialValue = propertyLayer.getLayerValue(providerModule.isExclusionFilter),
            trueText = StringUtils.translate("${prefix}Include"),
            falseText = StringUtils.translate("${prefix}Exclude"),
            valueGetter = {
                propertyLayer.getLayerValue(providerModule.isExclusionFilter)
            },
            onClickAction = { mouseButton ->
                if (mouseButton == 0) {
                    isExclusionFilter.toggle()
                    return@BiStateButton true
                }
                return@BiStateButton false
            }
    )

    override fun initGui() {
        super.initGui()
        addWidget(title)
        addWidget(extractionModeLabel)
        addWidget(extractionModeButton)
        addWidget(providerModeButton)
    }

    override fun drawFocalgroundLayer(mouseX: Float, mouseY: Float, partialTicks: Float) {
        for (guiButton in buttonList) {
            guiButton.drawButton(mc, mouseX.toInt(), mouseY.toInt(), partialTicks)
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        propertyLayer.unregister()
        if (mc.player != null && !propertyLayer.changedProperties().isEmpty()) {
            // send update to server, when there are changed properties
            MainProxy.sendPacketToServer(PropertyModuleUpdate.fromLayer(propertyLayer).setModulePos(providerModule))
        }
    }
}