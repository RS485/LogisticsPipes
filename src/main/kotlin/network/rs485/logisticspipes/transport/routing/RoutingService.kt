/*
 * Copyright (c) 2019  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019  RS485
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

package network.rs485.logisticspipes.transport.routing

import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.Tag
import network.rs485.logisticspipes.transport.network.PipeHolder
import network.rs485.logisticspipes.transport.network.PipeNetworkImpl
import network.rs485.logisticspipes.transport.network.service.InitializationContext
import network.rs485.logisticspipes.transport.network.service.NetworkService
import network.rs485.logisticspipes.util.DefaultedSerializableKey
import therealfarfetchd.hctm.common.graph.Graph
import therealfarfetchd.hctm.common.graph.Node
import java.util.*

object RoutingService : NetworkService {

    override fun initialize(ctx: InitializationContext) {
        ctx.registerPipeChangeHandler(::onPipeChanged)
    }

    private fun onPipeChanged(net: PipeNetworkImpl, pipe: PipeHolder) {
        updateRoutingTree(net)
    }

    private fun updateRoutingTree(net: PipeNetworkImpl) {
        val data = net[Data]
        val tree = Graph<UUID, Float>()
        for (node in net.graph.nodes) {
            val self = tree.add(node.data.id)
            for (connection in node.connections) {
                val otherPipe = connection.other(node)
                val other = tree.nodes.find { it.data == otherPipe.data.id }
                if (other != null) {
                    val length = node.data.pathHandler.getLength(connection.data(node)) +
                            otherPipe.data.pathHandler.getLength(connection.data(otherPipe))
                    tree.link(self, other, length, length)
                }
            }
        }
        if (tree.split().isNotEmpty()) error("Disconnected nodes found")
        val stack = mutableListOf<Node<UUID, Float>>()

        data.tree = tree
    }

    class Data {

        var tree = Graph<UUID, Float>()

        companion object Key : DefaultedSerializableKey<Data> {

            override fun create() = Data()

            override fun toTag(t: Data): Tag {
                return ByteTag.ZERO
            }

            override fun fromTag(tag: Tag): Data {
                return create()
            }

        }
    }

}