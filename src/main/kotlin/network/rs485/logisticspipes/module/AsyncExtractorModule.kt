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
import kotlinx.coroutines.flow.*
import logisticspipes.config.Configs
import logisticspipes.interfaces.*
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
import network.rs485.logisticspipes.logistics.LogisticsManager
import network.rs485.logisticspipes.util.equalsWithNBT
import network.rs485.logisticspipes.util.getExtractionMax
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.pow

data class ExtractorAsyncResult(
    val slot: Int,
    val itemid: ItemIdentifier,
    val destRouterId: Int,
    val sinkReply: SinkReply,
)

class AsyncExtractorModule(
    val inverseFilter: (ItemStack) -> Boolean = { stack -> stack.isEmpty },
) : AsyncModule<Channel<Pair<Int, ItemStack>>?, List<ExtractorAsyncResult>?>(), Gui, SneakyDirection,
    IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {

    companion object {
        @JvmStatic
        val name: String = "extractor"
    }

    private var nextChannelWork: (() -> Unit)? = null
    private val hudRenderer: IHUDModuleRenderer = HUDAsyncExtractor(this)
    private var _sneakyDirection: EnumFacing? = null
    private var currentSlot = 0
    val localModeWatchers = PlayerCollectionList()

    override var sneakyDirection: EnumFacing?
        get() = _sneakyDirection
        set(value) {
            _sneakyDirection = value
            MainProxy.sendToPlayerList(
                PacketHandler.getPacket(SneakyModuleDirectionUpdate::class.java).setDirection(_sneakyDirection)
                    .setModulePos(this), localModeWatchers
            )
        }

    override val module = this
    override val pipeGuiProvider: ModuleCoordinatesGuiProvider =
        NewGuiHandler.getGui(SneakyModuleInSlotGuiProvider::class.java).setSneakyOrientation(_sneakyDirection)
    override val inHandGuiProvider: ModuleInHandGuiProvider =
        NewGuiHandler.getGui(SneakyModuleInHandGuiProvider::class.java)
    override val everyNthTick: Int
        get() = (80 / upgradeManager.let { 2.0.pow(it.actionSpeedUpgrade) }).toInt().coerceAtLeast(2)

    private val stacksToExtract: Int
        get() = 1 + upgradeManager.itemStackExtractionUpgrade
    private val itemsToExtract: Int
        get() = upgradeManager.let { 2.0.pow(it.itemExtractionUpgrade) }.toInt()
    private val energyPerItem: Int
        get() = upgradeManager.let { 5 * 1.1.pow(it.itemExtractionUpgrade) * 1.2.pow(it.itemStackExtractionUpgrade) }
            .toInt()
    private val itemSendMode: CoreRoutedPipe.ItemSendMode
        get() = upgradeManager.let { um -> CoreRoutedPipe.ItemSendMode.Fast.takeIf { um.itemExtractionUpgrade > 0 } }
            ?: CoreRoutedPipe.ItemSendMode.Normal
    private val connectedInventory: IInventoryUtil?
        get() = _service?.availableSneakyInventories(sneakyDirection)?.firstOrNull()

    @ExperimentalCoroutinesApi
    override fun tickSetup(): Channel<Pair<Int, ItemStack>>? =
        // run item sequence as far as possible and return the channel
        connectedInventory?.let { ChunkedItemChannel(::connectedInventory).also(::channelWork).channel }

    private fun <T, O> channelWork(chunkedChannel: ChunkedChannel<T, O>) {
        if (chunkedChannel.get()) {
            // if the chunked channel asks for a re-run
            nextChannelWork = { channelWork(chunkedChannel) }
        }
    }

    override fun runSyncWork() {
        val task = nextChannelWork
        nextChannelWork = null
        task?.invoke()
    }

    private data class InventorySession(val serverRouter: ServerRouter, val inventory: IInventoryUtil)

    private inner class ChunkedItemChannel(private val inventoryGetter: () -> IInventoryUtil?) :
        ChunkedChannel<Pair<Int, ItemStack>, InventorySession?>(Channel(Channel.UNLIMITED)) {
        private var hasNext: Boolean = true
        private val timeLimit = TimeUnit.MILLISECONDS.toNanos(1)
        private val lastIndex = currentSlot - 1
        private var itemsLeft = itemsToExtract
        private var stacksLeft = stacksToExtract

        private fun sloterator(session: InventorySession) = sloterator(
            current = currentSlot,
            last = lastIndex,
            size = session.inventory.sizeInventory
        )

        override fun newSession(): InventorySession? = (_service?.router as? ServerRouter)?.let { serverRouter ->
            inventoryGetter()?.let { inventory ->
                InventorySession(serverRouter, inventory)
            }
        }

        @ExperimentalCoroutinesApi
        override fun hasWork(session: InventorySession?): Boolean = (
                hasNext
                        && !channel.isClosedForReceive
                        && stacksLeft > 0
                        && itemsLeft > 0
                        && session != null
                        && sloterator(session).any()
                )

        @ExperimentalCoroutinesApi
        override fun nextChunk(session: InventorySession?): List<Pair<Int, ItemStack>> {
            if (!hasWork(session)) return emptyList()
            session!! // as checked in hasWork

            return if (ServerRouter.getBiggestSimpleID() > Configs.ASYNC_THRESHOLD
                || AsyncRouting.routingTableNeedsUpdate(session.serverRouter)
            ) {
                // fall through to async
                val list: ArrayList<Pair<Int, ItemStack>> = ArrayList()
                iterateSlots(session) { slot, stack -> list.add(slot to stack) }
                list
            } else {
                iterateSlots(session) { slot, stack -> sendNow(stack, session, slot) }
                emptyList()
            }
        }

        private fun iterateSlots(
            session: InventorySession,
            slotStackConsumer: (slot: Int, stack: ItemStack) -> Unit,
        ) {
            val startTime = System.nanoTime()
            for (slot in sloterator(session)) {
                if (System.nanoTime() - startTime > timeLimit || stacksLeft < 1) break
                currentSlot = slot
                val stack = session.inventory.getStackInSlot(slot)
                if (inverseFilter(stack)) continue // filters the stack out by the given filter method
                slotStackConsumer.invoke(slot, stack)
                --stacksLeft
                // reset item count for next stack
                itemsLeft = itemsToExtract
            }
            // we're through all available slots
            hasNext = false
        }

        private fun sendNow(stack: ItemStack, session: InventorySession, slot: Int) {
            // find destinations and send stacks now
            var sourceStackLeft = stack.count
            LogisticsManager.allDestinations(
                stack,
                ItemIdentifier.get(stack),
                true,
                session.serverRouter
            ) { itemsLeft > 0 && sourceStackLeft > 0 }
                .forEach { pair ->
                    extractAndSend(
                        slot,
                        sourceStackLeft,
                        session.inventory,
                        pair.first,
                        pair.second,
                        itemsLeft
                    ).also {
                        itemsLeft -= it
                        sourceStackLeft -= it
                    }
                }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override suspend fun tickAsync(setupObject: Channel<Pair<Int, ItemStack>>?): List<ExtractorAsyncResult>? {
        setupObject ?: return null
        var itemsLeft = itemsToExtract
        return setupObject.consumeAsFlow().flatMapConcat { pair ->
            if (itemsLeft <= 0) return@flatMapConcat emptyFlow<ExtractorAsyncResult>()
            val serverRouter = this._service?.router as? ServerRouter
                ?: return@flatMapConcat emptyFlow<ExtractorAsyncResult>()
            var stackLeft = pair.second.count
            val itemid = ItemIdentifier.get(pair.second)
            AsyncRouting.updateRoutingTable(serverRouter)
            LogisticsManager.allDestinations(pair.second, itemid, true, serverRouter) { itemsLeft > 0 && stackLeft > 0 }
                .map { reply ->
                    val maxExtraction = getExtractionMax(stackLeft, itemsLeft, reply.second)
                    stackLeft -= maxExtraction
                    itemsLeft -= maxExtraction
                    ExtractorAsyncResult(pair.first, itemid, reply.first, reply.second)
                }.asFlow()
        }.toList()
    }

    @ExperimentalCoroutinesApi
    override fun completeTick(task: Deferred<List<ExtractorAsyncResult>?>) {
        // always get result, fast exit if it is null or throw on error
        val result = task.getCompleted() ?: return
        val inventory = connectedInventory ?: return
        var itemsLeft = itemsToExtract
        result.filter { it.slot < inventory.sizeInventory }
            .asSequence()
            .takeWhile { itemsLeft > 0 }
            .forEach {
                val stack = inventory.getStackInSlot(it.slot)
                if (it.itemid.equalsWithNBT(stack)) {
                    itemsLeft -= extractAndSend(
                        it.slot,
                        stack.count,
                        inventory,
                        it.destRouterId,
                        it.sinkReply,
                        itemsLeft
                    )
                }
            }
    }

    private fun extractAndSend(
        slot: Int,
        count: Int,
        inventory: IInventoryUtil,
        destRouterId: Int,
        sinkReply: SinkReply,
        itemsLeft: Int,
    ): Int {
        val service = this._service ?: return 0
        var extract = getExtractionMax(count, itemsLeft, sinkReply)
        if (extract < 1) return 0
        while (!service.useEnergy(energyPerItem * extract)) {
            service.spawnParticle(Particles.OrangeParticle, 2)
            if (extract < 2) return 0
            extract /= 2
        }
        val toSend = inventory.decrStackSize(slot, extract)
        if (toSend.isEmpty) return 0
        service.sendStack(toSend, destRouterId, sinkReply, itemSendMode, service.pointedOrientation)
        return toSend.count
    }

    override fun recievePassive(): Boolean = false

    override fun readFromNBT(nbttagcompound: NBTTagCompound) {
        _sneakyDirection = SneakyDirection.readSneakyDirection(nbttagcompound)
    }

    override fun writeToNBT(nbttagcompound: NBTTagCompound) {
        SneakyDirection.writeSneakyDirection(_sneakyDirection, nbttagcompound)
    }

    override fun hasGenericInterests(): Boolean = false

    override fun interestedInUndamagedID(): Boolean = false

    override fun interestedInAttachedInventory(): Boolean = true

    override fun getClientInformation(): MutableList<String> =
        mutableListOf("Extraction: ${_sneakyDirection?.name ?: "DEFAULT"}")

    override fun stopHUDWatching() {
        MainProxy.sendPacketToServer(
            PacketHandler.getPacket(HUDStopModuleWatchingPacket::class.java).setModulePos(this)
        )
    }

    override fun getHUDRenderer(): IHUDModuleRenderer = hudRenderer

    override fun startHUDWatching() {
        MainProxy.sendPacketToServer(
            PacketHandler.getPacket(HUDStartModuleWatchingPacket::class.java).setModulePos(this)
        )
    }

    override fun startWatching(player: EntityPlayer?) {
        Objects.requireNonNull(player, "player must not be null")
        localModeWatchers.add(player)
        MainProxy.sendPacketToPlayer(
            PacketHandler.getPacket(SneakyModuleDirectionUpdate::class.java).setDirection(_sneakyDirection)
                .setModulePos(this), player
        )
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
