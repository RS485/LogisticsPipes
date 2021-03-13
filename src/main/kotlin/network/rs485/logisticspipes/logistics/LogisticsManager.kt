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

package network.rs485.logisticspipes.logistics

import logisticspipes.modules.LogisticsModule
import logisticspipes.pipefxhandlers.Particles
import logisticspipes.proxy.SimpleServiceLocator
import logisticspipes.routing.AsyncRouting
import logisticspipes.routing.ExitRoute
import logisticspipes.routing.PipeRoutingConnectionType
import logisticspipes.routing.ServerRouter
import logisticspipes.utils.SinkReply
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.item.ItemStack
import java.util.*
import java.util.stream.Stream

object LogisticsManager {
    fun allDestinations(stack: ItemStack, itemid: ItemIdentifier, canBeDefault: Boolean, sourceRouter: ServerRouter, filter: () -> Boolean): Sequence<Pair<Int, SinkReply>> {
        val jamList = LinkedList<Int>()
        return generateSequence {
            return@generateSequence if (filter()) getDestination(stack, itemid, canBeDefault, sourceRouter, jamList)?.also { jamList.add(it.first) } else null
        }
    }

    fun getDestination(stack: ItemStack, itemid: ItemIdentifier, canBeDefault: Boolean, sourceRouter: ServerRouter, routersToExclude: List<Int>): Pair<Int, SinkReply>? {
        val destinationStream = ServerRouter.getRoutersInterestedIn(itemid).stream()
                .mapToObj(SimpleServiceLocator.routerManager::getServerRouter)
                .flatMap {
                    it?.let { router ->
                        AsyncRouting.getDistance(sourceRouter, router)?.let { routes ->
                            routes.stream().filter { exitRoute -> exitRoute.containsFlag(PipeRoutingConnectionType.canRouteTo) }
                        }
                    } ?: Stream.empty()
                }
        return getBestReply(stack, itemid, sourceRouter, destinationStream, routersToExclude, canBeDefault)
    }

    private fun getBestReply(stack: ItemStack, itemid: ItemIdentifier, sourceRouter: ServerRouter, destinationStream: Stream<ExitRoute>, routersToExclude: List<Int>, canBeDefault: Boolean): Pair<Int, SinkReply>? {
        var resultRouterId: Int? = null
        var result: SinkReply? = null
        destinationStream.filter {
            it.destination.id != sourceRouter.id &&
                    !routersToExclude.contains(it.destination.simpleID) &&
                    it.containsFlag(PipeRoutingConnectionType.canRouteTo) &&
                    it.filters.none { filter -> filter.blockRouting() || filter.isBlocked == filter.isFilteredItem(itemid) } &&
                    it.destination.logisticsModule != null &&
                    it.destination.logisticsModule.recievePassive() &&
                    it.destination.pipe != null &&
                    it.destination.pipe.isEnabled &&
                    !it.destination.pipe.isOnSameContainer(sourceRouter.pipe)
        }.sorted().forEachOrdered {
            val reply: SinkReply?
            val module: LogisticsModule = it.destination.logisticsModule
            reply = when {
                result == null -> module.sinksItem(stack, itemid, -1, 0, canBeDefault, true, true)
                result!!.maxNumberOfItems < 0 -> null
                else -> module.sinksItem(stack, itemid, result!!.fixedPriority.ordinal, result!!.customPriority, canBeDefault, true, true)
            }

            if (reply != null && (result == null ||
                            reply.fixedPriority.ordinal > result!!.fixedPriority.ordinal ||
                            (reply.fixedPriority == result!!.fixedPriority && reply.customPriority > result!!.customPriority))) {
                resultRouterId = it.destination.simpleID
                result = reply
            }
        }

        return result?.let { sinkReply ->
            resultRouterId?.let { destinationRouterId ->
                val pipe = SimpleServiceLocator.routerManager.getServerRouter(destinationRouterId)!!.pipe!!
                pipe.useEnergy(sinkReply.energyUse)
                pipe.spawnParticle(Particles.BlueParticle, 10)
                Pair(destinationRouterId, sinkReply)
            }
        }
    }

}
