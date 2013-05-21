package logisticspipes.blocks.powertile;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.config.Configs;
import logisticspipes.gui.hud.HUDPowerJunction;
import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IHeadUpDisplayBlockRendererProvider;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;



public class LogisticsPowerJuntionTileEntity_BuildCraft extends TileEntity implements IPowerReceptor, ILogisticsPowerProvider, IGuiOpenControler, IHeadUpDisplayBlockRendererProvider, IBlockWatchingHandler {
	
	// true if it needs more power, turns off at full, turns on at 50%.
	public boolean needMorePowerTriggerCheck = true;
	
	public final int BuildCraftMultiplier = 5;
	public final int MAX_STORAGE = 2000000;
	
	private IPowerProvider powerFramework;
	
	private List<EntityPlayer> guiListener = new ArrayList<EntityPlayer>();
	
	private int internalStorage = 0;
  	private int lastUpdateStorage = 0;
	
	private boolean init = false;
	private List<EntityPlayer> watcherList = new ArrayList<EntityPlayer>();
	private IHeadUpDisplayRenderer HUD;
	
	public LogisticsPowerJuntionTileEntity_BuildCraft() {
		powerFramework = PowerFramework.currentFramework.createPowerProvider();
		powerFramework.configure(0, 1, 250, 1, 750);
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
		return false;	}

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
		MainProxy.sendToPlayerList(new PacketPipeInteger(NetworkConstants.POWER_JUNCTION_POWER_LEVEL, xCoord, yCoord, zCoord, internalStorage).getPacket(), guiListener);
		MainProxy.sendToPlayerList(new PacketPipeInteger(NetworkConstants.POWER_JUNCTION_POWER_LEVEL, xCoord, yCoord, zCoord, internalStorage).getPacket(), watcherList);
		lastUpdateStorage = internalStorage;
	}
	
	@Override
	public boolean canUseEnergy(int amount) {
		return canUseEnergy(amount,null);
	}
	
	public void addEnergy(float amount) {
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
		if(MainProxy.isServer(worldObj)) {
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
			if(MainProxy.isClient(worldObj)) {
				LogisticsHUDRenderer.instance().add(this);
			}
			init = true;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if(MainProxy.isClient(this.worldObj)) {
			LogisticsHUDRenderer.instance().remove(this);
		}
	}

	@Override
	public void validate() {
		super.validate();
		if(MainProxy.isClient(this.worldObj)) {
			init = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(MainProxy.isClient(this.worldObj)) {
			LogisticsHUDRenderer.instance().remove(this);
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
	public int powerRequest(ForgeDirection from) {
		return Math.min(powerFramework.getMaxEnergyReceived(), freeSpace() / BuildCraftMultiplier);
	}

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

	public void handlePowerPacket(PacketPipeInteger packet) {
		if(MainProxy.isClient(this.worldObj)) {
			internalStorage = packet.integer;
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
		return worldObj;
	}

	@Override
	public void startWaitching() {
		MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.HUD_START_WATCHING_BLOCK, xCoord, yCoord, zCoord).getPacket());
	}

	@Override
	public void stopWaitching() {
		MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.HUD_STOP_WATCHING_BLOCK, xCoord, yCoord, zCoord).getPacket());
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
	public boolean isExistend() {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}
}
