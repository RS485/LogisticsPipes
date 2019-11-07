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

package network.rs485.logisticspipes.transport.network

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import network.rs485.logisticspipes.pipe.shape.BlockFace
import network.rs485.logisticspipes.pipe.shape.PipeShape
import network.rs485.logisticspipes.transport.*
import therealfarfetchd.hctm.common.graph.Graph
import therealfarfetchd.hctm.common.graph.Link
import therealfarfetchd.hctm.common.graph.Node
import java.util.*
import kotlin.math.roundToLong

internal typealias PipeGraph = Graph<PipeHolder<*>, Any?>
internal typealias PipeNode = Node<PipeHolder<*>, Any?>
internal typealias PipeLink = Link<PipeHolder<*>, Any?>

class PipeNetworkImpl(override val world: ServerWorld, override val id: UUID, val controller: PipeNetworkState) : PipeNetwork {

    private val insertTimes = Object2LongOpenHashMap<UUID>()
    private val updateTimes = Object2LongOpenHashMap<UUID>()

    internal val graph = PipeGraph()

    private val cellMap = mutableMapOf<UUID, CellHolder<*>>()

    private val nodesInPos = mutableMapOf<BlockPos, PipeNode>()

    private val portMap = mutableMapOf<BlockFace, PipeNetwork.PipePortAssoc<*>>()

    override val pipes: Iterable<Pipe<*, *>>
        get() = graph.nodes.asSequence().map { it.data.pipe }.asIterable()

    override val cells
        get() = cellMap.values.asSequence().map { it.cell }.asIterable()

    override fun <P : CellPath> insert(cell: Cell<*>, pipe: Pipe<P, *>, path: P) {
        cellMap[cell.id] = CellHolder(cell, pipe, path)

        val speed = BASE_SPEED * pipe.getSpeedFactor() * cell.getSpeedFactor()
        val length = path.getLength()
        val time = length / speed
        insertTimes[cell.id] = world.time
        updateTimes[cell.id] = world.time + time.roundToLong()
    }

    override fun <X> insertFrom(cell: Cell<*>, pipe: Pipe<*, X>, port: X): Boolean {
        val a = getConnectedPipe(pipe, port) ?: return false

        // Needed because generics are implemented horribly
        // Might want to actually make this publicly accessible if needed more often
        fun <X> insert(cell: Cell<*>, a: PipeNetwork.PipePortAssoc<X>) = insert(cell, a.pipe, a.port)

        insert(cell, a)
        return true
    }

    override fun <T : CellContent> untrack(cell: Cell<T>): T {
        untrack0(cell)
        return cell.content
    }

    private fun untrack0(cell: Cell<*>) {
        val cellId = cell.id
        updateTimes.removeLong(cellId)
        insertTimes.removeLong(cellId)
        cellMap.remove(cellId)
    }

    override fun getCellWorldPos(cell: Cell<*>, delta: Float): Vec3d {
        val absPos = cellMap[cell.id] ?: error("Cell $cell is not in network!")
        val base = insertTimes.getLong(cell.id)
        val duration = updateTimes.getLong(cell.id) - base
        val progress = (world.time - base) + delta
        val a = MathHelper.clamp(progress / duration, 0f, 1f)
        val pipeBasePos = Vec3d(0.5, 0.5, 0.5) // TODO
        return pipeBasePos.add(absPos.path.getItemPosition(a))
    }

    fun tick() {
        val iter = updateTimes.iterator()
        val queued = mutableListOf<Map.Entry<UUID, Long>>()
        for (entry in iter) {
            if (entry.value < world.time) {
                iter.remove()
                queued += entry
            } else break
        }
        for ((id, _) in queued) {
            val cp = cellMap[id] ?: continue
            cp.onFinish(this)
        }
    }

