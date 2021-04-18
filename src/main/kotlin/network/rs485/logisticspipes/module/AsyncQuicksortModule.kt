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
import logisticspipes.config.Configs.ASYNC_THRESHOLD
import logisticspipes.interfaces.IInventoryUtil
import logisticspipes.network.PacketHandler
import logisticspipes.network.packets.modules.QuickSortState
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
import network.rs485.logisticspipes.logistics.LogisticsManager
import network.rs485.logisticspipes.property.Property
import network.rs485.logisticspipes.util.equalsWithNBT
import network.rs485.logisticspipes.util.getExtractionMax

const val STALLED_DELAY = 24
const val NORMAL_DELAY = 6

data class QuicksortAsyncResult(
    val slot: Int,
    val itemid: ItemIdentifier,
    val destRouterId: Int,
    val sinkReply: SinkReply,
)

class AsyncQuicksortModule : AsyncModule<Pair<Int, ItemStack>?, QuicksortAsyncResult?>() {
    companion object {
        @JvmStatic
        val name: String = "quick_sort"
    }

    private val localSlotWatchers = PlayerCollectionList()
    private var stalled = true
    private var currentSlot = 0
        set(value) {
            field = value
            MainProxy.sendToPlayerList(PacketHandler.getPacket(QuickSortState::class.java).setInteger(value)
                .setModulePos(this), localSlotWatchers)
        }
    private var stallSlot = 0

    override val properties: List<Property<*>>
        get() = emptyList()

    private val stacksToExtract: Int
        get() = 1 + upgradeManager.itemStackExtractionUpgrade
    private val energyPerStack: Int
        get() = upgradeManager.let { 500 + 1000 * it.itemStackExtractionUpgrade }.toInt()
    override val everyNthTick: Int
        get() = if (stalled) STALLED_DELAY else NORMAL_DELAY

    override fun getLPName(): String = name

    override fun tickSetup(): Pair<Int, ItemStack>? {
        val serverRouter = this._service?.router as? ServerRouter ?: return null
        val inventory = _service?.availableInventories()?.firstOrNull() ?: return null
        if (inventory.sizeInventory == 0) return null
        if (currentSlot >= inventory.sizeInventory) currentSlot = 0
        val slot = currentSlot++
        val stack = inventory.getStackInSlot(slot)
        if (!stalled && slot == stallSlot) stalled = true
        if (stack.isEmpty) return null
        if (ServerRouter.getBiggestSimpleID() > (ASYNC_THRESHOLD * 2) || AsyncRouting.routingTableNeedsUpdate(
                serverRouter)
        ) {
            // go async
            return slot to stack
        }
        val itemid = ItemIdentifier.get(stack)
        val result = LogisticsManager.getDestination(stack, itemid, false, serverRouter, emptyList()) ?: return null
        extractAndSend(slot, stack, inventory, result.first, result.second)
        return null
    }

    override suspend fun tickAsync(setupObject: Pair<Int, ItemStack>?): QuicksortAsyncResult? {
        if (setupObject == null) return null
        val serverRouter = this._service?.router as? ServerRouter ?: return null
        AsyncRouting.updateRoutingTable(serverRouter)
        val itemid = ItemIdentifier.get(setupObject.second)
        val result =
            LogisticsManager.getDestination(setupObject.second, itemid, false, serverRouter, emptyList()) ?: return null
        return QuicksortAsyncResult(setupObject.first, itemid, result.first, result.second)
    }

    @ExperimentalCoroutinesApi
    override fun completeTick(task: Deferred<QuicksortAsyncResult?>) {
        val result = task.getCompleted() ?: return
        val inventory = _service?.availableInventories()?.firstOrNull() ?: return
        if (result.slot >= inventory.sizeInventory) return
        val stack = inventory.getStackInSlot(result.slot)
        if (result.itemid.equalsWithNBT(stack)) {
            extractAndSend(result.slot, stack, inventory, result.destRouterId, result.sinkReply)
        }
    }

    private fun extractAndSend(
        slot: Int,
        stack: ItemStack,
        inventory: IInventoryUtil,
        destRouterId: Int,
        sinkReply: SinkReply,
    ) {
        val service = _service ?: return
        val toExtract = getExtractionMax(stack.count, stack.maxStackSize, sinkReply)
        if (toExtract <= 0) return
        if (!service.useEnergy(energyPerStack)) return
        stalled = false
        stallSlot = slot
        val extracted = inventory.decrStackSize(slot, toExtract)
        if (extracted.isEmpty) return
        service.sendStack(extracted,
            destRouterId,
            sinkReply,
            CoreRoutedPipe.ItemSendMode.Fast,
            service.pointedOrientation)
        service.spawnParticle(Particles.OrangeParticle, 8)
    }

    override fun runSyncWork() {}

    override fun recievePassive(): Boolean = false

    override fun hasGenericInterests(): Boolean = false

    override fun interestedInUndamagedID(): Boolean = false

    override fun interestedInAttachedInventory(): Boolean = false

    fun addWatchingPlayer(player: EntityPlayer) {
        localSlotWatchers.add(player)
        MainProxy.sendPacketToPlayer(PacketHandler.getPacket(QuickSortState::class.java).setInteger(currentSlot)
            .setModulePos(this), player)
    }

    fun removeWatchingPlayer(player: EntityPlayer) {
        localSlotWatchers.remove(player)
    }

}
