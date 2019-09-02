package logisticspipes.proxy.cc;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.cc.wrapper.LPPeripheralTilePipeWrapper;
import logisticspipes.proxy.cc.wrapper.LPPeripheralTileSolidWrapper;

public class LPPeripheralProvider implements IPeripheralProvider {

	@Override
	public IPeripheral getPeripheral(World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof IPeripheral) {
			if (tile.getClass().getPackage().getName().startsWith("logisticspipes")) {
				return (IPeripheral) tile;
			}
		}
		if (tile instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) tile).pipe == null) {
				return null;
			}
			return new LPPeripheralTilePipeWrapper((LogisticsTileGenericPipe) tile, side);
		}
		if (tile instanceof LogisticsSolidTileEntity) {
			return new LPPeripheralTileSolidWrapper((LogisticsSolidTileEntity) tile, side);
		}
		return null;
	}
}