    override fun <X> isPortConnected(pipe: Pipe<*, X>, port: X): Boolean {
        // TODO optimize
        return graph.nodes.first { it.data == pipe }.connections.any { it.containsPort(port) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X> getConnectedPipe(self: Pipe<*, X>, output: X): PipeNetwork.PipePortAssoc<*>? {
        // TODO optimize
        val node = graph.nodes.first { it.data == self }
        return node.connections.singleOrNull { it.data(node) == output }?.let { PipeNetwork.PipePortAssoc(it.other(node).data as Pipe<*, Any?>, it.otherData(node)) }
    }

    fun <X> createNode(pos: BlockPos, shape: PipeShape<X>, pipe: Pipe<*, X>): PipeNode {
        val result = graph.add(PipeHolder(UUID.randomUUID(), pos, pipe, shape))
        result.data.pipe.onJoinNetwork(this)
        return result
    }

    @Suppress("UNCHECKED_CAST")
    fun removeNode(node: PipeNode) {
        for (link in node.connections.toSet()) {
            (link.first.data.pipe as Pipe<*, Any?>).onDisconnect(link.data1, link.second.data.pipe)
            (link.second.data.pipe as Pipe<*, Any?>).onDisconnect(link.data2, link.first.data.pipe)
        }
        node.data.pipe.onLeaveNetwork()
        graph.remove(node)
        split()
        if (graph.nodes.isEmpty()) controller.destroyNetwork(this.id)
        else controller.rebuildRefs(this.id)
    }

    fun getNodeById(id: UUID): PipeNode? {
        // TODO optimize
        return graph.nodes.find { it.data.id == id }
    }

    fun getNodeByFace(face: BlockFace): PipeNode? {
        // TODO optimize
        return graph.nodes.find { face in it.data.shape.ports.values }
    }

    fun getNodeAt(pos: BlockPos): PipeNode? {
        return graph.nodes.find { pos in it.data.shape.blocks }
    }

    fun merge(other: PipeNetworkImpl) {
        if (other.id != id) {
            other.graph.nodes.forEach { it.data.pipe.onLeaveNetwork() }
            val newNodes = other.graph.nodes.map { it.data.id }
            graph.join(other.graph)
            insertTimes.putAll(other.insertTimes)
            updateTimes.putAll(other.updateTimes)
            cellMap.putAll(other.cellMap)
            controller.posToNetworks += controller.posToNetworks.filterValues { it == other.id }.mapValues { this.id }
            controller.destroyNetwork(other.id)
            graph.nodes.filter { it.data.id in newNodes }.forEach { it.data.pipe.onJoinNetwork(this) }
        }
        controller.rebuildRefs(this.id)
    }

    fun split(): Set<PipeNetworkImpl> {
        val newGraphs = graph.split()

        if (newGraphs.isNotEmpty()) {
            controller.markDirty()

            val networks = newGraphs.map {
                val net = controller.createNetwork()
                net.graph.join(it)
                net
            }

            networks.forEach { controller.rebuildRefs(it.id) }
            controller.rebuildRefs(this.id)

            return networks.toSet()
        }

        return emptySet()
    }

    fun rebuildRefs() {
        controller.markDirty()
        nodesInPos.clear()
        portMap.clear()
        for (node in graph.nodes) {
            nodesInPos[node.data.pos] = node
            for ((port, face) in node.data.shape.ports) {
                @Suppress("UNCHECKED_CAST")
                portMap[face] = PipeNetwork.PipePortAssoc(node.data.pipe as Pipe<*, Any?>, port)
            }
        }
    }

    fun toTag(tag: CompoundTag = CompoundTag()): CompoundTag {

        return tag
    }

    fun fromTag(tag: CompoundTag) {
        val newId = tag.getUuid("id")
        if (newId != id) error("Tried to load data for $newId into network $id")

    }

    companion object {
        const val BASE_SPEED = 0.1f

        fun fromTag(world: ServerWorld, controller: PipeNetworkState, tag: CompoundTag): PipeNetworkImpl {
            val id = tag.getUuid("id")
            val obj = PipeNetworkImpl(world, id, controller)
            obj.fromTag(tag)
            return obj
        }
    }

}

private data class CellHolder<P : CellPath>(val cell: Cell<*>, val pipe: Pipe<P, *>, val path: P) {
    // Helper method because of generics bs.
    fun onFinish(network: PipeNetwork) {
        pipe.onFinishPath(network, path, cell)
    }
}

data class PipeHolder<X>(val id: UUID, val pos: BlockPos, val pipe: Pipe<*, X>, val shape: PipeShape<X>)