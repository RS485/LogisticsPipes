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

package network.rs485.logisticspipes.pipe

import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import network.rs485.logisticspipes.transport.Cell
import network.rs485.logisticspipes.transport.CellContent
import network.rs485.logisticspipes.transport.Pipe
import network.rs485.logisticspipes.transport.PipeNetwork

class HighSpeedPipe(val pathConstructor: (BiPort) -> HighSpeedPath, val itf: WorldInterface) : Pipe<HighSpeedPath, BiPort> {

    override fun onEnterPipe(network: PipeNetwork, from: BiPort, cell: Cell<*>) {
        network.insert(cell, this, pathConstructor(from))
    }

    override fun onFinishPath(network: PipeNetwork, path: HighSpeedPath, cell: Cell<*>) {
        // The cell has reached the end of the pipe.
        val nextPipe = network.getConnectedPipe(this, path.from.opposite)
        if (nextPipe != null) {
            // If there's a pipe connected to this one at the side the item is supposed to come out of (which it should), put it in there
            network.insertFrom(cell, this, path.from.opposite)
        } else {
            // Otherwise, again, drop the item.
            val content = network.untrack(cell)
            itf.dropItem(content, path.from.opposite)
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        return tag
    }

    override fun fromTag(tag: CompoundTag) {
    }

    override fun getTagFromPort(port: BiPort): Tag {
        return when (port) {
            BiPort.SIDE_1 -> ByteTag.of(0)
            BiPort.SIDE_2 -> ByteTag.of(1)
        }
    }

    override fun getPortFromTag(tag: Tag): BiPort {
        return if (tag is ByteTag && tag.int == 0) BiPort.SIDE_1
        else BiPort.SIDE_2
    }

    override fun getTagFromPath(path: HighSpeedPath): Tag {
        return getTagFromPort(path.from)
    }

    override fun getPathFromTag(tag: Tag): HighSpeedPath {
        return pathConstructor(getPortFromTag(tag))
    }

    interface WorldInterface {

        fun dropItem(content: CellContent, port: BiPort)

    }
}