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
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor
import network.rs485.logisticspipes.module.AsyncExtractorModule
import network.rs485.logisticspipes.util.TextUtil
import java.util.*
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
                    val isModule = false
                    val pipe = LogisticsBlockGenericPipe.getPipe(world, data.pos)
                    when (pipe) {
                        is PipeItemsFirewall -> addFirewallPipeInfo(pipe, probeInfo)
                        is PipeLogisticsChassis -> addChassisPipeInfo(pipe, probeInfo, mode)
                        is PipeItemsBasicTransport -> addBasicTransportPipeInfo(pipe, probeInfo)
                        is PipeItemsSatelliteLogistics -> addSatellitePipeInfo(pipe, probeInfo)
                        is PipeItemsBasicLogistics -> addItemSinkModuleInfo(
                            module = pipe.logisticsModule,
                            probeInfo = probeInfo,
                            mode = ProbeMode.EXTENDED,
                            isModule = isModule
                        )
                        is PipeItemsSupplierLogistics -> addActiveSupplierModuleInfo(
                            module = pipe.logisticsModule,
                            probeInfo = probeInfo,
                            mode = ProbeMode.EXTENDED,
                            isModule = isModule
                        )
                        is PipeItemsCraftingLogistics -> addCraftingModuleInfo(
                            module = pipe.logisticsModule,
                            probeInfo = probeInfo,
                            mode = ProbeMode.EXTENDED,
                            isModule = isModule
                        )
                        is PipeItemsProviderLogistics -> addProviderModuleInfo(
                            module = pipe.logisticsModule,
                            probeInfo = probeInfo,
                            mode = ProbeMode.EXTENDED,
                            isModule = isModule
                        )
                        is PipeItemsSystemDestinationLogistics -> Unit // TODO pipe doesn't work atm
                        is PipeItemsSystemEntranceLogistics -> Unit // TODO pipe doesn't work atm
                        is PipeItemsRemoteOrdererLogistics -> Unit
                        is PipeItemsRequestLogistics -> Unit
                        else -> {
                            if(LogisticsPipes.isDEBUG()) {
                                probeInfo.text("Not implemented.")
                                probeInfo.text(pipe.javaClass.name)
                            }
                        }
                    }
                    defaultInfo(pipe, probeInfo, mode)
                }
            }
        }

        private fun addFirewallPipeInfo(pipe: PipeItemsFirewall, probeInfo: IProbeInfo) {
            val translatedAllowed = translate("pipe.firewall.allowed",
                EnumSet.noneOf(TextFormatting::class.java),
                prepend = "",
                append = ""
            )
            val translatedBlocked = translate("pipe.firewall.blocked",
                EnumSet.noneOf(TextFormatting::class.java),
                prepend = "",
                append = ""
            )
            if(!pipe.inv.isEmpty){
                probeInfo.translatedFormatting(
                    "pipe.firewall.filtering",
                    pipe.inv.itemsAndCount.keys.count { !it.makeNormalStack(1).isEmpty }.toString(),
                    TextUtil.translate(if(pipe.isBlocking) translatedBlocked else translatedAllowed).toLowerCase()
                )
            }
            probeInfo.translatedFormatting(
                "pipe.firewall.providing",
                if(pipe.isBlockProvider){
                    translatedBlocked
                } else {
                    translatedAllowed
                }
            )
            probeInfo.translatedFormatting(
                "pipe.firewall.crafting",
                if(pipe.isBlockCrafer){
                    translatedBlocked
                } else {
                    translatedAllowed
                }
            )
            probeInfo.translatedFormatting(
                "pipe.firewall.sorting",
                if(pipe.isBlockSorting){
                    translatedBlocked
                } else {
                    translatedAllowed
                }
            )
            probeInfo.translatedFormatting(
                "pipe.firewall.power",
                if(pipe.isBlockPower){
                    translatedBlocked
                } else {
                    translatedAllowed
                }
            )
        }

        private fun addSatellitePipeInfo(pipe: PipeItemsSatelliteLogistics, probeInfo: IProbeInfo) {
            val satellitePipeName = pipe.satellitePipeName
            if (satellitePipeName.isNotBlank()) {
                probeInfo.translatedFormatting("pipe.satellite.name", satellitePipeName)
            } else {
                probeInfo.translatedFormatting("pipe.satellite.no_name")
            }
        }

        private fun addBasicLogisticsPipeInfo(pipe: PipeItemsBasicLogistics, probeInfo: IProbeInfo, mode: ProbeMode) {
            if (pipe.hasGenericInterests() || mode == ProbeMode.EXTENDED) {
                if (pipe.hasGenericInterests()) probeInfo.translatedFormatting("general.is_default_route")
            }
        }

        private fun addBasicTransportPipeInfo(pipe: PipeItemsBasicTransport, logisticsPipesInfoContainer: IProbeInfo) {
            val connections = pipe.container?.pipeConnectionsBuffer?.count { it } ?: 0
            if (connections > 2) {
                logisticsPipesInfoContainer.translatedFormatting("pipe.unrouted.too_many_connections")
            }
        }

        private fun defaultInfo(pipe: CoreUnroutedPipe, probeInfo: IProbeInfo, mode: ProbeMode) {
            if (mode == ProbeMode.EXTENDED) {
                addUpgradesInfo(pipe, probeInfo, mode)
            }
        }

        private fun addUpgradesInfo(pipe: CoreUnroutedPipe, probeInfo: IProbeInfo, mode: ProbeMode) {
            if (pipe is CoreRoutedPipe) {
                if (mode == ProbeMode.EXTENDED) {
                    val upgradeManagerInv = pipe.originalUpgradeManager.inv
                    val upgrades = (0 until upgradeManagerInv.sizeInventory).mapNotNull { slotId ->
                        upgradeManagerInv.getStackInSlot(slotId).takeIf { !it.isEmpty }?.displayName
                    }
                    if (upgrades.isNotEmpty()) {
                        probeInfo.translatedFormatting(
                            "general.upgrades",
                            upgrades.joinToString(
                                separator = "\$WHITE, \$AQUA",
                                prefix = "\$AQUA",
                                postfix = "\$WHITE;",
                                limit = 3
                            )
                        )
                    } else {
                        probeInfo.translatedFormatting("general.no_upgrades")
                    }
                }
            }
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
                    val isModule = true
                    when (module) {
                        is ModuleItemSink -> addItemSinkModuleInfo(module, infoCol, mode, isModule)
                        is ModuleProvider -> addProviderModuleInfo(module, infoCol, mode, isModule)
                        is ModuleCrafter -> addCraftingModuleInfo(module, infoCol, mode, isModule)
                        is ModuleActiveSupplier -> addActiveSupplierModuleInfo(module, infoCol, mode, isModule)
                        is AsyncExtractorModule -> addExtractorModuleInfo(module, infoCol, mode)
                        is AsyncAdvancedExtractor -> addAdvancedExtractorModuleInfo(module, infoCol, mode)
                        is ModulePassiveSupplier -> addFilteringListItemIdentifierInfo(
                            probeInfo = infoCol,
                            mode = mode,
                            positiveString = "module.passive_supplier.filter",
                            negativeString = "module.passive_supplier.no_filter",
                            items = module.filterInventory,
                            isModule = isModule
                        )
                        is ModuleTerminus -> addFilteringListItemIdentifierInfo(
                            probeInfo = infoCol,
                            mode = mode,
                            positiveString = "module.terminus.filter",
                            negativeString = "module.terminus.no_filter",
                            items = module.filterInventory,
                            isModule = isModule
                        )
                        is ModuleEnchantmentSinkMK2 -> addFilteringListItemIdentifierInfo(
                            probeInfo = infoCol,
                            mode = mode,
                            positiveString = "module.enchantment_sink.filter",
                            negativeString = "module.enchantment_sink.no_filter",
                            items = module.filterInventory,
                            isModule = isModule
                        )
                        is ModuleCreativeTabBasedItemSink -> addFilteringListStringInfo(
                            probeInfo = infoCol,
                            mode = mode,
                            positiveString = "module.creative_tab_item_sink.filter",
                            negativeString = "module.creative_tab_item_sink.no_filter",
                            strings = module.tabList,
                            isModule = isModule
                        )
                        is ModuleModBasedItemSink -> addFilteringListStringInfo(
                            probeInfo = infoCol,
                            mode = mode,
                            positiveString = "module.mod_item_sink.filter",
                            negativeString = "module.mod_item_sink.no_filter",
                            strings = module.modList,
                            isModule = isModule
                        )
                        is ModuleOreDictItemSink -> addFilteringListStringInfo(
                            probeInfo = infoCol,
                            mode = mode,
                            positiveString = "module.ore_item_sink.filter",
                            negativeString = "module.ore_item_sink.no_filter",
                            strings = module.oreList,
                            isModule = isModule
                        )
                    }
                }
            } else {
                chassisColumn.translatedFormatting("pipe.chassis.no_modules")
            }
        }

        /*
         Module information providers.
         */

        private fun addItemSinkModuleInfo(
            module: ModuleItemSink,
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            isModule: Boolean
        ) {
            if (mode == ProbeMode.EXTENDED) {
                if (module.isDefaultRoute) {
                    probeInfo.translatedFormatting(
                        key = "general.is_default_route",
                        baseFormatting = italic(isModule),
                        prepend = prepend(isModule),
                        append = ""
                    )
                } else if (isModule) {
                    probeInfo.translatedFormatting(
                        key = "general.is_not_default_route",
                        baseFormatting = italic(isModule),
                        prepend = prepend(isModule),
                        append = ""
                    )
                }
            }
        }

        private fun addActiveSupplierModuleInfo(
            module: ModuleActiveSupplier,
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            isModule: Boolean
        ) {
            if (mode == ProbeMode.EXTENDED) {
                if (module.inventory.isEmpty) {
                    probeInfo.translatedFormatting(
                        key = "module.active_supplier.no_filter",
                        baseFormatting = italic(isModule),
                        prepend = prepend(isModule),
                        append = ""
                    )
                } else {
                    probeInfo.translatedFormatting(
                        key = "module.active_supplier.mode",
                        baseFormatting = italic(isModule),
                        prepend = prepend(isModule),
                        append = "",
                        module.requestMode.value.name
                    )
                    addFilteringListItemIdentifierInfo(
                        probeInfo = probeInfo,
                        mode = mode,
                        positiveString = "module.active_supplier.filter",
                        negativeString = "",
                        items = module.inventory,
                        isModule = isModule
                    )
                }
            }
        }

        private fun addExtractorModuleInfo(module: AsyncExtractorModule, probeInfo: IProbeInfo, mode: ProbeMode) {
            if (mode == ProbeMode.EXTENDED) {
                addSneakyExtractorInfo(module.sneakyDirection, probeInfo)
            }
        }

        private fun addAdvancedExtractorModuleInfo(
            module: AsyncAdvancedExtractor,
            probeInfo: IProbeInfo,
            mode: ProbeMode
        ) {
            val isModule = true
            if (mode == ProbeMode.EXTENDED) {
                addFilteringListItemIdentifierInfo(
                    probeInfo = probeInfo,
                    mode = mode,
                    positiveString = if (module.itemsIncluded.value) {
                        "module.advanced_extractor.only"
                    } else {
                        "module.advanced_extractor.but"
                    },
                    negativeString = if (module.itemsIncluded.value) {
                        "module.advanced_extractor.none"
                    } else {
                        "module.advanced_extractor.all"
                    },
                    items = module.filterInventory,
                    isModule = isModule
                )
                addSneakyExtractorInfo(module.sneakyDirection, probeInfo)
            }
        }

        private fun addSneakyExtractorInfo(
            direction: EnumFacing?,
            probeInfo: IProbeInfo,
        ) {
            val isModule = true
            if (direction != null) {
                probeInfo.translatedFormatting(
                    key = "module.extractor.side",
                    baseFormatting = italic(isModule),
                    prepend = prepend(isModule),
                    append = "",
                    direction.name2
                )
            }
        }

        private fun addProviderModuleInfo(
            module: ModuleProvider,
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            isModule: Boolean
        ) {
            if (mode == ProbeMode.EXTENDED) {
                addFilteringListItemIdentifierInfo(
                    probeInfo = probeInfo,
                    mode = mode,
                    positiveString = if (module.isExclusionFilter.value) {
                        "module.provider.but"
                    } else {
                        "module.provider.only"
                    },
                    negativeString =
                    // TODO change this if the behaviour ever changes "module.provider.none"
                    "module.provider.all",
                    items = module.filterInventory,
                    isModule = isModule
                )
                if (!isModule) probeInfo.translatedFormatting("module.provider.mode")
                probeInfo.text(
                    prepend(isModule) + TextUtil.transform(
                        text = module.providerMode.value.extractionModeString,
                        baseFormatting = italic(isModule),
                    )
                )
            }
        }

        private fun addCraftingModuleInfo(
            module: ModuleCrafter,
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            isModule: Boolean
        ) {
            if (mode == ProbeMode.EXTENDED) {
                val result = module.craftedItem?.makeNormalStack() ?: ItemStack.EMPTY
                val byproduct = module.byproductItem?.makeNormalStack() ?: ItemStack.EMPTY
                if (!result.isEmpty) {
                    val fuzzyText = if (module.hasFuzzyUpgrade()) " \$GOLD[Fuzzy]\$WHITE" else ""
                    if (!byproduct.isEmpty && module.hasByproductUpgrade()) {
                        probeInfo.translatedFormatting(
                            key = "module.crafting.result_with_byproduct",
                            baseFormatting = italic(isModule),
                            prepend = prepend(isModule),
                            append = fuzzyText,
                            result.displayName,
                            byproduct.displayName
                        )
                    } else {
                        probeInfo.translatedFormatting(
                            key = "module.crafting.result",
                            baseFormatting = italic(isModule),
                            prepend = prepend(isModule),
                            append = fuzzyText,
                            result.displayName
                        )
                    }
                } else {
                    probeInfo.translatedFormatting(
                        key = "module.crafting.no_result",
                        baseFormatting = italic(isModule),
                        prepend = prepend(isModule),
                        append = ""
                    )
                }
            }
        }

        private fun addFilteringListItemIdentifierInfo(
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            positiveString: String,
            negativeString: String,
            items: IItemIdentifierInventory,
            isModule: Boolean,
            color: TextFormatting = TextFormatting.WHITE
        ) {
            addFilteringListStringInfo(
                probeInfo = probeInfo,
                mode = mode,
                positiveString = positiveString,
                negativeString = negativeString,
                strings = items.itemsAndCount.keys.mapNotNull {
                    if (it.makeNormalStack(1).isEmpty) null
                    else it.makeNormalStack(1).displayName
                },
                isModule = isModule,
                color = color
            )
        }

        private fun addFilteringListStringInfo(
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            positiveString: String,
            negativeString: String,
            strings: List<String>,
            limit: Int = 3,
            isModule: Boolean,
            color: TextFormatting = TextFormatting.WHITE
        ) {
            if (mode == ProbeMode.EXTENDED) {
                if (strings.isNotEmpty() && positiveString.isNotBlank()) {
                    probeInfo.translatedFormatting(
                        key = positiveString,
                        baseFormatting = italic(isModule, color),
                        prepend = prepend(isModule),
                        append = "",
                        strings.joinToString(
                            separator = "\$WHITE, \$AQUA",
                            prefix = "\$AQUA",
                            postfix = "\$WHITE",
                            limit = limit
                        )
                    )
                } else if (negativeString.isNotBlank()) {
                    probeInfo.translatedFormatting(
                        key = negativeString,
                        baseFormatting = italic(isModule),
                        prepend = prepend(isModule),
                        append = ""
                    )
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

        fun IProbeInfo.translatedFormatting(key: String, vararg args: String) {
            translatedFormatting(
                key = key,
                baseFormatting = EnumSet.noneOf(TextFormatting::class.java),
                prepend = "",
                append = "",
                args = args
            )
        }

        fun IProbeInfo.translatedFormatting(
            key: String,
            baseFormatting: EnumSet<TextFormatting>,
            prepend: String,
            append: String,
            vararg args: String
        ) {
            text(translate(key = key, baseFormatting = baseFormatting, prepend = prepend, append = append, args = args))
        }

        fun translate(
            key: String,
            baseFormatting: EnumSet<TextFormatting>,
            prepend: String,
            append: String,
            vararg args: String
        ) =
            TextUtil.translate(
                key = key.prefix(),
                baseFormatting = baseFormatting,
                prepend = prepend,
                append = append,
                args = args
            )

        fun String.prefix(): String = prefix + this

        fun italic(italic: Boolean, color: TextFormatting = TextFormatting.WHITE): EnumSet<TextFormatting> =
            if (italic) EnumSet.of(TextFormatting.ITALIC, color) else EnumSet.of(color)

        fun prepend(isModule: Boolean): String = if (isModule) "- " else ""
    }

}
