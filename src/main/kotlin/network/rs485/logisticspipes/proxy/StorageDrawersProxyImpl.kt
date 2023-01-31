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

package network.rs485.logisticspipes.proxy

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer
import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup
import logisticspipes.LPConstants
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler
import logisticspipes.utils.item.ItemIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.versioning.ArtifactVersion
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import net.minecraftforge.fml.common.versioning.VersionRange
import network.rs485.logisticspipes.inventory.ProviderMode
import network.rs485.logisticspipes.util.equalsWithNBT
import kotlin.math.min

class StorageDrawersProxyImpl : SpecialInventoryHandler.Factory {

    companion object {
        @JvmStatic
        @CapabilityInject(IDrawerGroup::class)
        val drawerGroupCapability: Capability<IDrawerGroup>? = null
    }

    override fun isType(tile: TileEntity, dir: EnumFacing?): Boolean = tile.hasCapability(drawerGroupCapability!!, dir)

    override fun getUtilForTile(
        tile: TileEntity,
        direction: EnumFacing?,
        mode: ProviderMode
    ): SpecialInventoryHandler? =
        drawerGroupCapability?.let {
            StorageDrawersInventoryHandler(
                tile.getCapability(drawerGroupCapability, direction)!!, mode
            )
        }

    override fun init(): Boolean =
        Loader.instance().modList.firstOrNull { it.modId == LPConstants.storagedrawersModID }?.let {
            val validVersions = VersionRange.createFromVersionSpec("[1.12.2-5.4.0,1.12.3)")
            val validVersionsFml = VersionRange.createFromVersionSpec("5.2.2")
            val version: ArtifactVersion = DefaultArtifactVersion(it.version)
            (validVersions.containsVersion(version) || validVersionsFml.containsVersion(version))
                    && drawerGroupCapability != null
        } ?: false

}

class StorageDrawersInventoryHandler(
    private val drawerGroup: IDrawerGroup,
    private val mode: ProviderMode,
) : SpecialInventoryHandler() {

    private fun checkSlot(slot: Int) = slot in mode.cropStart until (drawerGroup.drawerCount - mode.cropEnd)

    private fun <T> fullDrawerApply(slot: Int, apply: (IDrawer) -> T) =
        drawerGroup.getDrawer(slot).takeIf { it.isEnabled && !it.isEmpty }?.let(apply)

    private fun accessibleDrawerSlots() = drawerGroup.accessibleDrawerSlots.filter(::checkSlot)

    private fun slotMachine() = accessibleDrawerSlots()
        .flatMap { slot ->
            fullDrawerApply(slot) {
                listOf(Triple(slot, it.storedItemPrototype, it.storedItemCount))
            } ?: emptyList()
        }

    private fun Int.hideSinglePerTypeOrStack(): Int =
        (if (mode.hideOnePerStack || mode.hideOnePerType) minus(1) else this).coerceAtLeast(0)

    private fun Int.hideSinglePerStack(): Int = if (mode.hideOnePerStack) minus(1) else this

    private fun enabledDrawerSequence(): Sequence<IDrawer> =
        accessibleDrawerSlots().asSequence()
            .map { slot -> drawerGroup.getDrawer(slot) }
            .filter { drawer -> drawer.isEnabled }

    override fun getItems(): MutableSet<ItemIdentifier> = accessibleDrawerSlots().flatMapTo(HashSet()) { slot ->
        fullDrawerApply(slot) { listOf(ItemIdentifier.get(it.storedItemPrototype)) } ?: emptyList()
    }

    override fun getStackInSlot(slot: Int): ItemStack =
        if (checkSlot(slot) && slot in drawerGroup.accessibleDrawerSlots) {
            fullDrawerApply(slot) { it.storedItemPrototype } ?: ItemStack.EMPTY
        } else ItemStack.EMPTY

    override fun decrStackSize(slot: Int, amount: Int): ItemStack =
        if (amount <= 0) ItemStack.EMPTY else getStackInSlot(slot).let { slotStack ->
            if (slotStack.isEmpty) slotStack else slotStack.copy().also {
                it.grow((-min(amount, slotStack.count.hideSinglePerTypeOrStack())).apply {
                    drawerGroup.getDrawer(slot).adjustStoredItemCount(this)
                })
            }
        }

    override fun itemCount(itemid: ItemIdentifier): Int = slotMachine()
        .filter { (_, stack, _) -> itemid.equalsWithNBT(stack) }
        .sumOf { (_, _, count) -> count.hideSinglePerStack() }
        .apply { if (mode.hideOnePerType && !mode.hideOnePerStack) minus(1) }
        .coerceAtLeast(0)

    override fun getItemsAndCount(): MutableMap<ItemIdentifier, Int> = HashMap<ItemIdentifier, Int>().also { map ->
        slotMachine().forEach { (_, stack, count) ->
            map.compute(ItemIdentifier.get(stack)) { _, existing ->
                count.hideSinglePerStack() + (existing ?: if (mode.hideOnePerType && !mode.hideOnePerStack) -1 else 0)
            }
        }
        map.forEach { (k, v) -> map[k] = v.coerceAtLeast(0) }
    }

    override fun getSizeInventory(): Int = drawerGroup.drawerCount

    override fun getMultipleItems(itemid: ItemIdentifier, count: Int): ItemStack {
        var left = count
        enabledDrawerSequence()
            .takeWhile { left > 0 }
            .filter { drawer -> !drawer.isEmpty && itemid.equalsWithNBT(drawer.storedItemPrototype) }
            .forEach { drawer -> left = drawer.adjustStoredItemCount(-left) }
        return itemid.makeNormalStack(count - left)
    }

    override fun containsUndamagedItem(itemid: ItemIdentifier): Boolean =
        accessibleDrawerSlots().any { slot ->
            fullDrawerApply(slot) { drawer -> itemid.equalsWithNBT(drawer.storedItemPrototype) } ?: false
        }

    override fun add(stack: ItemStack, orientation: EnumFacing?, doAdd: Boolean): ItemStack {
        var left = stack.count
        enabledDrawerSequence()
            .takeWhile { left > 0 }
            .filter { drawer -> drawer.canItemBeStored(stack) }
            .forEach { drawer ->
                left = if (doAdd) {
                    drawer.adjustStoredItemCount(left)
                } else {
                    (left - drawer.acceptingRemainingCapacity).coerceAtLeast(0)
                }
            }
        return stack.copy().also { it.grow(-left) }
    }

    override fun roomForItem(stack: ItemStack): Int = accessibleDrawerSlots().map { slot ->
        drawerGroup.getDrawer(slot)
            .let { if (it.isEnabled && it.canItemBeStored(stack)) it.acceptingRemainingCapacity else 0 }
    }.sum()

    override fun getSingleItem(item: ItemIdentifier?): ItemStack = throw NotImplementedError("Unused operation")

}
