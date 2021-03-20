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

import logisticspipes.LPConstants
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.inventory.Container
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.util.math.Rectangle
import org.lwjgl.opengl.GL11

data class QuadD(val left: Double, val top: Double, val right: Double, val bottom: Double) {
    val topRight: Pair<Double, Double> get() = Pair(right, top)
    val topLeft: Pair<Double, Double> get() = Pair(left, top)
    val bottomLeft: Pair<Double, Double> get() = Pair(left, bottom)
    val bottomRight: Pair<Double, Double> get() = Pair(right, bottom)

    companion object {
        fun fromRectangle(rect: Rectangle) = QuadD(rect.x0.toDouble(), rect.y0.toDouble(), rect.x1.toDouble(), rect.y1.toDouble())
        fun fromResizedQuad(quad: QuadD, amount: Double) = QuadD(quad.left - amount, quad.top - amount, quad.right + amount, quad.bottom + amount)
        fun fromScaledQuad(quad: QuadD, scale: Double) = QuadD(quad.left * scale, quad.top * scale, quad.right * scale, quad.bottom * scale)
    }
}

/**
 * Drawing methods to help with Guis
 */
object LPGuiDrawer {

    private val guiAtlas = ResourceLocation(LPConstants.LP_MOD_ID, "textures/gui/gui.png")

    private const val border = 4
    private val guiBackgroundTexture = QuadD(0.0, 96.0, 16.0, 112.0)
    private val guiInnerBackgroundTexture = QuadD(4.0, 100.0, 12.0, 108.0)
    private val slotNormalTexture = QuadD(0.0, 112.0, 18.0, 130.0)
    private val slotDiskTexture = QuadD(18.0, 112.0, 36.0, 130.0)
    private val slotProgrammerTexture = QuadD(36.0, 112.0, 54.0, 130.0)
    private val slotSmallTexture = QuadD(54.0, 112.0, 62.0, 120.0)
    private val slotBigTexture = QuadD(0.0, 130.0, 26.0, 156.0)

    private val tessellator: Tessellator get() = Tessellator.getInstance()
    private val buffer: BufferBuilder get() = tessellator.buffer
    private val textureManager = Minecraft.getMinecraft().renderEngine
    private var z = 0.0
    private var textureSize = 256
        set(value) {
            textureFactor = 1.0 / value
            field = value
        }
    private var textureFactor = 0.0

    enum class Corner(val xMod: Int, val yMod: Int, val textureQuad: QuadD) {
        TOP_LEFT(border, border, QuadD(guiBackgroundTexture.left, guiBackgroundTexture.top, guiInnerBackgroundTexture.left, guiInnerBackgroundTexture.top)),
        TOP_RIGHT(-border, border, QuadD(guiInnerBackgroundTexture.right, guiBackgroundTexture.top, guiBackgroundTexture.right, guiInnerBackgroundTexture.top)),
        BOTTOM_LEFT(border, -border, QuadD(guiBackgroundTexture.left, guiInnerBackgroundTexture.bottom, guiInnerBackgroundTexture.left, guiBackgroundTexture.bottom)),
        BOTTOM_RIGHT(-border, -border, QuadD(guiInnerBackgroundTexture.right, guiInnerBackgroundTexture.bottom, guiBackgroundTexture.right, guiBackgroundTexture.bottom));
    }

    private fun Rectangle.quadFromCorner(corner: Corner): QuadD = when (corner) {
        Corner.TOP_LEFT -> QuadD(left.toDouble(), top.toDouble(), (left + corner.xMod).toDouble(), (top + corner.yMod).toDouble())
        Corner.TOP_RIGHT -> QuadD((right + corner.xMod).toDouble(), top.toDouble(), right.toDouble(), (top + corner.yMod).toDouble())
        Corner.BOTTOM_LEFT -> QuadD(left.toDouble(), (bottom + corner.yMod).toDouble(), (left + corner.xMod).toDouble(), bottom.toDouble())
        Corner.BOTTOM_RIGHT -> QuadD((right + corner.xMod).toDouble(), (bottom + corner.yMod).toDouble(), right.toDouble(), bottom.toDouble())
    }

    enum class Edge(val xMod: Int, val yMod: Int, val textureQuad: QuadD) {
        LEFT(border, border, QuadD(guiBackgroundTexture.left, guiInnerBackgroundTexture.top, guiInnerBackgroundTexture.left, guiInnerBackgroundTexture.bottom)),
        TOP(border, border, QuadD(guiInnerBackgroundTexture.left, guiBackgroundTexture.top, guiInnerBackgroundTexture.right, guiInnerBackgroundTexture.top)),
        RIGHT(-border, border, QuadD(guiInnerBackgroundTexture.right, guiInnerBackgroundTexture.top, guiBackgroundTexture.right, guiInnerBackgroundTexture.bottom)),
        BOTTOM(border, -border, QuadD(guiInnerBackgroundTexture.left, guiInnerBackgroundTexture.bottom, guiInnerBackgroundTexture.right, guiBackgroundTexture.bottom));
    }

    private fun Rectangle.quadFromEdge(edge: Edge): QuadD = when (edge) {
        Edge.LEFT -> QuadD(left.toDouble(), (top + edge.yMod).toDouble(), (left + edge.xMod).toDouble(), (bottom - edge.yMod).toDouble())
        Edge.TOP -> QuadD((left + edge.xMod).toDouble(), top.toDouble(), (right - edge.xMod).toDouble(), (top + edge.yMod).toDouble())
        Edge.RIGHT -> QuadD((right + edge.xMod).toDouble(), (top + edge.yMod).toDouble(), right.toDouble(), (bottom - edge.yMod).toDouble())
        Edge.BOTTOM -> QuadD((left + edge.xMod).toDouble(), (bottom + edge.yMod).toDouble(), (right - edge.xMod).toDouble(), bottom.toDouble())
    }

