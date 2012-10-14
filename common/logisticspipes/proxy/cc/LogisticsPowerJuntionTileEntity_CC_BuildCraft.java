package logisticspipes.proxy.cc;

import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class LogisticsPowerJuntionTileEntity_CC_BuildCraft extends
	LogisticsPowerJuntionTileEntity_BuildCraft implements IPeripheral {
	
	@Override
	public String getType() {
	return "LogisticsPowerJunction";
	}
	
	@Override
	public String[] getMethodNames() {
	return new String[]{"getPowerLevel"};
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception {
	return new Object[]{this.getPowerLevel()};
	}
	
	@Override
	public boolean canAttachToSide(int side) {
	return true;
	}
	
	@Override
	public void attach(IComputerAccess computer, String computerSide) {}
	
	@Override
	public void detach(IComputerAccess computer) {}
}
