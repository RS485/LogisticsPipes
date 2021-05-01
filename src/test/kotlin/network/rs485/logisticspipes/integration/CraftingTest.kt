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

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.time.withTimeoutOrNull
import logisticspipes.LPBlocks
import logisticspipes.LPItems
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity
import logisticspipes.pipes.PipeItemsCraftingLogistics
import logisticspipes.pipes.PipeItemsRequestLogistics
import logisticspipes.pipes.upgrades.FuzzyUpgrade
import logisticspipes.pipes.upgrades.UpgradeManager
import logisticspipes.request.RequestLog
import logisticspipes.request.RequestTree
import logisticspipes.request.resources.IResource
import logisticspipes.routing.order.LinkedLogisticsOrderList
import logisticspipes.utils.item.ItemIdentifierStack
import net.minecraft.block.BlockChest
import net.minecraft.block.BlockPlanks
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.EnumFacing
import network.rs485.logisticspipes.integration.MinecraftTest.TIMEOUT_MODIFIER
import network.rs485.logisticspipes.util.FuzzyFlag
import network.rs485.logisticspipes.util.FuzzyUtil
import network.rs485.minecraft.BlockPlacer
import network.rs485.minecraft.BlockPosSelector
import network.rs485.minecraft.TestState
import network.rs485.minecraft.configurator
import java.time.Duration
import kotlin.test.assertTrue
import kotlin.test.fail

@Suppress("FunctionName")
object CraftingTest {
    private val fuzzyUpgradeItem = Item.REGISTRY.getObject(LPItems.upgrades[FuzzyUpgrade.getName()])!!

