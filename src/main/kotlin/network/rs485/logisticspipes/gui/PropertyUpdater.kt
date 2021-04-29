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

package network.rs485.logisticspipes.gui

import logisticspipes.modules.LogisticsModule
import logisticspipes.network.PacketHandler
import logisticspipes.network.packets.module.ModulePropertiesUpdate
import logisticspipes.proxy.MainProxy
import net.minecraft.entity.player.EntityPlayer
import network.rs485.grow.Coroutines.scheduleServerTask
import network.rs485.logisticspipes.property.Property
import network.rs485.logisticspipes.property.addObserver
import network.rs485.logisticspipes.property.removeObserver
import network.rs485.logisticspipes.property.writeToNBT
import java.lang.ref.WeakReference
import java.util.function.Consumer

class PropertyUpdater(
    player: EntityPlayer,
    moduleIn: LogisticsModule,
    propertiesIn: List<Property<*>>
) : Consumer<Property<*>> {

    private val weakPlayer: WeakReference<EntityPlayer> = WeakReference(player)
    private val properties: List<Property<*>> = propertiesIn.also { it.addObserver(this::accept) }
    private val changedProperties = HashSet<Property<*>>()
    private val module: LogisticsModule = moduleIn
    private var shouldUpdate = false

    override fun accept(property: Property<*>) {
        changedProperties.add(property)
        if (!shouldUpdate) {
            shouldUpdate = true
            scheduleServerTask(5) { sendPropertyUpdate() }
        }
    }

    private fun sendPropertyUpdate() {
        if (shouldUpdate && !weakPlayer.isEnqueued) {
            val player = weakPlayer.get() ?: return
            val packet = PacketHandler.getPacket(ModulePropertiesUpdate::class.java)
            changedProperties.writeToNBT(packet.tag)
            changedProperties.clear()
            MainProxy.sendPacketToPlayer(packet.setModulePos(module), player)
            shouldUpdate = false
        }
    }

    internal fun removeForPlayer(entityPlayer: EntityPlayer): Boolean {
        val shouldBeRemoved = (weakPlayer.isEnqueued
                || weakPlayer.get() == null || weakPlayer.get() === entityPlayer)
        if (shouldBeRemoved) {
            properties.removeObserver(this::accept)
            shouldUpdate = false
        }
        return shouldBeRemoved
    }

}
