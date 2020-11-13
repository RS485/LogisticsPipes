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

package network.rs485.logisticspipes.gui

import logisticspipes.LPConstants
import lombok.Getter
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import network.rs485.logisticspipes.util.alpha
import network.rs485.logisticspipes.util.blue
import network.rs485.logisticspipes.util.green
import network.rs485.logisticspipes.util.red
import network.rs485.markdown.*
import org.lwjgl.opengl.GL11
import java.io.IOException
import java.util.*
import kotlin.math.ceil
import kotlin.math.tan

@Suppress("DuplicatedCode")
open class LPFontRenderer(fontName: String) {

    @Getter
    private val shadowColor = 0xEE3C3F41.toInt()
    private val fontPlain: IFont
    private val wrapperPlain: FontWrapper
    var zLevel = 5.0
    private val tessellator: Tessellator
    private val buffer: BufferBuilder

    private fun start() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
    }

    private fun render() {
        tessellator.draw()
        GlStateManager.disableAlpha()
    }

    /**
     * Adds the given char at the given spot with the set color to the buffer and returns the xOffset for the next character.
     *
     * @param char    Character to be drawn
     * @param x       X coordinate to start drawing the character (left boundary)
     * @param y       Y coordinate to draw the character. (top of the line)
     * @param color   ARGB color for the character to be drawn.
     * @param wrapper This is passed so you could draw with different fonts in quick succession if needed.
     * @param italics This defines if the characters will have a front lean to them.
     * @param scale   This defines the size of the character to be drawn.
     * @return the width of the drawn character
     */
    private fun putChar(char: Char, x: Double, y: Double, color: Int, wrapper: FontWrapper, italics: Boolean, scale: Double): Double {
        val c: Char
        // Find current the character's uv coordinates
        val texIndex = when {
            wrapper.getTextureIndex(char) != -1 -> {
                // Get the texture atlas in which this character is represented. In case it is not defined check the default char.
                c = char
                wrapper.getTextureIndex(c)
            }
            wrapper.getTextureIndex(wrapper.defaultChar) != -1 -> {
                // Get the texture atlas in which the default character is represented. In case it is not defined return 0 and don't add anything to the buffer.
                c = wrapper.defaultChar
                wrapper.getTextureIndex(wrapper.defaultChar)
            }
            else -> return 0.0
        }
        // Width of the drawn character texture
        val width = wrapper.getCharWidth(texIndex)
        // Height of the drawn character texture
        val height = wrapper.getCharHeight(texIndex)
        // X position of the drawn character
        val textureX = wrapper.getGlyphX(c)
        // Y position of the drawn character
        val textureY = wrapper.getGlyphY(c)
        // The actual character glyph containing the actual character size and offsets.
        val glyph = wrapper.getGlyph(c)
        // In case any of the above fails to be set return 0 without adding anything to the buffer.
        if (width == -1 || height == -1 || textureX == -1 || textureY == -1 || glyph == null) return 0.0
        // Scaled character dimensions.
        val sWidth = (glyph.width * scale)
        val sHeight = (glyph.height * scale)
        // Character draw coordinate calculation based on scale
        val x0 = x - (glyph.offsetX * scale)
        val y1 = y + ((wrapper.charHeight + wrapper.charOffsetY - glyph.offsetY) * scale)
        val x1 = x0 + sWidth
        val y0 = y1 - sHeight
        // Texture coordinates calculation (0.0 - 1.0 depending on the position relative to the size of the full texture)
        val u0 = textureX / width.toDouble()
        val v0 = textureY / height.toDouble()
        val u1 = (textureX + glyph.width) / width.toDouble()
        val v1 = (textureY + glyph.height) / height.toDouble()
        // The offset distance requires a tan calculation because merely adding a fixed value independent of the character's height would lead to slightly different angles for each character.
        val italicsOffset = if (italics) glyph.height * tan(0.2181662) else 0.0
        // Set the Magnification filter to Nearest neighbour to have sharp looking characters. (For some reason this wasn't working when applied in the start() method
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        // Bind the texture atlas where the current character is to GL11
        GlStateManager.bindTexture(wrapper.textures[texIndex])
        // Add character quad to buffer
        buffer.pos(x0 + italicsOffset, y0, zLevel).tex(u0, v0).color(red(color), green(color), blue(color), alpha(color)).endVertex()
        buffer.pos(x0, y1, zLevel).tex(u0, v1).color(red(color), green(color), blue(color), alpha(color)).endVertex()
        buffer.pos(x1, y1, zLevel).tex(u1, v1).color(red(color), green(color), blue(color), alpha(color)).endVertex()
        buffer.pos(x1 + italicsOffset, y0, zLevel).tex(u1, v0).color(red(color), green(color), blue(color), alpha(color)).endVertex()
        // Return the final width of the character, including the spacing for the next character, while being scaled or bypassing scaling in case the scale is set to 1.
        return glyph.dWidthX * scale
    }

    /**
     * Extension of the previous draw(char), that just implements functionality for bold characters, depending on the available wrappers
     */
    private fun putChar(char: Char, x: Double, y: Double, color: Int, italics: Boolean, bold: Boolean, scale: Double): Double {
        return if (bold) {
            putChar(char, x + scale, y, color, wrapperPlain, italics, scale)
            putChar(char, x, y, color, wrapperPlain, italics, scale) + scale
        } else {
            putChar(char, x, y, color, wrapperPlain, italics, scale)
        }
    }


    /**
     * Extension of the previous draw(char), that just implements functionality for shadowed characters.
     */
    private fun putChar(c: Char, x: Double, y: Double, color: Int, italics: Boolean, bold: Boolean, shadow: Boolean, scale: Double): Double {
        if (shadow) {
            putChar(c, x + scale, y + scale, shadowColor, italics, bold, scale)
        }
        return putChar(c, x, y, color, italics, bold, scale)
    }

    /**
     * Draws the given string with all the possible parameters applied to it.
     */
    fun drawString(string: String, x: Int, y: Int, color: Int, format: EnumSet<TextFormat>, scale: Double): Int =
            drawString(string = string, x = x, y = y, color = color, italic = format.italic(), bold = format.bold(),
                    shadow = format.shadow(), underline = format.underline(), strikethrough = format.strikethrough(), scale = scale)

    /**
     * Draws the given string with all the possible parameters applied to it.
     */
    private fun drawString(string: String, x: Int, y: Int, color: Int, italic: Boolean, bold: Boolean, shadow: Boolean, underline: Boolean, strikethrough: Boolean, scale: Double): Int {
        start()
        var stringSize = string.fold(0.0) { currentX, char -> currentX + putChar(char, x + currentX, y.toDouble(), color, italic, bold, shadow, scale) }
        // Lines...
        putOverlayFormatting(x = x, y = y, width = stringSize, color = color, italic = italic, underline = underline, strikethrough = strikethrough, shadow = shadow, scale = scale)
        render()
        if (italic) stringSize += scale
        return ceil(stringSize).toInt()
    }

    /**
     * Adds the formatting lines to the buffer, for underline and strikethrough.
     */
    private fun putOverlayFormatting(x: Int, y: Int, width: Double, color: Int, italic: Boolean, underline: Boolean, strikethrough: Boolean, shadow: Boolean, scale: Double) {
        if (underline) {
            val underlineY = (wrapperPlain.charHeight + wrapperPlain.charOffsetY + 1) * scale
            if (shadow) putHorizontalLine(x = x + scale, y = y + underlineY + scale, width = width, thickness = scale, color = shadowColor, italics = italic)
            putHorizontalLine(x = x + 0.0, y = y + underlineY + 0.0, width = width, thickness = scale, color = color, italics = italic)
        }
        if (strikethrough) {
            val strikethroughY = ((wrapperPlain.charHeight + wrapperPlain.charOffsetY + 2) / 2) * scale
            if (shadow) putHorizontalLine(x = x + scale, y = y + strikethroughY + scale, width = width, thickness = scale, color = shadowColor, italics = italic)
            putHorizontalLine(x = x + 0.0, y = y + strikethroughY + 0.0, width = width, thickness = scale, color = color, italics = italic)
        }
    }

    fun drawSpace(x: Int, y: Int, width: Int, color: Int, italic: Boolean, underline: Boolean, strikethrough: Boolean, shadow: Boolean, scale: Double): Int {
        if (width > 0 && (underline || strikethrough)) {
            start()
            putOverlayFormatting(x = x, y = y, width = width.toDouble(), color = color, italic = italic, underline = underline, strikethrough = strikethrough, shadow = shadow, scale = scale)
            render()
        }
        return width
    }

    fun drawCenteredString(string: String, x: Int, y: Int, color: Int, tags: EnumSet<TextFormat>, scale: Double): Int {
        return drawString(string, x - (getStringWidth(string, tags, scale) / 2.0).toInt(), y, color, tags, scale)
    }

    fun getFontHeight(scale: Double = 1.0): Int {
        return (wrapperPlain.charHeight * scale).toInt()
    }

    private fun getCharWidth(char: Char, wrapper: FontWrapper, scale: Double): Double {
        val glyph = wrapper.getGlyph(char) ?: return 0.0
        return glyph.dWidthX * scale
    }

    private fun getCharWidth(char: Char, bold: Boolean, scale: Double): Double {
        return if (bold) {
            getCharWidth(char, wrapperPlain, scale) + 1
        } else {
            getCharWidth(char, wrapperPlain, scale)
        }
    }


    fun getStringWidth(string: String, italics: Boolean, bold: Boolean, scale: Double): Int {
        val italicsOffset = if (italics) scale else 0.0
        return (string.fold(0.0) { currentX, char -> currentX + getCharWidth(char, bold, scale) } + italicsOffset).toInt()
    }

    fun getStringWidth(string: String, tags: EnumSet<TextFormat>, scale: Double): Int {
        return getStringWidth(string, tags.contains(TextFormat.Italic), tags.contains(TextFormat.Bold), scale)
    }

    fun getStringWidth(string: String): Int = getStringWidth(string = string, italics = false, bold = false, scale = 1.0)

    private fun putHorizontalLine(x: Double, y: Double, width: Double, thickness: Double, color: Int, italics: Boolean) {
        // TODO implement italics slight tilt. Done?
        // TODO try to apply this directly to the buffer before drawing, difficult right now because this is untextured quads and I don't know how to mix them.
        val italicsOffset = if (italics) thickness else 0.0
        //GlStateManager.disableTexture2D()
        buffer.pos(x, y, 5.0).tex(0.0, 0.0).color(red(color), green(color), blue(color), alpha(color)).endVertex()
        buffer.pos(x, y + thickness, 5.0).tex(0.0, 0.0).color(red(color), green(color), blue(color), alpha(color)).endVertex()
        buffer.pos(x + width + italicsOffset, y + thickness, 5.0).tex(0.0, 0.0).color(red(color), green(color), blue(color), alpha(color)).endVertex()
        buffer.pos(x + width + italicsOffset, y, 5.0).tex(0.0, 0.0).color(red(color), green(color), blue(color), alpha(color)).endVertex()
        //GlStateManager.enableTexture2D()
    }

    init {
        // TODO reuse parsed font after it was firstly parsed
        val fontResourcePlain = ResourceLocation(LPConstants.LP_MOD_ID, "fonts/$fontName.bdf")
        fontPlain = FontParser.read(fontResourcePlain) ?: throw IOException("Failed to load ${fontResourcePlain.resourcePath}, this is not tolerated.")
        wrapperPlain = FontWrapper(fontPlain)
        tessellator = Tessellator.getInstance()
        buffer = tessellator.buffer
    }
}
