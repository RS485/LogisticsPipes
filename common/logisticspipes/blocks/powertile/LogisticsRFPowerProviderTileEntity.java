package logisticspipes.blocks.powertile;

import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyStorage;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyHandler;

@ModDependentInterface(modId = { "CoFHAPI|energy" }, interfacePath = { "cofh.api.energy.IEnergyHandler" })
public class LogisticsRFPowerProviderTileEntity extends LogisticsPowerProviderTileEntity implements IEnergyHandler {

	public static final int MAX_STORAGE = 10000000;
	public static final int MAX_MAXMODE = 8;
	public static final int MAX_PROVIDE_PER_TICK = 10000; //TODO

	private ICoFHEnergyStorage storage;

	public LogisticsRFPowerProviderTileEntity() {
		storage = SimpleServiceLocator.cofhPowerProxy.getEnergyStorage(10000);
	}

	public void addEnergy(float amount) {
		if (MainProxy.isClient(getWorldObj())) {
			return;
		}
		internalStorage += amount;
		if (internalStorage > LogisticsRFPowerProviderTileEntity.MAX_STORAGE) {
			internalStorage = LogisticsRFPowerProviderTileEntity.MAX_STORAGE;
		}
		if (internalStorage >= getMaxStorage()) {
			needMorePowerTriggerCheck = false;
		}
	}

	private void addStoredRF() {
		int space = freeSpace();
		int available = (storage.extractEnergy(space, true));
		if (available > 0) {
			if (storage.extractEnergy(available, false) == available) {
				addEnergy(available);
			}
		}
	}

	public int freeSpace() {
		return (int) (getMaxStorage() - internalStorage);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (MainProxy.isServer(worldObj)) {
			if (freeSpace() > 0) {
				addStoredRF();
			}
		}
	}

	@Override
	@ModDependentMethod(modId = "CoFHAPI|energy")
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		return storage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	@ModDependentMethod(modId = "CoFHAPI|energy")
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return storage.extractEnergy(maxExtract, simulate);
	}

	@Override
	@ModDependentMethod(modId = "CoFHAPI|energy")
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	@ModDependentMethod(modId = "CoFHAPI|energy")
	public int getEnergyStored(ForgeDirection from) {
		return storage.getEnergyStored();
	}

	@Override
	@ModDependentMethod(modId = "CoFHAPI|energy")
	public int getMaxEnergyStored(ForgeDirection from) {
		return storage.getMaxEnergyStored();
	}

	@Override
	public int getMaxStorage() {
		maxMode = Math.min(LogisticsRFPowerProviderTileEntity.MAX_MAXMODE, Math.max(1, maxMode));
		return (LogisticsRFPowerProviderTileEntity.MAX_STORAGE / maxMode);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		storage.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		storage.writeToNBT(nbt);
	}

	@Override
	public String getBrand() {
		return "RF";
	}

	@Override
	protected float getMaxProvidePerTick() {
		return LogisticsRFPowerProviderTileEntity.MAX_PROVIDE_PER_TICK;
	}

	@Override
	protected void handlePower(CoreRoutedPipe pipe, float toSend) {
		pipe.handleRFPowerArival(toSend);
	}

	@Override
	protected int getLaserColor() {
		return LogisticsPowerProviderTileEntity.RF_COLOR;
	}
}
