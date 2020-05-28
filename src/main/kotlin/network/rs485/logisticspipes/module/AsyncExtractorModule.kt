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
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.Channel
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
import logisticspipes.routing.AsyncRouting
import logisticspipes.routing.ServerRouter
import logisticspipes.utils.PlayerCollectionList
import logisticspipes.utils.SinkReply
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.client.FMLClientHandler
import network.rs485.grow.ChunkedChannel
import network.rs485.grow.takeWhileTimeRemains
import network.rs485.logisticspipes.util.equalsWithNBT
import network.rs485.logisticspipes.util.getExtractionMax
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow

data class ExtractorAsyncResult(val slot: Int, val itemid: ItemIdentifier, val destRouterId: Int, val sinkReply: SinkReply)

class AsyncExtractorModule : AsyncModule<Channel<Pair<Int, ItemStack>>?, List<ExtractorAsyncResult>?>(), Gui, SneakyDirection, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {
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

    private val stacksToExtract: Int
        get() = 1 + (upgradeManager?.itemStackExtractionUpgrade ?: 0)
    private val itemsToExtract: Int
        get() = upgradeManager?.let { 2.0.pow(it.itemExtractionUpgrade) }?.toInt() ?: 0
    private val energyPerItem: Int
        get() = upgradeManager?.let { 5 * 1.1.pow(it.itemExtractionUpgrade) * 1.2.pow(it.itemStackExtractionUpgrade) }?.toInt() ?: 5
    private val itemSendMode: CoreRoutedPipe.ItemSendMode
        get() = upgradeManager?.let { um -> CoreRoutedPipe.ItemSendMode.Fast.takeIf { um.itemExtractionUpgrade > 0 } } ?: CoreRoutedPipe.ItemSendMode.Normal
    override val everyNthTick: Int
        get() = (80 / (upgradeManager?.let { 2.0.pow(it.actionSpeedUpgrade) } ?: 1.0)).toInt().coerceAtLeast(2)

    @ExperimentalCoroutinesApi
    override fun tickSetup(): Channel<Pair<Int, ItemStack>>? {
        val direction = sneakyDirection ?: _service.pointedOrientation?.opposite ?: return null
        val directedInventoryUtil = _service.getSneakyInventory(direction) ?: return null
        if (directedInventoryUtil.sizeInventory == 0) return null
        val limit = TimeUnit.MICROSECONDS.toNanos(50)
        var stacksLeft = stacksToExtract
        var itemsLeft = itemsToExtract

        class ChunkedItemChannel(private var currentSlot: Int = 0) :
                ChunkedChannel<Pair<Int, ItemStack>>(Channel(Channel.UNLIMITED)) {

            override fun hasWork(): Boolean = !channel.isClosedForReceive && stacksLeft > 0 && itemsLeft > 0 && currentSlot < directedInventoryUtil.sizeInventory
            override fun rerun(r: Runnable) = appendSyncWork(r)

            override fun sequenceFactory(): Sequence<Pair<Int, ItemStack>> {
                val start = System.nanoTime()
                return (currentSlot until directedInventoryUtil.sizeInventory).asSequence()
                        .constrainOnce()
                        .takeWhileTimeRemains(start, limit)
                        .map { slot ->
                            val stack = directedInventoryUtil.getStackInSlot(slot)
                            itemsLeft -= stack.count.also { if (it > 0) --stacksLeft }
                            // stores the next slot for continuing this sequence at a later time
                            currentSlot = slot + 1
                            (slot to stack).takeUnless { stack.isEmpty }
                        }.filterNotNull()
            }
        }

        val chunkedChannel = ChunkedItemChannel()
        chunkedChannel.run()
        return chunkedChannel.channel
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override suspend fun tickAsync(setupObject: Channel<Pair<Int, ItemStack>>?): List<ExtractorAsyncResult>? {
        setupObject ?: return null
        var itemsLeft = itemsToExtract
        val jamList = LinkedList<Int>()
        val serverRouter = this._service.router as? ServerRouter ?: error("Router was not set or not a ServerRouter")
        AsyncRouting.updateRoutingTable(serverRouter)
        return setupObject.consumeAsFlow().flatMapConcat { pair ->
            flow<ExtractorAsyncResult> {
                if (itemsLeft > 0) {
                    var stackLeft = pair.second.count
                    val itemid = ItemIdentifier.get(pair.second)
                    emitAll(AsyncLogisticsManager.allDestinations(itemid, true, serverRouter, jamList) { itemsLeft > 0 && stackLeft > 0 }
                            .map { reply ->
                                jamList.add(reply.first)
                                val maxExtraction = getExtractionMax(itemsLeft, stackLeft, reply.second)
                                stackLeft -= maxExtraction
                                itemsLeft -= maxExtraction
                                ExtractorAsyncResult(pair.first, itemid, reply.first, reply.second)
                            }.asFlow())
                }
            }
        }.toList()
    }

    @ExperimentalCoroutinesApi
    override fun completeTick(task: Deferred<List<ExtractorAsyncResult>?>) {
        // always get result, fast exit if it is null or throw on error
        val result = task.getCompleted() ?: return
        val direction = sneakyDirection ?: _service.pointedOrientation?.opposite ?: return
        val directedInventoryUtil = _service.getSneakyInventory(direction) ?: return
        var itemsLeft = itemsToExtract
        result.takeWhile { itemsLeft > 0 }
                .filter { it.slot < directedInventoryUtil.sizeInventory }
                .map { directedInventoryUtil.getStackInSlot(it.slot) to it }
                .filter { it.second.itemid.equalsWithNBT(it.first) }
                .forEach { stackToResult ->
                    var extract = getExtractionMax(itemsLeft, stackToResult.first.count, stackToResult.second.sinkReply)
                    if (extract < 1) return@forEach
                    while (!_service.useEnergy(energyPerItem * extract)) {
                        _service.spawnParticle(Particles.OrangeParticle, 2)
                        if (extract < 2) return@forEach
                        extract /= 2
                    }
                    val toSend = directedInventoryUtil.decrStackSize(stackToResult.second.slot, extract).takeUnless { it.isEmpty } ?: return@forEach
                    _service.sendStack(toSend, stackToResult.second.destRouterId, stackToResult.second.sinkReply, itemSendMode)
                    itemsLeft -= toSend.count
                }
    }

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
