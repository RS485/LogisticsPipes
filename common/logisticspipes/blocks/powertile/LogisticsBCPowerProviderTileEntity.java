package logisticspipes.blocks.powertile;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public class LogisticsBCPowerProviderTileEntity extends LogisticsPowerProviderTileEntity implements IPowerReceptor {
	
	public static final int MAX_STORAGE = 1000000;
	public static final int MAX_MAXMODE = 8;
	public static final int MAX_PROVIDE_PER_TICK = 1000; //TODO
	
	private PowerHandler powerFramework;
	
	public LogisticsBCPowerProviderTileEntity() {
		powerFramework = new PowerHandler(this, Type.STORAGE);
		powerFramework.configure(1, 250, 1000, 750); // never triggers doWork, as this is just an energy store, and tick does the actual work.
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(MainProxy.isServer(this.worldObj)) {
			if(freeSpace() > 0) {
				addStoredMJ();
			}
		}
	}

	public void addEnergy(float amount) {
		if(MainProxy.isClient(getWorld())) return;
		internalStorage += amount;
		if(internalStorage > MAX_STORAGE) {
			internalStorage = MAX_STORAGE;
		}
		if(internalStorage >= getMaxStorage())
			needMorePowerTriggerCheck=false;
	}
	
	private void addStoredMJ() {
		float space = freeSpace();
		int available = (int)(powerFramework.useEnergy(1, space, false));
		if(available > 0) {
			if(powerFramework.useEnergy(available, available, true) == available) {
				addEnergy(available);
			}
		}
	}

	public float freeSpace() {
		return getMaxStorage() - internalStorage;
	}
	
	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerFramework.getPowerReceiver();
	}
	
	@Override
	public void doWork(PowerHandler workProvider) {}
	
	@Override
	public World getWorld() {
		return this.getWorldObj();
	}
	
	public int getMaxStorage() {
		maxMode = Math.min(MAX_MAXMODE, Math.max(1, maxMode));
		return (MAX_STORAGE / maxMode);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		powerFramework.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		powerFramework.writeToNBT(nbt);
	}

	@Override
	public String getBrand() {
		return "MJ";
	}

	@Override
	protected float getMaxProvidePerTick() {
		return MAX_PROVIDE_PER_TICK;
	}

	@Override
	protected void handlePower(CoreRoutedPipe pipe, float toSend) {
		pipe.handleBCPowerArival(toSend);
	}

	@Override
	protected int getLaserColor() {
		return BC_COLOR;
	}
}
