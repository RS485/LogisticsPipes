package logisticspipes.blocks.powertile;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.gui.hud.HUDPowerLevel;
import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.interfaces.IHeadUpDisplayBlockRendererProvider;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IPowerLevelDisplay;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.guis.block.PowerJunctionGui;
import logisticspipes.network.packets.block.PowerJunctionLevel;
import logisticspipes.network.packets.hud.HUDStartBlockWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopBlockWatchingPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.utils.PlayerCollectionList;

@ModDependentInterface(modId = { LPConstants.ic2ModID }, interfacePath = { "ic2.api.energy.tile.IEnergySink" })
@CCType(name = "LogisticsPowerJunction")
public class LogisticsPowerJunctionTileEntity extends LogisticsSolidTileEntity implements IGuiTileEntity, ILogisticsPowerProvider, IPowerLevelDisplay, IGuiOpenControler, IHeadUpDisplayBlockRendererProvider, IBlockWatchingHandler, IEnergySink {

	public Object OPENPERIPHERAL_IGNORE; //Tell OpenPeripheral to ignore this class

	@CapabilityInject(IMjConnector.class)
	private static Capability<IMjConnector> MJ_CONN = null;

	@CapabilityInject(IMjReceiver.class)
	private static Capability<IMjReceiver> MJ_RECV = null;

	// true if it needs more power, turns off at full, turns on at 50%.
	public boolean needMorePowerTriggerCheck = true;

	public final static int IC2Multiplier = 2;
	public final static int RFDivisor = 2;
	public final static int MJMultiplier = 5;
	public final static int MAX_STORAGE = 2000000;

	private int internalStorage = 0;
	private int lastUpdateStorage = 0;
	private double internalBuffer = 0;

	//small buffer to hold a fractional LP worth of RF
	private int internalRFbuffer = 0;

	private boolean addedToEnergyNet = false;

	private boolean init = false;
	private PlayerCollectionList guiListener = new PlayerCollectionList();
	private PlayerCollectionList watcherList = new PlayerCollectionList();
	private IHeadUpDisplayRenderer HUD;

	private IEnergyStorage energyInterface = new IEnergyStorage() {

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (freeSpace() < 1) {
				return 0;
			}
			int RFspace = freeSpace() * LogisticsPowerJunctionTileEntity.RFDivisor - internalRFbuffer;
			int RFtotake = Math.min(maxReceive, RFspace);
			if (!simulate) {
				addEnergy(RFtotake / LogisticsPowerJunctionTileEntity.RFDivisor);
				internalRFbuffer += RFtotake % LogisticsPowerJunctionTileEntity.RFDivisor;
				if (internalRFbuffer >= LogisticsPowerJunctionTileEntity.RFDivisor) {
					addEnergy(1);
					internalRFbuffer -= LogisticsPowerJunctionTileEntity.RFDivisor;
				}
			}
			return RFtotake;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return internalStorage * LogisticsPowerJunctionTileEntity.RFDivisor + internalRFbuffer;
		}

