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
import kotlinx.coroutines.flow.*
import logisticspipes.interfaces.*
import logisticspipes.logistics.AsyncLogisticsManager
import logisticspipes.network.NewGuiHandler
import logisticspipes.network.PacketHandler
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider
import logisticspipes.network.guis.module.inhand.SneakyModuleInHandGuiProvider
import logisticspipes.network.guis.module.inpipe.SneakyModuleInSlotGuiProvider
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket
import logisticspipes.network.packets.modules.SneakyModuleDirectionUpdate
import logisticspipes.pipefxhandlers.Particles
import logisticspipes.pipes.basic.CoreRoutedPipe
import logisticspipes.proxy.MainProxy
import logisticspipes.routing.ServerRouter
import logisticspipes.utils.PlayerCollectionList
import logisticspipes.utils.SinkReply
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.client.FMLClientHandler
import java.util.*
import kotlin.math.min
import kotlin.math.pow

class AsyncExtractorModule : AsyncModule<List<Pair<Int, ItemStack>?>?, List<AsyncExtractorModule.AsyncResult>?>(), Gui, SneakyDirection, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {
    private val localModeWatchers = PlayerCollectionList()
    private val hudRenderer: IHUDModuleRenderer = HUDAsyncExtractor(this)
    private var _sneakyDirection: EnumFacing? = null

    override var sneakyDirection: EnumFacing?
        get() = _sneakyDirection
        set(value) {
            _sneakyDirection = value
            MainProxy.sendToPlayerList(PacketHandler.getPacket(SneakyModuleDirectionUpdate::class.java).setDirection(_sneakyDirection).setModulePos(this), localModeWatchers)
        }

    override val module = this
    override val pipeGuiProvider: ModuleCoordinatesGuiProvider = NewGuiHandler.getGui(SneakyModuleInSlotGuiProvider::class.java).setSneakyOrientation(_sneakyDirection)
    override val inHandGuiProvider: ModuleInHandGuiProvider = NewGuiHandler.getGui(SneakyModuleInHandGuiProvider::class.java)

    private val itemsToExtract: Int
        get() = upgradeManager?.let { 2.0.pow(it.itemExtractionUpgrade) }?.toInt() ?: 0
    private val energyPerItem: Int
        get() = upgradeManager?.let { 5 * 1.1.pow(it.itemExtractionUpgrade) * 1.2.pow(it.itemStackExtractionUpgrade) }?.toInt() ?: 5
    private val itemSendMode: CoreRoutedPipe.ItemSendMode
        get() = upgradeManager?.let { um -> CoreRoutedPipe.ItemSendMode.Fast.takeIf { um.itemExtractionUpgrade > 0 } } ?: CoreRoutedPipe.ItemSendMode.Normal
    override var everyNthTick: Int
        get() = (80 / (upgradeManager?.let { 2.0.pow(it.actionSpeedUpgrade) } ?: 1.0)).toInt().coerceAtLeast(2)
        set(value) {}

    override fun tickSetup(): List<Pair<Int, ItemStack>?>? {
        val direction = sneakyDirection ?: _service.pointedOrientation?.opposite ?: return null
        val directedInventoryUtil = _service.getSneakyInventory(direction) ?: return null
        return (0 until directedInventoryUtil.sizeInventory).map { slot ->
            val stack = directedInventoryUtil.getStackInSlot(slot)
            (slot to stack).takeUnless { stack.isEmpty }
        }
    }

    @ExperimentalCoroutinesApi
    override fun completeTick(task: Deferred<List<AsyncResult>?>) {
        val direction = sneakyDirection ?: _service.pointedOrientation?.opposite ?: return
        val directedInventoryUtil = _service.getSneakyInventory(direction) ?: return
        val inventorySize = directedInventoryUtil.sizeInventory
        var itemsLeft = itemsToExtract
        task.getCompleted()?.forEach { obj ->
            if (itemsLeft <= 0) return@forEach
            if (obj.slot >= inventorySize) return@forEach
            val stack = directedInventoryUtil.getStackInSlot(obj.slot)
            if (!obj.itemid.equalsWithoutNBT(stack)) return@forEach
            var extract = getExtractionMax(itemsLeft, stack, obj.sinkReply)
            while (extract > 0 && !_service.useEnergy(energyPerItem * extract)) {
                _service.spawnParticle(Particles.OrangeParticle, 2)
                extract--
            }
            if (extract <= 0) return@forEach
            val toSend = directedInventoryUtil.decrStackSize(obj.slot, extract).takeUnless { it.isEmpty } ?: return@forEach
            extract = toSend.count
            _service.sendStack(toSend, obj.destRouterId, obj.sinkReply, itemSendMode)
            itemsLeft -= extract
        }
    }

