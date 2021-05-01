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

import io.netty.buffer.ByteBuf
import logisticspipes.LPConstants
import logisticspipes.LogisticsPipes
import logisticspipes.modules.*
import logisticspipes.pipes.*
import logisticspipes.pipes.basic.CoreRoutedPipe
import logisticspipes.pipes.basic.CoreUnroutedPipe
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe
import logisticspipes.pipes.unrouted.PipeItemsBasicTransport
import logisticspipes.proxy.MainProxy
import mcjty.theoneprobe.api.*
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor
import network.rs485.logisticspipes.module.AsyncExtractorModule
import network.rs485.logisticspipes.util.LPDataIOWrapper
import network.rs485.logisticspipes.util.LPDataInput
import network.rs485.logisticspipes.util.LPDataOutput
import network.rs485.logisticspipes.util.TextUtil
import java.util.*
import java.util.function.Function
import kotlin.properties.Delegates

class TheOneProbeIntegration : Function<ITheOneProbe, Void?> {

    /** general translation key prefix for The One Probe translation keys */
    private val prefix = "top.logisticspipes."
    /** very simple translation key regex */
    private val translationKeyRegex = Regex("([a-z]+\\.)+[a-z]+")
    private var lpTextElementId by Delegates.notNull<Int>()
    private var renderText: ((x: Int, y: Int, txt: String) -> Int)? = null

    override fun apply(probe: ITheOneProbe): Void? {
        lpTextElementId = probe.registerElementFactory(::LPText)
        MainProxy.runOnClient(null) {
            Runnable {
                renderText = try {
                    val renderHelper = Class.forName("mcjty.theoneprobe.rendering.RenderHelper")
                    val renderTextMethod = renderHelper.getDeclaredMethod(
                        "renderText",
                        Minecraft::class.java,
                        Int::class.java,
                        Int::class.java,
                        String::class.java
                    );
                    // returns the width of the rendered text
                    { x, y, txt -> renderTextMethod.invoke(null, Minecraft.getMinecraft(), x, y, txt) as Int }
                } catch (e: ReflectiveOperationException) {
                    LogisticsPipes.log.error("Could not acquire RenderHelper.renderText", e)
                    null
                }
            }
        }

        probe.registerProvider(PipeInfoProvider())
        LogisticsPipes.log.info("The One Probe integration loaded.")
        return null
    }

