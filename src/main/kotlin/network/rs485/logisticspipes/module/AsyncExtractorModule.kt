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

import network.rs485.logisticspipes.logistics.LogisticsManager
import network.rs485.logisticspipes.property.NullableEnumProperty
import network.rs485.logisticspipes.property.Property
import network.rs485.logisticspipes.util.equalsWithNBT
import network.rs485.logisticspipes.util.getExtractionMax
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
import logisticspipes.routing.AsyncRouting.needsRoutingTableUpdate
import logisticspipes.routing.AsyncRouting.updateServerRouterLsa
import logisticspipes.routing.ServerRouter
import logisticspipes.utils.PlayerCollectionList
import logisticspipes.utils.item.ItemIdentifier
import logisticspipes.utils.item.ItemIdentifierStack
import net.minecraftforge.fml.client.FMLClientHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import java.util.*
import kotlin.math.min
import kotlin.math.pow
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow

class ExtractorJob(private val module: AsyncExtractorModule, private val inventoryGetter: () -> IInventoryUtil?) {
    private var inventorySize = inventoryGetter()?.sizeInventory ?: 0
    private val slotsPerTick: Int = determineSlotsPerTick(module.everyNthTick, inventorySize)
    private val slotStartIter =
        if (slotsPerTick == 0) emptyList<Int>().iterator()
        else IntProgression.fromClosedRange(0, inventorySize - 1, slotsPerTick).iterator()
    private var itemsLeft = module.itemsToExtract
    private var stacksLeft = module.stacksToExtract
    private val updateRoutingTableMsgChannel: Channel<Unit> = Channel(Channel.CONFLATED)
    private val slotItemsToExtract: MutableMap<Int, ItemIdentifierStack> = HashMap()

    fun runSyncWork() {
        try {
            val serverRouter = module.serverRouter
            val inventory = inventoryGetter()
            if (slotsPerTick == 0 || !slotStartIter.hasNext() || serverRouter == null || inventory == null) {
                updateRoutingTableMsgChannel.close()
                return
            }
            inventorySize = inventory.sizeInventory
            val startSlot = slotStartIter.next()
            val stopSlot = if (slotStartIter.hasNext()) {
                min(inventorySize, startSlot + slotsPerTick)
            } else inventorySize

            val slotRange = startSlot until stopSlot
            for (slot in slotRange) {
                val stack = inventory.getStackInSlot(slot)
                if (module.inverseFilter(stack)) continue // filters the stack out by the given filter method
                val toExtract = min(itemsLeft, stack.count)
                itemsLeft -= toExtract
                --stacksLeft
                slotItemsToExtract[slot] = ItemIdentifierStack(ItemIdentifier.get(stack), toExtract)
                if (itemsLeft < 1) break
                if (stacksLeft < 1) break
            }
            serverRouter.updateServerRouterLsa()
            if (serverRouter.needsRoutingTableUpdate()) {
                updateRoutingTableMsgChannel.trySend(Unit)
            } else {
                extractAndSend(serverRouter, inventory)
            }
            if (!slotStartIter.hasNext()) {
                updateRoutingTableMsgChannel.close()
            }
        } catch (error: Exception) {
            updateRoutingTableMsgChannel.close(error)
            throw error
        }
    }

    suspend fun runAsyncWork() {
        if (!Configs.DISABLE_ASYNC_WORK) {
            updateRoutingTableMsgChannel.consumeAsFlow().collect {
                module.serverRouter?.also { serverRouter ->
                    AsyncRouting.updateRoutingTable(serverRouter)
                }
            }
        }
    }

    fun extractAndSend(serverRouter: ServerRouter, inventory: IInventoryUtil) {
        slotItemsToExtract.forEach { (slot, itemIdStack) ->
            extractAndSendStack(serverRouter, inventory, slot, itemIdStack)
        }
        slotItemsToExtract.clear()
    }

    private fun extractAndSendStack(
        serverRouter: ServerRouter,
        inventory: IInventoryUtil,
        slot: Int,
        itemIdStack: ItemIdentifierStack,
    ) {
        val service = module.service ?: return
        val pointedOrientation = service.pointedOrientation ?: return
        val stack = inventory.getStackInSlot(slot)
        if (!itemIdStack.item.equalsWithNBT(stack)) return
        var sourceStackLeft = itemIdStack.stackSize
        val validDestinationSequence = LogisticsManager.allDestinations(
            stack = stack,
            itemid = ItemIdentifier.get(stack),
            canBeDefault = true,
            sourceRouter = serverRouter,
        ) { sourceStackLeft > 0 }
        validDestinationSequence.forEach { (destRouterId, sinkReply) ->
            var extract = getExtractionMax(stack.count, sourceStackLeft, sinkReply)
            if (extract < 1) return@forEach
            while (!service.useEnergy(module.energyPerItem * extract)) {
                service.spawnParticle(Particles.OrangeParticle, 2)
                if (extract < 2) break
                extract /= 2
            }
            val toSend = inventory.decrStackSize(slot, extract)
            if (toSend.isEmpty) return@forEach
            service.sendStack(toSend, destRouterId, sinkReply, module.itemSendMode, pointedOrientation)
            sourceStackLeft -= toSend.count
        }
    }
}

