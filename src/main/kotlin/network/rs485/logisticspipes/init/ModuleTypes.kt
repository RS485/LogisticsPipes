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

package network.rs485.logisticspipes.init

import logisticspipes.modules.*
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.module.ModuleType

object ModuleTypes {

    // Note this isn't all registered modules, but all modules added by LogisticsPipes.
    // If you want all registered modules, get them from Registries.ModuleType
    var All: Set<ModuleType<*>> = emptySet()
        private set

    val ItemSink = create("item_sink", ModuleType.Builder(::ModuleItemSink).build())
    val GroupItemSink = create("group_item_sink", ModuleType.Builder(::ModuleCreativeTabBasedItemSink).build())
    val ModBasedItemSink = create("mod_item_sink", ModuleType.Builder(::ModuleModBasedItemSink).build())
    val PolymorphicItemSink = create("polymorphic_item_sink", ModuleType.Builder(::ModulePolymorphicItemSink).build())
    // TODO implement tag based item sink
    val TagItemSink = create("tag_item_sink", ModuleType.Builder(::ModuleItemSink).build())
    val EnchantmentSink = create("enchantment_sink", ModuleType.Builder(::ModuleEnchantmentSink).build())
    val EnchantmentSinkMk2 = create("enchantment_sink_mk2", ModuleType.Builder(::ModuleEnchantmentSinkMK2).build())
    val Extractor = create("extractor", ModuleType.Builder(::ModuleExtractor).build())
    val AdvancedExtractor = create("advanced_extractor", ModuleType.Builder(::ModuleAdvancedExtractor).build())
    val QuickSort = create("quick_sort", ModuleType.Builder(::ModuleQuickSort).build())
    val PassiveSupplier = create("passive_supplier", ModuleType.Builder(::ModulePassiveSupplier).build())
    val ActiveSupplier = create("active_supplier", ModuleType.Builder(::ModuleActiveSupplier).build())
    val Crafting = create("crafting", ModuleType.Builder(::ModuleCrafter).build())
    val Provider = create("provider", ModuleType.Builder(::ModuleProvider).build())
    val Terminus = create("terminus", ModuleType.Builder(::ModuleTerminus).build())

    // these don't have an item, and are only used in pipes.
    // TODO don't use modules as pipe logic, especially when they don't have a module form
    val FluidSupplier = create("fluid_supplier", ModuleType.Builder { error("Can't construct") }.build())
    val Satellite = create("satellite", ModuleType.Builder { error("Can't construct") }.build())

    private fun <T : ModuleType<*>> create(name: String, type: T): T {
        return Registry.register(Registries.ModuleType, Identifier(ModID, name), type).also { All += it }
    }

}