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

package network.rs485.logisticspipes.util

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import network.rs485.logisticspipes.LogisticsPipes
import network.rs485.logisticspipes.init.Registries

class TypedMutableMap(val wrapped: MutableMap<SerializableKey<*>, Any> = mutableMapOf()) {

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: SerializableKey<T>): T? {
        return wrapped[key] as T?
    }

    fun <T : Any> get(key: SerializableKey<T>, ctor: () -> T): T {
        return this[key] ?: ctor().also { this[key] = it }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getValue(key: SerializableKey<T>): T {
        return wrapped.getValue(key) as T
    }

    operator fun <T : Any> set(key: SerializableKey<T>, value: T?) {
        if (value == null) {
            wrapped -= key
        } else {
            wrapped[key] = value
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> remove(key: SerializableKey<T>): T? {
        return wrapped.remove(key) as T?
    }

    operator fun contains(key: SerializableKey<*>) = key in wrapped

    @Suppress("UNCHECKED_CAST")
    fun toTag(tag: CompoundTag = CompoundTag()): CompoundTag {
        return wrapped.entries.fold(tag) { acc, (k, v) -> acc.apply { put(Registries.SerializableKey.getId(k)!!.toString(), (k as SerializableKey<Any>).toTag(v)) } }
    }

    companion object {
        fun fromTag(tag: CompoundTag): TypedMutableMap {
            val wrapped = mutableMapOf<SerializableKey<*>, Any>()

            wrapped += tag.keys.mapNotNull {
                val id = Identifier(it)
                val sk = Registries.SerializableKey[id]
                if (sk == null) {
                    LogisticsPipes.logger.warn("Ignoring unknown key '$id' in map")
                    null
                } else {
                    Pair(sk, sk.fromTag(tag[it]!!))
                }
            }

            return TypedMutableMap(wrapped)
        }
    }

}