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

package network.rs485.logisticspipes.connection

import logisticspipes.pipes.basic.CoreRoutedPipe
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import java.util.*

class SingleAdjacent(private val parent: CoreRoutedPipe, val dir: EnumFacing, private val adjacentType: ConnectionType) : Adjacent {
    override fun connectedPos(): Map<BlockPos, ConnectionType> = mapOf(parent.pos.offset(dir) to adjacentType)

    override fun get(direction: EnumFacing): ConnectionType? = adjacentType.takeIf { dir == direction }

    override fun optionalGet(direction: EnumFacing): Optional<ConnectionType> {
        return if (dir == direction) {
            Optional.of(adjacentType)
        } else {
            Optional.empty()
        }
    }

    override fun neighbors(): Map<NeighborTileEntity<TileEntity>, ConnectionType> =
        parent.world.getTileEntity(parent.pos.offset(dir))
            ?.let { mapOf(LPNeighborTileEntity(it, dir) to adjacentType) }
            ?: emptyMap()

    override fun inventories(): List<NeighborTileEntity<TileEntity>> =
        if (adjacentType.isItem()) {
            listOfNotNull(parent.world.getTileEntity(parent.pos.offset(dir))?.let { tile ->
                LPNeighborTileEntity(tile, dir).takeIf { it.canHandleItems() }
            })
        } else emptyList()

    override fun fluidTanks(): List<NeighborTileEntity<TileEntity>> =
        if (adjacentType.isFluid()) {
            listOfNotNull(parent.world.getTileEntity(parent.pos.offset(dir))?.let { tile ->
                LPNeighborTileEntity(tile, dir).takeIf { it.canHandleFluids() }
            })
        } else emptyList()

    override fun toString(): String = "SingleAdjacent(${dir.name2}: $adjacentType)"
}
