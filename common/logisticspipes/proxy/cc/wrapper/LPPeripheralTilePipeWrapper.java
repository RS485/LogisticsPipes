package logisticspipes.proxy.cc.wrapper;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;

import net.minecraftforge.common.util.ForgeDirection;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class LPPeripheralTilePipeWrapper implements IPeripheral {

	private final ForgeDirection dir;
	private CCCommandWrapper wrapped;
	private LogisticsTileGenericPipe pipe;

	public LPPeripheralTilePipeWrapper(LogisticsTileGenericPipe pipe, ForgeDirection dir) {
		this.pipe = pipe;
		wrapped = (CCCommandWrapper) CCObjectWrapper.checkForAnnotations(pipe.pipe, CCCommandWrapper.WRAPPER);
		this.dir = dir;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		pipe.currentPC = computer;
		wrapped.isDirectCall = true;
		Object[] result = wrapped.callMethod(context, method, arguments);
		pipe.currentPC = null;
		wrapped.isDirectCall = false;
		return result;
	}

	@Override
	public void attach(IComputerAccess computer) {
		pipe.connections.put(computer, dir);
		pipe.scheduleNeighborChange();
	}

	@Override
	public void detach(IComputerAccess computer) {
		pipe.connections.remove(computer);
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (other instanceof LPPeripheralTilePipeWrapper) {
			return ((LPPeripheralTilePipeWrapper) other).pipe.equals(pipe);
		}
		return false;
	}

	@Override
	public String getType() {
		return wrapped.getType();
	}

	@Override
	public String[] getMethodNames() {
		return wrapped.getMethodNames();
	}
}
