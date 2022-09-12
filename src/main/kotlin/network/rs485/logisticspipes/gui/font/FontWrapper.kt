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

import logisticspipes.LogisticsPipes
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureUtil
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.max

private val buffer = ByteBuffer.allocateDirect(10000000).asIntBuffer()

class FontWrapper(private val font: IFont) {
    val textures: List<Int> get() = textureIndex.keys.toList()

    private var glyphPosX: Map<Char, Int> = emptyMap()
    private var glyphPosY: Map<Char, Int> = emptyMap()
    private var textureIndex: Map<Int, CharRange> = emptyMap()

    val fontWidth: Int get() = font.width
    val fontHeight: Int get() = font.height
    val fontXOffset: Int get() = font.offsetX
    val fontYOffset: Int get() = font.offsetY
    val fontLineOffset: Int get() = fontHeight + fontYOffset

    val defaultChar = font.defaultChar

    private val maxTexSize = 512

    init {
        allocateTextures()
    }

    private fun setTexture(bitmap: BitSet, texture: Int, x: Int, y: Int, width: Int, height: Int) {
        buffer.clear()
        for (i in 0 until width * height)
            buffer.put(if (bitmap[i]) -1 else 0)
        buffer.flip()
        GlStateManager.bindTexture(texture)
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)
    }

    // Creates the necessary textures with all the glyphs in them
    private fun allocateTextures(): Pair<Int, Int> {
        // TODO change character-texture map to texture-IntRange relation.
        // TODO allocate texture and write it on the same pass.

        fun generateTexture(): Int{
            val texId = GL11.glGenTextures()
            GlStateManager.bindTexture(texId)
            TextureUtil.allocateTexture(texId, maxTexSize, maxTexSize)
            LogisticsPipes.log.warn("Created new texture: $texId")
            return texId
        }

        var currentWidth = 0
        var currentHeight = 0
        var currentRowMaxHeight = 0
        var currentMaxHeight = 0
        var currentMinOffset = 0
        var currentTexId = 0
        val firstChar = font.glyphs.keys.first()
        var startingChar = firstChar

        fun startNewRow(){
            // Use gathered row data to keep track of highest character.
            currentMaxHeight = max(currentMaxHeight, currentRowMaxHeight)
            // Use gathered row data to safely start the next row with interfering with the previous ones.
            currentHeight += currentRowMaxHeight.powerOf2()
            // Reset row variables.
            currentWidth = 0
            currentRowMaxHeight = 0
        }

        fun addTextureRange(character: Char) {
            textureIndex = textureIndex + (currentTexId to (startingChar..character))
            LogisticsPipes.log.warn("Finished texture, covering characters ${startingChar.toInt()}..${character.toInt()}")
        }

        fun startNewTexture(character: Char) {
            if(character != firstChar){
                // Store texture-character relation
                addTextureRange(character)
            }
            startingChar = character
            // Reset texture positional characters.
            currentWidth = 0
            currentHeight = 0
            // Create a new texture and get it's id.
            currentTexId = generateTexture()
        }

        fun allocateGlyph(character: Char, glyph: IGlyph){
            if(currentWidth + glyph.width > maxTexSize)
                error("Cannot write a texture of width=${glyph.width} at x=$currentWidth")
            if(currentHeight + glyph.height > maxTexSize)
                error("Cannot write a texture of height=${glyph.height} at y=$currentHeight")
            // Store the location of the glyph
            glyphPosX = glyphPosX + (character to currentWidth)
            glyphPosY = glyphPosY + (character to currentHeight)
            // Draw the glyph into the texture
            setTexture(glyph.bitmap, currentTexId, currentWidth, currentHeight, glyph.width, glyph.height)
            // After allocation
            currentWidth += glyph.width.powerOf2()
            currentRowMaxHeight = maxOf(currentRowMaxHeight, glyph.height)
            currentMinOffset = minOf(currentMinOffset, glyph.offsetY)
        }

        fun start() {
            startNewTexture(font.glyphs.keys.first())
        }

        fun finish(){
            val lastChar = font.glyphs.keys.last()
            if(startingChar != lastChar){
                addTextureRange(lastChar)
            }
        }

        start()
        for ((character, glyph) in font.glyphs) {
            if(currentWidth + glyph.width > maxTexSize) startNewRow()
            if(currentHeight + glyph.height > maxTexSize) startNewTexture(character)
            allocateGlyph(character, glyph)
        }
        finish()

        return currentMaxHeight to currentMinOffset
    }

    // Getter for the texture indexes, returns -1 if null.
    fun getTextureIndex(c: Char): Int = textureIndex.entries.find { entry -> entry.value.contains(c) }?.key ?: -1

    // Getter for width, return -1 if char not found.
    fun getFontTextureSize(): Int = maxTexSize

    // Getter for the glyph's X coordinate
    fun getGlyphX(c: Char): Int = glyphPosX[c] ?: -1

    // Getter for the glyph's Y coordinate
    fun getGlyphY(c: Char): Int = glyphPosY[c] ?: -1

    // Getter for the Glyph object
    fun getGlyph(c: Char): IGlyph? = font.glyphs[c] ?: font.glyphs[font.defaultChar]

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