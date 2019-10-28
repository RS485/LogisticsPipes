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
import net.minecraft.entity.EntityContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.IWorld
import net.minecraft.world.World
import network.rs485.logisticspipes.pipe.Pipe
import network.rs485.logisticspipes.pipe.PipeType
import network.rs485.logisticspipes.transport.network.PipeAttribute
import network.rs485.logisticspipes.transport.network.getPipeNetworkState

open class PipeBlock<T : Pipe>(settings: Settings, val pipeType: PipeType<T>) : Block(settings), AttributeProvider {

    init {
        defaultState = SIDE_PROPERTIES.values.fold(defaultState) { acc, prop -> acc.with(prop, false) }
    }

    override fun method_9517(state: BlockState, world: IWorld, pos: BlockPos, flags: Int) {
        super.method_9517(state, world, pos, flags)
        if (world is ServerWorld) {
            world.getPipeNetworkState().onBlockChanged(pos)
        }
    }

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        to.offer(PipeAttribute(pipeType))
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(*SIDE_PROPERTIES.values.toTypedArray())
    }

    override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, ctx: EntityContext): VoxelShape {
        return (Direction.values().mapNotNull { BOX_SIDE[it].takeIf { _ -> state.get(SIDE_PROPERTIES[it]) } } + BOX_CENTER).reduce(VoxelShapes::union)
    }

    companion object {
        val SIDE_PROPERTIES = mapOf(
                Direction.DOWN to Properties.DOWN,
                Direction.UP to Properties.UP,
                Direction.NORTH to Properties.NORTH,
                Direction.SOUTH to Properties.SOUTH,
                Direction.WEST to Properties.WEST,
                Direction.EAST to Properties.EAST
        )

        val BOX_CENTER = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75)
        val BOX_SIDE = mapOf(
                Direction.DOWN to VoxelShapes.cuboid(0.25, 0.0, 0.25, 0.75, 0.25, 0.75),
                Direction.UP to VoxelShapes.cuboid(0.25, 0.75, 0.25, 0.75, 1.0, 0.75),
                Direction.NORTH to VoxelShapes.cuboid(0.25, 0.25, 0.0, 0.75, 0.75, 0.25),
                Direction.SOUTH to VoxelShapes.cuboid(0.25, 0.25, 0.75, 0.75, 0.75, 1.0),
                Direction.WEST to VoxelShapes.cuboid(0.0, 0.25, 0.25, 0.25, 0.75, 0.75),
                Direction.EAST to VoxelShapes.cuboid(0.75, 0.25, 0.25, 1.0, 0.75, 0.75)
        )
    }

}