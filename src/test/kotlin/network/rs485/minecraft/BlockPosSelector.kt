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

package network.rs485.minecraft

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.minecraft.block.BlockColored
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.WorldServer
import network.rs485.logisticspipes.integration.ONE_VECTOR
import kotlin.math.max
import kotlin.math.min

class BlockPosSelector(val worldBuilder: WorldBuilder) {
    private val placersToOffsets = ArrayList<Pair<Placer, Vec3i>>()
    private val extraConfigurators = ArrayList<Configurator>()
    private var finalized: BlockPos? = null
    var localStart: Vec3i = BlockPos.NULL_VECTOR
        private set
    var localEnd: Vec3i = BlockPos.NULL_VECTOR
        private set
    var localOffset: Vec3i = BlockPos.NULL_VECTOR

    fun <T> resetOffsetAfter(block: BlockPosSelector.() -> T) = localOffset.let { startOffset ->
        block.invoke(this).also { localOffset = startOffset }
    }

    fun direction(direction: EnumFacing): BlockPosSelector = this.also {
        localOffset += direction.directionVec
    }

    fun place(placer: Placer): BlockPosSelector = this.also {
        localStart = BlockPos(
            min(localStart.x, localOffset.x),
            min(localStart.y, localOffset.y),
            min(localStart.z, localOffset.z)
        ).takeIf { finalized == null || it == localStart }
            ?: error("Range $localStart to $localEnd has been finalized!")
        localEnd = BlockPos(
            max(localEnd.x, localOffset.x),
            max(localEnd.y, localOffset.y),
            max(localEnd.z, localOffset.z)
        ).takeIf { finalized == null || it == localEnd }
            ?: error("Range $localStart to $localEnd has been finalized!")
        placersToOffsets += placer to localOffset
    }

    fun configure(configurator: Configurator): BlockPosSelector = this.also {
        extraConfigurators.add(configurator)
    }

    suspend fun finalize() {
        coroutineScope {
            val translated = worldBuilder.finalPosition(this@BlockPosSelector)
            val configurators = placersToOffsets.map {
                (translated + it.second).let { pos ->
                    worldBuilder.loadChunk(worldBuilder.world.getChunkFromBlockCoords(pos).pos)
                    async {
                        it.first.place(worldBuilder.world, pos)
                    }
                }
            }.awaitAll()
            finalized = translated
            setVisibleState(TestState.RUNNING)
            (configurators + extraConfigurators).map {
                async {
                    it.configure()
                }
            }.awaitAll()
        }
    }

    fun setVisibleState(state: TestState) = finalized
        ?.let { it + localStart - ONE_VECTOR }
        ?.to(state.let {
            when (it) {
                TestState.RUNNING -> Blocks.GLOWSTONE.defaultState
                TestState.FAIL -> Blocks.CONCRETE.defaultState.withProperty(BlockColored.COLOR, EnumDyeColor.RED)
                TestState.SUCCESS -> Blocks.CONCRETE.defaultState.withProperty(BlockColored.COLOR, EnumDyeColor.GREEN)
            }
        })
        ?.also {
            worldBuilder.world.setBlockState(it.first, it.second)
        }
}

enum class TestState {
    RUNNING,
    FAIL,
    SUCCESS;
}

interface Placer {
    suspend fun place(world: WorldServer, pos: BlockPos): Configurator
}

interface Configurator {
    suspend fun configure()
}

suspend fun configurator(name: String? = null, block: suspend () -> Unit) = object : Configurator {
    override suspend fun configure() = block.invoke()

    override fun toString(): String {
        return "Configurator" + (name?.let { "($it)" } ?: "")
    }
}

internal operator fun Vec3i.plus(other: Vec3i): Vec3i = Vec3i(x + other.x, y + other.y, z + other.z)

internal operator fun BlockPos.plus(other: Vec3i): BlockPos = add(other)
internal operator fun BlockPos.minus(other: Vec3i): BlockPos = subtract(other)
