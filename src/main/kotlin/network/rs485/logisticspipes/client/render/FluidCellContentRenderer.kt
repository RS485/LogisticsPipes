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

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import alexiil.mc.lib.attributes.fluid.volume.NormalFluidVolume
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.BufferBuilderStorage
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import network.rs485.logisticspipes.transport.Cell
import network.rs485.logisticspipes.transport.FluidCellContent
import kotlin.math.max

object FluidCellContentRenderer : CellContentRenderer<FluidCellContent> {

    val client = MinecraftClient.getInstance()

    override fun render(pos: Vec3d, cell: Cell<FluidCellContent>, lightLevel: Int, trStack: MatrixStack, buffers: BufferBuilderStorage) {
        val fluid = cell.content.fluid

        val sprite = if (fluid is NormalFluidVolume) {
            val fl = fluid.rawFluid
            val state = fl.defaultState.blockState
            client.blockRenderManager.getModel(state).sprite
        } else {
            client.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(fluid.sprite)
        }

        val color = fluid.renderColor
        val a = 255
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color and 0xFF

        val width = 0.25f
        val heightPct = (cell.content.fluid.amount / FluidVolume.BUCKET.toFloat()).coerceIn(0f, 1f)
        val extent = width / 2.0f
        val maxY = -extent + width * heightPct
        val maxV = MathHelper.lerp(heightPct, sprite.minV, sprite.maxV)

        val sky = lightLevel shr 20 and 0xF
        val block = lightLevel shr 4 and 0xF
        val total = max(sky, block)

        val lightLevel = total shl 4

        val mat = trStack.peek().model
        val nmat = trStack.peek().normal

        buffers.entityVertexConsumers.getBuffer(RenderLayer.getTranslucent()).run {
            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, -1f, 0f, 0f).next()
            vertex(mat, -extent, -extent, extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, -1f, 0f, 0f).next()
            vertex(mat, -extent, maxY, extent).color(r, g, b, a).texture(sprite.maxU, maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, -1f, 0f, 0f).next()
            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).texture(sprite.minU, maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, -1f, 0f, 0f).next()

            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, -1f).next()
            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).texture(sprite.minU, maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, -1f).next()
            vertex(mat, extent, maxY, -extent).color(r, g, b, a).texture(sprite.maxU, maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, -1f).next()
            vertex(mat, extent, -extent, -extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, -1f).next()

            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, -1f, 0f).next()
            vertex(mat, extent, -extent, -extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, -1f, 0f).next()
            vertex(mat, extent, -extent, extent).color(r, g, b, a).texture(sprite.maxU, sprite.maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, -1f, 0f).next()
            vertex(mat, -extent, -extent, extent).color(r, g, b, a).texture(sprite.minU, sprite.maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, -1f, 0f).next()

            vertex(mat, extent, maxY, -extent).color(r, g, b, a).texture(sprite.minU, maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 1f, 0f, 0f).next()
            vertex(mat, extent, maxY, extent).color(r, g, b, a).texture(sprite.maxU, maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 1f, 0f, 0f).next()
            vertex(mat, extent, -extent, extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 1f, 0f, 0f).next()
            vertex(mat, extent, -extent, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 1f, 0f, 0f).next()

            vertex(mat, extent, -extent, extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, 1f).next()
            vertex(mat, extent, maxY, extent).color(r, g, b, a).texture(sprite.maxU, maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, 1f).next()
            vertex(mat, -extent, maxY, extent).color(r, g, b, a).texture(sprite.minU, maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, 1f).next()
            vertex(mat, -extent, -extent, extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, 1f).next()

            vertex(mat, -extent, maxY, extent).color(r, g, b, a).texture(sprite.minU, sprite.maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 1f, 0f).next()
            vertex(mat, extent, maxY, extent).color(r, g, b, a).texture(sprite.maxU, sprite.maxV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 1f, 0f).next()
            vertex(mat, extent, maxY, -extent).color(r, g, b, a).texture(sprite.maxU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 1f, 0f).next()
            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).texture(sprite.minU, sprite.minV).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 1f, 0f).next()
        }

//        buffer.getBuffer(RenderLayers.SOLID_COLOR).run {
//            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, -1f, 0f, 0f).next()
//            vertex(mat, -extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, -1f, 0f, 0f).next()
//            vertex(mat, -extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, -1f, 0f, 0f).next()
//            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, -1f, 0f, 0f).next()
//
//            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, -1f).next()
//            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, -1f).next()
//            vertex(mat, extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, -1f).next()
//            vertex(mat, extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, -1f).next()
//
//            vertex(mat, -extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, -1f, 0f).next()
//            vertex(mat, extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, -1f, 0f).next()
//            vertex(mat, extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, -1f, 0f).next()
//            vertex(mat, -extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, -1f, 0f).next()
//
//            vertex(mat, extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 1f, 0f, 0f).next()
//            vertex(mat, extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 1f, 0f, 0f).next()
//            vertex(mat, extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 1f, 0f, 0f).next()
//            vertex(mat, extent, -extent, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 1f, 0f, 0f).next()
//
//            vertex(mat, extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, 1f).next()
//            vertex(mat, extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, 1f).next()
//            vertex(mat, -extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, 1f).next()
//            vertex(mat, -extent, -extent, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 0f, 1f).next()
//
//            vertex(mat, -extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 1f, 0f).next()
//            vertex(mat, extent, maxY, extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 1f, 0f).next()
//            vertex(mat, extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 1f, 0f).next()
//            vertex(mat, -extent, maxY, -extent).color(r, g, b, a).light(lightLevel, OverlayTexture.DEFAULT_UV).normal(nmat, 0f, 1f, 0f).next()
//        }
    }

}