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

package network.rs485.logisticspipes.integration

import network.rs485.grow.Coroutines
import network.rs485.util.checkBooleanProperty
import logisticspipes.LogisticsPipes
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.event.FMLServerStartedEvent
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import java.lang.management.ManagementFactory
import java.time.Duration
import kotlin.test.assertTrue
import kotlinx.coroutines.*
import kotlinx.coroutines.time.withTimeout

@Suppress("unused", "MemberVisibilityCanBePrivate")
object MinecraftTest {

    /**
     * If not debugging, the server watch dog is not disabled and the server is shut down after running the tests.
     */
    private val isDebugging = checkBooleanProperty("logisticspipes.test.debug")

    private lateinit var world: WorldServer
    private lateinit var firstBlockPos: BlockPos
    private lateinit var testBlockBuilder: TestWorldBuilder

    const val TIMEOUT_MODIFIER: Long = 1L

    fun serverStart(event: FMLServerStartedEvent) {
        assertTrue(message = "Test suite must run on the server") { event.side.isServer }
        val serverInstance = FMLCommonHandler.instance().minecraftServerInstance as DedicatedServer
        world = serverInstance.worlds[0]
        firstBlockPos = BlockPos(0, LEVEL, 0)
        if (isDebugging) {
            serverInstance.setProperty("max-tick-time", 0L)
            serverInstance.saveProperties()
            val threadmxbean = ManagementFactory.getThreadMXBean()
            val athreadinfo = threadmxbean.dumpAllThreads(true, true)
            val watchdog = athreadinfo.find { it.threadName == "Server Watchdog" }
            if (watchdog != null) error("Watchdog already running! Set max-tick-time to 0, please restart the server!")

            // set rules for spawning players without annoying stuff
            world.spawnPoint = firstBlockPos
            world.gameRules.setOrCreateGameRule("spawnRadius", "0")
            world.gameRules.setOrCreateGameRule("doDaylightCycle", "false")
            world.gameRules.setOrCreateGameRule("doWeatherCycle", "false")
            world.worldTime = 5000
            world.worldInfo.cleanWeatherTime = 15000
            world.worldInfo.rainTime = 0
            world.worldInfo.thunderTime = 0
            world.worldInfo.isRaining = false
            world.worldInfo.isThundering = false
        }
        val task = startTests(LogisticsPipes.log::info)
        task.invokeOnCompletion {
            if (it != null) throw it
            repeat(3) {
                println("All Tests done.")
            }
            if (!isDebugging) serverInstance.initiateShutdown()
        }
    }

    fun startTests(logger: (Any) -> Unit) =
        Coroutines.serverScope.launch(CoroutineName("logisticspipes.test")) {
            delay(Duration.ofSeconds(1 * TIMEOUT_MODIFIER).toMillis())
            println("[STARTING LOGISTICSPIPES TESTS]")
            withTimeout(Duration.ofMinutes(3)) {
                testBlockBuilder = TestWorldBuilder(world, firstBlockPos)
                world.spawnPoint = testBlockBuilder.buildSpawnPlatform()
                listOf(
                    async {
                        CraftingTest.`test fuzzy-input crafting succeeds multi-request with mixed input OreDict`(
                            loggerIn = logger,
                            selector = testBlockBuilder.newSelector(),
                        )
                    },
                    async {
                        CraftingTest.`test fuzzy-input crafting succeeds with mixed input OreDict`(
                            loggerIn = logger,
                            selector = testBlockBuilder.newSelector(),
                        )
                    },
                    async {
                        CraftingTest.`test fuzzy-input crafting succeeds multi-request with sufficient mixed input OreDict`(
                            loggerIn = logger,
                            selector = testBlockBuilder.newSelector(),
                        )
                    },
                    async {
                        CraftingTest.`test fuzzy-input crafting succeeds with sufficient mixed input OreDict`(
                            loggerIn = logger,
                            selector = testBlockBuilder.newSelector(),
                        )
                    },
                ).awaitAll()
            }
        }

}
