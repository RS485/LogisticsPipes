/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
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

package network.rs485.logisticspipes.module

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import logisticspipes.gui.hud.modules.HUDAdvancedExtractor
import logisticspipes.interfaces.*
import logisticspipes.network.NewGuiHandler
import logisticspipes.network.PacketHandler
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider
import logisticspipes.network.guis.module.inhand.AdvancedExtractorModuleInHand
import logisticspipes.network.guis.module.inpipe.AdvancedExtractorModuleSlot
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket
import logisticspipes.network.packets.module.ModuleInventory
import logisticspipes.network.packets.modules.AdvancedExtractorInclude
import logisticspipes.proxy.MainProxy
import logisticspipes.proxy.computers.interfaces.CCCommand
import logisticspipes.utils.ISimpleInventoryEventHandler
import logisticspipes.utils.item.ItemIdentifierInventory
import logisticspipes.utils.item.ItemIdentifierStack
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.world.IBlockAccess
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory
import network.rs485.logisticspipes.property.BooleanProperty
import network.rs485.logisticspipes.property.InventoryProperty
import network.rs485.logisticspipes.property.Property
import network.rs485.logisticspipes.util.matchingSequence


class AsyncAdvancedExtractor : AsyncModule<Channel<Pair<Int, ItemStack>>?, List<ExtractorAsyncResult>?>(), SimpleFilter,
    SneakyDirection, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive,
    ISimpleInventoryEventHandler, Gui {
    companion object {
        @JvmStatic
        val name: String = "extractor_advanced"
    }

    private val filterInventory = InventoryProperty(ItemIdentifierInventory(9, "Item list", 1), "")
    val itemsIncluded = BooleanProperty(true, "itemsIncluded")
    override val properties: List<Property<*>>
        get() = extractor.properties + listOf(filterInventory, itemsIncluded)

    private val hud = HUDAdvancedExtractor(this)
    private val extractor = AsyncExtractorModule(inverseFilter = {
        it.isEmpty || itemsIncluded.value != filterInventory.matchingSequence(it).any()
    })

    override var sneakyDirection: EnumFacing?
        get() = extractor.sneakyDirection
        set(value) {
            extractor.sneakyDirection = value
        }

    override val everyNthTick: Int
        get() = extractor.everyNthTick

    override val module = this

    override val pipeGuiProvider: ModuleCoordinatesGuiProvider
        get() = NewGuiHandler.getGui(AdvancedExtractorModuleSlot::class.java).setAreItemsIncluded(itemsIncluded.value)

    override val inHandGuiProvider: ModuleInHandGuiProvider
        get() = NewGuiHandler.getGui(AdvancedExtractorModuleInHand::class.java)

    override fun finishInit() {
        val isInitialized = super.initialized
        super.finishInit()
        if (isInitialized) return
        if (_service != null) {
            val blockAccess: IBlockAccess? = _world?.world
            MainProxy.runOnServer(blockAccess) {
                Runnable {
                    itemsIncluded.addObserver {
                        MainProxy.sendToPlayerList(
                            PacketHandler.getPacket(AdvancedExtractorInclude::class.java)
                                .setFlag(it.copyValue())
                                .setModulePos(this),
                            extractor.localModeWatchers,
                        )
                    }
                }
            }
        }
    }

    override fun getLPName(): String = name

    override fun registerHandler(world: IWorldProvider?, service: IPipeServiceProvider?) {
        super.registerHandler(world, service)
        extractor.registerHandler(world, service)
    }

    override fun registerPosition(slot: ModulePositionType, positionInt: Int) {
        super.registerPosition(slot, positionInt)
        extractor.registerPosition(slot, positionInt)
    }

    @ExperimentalCoroutinesApi
    override fun tickSetup(): Channel<Pair<Int, ItemStack>>? = extractor.tickSetup()

    override fun recievePassive(): Boolean = false

    override fun hasGenericInterests(): Boolean = false

    override fun interestedInUndamagedID(): Boolean = false

    override fun interestedInAttachedInventory(): Boolean = false

    @ExperimentalCoroutinesApi
    override fun completeTick(task: Deferred<List<ExtractorAsyncResult>?>) = extractor.completeTick(task)

    @ExperimentalCoroutinesApi
    @FlowPreview
    override suspend fun tickAsync(setupObject: Channel<Pair<Int, ItemStack>>?): List<ExtractorAsyncResult>? =
        extractor.tickAsync(setupObject)

    override fun runSyncWork() = extractor.runSyncWork()

    @CCCommand(description = "Returns the FilterInventory of this Module")
    override fun getFilterInventory(): IItemIdentifierInventory {
        return filterInventory
    }

    override fun handleInvContent(items: MutableCollection<ItemIdentifierStack>) =
        filterInventory.handleItemIdentifierList(items)

    override fun InventoryChanged(inventory: IInventory?) {
        MainProxy.runOnServer(world) {
            Runnable {
                MainProxy.sendToPlayerList(
                    PacketHandler.getPacket(ModuleInventory::class.java)
                        .setIdentList(ItemIdentifierStack.getListFromInventory(inventory))
                        .setModulePos(this),
                    extractor.localModeWatchers
                )
            }
        }
    }

    override fun getClientInformation(): MutableList<String> {
        val clientInformation = extractor.clientInformation
        clientInformation.add(if (itemsIncluded.value) "Included" else "Excluded")
        clientInformation.add("Filter:")
        clientInformation.addAll(filterInventory.clientInformation)
        return clientInformation
    }

    override fun startWatching(player: EntityPlayer?) {
        extractor.startWatching(player)
        MainProxy.sendPacketToPlayer(
            PacketHandler.getPacket(ModuleInventory::class.java)
                .setIdentList(ItemIdentifierStack.getListFromInventory(filterInventory))
                .setModulePos(this),
            player,
        )
        MainProxy.sendPacketToPlayer(
            PacketHandler.getPacket(AdvancedExtractorInclude::class.java)
                .setFlag(itemsIncluded.value)
                .setModulePos(this),
            player,
        )
    }

    override fun stopWatching(player: EntityPlayer?) = extractor.stopWatching(player)

    override fun startHUDWatching() {
        MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket::class.java)
            .setModulePos(this))
    }

    override fun getHUDRenderer(): IHUDModuleRenderer = hud

    override fun stopHUDWatching() {
        MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket::class.java)
            .setModulePos(this))
    }

}
