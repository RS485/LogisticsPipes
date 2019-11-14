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

package network.rs485.logisticspipes.block

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IWorld
import net.minecraft.world.World
import network.rs485.logisticspipes.pipe.BiPort
import network.rs485.logisticspipes.pipe.HighSpeedPath
import network.rs485.logisticspipes.pipe.HighSpeedPipe
import network.rs485.logisticspipes.pipe.PipeType
import network.rs485.logisticspipes.transport.CellContent
import network.rs485.logisticspipes.transport.Pipe
import network.rs485.logisticspipes.transport.network.CurvePipeCellPathHandler
import network.rs485.logisticspipes.transport.network.PipeAttribute
import network.rs485.logisticspipes.transport.network.getPipeNetworkState

class CurvePipeBlock<T : Pipe<HighSpeedPath, BiPort>>(settings: Block.Settings, val pipeType: PipeType<BiPort, T, HighSpeedPipe.WorldInterface>) : Block(settings), AttributeProvider {

    override fun method_9517(state: BlockState, world: IWorld, pos: BlockPos, flags: Int) {
        super.method_9517(state, world, pos, flags)
        if (world is ServerWorld) {
            world.getPipeNetworkState().onBlockChanged(pos)
        }
    }

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        to.offer(PipeAttribute(pipeType, CurvePipeCellPathHandler(state[DIRECTION]), WorldInterfaceImpl(world, pos)))
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(DIRECTION, ctx.playerFacing)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(DIRECTION)
    }

    inner class WorldInterfaceImpl(val world: World, val pos: BlockPos) : HighSpeedPipe.WorldInterface {

        override fun dropItem(content: CellContent, port: BiPort?) {
            if (port == null) {
                dropItem(content, pos, null)
            } else {
                val shape = pipeType.getBaseShape(world.getBlockState(pos)).translate(pos)
                val (pos, side) = shape.ports.getValue(port)
                dropItem(content, pos, side)
            }
        }

        private fun dropItem(content: CellContent, pos: BlockPos, side: Direction?) {
            if (side != null) {
                val dir = Vec3d(side.vector)
                val vec = Vec3d(pos).add(0.5, 0.5, 0.5).add(dir.multiply(0.75))
                val entity = content.createEntity(world, vec, dir.multiply(0.2)) ?: return
                world.spawnEntity(entity)
            } else {
                val entity = content.createEntity(world, Vec3d(pos).add(0.5, 0.5, 0.5), null)
                world.spawnEntity(entity)
            }
        }

    }

    companion object {
        val DIRECTION = Properties.HORIZONTAL_FACING
    }

}