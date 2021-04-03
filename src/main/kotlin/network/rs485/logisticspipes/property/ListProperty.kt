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
import java.util.function.UnaryOperator

abstract class ListProperty<T>(
    protected val list: MutableList<T>,
) : MutableList<T> by list, Property<MutableList<T>> {

    override val propertyObservers: MutableList<ObserverCallback<MutableList<T>>> = mutableListOf()

    abstract fun copyValue(obj: T): T

    override fun copyValue(): MutableList<T> = MutableList(list.size) { idx -> copyValue(list[idx]) }

    fun ensureSize(size: Int, fillWith: () -> T) =
        (size - list.size).takeIf { it > 0 }?.let { repeat(it) { list.add(fillWith()) } }?.alsoIChanged()

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

    override fun readFromNBT(tag: NBTTagCompound) {
        if (tag.hasKey(tagKey)) replaceContent(tag.getIntArray(tagKey))
    }

    override fun writeToNBT(tag: NBTTagCompound) = tag.setIntArray(tagKey, list.toIntArray())

    override fun copyValue(obj: Int): Int = obj

    override fun copyProperty(): IntListProperty = IntListProperty(tagKey = tagKey, list = copyValue())

    fun replaceContent(arr: IntArray) = replaceContent(arr.asList())

    fun replaceContent(col: Collection<Int>) =
        list.takeUnless { it == col.toMutableList() }
            ?.run { clear(); addAll(col) }
            ?.alsoIChanged()

    /**
     * Fills unpopulated entries with zeroes.
     */
    fun ensureSize(size: Int) = ensureSize(size) { 0 }

}
