/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
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

package network.rs485.logisticspipes.compat

import logisticspipes.LPConstants
import logisticspipes.LogisticsPipes
import logisticspipes.modules.*
import logisticspipes.pipes.*
import logisticspipes.pipes.basic.CoreRoutedPipe
import logisticspipes.pipes.basic.CoreUnroutedPipe
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe
import logisticspipes.pipes.unrouted.PipeItemsBasicTransport
import mcjty.theoneprobe.api.*
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor
import network.rs485.logisticspipes.module.AsyncExtractorModule
import network.rs485.logisticspipes.util.TextUtil
import java.util.function.Function

class TheOneProbeIntegration : Function<ITheOneProbe, Void?> {

    override fun apply(probe: ITheOneProbe): Void? {
        LogisticsPipes.log.info("The One Probe integration loaded.")
        probe.registerProvider(PipeInfoProvider)
        return null
    }

    private object PipeInfoProvider : IProbeInfoProvider {

        private const val prefix = "top.logistics_pipes."

        override fun getID(): String = "${LPConstants.LP_MOD_ID}:pipe_info_provider"

        override fun addProbeInfo(
            mode: ProbeMode,
            probeInfo: IProbeInfo?,
            player: EntityPlayer?,
            world: World,
            blockState: IBlockState?,
            data: IProbeHitData?,
        ) {
            if (probeInfo == null || blockState == null || data == null) return
            when (blockState.block) {
                is LogisticsBlockGenericPipe -> {
                    val pipe = LogisticsBlockGenericPipe.getPipe(world, data.pos)
                    when (pipe) {
                        is PipeItemsCraftingLogistics -> {
                            addCraftingInfo(pipe, probeInfo, mode)
                        }
                        is PipeLogisticsChassis -> {
                            addChassisPipeInfo(pipe, probeInfo, mode)
                        }
                        is PipeItemsBasicTransport -> {
                            addBasicTransportPipeInfo(pipe, probeInfo, mode)
                        }
                        is PipeItemsBasicLogistics -> {
                            addBasicLogisticsPipeInfo(pipe, probeInfo, mode)
                        }
                        is PipeItemsSatelliteLogistics -> {
                            addSatellitePipeInfo(pipe, probeInfo, mode)
                        }
                        is PipeItemsRequestLogistics -> {
                            defaultInfo(pipe, probeInfo, mode)
                        }
                        else -> {
                            probeInfo.text("Not implemented.")
                            probeInfo.text(pipe.javaClass.name)
                        }
                    }
                }
            }
        }

        private fun addSatellitePipeInfo(pipe: PipeItemsSatelliteLogistics, probeInfo: IProbeInfo, mode: ProbeMode) {
            val lpRectangle = probeInfo.getLogisticsPipesInfoContainer().vertical()
            val satellitePipeName = pipe.satellitePipeName
            if (satellitePipeName.isNotBlank()) {
                lpRectangle.translatedText("satellite_name")
                lpRectangle.text(satellitePipeName)
            } else {
                lpRectangle.translatedText("satellite_no_name")
            }
        }

        private fun addBasicLogisticsPipeInfo(pipe: PipeItemsBasicLogistics, probeInfo: IProbeInfo, mode: ProbeMode) {
            if (pipe.hasGenericInterests() || mode == ProbeMode.EXTENDED) {
                val lpRectangle = probeInfo.getLogisticsPipesInfoContainer()
                if (pipe.hasGenericInterests()) lpRectangle.translatedText("general_default_route")
                addUpgradesInfo(pipe, lpRectangle, mode)
            }
        }

        private fun addBasicTransportPipeInfo(
            pipe: PipeItemsBasicTransport,
            logisticsPipesInfoContainer: IProbeInfo,
            mode: ProbeMode,
        ) {
            val connections = pipe.container?.pipeConnectionsBuffer?.count { it } ?: 0
            if (connections > 2) {
                logisticsPipesInfoContainer.text("Warning:")
                    .text("Unrouted pipes should not have more than 2 connections!")
            }
        }

        private fun defaultInfo(pipe: CoreUnroutedPipe, probeInfo: IProbeInfo, mode: ProbeMode) {
            if (mode == ProbeMode.EXTENDED) {
                val lpRectangle = probeInfo.getLogisticsPipesInfoContainer()
                addUpgradesInfo(pipe, lpRectangle, mode)
            }
        }

