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

import network.rs485.logisticspipes.pipe.upgrade.UpgradeSlots

internal fun initUpgradeSlots() {
    UpgradeSlots.addForModule(UpgradeTypes.ActionSpeed, ModuleTypes.Extractor)
    UpgradeSlots.addForModule(UpgradeTypes.ActionSpeed, ModuleTypes.AdvancedExtractor)

    UpgradeSlots.addForPipe(UpgradeTypes.AdvancedSatellite, PipeTypes.Crafting)
    UpgradeSlots.addForModule(UpgradeTypes.AdvancedSatellite, ModuleTypes.Crafting)

    UpgradeSlots.addForPipes(UpgradeTypes.Disconnection, PipeTypes.All)

    UpgradeSlots.addForPipe(UpgradeTypes.CraftingByproduct, PipeTypes.Crafting)
    UpgradeSlots.addForModule(UpgradeTypes.CraftingByproduct, ModuleTypes.Crafting)

    UpgradeSlots.addForPipe(UpgradeTypes.CraftingCleanup, PipeTypes.Crafting)
    UpgradeSlots.addForModule(UpgradeTypes.CraftingCleanup, ModuleTypes.Crafting)

    // TODO: Request Table isn't implemented yet. Implementation TBD since having it be a glorified pipe is a hack and means the mod is not extensible enough ;)
    // UpgradeSlots.addForPipe(UpgradeTypes.CraftingMonitoring, PipeTypes.RequestTable)

    UpgradeSlots.addForPipe(UpgradeTypes.FluidCrafting, PipeTypes.Crafting)
    UpgradeSlots.addForModule(UpgradeTypes.FluidCrafting, ModuleTypes.Crafting)

    UpgradeSlots.addForPipe(UpgradeTypes.Fuzzy, PipeTypes.Crafting)
    UpgradeSlots.addForPipe(UpgradeTypes.Fuzzy, PipeTypes.Basic)
    UpgradeSlots.addForModule(UpgradeTypes.Fuzzy, ModuleTypes.Crafting)
    UpgradeSlots.addForModule(UpgradeTypes.Fuzzy, ModuleTypes.ItemSink)

    UpgradeSlots.addForPipe(UpgradeTypes.ItemExtraction, PipeTypes.Crafting)
    UpgradeSlots.addForPipe(UpgradeTypes.ItemExtraction, PipeTypes.Provider)
    UpgradeSlots.addForModule(UpgradeTypes.ItemExtraction, ModuleTypes.Crafting)
    UpgradeSlots.addForModule(UpgradeTypes.ItemExtraction, ModuleTypes.Provider)
    UpgradeSlots.addForModule(UpgradeTypes.ItemExtraction, ModuleTypes.Extractor)
    UpgradeSlots.addForModule(UpgradeTypes.ItemExtraction, ModuleTypes.AdvancedExtractor)

    UpgradeSlots.addForPipe(UpgradeTypes.StackExtraction, PipeTypes.Crafting)
    UpgradeSlots.addForPipe(UpgradeTypes.StackExtraction, PipeTypes.Provider)
    UpgradeSlots.addForModule(UpgradeTypes.StackExtraction, ModuleTypes.Crafting)
    UpgradeSlots.addForModule(UpgradeTypes.StackExtraction, ModuleTypes.Provider)

    UpgradeSlots.addForPipes(UpgradeTypes.LogicController, PipeTypes.All)

    UpgradeSlots.addForPipes(UpgradeTypes.Opaque, PipeTypes.All)

    UpgradeSlots.addForPipe(UpgradeTypes.Pattern, PipeTypes.Supplier)
    UpgradeSlots.addForModule(UpgradeTypes.Pattern, ModuleTypes.ActiveSupplier)

    UpgradeSlots.addForPipes(UpgradeTypes.PowerTransportation, PipeTypes.All)

    UpgradeSlots.addForPipes(UpgradeTypes.SneakyCombination, PipeTypes.All)

    UpgradeSlots.addForPipes(UpgradeTypes.Sneaky, PipeTypes.All)
    UpgradeSlots.addForModule(UpgradeTypes.Sneaky, ModuleTypes.ItemSink)
    UpgradeSlots.addForModule(UpgradeTypes.Sneaky, ModuleTypes.PolymorphicItemSink)
    UpgradeSlots.addForModule(UpgradeTypes.Sneaky, ModuleTypes.ModBasedItemSink)
    UpgradeSlots.addForModule(UpgradeTypes.Sneaky, ModuleTypes.TagItemSink)
    UpgradeSlots.addForModule(UpgradeTypes.Sneaky, ModuleTypes.GroupItemSink)

    UpgradeSlots.addForPipes(UpgradeTypes.Speed, PipeTypes.All)

    UpgradeSlots.addForPipe(UpgradeTypes.Module, PipeTypes.ChassisMk1)
    UpgradeSlots.addForPipe(UpgradeTypes.Module, PipeTypes.ChassisMk2)
    UpgradeSlots.addForPipe(UpgradeTypes.Module, PipeTypes.ChassisMk3)
    UpgradeSlots.addForPipe(UpgradeTypes.Module, PipeTypes.ChassisMk4)
    UpgradeSlots.addForPipe(UpgradeTypes.Module, PipeTypes.ChassisMk5)
}