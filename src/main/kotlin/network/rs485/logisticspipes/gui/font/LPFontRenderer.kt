/*
 * Copyright (c) 2022  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2022  RS485
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

package network.rs485.logisticspipes.gui.font

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import logisticspipes.LPConstants
import logisticspipes.LogisticsPipes
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import network.rs485.grow.Coroutines
import network.rs485.logisticspipes.gui.guidebook.pos
import network.rs485.logisticspipes.gui.guidebook.tex
import network.rs485.logisticspipes.util.alpha
import network.rs485.logisticspipes.util.blue
import network.rs485.logisticspipes.util.green
import network.rs485.logisticspipes.util.red
import network.rs485.markdown.*
import org.lwjgl.opengl.GL11
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.tan

class LPFontRenderer(private val fontName: String) {
    companion object Factory {
        private val fontRenderersOfThisWorld = ConcurrentHashMap<String, LPFontRenderer>()
        private val preloadFonts = listOf("ter-u12n")
        fun get(fontName: String): LPFontRenderer = fontRenderersOfThisWorld.getOrPut(fontName) { LPFontRenderer(fontName) }

        @ExperimentalCoroutinesApi
        fun asyncPreload() {
            Coroutines.ioScope.launch {
                preloadFonts.map {
                    async {
                        get(it).apply { ::fontPlain.get() }
                    }
                }.forEach { deferred ->
                    deferred.invokeOnCompletion {
                        if (it != null) {
                            LogisticsPipes.log.error("Error while preloading fonts:\n${it.stackTraceToString()}")
                        } else {
                            val fontRenderer = deferred.getCompleted()
                            LogisticsPipes.log.info("Preloaded font files: ${fontRenderer.fontName}")
                            Minecraft.getMinecraft().addScheduledTask {
                                fontRenderer::wrapperPlain.get()
                                LogisticsPipes.log.info("Created font textures for: ${fontRenderer.fontName}")
                            }
                        }
                    }
                }
            }
        }
    }

    private val fontPlain: IFont by lazy {
        val initialTime = System.currentTimeMillis()
        val fontResourcePlain = ResourceLocation(LPConstants.LP_MOD_ID, "fonts/$fontName.bdf")
        FontParser.read(fontResourcePlain).also {  LogisticsPipes.log.info("Elapsed time parsing font: ${System.currentTimeMillis() - initialTime}ms") } ?: throw IOException("Failed to load ${fontResourcePlain.resourcePath}, this is not tolerated.")
    }

    private val wrapperPlain: FontWrapper by lazy {
        val initialTime = System.currentTimeMillis()
        FontWrapper(fontPlain).also {  LogisticsPipes.log.info("Elapsed time wrapping font: ${System.currentTimeMillis() - initialTime}ms") }
    }

    private val shadowColor = 0xEE3C3F41.toInt()
    var zLevel: Float = 5f

    private val tessellator
        get() = Tessellator.getInstance()

    private fun start() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        tessellator.buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
    }

    private fun startUntextured() {
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        tessellator.buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
    }

    private fun render() {
        tessellator.draw()
        GlStateManager.disableAlpha()
        GlStateManager.enableTexture2D()
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
    private fun putChar(char: Char, x: Float, y: Float, color: Int, wrapper: FontWrapper, italics: Boolean, scale: Float): Float {
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
            else -> return 0f
        }
        // Width of the drawn character texture
        val width = wrapper.getFontTextureSize()
        // Height of the drawn character texture
        val height = wrapper.getFontTextureSize()
        // X position of the drawn character
        val textureX = wrapper.getGlyphX(c)
        // Y position of the drawn character
        val textureY = wrapper.getGlyphY(c)
        // The actual character glyph containing the actual character size and offsets.
        val glyph = wrapper.getGlyph(c)
        // In case any of the above fails to be set return 0 without adding anything to the buffer.
        if (width == -1 || height == -1 || textureX == -1 || textureY == -1 || glyph == null) return 0f
        // Scaled character dimensions.
        val sWidth = (glyph.width * scale)
        val sHeight = (glyph.height * scale)
        // Character draw coordinate calculation based on scale
        val x0 = x - (glyph.offsetX * scale)
        val y1 = y + ((wrapper.fontLineOffset - glyph.offsetY) * scale)
        val x1 = x0 + sWidth
        val y0 = y1 - sHeight
        // Texture coordinates calculation (0.0 - 1.0 depending on the position relative to the size of the full texture)
        val u0 = textureX / width.toFloat()
        val v0 = textureY / height.toFloat()
        val u1 = (textureX + glyph.width) / width.toFloat()
        val v1 = (textureY + glyph.height) / height.toFloat()
        // The offset distance requires a tan calculation because merely adding a fixed value independent of the character's height would lead to slightly different angles for each character.
        val italicsOffset = if (italics) glyph.height * tan(0.2181662f) else 0f
        // Set the Magnification filter to Nearest neighbour to have sharp looking characters. (For some reason this wasn't working when applied in the start() method
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        // Bind the texture atlas where the current character is to GL11
        GlStateManager.bindTexture(texIndex)
        // Add character quad to buffer
        tessellator.buffer.pos(x0 + italicsOffset, y0, zLevel).tex(u0, v0).color(color.red(), color.green(), color.blue(), color.alpha()).endVertex()
        tessellator.buffer.pos(x0, y1, zLevel).tex(u0, v1).color(color.red(), color.green(), color.blue(), color.alpha()).endVertex()
        tessellator.buffer.pos(x1, y1, zLevel).tex(u1, v1).color(color.red(), color.green(), color.blue(), color.alpha()).endVertex()
        tessellator.buffer.pos(x1 + italicsOffset, y0, zLevel).tex(u1, v0).color(color.red(), color.green(), color.blue(), color.alpha()).endVertex()
        // Return the final width of the character, including the spacing for the next character, while being scaled or bypassing scaling in case the scale is set to 1.
        return glyph.dWidthX * scale
    }

    /**
     * Extension of the previous draw(char), that just implements functionality for bold characters, depending on the available wrappers
     */
    private fun putChar(char: Char, x: Float, y: Float, color: Int, italics: Boolean, bold: Boolean, scale: Float): Float {
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
    private fun putChar(c: Char, x: Float, y: Float, color: Int, italics: Boolean, bold: Boolean, shadow: Boolean, scale: Float): Float {
        if (shadow) {
            putChar(c, x + scale, y + scale, shadowColor, italics, bold, scale)
        }
        return putChar(c, x, y, color, italics, bold, scale)
    }

    /**
     * Draws the given string with all the possible parameters applied to it.
     */
    fun drawString(string: String, x: Float, y: Float, color: Int, format: Set<TextFormat>, scale: Float): Int =
            drawString(string = string, x = x, y = y, color = color, italic = format.italic(), bold = format.bold(), shadow = format.shadow(), underline = format.underline(), strikethrough = format.strikethrough(), scale = scale)

    /**
     * Draws the given string with all the possible parameters applied to it.
     */
    private fun drawString(string: String, x: Float, y: Float, color: Int, italic: Boolean, bold: Boolean, shadow: Boolean, underline: Boolean, strikethrough: Boolean, scale: Float): Int {
        start()
        var stringSize = string.fold(0f) { currentX, char -> currentX + putChar(char, x + currentX, y, color, italic, bold, shadow, scale) }
        // Lines...
        render()
        if(underline || strikethrough) {
            startUntextured()
            putOverlayFormatting(
                x = x,
                y = y,
                width = stringSize,
                color = color,
                italic = italic,
                underline = underline,
                strikethrough = strikethrough,
                shadow = shadow,
                scale = scale
            )
            render()
        }
        if (italic) stringSize += scale
        return ceil(stringSize).toInt()
    }

    /**
     * Adds the formatting lines to the buffer, for underline and strikethrough.
     */
    private fun putOverlayFormatting(x: Float, y: Float, width: Float, color: Int, italic: Boolean, underline: Boolean, strikethrough: Boolean, shadow: Boolean, scale: Float) {
        if (underline) {
            val underlineY = (wrapperPlain.fontHeight - 1) * scale
            if (shadow) putHorizontalLine(x = x + scale, y = y + underlineY + scale, width = width, thickness = scale, color = shadowColor, italics = italic)
            putHorizontalLine(x = x, y = y + underlineY, width = width, thickness = scale, color = color, italics = italic)
        }
        if (strikethrough) {
            val strikethroughY = ((wrapperPlain.fontHeight) / 2f) * scale
            if (shadow) putHorizontalLine(x = x + scale, y = y + strikethroughY + scale, width = width, thickness = scale, color = shadowColor, italics = italic)
            putHorizontalLine(x = x, y = y + strikethroughY, width = width, thickness = scale, color = color, italics = italic)
        }
    }

    fun drawSpace(x: Float, y: Float, width: Int, color: Int, italic: Boolean, underline: Boolean, strikethrough: Boolean, shadow: Boolean, scale: Float): Int {
        if (width > 0 && (underline || strikethrough)) {
            startUntextured()
            putOverlayFormatting(x = x, y = y, width = width.toFloat(), color = color, italic = italic, underline = underline, strikethrough = strikethrough, shadow = shadow, scale = scale)
            render()
        }
        return width
    }

    fun drawCenteredString(string: String, x: Float, y: Float, color: Int, tags: Set<TextFormat>, scale: Float): Int {
        return drawString(string, x - (getStringWidth(string, tags, scale) / 2f), y, color, tags, scale)
    }

    // It's not being rounded because it's supposed to be floored.
    fun getFontHeight(scale: Float = 1f): Int = (wrapperPlain.fontHeight * scale).toInt()

    private fun getCharWidth(char: Char, wrapper: FontWrapper, scale: Float): Float {
        val glyph = wrapper.getGlyph(char) ?: return 0f
        return glyph.dWidthX * scale
    }

    private fun getCharWidth(char: Char, bold: Boolean, scale: Float): Float {
        return if (bold) {
            getCharWidth(char, wrapperPlain, scale) + 1
        } else {
            getCharWidth(char, wrapperPlain, scale)
        }
    }

    fun getStringWidth(string: String, italics: Boolean, bold: Boolean, scale: Float): Int {
        val italicsOffset = if (italics) scale else 0f
        return (string.fold(0.0) { currentX, char -> currentX + getCharWidth(char, bold, scale) } + italicsOffset).toInt()
    }

    fun getStringWidth(string: String, tags: Set<TextFormat>, scale: Float): Int {
        return getStringWidth(string, tags.italic(), tags.bold(), scale)
    }

    fun getStringWidth(string: String): Int = getStringWidth(string = string, italics = false, bold = false, scale = 1f)

    private fun putHorizontalLine(x: Float, y: Float, width: Float, thickness: Float, color: Int, italics: Boolean) {
        val italicsOffset = if (italics) thickness else 0f
        tessellator.buffer.pos(x, y, 5f).color(color.red(), color.green(), color.blue(), color.alpha()).endVertex()
        tessellator.buffer.pos(x, y + thickness, 5f).color(color.red(), color.green(), color.blue(), color.alpha()).endVertex()
        tessellator.buffer.pos(x + width + italicsOffset, y + thickness, 5f).color(color.red(), color.green(), color.blue(), color.alpha()).endVertex()
        tessellator.buffer.pos(x + width + italicsOffset, y, 5f).color(color.red(), color.green(), color.blue(), color.alpha()).endVertex()
    }
}
