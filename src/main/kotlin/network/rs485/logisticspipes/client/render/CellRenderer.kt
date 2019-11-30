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

package network.rs485.logisticspipes.client.render

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferBuilderStorage
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import network.rs485.logisticspipes.transport.Cell
import network.rs485.logisticspipes.transport.CellContent
import network.rs485.logisticspipes.transport.network.CellPathHandler
import network.rs485.logisticspipes.transport.network.PipeAttribute
import network.rs485.logisticspipes.transport.network.client.ClientTrackedCells

class CellRenderer(val client: MinecraftClient) {

    @Suppress("UNCHECKED_CAST")
    fun render(x: Double, y: Double, z: Double, delta: Float, trStack: MatrixStack, buffers: BufferBuilderStorage) {
        trStack.push()
        trStack.translate(-x, -y, -z)
        val itemRenderer = client.itemRenderer
        for ((cell, pos) in getCells(delta)) {
            trStack.push()
            trStack.translate(pos.x, pos.y, pos.z)
            val lightLevel = client.world?.let { world ->
                val bp = BlockPos(pos)
                if (world.isChunkLoaded(bp)) WorldRenderer.getLightmapCoordinates(world, bp) else null
            } ?: 0
            (CellRendererRegistry.getRenderer(cell.content.getType()) as CellContentRenderer<CellContent>?)
                    ?.render(pos, cell, lightLevel, trStack, buffers)
            trStack.pop()
        }
        trStack.pop()
    }

    private fun getCells(delta: Float): Map<Cell<*>, Vec3d> {
        return ClientTrackedCells.cells.values.associate { it.cell to getCellWorldPos(it, delta) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCellWorldPos(e: ClientTrackedCells.Entry, delta: Float): Vec3d {
        val world = MinecraftClient.getInstance().world!!
        val base = e.insertTime
        val duration = e.updateTime - base
        val progress = (world.time - base) + delta
        val a = MathHelper.clamp(progress / duration, 0f, 1f)
        val pipeBasePos = Vec3d(e.pos).add(Vec3d(0.5, 0.5, 0.5))
        val cra = PipeAttribute.ATTRIBUTE.getFirstOrNull(world, e.pos) ?: error("Pipe at ${e.pos} doesn't have pipe attribute")
        return pipeBasePos.add((cra.pathHandler as CellPathHandler<Any?>).getCellPosition(e.path, a))
    }

}