    private inner class PipeInfoProvider : IProbeInfoProvider {

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
                            if (LogisticsPipes.isDEBUG()) {
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
            val allowed = "${prefix}pipe.firewall.allowed"
            val blocked = "${prefix}pipe.firewall.blocked"
            if (!pipe.inv.isEmpty) {
                probeInfo.element(LPText("${prefix}pipe.firewall.filtering").apply {
                    arguments.add(pipe.inv.itemsAndCount.count { it.value > 0 }.toString())
                    arguments.add(if (pipe.isBlocking) blocked else allowed)
                })
            }
            listOf(
                "pipe.firewall.providing" to pipe.isBlockProvider,
                "pipe.firewall.crafting" to pipe.isBlockCrafer,
                "pipe.firewall.sorting" to pipe.isBlockSorting,
                "pipe.firewall.power" to pipe.isBlockPower,
            ).forEach {
                probeInfo.element(LPText(prefix + it.first).apply {
                    arguments.add(if (it.second) blocked else allowed)
                })
            }
        }

        private fun addSatellitePipeInfo(pipe: PipeItemsSatelliteLogistics, probeInfo: IProbeInfo) {
            val satellitePipeName = pipe.satellitePipeName
            if (satellitePipeName.isNotBlank()) {
                probeInfo.element(LPText("${prefix}pipe.satellite.name").apply { arguments.add(satellitePipeName) })
            } else {
                probeInfo.element(LPText("${prefix}pipe.satellite.no_name"))
            }
        }

        private fun addBasicTransportPipeInfo(pipe: PipeItemsBasicTransport, logisticsPipesInfoContainer: IProbeInfo) {
            val connections = pipe.container?.pipeConnectionsBuffer?.count { it } ?: 0
            if (connections > 2) {
                logisticsPipesInfoContainer.element(LPText("${prefix}pipe.unrouted.too_many_connections"))
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
                        probeInfo.element(LPText("${prefix}general.upgrades").apply {
                            arguments.add(
                                upgrades.joinToString(
                                    separator = "\$WHITE, \$AQUA",
                                    prefix = "\$AQUA",
                                    postfix = "\$WHITE;",
                                    limit = 3
                                )
                            )
                        })
                    } else {
                        probeInfo.element(LPText("${prefix}general.no_upgrades"))
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
            if (modules.isNotEmpty()) {
                if (mode == ProbeMode.EXTENDED) {
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
                                positiveTranslationKey = "${prefix}module.passive_supplier.filter",
                                negativeTranslationKey = "${prefix}module.passive_supplier.no_filter",
                                items = module.filterInventory,
                                isModule = isModule
                            )
                            is ModuleTerminus -> addFilteringListItemIdentifierInfo(
                                probeInfo = infoCol,
                                mode = mode,
                                positiveTranslationKey = "${prefix}module.terminus.filter",
                                negativeTranslationKey = "${prefix}module.terminus.no_filter",
                                items = module.filterInventory,
                                isModule = isModule
                            )
                            is ModuleEnchantmentSinkMK2 -> addFilteringListItemIdentifierInfo(
                                probeInfo = infoCol,
                                mode = mode,
                                positiveTranslationKey = "${prefix}module.enchantment_sink.filter",
                                negativeTranslationKey = "${prefix}module.enchantment_sink.no_filter",
                                items = module.filterInventory,
                                isModule = isModule
                            )
                            is ModuleCreativeTabBasedItemSink -> addFilteringListStringInfo(
                                probeInfo = infoCol,
                                mode = mode,
                                positiveTranslationKey = "${prefix}module.creative_tab_item_sink.filter",
                                negativeTranslationKey = "${prefix}module.creative_tab_item_sink.no_filter",
                                strings = module.tabList,
                                isModule = isModule
                            )
                            is ModuleModBasedItemSink -> addFilteringListStringInfo(
                                probeInfo = infoCol,
                                mode = mode,
                                positiveTranslationKey = "${prefix}module.mod_item_sink.filter",
                                negativeTranslationKey = "${prefix}module.mod_item_sink.no_filter",
                                strings = module.modList,
                                isModule = isModule
                            )
                            is ModuleOreDictItemSink -> addFilteringListStringInfo(
                                probeInfo = infoCol,
                                mode = mode,
                                positiveTranslationKey = "${prefix}module.ore_item_sink.filter",
                                negativeTranslationKey = "${prefix}module.ore_item_sink.no_filter",
                                strings = module.oreList,
                                isModule = isModule
                            )
                        }
                    }
                } else {
                    val infoRow = probeInfo.horizontal()
                    modules.forEach { (_, stack) ->
                        infoRow.item(stack)
                    }
                }
            } else {
                chassisColumn.element(LPText("${prefix}pipe.chassis.no_modules"))
            }
        }

        private fun addItemSinkModuleInfo(
            module: ModuleItemSink,
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            isModule: Boolean
        ) {
            if (mode == ProbeMode.EXTENDED) {
                if (module.isDefaultRoute) {
                    probeInfo.element(LPText("${prefix}general.is_default_route").apply {
                        baseFormatting.addAll(italic(isModule))
                        prepend = prepend(isModule)
                    })
                } else if (isModule) {
                    probeInfo.element(LPText("${prefix}general.is_not_default_route").apply {
                        baseFormatting.addAll(italic(isModule))
                        prepend = prepend(isModule)
                    })
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
                    probeInfo.element(LPText("${prefix}module.active_supplier.no_filter").apply {
                        baseFormatting.addAll(italic(isModule))
                        prepend = prepend(isModule)
                    })
                } else {
                    probeInfo.element(LPText("${prefix}module.active_supplier.mode").apply {
                        baseFormatting.addAll(italic(isModule))
                        prepend = prepend(isModule)
                        arguments.add(module.requestMode.value.name)
                    })
                    addFilteringListItemIdentifierInfo(
                        probeInfo = probeInfo,
                        mode = mode,
                        positiveTranslationKey = "${prefix}module.active_supplier.filter",
                        negativeTranslationKey = "",
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
                    positiveTranslationKey = if (module.itemsIncluded.value) {
                        "${prefix}module.advanced_extractor.only"
                    } else {
                        "${prefix}module.advanced_extractor.but"
                    },
                    negativeTranslationKey = if (module.itemsIncluded.value) {
                        "${prefix}module.advanced_extractor.none"
                    } else {
                        "${prefix}module.advanced_extractor.all"
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
                probeInfo.element(LPText("${prefix}module.extractor.side").apply {
                    baseFormatting.addAll(italic(isModule))
                    prepend = prepend(isModule)
                    arguments.add(direction.name2)
                })
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
                    positiveTranslationKey = if (module.isExclusionFilter.value) {
                        "${prefix}module.provider.but"
                    } else {
                        "${prefix}module.provider.only"
                    },
                    // TODO change this if the behaviour ever changes "module.provider.none"
                    negativeTranslationKey = "${prefix}module.provider.all",
                    items = module.filterInventory,
                    isModule = isModule
                )
                if (!isModule) {
                    probeInfo.element(LPText("${prefix}module.provider.mode"))
                }
                probeInfo.element(LPText(module.providerMode.value.extractionModeTranslationKey).apply {
                    baseFormatting.addAll(italic(isModule))
                    prepend = prepend(isModule)
                })
            }
        }

        private fun addCraftingModuleInfo(
            module: ModuleCrafter,
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            isModule: Boolean
        ) {
            if (mode == ProbeMode.EXTENDED) {
                if (module.craftedItem != null) {
                    val fuzzyText = if (module.hasFuzzyUpgrade()) " \$GOLD[Fuzzy]\$WHITE" else ""
                    if (module.hasByproductUpgrade() && module.byproductItem != null) {
                        probeInfo.element(LPText("${prefix}module.crafting.result_with_byproduct").apply {
                            baseFormatting.addAll(italic(isModule))
                            prepend = prepend(isModule)
                            append = fuzzyText
                            arguments.add(module.craftedItem!!.friendlyName)
                            arguments.add(module.byproductItem!!.friendlyName)
                        })
                    } else {
                        probeInfo.element(LPText("${prefix}module.crafting.result").apply {
                            baseFormatting.addAll(italic(isModule))
                            prepend = prepend(isModule)
                            append = fuzzyText
                            arguments.add(module.craftedItem!!.friendlyName)
                        })
                    }
                } else {
                    probeInfo.element(LPText("${prefix}module.crafting.no_result").apply {
                        baseFormatting.addAll(italic(isModule))
                        prepend = prepend(isModule)
                    })
                }
            }
        }

        private fun addFilteringListItemIdentifierInfo(
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            positiveTranslationKey: String,
            negativeTranslationKey: String,
            items: IItemIdentifierInventory,
            isModule: Boolean,
            color: TextFormatting = TextFormatting.WHITE
        ) {
            addFilteringListStringInfo(
                probeInfo = probeInfo,
                mode = mode,
                positiveTranslationKey = positiveTranslationKey,
                negativeTranslationKey = negativeTranslationKey,
                strings = items.itemsAndCount.mapNotNull {
                    if (it.value > 0) it.key.friendlyName else null
                },
                isModule = isModule,
                color = color
            )
        }

        private fun addFilteringListStringInfo(
            probeInfo: IProbeInfo,
            mode: ProbeMode,
            positiveTranslationKey: String,
            negativeTranslationKey: String,
            strings: List<String>,
            limit: Int = 3,
            isModule: Boolean,
            color: TextFormatting = TextFormatting.WHITE
        ) {
            if (mode == ProbeMode.EXTENDED) {
                if (strings.isNotEmpty() && positiveTranslationKey.isNotBlank()) {
                    probeInfo.element(LPText(positiveTranslationKey).apply {
                        baseFormatting.addAll(italic(isModule, color))
                        prepend = prepend(isModule)
                        arguments.add(
                            strings.joinToString(
                                separator = "\$WHITE, \$AQUA",
                                prefix = "\$AQUA",
                                postfix = "\$WHITE",
                                limit = limit
                            )
                        )
                    })
                } else if (negativeTranslationKey.isNotBlank()) {
                    probeInfo.element(LPText(negativeTranslationKey).apply {
                        baseFormatting.addAll(italic(isModule))
                        prepend = prepend(isModule)
                    })
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

        fun italic(italic: Boolean, color: TextFormatting = TextFormatting.WHITE): EnumSet<TextFormatting> =
            if (italic) EnumSet.of(TextFormatting.ITALIC, color) else EnumSet.of(color)

        fun prepend(isModule: Boolean): String = if (isModule) "- " else ""
    }

    inner class LPText : IElement {
        var append: String = ""
        var prepend: String = ""
        val baseFormatting: EnumSet<TextFormatting> = EnumSet.noneOf(TextFormatting::class.java)
        val arguments: MutableList<String> = ArrayList<String>()
        var key: String? = null

        /**
         * Only for clients.
         */
        val translated
            get() = TextUtil.translate(
                key = key!!,
                baseFormatting = baseFormatting,
                prepend = translateIfApplicable(prepend),
                append = translateIfApplicable(append),
                args = arguments.map { translateIfApplicable(it) }.toTypedArray(),
            )

        private fun translateIfApplicable(text: String) =
            if (translationKeyRegex.matches(text)) TextUtil.translate(text) else text

        constructor(key: String) {
            this.key = key
        }

        constructor(buf: ByteBuf) {
            try {
                LPDataIOWrapper.provideData(buf) { input ->
                    key = input.readUTF()
                    input.readArrayList(LPDataInput::readUTF)?.filterNotNull()?.also { arguments.addAll(it) }
                    baseFormatting.addAll(input.readEnumSet(TextFormatting::class.java))
                    input.readUTF()?.also { prepend = it }
                    input.readUTF()?.also { append = it }
                }
            } catch (e: Exception) {
                LogisticsPipes.log.error("Problem when reading buffer for TheOneProbe", e)
            }
        }

        override fun toBytes(buf: ByteBuf) = try {
            LPDataIOWrapper.writeData(buf) {
                it.writeUTF(key)
                it.writeCollection(arguments, LPDataOutput::writeUTF)
                it.writeEnumSet(baseFormatting, TextFormatting::class.java)
                it.writeUTF(prepend)
                it.writeUTF(append)
            }
        } catch (e: Exception) {
            LogisticsPipes.log.error("Problem when writing buffer for TheOneProbe", e)
        }

        /**
         * Obviously only for clients.
         */
        override fun render(x: Int, y: Int) {
            renderText?.invoke(x, y, translated)
        }

        /**
         * Only for clients.
         */
        override fun getWidth(): Int {
            return Minecraft.getMinecraft()?.fontRenderer?.getStringWidth(translated) ?: 0
        }

        override fun getHeight(): Int {
            return 10
        }

        override fun getID(): Int {
            return lpTextElementId
        }

    }

}
