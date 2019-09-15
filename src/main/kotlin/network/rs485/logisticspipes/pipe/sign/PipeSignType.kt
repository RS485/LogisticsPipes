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

package network.rs485.logisticspipes.pipe.sign

import logisticspipes.pipes.basic.CoreRoutedPipe
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.SystemUtil
import net.minecraft.util.math.Direction
import network.rs485.logisticspipes.init.Registries
import kotlin.reflect.KClass

abstract class PipeSignType<T : PipeSign> {

    private var translationKey: String? = null

    abstract fun create(pipe: CoreRoutedPipe, side: Direction): T

    abstract fun isAllowedFor(pipe: CoreRoutedPipe): Boolean

    protected fun getOrCreateTranslationKey(): String {
        return translationKey ?: run {
            val key = SystemUtil.createTranslationKey("pipe_sign_type", Registries.SignType.getId(this));
            translationKey = key
            key
        }
    }

    open fun getTranslationKey(): String {
        return getOrCreateTranslationKey()
    }

    open fun getName(): Text {
        return TranslatableText(getTranslationKey())
    }

    class Builder<T : PipeSign>(private val constructor: (pipe: CoreRoutedPipe, side: Direction) -> T) {

        private val list = mutableListOf<KClass<out CoreRoutedPipe>>()
        private var whitelist = false

        fun allowAll(): Builder<T> {
            list.clear()
            whitelist = false
            return this
        }

        fun denyAll(): Builder<T> {
            list.clear()
            whitelist = true
            return this
        }

        fun allow(cls: Class<out CoreRoutedPipe>) = allow(cls.kotlin)

        fun allow(cls: KClass<out CoreRoutedPipe>): Builder<T> {
            if (whitelist) list += cls
            return this
        }

        fun deny(cls: Class<out CoreRoutedPipe>) = deny(cls.kotlin)

        fun deny(cls: KClass<out CoreRoutedPipe>): Builder<T> {
            if (!whitelist) list += cls
            return this
        }

        fun build(): PipeSignType<T> = object : PipeSignType<T>() {
            override fun create(pipe: CoreRoutedPipe, side: Direction): T = constructor(pipe, side)

            override fun isAllowedFor(pipe: CoreRoutedPipe): Boolean {
                return list.any { it.isInstance(pipe) } == whitelist
            }
        }
    }

}