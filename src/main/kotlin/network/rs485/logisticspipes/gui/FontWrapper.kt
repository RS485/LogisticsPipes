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

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer
import java.util.*

private val buffer = ByteBuffer.allocateDirect(10000000).asIntBuffer()

class FontWrapper(private val font: IFont) {
    var textures: List<Int> = emptyList(); private set

    private var glyphPosX: Map<Char, Int> = emptyMap()
    private var glyphPosY: Map<Char, Int> = emptyMap()
    private var textureIndex: Map<Char, Int> = emptyMap()

    private var widthMap: Map<Int, Int> = emptyMap()
    private var heightMap: Map<Int, Int> = emptyMap()

    val charHeight: Int
    val charOffsetY: Int
    private val charBottomLine: Int

    val defaultChar = font.defaultChar

    private val maxTexSize = 256

    init {
        allocateTextures()

        var currMaxCharHeight = 0
        var currMinOffsetY = 0

        var currentX = 0
        var currentY = 0
        var currentMaxHeight = 0
        var currentTex = 0

        for ((character, glyph) in font.glyphs) {
            currMaxCharHeight = maxOf(currMaxCharHeight, glyph.height + glyph.offsetY)
            currMinOffsetY = minOf(currMinOffsetY, glyph.offsetY)

            if (currentX + glyph.width > widthMap[currentTex]!!) {
                currentX = 0
                currentY += currentMaxHeight
                currentMaxHeight = 0
                if (currentY > heightMap[currentTex]!!) {
                    currentY = 0
                    currentTex++
                }
            }
            glyphPosX = glyphPosX + (character to currentX)
            glyphPosY = glyphPosY + (character to currentY)
            textureIndex = textureIndex + (character to currentTex)
            setTexture(glyph.bitmap, currentTex, currentX, currentY, glyph.width, glyph.height)
            currentX += glyph.width
            currentMaxHeight = maxOf(currentMaxHeight, glyph.height)
            if (currentTex > textures.size)
                error("A fatal error occurred while writing texture sheet. This shouldn't ever happen unless this code has a bug. RIP")
        }

        charHeight = currMaxCharHeight
        charOffsetY = currMinOffsetY
        charBottomLine = charHeight + charOffsetY
    }

    private fun setTexture(bitmap: BitSet, texture: Int, x: Int, y: Int, width: Int, height: Int) {
        buffer.clear()
        for (i in 0 until width * height)
            buffer.put(if (bitmap[i]) -1 else 0)
        buffer.flip()
        GlStateManager.bindTexture(textures[texture])
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)
    }

    // Creates the necessary textures with all the glyphs in them
    private fun allocateTextures() {
        var currentWidth = 0
        var currentHeight = 0
        var currentMaxHeight = 0
        var texCount = 1
        for (glyph in font.glyphs.values) {
            if (currentWidth + glyph.width < maxTexSize) {
                currentWidth += glyph.width
                currentMaxHeight = maxOf(currentMaxHeight, glyph.height)
            } else {
                if (currentHeight + glyph.height < maxTexSize) {
                    currentWidth = 0
                    currentHeight += currentMaxHeight
                } else {
                    currentWidth = 0
                    currentHeight = 0
                    texCount++
                }
            }
        }

        for (i in 0 until texCount) {
            val texId = GL11.glGenTextures()
            textures = textures + texId
            widthMap = widthMap + (i to maxTexSize)
            heightMap = heightMap + (i to maxTexSize)

            GlStateManager.bindTexture(texId)
            TextureUtil.allocateTexture(texId, maxTexSize, maxTexSize)
        }
    }

    // Getter for the texture indexes, returns -1 if null.
    fun getTextureIndex(c: Char): Int {
        return textureIndex[c] ?: -1
    }

    // Getter for width and height, return -1 if null.
    fun getCharWidth(textureIndex: Int): Int {
        return widthMap[textureIndex] ?: -1
    }

    fun getCharHeight(textureIndex: Int): Int {
        return heightMap[textureIndex] ?: -1
    }

    // Getter for the glyph's X coordinate
    fun getGlyphX(c: Char): Int {
        return glyphPosX[c] ?: -1
    }

    // Getter for the glyph's Y coordinate
    fun getGlyphY(c: Char): Int {
        return glyphPosY[c] ?: -1
    }

    // Getter for the Glyph object
    fun getGlyph(c: Char): IGlyph? {
        return font.glyphs[c] ?: font.glyphs[font.defaultChar]
    }

    // No idea how this works, found on StackOverflow :P This returns the closest greatest power of 2.
    private fun Int.powerOf2(): Int {
        var n = this - 1
        n = n or n.ushr(1)
        n = n or n.ushr(2)
        n = n or n.ushr(4)
        n = n or n.ushr(8)
        n = n or n.ushr(16)
        return if (n < 0) 1 else if (n >= Integer.MAX_VALUE) Integer.MAX_VALUE else n + 1
    }
}