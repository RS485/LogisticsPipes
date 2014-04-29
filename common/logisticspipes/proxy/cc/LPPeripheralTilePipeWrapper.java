package logisticspipes.proxy.cc;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraftforge.common.util.ForgeDirection;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public class LPPeripheralTilePipeWrapper extends LPTilePipeWrapper implements IPeripheral {

	private final ForgeDirection dir;

	public LPPeripheralTilePipeWrapper(LogisticsTileGenericPipe pipe, ForgeDirection dir) {
		super(pipe);
		this.dir = dir;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
		pipe.currentPC = computer;
		Object[] result = super.callMethod(context, method, arguments);
		pipe.currentPC = null;
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
		if(other instanceof LPPeripheralTilePipeWrapper) {
			return ((LPPeripheralTilePipeWrapper)other).pipe.equals(pipe);
		}
		return false;
	}
}
