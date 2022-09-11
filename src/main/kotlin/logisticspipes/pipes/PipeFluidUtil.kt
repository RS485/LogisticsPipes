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

package logisticspipes.pipes

import logisticspipes.interfaces.ISpecialTankAccessHandler
import logisticspipes.interfaces.ITankUtil
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe
import logisticspipes.proxy.SimpleServiceLocator
import logisticspipes.utils.FluidIdentifier
import logisticspipes.utils.FluidIdentifierStack
import logisticspipes.utils.SpecialTankUtil
import logisticspipes.utils.TankUtil
import logisticspipes.utils.item.ItemIdentifierStack
import logisticspipes.utils.tuples.Pair
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidTankProperties
import network.rs485.logisticspipes.connection.getTankUtil
import kotlin.streams.toList

object PipeFluidUtil {

    fun getTankUtilForTE(tile: TileEntity?, dirOnEntity: EnumFacing?): ITankUtil? {
        if (SimpleServiceLocator.specialTankHandler.hasHandlerFor(tile)) {
            val handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(tile)
            if (handler is ISpecialTankAccessHandler) {
                if (tile!!.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dirOnEntity)) {
                    val fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dirOnEntity)
                    if (fluidHandler != null) {
                        return SpecialTankUtil(fluidHandler, tile, handler)
                    }
                }
            }
        }
        if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dirOnEntity)) {
            val fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dirOnEntity)
            if (fluidHandler != null) {
                return TankUtil(fluidHandler)
            }
        }
        return null
    }

    fun FluidRoutedPipe.getAdjacentTanks(listNearbyPipes: Boolean) =
        availableAdjacent.fluidTanks()
            .filter { isConnectableTank(it.tileEntity, it.direction, listNearbyPipes) }
            .flatMap { adjacent ->
                adjacent.getTankUtil()?.let { listOf(Pair(adjacent, it)) } ?: emptyList()
            }


    fun FluidRoutedPipe.getAllTankTiles(): List<TileEntity> = getAdjacentTanks(false)
        .flatMap { pair -> SimpleServiceLocator.specialTankHandler.getBaseTileFor(pair.component1().tileEntity) }

    fun PipeFluidSatellite.fluidsToItemList(): List<ItemIdentifierStack> {
        val fluidIdentStacks = getAdjacentTanks(false)
            .flatMap { (_, util) -> util.tanks().toList() }
            .mapNotNull { tank: IFluidTankProperties -> FluidIdentifierStack.getFromStack(tank.contents) }
        val distinctionSet = HashSet<FluidIdentifier>()
        val outputList = ArrayList<ItemIdentifierStack>()
        for (identStack in fluidIdentStacks) {
            if (distinctionSet.add(identStack.fluid)) {
                outputList.add(identStack.fluid.itemIdentifier.makeStack(identStack.amount))
            } else {
                outputList.find { it.item == identStack.fluid.itemIdentifier }!!.stackSize += identStack.amount
            }
        }
        return outputList
    }

}
