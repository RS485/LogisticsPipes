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

package network.rs485.logisticspipes.gui.guidebook

import logisticspipes.LogisticsPipes
import logisticspipes.utils.MinecraftColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.PngSizeInfo
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.gui.LPGuiDrawer
import network.rs485.logisticspipes.util.math.Rectangle
import java.io.IOException
import kotlin.math.min

class DrawableImageParagraph(private val alternativeText: List<DrawableWord>, val image: DrawableImage) : DrawableParagraph() {
    override var relativeBody = Rectangle()
    override var parent: Drawable? = null

    override fun setPos(x: Int, y: Int): Int {
        relativeBody.setPos(x, y)
        // This has to be done in two steps because setChildrenPos() depends on the width already being set.
        relativeBody.setSize(parent!!.width, 0)
        relativeBody.setSize(relativeBody.roundedWidth, setChildrenPos())
        return relativeBody.roundedHeight
    }

    override fun setChildrenPos(): Int {
        var currentY = 0
        currentY += if (image.broken) {
            splitAndInitialize(alternativeText, 5, currentY, width - 10, false)
        } else {
            image.setPos(0, currentY)
        }
        return currentY
    }

    override fun getHovered(mouseX: Float, mouseY: Float): Drawable? = if (image.broken) {
        alternativeText.firstOrNull { it.isMouseHovering(mouseX, mouseY) }
    } else {
        null
    }

    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        if (image.broken) {
            super.draw(mouseX, mouseY, delta, visibleArea)
            for (drawableWord in alternativeText.filter { it.visible(visibleArea) }) {
                drawableWord.draw(mouseX, mouseY, delta, visibleArea)
                LPGuiDrawer.drawOutlineRect(absoluteBody, MinecraftColor.WHITE.colorCode)
            }
        } else {
            image.draw(mouseX, mouseY, delta, visibleArea)
        }
    }
}

class DrawableImage(private var imageResource: ResourceLocation) : Drawable {

    override var relativeBody: Rectangle = Rectangle()
    override var parent: Drawable? = null

    private var imageSize: PngSizeInfo? = try {
        val resource = Minecraft.getMinecraft().resourceManager.getResource(imageResource)
        PngSizeInfo.makeFromResource(resource)
    } catch (e: IOException) {
        LogisticsPipes.log.error("File not found: ${imageResource.resourcePath}")
        null
    }
    val broken: Boolean get() = imageSize == null


    override fun draw(mouseX: Float, mouseY: Float, delta: Float, visibleArea: Rectangle) {
        if (imageSize != null) {
            drawImage(absoluteBody, visibleArea, imageResource)
        } else {
            LPGuiDrawer.drawOutlineRect(absoluteBody, MinecraftColor.WHITE.colorCode)
        }
    }

    private fun putTexturedImage(
        bufferBuilder: BufferBuilder,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        uw: Int,
        vh: Int,
        u0: Int,
        v0: Int,
        u1: Int,
        v1: Int,
    ) {
        val atlasWidthScale = 1 / uw.toDouble()
        val atlasHeightScale = 1 / vh.toDouble()
        val u0S = u0 * atlasWidthScale
        val v0S = v0 * atlasHeightScale
        val u1S = u1 * atlasWidthScale
        val v1S = v1 * atlasHeightScale
        bufferBuilder.pos(x0, y1, 0f).tex(u0S, v1S).endVertex()
        bufferBuilder.pos(x1, y1, 0f).tex(u1S, v1S).endVertex()
        bufferBuilder.pos(x1, y0, 0f).tex(u1S, v0S).endVertex()
        bufferBuilder.pos(x0, y0, 0f).tex(u0S, v0S).endVertex()
    }

    fun drawImage(imageBody: Rectangle, visibleArea: Rectangle, image: ResourceLocation) {
        val visibleImageBody = imageBody.overlap(visibleArea)
        val xOffset = min(imageBody.x0 - visibleArea.x0, 0f)
        val yOffset = min(imageBody.y0 - visibleArea.y0, 0f)
        val visibleImageTexture = Rectangle.fromRectangle(visibleImageBody)
            .resetPos()
            .translate(xOffset, -yOffset)
        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().textureManager.bindTexture(image)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        putTexturedImage(
            bufferBuilder = bufferBuilder,
            x0 = visibleImageBody.x0,
            y0 = visibleImageBody.y0,
            x1 = visibleImageBody.x1,
            y1 = visibleImageBody.y1,
            uw = imageBody.roundedWidth,
            vh = imageBody.roundedHeight,
            u0 = visibleImageTexture.roundedLeft,
            v0 = visibleImageTexture.roundedTop,
            u1 = visibleImageTexture.roundedRight,
            v1 = visibleImageTexture.roundedBottom,
        )
        tessellator.draw()
        GlStateManager.popMatrix()
    }

    override fun setPos(x: Int, y: Int): Int {
        if (imageSize != null) {
            // Checks width of image to scale down to a size that fits on the page
            relativeBody.setSize(imageSize!!.pngWidth, imageSize!!.pngHeight)
            if (imageSize!!.pngWidth > parent!!.width) {
                val downScaleFactor = parent!!.width.toFloat() / width
                relativeBody.scale(downScaleFactor)
            }
        } else {
            relativeBody.setSize(20, 20)
        }
        return super.setPos(x, y)
    }

}

fun BufferBuilder.pos(x: Float, y: Float, z: Float): BufferBuilder = pos(x.toDouble(), y.toDouble(), z.toDouble())

fun BufferBuilder.tex(u: Float, v: Float): BufferBuilder = tex(u.toDouble(), v.toDouble())
