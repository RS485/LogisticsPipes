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

import logisticspipes.LogisticsPipes
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.WorldServer
import net.minecraftforge.common.ForgeChunkManager
import network.rs485.minecraft.BlockPosSelector
import network.rs485.minecraft.WorldBuilder
import network.rs485.minecraft.minus
import kotlin.math.min

const val LEVEL = 100
val ONE_VECTOR = Vec3i(1, 1, 1)

class TestWorldBuilder(override val world: WorldServer) : WorldBuilder {

    private val selectors = ArrayList<Pair<BlockPosSelector, BlockPos>>()

    // TODO: grouping and expanding groups in z needs collecting all selectors/builds before configuration
    private var nextPos = BlockPos(0, LEVEL, 0)
    private var lowest = LEVEL

    private val tickets: HashSet<ForgeChunkManager.Ticket> = HashSet()
    private val chunksToLoad: MutableSet<ChunkPos> = HashSet()

    fun newSelector() = BlockPosSelector(worldBuilder = this)

    init {
        ForgeChunkManager.setForcedChunkLoadingCallback(LogisticsPipes.instance) { ticketsIn, world ->
            if (world == this@TestWorldBuilder.world) tickets.addAll(ticketsIn!!)
        }
        tickets.add(ForgeChunkManager.requestTicket(LogisticsPipes.instance, world, ForgeChunkManager.Type.NORMAL)!!)
    }

    override fun finalPosition(selector: BlockPosSelector): BlockPos = (nextPos - selector.localStart).also {
        selectors.add(selector to it)
        val start = it.add(selector.localStart)
        val borderStart = start.subtract(ONE_VECTOR)
        val end = it.add(selector.localEnd)
        val borderEnd = end.add(ONE_VECTOR)
        lowest = min(lowest, borderStart.y)
        nextPos = BlockPos(borderEnd.x + 2, LEVEL, 0)
        world.setBlocksToAir(start = borderStart, end = borderEnd)
        world.setBlocks(
            start = BlockPos(borderStart.x, lowest, borderStart.z),
            end = BlockPos(borderStart.x, lowest, borderEnd.z),
            state = Blocks.DOUBLE_STONE_SLAB.defaultState,
        )
        world.setBlocks(
            start = BlockPos(borderStart.x, lowest, borderEnd.z),
            end = BlockPos(borderEnd.x, lowest, borderEnd.z),
            state = Blocks.DOUBLE_STONE_SLAB.defaultState,
        )
        world.setBlocks(
            start = BlockPos(borderEnd.x, lowest, borderStart.z),
            end = BlockPos(borderEnd.x, lowest, borderEnd.z),
            state = Blocks.DOUBLE_STONE_SLAB.defaultState,
        )
        world.setBlocks(
            start = BlockPos(borderStart.x, lowest, borderStart.z),
            end = BlockPos(borderEnd.x, lowest, borderStart.z),
            state = Blocks.DOUBLE_STONE_SLAB.defaultState,
        )
        world.setBlocks(
            start = BlockPos(start.x, lowest, start.z),
            end = BlockPos(end.x, lowest, end.z),
            state = Blocks.STONE.defaultState,
        )
    }

    override fun loadChunk(pos: ChunkPos) {
        if (chunksToLoad.add(pos)) ForgeChunkManager.forceChunk(tickets.first(), pos)
    }

}

private fun WorldServer.setBlocks(start: BlockPos, end: BlockPos, state: IBlockState) =
    blocksIn(start, end).forEach { setBlockState(it, state) }

private fun WorldServer.setBlocksToAir(start: BlockPos, end: BlockPos) =
    blocksIn(start, end).forEach(::setBlockToAir)

private fun blocksIn(start: BlockPos, end: BlockPos): List<BlockPos> {
    assert(start.x <= end.x)
    assert(start.y <= end.y)
    assert(start.z <= end.z)
    return (start.x..end.x).flatMap { x ->
        (start.y..end.y).flatMap { y ->
            (start.z..end.z).map { z -> BlockPos(x, y, z) }
        }
    }
}
