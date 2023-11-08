/*
 * Copyright (c) 2020  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2020  RS485
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

package network.rs485.logisticspipes.module

import network.rs485.grow.Coroutines
import logisticspipes.LogisticsPipes
import logisticspipes.modules.LogisticsModule
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntity
import java.time.Duration
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.time.withTimeout

abstract class AsyncModule<S, C> : LogisticsModule() {
    private var currentJob: Deferred<C?>? = null

    /**
     * Represents the wait time in ticks until the next job is started.
     */
    open val everyNthTick: Int = 20

    /**
     * A debug helper for adding the connected [TileEntity] to error
     * information. May return null, if the information is not
     * available.
     */
    private val connectedEntity: TileEntity?
        get() = _service?.availableAdjacent?.inventories()?.firstOrNull()?.tileEntity

    @ExperimentalCoroutinesApi
    override fun tick() {
        val currentJobTemp = currentJob
        if (currentJobTemp?.isActive == true) {
            runSyncWork()
        } else {
            if (currentJobTemp?.isCompleted == true) {
                try {
                    runSyncWork()
                    completeJob(currentJobTemp)
                } finally {
                    currentJob = null
                }
            }

            if (_service?.isNthTick(everyNthTick) == true) {
                val setup = jobSetup()
                currentJob = Coroutines.asynchronousScope.async {
                    try {
                        return@async withTimeout(Duration.ofSeconds(90)) {
                            tickAsync(setup)
                        }
                    } catch (e: RuntimeException) {
                        val isGamePaused = world?.isRemote == false && Minecraft.getMinecraft().isGamePaused
                        if (e !is TimeoutCancellationException && !isGamePaused) {
                            val connected = connectedEntity?.let { " connected to $it at ${it.pos}" } ?: ""
                            LogisticsPipes.log.error("Error in ticking async module $module$connected", e)
                        }
                    }
                    return@async null
                }
            }
        }
    }

    /**
     * Setup function which is run on every new module tick.
     *
     * @return setup object S which is passed to [tickAsync].
     */
    abstract fun jobSetup(): S

    /**
     * Completion function that is run after every successful module
     * tick and all asynchronous work is done.
     *
     * @param deferred the [Deferred] of the asynchronous work.
     */
    abstract fun completeJob(deferred: Deferred<C?>)

    /**
     * Asynchronous tick function that is run with a timeout. Takes the
     * setup object and returns an object for [completeJob].
     *
     * @param setupObject the setup object from [jobSetup].
     * @return a completion object for [completeJob].
     */
    abstract suspend fun tickAsync(setupObject: S): C

    /**
     * Runs every tick while the [current async task][currentJob] is active.
     */
    abstract fun runSyncWork()
}
