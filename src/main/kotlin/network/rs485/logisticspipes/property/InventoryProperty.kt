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

package network.rs485.logisticspipes.property

import logisticspipes.utils.item.ItemIdentifierInventory
import logisticspipes.utils.item.ItemIdentifierStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory
import network.rs485.logisticspipes.inventory.SlotAccess
import java.util.concurrent.CopyOnWriteArraySet

class InventoryProperty(private val inv: ItemIdentifierInventory, override val tagKey: String) :
    Property<ItemIdentifierInventory>, IItemIdentifierInventory by inv {

    override val slotAccess: SlotAccess = object : SlotAccess by inv.slotAccess {
        override fun mergeSlots(intoSlot: Int, fromSlot: Int) =
            inv.slotAccess.mergeSlots(intoSlot, fromSlot).alsoIChanged()
    }

    override val propertyObservers: CopyOnWriteArraySet<ObserverCallback<ItemIdentifierInventory>> =
        CopyOnWriteArraySet()

    override fun decrStackSize(index: Int, count: Int): ItemStack = inv.decrStackSize(index, count).alsoIChanged()

    override fun removeStackFromSlot(index: Int): ItemStack = inv.removeStackFromSlot(index).alsoIChanged()

    override fun setInventorySlotContents(index: Int, stack: ItemStack) =
        inv.setInventorySlotContents(index, stack).alsoIChanged()

    override fun setInventorySlotContents(i: Int, itemstack: ItemIdentifierStack?) =
        inv.setInventorySlotContents(i, itemstack).alsoIChanged()

    override fun setField(id: Int, value: Int) = inv.setField(id, value).alsoIChanged()

    override fun handleItemIdentifierList(_allItems: Collection<ItemIdentifierStack>) =
        inv.handleItemIdentifierList(_allItems).alsoIChanged()

    override fun clear() = inv.clear().alsoIChanged()

    override fun recheckStackLimit() = inv.recheckStackLimit().alsoIChanged()

    override fun clearInventorySlotContents(i: Int) = inv.clearInventorySlotContents(i).alsoIChanged()

    override fun readFromNBT(tag: NBTTagCompound) {
        if (tagKey.isEmpty() || tag.hasKey(tagKey)) inv.readFromNBT(tag, tagKey).alsoIChanged()
    }

    override fun writeToNBT(tag: NBTTagCompound) = inv.writeToNBT(tag, tagKey)

    override fun copyValue(): ItemIdentifierInventory = ItemIdentifierInventory(inv)

    override fun copyProperty(): InventoryProperty = InventoryProperty(copyValue(), tagKey)

}
