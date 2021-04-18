/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
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

import logisticspipes.utils.SinkReply
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.item.ItemStack
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory
import kotlin.math.min


fun ItemIdentifier.equalsWithNBT(stack: ItemStack): Boolean = this.item == stack.item &&
        this.itemDamage == stack.itemDamage &&
        ((this.tag == null && stack.tagCompound == null) ||
                (this.tag != null && stack.tagCompound != null && this.tag == stack.tagCompound))

fun IItemIdentifierInventory.matchingSequence(stack: ItemStack) =
    (0 until sizeInventory).asSequence().map { getIDStackInSlot(it) }
        .filter { it != null && it.item.equalsWithoutNBT(stack) }


fun getExtractionMax(stackCount: Int, maxExtractionCount: Int, sinkReply: SinkReply): Int {
    return min(stackCount, maxExtractionCount).let {
        if (sinkReply.maxNumberOfItems > 0) {
            min(it, sinkReply.maxNumberOfItems)
        } else it
    }
}