        private fun addUpgradesInfo(pipe: CoreUnroutedPipe, probeInfo: IProbeInfo, mode: ProbeMode) {
            if (pipe is CoreRoutedPipe) {
                if (mode == ProbeMode.EXTENDED) {
                    probeInfo.translatedText("general_upgrades")
                    var any = false
                    val upgrades = probeInfo.vertical(probeInfo.defaultLayoutStyle())
                    (0 until pipe.originalUpgradeManager.inv.sizeInventory).mapNotNull {
                        pipe.originalUpgradeManager.inv.getStackInSlot(it)
                    }.filter {
                        !it.isEmpty
                    }.forEach {
                        any = true
                        upgrades.addItemWithText(it)
                    }
                    if (!any) upgrades.translatedText("general_no_upgrades")
                }
            }
        }

        /*
         *  Adds all the useful information about the crafting pipe:
         *  Default:
         *      Result (if any)
         *      Byproduct (if any)
         *  Extended:
         *      Ingredients:
         *          Direct and Satellite.
         */
        private fun addCraftingInfo(pipe: PipeItemsCraftingLogistics, probeInfo: IProbeInfo, mode: ProbeMode) {
            val pipeResult = pipe.dummyInventory.getStackInSlot(9)
            val lpRectangle = probeInfo.getLogisticsPipesInfoContainer()
            if (!pipeResult.isEmpty) {
                lpRectangle.translatedText("crafting_result")
                lpRectangle.addItemWithText(pipeResult)
                val byproduct = pipe.dummyInventory.getStackInSlot(10)
                if (!byproduct.isEmpty) {
                    lpRectangle.translatedText("crafting_extracting_byproduct")
                    lpRectangle.addItemWithText(byproduct)
                }
            } else {
                lpRectangle.translatedText("crafting_no_result")
            }
            addUpgradesInfo(pipe, lpRectangle, mode)
        }

        private fun addChassisPipeInfo(pipe: PipeLogisticsChassis, probeInfo: IProbeInfo, mode: ProbeMode) {
            val chassisColumn = probeInfo.vertical()
            val modules = (0 until pipe.chassieSize).mapNotNull { slotId ->
                val module = pipe.getSubModule(slotId)
                val stack = pipe.getModuleInventory().getStackInSlot(slotId)
                if (module == null) null
                else module to stack
            }
            if(modules.isNotEmpty()){
                modules.forEach { (module, stack) ->
                    val infoCol = chassisColumn.addItemWithText(stack)
                    when (module) {
                        is ModuleItemSink -> addItemSinkModuleInfo(module, infoCol, mode)
                        is ModulePassiveSupplier -> {}
                        is ModuleActiveSupplier -> {}
                        is AsyncExtractorModule -> {}
                        is AsyncAdvancedExtractor -> {}
                        is ModuleTerminus -> addTerminusModuleInfo(module, infoCol, mode)
                        is ModuleProvider -> addProviderModuleInfo(module, infoCol, mode)
                        is ModuleEnchantmentSinkMK2 -> {}
                        is ModuleCrafter -> addCraftingModuleInfo(module, infoCol, mode)
                        is ModuleCreativeTabBasedItemSink -> {
                            addStringBasedItemSinkInfo(infoCol, mode, "module_creative_tab_item_sink_filtered", "module_creative_tab_item_sink_no_filter", module.tabList)
                        }
                        is ModuleModBasedItemSink -> {
                            addStringBasedItemSinkInfo(infoCol, mode, "module_mod_item_sink_filtered", "module_mod_item_sink_no_filter", module.modList)
                        }
                        is ModuleOreDictItemSink -> {
                            addStringBasedItemSinkInfo(infoCol, mode, "module_ore_item_sink_filtered", "module_ore_item_sink_no_filter", module.oreList)
                        }
                    }
                }
            } else {
                chassisColumn.translatedText("chassis_no_modules")
            }
        }

        /*
         Module information providers.
         */

