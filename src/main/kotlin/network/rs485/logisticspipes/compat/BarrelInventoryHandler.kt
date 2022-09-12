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

package network.rs485.logisticspipes.compat

import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import network.rs485.logisticspipes.inventory.ProviderMode
import pl.asie.charset.api.storage.IBarrel

class CharsetImplementationFactory : SpecialInventoryHandler.Factory {

    companion object {
        @JvmStatic
        @CapabilityInject(IBarrel::class)
        val barrelCapability: Capability<IBarrel>? = null
    }

    override fun init(): Boolean = true

    override fun isType(tile: TileEntity, dir: EnumFacing?): Boolean = barrelCapability?.let { tile.hasCapability(it, dir) } ?: false

    override fun getUtilForTile(
            tile: TileEntity,
            direction: EnumFacing?,
            mode: ProviderMode
    ): SpecialInventoryHandler = BarrelInventoryHandler(tile.getCapability(barrelCapability!!, direction)!!, mode)

}

class BarrelInventoryHandler(val tile: IBarrel, val mode: ProviderMode) : SpecialInventoryHandler() {

    override fun decrStackSize(slot: Int, amount: Int): ItemStack {
        if (slot != 0) return ItemStack.EMPTY
        return tile.extractItem(amount, false)
    }

    override fun containsUndamagedItem(item: ItemIdentifier): Boolean =
            ItemIdentifier.get(getItem()).undamaged.equals(item)

    override fun add(stack: ItemStack, orientation: EnumFacing?, doAdd: Boolean): ItemStack {
        if (tile.shouldInsertToSide(orientation) && (isEmpty() || isValidItem(ItemIdentifier.get(stack)))) {
            val leftover = tile.insertItem(stack, !doAdd)
            return if (leftover.isEmpty) {
                stack
            } else {
                assert(leftover.item == stack.item)
                stack.copy().also { it.count -= leftover.count }
            }
        }
        return stack
    }

    override fun getMultipleItems(itemIdent: ItemIdentifier, count: Int): ItemStack {
        if (!isEmpty() && isValidItem(itemIdent)) {
            if (itemCount(itemIdent) >= count) {
                return tile.extractItem(count, false)
            }
        }
        return ItemStack.EMPTY
    }

    override fun getSingleItem(itemIdent: ItemIdentifier): ItemStack = getMultipleItems(itemIdent, 1)

    override fun getItems(): MutableSet<ItemIdentifier> = mutableSetOf(ItemIdentifier.get(getItem()))

    override fun getItemsAndCount(): MutableMap<ItemIdentifier, Int> =
            mutableMapOf(getIdentifier() to itemCount(getIdentifier()))

    override fun getSizeInventory(): Int = 1

    override fun getStackInSlot(slot: Int): ItemStack {
        if (slot != 0) return ItemStack.EMPTY
        val stack = getItem()
        stack.count = itemCount(getIdentifier())
        return if (stack.isEmpty) ItemStack.EMPTY
        else stack
    }

    override fun roomForItem(stack: ItemStack): Int {
        val identifier = ItemIdentifier.get(stack)
        return if (isValidItem(identifier)) {
            tile.maxItemCount - itemCount(identifier)
        } else 0
    }

    override fun itemCount(itemIdent: ItemIdentifier): Int {
        return if (!isEmpty() && isValidItem(itemIdent)) tile.itemCount.providerMode()
        else 0
    }

    /**
     * Correct amount for current provider mode.
     */
    private fun Int.providerMode(): Int {
        return if (mode.hideOnePerType || mode.hideOnePerStack) (this - 1)
        else this
    }

    /**
     * Checks if the barrel is empty.
     */
    private fun isEmpty() = getItem().isEmpty

    /**
     * Checks if the item matches the one inside the barrel.
     */
    private fun isValidItem(itemIdent: ItemIdentifier) = itemIdent.equals(ItemIdentifier.get(getItem()))

    /**
     * Returns an ItemStack with the same item as the one in the barrel.
     */
    private fun getItem(): ItemStack = tile.extractItem(1, true)

    /**
     * Returns the ItemIdentifier from the getItem method.
     */
    private fun getIdentifier(): ItemIdentifier = ItemIdentifier.get(getItem())
}