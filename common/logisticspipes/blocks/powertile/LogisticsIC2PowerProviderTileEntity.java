package logisticspipes.blocks.powertile;

import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsHUDRenderer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import ic2.api.energy.tile.IEnergySink;

@ModDependentInterface(modId = { "IC2" }, interfacePath = { "ic2.api.energy.tile.IEnergySink" })
public class LogisticsIC2PowerProviderTileEntity extends LogisticsPowerProviderTileEntity implements IEnergySink {

	public static final int MAX_STORAGE = 40000000;
	public static final int MAX_MAXMODE = 8;
	public static final int MAX_PROVIDE_PER_TICK = 2048 * 6; //TODO

	private boolean addedToEnergyNet = false;
	private boolean init = false;

	public LogisticsIC2PowerProviderTileEntity() {
		super();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!init) {
			if (!addedToEnergyNet) {
				SimpleServiceLocator.IC2Proxy.registerToEneryNet(this);
				addedToEnergyNet = true;
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (MainProxy.isClient(getWorld())) {
			LogisticsHUDRenderer.instance().remove(this);
		}
		if (addedToEnergyNet) {
			SimpleServiceLocator.IC2Proxy.unregisterToEneryNet(this);
			addedToEnergyNet = false;
		}
	}

	@Override
	public void validate() {
		super.validate();
		if (MainProxy.isClient(getWorld())) {
			init = false;
		}
		if (!addedToEnergyNet) {
			init = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (MainProxy.isClient(getWorld())) {
			LogisticsHUDRenderer.instance().remove(this);
		}
		if (addedToEnergyNet) {
			SimpleServiceLocator.IC2Proxy.unregisterToEneryNet(this);
			addedToEnergyNet = false;
		}
	}

	public void addEnergy(float amount) {
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		internalStorage += amount;
		if (internalStorage > LogisticsIC2PowerProviderTileEntity.MAX_STORAGE) {
			internalStorage = LogisticsIC2PowerProviderTileEntity.MAX_STORAGE;
		}
		if (internalStorage >= getMaxStorage()) {
			needMorePowerTriggerCheck = false;
		}
	}

	public float freeSpace() {
		return getMaxStorage() - internalStorage;
	}

	@Override
	public int getMaxStorage() {
		maxMode = Math.min(LogisticsIC2PowerProviderTileEntity.MAX_MAXMODE, Math.max(1, maxMode));
		return (LogisticsIC2PowerProviderTileEntity.MAX_STORAGE / maxMode);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
	}

	@Override
	public String getBrand() {
		return "EU";
	}

	@Override
	protected float getMaxProvidePerTick() {
		return LogisticsIC2PowerProviderTileEntity.MAX_PROVIDE_PER_TICK;
	}

	@Override
	protected void handlePower(CoreRoutedPipe pipe, float toSend) {
		pipe.handleIC2PowerArival(toSend);
	}

	@Override
	protected int getLaserColor() {
		return LogisticsPowerProviderTileEntity.IC2_COLOR;
	}

	@Override
	@ModDependentMethod(modId = "IC2")
	public boolean acceptsEnergyFrom(TileEntity tile, ForgeDirection dir) {
		return true;
	}

	@Override
	@ModDependentMethod(modId = "IC2")
	public double getDemandedEnergy() {
		return freeSpace();
	}

	@Override
	@ModDependentMethod(modId = "IC2")
	public int getSinkTier() {
		return Integer.MAX_VALUE;
	}

	@Override
	@ModDependentMethod(modId = "IC2")
	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
		addEnergy((float) amount);
		return 0;
	}
}
