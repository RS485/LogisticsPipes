package logisticspipes.blocks.powertile;

import ic2.api.energy.tile.IEnergySink;

import java.util.List;

import logisticspipes.Configs;
import logisticspipes.LPConstants;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.blocks.LogisticsSolidTileEntity;
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
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@ModDependentInterface(modId={"IC2", "CoFHCore", "BuildCraft|Transport"}, interfacePath={"ic2.api.energy.tile.IEnergySink", "cofh.api.energy.IEnergyHandler", "buildcraft.api.power.IPowerReceptor"})
@CCType(name="LogisticsPowerJunction")
public class LogisticsPowerJunctionTileEntity extends LogisticsSolidTileEntity implements IGuiTileEntity, IPowerReceptor, ILogisticsPowerProvider, IPowerLevelDisplay, IGuiOpenControler, IHeadUpDisplayBlockRendererProvider, IBlockWatchingHandler, IEnergySink, IEnergyHandler {

	public Object OPENPERIPHERAL_IGNORE; //Tell OpenPeripheral to ignore this class
	
	// true if it needs more power, turns off at full, turns on at 50%.
	public boolean needMorePowerTriggerCheck = true;
	
	public final int IC2Multiplier = 2;
	public final int RFDivisor = 2;
	public final int MAX_STORAGE = 2000000;
	
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
	
	public LogisticsPowerJunctionTileEntity() {
		HUD = new HUDPowerLevel(this);
	}

	@Override
	public boolean useEnergy(int amount, List<Object> providersToIgnore) {
		if(providersToIgnore!=null && providersToIgnore.contains(this))
			return false;
		if(canUseEnergy(amount,null)) {
			internalStorage -= (int) ((amount * Configs.POWER_USAGE_MULTIPLIER) + 0.5D);
			if(internalStorage<MAX_STORAGE/2)
				needMorePowerTriggerCheck=true;
			return true;
		}
		return false;
	}

	@Override
	public boolean canUseEnergy(int amount, List<Object> providersToIgnore) {
		if(providersToIgnore!=null && providersToIgnore.contains(this))
			return false;
		return internalStorage >= (int) ((amount * Configs.POWER_USAGE_MULTIPLIER) + 0.5D);
	}	
	@Override
	public boolean useEnergy(int amount) {
		return useEnergy(amount,null);
	}
	
	public int freeSpace() {
		return MAX_STORAGE - internalStorage;
	}
	
