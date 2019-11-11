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

package network.rs485.logisticspipes.item

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import network.rs485.logisticspipes.ModID

class RemoteOrdererItem(settings: Settings) : ItemWithInfo(settings) {

    // override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
    //     val stack = player.getStackInHand(hand)
    //     if (world is ServerWorld) {
    //         val pipe = getLink(stack, world.server) ?: return TypedActionResult(ActionResult.FAIL, stack)
    //         val energyUse =
    //                 if (pipe.world.dimension != world.dimension) 2500.0
    //                 else player.pos.distanceTo(Vec3d(pipe.pos).add(0.5, 0.5, 0.5))
    //         if (pipe.useEnergy(energyUse.roundToInt())) {
    //             // TODO player.openContainer()
    //             return TypedActionResult(ActionResult.SUCCESS, stack)
    //         }
    //     } else {
    //         // just assume opening the remote pipe succeeded if this item is linked
    //         return if (hasLinkPos(stack)) TypedActionResult(ActionResult.SUCCESS, stack)
    //         else TypedActionResult(ActionResult.FAIL, stack)
    //     }
    //     return super.use(world, player, hand)
    // }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, ctx: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, ctx)

        if (hasLinkPos(stack)) {
            tooltip.add(TranslatableText("tooltip.$ModID.remote_orderer.has_link").formatted(Formatting.GRAY))
        }
    }

    companion object {
        // fun getLink(stack: ItemStack, server: MinecraftServer): PipeItemsRemoteOrdererLogistics? {
        //     val (pos, dim) = getLinkPos(stack) ?: return null
        //     val world = server.getWorld(dim) ?: return null
        //     val entity = world.getBlockEntity(pos) as? LogisticsTileGenericPipe ?: return null
        //     return entity.pipe as? PipeItemsRemoteOrdererLogistics
        // }

        fun getLinkPos(stack: ItemStack): Pair<BlockPos, DimensionType>? {
            if (stack.item !is RemoteOrdererItem) error("Can't get link position from $stack")
            if (!hasLinkPos(stack)) return null
            val tag = stack.tag ?: return null
            val x = tag.getInt("link_x")
            val y = tag.getInt("link_y")
            val z = tag.getInt("link_z")
            val dimId = tag.getString("link_dim")
            val pos = BlockPos(x, y, z)
            val dim = Registry.DIMENSION_TYPE[Identifier(dimId)] ?: return null
            return Pair(pos, dim)
        }

        fun hasLinkPos(stack: ItemStack): Boolean {
            if (stack.item !is RemoteOrdererItem) error("Can't get link position from $stack")
            val tag = stack.tag ?: return false
            return setOf("link_x", "link_y", "link_z", "link_dim").all(tag::contains)
        }

        fun setLinkPos(stack: ItemStack, pos: BlockPos, dim: DimensionType) {
            if (stack.item !is RemoteOrdererItem) error("Can't set link position on $stack")
            val dimId = Registry.DIMENSION_TYPE.getId(dim) ?: error("Tried to set link position to unregistered dimension?!")
            val tag = stack.orCreateTag
            tag.putInt("link_x", pos.x)
            tag.putInt("link_y", pos.y)
            tag.putInt("link_z", pos.z)
            tag.putString("link_dim", dimId.toString())
        }
    }

}