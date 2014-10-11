package logisticspipes.proxy.buildcraft.bc60.subproxies;

import logisticspipes.proxy.buildcraft.subproxies.ILPBCPowerProxy;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public class LPBCPowerProxy implements ILPBCPowerProxy {
	
	private final PowerReceiver receptor;
	
	public LPBCPowerProxy(PowerReceiver receptor) {
		this.receptor = receptor;
	}

	@Override
	public double getMaxEnergyReceived() {
		return receptor.getMaxEnergyReceived();
	}

	@Override
	public double getMaxEnergyStored() {
		return receptor.getMaxEnergyStored();
	}

	@Override
	public double getEnergyStored() {
		return receptor.getEnergyStored();
	}

	@Override
	public double receiveEnergy(double quantity, ForgeDirection from) {
		return receptor.receiveEnergy(Type.PIPE, quantity, from);
	}
	
}
