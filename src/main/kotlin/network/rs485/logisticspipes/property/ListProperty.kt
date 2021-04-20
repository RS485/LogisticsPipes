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
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.UnaryOperator

abstract class ListProperty<T>(
    protected val list: MutableList<T>,
) : MutableList<T> by list, Property<MutableList<T>> {

    companion object {
        private const val SIZE_NAME = "listSize"
        private const val ITEM_NAME = "listItem"

        fun sizeTagKey(tagKey: String) = if (tagKey.isEmpty()) SIZE_NAME else "$tagKey.$SIZE_NAME"
        fun itemTagKey(tagKey: String, idx: Int) =
            if (tagKey.isEmpty()) "$ITEM_NAME.$idx" else "$tagKey.$ITEM_NAME.$idx"
    }

    override val propertyObservers: CopyOnWriteArraySet<ObserverCallback<MutableList<T>>> = CopyOnWriteArraySet()

    fun ensureSize(size: Int, fillWith: (Int) -> T) =
        (size - list.size).takeIf { it > 0 }?.let { repeat(it) { list.add(fillWith(list.size)) } }?.alsoIChanged()

    fun ensureSize(size: Int) = ensureSize(size) { defaultValue(it) }

    fun replaceContent(col: Collection<T>) =
        list.takeUnless { it == col.toMutableList() }
            ?.run { clear(); addAll(col) }
            ?.alsoIChanged()

    fun replaceContent(arr: Array<T>) = replaceContent(arr.asList())

    abstract fun defaultValue(idx: Int): T

    override fun readFromNBT(tag: NBTTagCompound) {
        if (tag.hasKey(sizeTagKey(tagKey))) {
            replaceContent(
                MutableList(tag.getInteger(sizeTagKey(tagKey))) { idx ->
                    if (tag.hasKey(itemTagKey(tagKey, idx))) {
                        readSingleFromNBT(tag, itemTagKey(tagKey, idx))
                    } else defaultValue(idx)
                }
            )
        }
    }

    abstract fun readSingleFromNBT(tag: NBTTagCompound, key: String): T

    override fun writeToNBT(tag: NBTTagCompound) {
        tag.setInteger(sizeTagKey(tagKey), list.size)
        list.withIndex().forEach { writeSingleToNBT(tag, itemTagKey(tagKey, it.index), it.value) }
    }

    abstract fun writeSingleToNBT(tag: NBTTagCompound, key: String, value: T)

    abstract fun copyValue(obj: T): T

    override fun copyValue(): MutableList<T> = MutableList(list.size) { idx -> copyValue(list[idx]) }

    override fun add(element: T): Boolean = list.add(element).alsoIChanged()

    override fun add(index: Int, element: T) = list.add(index, element).alsoIChanged()

    override fun addAll(index: Int, elements: Collection<T>): Boolean = list.addAll(index, elements).alsoIChanged()

    override fun addAll(elements: Collection<T>): Boolean = list.addAll(elements).alsoIChanged()

    override fun clear() = list.clear().alsoIChanged()

    override fun remove(element: T): Boolean = list.remove(element).alsoIChanged()

    override fun removeAll(elements: Collection<T>): Boolean = list.removeAll(elements).alsoIChanged()

    override fun removeAt(index: Int): T = list.removeAt(index).alsoIChanged()

    override fun replaceAll(operator: UnaryOperator<T>) = list.replaceAll(operator).alsoIChanged()

    override fun retainAll(elements: Collection<T>): Boolean = list.retainAll(elements).alsoIChanged()

    override fun set(index: Int, element: T): T = list.set(index, element).alsoIChanged()

    override fun sort(c: Comparator<in T>) = list.sortWith(c).alsoIChanged()

    override fun listIterator(): MutableListIterator<T> =
        TODO("Returned MutableListIterator needs to inform of changes")

    override fun listIterator(index: Int): MutableListIterator<T> =
        TODO("Returned MutableListIterator needs to inform of changes")

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> =
        TODO("Returned sub list needs to inform of changes")

}

class IntListProperty : ListProperty<Int> {

    override val tagKey: String

    constructor(tagKey: String) : super(mutableListOf()) {
        this.tagKey = tagKey
    }

    private constructor(tagKey: String, list: MutableList<Int>) : super(list) {
        this.tagKey = tagKey
    }

    fun replaceContent(arr: IntArray) = replaceContent(arr.asList())

    fun getArray(): IntArray = list.toIntArray()

    fun increase(index: Int, by: Int) = set(index, get(index) + by)

    override fun defaultValue(idx: Int): Int = 0

    override fun readFromNBT(tag: NBTTagCompound) {
        if (tag.hasKey(tagKey)) replaceContent(tag.getIntArray(tagKey))
    }

    override fun writeToNBT(tag: NBTTagCompound) = tag.setIntArray(tagKey, list.toIntArray())

    override fun copyValue(obj: Int): Int = obj

    override fun copyProperty(): IntListProperty = IntListProperty(tagKey = tagKey, list = copyValue())

    override fun readSingleFromNBT(tag: NBTTagCompound, key: String): Int = tag.getInteger(key)

    override fun writeSingleToNBT(tag: NBTTagCompound, key: String, value: Int) = tag.setInteger(key, value)

}

class StringListProperty : ListProperty<String> {

    override val tagKey: String

    constructor(tagKey: String) : super(mutableListOf()) {
        this.tagKey = tagKey
    }

    private constructor(tagKey: String, list: MutableList<String>) : super(list) {
        this.tagKey = tagKey
    }

    override fun defaultValue(idx: Int): String = ""

    override fun readSingleFromNBT(tag: NBTTagCompound, key: String): String = tag.getString(key)

    override fun writeSingleToNBT(tag: NBTTagCompound, key: String, value: String) = tag.setString(key, value)

    override fun copyValue(obj: String): String = obj

    override fun copyProperty(): StringListProperty = StringListProperty(tagKey = tagKey, list = copyValue())

}
