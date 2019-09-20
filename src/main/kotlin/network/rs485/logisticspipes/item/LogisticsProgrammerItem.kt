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
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.api.ItemAdvancedRecipeRemainder
import network.rs485.logisticspipes.ext.makeStack

class LogisticsProgrammerItem(settings: Settings) : ItemWithInfo(settings), ItemAdvancedRecipeRemainder {

    override fun getRecipeRemainder(stack: ItemStack): ItemStack {
        if (stack.isEmpty) return this.makeStack()
        return stack.copy()
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, ctx: TooltipContext) {
        val target = getRecipeTarget(stack)

        tooltip += when (target) {
            is ModuleItem<*> -> listOf(
                    TranslatableText("tooltip.$ModID.programmer.for_module"),
                    stack.item.getName(stack)
            )
            is UpgradeItem<*> -> listOf(
                    TranslatableText("tooltip.$ModID.programmer.for_upgrade"),
                    stack.item.getName(stack)
            )
            is PipeItem<*> -> listOf(
                    TranslatableText("tooltip.$ModID.programmer.for_pipe"),
                    stack.item.getName(stack)
            )
            else -> listOf(
                    TranslatableText("tooltip.$ModID.programmer.for_unknown.1"),
                    TranslatableText("tooltip.$ModID.programmer.for_unknown.2"),
                    TranslatableText("tooltip.$ModID.programmer.for_unknown.3")
            )
        }

        super.appendTooltip(stack, world, tooltip, ctx)
    }

    companion object {
        fun getRecipeTarget(stack: ItemStack): Item? {
            if (stack.item !is LogisticsProgrammerItem) error("Can't get recipe target for $stack")
            val target = stack.tag?.getString("target") ?: return null
            return Registry.ITEM.get(Identifier(target))
        }
    }

}