        private fun addItemSinkModuleInfo(module: ModuleItemSink, probeInfo: IProbeInfo, mode: ProbeMode) {
            if(mode == ProbeMode.EXTENDED){
                if(module.isDefaultRoute) {
                    probeInfo.translatedText("general_is_default_route")
                } else {
                    probeInfo.translatedText("general_is_not_default_route")
                }
            }
        }
        private fun addPassiveSupplierModuleInfo(module: ModulePassiveSupplier, probeInfo: IProbeInfo, mode: ProbeMode) {}
        private fun addActiveSupplierModuleInfo(module: ModuleActiveSupplier, probeInfo: IProbeInfo, mode: ProbeMode) {}
        private fun addExtractorModuleInfo(module: AsyncExtractorModule, probeInfo: IProbeInfo, mode: ProbeMode) {}
        private fun addAdvancedExtractorModuleInfo(module: AsyncAdvancedExtractor, probeInfo: IProbeInfo, mode: ProbeMode) {}
        private fun addTerminusModuleInfo(module: ModuleTerminus, probeInfo: IProbeInfo, mode: ProbeMode) {
            if(mode == ProbeMode.EXTENDED){
                if (!module.getFilterInventory().isEmpty) {
                    probeInfo.translatedText("module_terminus_terminated_items")
                }
            }
        }
        private fun addProviderModuleInfo(module: ModuleProvider, probeInfo: IProbeInfo, mode: ProbeMode) {
            if(mode == ProbeMode.EXTENDED){
                if(!module.filterInventory.isEmpty){
                    val modeString = TextUtil.translate(prefix + if(module.isExclusionFilter.value) {
                        "general_exclude"
                    } else {
                        "general_include"
                    })
                    probeInfo.translateFormatting("module_provider_filter", modeString)
                }
                probeInfo.text("§o" + module.providerMode.value.extractionModeString)
            }
        }
        private fun addOreDictBasedItemSinkModuleInfo(module: ModuleOreDictItemSink, probeInfo: IProbeInfo, mode: ProbeMode) {
            if (mode == ProbeMode.EXTENDED){
                if(module.oreList.isNotEmpty()) {
                    probeInfo.translateFormatting("module_mod_item_sink_filtered",
                        module.oreList.joinToString(
                            separator = "§f§o, §b§o",
                            prefix = "§b§o",
                            postfix = "§f§o;",
                            limit = 3
                        )
                    )
                } else {
                    probeInfo.translatedText("module_mod_item_sink_no_filter")
                }
            }
        }
        private fun addEnchantmentSinkModuleMk2Info(module: ModuleEnchantmentSinkMK2, probeInfo: IProbeInfo, mode: ProbeMode) {}
        private fun addCraftingModuleInfo(module: ModuleCrafter, probeInfo: IProbeInfo, mode: ProbeMode) {
            if(mode == ProbeMode.EXTENDED){
                val result = module.craftedItem?.makeNormalStack()?: ItemStack.EMPTY
                val byproduct = module.byproductItem?.makeNormalStack()?: ItemStack.EMPTY
                if(!result.isEmpty){
                    if (byproduct.isEmpty){
                        probeInfo.translateFormatting( "module_crafting_result", result.displayName)
                    } else {
                        probeInfo.translateFormatting( "module_crafting_result_with_byproduct", result.displayName, byproduct.displayName)
                    }
                }
            }
        }
        private fun addCreativeTabBasedItemModuleInfo(module: ModuleCreativeTabBasedItemSink, item: ItemStack, probeInfo: IProbeInfo, mode: ProbeMode) {}

        private fun addStringBasedItemSinkInfo(probeInfo: IProbeInfo, mode: ProbeMode, positiveString: String, negativeString: String, strings: List<String>){
            if (mode == ProbeMode.EXTENDED){
                if(strings.isNotEmpty()) {
                    probeInfo.translateFormatting(positiveString,
                        strings.joinToString(
                            separator = "§f§o, §b§o",
                            prefix = "§b§o",
                            postfix = "§f§o;",
                            limit = 3
                        )
                    )
                } else {
                    probeInfo.translatedText(negativeString)
                }
            }
        }

        /**
         * Adds a item icon/text combo.
         * @param itemStack item to be displayed.
         * @param text to be displayed, will display the item's translated name if blank.
         */
        fun IProbeInfo.addItemWithText(itemStack: ItemStack, text: String = ""): IProbeInfo {
            val resultLine =
                horizontal(defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(3))
            resultLine.vertical().item(itemStack)
            val textColumn = resultLine.vertical()
            if (text.isBlank()) {
                textColumn.itemLabel(itemStack)
            } else {
                textColumn.text(text)
            }
            return textColumn
        }

        fun IProbeInfo.getLogisticsPipesInfoContainer(): IProbeInfo =
            horizontal(defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .vertical(defaultLayoutStyle().borderColor(0xa0FF5555.toInt()).spacing(3))

        fun IProbeInfo.translatedText(text: String): IProbeInfo = text("$prefix${text}".translated())

        fun IProbeInfo.translateFormatting(text: String, vararg args: String): IProbeInfo =
            text(I18n.format(prefix + text, *args))

        fun String.translated(): String = "${IProbeInfo.STARTLOC}$this${IProbeInfo.ENDLOC}"
    }

}
