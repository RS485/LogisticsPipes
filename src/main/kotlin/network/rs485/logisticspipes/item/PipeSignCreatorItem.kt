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

import logisticspipes.pipes.basic.CoreRoutedPipe
import logisticspipes.pipes.basic.LogisticsTileGenericPipe
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import network.rs485.logisticspipes.pipe.sign.PipeSignType
import network.rs485.logisticspipes.init.Registries

class PipeSignCreatorItem(settings: Settings) : ItemWithInfo(settings) {

    override fun useOnBlock(ctx: ItemUsageContext): ActionResult {
        val player = ctx.player
        val stack = ctx.stack
        val side = ctx.side
        val entity = ctx.world.getBlockEntity(ctx.blockPos)
        if (entity !is LogisticsTileGenericPipe) return ActionResult.PASS
        val pipe = entity.pipe
        if (pipe !is CoreRoutedPipe) return ActionResult.FAIL

        val result = if (player != null) {
            if (!ctx.shouldCancelInteraction()) {
                when {
                    pipe.activatePipeSign(side, player) -> true
                    pipe.addPipeSign(side, getSignType(stack).create(), player) -> {
                        stack.damage++
                        true
                    }
                    else -> false
                }
            } else {
                if (pipe.removePipeSign(side, player)) {
                    stack.damage--
                    true
                } else false
            }
        } else false

        return if (result) ActionResult.SUCCESS else ActionResult.FAIL
    }

    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = player.getStackInHand(hand)
        if (player.isSneaking) {
            cycleSignType(stack)
            return TypedActionResult(ActionResult.SUCCESS, stack)
        }
        return super.use(world, player, hand)
    }

    override fun getName(stack: ItemStack): Text {
        // TODO this is probably going to be verbose af
        return super.getName(stack).append(LiteralText(" [")).append(getSignType(stack).getName()).append(LiteralText("]"))
    }

    companion object {

        fun cycleSignType(stack: ItemStack) {
            if (stack.item !is PipeSignCreatorItem) error("Can't cycle sign type on $stack")
            val st = getSignType(stack)
            val next = (Registries.SignType.getRawId(st) + 1) % Registries.SignType.ids.size
            setSignType(stack, Registries.SignType[next])
        }

        fun getSignType(stack: ItemStack): PipeSignType<*> {
            if (stack.item !is PipeSignCreatorItem) error("Can't get sign type from $stack")
            val type = stack.tag?.getString("sign_type") ?: return Registries.SignType[Registries.SignType.defaultId]
            return Registries.SignType[Identifier(type)]
        }

        fun setSignType(stack: ItemStack, type: PipeSignType<*>) {
            if (stack.item !is PipeSignCreatorItem) error("Can't set sign type on $stack")
            val str = Registries.SignType.getId(type).toString()
            stack.orCreateTag.putString("sign_type", str)
        }
    }
}