    suspend fun `test fuzzy-input crafting succeeds multi-request with mixed input OreDict`(
        loggerIn: (Any) -> Unit,
        selector: BlockPosSelector,
    ) {
        val testName = Throwable().stackTrace[0].methodName
        val logger = { msg: Any -> loggerIn("$testName $msg") }
        try {
            val (requesterPipe, chest) = `setup fuzzy crafting chest`(
                selector = selector,
                providerStacks = arrayOf(
                    ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.OAK.metadata),
                    ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.DARK_OAK.metadata),
                ),
            )
            selector.finalize()
            delay(5000) // FIXME: delay and pipe update should not be needed
            requesterPipe.pipe.router.update(true, requesterPipe.pipe)
            val requestSuccessful = CompletableDeferred<Boolean>()
            val stacks = listOf(ItemStack(Blocks.CHEST))
            RequestTree.request(
                stacks.map { ItemIdentifierStack.getFromStack(it) }.toList(),
                requesterPipe.pipe,
                object : RequestLog {
                    override fun handleMissingItems(resources: List<IResource>) {
                        fail("Request should have succeeded. Missing items: $resources")
                    }

                    override fun handleSucessfullRequestOf(item: IResource, parts: LinkedLogisticsOrderList) {
                        requestSuccessful.complete(true)
                    }

                    override fun handleSucessfullRequestOfList(
                        resources: List<IResource>,
                        parts: LinkedLogisticsOrderList
                    ) {
                        requestSuccessful.complete(true)
                    }
                },
                RequestTree.defaultRequestFlags.clone().apply {
                    add(RequestTree.ActiveRequestType.LogMissing)
                },
                null,
            )
            val requestWasSuccessful = withTimeoutOrNull(Duration.ofSeconds(1 * TIMEOUT_MODIFIER)) {
                requestSuccessful.await()
            }
            assertTrue(message = "the request was not reported successful") {
                requestWasSuccessful == true
            }
            val waitForChestResult = waitForOrNull(
                timeout = Duration.ofSeconds(3 * TIMEOUT_MODIFIER),
                check = { chest.getTileEntity<TileEntityChest>().containsAll(stacks) },
            )
            assertTrue(message = "a chest item was not found before a timeout") {
                waitForChestResult !== null
            }
            selector.setVisibleState(TestState.SUCCESS)
            logger("[PASSED]")
        } catch (e: Throwable) {
            selector.setVisibleState(TestState.FAIL)
            logger("[FAILED]\n==> ${e.stackTraceToString()}")
        }
    }

    suspend fun `test fuzzy-input crafting succeeds with mixed input OreDict`(
        loggerIn: (Any) -> Unit,
        selector: BlockPosSelector,
    ) {
        val testName = Throwable().stackTrace[0].methodName
        val logger = { msg: Any -> loggerIn("$testName $msg") }
        try {
            val (requesterPipe, chest) = `setup fuzzy crafting chest`(
                selector = selector,
                providerStacks = arrayOf(
                    ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.OAK.metadata),
                    ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.DARK_OAK.metadata),
                ),
            )
            selector.finalize()
            delay(5000)
            requesterPipe.pipe.router.update(true, requesterPipe.pipe)
            val requestSuccessful = CompletableDeferred<Boolean>()
            RequestTree.request(
                ItemIdentifierStack.getFromStack(ItemStack(Blocks.CHEST)),
                requesterPipe.pipe,
                object : RequestLog {
                    override fun handleMissingItems(resources: List<IResource>) {
                        fail("Request should have succeeded. Missing items: $resources")
                    }

                    override fun handleSucessfullRequestOf(item: IResource, parts: LinkedLogisticsOrderList) {
                        requestSuccessful.complete(true)
                    }

                    override fun handleSucessfullRequestOfList(
                        resources: List<IResource>,
                        parts: LinkedLogisticsOrderList
                    ) {
                        requestSuccessful.complete(true)
                    }
                },
                false, false, true, false,
                RequestTree.defaultRequestFlags.clone().apply {
                    add(RequestTree.ActiveRequestType.LogMissing)
                },
                null,
            )
            val requestWasSuccessful = withTimeoutOrNull(Duration.ofSeconds(1 * TIMEOUT_MODIFIER)) {
                requestSuccessful.await()
            }
            assertTrue(message = "the request was not reported successful") {
                requestWasSuccessful == true
            }
            val waitForChestResult = waitForOrNull(
                timeout = Duration.ofSeconds(3 * TIMEOUT_MODIFIER),
                check = { chest.getTileEntity<TileEntityChest>().containsAll(listOf(ItemStack(Blocks.CHEST))) },
            )
            assertTrue(message = "a chest item was not found before a timeout") {
                waitForChestResult !== null
            }
            selector.setVisibleState(TestState.SUCCESS)
            logger("[PASSED]")
        } catch (e: Throwable) {
            selector.setVisibleState(TestState.FAIL)
            logger("[FAILED]\n==> ${e.stackTraceToString()}")
        }
    }

    suspend fun `test fuzzy-input crafting succeeds with sufficient mixed input OreDict`(
        loggerIn: (Any) -> Unit,
        selector: BlockPosSelector,
    ) {
        val testName = Throwable().stackTrace[0].methodName
        val logger = { msg: Any -> loggerIn("$testName $msg") }
        try {
            val (requesterPipe, chest) = `setup fuzzy crafting chest`(
                selector = selector,
                providerStacks = arrayOf(
                    ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.OAK.metadata),
                    ItemStack(Blocks.PLANKS, 8, BlockPlanks.EnumType.DARK_OAK.metadata),
                ),
            )
            selector.finalize()
            delay(5000)
            requesterPipe.pipe.router.update(true, requesterPipe.pipe)
            val requestSuccessful = CompletableDeferred<Boolean>()
            RequestTree.request(
                ItemIdentifierStack.getFromStack(ItemStack(Blocks.CHEST)),
                requesterPipe.pipe,
                object : RequestLog {
                    override fun handleMissingItems(resources: List<IResource>) {
                        fail("Request should have succeeded. Missing items: $resources")
                    }

                    override fun handleSucessfullRequestOf(item: IResource, parts: LinkedLogisticsOrderList) {
                        requestSuccessful.complete(true)
                    }

                    override fun handleSucessfullRequestOfList(
                        resources: List<IResource>,
                        parts: LinkedLogisticsOrderList
                    ) {
                        requestSuccessful.complete(true)
                    }
                },
                false, false, true, false,
                RequestTree.defaultRequestFlags.clone().apply {
                    add(RequestTree.ActiveRequestType.LogMissing)
                },
                null,
            )
            val requestWasSuccessful = withTimeoutOrNull(Duration.ofSeconds(1 * TIMEOUT_MODIFIER)) {
                requestSuccessful.await()
            }
            assertTrue(message = "the request was not reported successful") {
                requestWasSuccessful == true
            }
            val waitForChestResult = waitForOrNull(
                timeout = Duration.ofSeconds(3 * TIMEOUT_MODIFIER),
                check = { chest.getTileEntity<TileEntityChest>().containsAll(listOf(ItemStack(Blocks.CHEST))) },
            )
            assertTrue(message = "a chest item was not found before a timeout") {
                waitForChestResult !== null
            }
            selector.setVisibleState(TestState.SUCCESS)
            logger("[PASSED]")
        } catch (e: Throwable) {
            selector.setVisibleState(TestState.FAIL)
            logger("[FAILED]\n==> ${e.stackTraceToString()}")
        }
    }

    suspend fun `test fuzzy-input crafting succeeds multi-request with sufficient mixed input OreDict`(
        loggerIn: (Any) -> Unit,
        selector: BlockPosSelector,
    ) {
        val testName = Throwable().stackTrace[0].methodName
        val logger = { msg: Any -> loggerIn("$testName $msg") }
        try {
            val (requesterPipe, chest) = `setup fuzzy crafting chest`(
                selector = selector,
                providerStacks = arrayOf(
                    ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.OAK.metadata),
                    ItemStack(Blocks.PLANKS, 8, BlockPlanks.EnumType.DARK_OAK.metadata),
                ),
            )
            selector.finalize()
            delay(5000)
            requesterPipe.pipe.router.update(true, requesterPipe.pipe)
            val requestSuccessful = CompletableDeferred<Boolean>()
            val stacks = listOf(ItemStack(Blocks.CHEST))
            RequestTree.request(
                stacks.map { ItemIdentifierStack.getFromStack(it) }.toList(),
                requesterPipe.pipe,
                object : RequestLog {
                    override fun handleMissingItems(resources: List<IResource>) {
                        fail("Request should have succeeded. Missing items: $resources")
                    }

                    override fun handleSucessfullRequestOf(item: IResource, parts: LinkedLogisticsOrderList) {
                        requestSuccessful.complete(true)
                    }

                    override fun handleSucessfullRequestOfList(
                        resources: List<IResource>,
                        parts: LinkedLogisticsOrderList
                    ) {
                        requestSuccessful.complete(true)
                    }
                },
                RequestTree.defaultRequestFlags.clone().apply {
                    add(RequestTree.ActiveRequestType.LogMissing)
                },
                null,
            )
            val requestWasSuccessful = withTimeoutOrNull(Duration.ofSeconds(1 * TIMEOUT_MODIFIER)) {
                requestSuccessful.await()
            }
            assertTrue(message = "the request was not reported successful") {
                requestWasSuccessful == true
            }
            val waitForChestResult = waitForOrNull(
                timeout = Duration.ofSeconds(3 * TIMEOUT_MODIFIER),
                check = { chest.getTileEntity<TileEntityChest>().containsAll(stacks) },
            )
            assertTrue(message = "a chest item was not found before a timeout") {
                waitForChestResult !== null
            }
            selector.setVisibleState(TestState.SUCCESS)
            logger("[PASSED]")
        } catch (e: Throwable) {
            selector.setVisibleState(TestState.FAIL)
            logger("[FAILED]\n==> ${e.stackTraceToString()}")
        }
    }

    private suspend fun `setup fuzzy crafting chest`(
        selector: BlockPosSelector,
        providerStacks: Array<ItemStack>,
    ): Pair<PipePlacer<PipeItemsRequestLogistics>, BlockPlacer<BlockChest>> {
        val fuzzyCraftingTableHasRecipe = CompletableDeferred<Unit>()
        val craftingPipeInitialized = CompletableDeferred<PipeItemsCraftingLogistics>()
        return selector.place(
            BlockPlacer(block = LPBlocks.crafterFuzzy) { placer ->
                placer.getTileEntity<LogisticsCraftingTableTileEntity>().apply {
                    (0 until 9).filter { it != 4 }.forEach {
                        matrix.setInventorySlotContents(it, ItemStack(Blocks.PLANKS))
                        FuzzyUtil.set(inputFuzzy(it), FuzzyFlag.USE_ORE_DICT, true)
                    }
                    cacheRecipe()
                }
                fuzzyCraftingTableHasRecipe.complete(Unit)
            })
            .direction(EnumFacing.NORTH)
            .place(PipePlacer(PipeItemsCraftingLogistics(LPItems.pipeCrafting)) {
                (it.pipe.upgradeManager as UpgradeManager).inv.apply {
                    setInventorySlotContents(0, ItemStack(fuzzyUpgradeItem))
                    markDirty()
                }
                it.waitForPipeInitialization()
                assertTrue(message = "Expected crafting pipe to have fuzzy upgrade") {
                    it.pipe.logisticsModule.hasFuzzyUpgrade()
                }
                craftingPipeInitialized.complete(it.pipe)
                it.updateConnectionsAndWait()
            })
            .configure(configurator(name = "crafting recipe importer") {
                fuzzyCraftingTableHasRecipe.await()
                val craftingPipe = craftingPipeInitialized.await()
                craftingPipe.logisticsModule.importFromCraftingTable(null)
            })
            .apply { setupLogisticsPower(EnumFacing.EAST, 100000F) }
            .apply { setupProvidingChest(EnumFacing.WEST, *providerStacks) }
            .run { setupRequestingChest(EnumFacing.UP) }
    }

}
