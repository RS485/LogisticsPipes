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

package network.rs485.logisticspipes.connection

import logisticspipes.LPConstants
import logisticspipes.LogisticsPipes
import logisticspipes.interfaces.WrappedInventory
import logisticspipes.pipes.basic.LogisticsTileGenericPipe
import logisticspipes.utils.InventoryUtilFactory
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.Direction
import java.util.*

open class NeighborBlockEntity<T : BlockEntity>(val blockEntity: T, val direction: Direction) {
    open fun getOurDirection(): Direction {
        return direction.opposite
    }

    inline fun <reified C : T> getInstanceOf(): NeighborBlockEntity<C>? {
        return if (blockEntity is C) NeighborBlockEntity(blockEntity, direction) else null
    }

    fun <C : T> getJavaInstanceOf(clazz: Class<C>): Optional<NeighborBlockEntity<C>> {
        return if (clazz.isInstance(blockEntity)) {
            Optional.of(NeighborBlockEntity(clazz.cast(blockEntity), direction))
        } else {
            Optional.empty()
        }
    }

    fun getInventoryUtil(): WrappedInventory? {
        return InventoryUtilFactory.INSTANCE.getInventoryUtil(blockEntity, getOurDirection())
    }

    fun getUtilForItemHandler(): WrappedInventory {
        if (LPConstants.DEBUG) {
            if (!hasCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY)) {
                error("Constraint broken: getUtilForItemHandler was called, but adjacent tile entity is not an item handler")
            }
        }
        return getInventoryUtil() ?: throw NullPointerException("IInventoryUtil is null for an item handler")
    }

    fun isLogisticsPipe(): Boolean {
        return blockEntity is LogisticsTileGenericPipe
    }

    fun isItemHandler(): Boolean {
        return hasCapability(LogisticsPipes.ITEM_HANDLER_CAPABILITY) // david said to use LogisticsPipes.ITEM_HANDLER_CAPABILITY
    }

    fun sneakyInsertion(): NeighborBlockEntitySneakyInsertion<T> {
        return NeighborBlockEntitySneakyInsertion(blockEntity, direction)
    }
}

