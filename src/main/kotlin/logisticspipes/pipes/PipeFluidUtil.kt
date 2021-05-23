package logisticspipes.pipes

import logisticspipes.pipes.basic.fluid.FluidRoutedPipe
import logisticspipes.proxy.SimpleServiceLocator
import logisticspipes.utils.FluidIdentifier
import logisticspipes.utils.FluidIdentifierStack
import logisticspipes.utils.item.ItemIdentifierStack
import logisticspipes.utils.tuples.Pair
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.capability.IFluidTankProperties
import network.rs485.logisticspipes.connection.getTankUtil
import kotlin.streams.toList

object PipeFluidUtil {

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
