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

import logisticspipes.LPBlocks
import logisticspipes.LPItems
import logisticspipes.blocks.LogisticsSolidBlock
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity
import logisticspipes.pipes.PipeItemsBasicLogistics
import logisticspipes.pipes.PipeItemsProviderLogistics
import logisticspipes.pipes.PipeItemsRequestLogistics
import logisticspipes.pipes.basic.CoreRoutedPipe
import logisticspipes.pipes.basic.CoreUnroutedPipe
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.block.BlockChest
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import network.rs485.minecraft.BlockBuilder
import network.rs485.minecraft.BlockPosSelector
import java.time.Duration
import kotlin.test.assertTrue

fun <T : CoreUnroutedPipe> BlockPosSelector.placePipe(pipe: T): PipeBuilder<T> {
    worldBuilder.world.setBlockToAir(current)
    assertTrue(message = "Expected $pipe to be placed at $current (${worldBuilder.world})") {
        LogisticsBlockGenericPipe.placePipe(pipe, worldBuilder.world, current, LPBlocks.pipe)
    }
    return PipeBuilder(worldBuilder.world, this, pipe, current)
}

class PipeBuilder<T : CoreUnroutedPipe>(
    world: WorldServer,
    selector: BlockPosSelector,
    val pipe: T,
    pos: BlockPos,
) : BlockBuilder<LogisticsBlockGenericPipe>(world, selector, LPBlocks.pipe, pos) {

    suspend fun waitForPipeInitialization() {
        waitFor(
            timeout = Duration.ofSeconds(1),
            check = { (pipe as? CoreRoutedPipe)?.let { !it.initialInit() } ?: pipe.isInitialized },
            lazyErrorMessage = { "Timed out waiting for pipe init on $pipe at $pos" },
        )
    }

    suspend fun setupLogisticsPower(
        direction: EnumFacing,
        amount: Float,
    ): Pair<PipeBuilder<PipeItemsBasicLogistics>, BlockBuilder<LogisticsSolidBlock>> {
        val basicPipe = direction(direction).placePipe(PipeItemsBasicLogistics(LPItems.pipeBasic))
        val powerJunction = basicPipe.direction(direction).place(LPBlocks.powerJunction)
        powerJunction.getTileEntity<LogisticsPowerJunctionTileEntity>().apply {
            addEnergy(amount)
        }
        basicPipe.waitForPipeInitialization()
        return basicPipe to powerJunction
    }

    suspend fun setupProvidingChest(
        direction: EnumFacing,
        vararg stacks: ItemStack,
    ): Pair<PipeBuilder<PipeItemsProviderLogistics>, BlockBuilder<BlockChest>> {
        val providerPipe = direction(direction).placePipe(PipeItemsProviderLogistics(LPItems.pipeProvider))
        val chest = providerPipe.direction(direction).place(Blocks.CHEST)
        chest.getTileEntity<TileEntityChest>().apply {
            stacks.forEachIndexed { index, itemStack -> setInventorySlotContents(index, itemStack) }
        }
        providerPipe.waitForPipeInitialization()
        return providerPipe to chest
    }

    suspend fun setupRequestingChest(
        direction: EnumFacing,
    ): Pair<PipeBuilder<PipeItemsRequestLogistics>, BlockBuilder<BlockChest>> {
        val requesterPipe = direction(direction).placePipe(PipeItemsRequestLogistics(LPItems.pipeRequest))
        val chest = requesterPipe.direction(direction).place(Blocks.CHEST)
        requesterPipe.waitForPipeInitialization()
        return requesterPipe to chest
    }

    suspend fun updateConnectionsAndWait() {
        (pipe as CoreRoutedPipe).connectionUpdate()
        waitFor(
            timeout = Duration.ofSeconds(1),
            check = {
                CoreRoutedPipe::class.java.getDeclaredField("recheckConnections").run {
                    isAccessible = true
                    !getBoolean(pipe)
                }
            },
            lazyErrorMessage = { "Timed out waiting for connection update on $pipe at $pos" },
        )
    }

}

fun TileEntityChest.containsAll(stacks: Collection<ItemStack>): Boolean {
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