    private fun getExtractionMax(itemsLeft: Int, stack: ItemStack, sinkReply: SinkReply): Int {
        return min(itemsLeft, stack.count).let {
            if (sinkReply.maxNumberOfItems > 0) {
                min(it, sinkReply.maxNumberOfItems)
            } else it
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override suspend fun tickAsync(setupObject: List<Pair<Int, ItemStack>?>?): List<AsyncResult>? {
        setupObject ?: return null
        var itemsLeft = itemsToExtract
        val jamList = LinkedList<Int>()
        val serverRouter = this._service.router as? ServerRouter ?: error("Router was not set or not a ServerRouter")

        return setupObject.asFlow().filterNotNull().flatMapConcat { pair ->
            flow<AsyncResult> {
                if (itemsLeft > 0) {
                    val itemid = ItemIdentifier.get(pair.second)
                    emitAll(AsyncLogisticsManager.allDestinations(itemid, true, serverRouter, jamList) { itemsLeft > 0 }.map { reply ->
                        jamList.add(reply.first)
                        itemsLeft -= getExtractionMax(itemsLeft, pair.second, reply.second)
                        AsyncResult(pair.first, itemid, reply.first, reply.second)
                    })
                }
            }
        }.toList()
    }

    data class AsyncResult(val slot: Int, val itemid: ItemIdentifier, val destRouterId: Int, val sinkReply: SinkReply)

    override fun recievePassive(): Boolean = false

    override fun readFromNBT(nbttagcompound: NBTTagCompound) {
        _sneakyDirection = SneakyDirection.readSneakyDirection(nbttagcompound)
    }

    override fun writeToNBT(nbttagcompound: NBTTagCompound) {
        SneakyDirection.writeSneakyDirection(_sneakyDirection, nbttagcompound)
    }

    override fun hasGenericInterests(): Boolean = false

    override fun sinksItem(stack: ItemIdentifier?, bestPriority: Int, bestCustomPriority: Int, allowDefault: Boolean, includeInTransit: Boolean, forcePassive: Boolean): SinkReply? = null

    override fun interestedInUndamagedID(): Boolean = false

    override fun interestedInAttachedInventory(): Boolean = true

    override fun getClientInformation(): MutableList<String> = mutableListOf("Extraction: ${_sneakyDirection?.name ?: "DEFAULT"}")

    override fun stopHUDWatching() {
        MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket::class.java).setModulePos(this))
    }

    override fun getHUDRenderer(): IHUDModuleRenderer = hudRenderer

    override fun startHUDWatching() {
        MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket::class.java).setModulePos(this))
    }

    override fun startWatching(player: EntityPlayer?) {
        Objects.requireNonNull(player, "player must not be null")
        localModeWatchers.add(player)
        MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SneakyModuleDirectionUpdate::class.java).setDirection(_sneakyDirection).setModulePos(this), player)
    }

    override fun stopWatching(player: EntityPlayer?) {
        Objects.requireNonNull(player, "player must not be null")
        if (localModeWatchers.contains(player)) localModeWatchers.remove(player)
    }

    class HUDAsyncExtractor(private val module: AsyncExtractorModule) : IHUDModuleRenderer {
        override fun renderContent(shifted: Boolean) {
            val mc = FMLClientHandler.instance().client

            val d: EnumFacing? = module.sneakyDirection
            mc.fontRenderer.drawString("Extract", -22, -22, 0)
            mc.fontRenderer.drawString("from:", -22, -9, 0)
            mc.fontRenderer.drawString(d?.name ?: "DEFAULT", -22, 18, 0)
        }

        override fun getButtons(): MutableList<IHUDButton>? = null

    }

}