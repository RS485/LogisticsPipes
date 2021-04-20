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

import java.util.*
import kotlin.streams.toList

open class PropertyLayer(propertiesIn: Collection<Property<*>>) : PropertyHolder {
    private val lowerLayer: List<Property<*>> = propertiesIn.toList()
    private val upperLayer: MutableList<Property<*>?> = MutableList(propertiesIn.size) { null }
    private val changedIndices: BitSet = BitSet(propertiesIn.size)
    private val observersToRemove: MutableMap<Int, ObserverCallback<*>> = mutableMapOf()

    /**
     * A list consisting only of changed [properties][Property] on this [PropertyLayer].
     */
    override val properties: List<Property<*>>
        get() = changedIndices.stream().mapToObj { upperLayer[it]!! }.toList()

    private fun prepareWrite(idx: Int) {
        upperLayer[idx] = lowerLayer[idx].copyProperty().also {
            it.addObserver {
                // set to changed once the copied property is actually changed
                changedIndices.set(idx)
                observersToRemove.remove(idx)?.also { obs ->
                    lowerLayer[idx].propertyObservers.remove(obs)
                    it.addObserver(obs)
                    obs(it)
                }
            }
        }
    }

    private fun lookupIndex(prop: Property<*>, propList: List<Property<*>>): Int =
        propList.indexOfFirst { other -> prop === other }.takeUnless { it == -1 }
            ?: throw IllegalArgumentException("Property <$prop> not in this layer")

    fun <T, P : ValueProperty<T>> overlay(valueProp: P) = ValuePropertyOverlay<T, P>(lookupIndex(valueProp, lowerLayer))
    fun <T, P : Property<T>> overlay(prop: P) = PropertyOverlay<T, P>(lookupIndex(prop, lowerLayer))

    @Suppress("UNCHECKED_CAST")
    fun <T, P : Property<T>> writeProp(prop: P): P {
        val idx = lookupIndex(prop, lowerLayer)
        if (!changedIndices.get(idx)) {
            prepareWrite(idx)
        }
        return upperLayer[idx]!! as P
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, P : Property<T>> addObserver(prop: P, observer: ObserverCallback<T>) {
        val idx = lookupIndex(prop, lowerLayer)
        if (changedIndices.get(idx)) {
            (upperLayer[idx]!! as P).addObserver(observer)
        } else {
            observersToRemove[idx] = observer as ObserverCallback<*>
            (lowerLayer[idx] as P).addObserver(observer)
        }
    }

    fun unregister() = observersToRemove.forEach { lowerLayer[it.key].propertyObservers.remove(it.value) }

    open inner class PropertyOverlay<T, P : Property<T>>(private val idx: Int) {

        @Suppress("UNCHECKED_CAST")
        protected fun lookupRead(): P = if (changedIndices.get(idx)) {
            upperLayer[idx]!! as P
        } else lowerLayer[idx] as P

        @Suppress("UNCHECKED_CAST")
        protected fun lookupWrite(): P {
            if (!changedIndices.get(idx)) {
                prepareWrite(idx)
            }
            return upperLayer[idx]!! as P
        }

        fun <V> read(func: (P) -> V): V = func(lookupRead())

        fun <V> write(func: (P) -> V): V = func(lookupWrite())

    }

    inner class ValuePropertyOverlay<T, P : ValueProperty<T>>(idx: Int) : PropertyOverlay<T, P>(idx) {
        fun get(): T = lookupRead().value

        fun set(value: T) {
            lookupWrite().value = value
        }

    }

}
