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

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.world.World
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.init.Registries
import network.rs485.logisticspipes.pipe.upgrade.Upgrade
import network.rs485.logisticspipes.pipe.upgrade.UpgradeSlots
import network.rs485.logisticspipes.pipe.upgrade.UpgradeType

open class UpgradeItem<T : Upgrade>(settings: Settings, val type: UpgradeType<T>) : ItemWithInfo(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, ctx: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, ctx)
        if (Screen.hasShiftDown()) {
            // I'm just going to bet on the fact that there's not enough pipes/modules registered for this to lag.
            // This info could be cached tbh, but not going to bother with it right now.

            val allPipes = Registries.PipeType.toList()
            val supportedPipes = UpgradeSlots.getUpgradePipeTargets(type)
            val allModules = Registries.ModuleType.toList()
            val supportedModules = UpgradeSlots.getUpgradeModuleTargets(type)

            when {
                supportedPipes.isEmpty() -> tooltip.add(TranslatableText("tooltip.$ModID.upgrade.no_pipes"))
                supportedPipes == allPipes -> tooltip.add(TranslatableText("tooltip.$ModID.upgrade.all_pipes"))
                supportedPipes.size < allPipes.size / 2 -> {
                    tooltip.add(TranslatableText("tooltip.$ModID.upgrade.allowed_pipes"))
                    tooltip += supportedPipes.map { LiteralText(" - ").append(it.getName()) }
                }
                else -> {
                    val disallowed = allPipes - supportedPipes
                    tooltip.add(TranslatableText("tooltip.$ModID.upgrade.disallowed_pipes"))
                    tooltip += disallowed.map { LiteralText(" - ").append(it.getName()) }
                }
            }

            when {
                supportedModules.isEmpty() -> tooltip.add(TranslatableText("tooltip.$ModID.upgrade.no_modules"))
                supportedModules == allModules -> tooltip.add(TranslatableText("tooltip.$ModID.upgrade.all_modules"))
                supportedModules.size < allModules.size / 2 -> {
                    tooltip.add(TranslatableText("tooltip.$ModID.upgrade.allowed_modules"))
                    tooltip += supportedModules.map { LiteralText(" - ").append(it.getName()) }
                }
                else -> {
                    val disallowed = allModules - supportedModules
                    tooltip.add(TranslatableText("tooltip.$ModID.upgrade.disallowed_modules"))
                    tooltip += disallowed.map { LiteralText(" - ").append(it.getName()) }
                }
            }
        }
    }

}