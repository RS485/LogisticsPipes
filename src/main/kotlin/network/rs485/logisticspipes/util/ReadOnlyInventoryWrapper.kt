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

import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import kotlin.math.min

class ReadOnlyInventoryWrapper(val wrapped: Inventory) : Inventory by wrapped {

    override fun getInvStack(slot: Int): ItemStack {
        return wrapped.getInvStack(slot).copy()
    }

    override fun markDirty() {}

    override fun setInvStack(slot: Int, stack: ItemStack) {}

    override fun removeInvStack(slot: Int): ItemStack {
        return getInvStack(slot)
    }

    override fun takeInvStack(slot: Int, count: Int): ItemStack {
        val stack = getInvStack(slot)
        stack.count = min(count, stack.count)
        return stack
    }

    companion object {
        @JvmStatic
        fun wrap(inventory: Inventory): Inventory {
            return when (inventory) {
                is ReadOnlyInventoryWrapper, is ReadOnlySidedInventoryWrapper -> inventory
                is SidedInventory -> ReadOnlySidedInventoryWrapper(inventory)
                else -> ReadOnlyInventoryWrapper(inventory)
            }
        }
    }
}

class ReadOnlySidedInventoryWrapper(val wrapped: SidedInventory) : SidedInventory by wrapped {

    override fun getInvStack(slot: Int): ItemStack {
        return wrapped.getInvStack(slot).copy()
    }

    override fun markDirty() {}

    override fun setInvStack(slot: Int, stack: ItemStack) {}

    override fun removeInvStack(slot: Int): ItemStack {
        return getInvStack(slot)
    }

    override fun takeInvStack(slot: Int, count: Int): ItemStack {
        val stack = getInvStack(slot)
        stack.count = min(count, stack.count)
        return stack
    }
}