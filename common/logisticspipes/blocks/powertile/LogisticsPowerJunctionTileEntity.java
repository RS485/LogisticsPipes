package logisticspipes.blocks.powertile;

import ic2.api.energy.tile.IEnergySink;

import java.util.List;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.ModDependentField;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
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
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.utils.PlayerCollectionList;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@ModDependentInterface(modId={"IC2", "ComputerCraft@1.6", "CoFHCore", "BuildCraft|Transport"}, interfacePath={"ic2.api.energy.tile.IEnergySink", "dan200.computercraft.api.peripheral.IPeripheral", "cofh.api.energy.IEnergyHandler", "buildcraft.api.power.IPowerReceptor"})
public class LogisticsPowerJunctionTileEntity extends TileEntity implements IGuiTileEntity, IPowerReceptor, ILogisticsPowerProvider, IPowerLevelDisplay, IGuiOpenControler, IHeadUpDisplayBlockRendererProvider, IBlockWatchingHandler, IEnergySink, IPeripheral, IEnergyHandler {

	public Object OPENPERIPHERAL_IGNORE; //Tell OpenPeripheral to ignore this class
	
	// true if it needs more power, turns off at full, turns on at 50%.
	public boolean needMorePowerTriggerCheck = true;
	
	public final int BuildCraftMultiplier = 5;
	public final int IC2Multiplier = 2;
	public final int RFDivisor = 2;
	public final int MAX_STORAGE = 2000000;

	@ModDependentField(modId="BuildCraft|Transport")
	private PowerHandler powerFramework;
	
	@MjBattery(maxCapacity=1000, maxReceivedPerCycle=1000)
	@ModDependentField(modId="BuildCraft|Transport")
	public double bcMJBatery = 0;
	
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
		if(SimpleServiceLocator.buildCraftProxy.isInstalled()) {
			powerFramework = new PowerHandler(this, Type.STORAGE);
			powerFramework.configure(1, 250, 1000, 750); // never triggers doWork, as this is just an energy store, and tick does the actual work.
		}
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
	
	private void addStoredMJ() {
		float space = freeSpace() / BuildCraftMultiplier;
		float minrequest = 1.01f / BuildCraftMultiplier;	//we round down, so always ask for a bit over 1LP-equivalent
		if(space < minrequest)
			space = minrequest;
		int availablelp = (int)(powerFramework.useEnergy(minrequest, space, false) * BuildCraftMultiplier);
		if(availablelp > 0) {
			float totake = (float)availablelp / BuildCraftMultiplier;
			if(powerFramework.useEnergy(totake, totake, true) == totake) {
				addEnergy(availablelp);
			}
		}
		if(freeSpace() <= 0) return;
		space = freeSpace() / BuildCraftMultiplier;
		if(space < minrequest)
			space = minrequest;
		availablelp = (int) (Math.min(bcMJBatery, space) * BuildCraftMultiplier);
		if(availablelp > 0) {
			float totake = (float) availablelp / BuildCraftMultiplier;
			bcMJBatery -= totake;
			addEnergy(availablelp);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		if(SimpleServiceLocator.buildCraftProxy.isInstalled()) {
			powerFramework.readFromNBT(par1nbtTagCompound);
		}
		internalStorage = par1nbtTagCompound.getInteger("powerLevel");
		if(par1nbtTagCompound.hasKey("needMorePowerTriggerCheck")) {
			needMorePowerTriggerCheck = par1nbtTagCompound.getBoolean("needMorePowerTriggerCheck");
		}
		bcMJBatery = par1nbtTagCompound.getDouble("bcMJBatery");
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		if(SimpleServiceLocator.buildCraftProxy.isInstalled()) {
			powerFramework.writeToNBT(par1nbtTagCompound);
		}
		par1nbtTagCompound.setInteger("powerLevel", internalStorage);
		par1nbtTagCompound.setBoolean("needMorePowerTriggerCheck", needMorePowerTriggerCheck);
		par1nbtTagCompound.setDouble("bcMJBatery", bcMJBatery);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if(MainProxy.isServer(getWorld())) {
			if(SimpleServiceLocator.buildCraftProxy.isActive()) {
				if(freeSpace() > 0 && (powerFramework.getEnergyStored() > 0 || bcMJBatery > 0)) {
					addStoredMJ();
				}
			}
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
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public void doWork(PowerHandler p) {}

	@Override
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
		par1CrashReportCategory.addCrashSection("LP-Version", LogisticsPipes.VERSION);
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
	@ModDependentMethod(modId="ComputerCraft@1.6")
	public String getType() {
		return "LogisticsPowerJunction";
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft@1.6")
	public String[] getMethodNames() {
		return new String[]{"getPowerLevel"};
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft@1.6")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		return new Object[]{this.getPowerLevel()};
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft@1.6")
	public boolean equals(IPeripheral other) {
		return this.equals((Object) other);
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft@1.6")
	public void attach(IComputerAccess computer) {}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft@1.6")
	public void detach(IComputerAccess computer) {}
	
	@Override
	@ModDependentMethod(modId = "BuildCraft|Transport")
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerFramework.getPowerReceiver();
	}

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
	public boolean canInterface(ForgeDirection from) {
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
