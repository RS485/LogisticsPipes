package logisticspipes.blocks.powertile;

import logisticspipes.interfaces.routing.ILogisticsPowerProvider;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.power.PowerProvider;

public class LogisticsPowerJuntionTileEntity_BuildCraft extends TileEntity implements IPowerReceptor, ILogisticsPowerProvider {
	
	public final int BuildCraftMultiplier = 5;
	public final int MAX_STORAGE = 20000000;
	
	private IPowerProvider powerFramework;
	
	protected int internalStorage = 0;
	
	public LogisticsPowerJuntionTileEntity_BuildCraft() {
		powerFramework = PowerFramework.currentFramework.createPowerProvider();
		powerFramework.configure(0, 1, 250, 1, 750);
	}
	
	@Override
	public boolean useEnergy(int amount) {
		if(canUseEnergy(amount)) {
			internalStorage -= amount;
			return true;
		}
		return false;
	}
	
	public int freeSpace() {
		return MAX_STORAGE - internalStorage;
	}
	
	@Override
	public boolean canUseEnergy(int amount) {
		return internalStorage >= amount;
	}
	
	public void addEnergy(float amount) {
		internalStorage += amount;
		if(internalStorage > MAX_STORAGE) {
			internalStorage = MAX_STORAGE;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		internalStorage = par1nbtTagCompound.getInteger("powerLevel");
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger("powerLevel", internalStorage);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		float energy = Math.max(powerFramework.getEnergyStored(), freeSpace() / BuildCraftMultiplier);
		if(powerFramework.useEnergy(energy, energy, false) == energy) {
			powerFramework.useEnergy(energy, energy, true);
			addEnergy(energy * BuildCraftMultiplier);
		}
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {
		powerFramework = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerFramework;
	}

	@Override
	public void doWork() {}

	@Override
	public int powerRequest() {
		return Math.min(powerFramework.getMaxEnergyReceived(), freeSpace() / BuildCraftMultiplier);
	}

	@Override
	public int getPowerLevel() {
		return internalStorage;
	}
}
