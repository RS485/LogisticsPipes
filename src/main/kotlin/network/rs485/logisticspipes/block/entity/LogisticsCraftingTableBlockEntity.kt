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

package network.rs485.logisticspipes.block.entity

import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.container.Container
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.DefaultedList
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.init.BlockEntityTypes

class LogisticsCraftingTableBlockEntity(val fuzzy: Boolean): LockableContainerBlockEntity(BlockEntityTypes.CraftingTable) {

    val buffer = DefaultedList.ofSize(18, ItemStack.EMPTY)
    val grid = DefaultedList.ofSize(9, ItemStack.EMPTY)
    var output = ItemStack.EMPTY

    override fun getInvStack(slot: Int): ItemStack {
        return buffer[slot]
    }

    override fun canPlayerUseInv(player: PlayerEntity): Boolean {
        return true
    }

    override fun removeInvStack(slot: Int): ItemStack {
        return buffer.removeAt(slot)
    }

    override fun isInvEmpty(): Boolean {
        return buffer.all(ItemStack::isEmpty)
    }

    override fun clear() {
        buffer.clear()
    }

    override fun createContainer(var1: Int, inv: PlayerInventory): Container {
        TODO("not implemented")
    }

    override fun takeInvStack(slot: Int, count: Int): ItemStack {
        return buffer[slot].split(count)
    }

    override fun setInvStack(slot: Int, stack: ItemStack) {
        buffer[slot] = stack
    }

    override fun getInvSize(): Int {
        return buffer.size
    }

    override fun getContainerName(): Text {
        return TranslatableText("container.$ModID.crafting_table")
    }

}