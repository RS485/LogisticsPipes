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

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateFactory
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class LogisticsSolidBlockWithEntity(settings: Block.Settings) : BlockWithEntity(settings) {

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, neighbor: Block, neighborPos: BlockPos, flag: Boolean) {
        super.neighborUpdate(state, world, pos, neighbor, neighborPos, flag)
        // val entity = world.getBlockEntity(pos) as? LogisticsSolidTileEntity
        // entity?.notifyOfBlockChange()
    }

    override fun activate(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hitResult: BlockHitResult): Boolean {
        if (player.isSneaking) return false

        val entity = world.getBlockEntity(pos)
        // if (entity is IGuiTileEntity) {
        //     if (!world.isClient) entity.guiProvider.setTilePos(entity).open(player)
        //     return true
        // }
        return false
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return super.getPlacementState(ctx)!!
                .with(FACING, ctx.playerLookDirection.opposite)
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        val entity = world.getBlockEntity(pos)
        // if (entity is LogisticsSolidTileEntity) {
        //     entity.onBlockBreak()
        // }
        super.onBreak(world, pos, state, player)
    }

    override fun appendProperties(builder: StateFactory.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(FACING)
        builder.add(ACTIVE)
        builder.add(*CONNECTIONS.values.toTypedArray())
    }

    companion object {
        @JvmField
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
        @JvmField
        val ACTIVE: BooleanProperty = BooleanProperty.of("active")
        @JvmField
        val CONNECTIONS: Map<Direction, BooleanProperty> = LogisticsSolidBlock.CONNECTIONS
    }

}