	public void updateClients() {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(PowerJunctionLevel.class).setInteger(internalStorage).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), guiListener);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(PowerJunctionLevel.class).setInteger(internalStorage).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), watcherList);
		lastUpdateStorage = internalStorage;
	}
	
	@Override
	public boolean canUseEnergy(int amount) {
		return canUseEnergy(amount,null);
	}
	
	public void addEnergy(float amount) {
		if(MainProxy.isClient(getWorld())) return;
		internalStorage += amount;
		if(internalStorage > MAX_STORAGE) {
			internalStorage = MAX_STORAGE;
		}
		if(internalStorage == MAX_STORAGE)
			needMorePowerTriggerCheck=false;
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		internalStorage = par1nbtTagCompound.getInteger("powerLevel");
		if(par1nbtTagCompound.hasKey("needMorePowerTriggerCheck")) {
			needMorePowerTriggerCheck = par1nbtTagCompound.getBoolean("needMorePowerTriggerCheck");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger("powerLevel", internalStorage);
		par1nbtTagCompound.setBoolean("needMorePowerTriggerCheck", needMorePowerTriggerCheck);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if(MainProxy.isServer(getWorld())) {
			if(internalStorage != lastUpdateStorage) {
				updateClients();
			}
		}
		if(!init) {
			if(MainProxy.isClient(getWorld())) {
				LogisticsHUDRenderer.instance().add(this);
			}
			if(!addedToEnergyNet) {
				SimpleServiceLocator.IC2Proxy.registerToEneryNet(this);
				addedToEnergyNet = true;
			}
			init = true;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if(MainProxy.isClient(this.getWorld())) {
			LogisticsHUDRenderer.instance().remove(this);
		}
		if(addedToEnergyNet) {
			SimpleServiceLocator.IC2Proxy.unregisterToEneryNet(this);
			addedToEnergyNet = false;
		}
	}

	@Override
	public void validate() {
		super.validate();
		if(MainProxy.isClient(this.getWorld())) {
			init = false;
		}
		if(!addedToEnergyNet) {
			init = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(MainProxy.isClient(this.getWorld())) {
			LogisticsHUDRenderer.instance().remove(this);
		}
		if(addedToEnergyNet) {
			SimpleServiceLocator.IC2Proxy.unregisterToEneryNet(this);
			addedToEnergyNet = false;
		}
	}

	@Override
	@CCCommand(description="Returns the currently stored power")
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
	@CCCommand(description="Returns the max. storable power")
	public int getMaxStorage() {
		return MAX_STORAGE;
	}

	public int getChargeState() {
		return internalStorage * 100 / MAX_STORAGE;
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
		if(MainProxy.isClient(this.getWorld())) {
			internalStorage = integer;
		}
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD ;
	}

	@Override
	public int getX() {
		return xCoord;
	}

	@Override
	public int getY() {
		return yCoord;
	}

	@Override
	public int getZ() {
		return zCoord;
	}

	@Override
	public World getWorld() {
		return this.getWorldObj();
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
		return getWorld().getTileEntity(xCoord, yCoord, zCoord) == this;
	}
	
	@Override
	public void func_145828_a(CrashReportCategory par1CrashReportCategory) {
		super.func_145828_a(par1CrashReportCategory);
		par1CrashReportCategory.addCrashSection("LP-Version", LPConstants.VERSION);
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public boolean acceptsEnergyFrom(TileEntity tile, ForgeDirection dir) {
		return true;
	}

	private void transferFromIC2Buffer() {
		if(freeSpace() > 0 && internalBuffer >= 1) {
			int addAmount = Math.min((int)Math.floor(internalBuffer), freeSpace());
			addEnergy(addAmount);
			internalBuffer -= addAmount;
		}
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public double getDemandedEnergy() {
		if(!addedToEnergyNet) return 0;
		transferFromIC2Buffer();
		//round up so we demand enough to completely fill visible storage
		return (freeSpace() + IC2Multiplier - 1) / IC2Multiplier;
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
		internalBuffer += amount * IC2Multiplier;
		transferFromIC2Buffer();
		return 0;
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public int getSinkTier() {
		return Integer.MAX_VALUE;
	}
	
	@Override

	@Override
	public boolean isHUDInvalid() {
		return this.isInvalid();
	}

	@Override
	@ModDependentMethod(modId="CoFHCore")
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		if(freeSpace() < 1)
			return 0;
		int RFspace = freeSpace() * RFDivisor - internalRFbuffer;
		int RFtotake = Math.min(maxReceive, RFspace);
		if(!simulate) {
			addEnergy(RFtotake / RFDivisor);
			internalRFbuffer += RFtotake % RFDivisor;
			if(internalRFbuffer >= RFDivisor) {
				addEnergy(1);
				internalRFbuffer -= RFDivisor;
			}
		}
		return RFtotake;
	}

	@Override
	@ModDependentMethod(modId="CoFHCore")
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return 0;
	}

	@Override
	@ModDependentMethod(modId="CoFHCore")
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	@ModDependentMethod(modId="CoFHCore")
	public int getEnergyStored(ForgeDirection from) {
		return internalStorage * RFDivisor + internalRFbuffer;
	}

	@Override
	@ModDependentMethod(modId="CoFHCore")
	public int getMaxEnergyStored(ForgeDirection from) {
		return MAX_STORAGE * RFDivisor;
	}
	
	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(PowerJunctionGui.class);
	}
}
