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

package network.rs485.logisticspipes.transport

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

interface Pipe<P : CellPath, X> {

    /**
     * How fast cells flow through this pipe. 1.0 is normal speed
     */
    @JvmDefault
    fun getSpeedFactor(): Float = 1.0f

    /**
     * Gets called when a cell is requested to enter this pipe.
     * Insert the cell into this pipe properly here, with the correct path.
     */
    fun onEnterPipe(network: PipeNetwork, from: X, cell: Cell<*>)

    /**
     * Gets called when a cell has reached the end of its path.
     */
    fun onFinishPath(network: PipeNetwork, path: P, cell: Cell<*>)

    @JvmDefault
    fun canConnectTo(port: X, other: Pipe<*, *>) = true

    @JvmDefault
    fun onConnectTo(port: X, other: Pipe<*, *>) {
    }

    @JvmDefault
    fun onDisconnect(port: X, other: Pipe<*, *>) {
    }

    @JvmDefault
    fun onJoinNetwork(network: PipeNetwork) {
    }

    @JvmDefault
    fun onLeaveNetwork() {
    }

    fun toTag(tag: CompoundTag = CompoundTag()): CompoundTag

    fun fromTag(tag: CompoundTag)

    fun getTagFromPort(port: X): Tag

    fun getPortFromTag(tag: Tag): X

    fun getTagFromPath(path: P): Tag

    fun getPathFromTag(tag: Tag): P

}