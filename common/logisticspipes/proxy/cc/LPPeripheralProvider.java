package logisticspipes.proxy.cc;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.cc.wrapper.LPPeripheralTilePipeWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

public class LPPeripheralProvider implements IPeripheralProvider {
	@Override
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(tile instanceof IPeripheral) {
			if(tile.getClass().getPackage().getName().startsWith("logisticspipes")) {
				return (IPeripheral)tile;
			}
		}
		if(tile instanceof LogisticsTileGenericPipe) {
			return new LPPeripheralTilePipeWrapper((LogisticsTileGenericPipe)tile, ForgeDirection.VALID_DIRECTIONS[side]);
		}
		return null;
	}
}
