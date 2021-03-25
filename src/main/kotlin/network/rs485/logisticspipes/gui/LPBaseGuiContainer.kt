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

import logisticspipes.LPConstants
import logisticspipes.asm.ModDependentInterface
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import network.rs485.logisticspipes.util.math.Rectangle

@ModDependentInterface(modId = [LPConstants.neiModID], interfacePath = ["codechicken.nei.api.INEIGuiHandler"])
abstract class LPBaseGuiContainer(inventorySlotsIn: Container, widthIn: Int, heightIn: Int, xOffset: Int = 0, yOffset: Int = 0) : GuiContainer(inventorySlotsIn) {

    val screenArea = Rectangle(width, height)
    val guiArea = Rectangle((width - widthIn) / 2, (height - heightIn) / 2 , widthIn, heightIn)

    val left: Int get() = guiArea.roundedLeft
    val top: Int get() = guiArea.roundedTop
    val right: Int get() = guiArea.roundedRight
    val bottom: Int get() = guiArea.roundedBottom

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        //helper.drawGuibackground(guiArea)
    }

    companion object {
        val helper = LPGuiDrawer
    }

}