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

import net.minecraft.block.BlockRenderLayer
import net.minecraft.class_4587
import net.minecraft.class_4597
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import network.rs485.logisticspipes.transport.Cell
import network.rs485.logisticspipes.transport.FluidCellContent

class CellRenderer(val client: MinecraftClient) {

    var prov: CellProvider = DummyCellProvider // EmptyCellProvider

    fun render(x: Double, y: Double, z: Double, delta: Float, trStack: class_4587, buffer: class_4597) {
        trStack.method_22903()
        trStack.method_22904(-x, -y, -z)
        val itemRenderer = client.itemRenderer
        for ((cell, pos) in prov.getCells(delta)) {
            trStack.method_22903()
            trStack.method_22904(pos.x, pos.y, pos.z)
            val lightLevel = client.world?.let { world ->
                val bp = BlockPos(pos)
                if (world.isChunkLoaded(bp)) world.getLightmapIndex(bp) else null
            } ?: 0
            if (cell.content is FluidCellContent) {
                @Suppress("UNCHECKED_CAST")
                renderFluidCell(pos, cell as Cell<FluidCellContent>, lightLevel, trStack, buffer)
            } else renderItemCell(pos, cell, itemRenderer, lightLevel, trStack, buffer)
            trStack.method_22909()
        }
        trStack.method_22909()
    }

    private fun renderItemCell(pos: Vec3d, cell: Cell<*>, itemRenderer: ItemRenderer, lightLevel: Int, trStack: class_4587, buffer: class_4597) {
        trStack.method_22905(0.5f, 0.5f, 0.5f)

        val stack = cell.content.getDisplayStack()
        itemRenderer.method_23178(stack, ModelTransformation.Type.FIXED, lightLevel, trStack, buffer)
    }

    private fun renderFluidCell(pos: Vec3d, cell: Cell<FluidCellContent>, lightLevel: Int, trStack: class_4587, buffer: class_4597) {
        val buf = buffer.getBuffer(BlockRenderLayer.TRANSLUCENT)
        val sprite = client.spriteAtlas.getSprite(cell.content.fluid.sprite)

        VertexFormats.POSITION_COLOR_UV_NORMAL
        buf.vertex().color().texture().vertex().next()
    }

}