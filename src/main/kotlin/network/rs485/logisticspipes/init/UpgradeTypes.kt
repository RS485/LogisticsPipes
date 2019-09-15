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

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.pipe.upgrade.*

object UpgradeTypes {

    val ActionSpeed = create("action_speed", UpgradeType.Builder(::ActionSpeedUpgrade).build())
    val AdvancedSatellite = create("advanced_satellite", UpgradeType.Builder(::AdvancedSatelliteUpgrade).build())
    val CraftingByproduct = create("crafting_byproduct", UpgradeType.Builder(::CraftingByproductUpgrade).build())
    val CraftingCleanup = create("crafting_cleanup", UpgradeType.Builder(::CraftingCleanupUpgrade).build())
    val CraftingMonitoring = create("crafting_monitoring", UpgradeType.Builder(::CraftingMonitoringUpgrade).build())
    val Disconnection = create("disconnection", UpgradeType.Builder(::DisconnectionUpgrade).build())
    val FluidCrafting = create("fluid_crafting", UpgradeType.Builder(::FluidCraftingUpgrade).build())
    val Fuzzy = create("fuzzy", UpgradeType.Builder(::FuzzyCraftingUpgrade).build())
    val ItemExtraction = create("item_extraction", UpgradeType.Builder(::ItemExtractionUpgrade).build())
    val LogicController = create("logic_controller", UpgradeType.Builder(::LogicControllerUpgrade).build())
    val Module = create("module", UpgradeType.Builder(::ModuleUpgrade).build())
    val Opaque = create("opaque", UpgradeType.Builder(::OpaqueUpgrade).build())
    val Pattern = create("pattern", UpgradeType.Builder(::PatternUpgrade).build())
    val PowerTransportation = create("power_transportation", UpgradeType.Builder(::PowerTransportationUpgrade).build())
    val SneakyCombination = create("sneaky_combination", UpgradeType.Builder(::SneakyCombinationUpgrade).build())
    val Sneaky = create("sneaky", UpgradeType.Builder(::SneakyUpgrade).build())
    val Speed = create("speed", UpgradeType.Builder(::SpeedUpgrade).build())
    val StackExtraction = create("stack_extraction", UpgradeType.Builder(::StackExtractionUpgrade).build())

    private fun <T : UpgradeType<*>> create(name: String, type: T): T {
        return Registry.register(Registries.UpgradeType, Identifier(ModID, name), type)
    }

}