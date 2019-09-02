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

package network.rs485.debug

import java.time.Duration

class PerformanceMeter(val label: String, private val meanCalcCallCount: Int, private val printToConsole: Boolean) {
    private val meanCalcGroup: LongArray = LongArray(meanCalcCallCount)
    private var currentGroupIndex: Int = 0

    val sum: Duration
        get() {
            val durationSum = meanCalcGroup.sum()
            return if (durationSum == 0L) Duration.ZERO else Duration.ofNanos(durationSum)
        }

    val calculatedMean: Duration
        get() = sum.dividedBy(meanCalcCallCount.toLong())

    private fun increaseGroupIndex() {
        currentGroupIndex++
        if (currentGroupIndex >= meanCalcCallCount) {
            currentGroupIndex = 0

            if (printToConsole) {
                println("[PerformanceMeter.$label] Sum: $sum of $meanCalcCallCount measures - Mean time: $calculatedMean")
            }
        }
    }

    fun newPerfValue(nanoDuration: Long) {
        meanCalcGroup[currentGroupIndex] = nanoDuration
        increaseGroupIndex()
    }

    fun newPerfValue(duration: Duration) {
        newPerfValue(duration.toNanos())
    }
}
