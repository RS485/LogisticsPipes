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
import logisticspipes.logistics.AsyncLogisticsManager
import logisticspipes.pipefxhandlers.Particles
import logisticspipes.pipes.basic.CoreRoutedPipe
import logisticspipes.routing.ServerRouter
import logisticspipes.utils.SinkReply
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import network.rs485.logisticspipes.util.getExtractionMax
import java.util.*

const val MAX_EXTRACT = 64
const val STALLED_DELAY = 24
const val NORMAL_DELAY = 6

data class QuicksortAsyncResult(val slot: Int, val itemid: ItemIdentifier, val destRouterId: Int, val sinkReply: SinkReply)

class AsyncQuicksortModule : AsyncModule<Pair<Int, ItemStack>?, QuicksortAsyncResult?>() {
    private var stalled = true
    private var currentSlot = 0
    private var stallSlot = 0

    private val stacksToExtract: Int
        get() = 1 + (upgradeManager?.itemStackExtractionUpgrade ?: 0)
    private val energyPerStack: Int
        get() = upgradeManager?.let { 500 + 1000 * it.itemStackExtractionUpgrade }?.toInt() ?: 5
    override val everyNthTick: Int
        get() = if (stalled) STALLED_DELAY else NORMAL_DELAY

    override fun tickSetup(): Pair<Int, ItemStack>? {
        val inventory = _service.pointedInventory ?: return null
        if (inventory.sizeInventory == 0) return null
        if (currentSlot >= inventory.sizeInventory) currentSlot = 0
        val stack = inventory.getStackInSlot(currentSlot)
        ++currentSlot
        if (!stalled && currentSlot == stallSlot) stalled = true
        if (stack.isEmpty) return null
        return currentSlot to stack
    }

    override suspend fun tickAsync(setupObject: Pair<Int, ItemStack>?): QuicksortAsyncResult? {
        if (setupObject == null) return null
        val itemid = ItemIdentifier.get(setupObject.second)
        val jamList = LinkedList<Int>()
        val result = AsyncLogisticsManager.getDestination(itemid, false, _service.router as ServerRouter, jamList) ?: return null
        return QuicksortAsyncResult(setupObject.first, itemid, result.first, result.second)
    }

    @ExperimentalCoroutinesApi
    override fun completeTick(task: Deferred<QuicksortAsyncResult?>) {
        val result = task.getCompleted() ?: return
        val inventory = _service.pointedInventory ?: return
        if (result.slot >= inventory.sizeInventory) return
        val stack = inventory.getStackInSlot(result.slot)
        val toExtract = getExtractionMax(stack.count, MAX_EXTRACT, result.sinkReply)
        if (toExtract <= 0) return
        if (!_service.useEnergy(energyPerStack)) return
        stalled = false
        stallSlot = result.slot
        val extracted = inventory.decrStackSize(result.slot, toExtract)
        if (extracted.isEmpty) return
        _service.sendStack(extracted, result.destRouterId, result.sinkReply, CoreRoutedPipe.ItemSendMode.Fast)
        _service.spawnParticle(Particles.OrangeParticle, 8)
    }

    override fun recievePassive(): Boolean = false

    override fun readFromNBT(nbttagcompound: NBTTagCompound) {}

    override fun writeToNBT(nbttagcompound: NBTTagCompound) {}

    override fun hasGenericInterests(): Boolean = false

    override fun sinksItem(stack: ItemIdentifier?, bestPriority: Int, bestCustomPriority: Int, allowDefault: Boolean, includeInTransit: Boolean, forcePassive: Boolean): SinkReply? = null

    override fun interestedInUndamagedID(): Boolean = false

    override fun interestedInAttachedInventory(): Boolean = false

}
