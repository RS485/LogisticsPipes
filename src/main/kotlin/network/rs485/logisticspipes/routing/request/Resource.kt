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

package network.rs485.logisticspipes.routing.request

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import logisticspipes.interfaces.routing.FluidRequester
import logisticspipes.interfaces.routing.ItemRequester
import logisticspipes.routing.Router
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.init.Items
import network.rs485.logisticspipes.item.FluidContainerItem
import network.rs485.logisticspipes.util.ItemVariant

sealed class Resource(private val type: Identifier) {

    abstract val router: Router
    abstract val displayText: Text
    abstract val displayStack: ItemStack

    abstract var requestedAmount: Int

    fun toTag() = toTag(CompoundTag())

    open fun toTag(tag: CompoundTag): CompoundTag {
        tag.putString("type", type.toString())
        return tag
    }

    @Deprecated("Convenience method until fluids in pipes aren't stored as items anymore")
    fun matchesAny(stack: ItemStack, ignoreNbt: Boolean): Boolean {
        return if (this is Fluid && stack.item == Items.FluidContainer) {
            matches(FluidContainerItem.getFluid(stack).fluidKey)
        } else if (stack.item != Items.FluidContainer) when (this) {
            is Item -> matches(stack, ignoreNbt)
            is Dict -> matches(stack, ignoreNbt)
            else -> false
        } else false
    }

    abstract fun copy(): Resource

    open class Item(val stack: ItemStack, val requester: ItemRequester?) : Resource(Identifier(ModID, "item")) {
        override val router: Router
            get() = requester?.router ?: error("Requester is null!")

        override val displayText: Text
            get() = stack.name

        override val displayStack: ItemStack
            get() = stack

        override var requestedAmount: Int
            get() = stack.count
            set(value) {
                stack.count = value
            }

        fun matches(stack: ItemStack, ignoreNbt: Boolean): Boolean {
            return matches(ItemVariant.fromStack(stack), ignoreNbt)
        }

        fun matches(item: ItemVariant, ignoreNbt: Boolean): Boolean {
            return stack.item == item.item &&
                    (ignoreNbt || stack.tag == item.tag)
        }

        open fun withStack(stack: ItemStack) = Item(stack, requester)

        override fun toTag(tag: CompoundTag): CompoundTag {
            super.toTag(tag)
            tag.put("item", stack.toTag(CompoundTag()))
            return tag
        }

        override fun copy(): Item {
            return Item(stack.copy(), requester)
        }

        override fun toString(): String {
            return "Item(stack=$stack, requester=$requester)"
        }

        companion object {
            fun fromTag(tag: CompoundTag): Item {
                val stack = ItemStack.fromTag(tag.getCompound("item"))
                return Item(stack, null)
            }
        }
    }

    // TODO
    class Dict(stack: ItemStack, requester: ItemRequester?) : Item(stack, requester) {

        override fun withStack(stack: ItemStack) = Dict(stack, requester)

        override fun toString(): String {
            return "Dict(stack=$stack, requester=$requester)"
        }

        companion object {
            fun fromTag(tag: CompoundTag): Dict {
                val stack = ItemStack.fromTag(tag.getCompound("item"))
                return Dict(stack, null)
            }
        }

    }

    class Fluid(volume: FluidVolume, val requester: FluidRequester?) : Resource(Identifier(ModID, "fluid")) {

        var volume = volume
            private set

        override val router: Router
            get() = requester?.router ?: error("Requester is null!")

        override val displayText: Text
            get() = TranslatableText("misc.$ModID.fluid_volume_fmt", volume.name, volume.fluidKey.unit.localizeAmount(volume.amount))

        override val displayStack: ItemStack
            get() = FluidContainerItem.makeStack(volume)

        override var requestedAmount: Int
            get() = volume.amount
            set(value) {
                volume = volume.fluidKey.withAmount(value)
            }

        fun matches(fluid: FluidKey): Boolean {
            return volume.fluidKey == fluid
        }

        override fun toTag(tag: CompoundTag): CompoundTag {
            super.toTag(tag)
            tag.put("fluid", volume.toTag())
            return tag
        }

        override fun copy(): Fluid {
            return Fluid(volume.copy(), requester)
        }

        override fun toString(): String {
            return "Fluid(requester=$requester, volume=$volume)"
        }

        companion object {
            fun fromTag(tag: CompoundTag): Fluid {
                val volume = FluidVolume.fromTag(tag.getCompound("fluid"))
                return Fluid(volume, null)
            }
        }
    }

    // TODO: fuzzy resource (ignore nbt, tag based match)

    companion object {
        fun fromTag(tag: CompoundTag): Resource {
            return when (val type = tag.getString("type")) {
                "$ModID:item" -> Item.fromTag(tag)
                "$ModID:fluid" -> Fluid.fromTag(tag)
                else -> error("Invalid resource type '$type'!")
            }
        }
    }

}