class AsyncExtractorModule(
    val inverseFilter: (ItemStack) -> Boolean = { stack -> stack.isEmpty },
) : AsyncModule<ExtractorJob, Unit>(), Gui, SneakyDirection,
    IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {

    companion object {
        @JvmStatic
        val name: String = "extractor"
    }

    private val sneakyDirectionProp = NullableEnumProperty(null, "sneakydirection", EnumFacing.values())

    override val properties: List<Property<*>>
        get() = listOf(sneakyDirectionProp)

    override var sneakyDirection: EnumFacing?
        get() = sneakyDirectionProp.value
        set(value) {
            sneakyDirectionProp.value = value
            MainProxy.sendToPlayerList(
                PacketHandler.getPacket(SneakyModuleDirectionUpdate::class.java)
                    .setDirection(sneakyDirection)
                    .setModulePos(this),
                localModeWatchers,
            )
        }

    private val hudRenderer: IHUDModuleRenderer = HUDAsyncExtractor(this)
    val localModeWatchers = PlayerCollectionList()
    override val module = this
    private var currentJob: ExtractorJob? = null

    internal val serverRouter: ServerRouter?
        get() = _service?.router as? ServerRouter

    internal val service: IPipeServiceProvider?
        get() = _service

    override val pipeGuiProvider: ModuleCoordinatesGuiProvider
        get() =
            NewGuiHandler.getGui(SneakyModuleInSlotGuiProvider::class.java).setSneakyOrientation(sneakyDirection)

    override val inHandGuiProvider: ModuleInHandGuiProvider
        get() =
            NewGuiHandler.getGui(SneakyModuleInHandGuiProvider::class.java)

    override val everyNthTick: Int
        get() = (80 / upgradeManager.let { 2.0.pow(it.actionSpeedUpgrade) }).toInt() + Configs.MINIMUM_JOB_TICK_LENGTH

    val stacksToExtract: Int
        get() = 1 + upgradeManager.itemStackExtractionUpgrade

    val itemsToExtract: Int
        get() = upgradeManager.let { 4 * it.itemExtractionUpgrade + 64 * upgradeManager.itemStackExtractionUpgrade }
            .coerceAtLeast(1)

    internal val energyPerItem: Int
        get() = upgradeManager.let {
            5 * 1.1.pow(it.itemExtractionUpgrade) * 1.2.pow(it.itemStackExtractionUpgrade)
        }.toInt()

    internal val itemSendMode: CoreRoutedPipe.ItemSendMode
        get() = upgradeManager.let { um ->
            CoreRoutedPipe.ItemSendMode.Fast.takeIf { um.itemExtractionUpgrade > 0 }
        } ?: CoreRoutedPipe.ItemSendMode.Normal

    private val connectedInventory: IInventoryUtil?
        get() = _service?.availableSneakyInventories(sneakyDirection)?.firstOrNull()

    override fun getLPName(): String = name

    override fun jobSetup(): ExtractorJob = ExtractorJob(this) { connectedInventory }.also {
        currentJob = it
        it.runSyncWork()
    }

    override fun runSyncWork() {
        currentJob?.runSyncWork()
    }

    override suspend fun tickAsync(setupObject: ExtractorJob) {
        setupObject.runAsyncWork()
    }

    override fun completeJob(deferred: Deferred<Unit?>) {
        val serverRouter = module.serverRouter ?: return
        val inventory = connectedInventory ?: return
        serverRouter.ensureLatestRoutingTable()
        currentJob?.extractAndSend(serverRouter, inventory)
    }

    override fun receivePassive(): Boolean = false

    override fun hasGenericInterests(): Boolean = false

    override fun interestedInUndamagedID(): Boolean = false

    override fun interestedInAttachedInventory(): Boolean = true

    override fun getClientInformation(): MutableList<String> =
        mutableListOf("Extraction: ${sneakyDirection?.name ?: "DEFAULT"}")

    override fun stopHUDWatching() {
        MainProxy.sendPacketToServer(
            PacketHandler.getPacket(HUDStopModuleWatchingPacket::class.java).setModulePos(this),
        )
    }

    override fun getHUDRenderer(): IHUDModuleRenderer = hudRenderer

    override fun startHUDWatching() {
        MainProxy.sendPacketToServer(
            PacketHandler.getPacket(HUDStartModuleWatchingPacket::class.java).setModulePos(this),
        )
    }

    override fun startWatching(player: EntityPlayer?) {
        Objects.requireNonNull(player, "player must not be null")
        localModeWatchers.add(player)
        MainProxy.sendPacketToPlayer(
            PacketHandler.getPacket(SneakyModuleDirectionUpdate::class.java).setDirection(sneakyDirection)
                .setModulePos(this),
            player,
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
