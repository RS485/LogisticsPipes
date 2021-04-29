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
import network.rs485.logisticspipes.util.FuzzyFlag
import network.rs485.logisticspipes.util.FuzzyUtil
import network.rs485.minecraft.BlockBuilder
import network.rs485.minecraft.BlockPosSelector
import java.time.Duration
import kotlin.test.assertTrue

@Suppress("FunctionName")
object CraftingTest {
    private val fuzzyUpgradeItem = Item.REGISTRY.getObject(LPItems.upgrades[FuzzyUpgrade.getName()])!!

    suspend fun `test fuzzy-input crafting fails with mixed input OreDict`(
        logger: (Any) -> Unit,
        selector: BlockPosSelector,
    ) {
        val testName = Throwable().stackTrace[0].methodName
        try {
            val requesterPair = `setup fuzzy crafting chest`(
                selector = selector,
                providerStacks = arrayOf(
                    ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.OAK.metadata),
                    ItemStack(Blocks.PLANKS, 4, BlockPlanks.EnumType.DARK_OAK.metadata),
                ),
            )
            val missingItems = CompletableDeferred<Boolean>()
            val stacks = listOf(ItemStack(Blocks.CHEST))
            RequestTree.request(
                stacks.map { ItemIdentifierStack.getFromStack(it) }.toList(),
                requesterPair.first.pipe,
                object : RequestLog {
                    override fun handleMissingItems(resources: List<IResource>) {
                        missingItems.complete(true)
                    }

                    override fun handleSucessfullRequestOf(item: IResource, parts: LinkedLogisticsOrderList) {
                        missingItems.complete(false)
                    }

                    override fun handleSucessfullRequestOfList(
                        resources: List<IResource>,
                        parts: LinkedLogisticsOrderList
                    ) {
                        missingItems.complete(false)
                    }
                },
                RequestTree.defaultRequestFlags.clone().apply { add(RequestTree.ActiveRequestType.LogMissing) },
                null,
            )
            val requestMissedItems = withTimeoutOrNull(Duration.ofSeconds(1)) {
                missingItems.await()
            }
            assertTrue(message = "the request did not report missing items") {
                requestMissedItems == true
            }
            val waitForChestResult = waitForOrNull(
                timeout = Duration.ofSeconds(3),
                check = { requesterPair.second.getTileEntity<TileEntityChest>().containsAll(stacks) },
            )
            assertTrue(message = "a chest item was found in the requester chest") {
                waitForChestResult === null
            }
            logger("$testName [PASSED]")
        } catch (e: Throwable) {
            logger("$testName [FAILED]\n==> ${e.stackTraceToString()}")
        }
    }

    private suspend fun `setup fuzzy crafting chest`(
        selector: BlockPosSelector, providerStacks: Array<ItemStack>
    ): Pair<PipeBuilder<PipeItemsRequestLogistics>, BlockBuilder<BlockChest>> {
        val fuzzyCraftingTable = selector.place(LPBlocks.crafterFuzzy)
        val craftingPipe = fuzzyCraftingTable
            .direction(EnumFacing.NORTH)
            .placePipe(PipeItemsCraftingLogistics(LPItems.pipeCrafting))
        (craftingPipe.pipe.upgradeManager as UpgradeManager).inv.apply {
            setInventorySlotContents(0, ItemStack(fuzzyUpgradeItem))
            markDirty()
        }
        craftingPipe.waitForPipeInitialization()
        assertTrue(message = "Expected crafting pipe to have fuzzy upgrade") {
            craftingPipe.pipe.logisticsModule.hasFuzzyUpgrade()
        }
        fuzzyCraftingTable.getTileEntity<LogisticsCraftingTableTileEntity>().apply {
            (0 until 9).filter { it != 4 }.forEach {
                matrix.setInventorySlotContents(it, ItemStack(Blocks.PLANKS))
                FuzzyUtil.set(inputFuzzy(it), FuzzyFlag.USE_ORE_DICT, true)
            }
            cacheRecipe()
        }
        craftingPipe.updateConnectionsAndWait()
        craftingPipe.pipe.logisticsModule.importFromCraftingTable(null)
        craftingPipe.setupLogisticsPower(EnumFacing.EAST, 100000F)
        craftingPipe.setupProvidingChest(EnumFacing.WEST, *providerStacks)
            .also { it.first.updateConnectionsAndWait() }
        return craftingPipe.setupRequestingChest(EnumFacing.UP)
            .also { it.first.updateConnectionsAndWait() }
    }

}