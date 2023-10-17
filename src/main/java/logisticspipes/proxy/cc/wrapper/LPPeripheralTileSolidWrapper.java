package logisticspipes.proxy.cc.wrapper;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;

public class LPPeripheralTileSolidWrapper implements IPeripheral {

	private CCCommandWrapper wrapped;
	private LogisticsSolidTileEntity tile;

	public LPPeripheralTileSolidWrapper(LogisticsSolidTileEntity tile, EnumFacing dir) {
		this.tile = tile;
		wrapped = (CCCommandWrapper) CCObjectWrapper.checkForAnnotations(tile, CCCommandWrapper.WRAPPER);
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) {
		wrapped.isDirectCall = true;
		Object[] result = wrapped.callMethod(context, method, arguments);
		wrapped.isDirectCall = false;
		return result;
	}

	@Override
	public void attach(@Nonnull IComputerAccess computer) {}

	@Override
	public void detach(@Nonnull IComputerAccess computer) {}

	@Override
	public boolean equals(IPeripheral other) {
		if (other instanceof LPPeripheralTileSolidWrapper) {
			return ((LPPeripheralTileSolidWrapper) other).tile.equals(tile);
		}
		return false;
	}

	@Nonnull
	@Override
	public String getType() {
		return wrapped.getType();
	}

	@Nonnull
	@Override
	public String[] getMethodNames() {
		return wrapped.getMethodNames();
	}
}
