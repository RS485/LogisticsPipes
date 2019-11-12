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
import network.rs485.logisticspipes.pipe.shape.AxisFixedAngle.*
import kotlin.math.roundToInt

data class RotatedPipeShape<X>(
        val wrapped: PipeShape<X>,
        val axis: Axis,
        val angle: AxisFixedAngle
) : PipeShape<X> {

    override val ports: Map<X, BlockFace>
        get() = wrapped.ports.mapValues { (_, v) -> BlockFace(rotatePos(v.pos), rotateDirection(v.side)) }

    private fun rotatePos(pos: BlockPos): BlockPos {
        if (angle == _0) return pos

        return when (axis) {
            X -> when (angle) {
                _0 -> pos
                _90 -> BlockPos(pos.x, pos.y, -pos.z)
                _180 -> BlockPos(pos.x, -pos.y, -pos.z)
                _270 -> BlockPos(pos.x, -pos.y, pos.z)
            }
            Y -> when (angle) {
                _0 -> pos
                _90 -> BlockPos(-pos.x, pos.y, pos.z)
                _180 -> BlockPos(-pos.x, pos.y, -pos.z)
                _270 -> BlockPos(pos.x, pos.y, -pos.z)
            }
            Z -> when (angle) {
                _0 -> pos
                _90 -> BlockPos(-pos.x, pos.y, pos.z)
                _180 -> BlockPos(-pos.x, -pos.y, pos.z)
                _270 -> BlockPos(pos.x, -pos.y, -pos.z)
            }
        }
    }

    private fun rotateDirection(dir: Direction): Direction {
        val list = when (axis) {
            X -> listOf(DOWN, SOUTH, UP, NORTH)
            Y -> listOf(WEST, SOUTH, EAST, NORTH)
            Z -> listOf(DOWN, EAST, UP, WEST)
        }

        return if (dir in list) list[(angle.ordinal + list.indexOf(dir)) % 4] else dir
    }
}

@Suppress("EnumEntryName")
enum class AxisFixedAngle {
    _0,
    _90,
    _180,
    _270;

    companion object {
        fun from(angle: Int): AxisFixedAngle {
            return when (((angle / 90f).roundToInt() % 4 + 4) % 4) {
                0 -> _0
                1 -> _90
                2 -> _180
                3 -> _270
                else -> error("unreachable")
            }
        }
    }
}