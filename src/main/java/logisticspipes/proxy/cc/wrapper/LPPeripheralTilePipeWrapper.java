package logisticspipes.proxy.cc.wrapper;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;

public class LPPeripheralTilePipeWrapper implements IPeripheral {

	private final EnumFacing dir;
	private CCCommandWrapper wrapped;
	private LogisticsTileGenericPipe pipe;

	public LPPeripheralTilePipeWrapper(LogisticsTileGenericPipe pipe, EnumFacing dir) {
		this.pipe = pipe;
		wrapped = (CCCommandWrapper) CCObjectWrapper.checkForAnnotations(pipe.pipe, CCCommandWrapper.WRAPPER);
		this.dir = dir;
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) {
		pipe.currentPC = computer;
		wrapped.isDirectCall = true;
		Object[] result = wrapped.callMethod(context, method, arguments);
		pipe.currentPC = null;
		wrapped.isDirectCall = false;
		return result;
	}

	@Override
	public void attach(@Nonnull IComputerAccess computer) {
		pipe.connections.put(computer, dir);
		pipe.scheduleNeighborChange();
	}

	@Override
	public void detach(@Nonnull IComputerAccess computer) {
		pipe.connections.remove(computer);
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (other instanceof LPPeripheralTilePipeWrapper) {
			return ((LPPeripheralTilePipeWrapper) other).pipe.equals(pipe);
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
