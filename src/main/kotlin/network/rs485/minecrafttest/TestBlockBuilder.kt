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

package network.rs485.minecrafttest

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.time.withTimeoutOrNull
import logisticspipes.LPBlocks
import logisticspipes.LPItems
import logisticspipes.LogisticsPipes
import logisticspipes.blocks.LogisticsSolidBlock
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity
import logisticspipes.pipes.PipeItemsBasicLogistics
import logisticspipes.pipes.PipeItemsProviderLogistics
import logisticspipes.pipes.PipeItemsRequestLogistics
import logisticspipes.pipes.basic.CoreRoutedPipe
import logisticspipes.pipes.basic.CoreUnroutedPipe
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.block.Block
import net.minecraft.block.BlockChest
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import net.minecraftforge.common.ForgeChunkManager
import network.rs485.grow.ServerTickDispatcher
import java.time.Duration

class TestBlockBuilder(private val world: WorldServer) {

    val tickets: MutableSet<ForgeChunkManager.Ticket> = HashSet()

    fun newTest() = TestBuilder(0, 100, 0)

    init {
        ForgeChunkManager.setForcedChunkLoadingCallback(LogisticsPipes.instance) { ticketsIn, world ->
            if (world == this@TestBlockBuilder.world) tickets.addAll(ticketsIn!!)
        }
        tickets.add(ForgeChunkManager.requestTicket(LogisticsPipes.instance, world, ForgeChunkManager.Type.NORMAL)!!)
    }

    inner class TestBuilder(xBlock: Int, yBlock: Int, zBlock: Int) {
        private var start = BlockPos(xBlock, yBlock, zBlock) // TODO: add boundary checking for x tests
        private var end = BlockPos(xBlock, yBlock, zBlock)
        var current = BlockPos(xBlock, yBlock, zBlock)

        fun <T : Block> place(block: T): BlockBuilder<T> {
            world.setBlockToAir(current)
            world.setBlockState(current, block.defaultState)
            return BlockBuilder(block, current)
        }

        fun <T : CoreUnroutedPipe> placePipe(pipe: T): PipeBuilder<T> {
            world.setBlockToAir(current)
            assert(LogisticsBlockGenericPipe.placePipe(pipe, world, current, LPBlocks.pipe))
            return PipeBuilder(pipe, current)
        }

        inner class PipeBuilder<T : CoreUnroutedPipe>(val pipe: T, pos: BlockPos) :
            IBlockBuilder by BlockBuilder<LogisticsBlockGenericPipe>(LPBlocks.pipe, pos) {

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

        inner class BlockBuilder<T : Block>(val block: T, override val pos: BlockPos) : IBlockBuilder {
            init {
                ForgeChunkManager.forceChunk(tickets.first(), world.getChunkFromBlockCoords(pos).pos)
            }

            override fun direction(direction: EnumFacing): TestBuilder {
                current = pos.offset(direction)
                return this@TestBuilder
            }

            @Suppress("UNCHECKED_CAST")
            override fun <Y : TileEntity> getTileEntity(): Y = (world.getTileEntity(pos) as Y)
        }
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

interface IBlockBuilder {
    val pos: BlockPos
    fun direction(direction: EnumFacing): TestBlockBuilder.TestBuilder
    fun <Y : TileEntity> getTileEntity(): Y
}

suspend fun waitForOrNull(timeout: Duration, check: () -> Boolean): Unit? =
    withTimeoutOrNull(timeout) {
        val response = CompletableDeferred<Unit>()

        fun schedule() {
            ServerTickDispatcher.scheduleNextTick {
                try {
                    if (check()) {
                        response.complete(Unit)
                    } else {
                        schedule()
                    }
                } catch (e: Exception) {
                    response.completeExceptionally(e)
                }
            }
        }

        schedule()
        response.await()
    }

suspend fun waitFor(timeout: Duration, check: () -> Boolean, lazyErrorMessage: () -> Any) =
    waitForOrNull(timeout, check) ?: error(lazyErrorMessage())
