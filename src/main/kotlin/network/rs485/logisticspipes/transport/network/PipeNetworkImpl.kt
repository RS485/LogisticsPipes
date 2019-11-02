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

class PipeNetworkImpl(override val world: ServerWorld, override val id: UUID) : PipeNetwork {

    private val insertTimes = Object2LongOpenHashMap<UUID>()
    private val updateTimes = Object2LongOpenHashMap<UUID>()

    private val graph = PipeGraph()

    override val pipes: Iterable<Pipe<*, *>>
        get() = graph.nodes.asSequence().map { it.data.pipe }.asIterable()

    private val cellPositions = mutableMapOf<UUID, AbsoluteCellPosition<*>>()
    private val cellMap = mutableMapOf<UUID, Cell<*>>()

    override val cells
        get() = cellMap.values.toSet()

    override fun <P : CellPath> insert(cell: Cell<*>, pipe: Pipe<P, *>, path: P) {
        cellPositions[cell.id] = AbsoluteCellPosition(pipe, path)
        cellMap[cell.id] = cell

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
        cellPositions.remove(cellId)
        cellMap.remove(cellId)
    }

    override fun getCellWorldPos(cell: Cell<*>, delta: Float): Vec3d {
        val absPos = cellPositions[cell.id] ?: error("Cell $cell is not in network!")
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
            val cell = cellMap[id] ?: continue
            val cp = cellPositions[id] ?: continue
            cp.onFinish(this, cell)
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

    fun <X> createNode(shape: PipeShape<X>, pipe: Pipe<*, X>): PipeNode {
        return graph.add(PipeHolder(pipe, shape))
    }

    fun removeNodeAt(pos: BlockPos): Boolean {
        return graph.nodes.filter { pos in it.data.shape.blocks }.onEach { removeNode(it) }.isNotEmpty()
    }

    fun removeNode(node: PipeNode) {
        graph.remove(node)

        // TODO split
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

        fun fromTag(world: ServerWorld, tag: CompoundTag): PipeNetworkImpl {
            val id = tag.getUuid("id")
            val obj = PipeNetworkImpl(world, id)
            obj.fromTag(tag)
            return obj
        }
    }

}

private data class AbsoluteCellPosition<P : CellPath>(val pipe: Pipe<P, *>, val path: P) {
    // Helper method because of generics bs.
    fun onFinish(network: PipeNetwork, cell: Cell<*>) {
        pipe.onFinishPath(network, path, cell)
    }
}

data class PipeHolder<X>(val pipe: Pipe<*, X>, val shape: PipeShape<X>)