/*
 * Copyright (c) 2019  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019  RS485
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

package network.rs485.logisticspipes.item

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import logisticspipes.interfaces.ItemAdvancedExistence
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.world.World
import network.rs485.logisticspipes.ext.makeStack
import network.rs485.logisticspipes.init.Items
import network.rs485.logisticspipes.util.ItemVariant

class FluidContainerItem(settings: Settings) : Item(settings), ItemAdvancedExistence {

    override fun getName(stack: ItemStack): Text {
        val fluid = getFluid(stack)
        return fluid.name
    }

    override fun appendTooltip(stack: ItemStack, world: World?, list: MutableList<Text>, ctx: TooltipContext) {
        super.appendTooltip(stack, world, list, ctx)
        val fluid = getFluid(stack)
        list.add(LiteralText(fluid.fluidKey.unit.localizeAmount(fluid.amount)))
    }

    override fun canExistInNormalInventory(stack: ItemStack) = false

    override fun canExistInWorld(stack: ItemStack) = false

    companion object {
        fun getFluid(stack: ItemStack): FluidVolume {
            if (stack.item != Items.FluidContainer) error("Can't get fluid from $stack!")
            val tag = stack.getSubTag("fluid")
            return if (tag != null) FluidVolume.fromTag(tag)
            else FluidKeys.EMPTY.withAmount(0)
        }

        fun setFluid(stack: ItemStack, fluid: FluidVolume) {
            if (stack.item != Items.FluidContainer) error("Can't set fluid on $stack!")
            stack.putSubTag("fluid", fluid.toTag())
        }

        @Deprecated("Please don't, if you can avoid it.")
        fun getFluid(stack: ItemVariant): FluidVolume {
            if (stack.item != Items.FluidContainer) error("Can't get fluid from $stack!")
            val tag = stack.tag?.getCompound("fluid")
            return if (tag != null) FluidVolume.fromTag(tag)
            else FluidKeys.EMPTY.withAmount(0)
        }

        @Deprecated("Please don't, if you can avoid it.")
        fun setFluid(stack: ItemVariant, fluid: FluidVolume) {
            if (stack.item != Items.FluidContainer) error("Can't set fluid on $stack!")
            val tag = stack.tag ?: CompoundTag().also { stack.tag = it }
            tag.put("fluid", fluid.toTag())
        }

        fun makeStack(fluid: FluidVolume): ItemStack {
            val stack = Items.FluidContainer.makeStack()
            setFluid(stack, fluid)
            return stack
        }
    }

}