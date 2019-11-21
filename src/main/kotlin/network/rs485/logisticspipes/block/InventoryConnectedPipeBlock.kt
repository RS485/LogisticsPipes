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
import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.item.FixedItemInv
import alexiil.mc.lib.attributes.item.ItemAttributes
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import network.rs485.logisticspipes.pipe.InventoryConnectedPipe
import network.rs485.logisticspipes.pipe.PipeType
import network.rs485.logisticspipes.pipe.StandardPipeCellPath
import network.rs485.logisticspipes.transport.Pipe
import network.rs485.logisticspipes.transport.network.PipeAttribute
import network.rs485.logisticspipes.transport.network.StandardCellPathHandler
import network.rs485.logisticspipes.util.OptionalDirection

abstract class InventoryConnectedPipeBlock(settings: Settings) : StandardPipeBlock(settings) {

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, neighborPos: BlockPos, moved: Boolean) {
        recalculateInventorySide(state, world, pos)
    }

    fun recalculateInventorySide(state: BlockState, world: World, pos: BlockPos) {
        val side = Direction.values().firstOrNull { side -> ItemAttributes.FIXED_INV.get(world, pos.offset(side), SearchOptions.inDirection(side)).slotCount != 0 }
        var newState = state
        val oldSide = newState[INVENTORY_SIDE].direction
        if (oldSide != null) {
            if (ItemAttributes.FIXED_INV.get(world, pos.offset(oldSide), SearchOptions.inDirection(oldSide)).slotCount != 0) {
                return
            } else {
                newState = newState
                        .with(INVENTORY_SIDE, OptionalDirection.NONE)
                        .with(SIDE_PROPERTIES.getValue(state[INVENTORY_SIDE].direction!!), false)
            }
        }
        newState = newState.with(INVENTORY_SIDE, OptionalDirection.fromDirection(side))

        if (side != null) {
            newState = newState.with(SIDE_PROPERTIES.getValue(side), true)
        }

        if (state != newState) {
            world.setBlockState(pos, newState)
        }
    }

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        super.addAllAttributes(world, pos, state, to)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(INVENTORY_SIDE)
    }

    class WorldInterfaceImpl(world: World, pos: BlockPos) : StandardPipeBlock.WorldInterfaceImpl(world, pos), InventoryConnectedPipe.WorldInterface {

        override fun setConnection(side: Direction, connected: Boolean) {
            if (side == getInventorySide()) return
            super.setConnection(side, connected)
        }

        override fun getAttachedInventory(): FixedItemInv? {
            val side = getInventorySide() ?: return null
            return ItemAttributes.FIXED_INV.get(world, pos.offset(side), SearchOptions.inDirection(side))
        }

        override fun getInventorySide(): Direction? {
            return world.getBlockState(pos)[INVENTORY_SIDE].direction
        }

    }

    companion object {
        val INVENTORY_SIDE = EnumProperty.of("inventory", OptionalDirection::class.java)

        @JvmStatic
        fun <T : Pipe<StandardPipeCellPath, Direction>> create(
                settings: Settings,
                pipeType: PipeType<Direction, T, InventoryConnectedPipe.WorldInterface>
        ) = object : InventoryConnectedPipeBlock(settings) {
            override fun createPipeAttribute(world: World, pos: BlockPos, state: BlockState): PipeAttribute<Pipe<StandardPipeCellPath, Direction>, *, StandardPipeCellPath> {
                return PipeAttribute(pipeType, StandardCellPathHandler, WorldInterfaceImpl(world, pos))
            }
        }
    }

}
