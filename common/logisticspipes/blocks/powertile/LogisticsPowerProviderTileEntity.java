package logisticspipes.blocks.powertile;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LPConstants;
import logisticspipes.blocks.LogisticsSolidTileEntity;
import logisticspipes.gui.hud.HUDPowerLevel;
import logisticspipes.interfaces.IBlockWatchingHandler;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.interfaces.IHeadUpDisplayBlockRendererProvider;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IPowerLevelDisplay;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.guis.block.PowerProviderGui;
import logisticspipes.network.packets.block.PowerProviderLevel;
import logisticspipes.network.packets.hud.HUDStartBlockWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopBlockWatchingPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

@CCType(name = "LogisticsPowerProvider")
public abstract class LogisticsPowerProviderTileEntity extends LogisticsSolidTileEntity implements IGuiTileEntity, ISubSystemPowerProvider, IPowerLevelDisplay, IGuiOpenControler, IHeadUpDisplayBlockRendererProvider, IBlockWatchingHandler {

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
		super.updateEntity();
		pauseRequesting = false;
		if (!init) {
			if (MainProxy.isClient(getWorld())) {
				LogisticsHUDRenderer.instance().add(this);
			}
			init = true;
		}
		float globalRequest = 0;
		for (Entry<Integer, Float> order : orders.entrySet()) {
			globalRequest += order.getValue();
		}
		if (globalRequest > 0) {
			float fullfullratio = Math.min(1, Math.min(internalStorage, getMaxProvidePerTick()) / globalRequest);
			if (fullfullratio > 0) {
				for (Entry<Integer, Float> order : orders.entrySet()) {
					float toSend = order.getValue() * fullfullratio;
					if (toSend > internalStorage) {
						toSend = internalStorage;
					}
					IRouter destinationRouter = SimpleServiceLocator.routerManager.getRouter(order.getKey());
					if (destinationRouter != null && destinationRouter.getPipe() != null) {
						WorldUtil util = new WorldUtil(getWorldObj(), xCoord, yCoord, zCoord);
						outerTiles:
							for (AdjacentTile adjacent : util.getAdjacentTileEntities(false)) {
								if (adjacent.tile instanceof LogisticsTileGenericPipe) {
									if (((LogisticsTileGenericPipe) adjacent.tile).pipe instanceof CoreRoutedPipe) {
										if (((CoreRoutedPipe) ((LogisticsTileGenericPipe) adjacent.tile).pipe).stillNeedReplace()) {
											continue;
										}
										IRouter sourceRouter = ((CoreRoutedPipe) ((LogisticsTileGenericPipe) adjacent.tile).pipe).getRouter();
										if (sourceRouter != null) {
											outerRouters:
												for (ExitRoute exit : sourceRouter.getDistanceTo(destinationRouter)) {
													if (exit.containsFlag(PipeRoutingConnectionType.canPowerSubSystemFrom)) {
														for (IFilter filter : exit.filters) {
															if (filter.blockPower()) {
																continue outerRouters;
															}
														}
														CoreRoutedPipe pipe = sourceRouter.getPipe();
														if (pipe != null && pipe.isInitialized()) {
															pipe.container.addLaser(adjacent.orientation.getOpposite(), 1, getLaserColor(), true, true);
														}
														sendPowerLaserPackets(sourceRouter, destinationRouter, exit.exitOrientation, exit.exitOrientation != adjacent.orientation);
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
		if (MainProxy.isServer(worldObj)) {
			if (internalStorage != lastUpdateStorage) {
				updateClients();
				lastUpdateStorage = internalStorage;
			}
		}
	}

	protected abstract void handlePower(CoreRoutedPipe pipe, float toSend);

	private void sendPowerLaserPackets(IRouter sourceRouter, IRouter destinationRouter, ForgeDirection exitOrientation, boolean addBall) {
		if (sourceRouter == destinationRouter) {
			return;
		}
		LinkedList<Triplet<IRouter, ForgeDirection, Boolean>> todo = new LinkedList<Triplet<IRouter, ForgeDirection, Boolean>>();
		todo.add(new Triplet<IRouter, ForgeDirection, Boolean>(sourceRouter, exitOrientation, addBall));
		while (!todo.isEmpty()) {
			Triplet<IRouter, ForgeDirection, Boolean> part = todo.pollFirst();
			List<ExitRoute> exits = part.getValue1().getRoutersOnSide(part.getValue2());
			for (ExitRoute exit : exits) {
				if (exit.containsFlag(PipeRoutingConnectionType.canPowerSubSystemFrom)) { // Find only result (caused by only straight connections)
					int distance = part.getValue1().getDistanceToNextPowerPipe(exit.exitOrientation);
					CoreRoutedPipe pipe = part.getValue1().getPipe();
					if (pipe != null && pipe.isInitialized()) {
						pipe.container.addLaser(exit.exitOrientation, distance, getLaserColor(), false, part.getValue3());
					}
					IRouter nextRouter = exit.destination; // Use new sourceRouter
					if (nextRouter == destinationRouter) {
						return;
					}
					outerRouters:
						for (ExitRoute newExit : nextRouter.getDistanceTo(destinationRouter)) {
							if (newExit.containsFlag(PipeRoutingConnectionType.canPowerSubSystemFrom)) {
								for (IFilter filter : newExit.filters) {
									if (filter.blockPower()) {
										continue outerRouters;
									}
								}
								todo.addLast(new Triplet<IRouter, ForgeDirection, Boolean>(nextRouter, newExit.exitOrientation, newExit.exitOrientation != exit.exitOrientation));
							}
						}
				}
			}
		}
	}

	protected abstract float getMaxProvidePerTick();

	@CCCommand(description = "Returns the color for the power provided by this power provider")
	protected abstract int getLaserColor();

	@Override
	@CCCommand(description = "Returns the max. amount of storable power")
	public abstract int getMaxStorage();

	@Override
	@CCCommand(description = "Returns the power type stored in this power provider")
	public abstract String getBrand();

	@Override
	public void invalidate() {
		super.invalidate();
		if (MainProxy.isClient(getWorld())) {
			LogisticsHUDRenderer.instance().remove(this);
		}
	}

	@Override
	public void validate() {
		super.validate();
		if (MainProxy.isClient(getWorld())) {
			init = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (MainProxy.isClient(getWorld())) {
			LogisticsHUDRenderer.instance().remove(this);
		}
	}

	@Override
	public void requestPower(int destination, float amount) {
		if (pauseRequesting) {
			return;
		}
		if (getBrand().equals("EU")) {
			System.out.print("");
		}
		if (orders.containsKey(destination)) {
			if (reOrdered.get(destination)) {
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
	@CCCommand(description = "Returns the current power level for this power provider")
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
	public void func_145828_a(CrashReportCategory par1CrashReportCategory) {
		super.func_145828_a(par1CrashReportCategory);
		par1CrashReportCategory.addCrashSection("LP-Version", LPConstants.VERSION);
	}

	public void handlePowerPacket(float float1) {
		if (MainProxy.isClient(getWorld())) {
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
		return isInvalid();
	}

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(PowerProviderGui.class);
	}
}
