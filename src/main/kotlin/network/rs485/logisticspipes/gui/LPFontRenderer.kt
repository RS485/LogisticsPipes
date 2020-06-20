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
    private val fontBold: IFont?
    private val wrapperPlain: FontWrapper
    private val wrapperBold: FontWrapper?
    private val zLevel = 15.0
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
    private fun draw(char: Char, x: Double, y: Double, color: Int, wrapper: FontWrapper, italics: Boolean, scale: Double): Double {
        val clr = Color(color)
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
        // Y position of the drawn character (because there is a single column per texture a X value is not needed)
        val textureY = wrapper.getGlyphY(c)
        // The actual character glyph containing the actual character size and offsets.
        val glyph = wrapper.getGlyph(c)
        // In case any of the above fails to be set return 0 without adding anything to the buffer.
        if (width == -1 || height == -1 || textureY == -1 || glyph == null) return 0.0
        // Character draw coordinate calculation based on scale
        val x0 = x - (glyph.offsetX * scale)
        val y1 = y + ((wrapper.charHeight + wrapper.charOffsetY - glyph.offsetY) * scale)
        val x1 = x0 + (glyph.width * scale)
        val y0 = y1 - (glyph.height * scale)
        // Texture coordinates calculation (0.0 - 1.0 depending on the position relative to the size of the full texture)
        val u0 = 0.0 / width.toDouble()
        val v0 = textureY / height.toDouble()
        val u1 = glyph.width / width.toDouble()
        val v1 = (textureY + glyph.height) / height.toDouble()
        // The offset distance requires a tan calculation because merely adding a fixed value independent of the character's height would lead to slightly different angles for each character.
        val italicsOffset = if (italics) glyph.height * tan(0.2181662) else 0.0
        // Set the Magnification filter to Nearest neighbour to have sharp looking characters. (For some reason this wasn't working when applied in the start() method
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        // Bind the texture atlas where the current character is to GL11
        GlStateManager.bindTexture(wrapper.textures[texIndex])
        // Add character quad to buffer
        buffer.pos(x0 + italicsOffset, y0, zLevel).tex(u0, v0).color(clr.red, clr.green, clr.blue, clr.alpha).endVertex()
        buffer.pos(x0, y1, zLevel).tex(u0, v1).color(clr.red, clr.green, clr.blue, clr.alpha).endVertex()
        buffer.pos(x1, y1, zLevel).tex(u1, v1).color(clr.red, clr.green, clr.blue, clr.alpha).endVertex()
        buffer.pos(x1 + italicsOffset, y0, zLevel).tex(u1, v0).color(clr.red, clr.green, clr.blue, clr.alpha).endVertex()
        // Return the final width of the character, including the spacing for the next character, while being scaled or bypassing scaling in case the scale is set to 1.
        return glyph.dWidthX * scale
    }

    /**
     * Extension of the previous draw(char), that just implements functionality for bold characters, depending on the available wrappers
     */
    private fun draw(char: Char, x: Double, y: Double, color: Int, italics: Boolean, bold: Boolean, scale: Double): Double {
        return when{
            bold && wrapperBold != null -> {
                draw(char, x, y, color, wrapperBold, italics, scale)
            }
            bold -> {
                draw(char, x + scale, y, color, wrapperPlain, italics, scale)
                draw(char, x, y, color, wrapperPlain, italics, scale)
            }
            else -> draw(char, x, y, color, wrapperPlain, italics, scale)
        }
    }

    /**
     * Extension of the previous draw(char), that just implements functionality for shadowed characters.
     */
    private fun draw(c: Char, x: Double, y: Double, color: Int, italics: Boolean, bold: Boolean, shadow: Boolean, scale: Double): Double {
        if (shadow) {
            draw(c, x + scale, y + scale, shadowColor, italics, bold, scale)
        }
        return draw(c, x, y, color, italics, bold, scale)
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
        var stringSize = string.fold(0.0) { currentX, char -> currentX + draw(char, x + currentX, y.toDouble(), color, italic, bold, shadow, scale) }
        render()
        // Lines...
        if (underline){
            val underlineY = wrapperPlain.charHeight + wrapperPlain.charOffsetY + 1
            lineDrawHorizontal(x, y + underlineY, stringSize.toInt(), scale, color, italic, shadow)
        }
        if (strikethrough) {
            val strikethroughY = (wrapperPlain.charHeight + wrapperPlain.charOffsetY) / 2
            lineDrawHorizontal(x, y + strikethroughY, stringSize.toInt(), scale, color, italic, shadow)
        }
        if (italic) stringSize += scale
        return ceil(stringSize).toInt()
    }

    fun drawSpace(x: Int, y: Int, size: Int, color: Int, italics: Boolean, underline: Boolean, strikethrough: Boolean, shadow: Boolean, scale: Double): Int {
        if (underline){
            val underlineY = wrapperPlain.charHeight + wrapperPlain.charOffsetY + 1
            lineDrawHorizontal(x, y + underlineY, size, 1.0, color, italics, shadow)
        }
        if (strikethrough) {
            val strikethroughY = (wrapperPlain.charHeight + wrapperPlain.charOffsetY) / 2
            lineDrawHorizontal(x, y + strikethroughY, size, scale, color, italics, shadow)
        }
        return size
    }

    fun drawSpace(x: Int, y: Int, size: Int, color: Int, tags: EnumSet<TextFormat>, scale: Double): Int {
        return drawSpace(x, y, size, color, tags.contains(TextFormat.Italic), tags.contains(TextFormat.Underline), tags.contains(TextFormat.Strikethrough), tags.contains(TextFormat.Shadow), scale)
    }

    fun drawCenteredString(string: String, x: Int, y: Int, tags: EnumSet<TextFormat>, scale: Double): Int {
        return drawCenteredString(string, x - (getStringWidth(string, tags, scale) / 2.0).toInt(), y, tags, scale)
    }

    fun getFontHeight(scale: Double = 1.0): Int{
        return (wrapperPlain.charHeight * scale).toInt()
    }

    private fun getCharWidth(char: Char, wrapper: FontWrapper, scale: Double): Double{
        val glyph = wrapper.getGlyph(char) ?: return 0.0
        return glyph.dWidthX * scale
    }

    private fun getCharWidth(char: Char, bold: Boolean, scale: Double): Double{
        return when{
            bold && wrapperBold != null -> {
                getCharWidth(char, wrapperBold, scale)
            }
            bold -> {
                getCharWidth(char, wrapperPlain, scale) + 1
            }
            else -> getCharWidth(char, wrapperPlain, scale)
        }
    }

    fun getStringWidth(string: String, italics: Boolean, bold: Boolean, scale: Double): Int {
        val italicsOffset = if (italics) scale else 0.0
        return (string.fold(0.0) { currentX, char -> currentX + getCharWidth(char, bold, scale) } + italicsOffset).toInt()
    }

    fun getStringWidth(string: String, tags: EnumSet<TextFormat>, scale: Double): Int {
        return  getStringWidth(string, tags.contains(TextFormat.Italic), tags.contains(TextFormat.Bold), scale)
    }

    private fun putHorizontalLine(buff: BufferBuilder, x: Double, y: Double, width: Int, thickness: Double, color: Int, italics: Boolean) {
        // TODO implement italics slight tilt. Done?
        // TODO try to apply this directly to the buffer before drawing, difficult right now because this is untextured quads and I don't know how to mix them.
        val italicsOffset = if (italics) thickness * tan(0.2181662) else 0.0
        val clr = Color(color)
        buff.pos(x + italicsOffset, y, 5.0).color(clr.red, clr.green, clr.blue, clr.alpha).endVertex()
        buff.pos(x + italicsOffset, y + thickness, 5.0).color(clr.red, clr.green, clr.blue, clr.alpha).endVertex()
        buff.pos(x + width.toDouble(), y + thickness, 5.0).color(clr.red, clr.green, clr.blue, clr.alpha).endVertex()
        buff.pos(x + width.toDouble(), y, 5.0).color(clr.red, clr.green, clr.blue, clr.alpha).endVertex()
    }

    private fun lineDrawHorizontal(x: Int, y: Int, width: Int, thickness: Double, color: Int, italics: Boolean, shadow: Boolean) {
        GlStateManager.disableTexture2D()
        val tessellator = Tessellator.getInstance()
        val buff = tessellator.buffer
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        if(shadow) putHorizontalLine(buff , x + thickness, y + thickness, width,  thickness, shadowColor, italics)
        putHorizontalLine(buff , x.toDouble(), y.toDouble(), width,  thickness, color, italics)
        tessellator.draw()
        GlStateManager.enableTexture2D()
    }

    // Old Stuff
    init {
        val fontResourcePlain = ResourceLocation(LPConstants.LP_MOD_ID, "fonts/$fontName-plain.bdf")
        val fontResourceBold = ResourceLocation(LPConstants.LP_MOD_ID, "fonts/$fontName-bold.bdf")
        fontPlain = FontParser.read(fontResourcePlain)?: throw IOException("Failed to load ${fontResourcePlain.resourcePath}, this is not tolerated.")
        fontBold = FontParser.read(fontResourceBold)
        wrapperPlain = FontWrapper(fontPlain)
        wrapperBold = if(fontBold != null){
            FontWrapper(fontBold)
        } else null
        //wrapper = wrapperPlain
        tessellator = Tessellator.getInstance()
        buffer = tessellator.buffer
    }

    private data class Color(val color: Int) {
        val alpha = color ushr 24 and 0xff
        val red = color ushr 16 and 0xff
        val green = color ushr 8 and 0xff
        val blue = color and 0xff

        fun getAlphaF(): Float {
            return alpha / 255.0f
        }

        /**
         * Extracts the 8 bits of red color information from the color code and returns them as an percentage float.
         * @return 8 bit (0-255) value of the Red channel as a float (0.0f - 1.0f).
         */
        fun getRedF(): Float {
            return red / 255.0f
        }

        /**
         * Extracts the 8 bits of green color information from the color code and returns them as an percentage float.
         * @return 8 bit (0-255) value of the Green channel as a float (0.0f - 1.0f).
         */
        fun getGreenF(): Float {
            return green / 255.0f
        }

        /**
         * Extracts the 8 bits of blue color information from the color code and returns them as an percentage float.
         * @return 8 bit (0-255) value of the Blue channel as a float (0.0f - 1.0f).
         */
        fun getBlueF(): Float {
            return blue / 255.0f
        }
    }
}
