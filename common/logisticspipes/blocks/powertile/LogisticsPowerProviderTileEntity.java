package logisticspipes.blocks.powertile;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDPowerLevel;
import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IHeadUpDisplayBlockRendererProvider;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IPowerLevelDisplay;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.PowerPacketLaser;
import logisticspipes.network.packets.block.PowerProviderLevel;
import logisticspipes.network.packets.hud.HUDStartBlockWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopBlockWatchingPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class LogisticsPowerProviderTileEntity extends TileEntity implements ISubSystemPowerProvider, IPowerLevelDisplay, IGuiOpenControler, IHeadUpDisplayBlockRendererProvider, IBlockWatchingHandler {
	public static final int BC_COLOR = 0x00ffff;
	public static final int RF_COLOR = 0xff0000;
	public static final int IC2_COLOR = 0xffff00;
	
	
	// true if it needs more power, turns off at full, turns on at 50%.
	public boolean needMorePowerTriggerCheck = true;

	protected Map<Integer, Float> orders = new HashMap<Integer, Float>();
	protected BitSet reOrdered = new BitSet(ServerRouter.getBiggestSimpleID());
	protected boolean pauseRequesting = false;
	
	protected float internalStorage = 0;
  	private float lastUpdateStorage = 0;
	protected int maxMode = 1;

	private PlayerCollectionList guiListener = new PlayerCollectionList();
	private PlayerCollectionList watcherList = new PlayerCollectionList();
	private IHeadUpDisplayRenderer HUD;
	private boolean init = false;
	
	protected LogisticsPowerProviderTileEntity() {
		HUD = new HUDPowerLevel(this);
	}
	
	@Override
	public void updateEntity() {
		pauseRequesting = false;
		if(MainProxy.isServer(worldObj) && this.getBrand().equals("EU")) {
			System.out.print("");
		}
		if(!init) {
			if(MainProxy.isClient(getWorld())) {
				LogisticsHUDRenderer.instance().add(this);
			}
			init = true;
		}
		float globalRequest = 0;
		for(Entry<Integer, Float> order:orders.entrySet()) {
			globalRequest += order.getValue();
		}
		if(globalRequest > 0) {
			float fullfullratio = Math.min(1, Math.min(internalStorage, getMaxProvidePerTick()) / globalRequest);
			if(fullfullratio > 0) {
				for(Entry<Integer, Float> order:orders.entrySet()) {
					float toSend = order.getValue() * fullfullratio;
					if(toSend > internalStorage) {
						toSend = internalStorage;
					}
					IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(order.getKey());
					if(destinationRouter != null && destinationRouter.getPipe() != null) {
						WorldUtil util = new WorldUtil(getWorldObj(), xCoord, yCoord, zCoord);
						outerTiles:
						for(AdjacentTile adjacent: util.getAdjacentTileEntities(false)) {
							if(adjacent.tile instanceof LogisticsTileGenericPipe) {
								if(((LogisticsTileGenericPipe)adjacent.tile).pipe instanceof CoreRoutedPipe) {
									IRouter sourceRouter = ((CoreRoutedPipe)((LogisticsTileGenericPipe)adjacent.tile).pipe).getRouter();
									if(sourceRouter != null) {
										outerRouters:
										for(ExitRoute exit:sourceRouter.getDistanceTo(destinationRouter)) {
											if(exit.containsFlag(PipeRoutingConnectionType.canPowerSubSystemFrom)) {
												for(IFilter filter:exit.filters) {
													if(filter.blockPower()) continue outerRouters;
												}
												MainProxy.sendPacketToAllWatchingChunk(xCoord, zCoord, sourceRouter.getDimension(), PacketHandler.getPacket(PowerPacketLaser.class).setColor(this.getLaserColor()).setPos(sourceRouter.getLPPosition()).setDir(adjacent.orientation.getOpposite()).setReverse(true).setLength(1));
												sendPowerLaserPackets(sourceRouter, destinationRouter, exit.exitOrientation);
												internalStorage -= toSend;
												handlePower(destinationRouter.getPipe(), toSend);
												break outerTiles;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		orders.clear();
		if(MainProxy.isServer(worldObj)) {
			if(internalStorage != lastUpdateStorage) {
				updateClients();
				lastUpdateStorage = internalStorage;
			}
		}
	}

	protected abstract void handlePower(CoreRoutedPipe pipe, float toSend);

	private void sendPowerLaserPackets(IRouter sourceRouter, IRouter destinationRouter, ForgeDirection exitOrientation) {
		if(sourceRouter == destinationRouter) return;
		List<ExitRoute> exits = sourceRouter.getRoutersOnSide(exitOrientation);
		for(ExitRoute exit:exits) {
			if(exit.containsFlag(PipeRoutingConnectionType.canPowerSubSystemFrom)) { // Find only result (caused by only straight connections)
				int distance = sourceRouter.getDistanceToNextPowerPipe(exit.exitOrientation);
				MainProxy.sendPacketToAllWatchingChunk(xCoord, zCoord, sourceRouter.getDimension(), PacketHandler.getPacket(PowerPacketLaser.class).setColor(this.getLaserColor()).setPos(sourceRouter.getLPPosition()).setDir(exit.exitOrientation).setRenderBall(true).setLength(distance));
				sourceRouter = exit.destination; // Use new sourceRouter
				if(sourceRouter == destinationRouter) return;
				outerRouters:
				for(ExitRoute newExit:sourceRouter.getDistanceTo(destinationRouter)) {
					if(newExit.containsFlag(PipeRoutingConnectionType.canPowerSubSystemFrom)) {
						for(IFilter filter:newExit.filters) {
							if(filter.blockPower()) continue outerRouters;
						}
						sendPowerLaserPackets(sourceRouter, destinationRouter, newExit.exitOrientation);
					}
				}
			}
		}
	}

	protected abstract float getMaxProvidePerTick();
	
	protected abstract int getLaserColor();

	@Override
	public void invalidate() {
		super.invalidate();
		if(MainProxy.isClient(this.getWorld())) {
			LogisticsHUDRenderer.instance().remove(this);
		}
	}

	@Override
	public void validate() {
		super.validate();
		if(MainProxy.isClient(this.getWorld())) {
			init = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(MainProxy.isClient(this.getWorld())) {
			LogisticsHUDRenderer.instance().remove(this);
		}
	}
	
	@Override
	public void requestPower(int destination, float amount) {
		if(pauseRequesting) return;
		if(this.getBrand().equals("EU")) {
			System.out.print("");
		}
		if(orders.containsKey(destination)) {
			if(reOrdered.get(destination)) {
				pauseRequesting = true;
				reOrdered.clear();
			} else {
				reOrdered.set(destination);
			}
		} else {
			reOrdered.clear();
		}
		orders.put(destination, amount);
	}

	@Override
	public float getPowerLevel() {
		return lastUpdateStorage;
	}

	@Override
	public boolean usePaused() {
		return pauseRequesting;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		internalStorage = nbt.getFloat("internalStorage");
		maxMode = nbt.getInteger("maxMode");
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setFloat("internalStorage", internalStorage);
		nbt.setInteger("maxMode", maxMode);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
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
		return getWorldObj();
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
	public void guiOpenedByPlayer(EntityPlayer player) {
		guiListener.add(player);
		updateClients();
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		guiListener.remove(player);
	}
	
	public void updateClients() {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(PowerProviderLevel.class).setFloat(internalStorage).setTilePos(this), guiListener);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(PowerProviderLevel.class).setFloat(internalStorage).setTilePos(this), watcherList);
	}
	
	@Override
	public void func_85027_a(CrashReportCategory par1CrashReportCategory) {
		super.func_85027_a(par1CrashReportCategory);
		par1CrashReportCategory.addCrashSection("LP-Version", LogisticsPipes.VERSION);
	}

	public Container createContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player, null, this);
		dummy.addNormalSlotsForPlayerInventory(8, 80);
		return dummy;
	}

	public void handlePowerPacket(float float1) {
		if(MainProxy.isClient(this.getWorld())) {
			internalStorage = float1;
		}
	}

	@Override
	public int getChargeState() {
		return (int) Math.min(100F, internalStorage * 100 / getMaxStorage());
	}

	@Override
	public int getDisplayPowerLevel() {
		return Math.round(internalStorage);
	}

	@Override
	public boolean isHUDInvalid() {
		return this.isInvalid();
	}
}
