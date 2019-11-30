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
package network.rs485.logisticspipes.util

import drawer.ForCompoundTag
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

// Effectively an item stack without item count. Equivalent to the old ItemIdentifier

@Serializable(ForItemVariant::class)
data class ItemVariant @JvmOverloads constructor(val item: Item, var tag: CompoundTag? = null) : ItemConvertible, Comparable<ItemVariant> {

    @JvmOverloads
    fun makeStack(count: Int = 1) = ItemStack(item, count).also { it.tag = tag?.copy() }

    fun isEmpty() = item == Items.AIR

    fun toTag(tag: CompoundTag = CompoundTag()): CompoundTag {
        val id = Registry.ITEM.getId(item)
        tag.putString("id", id.toString())
        this.tag?.also { tag.put("tag", it) }
        return tag
    }

    fun matches(stack: ItemStack) = stack.item == item && stack.tag == tag

    fun equalsForCrafting(other: ItemVariant) =
            item == other.item &&
                    (!item.isDamageable || makeStack().damage == other.makeStack().damage)

    override fun asItem(): Item = item

    fun copy() = ItemVariant(item, tag?.copy())

    override fun compareTo(other: ItemVariant): Int {
        return ItemComparator.compare(item, other.item)
    }

    companion object {
        @JvmStatic
        fun fromStack(stack: ItemStack) = ItemVariant(stack.item, stack.tag)

        @JvmStatic
        fun fromTag(tag: CompoundTag): ItemVariant {
            val id = Identifier(tag.getString("id"))
            val item = Registry.ITEM[id]
            val data = if (tag.contains("tag", NbtType.COMPOUND)) tag.getCompound("tag") else null
            return ItemVariant(item, data)
        }

        @JvmStatic
        fun stacksEqual(stack1: ItemStack, stack2: ItemStack): Boolean {
            return ItemStack.areItemsEqual(stack1, stack2) &&
                    ItemStack.areTagsEqual(stack1, stack2)
        }
    }

}

object ForItemVariant : KSerializer<ItemVariant> {
    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("ItemVariant") {
        init {
            addElement("id")
            addElement("tag")
        }
    }

    override fun serialize(encoder: Encoder, obj: ItemVariant) {
        ForCompoundTag.serialize(encoder, obj.toTag(CompoundTag()))
    }

    override fun deserialize(decoder: Decoder): ItemVariant {
        return ItemVariant.fromTag(ForCompoundTag.deserialize(decoder))
    }
}