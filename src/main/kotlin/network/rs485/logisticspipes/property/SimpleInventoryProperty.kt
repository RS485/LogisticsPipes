/*
 * Copyright (c) 2023  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2023  RS485
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

package network.rs485.logisticspipes.property

import logisticspipes.utils.item.SimpleStackInventory
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.concurrent.CopyOnWriteArraySet

class SimpleInventoryProperty(private val inv: SimpleStackInventory, override val tagKey: String) :
    Property<SimpleStackInventory>, IInventory by inv, Collection<ItemStack> {

    override val propertyObservers: CopyOnWriteArraySet<ObserverCallback<SimpleStackInventory>> =
        CopyOnWriteArraySet()

    override fun copyValue(): SimpleStackInventory = SimpleStackInventory(inv)

    override fun copyProperty(): Property<SimpleStackInventory> = SimpleInventoryProperty(copyValue(), tagKey)

    override fun readFromNBT(tag: NBTTagCompound) = inv.readFromNBT(tag, tagKey)

    override fun writeToNBT(tag: NBTTagCompound) = inv.writeToNBT(tag, tagKey)

    override val size: Int = sizeInventory

    override fun iterator(): Iterator<ItemStack> = inv.map { it -> it.component1() }.iterator()

    override fun containsAll(elements: Collection<ItemStack>): Boolean =
        inv.map { it -> it.component1() }.containsAll(elements)

    override fun contains(element: ItemStack): Boolean =
        inv.map { it -> it.component1() }.contains(element)

    fun clearInventorySlotContents(i: Int) = inv.clearInventorySlotContents(i)

    fun dropContents(world: World, pos: BlockPos) = inv.dropContents(world, pos)

    fun addCompressed(left: ItemStack, b: Boolean): Int = inv.addCompressed(left, b)

}
