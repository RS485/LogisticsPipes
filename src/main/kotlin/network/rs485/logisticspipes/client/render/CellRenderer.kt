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
import net.minecraft.client.render.LayeredVertexConsumerStorage
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.Matrix4f
import net.minecraft.client.util.math.Vector4f
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import network.rs485.logisticspipes.transport.Cell
import network.rs485.logisticspipes.transport.FluidCellContent
import kotlin.math.PI
import kotlin.math.sin

class CellRenderer(val client: MinecraftClient) {

    var prov: CellProvider = DummyCellProvider // EmptyCellProvider

    fun render(x: Double, y: Double, z: Double, delta: Float, trStack: MatrixStack, buffer: LayeredVertexConsumerStorage) {
        trStack.push()
        trStack.translate(-x, -y, -z)
        val itemRenderer = client.itemRenderer
        for ((cell, pos) in prov.getCells(delta)) {
            trStack.push()
            trStack.translate(pos.x, pos.y, pos.z)
            val lightLevel = client.world?.let { world ->
                val bp = BlockPos(pos)
                if (world.isChunkLoaded(bp)) world.getLightmapCoordinates(bp) else null
            } ?: 0
            if (cell.content is FluidCellContent) {
                @Suppress("UNCHECKED_CAST")
                renderFluidCell(pos, cell as Cell<FluidCellContent>, lightLevel, trStack, buffer)
            } else renderItemCell(pos, cell, itemRenderer, lightLevel, trStack, buffer)
            trStack.pop()
        }
        trStack.pop()
    }

    private fun renderItemCell(pos: Vec3d, cell: Cell<*>, itemRenderer: ItemRenderer, lightLevel: Int, trStack: MatrixStack, buffer: LayeredVertexConsumerStorage) {
        trStack.scale(0.5f, 0.5f, 0.5f)

        val stack = cell.content.getDisplayStack()
        itemRenderer.method_23178(stack, ModelTransformation.Type.FIXED, lightLevel, OverlayTexture.field_21444, trStack, buffer)
    }

    // TODO don't hardcode this
    private fun renderFluidCell(pos: Vec3d, cell: Cell<FluidCellContent>, lightLevel: Int, trStack: MatrixStack, buffer: LayeredVertexConsumerStorage) {
        val fluid = cell.content.fluid
        val sprite = client.spriteAtlas.getSprite(fluid.sprite)
        val color = fluid.renderColor
        val a = 255
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color and 0xFF

        val width = 0.25f
        val heightPct = (sin((System.nanoTime() % 2000000000L).toDouble() / 1000000000.0 * PI).toFloat() + 1.0f) / 2.0f
        val extent = width / 2.0f
        val maxY = -extent + width * heightPct
        val maxV = MathHelper.lerp(heightPct, sprite.minV, sprite.maxV)

        val lightLevel = 0x0F00F0 // FIXME drawn texture looks fucked otherwise

        val mat = trStack.peek()

        buffer.getBuffer(RenderLayer.getTranslucent()).run {
            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, -1f, 0f, 0f).next()
            vertex(mat, -extent, -extent, extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, -1f, 0f, 0f).next()
            vertex(mat, -extent, maxY, extent).color(r, g, b, a).texture(sprite.maxU, maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, -1f, 0f, 0f).next()
            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).texture(sprite.minU, maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, -1f, 0f, 0f).next()

            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, -1f).next()
            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).texture(sprite.minU, maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, -1f).next()
            vertex(mat, extent, maxY, -extent).color(r, g, b, a).texture(sprite.maxU, maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, -1f).next()
            vertex(mat, extent, -extent, -extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, -1f).next()

            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, -1f, 0f).next()
            vertex(mat, extent, -extent, -extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, -1f, 0f).next()
            vertex(mat, extent, -extent, extent).color(r, g, b, a).texture(sprite.maxU, sprite.maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, -1f, 0f).next()
            vertex(mat, -extent, -extent, extent).color(r, g, b, a).texture(sprite.minU, sprite.maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, -1f, 0f).next()

            vertex(mat, extent, maxY, -extent).color(r, g, b, a).texture(sprite.minU, maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 1f, 0f, 0f).next()
            vertex(mat, extent, maxY, extent).color(r, g, b, a).texture(sprite.maxU, maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 1f, 0f, 0f).next()
            vertex(mat, extent, -extent, extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 1f, 0f, 0f).next()
            vertex(mat, extent, -extent, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 1f, 0f, 0f).next()

            vertex(mat, extent, -extent, extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, 1f).next()
            vertex(mat, extent, maxY, extent).color(r, g, b, a).texture(sprite.maxU, maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, 1f).next()
            vertex(mat, -extent, maxY, extent).color(r, g, b, a).texture(sprite.minU, maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, 1f).next()
            vertex(mat, -extent, -extent, extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, 1f).next()

            vertex(mat, -extent, maxY, extent).color(r, g, b, a).texture(sprite.minU, sprite.maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 1f, 0f).next()
            vertex(mat, extent, maxY, extent).color(r, g, b, a).texture(sprite.maxU, sprite.maxV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 1f, 0f).next()
            vertex(mat, extent, maxY, -extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 1f, 0f).next()
            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 1f, 0f).next()
        }

//        buffer.getBuffer(RenderLayers.SOLID_COLOR).run {
//            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, -1f, 0f, 0f).next()
//            vertex(mat, -extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, -1f, 0f, 0f).next()
//            vertex(mat, -extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, -1f, 0f, 0f).next()
//            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, -1f, 0f, 0f).next()
//
//            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, -1f).next()
//            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, -1f).next()
//            vertex(mat, extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, -1f).next()
//            vertex(mat, extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, -1f).next()
//
//            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, -1f, 0f).next()
//            vertex(mat, extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, -1f, 0f).next()
//            vertex(mat, extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, -1f, 0f).next()
//            vertex(mat, -extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, -1f, 0f).next()
//
//            vertex(mat, extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 1f, 0f, 0f).next()
//            vertex(mat, extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 1f, 0f, 0f).next()
//            vertex(mat, extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 1f, 0f, 0f).next()
//            vertex(mat, extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 1f, 0f, 0f).next()
//
//            vertex(mat, extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, 1f).next()
//            vertex(mat, extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, 1f).next()
//            vertex(mat, -extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, 1f).next()
//            vertex(mat, -extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 0f, 1f).next()
//
//            vertex(mat, -extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 1f, 0f).next()
//            vertex(mat, extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 1f, 0f).next()
//            vertex(mat, extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 1f, 0f).next()
//            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.field_21444).normal(mat, 0f, 1f, 0f).next()
//        }
    }

}

fun VertexConsumer.normal(mat: Matrix4f, x: Float, y: Float, z: Float): VertexConsumer {
    val vec = Vector4f(x, y, z, 1f).apply { multiply(mat) }
    return normal(vec.x, vec.y, vec.z)
}