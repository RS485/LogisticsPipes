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

package network.rs485.logisticspipes.pipe.shape

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.Direction.Axis.*
import kotlin.math.roundToInt

data class RotatedPipeShape<X>(
        val wrapped: PipeShape<X>,
        val axis: Axis,
        val angle: AxisFixedAngle
) : PipeShape<X> {

    override fun getBlocks(): Set<BlockPos> {
        return wrapped.getBlocks().map(::rotatePos).toSet()
    }

    override fun getPorts(): Map<X, BlockFace> {
        return wrapped.getPorts().mapValues { (_, v) -> BlockFace(rotatePos(v.pos), rotateDirection(v.side)) }
    }

    private fun rotatePos(pos: BlockPos): BlockPos {
        TODO("not implemented")
    }

    private fun rotateDirection(dir: Direction): Direction {
        return lookupTable[dir.id or (axis.ordinal shl 3) or (angle.ordinal shl 5)]
    }

    companion object {
        private val lookupTable = Array(0b10000000) { data ->
            val d = byId(data and 0b0000111)
            val axis = Axis.values()[data shr 3 and 0b0011 % 3]
            val angle = data shr 5 and 0b11

            val list = when (axis) {
                X -> listOf(DOWN, SOUTH, UP, NORTH)
                Y -> listOf(WEST, SOUTH, EAST, NORTH)
                Z -> listOf(DOWN, EAST, UP, WEST)
            }

            if (d in list) list[angle + list.indexOf(d) % 4] else d
        }
    }

}

enum class AxisFixedAngle {
    _0,
    _90,
    _180,
    _270;

    companion object {
        fun from(angle: Int): AxisFixedAngle {
            val a = (((angle / 90f).roundToInt() % 4) + 4) % 4
            return when (a) {
                0 -> _0
                1 -> _90
                2 -> _180
                3 -> _270
                else -> error("unreachable")
            }
        }
    }
}

data class BlockFace(val pos: BlockPos, val side: Direction) {
    val opposite
        get() = BlockFace(pos.offset(side), side.opposite)
}