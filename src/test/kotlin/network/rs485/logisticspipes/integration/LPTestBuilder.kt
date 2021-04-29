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

package network.rs485.logisticspipes.integration

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.time.withTimeoutOrNull
import logisticspipes.LPBlocks
import logisticspipes.LPItems
import logisticspipes.blocks.LogisticsSolidBlock
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity
import logisticspipes.pipes.PipeItemsBasicLogistics
import logisticspipes.pipes.PipeItemsProviderLogistics
import logisticspipes.pipes.PipeItemsRequestLogistics
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.block.BlockChest
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.EnumFacing
import network.rs485.grow.ServerTickDispatcher
import network.rs485.minecraft.BlockPlacer
import network.rs485.minecraft.BlockPosSelector
import java.time.Duration

fun BlockPosSelector.setupLogisticsPower(
    direction: EnumFacing,
    amount: Float,
): Pair<PipePlacer<PipeItemsBasicLogistics>, BlockPlacer<LogisticsSolidBlock>> =
    resetOffsetAfter {
        val basicPipePlacer = PipePlacer(PipeItemsBasicLogistics(LPItems.pipeBasic))
            .also { direction(direction).place(it) }
        val powerJunctionPlacer = BlockPlacer(LPBlocks.powerJunction) {
            it.getTileEntity<LogisticsPowerJunctionTileEntity>().apply {
                addEnergy(amount)
            }
        }.also { direction(direction).place(it) }
        basicPipePlacer to powerJunctionPlacer
    }

fun BlockPosSelector.setupProvidingChest(
    direction: EnumFacing,
    vararg stacks: ItemStack,
): Pair<PipePlacer<PipeItemsProviderLogistics>, BlockPlacer<BlockChest>> =
    resetOffsetAfter {
        val providerPlacer = PipePlacer(PipeItemsProviderLogistics(LPItems.pipeProvider))
            .also { direction(direction).place(it) }
        val chestPlacer = BlockPlacer(Blocks.CHEST) {
            it.getTileEntity<TileEntityChest>().apply {
                stacks.forEachIndexed { index, itemStack -> setInventorySlotContents(index, itemStack) }
            }
        }.also { direction(direction).place(it) }
        providerPlacer to chestPlacer
    }

fun BlockPosSelector.setupRequestingChest(
    direction: EnumFacing,
): Pair<PipePlacer<PipeItemsRequestLogistics>, BlockPlacer<BlockChest>> =
    resetOffsetAfter {
        val requesterPlacer = PipePlacer(PipeItemsRequestLogistics(LPItems.pipeRequest))
            .also { direction(direction).place(it) }
        val chestPlacer = BlockPlacer(Blocks.CHEST)
            .also { direction(direction).place(it) }
        requesterPlacer to chestPlacer
    }

fun IInventory.containsAll(stacks: Collection<ItemStack>): Boolean {
    val stacksLeft = stacks.associateByTo(
        destination = HashMap(),
        keySelector = { ItemIdentifier.get(it) },
        valueTransform = { it.count }
    )
    (0 until sizeInventory).map(this::getStackInSlot).filterNot(ItemStack::isEmpty).forEach { stack ->
        stacksLeft.computeIfPresent(ItemIdentifier.get(stack)) { _, amount ->
            (amount - stack.count).takeIf { it > 0 }
        }
    }
    return stacksLeft.isEmpty()
}

suspend fun waitForOrNull(timeout: Duration, check: () -> Boolean): Unit? =
    CompletableDeferred<Unit>().let { response ->
        withTimeoutOrNull(timeout) {
            fun reschedule() {
                ServerTickDispatcher.scheduleNextTick {
                    try {
                        when {
                            response.isCancelled -> return@scheduleNextTick
                            check() -> response.complete(Unit)
                            else -> reschedule()
                        }
                    } catch (e: Exception) {
                        response.completeExceptionally(e)
                    }
                }
            }

            reschedule()
            response.await()
        }.also {
            if (response.isActive) response.cancelAndJoin()
        }
    }

suspend fun waitFor(timeout: Duration, check: () -> Boolean, lazyErrorMessage: () -> Any) =
    waitForOrNull(timeout, check) ?: error(lazyErrorMessage())
