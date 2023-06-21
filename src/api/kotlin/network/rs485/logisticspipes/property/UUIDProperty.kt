/*
 * Copyright (c) 2021  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2021  RS485
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

package network.rs485.logisticspipes.property

import net.minecraft.nbt.NBTTagCompound
import java.util.*

private val zero = UUID(0L, 0L)

fun isZero(uuid: UUID) = uuid == zero

class UUIDProperty(initialValue: UUID?, override val tagKey: String) : ValueProperty<UUID>(initialValue ?: zero) {

    override fun readFromNBT(tag: NBTTagCompound) {
        // FIXME after 1.12: remove support for empty string
        if (tag.hasKey(tagKey)) tag.getString(tagKey).takeUnless(String::isEmpty)?.also { value = UUID.fromString(it) }
    }

    override fun writeToNBT(tag: NBTTagCompound) = tag.setString(tagKey, value.toString())

    override fun copyValue(): UUID = value

    override fun copyProperty(): UUIDProperty = UUIDProperty(copyValue(), tagKey)

    fun isZero() = isZero(value)

    fun zero() {
        value = zero
    }

}

class UUIDListProperty : ListProperty<UUID> {

    override val tagKey: String

    constructor(tagKey: String) : super(mutableListOf()) {
        this.tagKey = tagKey
    }

    private constructor(tagKey: String, list: MutableList<UUID>) : super(list) {
        this.tagKey = tagKey
    }

    override fun defaultValue(idx: Int): UUID = zero

    override fun readSingleFromNBT(tag: NBTTagCompound, key: String): UUID = UUID.fromString(tag.getString(key))

    override fun writeSingleToNBT(tag: NBTTagCompound, key: String, value: UUID) = tag.setString(key, value.toString())

    // UUID objects are immutable
    override fun copyValue(obj: UUID): UUID = obj

    override fun copyProperty(): UUIDListProperty = UUIDListProperty(tagKey, list)

    fun isZero(idx: Int) = isZero(get(idx))

    fun zero(idx: Int) = set(idx, zero)

}
