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

import akka.io.Tcp
import logisticspipes.LogisticsPipes
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.io.IOException
import java.util.*

object FontParser {

    fun read(resourceLocation: ResourceLocation): BDF? {
        return try {
            read(Minecraft.getMinecraft().resourceManager.getResource(resourceLocation).inputStream.bufferedReader().use { it.readLines() })
        } catch (e: IOException) {
            LogisticsPipes.log.error("Font ${resourceLocation.resourcePath} not found, and will not be rendered.", e)
            null
        }
    }

    fun read(lines: List<String>): BDF {
        val parser = LineParser();
        lines.asSequence().filter { it.isNotBlank() }.withIndex().forEach { (index, line) -> parser.line(index, line) }
        return BDF(glyphs = parser.glyphs, defaultChar = parser.defaultChar)
    }

    private class LineParser {
        var state: State.IState = State.Font.Start

        var chars = 0 // Used to know how many chars are supposed to be on the file
        var gWidth = 0   //
        var gHeight = 0  // Used to store the global bounding box of the font
        var gOffsetX = 0 // This serves as fallback values in case some value is missing in an individual char
        var gOffsetY = 0 //
        var defaultChar: Char = '?'

        lateinit var currentGlyph: Glyph // Used to keep define the current Glyph and later add to the Glyph map
        var glyphs: Map<Char, IGlyph> = emptyMap() // Aforementioned Glyph map

        var currentByte = 0 // Used to keep track of how many bits were read.



        fun line(index: Int, line: String) = when (state) {
            State.Font.Start -> handleFontStart(index, line)
            State.Font.Definition -> handleFontDefinition(index, line)
            State.Char.Pre -> handleCharPre(index, line)
            State.Char.Definition -> handleCharDefinition(index, line)
            State.Char.Bitmap -> handleCharBitmap(index, line)
            State.Font.End -> throw FontParserSyntaxException(state, index, "There must be nothing in the lines following 'ENDFONT'")
            else -> Unit
        }

        private fun handleCharBitmap(index: Int, line: String) {
            val tokens = line.split(" ")
            val head = tokens.first()
            when(head){
                "ENDCHAR" -> {
                    state = State.Char.Pre
                    if(currentByte != currentGlyph.height) throw throw FontParserSyntaxException(state, index, "Current Glyph is missing some Bytes, last byte index is $currentByte, expected: ${currentGlyph.height};")
                    glyphs = glyphs + (currentGlyph.charPoint to currentGlyph)
                }
                else -> {
                    val wordlen = head.length * 4 - 1
                    val data = head.toInt(16)
                    for (i in 0 until currentGlyph.width) {
                        currentGlyph.bitmap[i + currentByte * currentGlyph.width] = data ushr wordlen - i and 1 != 0
                    }
                    currentByte++
                }
            }
        }

        private fun handleCharDefinition(index: Int, line: String) {
            val tokens = line.split(" ")
            when(val head = tokens.first()){
                "SWIDTH" -> Unit

                "ENCODING" -> currentGlyph.charPoint = tokens[1].toInt().toChar()

                "DWIDTH" -> currentGlyph.apply{
                    dWidthX = tokens[1].toInt()
                    dWidthY = tokens[2].toInt()
                }

                "BBX" -> currentGlyph.apply{
                    width = tokens[1].toInt()
                    height = tokens[2].toInt()
                    offsetX = tokens[3].toInt()
                    offsetY = tokens[4].toInt()
                }

                "BITMAP" -> {
                    state = State.Char.Bitmap
                    currentByte = 0
                }

                else -> println("[@$state][Ln$index] Declaration start not recognized: $head")
            }
        }

        private fun handleCharPre(index: Int, line: String) {
            val tokens = line.split(" ")
            when(val head = tokens.first()){
                "STARTCHAR" -> {
                    state = State.Char.Definition
                    currentGlyph = Glyph(tokens[1]).apply {
                        width = gWidth
                        height = gHeight
                        offsetX = gOffsetX
                        offsetY = gOffsetY
                    }
                }

                "ENDFONT" -> {
                    if(chars != glyphs.size) println("[@$state][Ln$index] Expected a different amount of Glyphs, got ${glyphs.size}, expected: $chars")
                    state = State.Font.End
                }

                else -> println("[@$state][Ln$index] Declaration start not recognized: $head")
            }
        }

        private fun handleFontDefinition(index: Int, line: String) {
            val tokens = line.split(" ")
            val head = tokens.first()
            when (head) {
                "SIZE", "STARTPROPERTIES", "FONT_ASCENT", "FONT_DESCENT", "CAP_HEIGHT",
                "COPYRIGHT", "FACE_NAME", "FONT", "FONT_VERSION", "FOUNDRY", "FAMILY_NAME",
                "NOTICE", "POINT_SIZE", "RESOLUTION_X", "RESOLUTION_Y", "SLANT",
                "WEIGHT_NAME", "X_HEIGHT", "ENDPROPERTIES", "COMMENT" -> Unit

                "DEFAULT_CHAR" -> defaultChar = tokens[1].toInt().toChar()

                "FONTBOUNDINGBOX" -> {
                    gWidth = tokens[1].toInt()
                    gHeight = tokens[2].toInt()
                    gOffsetX = tokens[3].toInt()
                    gOffsetY = tokens[4].toInt()
                }

                "CHARS" -> {
                    chars = tokens[1].toInt()
                    state = State.Char.Pre
                }

                else -> println("[@$state][Ln$index] Declaration start not recognized: $head")
            }
        }

        fun handleFontStart(index: Int, line: String) {
            val (token, version) = line.split(" ")
            if (token != "STARTFONT") throw FontParserSyntaxException(state, index, "Expected 'STARTFONT', found: '$token';")
            if (version != "2.1") throw FontParserSyntaxException(state, index, "Only supported version currently is '2.1' , found: '$version';")
            state = State.Font.Definition
        }

    }

    private class Glyph(override val name: String) : IGlyph {
        override var charPoint: Char = '\u0000'
        override var dWidthX: Int = 0
        override var dWidthY: Int = 0
        override var width: Int = 0
        override var height: Int = 0
        override var offsetX: Int = 0
        override var offsetY: Int = 0
        override val bitmap: BitSet = BitSet()
    }

    //public data class BoundingBox(val width: Int, val height: Int, val xOffset: Int, val yOffset: Int)

    enum class State {
        ;

        interface IState

        enum class Font : IState {
            Start, //      (Starts @BOF, Ends @STARTFONT) Expects the start of the font with th version "2.1"
            Definition, // (Starts @STARTFONT, Ends @CHARS) Defines the global properties
            End; //        (Starts @ENDFONT, Ends @EOF) Anything after will throw an exception
        }

        enum class Char : IState {
            Pre, //        (Starts @CHARS, Ends @STARTCHAR) // Is set before every STARTCHAR
            Definition, // (Starts @STARTCHAR, Ends @BITMAP) // Is set while setting the char's properties
            Bitmap, //     (Starts @BITMAP, Ends @ENDCHAR) // Is set while setting the bitmap
        }
    }

    class FontParserSyntaxException(val state: State.IState, val line: Int, val s: String)
        : RuntimeException("Syntax exception on line $line (state $state): $s")
}