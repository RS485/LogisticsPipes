/*
 * Copyright (c) 2019-2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019-2021  RS485
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

package network.rs485.logisticspipes.world

import logisticspipes.LogisticsPipes
import logisticspipes.proxy.MainProxy
import logisticspipes.proxy.SimpleServiceLocator
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import network.rs485.logisticspipes.connection.LPNeighborTileEntity

data class WorldCoordinatesWrapper(private val world: World, private val pos: BlockPos) {
    constructor(tileEntity: TileEntity) : this(tileEntity.world, tileEntity.pos)

    val tileEntity: TileEntity?
        get() = world.getTileEntity(pos)

    fun allNeighborTileEntities(): List<LPNeighborTileEntity<TileEntity>> =
        EnumFacing.VALUES.mapNotNull { direction: EnumFacing -> getNeighbor(direction) }

    fun connectedTileEntities(): List<LPNeighborTileEntity<TileEntity>> {
        val pipe = tileEntity
        if (SimpleServiceLocator.pipeInformationManager.isNotAPipe(pipe)) {
            LogisticsPipes.log.warn("The coordinates didn't hold a pipe at all", Throwable("Stack trace"))
            return emptyList()
        }
        return allNeighborTileEntities().filter { adjacent -> MainProxy.checkPipesConnections(pipe, adjacent.tileEntity, adjacent.direction) }
    }

    fun getNeighbor(direction: EnumFacing): LPNeighborTileEntity<TileEntity>? {
        val tileEntity = world.getTileEntity(pos.offset(direction)) ?: return null
        return LPNeighborTileEntity(tileEntity, direction)
    }

    override fun hashCode(): Int {
        var result = world.hashCode()
        result = 31 * result + pos.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is WorldCoordinatesWrapper) return false
        return world == other.world && pos == other.pos
    }

    override fun toString(): String {
        return "WorldCoordinatesWrapper(world=$world, pos=$pos)"
    }

}
