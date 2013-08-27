package logisticspipes.blocks.powertile;

import ic2.api.Direction;
import ic2.api.energy.tile.IEnergySink;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.config.Configs;
import logisticspipes.gui.hud.HUDPowerJunction;
import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IHeadUpDisplayBlockRendererProvider;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.PowerJunctionLevel;
import logisticspipes.network.packets.hud.HUDStartBlockWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopBlockWatchingPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;

@ModDependentInterface(modId={"IC2", "ComputerCraft"}, interfacePath={"ic2.api.energy.tile.IEnergySink", "dan200.computer.api.IPeripheral"})
public class LogisticsPowerJunctionTileEntity extends TileEntity implements IPowerReceptor, ILogisticsPowerProvider, IGuiOpenControler, IHeadUpDisplayBlockRendererProvider, IBlockWatchingHandler, IEnergySink, IPeripheral {

	// true if it needs more power, turns off at full, turns on at 50%.
	public boolean needMorePowerTriggerCheck = true;
	
	public final int BuildCraftMultiplier = 5;
	public final int IC2Multiplier = 2;
	public final int MAX_STORAGE = 2000000;
	
	private PowerHandler powerFramework;
	
	private PlayerCollectionList guiListener = new PlayerCollectionList();
	
	private int internalStorage = 0;
  	private int lastUpdateStorage = 0;
  	private int internalBuffer = 0;
	
  	private boolean addedToEnergyNet = false;
	
	private boolean init = false;
	private PlayerCollectionList watcherList = new PlayerCollectionList();
	private IHeadUpDisplayRenderer HUD;
	
	public LogisticsPowerJunctionTileEntity() {
		powerFramework = new PowerHandler(this, Type.STORAGE);
		powerFramework.configure(0, 250, 1, 750);
		HUD = new HUDPowerJunction(this);
	}
	@Override
	public boolean useEnergy(int amount, List<Object> providersToIgnore) {
		if(providersToIgnore!=null && providersToIgnore.contains(this))
			return false;
		if(canUseEnergy(amount,null)) {
			internalStorage -= (amount * Configs.POWER_USAGE_MULTIPLIER);
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
		return internalStorage >= (amount * Configs.POWER_USAGE_MULTIPLIER);
	}	
	@Override
	public boolean useEnergy(int amount) {
		return useEnergy(amount,null);
	}
	
	public int freeSpace() {
		return MAX_STORAGE - internalStorage;
	}
	
	public void updateClients() {
//TODO 	MainProxy.sendToPlayerList(new PacketPipeInteger(NetworkConstants.POWER_JUNCTION_POWER_LEVEL, xCoord, yCoord, zCoord, internalStorage).getPacket(), guiListener);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(PowerJunctionLevel.class).setInteger(internalStorage).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), guiListener);
//TODO 	MainProxy.sendToPlayerList(new PacketPipeInteger(NetworkConstants.POWER_JUNCTION_POWER_LEVEL, xCoord, yCoord, zCoord, internalStorage).getPacket(), watcherList);
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
			float energy = Math.min(powerFramework.getEnergyStored(), freeSpace() / BuildCraftMultiplier);
			if(freeSpace() > 0 && energy == 0 && powerFramework.getEnergyStored() > 0) {
				energy = 1;
			}
			if(powerFramework.useEnergy(energy, energy, false) == energy) {
				powerFramework.useEnergy(energy, energy, true);
				addEnergy(energy * BuildCraftMultiplier);
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
	public void doWork(PowerHandler p) {}


	@Override
	public int getPowerLevel() {
		return internalStorage;
	}

	public int getChargeState() {
		return internalStorage * 100 / MAX_STORAGE;
	}

	public Container createContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player, null, this);
		dummy.addNormalSlotsForPlayerInventory(8, 80);
		return dummy;
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
//TODO 	MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.HUD_START_WATCHING_BLOCK, xCoord, yCoord, zCoord).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartBlockWatchingPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.HUD_STOP_WATCHING_BLOCK, xCoord, yCoord, zCoord).getPacket());
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
	public boolean isExistent() {
		return getWorld().getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}
	
	@Override
	public void func_85027_a(CrashReportCategory par1CrashReportCategory) {
		super.func_85027_a(par1CrashReportCategory);
		par1CrashReportCategory.addCrashSection("LP-Version", LogisticsPipes.VERSION);
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
		return true;
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public boolean isAddedToEnergyNet() {
		return addedToEnergyNet;
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public int demandsEnergy() {
		if(!addedToEnergyNet) return 0;
		if(internalBuffer > 0 && freeSpace() > 0) {
			internalBuffer = injectEnergy(null, internalBuffer);
		}
		return freeSpace();
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public int injectEnergy(Direction directionFrom, int amount) {
		int addAmount = Math.min(amount, freeSpace() / IC2Multiplier);
		if(freeSpace() > 0 && addAmount == 0) {
			addAmount = 1;
		}
		if(!addedToEnergyNet) addAmount = 0;
		addEnergy(addAmount * IC2Multiplier);
		if(addAmount == 0 && directionFrom != null) {
			internalBuffer += amount;
			return 0;
		}
		return amount - addAmount;
	}

	@Override
	@ModDependentMethod(modId="IC2")
	public int getMaxSafeInput() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft")
	public String getType() {
	return "LogisticsPowerJunction";
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft")
	public String[] getMethodNames() {
	return new String[]{"getPowerLevel"};
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception {
	return new Object[]{this.getPowerLevel()};
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft")
	public boolean canAttachToSide(int side) {
	return true;
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft")
	public void attach(IComputerAccess computer) {}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft")
	public void detach(IComputerAccess computer) {}
	
	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerFramework.getPowerReceiver();
	}
}
