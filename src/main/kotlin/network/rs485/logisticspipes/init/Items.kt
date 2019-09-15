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

import logisticspipes.items.LogisticsFluidContainer
import logisticspipes.items.LogisticsItemCard
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroup
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import network.rs485.logisticspipes.ModID
import network.rs485.logisticspipes.item.*
import network.rs485.logisticspipes.pipe.upgrade.SneakyCombinationUpgrade

object Items {

    val SecurityCard = create("security_card", LogisticsItemCard(Settings().group(ItemGroups.All), LogisticsItemCard.Type.SEC_CARD))
    val FrequencyCard = create("frequency_card", LogisticsItemCard(Settings().group(ItemGroups.All), LogisticsItemCard.Type.FREQ_CARD))

    val BlankModule: ItemWithInfo = create("blank_module", ItemWithInfo(Settings().group(ItemGroups.All)))
    val Disk = create("disk", DiskItem(Settings().group(ItemGroups.All).maxCount(1)))

    @JvmField
    val GuideBook = create("guide_book", GuideBookItem(Settings().group(ItemGroups.All)))

    val BasicChip = create("basic_chip", ItemWithInfo(Settings().group(ItemGroups.All)))
    val RawBasicChip = create("raw_basic_chip", ItemWithInfo(Settings().group(ItemGroups.All)))
    val AdvancedChip = create("advanced_chip", ItemWithInfo(Settings().group(ItemGroups.All)))
    val RawAdvancedChip = create("raw_advanced_chip", ItemWithInfo(Settings().group(ItemGroups.All)))
    val FpgaChip = create("fpga_chip", ItemWithInfo(Settings().group(ItemGroups.All)))
    val RawFpgaChip = create("raw_fpga_chip", ItemWithInfo(Settings().group(ItemGroups.All)))

    val HudBow = create("hud_bow", ItemWithInfo(Settings().group(ItemGroups.All)))
    val HudGlass = create("hud_glass", ItemWithInfo(Settings().group(ItemGroups.All)))
    val HudNoseBridge = create("hud_nose_bridge", ItemWithInfo(Settings().group(ItemGroups.All)))
    val NanoHopper = create("nano_hopper", ItemWithInfo(Settings().group(ItemGroups.All)))

    val LogisticsProgrammer = create("programmer", LogisticsProgrammerItem(Settings().group(ItemGroups.All)))
    val PipeController = create("pipe_controller", PipeControllerItem(Settings().group(ItemGroups.All)))
    val PipeManager = create("pipe_manager", PipeManagerItem(Settings().group(ItemGroups.All)))
    val PipeSignCreator = create("sign_creator", PipeSignCreatorItem(Settings().group(ItemGroups.All).maxDamage(250)))

    val RemoteOrderer = create("remote_orderer", RemoteOrdererItem(Settings().group(ItemGroups.All).maxCount(1)))
    val ColoredRemoteOrderers = DyeColor.values().associate { it to create("${it.getName()}_remote_orderer", RemoteOrdererItem(Settings().group(ItemGroups.All).maxCount(1))) }

    val FluidContainer = create("fluid_container", FluidContainerItem(Settings().maxCount(1)))

    val ActionSpeedUpgrade = create("action_speed_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.ActionSpeed))
    val AdvancedSatelliteUpgrade = create("advanced_satellite_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.AdvancedSatellite))
    val CraftingByproductUpgrade = create("crafting_byproduct_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.CraftingByproduct))
    val CraftingCleanupUpgrade = create("crafting_cleanup_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.CraftingCleanup))
    val CraftingMonitoringUpgrade = create("crafting_monitoring_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.CraftingMonitoring))
    val DisconnectionUpgrade = create("disconnection_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.Disconnection))
    val FluidCraftingUpgrade = create("fluid_crafting_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.FluidCrafting))
    val FuzzyUpgrade = create("fuzzy_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.Fuzzy))
    val ItemExtractionUpgrade = create("item_extraction_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.ItemExtraction))
    val LogicControllerUpgrade = create("logic_controller_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.LogicController))
    val ModuleUpgrade = create("module_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.Module))
    val OpaqueUpgrade = create("opaque_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.Opaque))
    val PatternUpgrade = create("pattern_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.Pattern))
    val PowerTransportationUpgrade = create("power_transportation_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.PowerTransportation))
    val SneakyCombinationUpgrade = create("sneaky_combination_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.SneakyCombination))
    val SneakyUpgrade = create("sneaky_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.Sneaky))
    val SpeedUpgrade = create("speed_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.Speed))
    val StackExtractionUpgrade = create("stack_extraction_upgrade", UpgradeItem(Settings().group(ItemGroups.All), UpgradeTypes.StackExtraction))

    private fun <T : Block> create(name: String, block: T): BlockItem {
        return create(name, BlockItem(block, Settings().group(ItemGroups.All)))
    }

    private fun <T : Item> create(name: String, item: T): T {
        return Registry.register(Registry.ITEM, Identifier(ModID, name), item)
    }

}