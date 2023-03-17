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
import logisticspipes.interfaces.IClientInformationProvider
import logisticspipes.interfaces.IModuleWatchReciver
import logisticspipes.interfaces.IPipeServiceProvider
import logisticspipes.interfaces.IWorldProvider
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider
import logisticspipes.proxy.MainProxy
import logisticspipes.utils.PlayerCollectionList
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import network.rs485.logisticspipes.property.Property

class AsyncComputerQuicksort : AsyncModule<Pair<Int, ItemStack>?, QuicksortAsyncResult?>(), Gui,
    IClientInformationProvider, IModuleWatchReciver {

    companion object {
        @JvmStatic
        val name: String = "quick_sort_cc"
    }

    private val quicksort = AsyncQuicksortModule()
    private val localModeWatchers = PlayerCollectionList()

    override val properties: List<Property<*>>
        get() = quicksort.properties

    private var _timeout: Int = 100
    var timeout: Int
        get() = _timeout
        set(value) {
            _timeout = if (value == 0) 100 else value
            MainProxy.runOnServer(world) {
                Runnable {
//                    MainProxy.sendToPlayerList(PacketHandler.getPacket(CCBasedQuickSortMode::class.java).setTimeOut(timeout).setModulePos(this), localModeWatchers)
                }
            }
        }

    private var _sinkSize: Int = 0
    var sinkSize: Int
        get() = _sinkSize
        set(value) {
            _sinkSize = value
            MainProxy.runOnServer(world) {
                Runnable {
//                    MainProxy.sendToPlayerList(PacketHandler.getPacket(CCBasedQuickSortSinkSize::class.java).setSinkSize(sinkSize).setModulePos(this), localModeWatchers)
                }
            }
        }

    override val module = this
    override val pipeGuiProvider: ModuleCoordinatesGuiProvider
        get() = TODO("Reimplement CCBasedQuickSortSlot")
    override val inHandGuiProvider: ModuleInHandGuiProvider
        get() = TODO("Reimplement CCBasedQuickSortInHand")

    init {
        TODO("Class still needs to be implemented")
    }

    override fun getLPName(): String = name

    override fun registerHandler(world: IWorldProvider?, service: IPipeServiceProvider?) {
        super.registerHandler(world, service)
        quicksort.registerHandler(world, service)
    }

    override fun registerPosition(slot: ModulePositionType, positionInt: Int) {
        super.registerPosition(slot, positionInt)
        quicksort.registerPosition(slot, positionInt)
    }

    override fun tickSetup(): Pair<Int, ItemStack>? = quicksort.tickSetup()

    override suspend fun tickAsync(setupObject: Pair<Int, ItemStack>?): QuicksortAsyncResult? =
        quicksort.tickAsync(setupObject)

    @ExperimentalCoroutinesApi
    override fun completeTick(task: Deferred<QuicksortAsyncResult?>) = quicksort.completeTick(task)

    override fun runSyncWork() = quicksort.runSyncWork()

    override fun readFromNBT(nbttagcompound: NBTTagCompound) {
        quicksort.readFromNBT(nbttagcompound)
        timeout = nbttagcompound.getInteger("Timeout")
    }

    override fun writeToNBT(nbttagcompound: NBTTagCompound) {
        quicksort.writeToNBT(nbttagcompound)
        nbttagcompound.setInteger("Timeout", timeout)
    }

    override fun recievePassive(): Boolean = false

    override fun hasGenericInterests(): Boolean = false

    override fun interestedInUndamagedID(): Boolean = false

    override fun interestedInAttachedInventory(): Boolean = false

    override fun getClientInformation(): MutableList<String> = mutableListOf("Timeout: $timeout")

    override fun startWatching(player: EntityPlayer?) {
        localModeWatchers.add(player)
//        MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CCBasedQuickSortMode::class.java).setTimeOut(timeout).setModulePos(this), player)
//        MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CCBasedQuickSortSinkSize::class.java).setSinkSize(sinkSize).setModulePos(this), player)
    }

    override fun stopWatching(player: EntityPlayer?) {
        localModeWatchers.remove(player)
    }

}