    fun drawGuiBackground(guiArea: Rectangle, z: Double, container: Container) {
        this.z = z
        textureManager.bindTexture(guiAtlas)
        textureSize = 256

        start()

        buffer.putGuiBackgroundBase(guiArea)
        buffer.putContainerSlots(guiArea, container)

        finish()
    }

    private fun BufferBuilder.putGuiBackgroundBase(guiArea: Rectangle) {

        Corner.values().forEach {
            putTexturedQuad(guiArea.quadFromCorner(it), it.textureQuad, MinecraftColor.WHITE.colorCode)
        }

        Edge.values().forEach {
            putTexturedQuad(guiArea.quadFromEdge(it), it.textureQuad, MinecraftColor.WHITE.colorCode)
        }

        putTexturedQuad(
            quad = QuadD(
                left = (guiArea.left + border).toDouble(),
                top = (guiArea.top + border).toDouble(),
                right = (guiArea.right - border).toDouble(),
                bottom = (guiArea.bottom - border).toDouble()
            ),
            texture = guiInnerBackgroundTexture,
            color = MinecraftColor.WHITE.colorCode,
        )

    }

    private fun BufferBuilder.putContainerSlots(guiArea: Rectangle, container: Container) {

        for (slot in container.inventorySlots) {
            putNormalSlot(guiArea.left + slot.xPos.toDouble(), guiArea.top + slot.yPos.toDouble())
        }

    }

    private fun BufferBuilder.putNormalSlot(x: Double, y: Double){
        val normalSlotSize = 16.0
        putTexturedQuad(
            QuadD.fromResizedQuad(
                QuadD(x, y, x + normalSlotSize, y + normalSlotSize),
                1.0
            ),
            slotNormalTexture,
            MinecraftColor.WHITE.colorCode
        )
    }

    fun drawRect(area: Rectangle, z: Double, color: Int){
        this.z = z
        GlStateManager.disableTexture2D()
        GlStateManager.disableAlpha()
        start(DefaultVertexFormats.POSITION_COLOR)
        buffer.putQuad(QuadD.fromRectangle(area), color)
        finish()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    fun drawHorizontalGradientRect(area: Rectangle, z: Double, colorLeft: Int, colorRight: Int) {
        drawGradientQuad(area, z, colorRight, colorLeft, colorLeft, colorRight)
    }

    fun drawVerticalGradientRect(area: Rectangle, z: Double, colorTop: Int, colorBottom: Int) {
        drawGradientQuad(area, z, colorTop, colorTop, colorBottom, colorBottom)
    }

    private fun drawGradientQuad(area: Rectangle, z: Double, colorTopRight: Int, colorTopLeft: Int, colorBottomLeft: Int, colorBottomRight: Int) {

        this.z = z
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
        start(DefaultVertexFormats.POSITION_COLOR)
        buffer.pos(area.x1.toDouble() to area.y0.toDouble()).rgba(colorTopRight).endVertex()
        buffer.pos(area.x0.toDouble() to area.y0.toDouble()).rgba(colorTopLeft).endVertex()
        buffer.pos(area.x0.toDouble() to area.y1.toDouble()).rgba(colorBottomRight).endVertex()
        buffer.pos(area.x1.toDouble() to area.y1.toDouble()).rgba(colorBottomLeft).endVertex()
        finish()
        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()

    }

    private fun BufferBuilder.putTexturedQuad(quad: QuadD, texture: QuadD, color: Int) {
        pos(quad.topRight).tex(texture.topRight).rgba(color).endVertex()
        pos(quad.topLeft).tex(texture.topLeft).rgba(color).endVertex()
        pos(quad.bottomLeft).tex(texture.bottomLeft).rgba(color).endVertex()
        pos(quad.bottomRight).tex(texture.bottomRight).rgba(color).endVertex()
    }

    private fun BufferBuilder.putQuad(quad: QuadD, color: Int) {
        pos(quad.topRight).rgba(color).endVertex()
        pos(quad.topLeft).rgba(color).endVertex()
        pos(quad.bottomLeft).rgba(color).endVertex()
        pos(quad.bottomRight).rgba(color).endVertex()
    }

    /**
     * Starts the BufferBuilder
     */
    private fun start(vertexFormats: VertexFormat = DefaultVertexFormats.POSITION_TEX_COLOR) {
        buffer.begin(GL11.GL_QUADS, vertexFormats)
    }

    private fun finish() {
        tessellator.draw()
    }

    /**
     * Cleanly splits the integer into the 4 rgba components and applies those to the BufferBuilder
     * Uses BufferBuilder.color(I,I,I,I) because BufferBuilder.color(F,F,F,F) will just multiply the floats back into integers.
     * @param color rgba color to be used
     * @return used buffer builder.
     */
    private fun BufferBuilder.rgba(color: Int): BufferBuilder {
        val a = (color shr 24 and 255)
        val r = (color shr 16 and 255)
        val g = (color shr 8 and 255)
        val b = (color and 255)
        return this.color(r, g, b, a)
    }

    private fun BufferBuilder.tex(point: Pair<Double, Double>): BufferBuilder = tex(point.first * textureFactor, point.second * textureFactor)

    private fun BufferBuilder.pos(point: Pair<Double, Double>): BufferBuilder = pos(point.first, point.second, z)
}