		@Override
		public int getMaxEnergyStored() {
			return LogisticsPowerJunctionTileEntity.MAX_STORAGE * LogisticsPowerJunctionTileEntity.RFDivisor;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return true;
		}
	};

	private Object mjReceiver;

	public LogisticsPowerJunctionTileEntity() {
		HUD = new HUDPowerLevel(this);
		mjReceiver = SimpleServiceLocator.buildCraftProxy.createMjReceiver(this);
	}

	@Override
	public boolean useEnergy(int amount) {
		return useEnergy(amount, Collections.emptyList());
	}

	@Override
	public boolean useEnergy(int amount, @Nonnull List<Object> providersToIgnore) {
		if (providersToIgnore.contains(this)) {
			return false;
		}
		if (canUseEnergy(amount)) {
			this.markDirty();
			internalStorage -= (int) ((amount * Configs.POWER_USAGE_MULTIPLIER) + 0.5D);
			if (internalStorage < LogisticsPowerJunctionTileEntity.MAX_STORAGE / 2) {
				needMorePowerTriggerCheck = true;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canUseEnergy(int amount) {
		return canUseEnergy(amount, Collections.emptyList());
	}

	@Override
	public boolean canUseEnergy(int amount, @Nonnull List<Object> providersToIgnore) {
		if (providersToIgnore.contains(this)) {
			return false;
		}
		return internalStorage >= (int) ((amount * Configs.POWER_USAGE_MULTIPLIER) + 0.5D);
	}

	public int freeSpace() {
		return LogisticsPowerJunctionTileEntity.MAX_STORAGE - internalStorage;
	}

	public void updateClients() {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(PowerJunctionLevel.class).setInteger(internalStorage).setBlockPos(pos), guiListener);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(PowerJunctionLevel.class).setInteger(internalStorage).setBlockPos(pos), watcherList);
		lastUpdateStorage = internalStorage;
	}

	public void addEnergy(float amount) {
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		internalStorage += amount;
		if (internalStorage > LogisticsPowerJunctionTileEntity.MAX_STORAGE) {
			internalStorage = LogisticsPowerJunctionTileEntity.MAX_STORAGE;
		}
		if (internalStorage == LogisticsPowerJunctionTileEntity.MAX_STORAGE) {
			needMorePowerTriggerCheck = false;
		}
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		internalStorage = par1nbtTagCompound.getInteger("powerLevel");
		if (par1nbtTagCompound.hasKey("needMorePowerTriggerCheck")) {
			needMorePowerTriggerCheck = par1nbtTagCompound.getBoolean("needMorePowerTriggerCheck");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound par1nbtTagCompound) {
		par1nbtTagCompound = super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger("powerLevel", internalStorage);
		par1nbtTagCompound.setBoolean("needMorePowerTriggerCheck", needMorePowerTriggerCheck);
		return par1nbtTagCompound;
	}

	@Override
	public void update() {
		super.update();
		if (MainProxy.isServer(getWorld())) {
			if (internalStorage != lastUpdateStorage) {
				updateClients();
			}
		}
		if (!init) {
			if (MainProxy.isClient(getWorld())) {
				LogisticsHUDRenderer.instance().add(this);
			}
			if (!addedToEnergyNet) {
				SimpleServiceLocator.IC2Proxy.registerToEneryNet(this);
				addedToEnergyNet = true;
			}
			init = true;
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

	@Override
	@CCCommand(description = "Returns the currently stored power")
	public int getPowerLevel() {
		return internalStorage;
	}

	@Override
	public int getDisplayPowerLevel() {
		return getPowerLevel();
	}

	@Override
	public String getBrand() {
		return "LP";
	}

	@Override
	@CCCommand(description = "Returns the max. storable power")
	public int getMaxStorage() {
		return LogisticsPowerJunctionTileEntity.MAX_STORAGE;
	}

	@Override
	public int getChargeState() {
		return internalStorage * 100 / LogisticsPowerJunctionTileEntity.MAX_STORAGE;
	}

	@Override
	public void guiOpenedByPlayer(EntityPlayer player) {
		guiListener.add(player);
		updateClients();
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		guiListener.remove(player);
	}

	public void handlePowerPacket(int integer) {
		if (MainProxy.isClient(getWorld())) {
			internalStorage = integer;
		}
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}

	@Nonnull
	@Override
	public BlockPos getBlockPos() {
		return pos;
	}

	@Override
	public int getX() {
		return pos.getX();
	}

	@Override
	public int getY() {
		return pos.getY();
	}

	@Override
	public int getZ() {
		return pos.getZ();
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartBlockWatchingPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopBlockWatchingPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void playerStartWatching(EntityPlayer player) {
		watcherList.add(player);
		updateClients();
	}

	@Override
	public void playerStopWatching(EntityPlayer player) {
		watcherList.remove(player);
	}

	@Override
	public boolean isHUDExistent() {
		return getWorld().getTileEntity(pos) == this;
	}

	@Override
	public void addInfoToCrashReport(CrashReportCategory par1CrashReportCategory) {
		super.addInfoToCrashReport(par1CrashReportCategory);
		par1CrashReportCategory.addCrashSection("LP-Version", LogisticsPipes.getVersionString());
	}

	@Override
	@ModDependentMethod(modId = LPConstants.ic2ModID)
	public boolean acceptsEnergyFrom(IEnergyEmitter tile, EnumFacing dir) {
		return true;
	}

	private void transferFromIC2Buffer() {
		if (freeSpace() > 0 && internalBuffer >= 1) {
			int addAmount = Math.min((int) Math.floor(internalBuffer), freeSpace());
			addEnergy(addAmount);
			internalBuffer -= addAmount;
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.ic2ModID)
	public double getDemandedEnergy() {
		if (!addedToEnergyNet) {
			return 0;
		}
		transferFromIC2Buffer();
		//round up so we demand enough to completely fill visible storage
		return (freeSpace() + LogisticsPowerJunctionTileEntity.IC2Multiplier - 1) / LogisticsPowerJunctionTileEntity.IC2Multiplier;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.ic2ModID)
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
		internalBuffer += amount * LogisticsPowerJunctionTileEntity.IC2Multiplier;
		transferFromIC2Buffer();
		return 0;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.ic2ModID)
	public int getSinkTier() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isHUDInvalid() {
		return isInvalid();
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		if (capability == MJ_CONN || capability == MJ_RECV) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return (T) energyInterface;
		}
		if (capability == MJ_CONN || capability == MJ_RECV) {
			return (T) mjReceiver;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(PowerJunctionGui.class);
	}
}
