/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IClientState;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILPPositionProvider;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.interfaces.IQueueCCEvent;
import logisticspipes.interfaces.ISecurityProvider;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.ItemPipeSignCreator;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IAdjacentWorldAccess;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.ITrackStatistics;
import logisticspipes.logisticspipes.PipeTransportLayer;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.pipe.PipeController;
import logisticspipes.network.packets.pipe.ParticleFX;
import logisticspipes.network.packets.pipe.PipeSignTypes;
import logisticspipes.network.packets.pipe.RequestRoutingLasersPacket;
import logisticspipes.network.packets.pipe.RequestSignPacket;
import logisticspipes.network.packets.pipe.StatUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.pipes.signs.IPipeSign;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.CCConstants;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCDirectCall;
import logisticspipes.proxy.computers.interfaces.CCSecurtiyCheck;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.IRouterQueuedTask;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LogisticsItemOrderManager;
import logisticspipes.routing.order.LogisticsOrderManager;
import logisticspipes.security.PermissionException;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;

@CCType(name = "LogisticsPipes:Normal")
public abstract class CoreRoutedPipe extends CoreUnroutedPipe implements IClientState, IRequestItems, IAdjacentWorldAccess, ITrackStatistics, IWorldProvider, IWatchingHandler, IPipeServiceProvider, IQueueCCEvent, ILPPositionProvider {

	public enum ItemSendMode {
		Normal,
		Fast
	}

	protected boolean stillNeedReplace = true;
	private boolean recheckConnections = false;

	protected IRouter router;
	protected String routerId;
	protected Object routerIdLock = new Object();
	private static int pipecount = 0;
	protected int _delayOffset = 0;

	public boolean _textureBufferPowered;

	protected boolean _initialInit = true;

	private boolean enabled = true;
	private boolean preventRemove = false;
	private boolean destroyByPlayer = false;
	private PowerSupplierHandler powerHandler = new PowerSupplierHandler(this);

	public long delayTo = 0;
	public int repeatFor = 0;

	protected RouteLayer _routeLayer;
	protected TransportLayer _transportLayer;
	protected final PriorityBlockingQueue<ItemRoutingInformation> _inTransitToMe = new PriorityBlockingQueue<ItemRoutingInformation>(10, new ItemRoutingInformation.DelayComparator());

	protected UpgradeManager upgradeManager = new UpgradeManager(this);
	protected LogisticsItemOrderManager _orderItemManager = null;

	@Getter
	private List<IOrderInfoProvider> clientSideOrderManager = new ArrayList<IOrderInfoProvider>();

	public int stat_session_sent;
	public int stat_session_recieved;
	public int stat_session_relayed;

	public long stat_lifetime_sent;
	public long stat_lifetime_recieved;
	public long stat_lifetime_relayed;

	public int server_routing_table_size = 0;

	protected final LinkedList<Triplet<IRoutedItem, ForgeDirection, ItemSendMode>> _sendQueue = new LinkedList<Triplet<IRoutedItem, ForgeDirection, ItemSendMode>>();

	protected final Map<ItemIdentifier, Queue<Pair<Integer, ItemRoutingInformation>>> queuedDataForUnroutedItems = Collections.synchronizedMap(new TreeMap<ItemIdentifier, Queue<Pair<Integer, ItemRoutingInformation>>>());

	public final PlayerCollectionList watchers = new PlayerCollectionList();

	protected List<IInventory> _cachedAdjacentInventories;

	protected ForgeDirection pointedDirection = ForgeDirection.UNKNOWN;
	//public BaseRoutingLogic logic;
	// from BaseRoutingLogic
	protected int throttleTime = 20;
	private int throttleTimeLeft = 20 + new Random().nextInt(Configs.LOGISTICS_DETECTION_FREQUENCY);

	private int[] queuedParticles = new int[Particles.values().length];
	private boolean hasQueuedParticles = false;

	protected IPipeSign[] signItem = new IPipeSign[6];
	private boolean isOpaqueClientSide = false;

	private CacheHolder cacheHolder;

	public CoreRoutedPipe(Item item) {
		this(new PipeTransportLogistics(true), item);
	}

	public CoreRoutedPipe(PipeTransportLogistics transport, Item item) {
		super(transport, item);

		CoreRoutedPipe.pipecount++;

		//Roughly spread pipe updates throughout the frequency, no need to maintain balance
		_delayOffset = CoreRoutedPipe.pipecount % Configs.LOGISTICS_DETECTION_FREQUENCY;
	}

	public RouteLayer getRouteLayer() {
		if (_routeLayer == null) {
			_routeLayer = new RouteLayer(getRouter(), getTransportLayer(), this);
		}
		return _routeLayer;
	}

	public TransportLayer getTransportLayer() {
		if (_transportLayer == null) {
			_transportLayer = new PipeTransportLayer(this, this, getRouter());
		}
		return _transportLayer;
	}

	@Override
	public ISlotUpgradeManager getUpgradeManager(ModulePositionType slot, int positionInt) {
		return upgradeManager;
	}

	@Override
	public IPipeUpgradeManager getUpgradeManager() {
		return upgradeManager;
	}

	public UpgradeManager getOriginalUpgradeManager() {
		return upgradeManager;
	}

	@Override
	public void queueRoutedItem(IRoutedItem routedItem, ForgeDirection from) {
		if (from == null) {
			throw new NullPointerException();
		}
		_sendQueue.addLast(new Triplet<IRoutedItem, ForgeDirection, ItemSendMode>(routedItem, from, ItemSendMode.Normal));
		sendQueueChanged(false);
	}

	public void queueRoutedItem(IRoutedItem routedItem, ForgeDirection from, ItemSendMode mode) {
		if (from == null) {
			throw new NullPointerException();
		}
		_sendQueue.addLast(new Triplet<IRoutedItem, ForgeDirection, ItemSendMode>(routedItem, from, mode));
		sendQueueChanged(false);
	}

	/**
	 * @param force
	 *            == true never delegates to a thread
	 * @return number of things sent.
	 */
	public int sendQueueChanged(boolean force) {
		return 0;
	}

	private void sendRoutedItem(IRoutedItem routedItem, ForgeDirection from) {

		if (from == null) {
			throw new NullPointerException();
		}

		transport.injectItem(routedItem, from.getOpposite());

		IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(routedItem.getDestination(), false);
		if (r != null) {
			CoreRoutedPipe pipe = r.getCachedPipe();
			if (pipe != null) {
				pipe.notifyOfSend(routedItem.getInfo());
			} else {
				// TODO: handle sending items to known chunk-unloaded destination?
			}
		} // should not be able to send to a non-existing router
		// router.startTrackingRoutedItem((RoutedEntityItem) routedItem.getTravelingItem());
		spawnParticle(Particles.OrangeParticle, 2);
		stat_lifetime_sent++;
		stat_session_sent++;
		updateStats();
	}

	private void notifyOfSend(ItemRoutingInformation routedItem) {
		_inTransitToMe.add(routedItem);
		//LogisticsPipes.log.info("Sending: "+routedItem.getIDStack().getItem().getFriendlyName());
	}

	public void notifyOfReroute(ItemRoutingInformation routedItem) {
		_inTransitToMe.remove(routedItem);
	}

	//When Recreating the Item from the TE version we have the same hashCode but a different instance so we need to refresh this
	public void refreshItem(ItemRoutingInformation routedItem) {
		if (_inTransitToMe.contains(routedItem)) {
			_inTransitToMe.remove(routedItem);
			_inTransitToMe.add(routedItem);
		}
	}

	public abstract ItemSendMode getItemSendMode();

	/**
	 * Designed to help protect against routing loops - if both pipes are on the
	 * same block, and of ISided overlapps, return true
	 * 
	 * @param other
	 * @return boolean indicating if both pull from the same inventory.
	 */
	public boolean sharesInterestWith(CoreRoutedPipe other) {
		List<IInventory> others = other.getConnectedRawInventories();
		if (others == null || others.size() == 0) {
			return false;
		}
		for (IInventory i : getConnectedRawInventories()) {
			if (others.contains(i)) {
				return true;
			}
		}
		return false;
	}

	protected List<IInventory> getConnectedRawInventories() {
		if (_cachedAdjacentInventories != null) {
			return _cachedAdjacentInventories;
		}
		WorldUtil worldUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		LinkedList<IInventory> adjacent = new LinkedList<IInventory>();
		for (AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			if (!(tile.tile instanceof IInventory)) {
				continue;
			}
			adjacent.add(InventoryHelper.getInventory((IInventory) tile.tile));
		}
		_cachedAdjacentInventories = adjacent;
		return _cachedAdjacentInventories;
	}

	/***
	 * first tick just create a router and do nothing.
	 */
	public void firstInitialiseTick() {
		getRouter();
		if (MainProxy.isClient(getWorld())) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSignPacket.class).setTilePos(container));
		}
	}

	/***
	 * Only Called Server Side Only Called when the pipe is enabled
	 */
	public void enabledUpdateEntity() {
		powerHandler.update();
		for (int i = 0; i < 6; i++) {
			if (signItem[i] != null) {
				signItem[i].updateServerSide();
			}
		}
	}

	/***
	 * Called Server and Client Side Called every tick
	 */
	public void ignoreDisableUpdateEntity() {}

	@Override
	public final void updateEntity() {
		debug.tick();
		spawnParticleTick();
		if (stillNeedReplace) {
			stillNeedReplace = false;
			getWorld().notifyBlockChange(getX(), getY(), getZ(), getWorld().getBlock(getX(), getY(), getZ()));
			/* TravelingItems are just held by a pipe, they don't need to know their world
			 * for(Triplet<IRoutedItem, ForgeDirection, ItemSendMode> item : _sendQueue) {
				//assign world to any entityitem we created in readfromnbt
				item.getValue1().getTravelingItem().setWorld(getWorld());
			}*/
			//first tick just create a router and do nothing.
			firstInitialiseTick();
			return;
		}
		if (repeatFor > 0) {
			if (delayTo < System.currentTimeMillis()) {
				delayTo = System.currentTimeMillis() + 200;
				repeatFor--;
				getWorld().markBlockForUpdate(getX(), getY(), getZ());
			}
		}

		// remove old items _inTransit -- these should have arrived, but have probably been lost instead. In either case, it will allow a re-send so that another attempt to re-fill the inventory can be made.
		while (_inTransitToMe.peek() != null && _inTransitToMe.peek().getTickToTimeOut() <= 0) {
			final ItemRoutingInformation p = _inTransitToMe.poll();
			if (LPConstants.DEBUG) {
				LogisticsPipes.log.info("Timed Out: " + p.getItem().getFriendlyName() + " (" + p.hashCode() + ")");
			}
			debug.log("Timed Out: " + p.getItem().getFriendlyName() + " (" + p.hashCode() + ")");
		}
		//update router before ticking logic/transport
		getRouter().update(getWorld().getTotalWorldTime() % Configs.LOGISTICS_DETECTION_FREQUENCY == _delayOffset || _initialInit || recheckConnections, this);
		recheckConnections = false;
		getOriginalUpgradeManager().securityTick();
		super.updateEntity();

		if (isNthTick(200)) {
			getCacheHolder().trigger(null);
		}

		// from BaseRoutingLogic
		if (--throttleTimeLeft <= 0) {
			throttledUpdateEntity();
			throttleTimeLeft = throttleTime;
		}

		ignoreDisableUpdateEntity();
		_initialInit = false;
		if (!_sendQueue.isEmpty()) {
			if (getItemSendMode() == ItemSendMode.Normal) {
				Triplet<IRoutedItem, ForgeDirection, ItemSendMode> itemToSend = _sendQueue.getFirst();
				sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
				_sendQueue.removeFirst();
				for (int i = 0; i < 16 && !_sendQueue.isEmpty() && _sendQueue.getFirst().getValue3() == ItemSendMode.Fast; i++) {
					if (!_sendQueue.isEmpty()) {
						itemToSend = _sendQueue.getFirst();
						sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
						_sendQueue.removeFirst();
					}
				}
				sendQueueChanged(false);
			} else if (getItemSendMode() == ItemSendMode.Fast) {
				for (int i = 0; i < 16; i++) {
					if (!_sendQueue.isEmpty()) {
						Triplet<IRoutedItem, ForgeDirection, ItemSendMode> itemToSend = _sendQueue.getFirst();
						sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
						_sendQueue.removeFirst();
					}
				}
				sendQueueChanged(false);
			} else if (getItemSendMode() == null) {
				throw new UnsupportedOperationException("getItemSendMode() can't return null. " + this.getClass().getName());
			} else {
				throw new UnsupportedOperationException("getItemSendMode() returned unhandled value. " + getItemSendMode().name() + " in " + this.getClass().getName());
			}
		}
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		checkTexturePowered();
		if (!isEnabled()) {
			return;
		}
		enabledUpdateEntity();
		if (getLogisticsModule() == null) {
			return;
		}
		getLogisticsModule().tick();
	}

	protected void onAllowedRemoval() {}

	// From BaseRoutingLogic
	public void throttledUpdateEntity() {}

	protected void delayThrottle() {
		//delay 6(+1) ticks to prevent suppliers from ticking between a item arriving at them and the item hitting their adj. inv
		if (throttleTimeLeft < 7) {
			throttleTimeLeft = 7;
		}
	}

	@Override
	public boolean isNthTick(int n) {
		return ((getWorld().getTotalWorldTime() + _delayOffset) % n == 0);
	}

	private void doDebugStuff(EntityPlayer entityplayer) {
		//entityplayer.worldObj.setWorldTime(4951);
		IRouter r = getRouter();
		if (!(r instanceof ServerRouter)) {
			return;
		}
		System.out.println("***");
		System.out.println("---------Interests---------------");
		for (Entry<ItemIdentifier, Set<IRouter>> i : ServerRouter.getInterestedInSpecifics().entrySet()) {
			System.out.print(i.getKey().getFriendlyName() + ":");
			for (IRouter j : i.getValue()) {
				System.out.print(j.getSimpleID() + ",");
			}
			System.out.println();
		}

		System.out.print("ALL ITEMS:");
		for (IRouter j : ServerRouter.getInterestedInGeneral()) {
			System.out.print(j.getSimpleID() + ",");
		}
		System.out.println();

		ServerRouter sr = (ServerRouter) r;

		System.out.println(r.toString());
		System.out.println("---------CONNECTED TO---------------");
		for (CoreRoutedPipe adj : sr._adjacent.keySet()) {
			System.out.println(adj.getRouter().getSimpleID());
		}
		System.out.println();
		System.out.println("========DISTANCE TABLE==============");
		for (ExitRoute n : r.getIRoutersByCost()) {
			System.out.println(n.destination.getSimpleID() + " @ " + n.distanceToDestination + " -> " + n.connectionDetails + "(" + n.destination.getId() + ")");
		}
		System.out.println();
		System.out.println("*******EXIT ROUTE TABLE*************");
		List<List<ExitRoute>> table = r.getRouteTable();
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i) != null) {
				if (table.get(i).size() > 0) {
					System.out.println(i + " -> " + table.get(i).get(0).destination.getSimpleID());
					for (ExitRoute route : table.get(i)) {
						System.out.println("\t\t via " + route.exitOrientation + "(" + route.distanceToDestination + " distance)");
					}
				}
			}
		}
		System.out.println();
		System.out.println("++++++++++CONNECTIONS+++++++++++++++");
		System.out.println(Arrays.toString(ForgeDirection.VALID_DIRECTIONS));
		System.out.println(Arrays.toString(sr.sideDisconnected));
		System.out.println(Arrays.toString(container.pipeConnectionsBuffer));
		System.out.println();
		System.out.println("~~~~~~~~~~~~~~~POWER~~~~~~~~~~~~~~~~");
		System.out.println(r.getPowerProvider());
		System.out.println();
		System.out.println("~~~~~~~~~~~SUBSYSTEMPOWER~~~~~~~~~~~");
		System.out.println(r.getSubSystemPowerProvider());
		System.out.println();
		if(_orderItemManager != null) {
			System.out.println("################ORDERDUMP#################");
			_orderItemManager.dump();
		}
		System.out.println("################END#################");
		refreshConnectionAndRender(true);
		System.out.print("");
		sr.CreateRouteTable(Integer.MAX_VALUE);
	}

	// end FromBaseRoutingLogic

	@Override
	public final void onBlockRemoval() {
		try {
			onAllowedRemoval();
			super.onBlockRemoval();
			//invalidate() removes the router
			//				if (logic instanceof BaseRoutingLogic){
			//					((BaseRoutingLogic)logic).destroy();
			//				}
			//Just in case
			CoreRoutedPipe.pipecount = Math.max(CoreRoutedPipe.pipecount - 1, 0);

			if (transport != null && transport instanceof PipeTransportLogistics) {
				transport.dropBuffer();
			}
			getOriginalUpgradeManager().dropUpgrades();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (router != null) {
			router.destroy();
			router = null;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (router != null) {
			router.clearPipeCache();
			router.clearInterests();
		}
	}

	public void checkTexturePowered() {
		if (Configs.LOGISTICS_POWER_USAGE_DISABLED) {
			return;
		}
		if (!isNthTick(10)) {
			return;
		}
		if (stillNeedReplace || _initialInit || router == null) {
			return;
		}
		boolean flag;
		if ((flag = canUseEnergy(1)) != _textureBufferPowered) {
			_textureBufferPowered = flag;
			refreshRender(false);
			spawnParticle(Particles.RedParticle, 3);
		}
	}

	@Override
	public int getTextureIndex() {
		return getCenterTexture().newTexture;
	}

	public abstract TextureType getCenterTexture();

	public TextureType getTextureType(ForgeDirection connection) {
		if (stillNeedReplace || _initialInit) {
			return getCenterTexture();
		}

		if (connection == ForgeDirection.UNKNOWN) {
			return getCenterTexture();
		} else if ((router != null) && getRouter().isRoutedExit(connection)) {
			return getRoutedTexture(connection);
		} else {
			TextureType texture = getNonRoutedTexture(connection);
			if (this.getUpgradeManager().hasRFPowerSupplierUpgrade() || this.getUpgradeManager().getIC2PowerLevel() > 0) {
				if (texture.fileName.equals(Textures.LOGISTICSPIPE_NOTROUTED_TEXTURE.fileName)) {
					texture = Textures.LOGISTICSPIPE_NOTROUTED_POWERED_TEXTURE;
				} else if (texture.fileName.equals(Textures.LOGISTICSPIPE_LIQUID_TEXTURE.fileName)) {
					texture = Textures.LOGISTICSPIPE_LIQUID_POWERED_TEXTURE;
				} else if (texture.fileName.equals(Textures.LOGISTICSPIPE_POWERED_TEXTURE.fileName)) {
					texture = Textures.LOGISTICSPIPE_POWERED_POWERED_TEXTURE;
				} else if (texture.fileName.equals(Textures.LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE.fileName)) {
					texture = Textures.LOGISTICSPIPE_NOTROUTED_POWERED_TEXTURE;
				} else if (texture.fileName.equals(Textures.LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE.fileName)) {
					texture = Textures.LOGISTICSPIPE_DIRECTION_POWERED_TEXTURE;
				} else {
					System.out.println("Unknown texture to power, :" + texture.fileName);
					System.out.println(this.getClass());
					System.out.println(connection);
				}
			}
			return texture;
		}
	}

	public TextureType getRoutedTexture(ForgeDirection connection) {
		if (getRouter().isSubPoweredExit(connection)) {
			return Textures.LOGISTICSPIPE_SUBPOWER_TEXTURE;
		} else {
			return Textures.LOGISTICSPIPE_ROUTED_TEXTURE;
		}
	}

	public TextureType getNonRoutedTexture(ForgeDirection connection) {
		if (isPowerProvider(connection)) {
			return Textures.LOGISTICSPIPE_POWERED_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_NOTROUTED_TEXTURE;
	}

	@Override
	public void spawnParticle(Particles particle, int amount) {
		if (!Configs.ENABLE_PARTICLE_FX) {
			return;
		}
		queuedParticles[particle.ordinal()] += amount;
		hasQueuedParticles = true;
	}

	private void spawnParticleTick() {
		if (!hasQueuedParticles) {
			return;
		}
		if (MainProxy.isServer(getWorld())) {
			ArrayList<ParticleCount> tosend = new ArrayList<ParticleCount>(queuedParticles.length);
			for (int i = 0; i < queuedParticles.length; i++) {
				if (queuedParticles[i] > 0) {
					tosend.add(new ParticleCount(Particles.values()[i], queuedParticles[i]));
				}
			}
			MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(getWorld()), PacketHandler.getPacket(ParticleFX.class).setParticles(tosend).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			if (Minecraft.isFancyGraphicsEnabled()) {
				for (int i = 0; i < queuedParticles.length; i++) {
					if (queuedParticles[i] > 0) {
						PipeFXRenderHandler.spawnGenericParticle(Particles.values()[i], getX(), getY(), getZ(), queuedParticles[i]);
					}
				}
			}
		}
		for (int i = 0; i < queuedParticles.length; i++) {
			queuedParticles[i] = 0;
		}
		hasQueuedParticles = false;
	}

	protected boolean isPowerProvider(ForgeDirection ori) {
		TileEntity tilePipe = container.getTile(ori);
		if (tilePipe == null || !container.canPipeConnect(tilePipe, ori)) {
			return false;
		}

		if (tilePipe instanceof ILogisticsPowerProvider || tilePipe instanceof ISubSystemPowerProvider) {
			return true;
		}
		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		synchronized (routerIdLock) {
			if (routerId == null || routerId.isEmpty()) {
				if (router != null) {
					routerId = router.getId().toString();
				} else {
					routerId = UUID.randomUUID().toString();
				}
			}
		}
		nbttagcompound.setString("routerId", routerId);
		nbttagcompound.setLong("stat_lifetime_sent", stat_lifetime_sent);
		nbttagcompound.setLong("stat_lifetime_recieved", stat_lifetime_recieved);
		nbttagcompound.setLong("stat_lifetime_relayed", stat_lifetime_relayed);
		if (getLogisticsModule() != null) {
			getLogisticsModule().writeToNBT(nbttagcompound);
		}
		NBTTagCompound upgradeNBT = new NBTTagCompound();
		upgradeManager.writeToNBT(upgradeNBT);
		nbttagcompound.setTag("upgradeManager", upgradeNBT);

		NBTTagCompound powerNBT = new NBTTagCompound();
		powerHandler.writeToNBT(powerNBT);
		if (!powerNBT.hasNoTags()) {
			nbttagcompound.setTag("powerHandler", powerNBT);
		}

		NBTTagList sendqueue = new NBTTagList();
		for (Triplet<IRoutedItem, ForgeDirection, ItemSendMode> p : _sendQueue) {
			NBTTagCompound tagentry = new NBTTagCompound();
			NBTTagCompound tagentityitem = new NBTTagCompound();
			p.getValue1().writeToNBT(tagentityitem);
			tagentry.setTag("entityitem", tagentityitem);
			tagentry.setByte("from", (byte) (p.getValue2().ordinal()));
			tagentry.setByte("mode", (byte) (p.getValue3().ordinal()));
			sendqueue.appendTag(tagentry);
		}
		nbttagcompound.setTag("sendqueue", sendqueue);

		for (int i = 0; i < 6; i++) {
			if (signItem[i] != null) {
				nbttagcompound.setBoolean("PipeSign_" + i, true);
				int signType = -1;
				List<Class<? extends IPipeSign>> typeClasses = ItemPipeSignCreator.signTypes;
				for (int j = 0; j < typeClasses.size(); j++) {
					if (typeClasses.get(j) == signItem[i].getClass()) {
						signType = j;
						break;
					}
				}
				nbttagcompound.setInteger("PipeSign_" + i + "_type", signType);
				NBTTagCompound tag = new NBTTagCompound();
				signItem[i].writeToNBT(tag);
				nbttagcompound.setTag("PipeSign_" + i + "_tags", tag);
			} else {
				nbttagcompound.setBoolean("PipeSign_" + i, false);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		synchronized (routerIdLock) {
			routerId = nbttagcompound.getString("routerId");
		}

		stat_lifetime_sent = nbttagcompound.getLong("stat_lifetime_sent");
		stat_lifetime_recieved = nbttagcompound.getLong("stat_lifetime_recieved");
		stat_lifetime_relayed = nbttagcompound.getLong("stat_lifetime_relayed");
		if (getLogisticsModule() != null) {
			getLogisticsModule().readFromNBT(nbttagcompound);
		}
		upgradeManager.readFromNBT(nbttagcompound.getCompoundTag("upgradeManager"));
		powerHandler.readFromNBT(nbttagcompound.getCompoundTag("powerHandler"));

		_sendQueue.clear();
		NBTTagList sendqueue = nbttagcompound.getTagList("sendqueue", nbttagcompound.getId());
		for (int i = 0; i < sendqueue.tagCount(); i++) {
			NBTTagCompound tagentry = sendqueue.getCompoundTagAt(i);
			NBTTagCompound tagentityitem = tagentry.getCompoundTag("entityitem");
			LPTravelingItemServer item = new LPTravelingItemServer(tagentityitem);
			ForgeDirection from = ForgeDirection.values()[tagentry.getByte("from")];
			ItemSendMode mode = ItemSendMode.values()[tagentry.getByte("mode")];
			_sendQueue.add(new Triplet<IRoutedItem, ForgeDirection, ItemSendMode>(item, from, mode));
		}
		for (int i = 0; i < 6; i++) {
			if (nbttagcompound.getBoolean("PipeSign_" + i)) {
				int type = nbttagcompound.getInteger("PipeSign_" + i + "_type");
				Class<? extends IPipeSign> typeClass = ItemPipeSignCreator.signTypes.get(type);
				try {
					signItem[i] = typeClass.newInstance();
					signItem[i].init(this, ForgeDirection.getOrientation(i));
					signItem[i].readFromNBT(nbttagcompound.getCompoundTag("PipeSign_" + i + "_tags"));
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public IRouter getRouter() {
		if (stillNeedReplace) {
			System.out.format("Hey, don't get routers for pipes that aren't ready (%d, %d, %d, '%s')", this.getX(), this.getY(), this.getZ(), this.getWorld().getWorldInfo().getWorldName());
			new Throwable().printStackTrace();
		}
		if (router == null) {
			synchronized (routerIdLock) {

				UUID routerIntId = null;
				if (routerId != null && !routerId.isEmpty()) {
					routerIntId = UUID.fromString(routerId);
				}
				router = SimpleServiceLocator.routerManager.getOrCreateRouter(routerIntId, MainProxy.getDimensionForWorld(getWorld()), getX(), getY(), getZ(), false);
			}
		}
		return router;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);
		clearCache();
		if (!stillNeedReplace && MainProxy.isServer(getWorld())) {
			onNeighborBlockChange_Logistics();
		}
	}

	public void onNeighborBlockChange_Logistics() {}

	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();
	}

	@CCCommand(description = "Returns the Internal LogisticsModule for this pipe")
	public abstract LogisticsModule getLogisticsModule();

	@Override
	public final boolean blockActivated(EntityPlayer entityplayer) {
		SecuritySettings settings = null;
		if (MainProxy.isServer(entityplayer.worldObj)) {
			LogisticsSecurityTileEntity station = SimpleServiceLocator.securityStationManager.getStation(getOriginalUpgradeManager().getSecurityID());
			if (station != null) {
				settings = station.getSecuritySettingsForPlayer(entityplayer, true);
			}
		}

		if (MainProxy.isPipeControllerEquipped(entityplayer)) {
			if (MainProxy.isServer(entityplayer.worldObj)) {
				if (settings == null || settings.openNetworkMonitor) {
					NewGuiHandler.getGui(PipeController.class).setTilePos(container).open(entityplayer);
				} else {
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			return true;
		}

		if (handleClick(entityplayer, settings)) {
			return true;
		}

		if (entityplayer.getCurrentEquippedItem() == null) {
			if (!entityplayer.isSneaking()) {
				return false;
			}
			/*
			if (MainProxy.isClient(entityplayer.worldObj)) {
				if (!LogisticsHUDRenderer.instance().hasLasers()) {
					MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestRoutingLasersPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
				} else {
					LogisticsHUDRenderer.instance().resetLasers();
				}
			}
			*/
			if (LPConstants.DEBUG) {
				doDebugStuff(entityplayer);
			}
			return true;
		}

		if (entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer) {
			if (MainProxy.isServer(entityplayer.worldObj)) {
				if (settings == null || settings.openRequest) {
					entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, getWorld(), getX(), getY(), getZ());
				} else {
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			return true;
		}

		if (SimpleServiceLocator.toolWrenchHandler.isWrenchEquipped(entityplayer) && SimpleServiceLocator.toolWrenchHandler.canWrench(entityplayer, getX(), getY(), getZ())) {
			if (MainProxy.isServer(entityplayer.worldObj)) {
				if (settings == null || settings.openGui) {
					if (getLogisticsModule() != null && getLogisticsModule() instanceof LogisticsGuiModule) {
						((LogisticsGuiModule) getLogisticsModule()).getPipeGuiProviderForModule().setTilePos(container).open(entityplayer);
					} else {
						onWrenchClicked(entityplayer);
					}
				} else {
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			SimpleServiceLocator.toolWrenchHandler.wrenchUsed(entityplayer, getX(), getY(), getZ());
			return true;
		}

		if (!(entityplayer.isSneaking()) && getOriginalUpgradeManager().tryIserting(getWorld(), entityplayer)) {
			return true;
		}

		return super.blockActivated(entityplayer);
	}

	protected boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		return false;
	}

	protected void clearCache() {
		_cachedAdjacentInventories = null;
	}

	public void refreshRender(boolean spawnPart) {

		container.scheduleRenderUpdate();
		if (spawnPart) {
			spawnParticle(Particles.GreenParticle, 3);
		}
	}

	public void refreshConnectionAndRender(boolean spawnPart) {
		clearCache();
		container.scheduleNeighborChange();
		if (spawnPart) {
			spawnParticle(Particles.GreenParticle, 3);
		}
	}

	/*** -- IAdjacentWorldAccess -- ***/

	@Override
	public LinkedList<AdjacentTile> getConnectedEntities() {
		WorldUtil world = new WorldUtil(getWorld(), getX(), getY(), getZ());
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities(true);

		Iterator<AdjacentTile> iterator = adjacent.iterator();
		while (iterator.hasNext()) {
			AdjacentTile tile = iterator.next();
			if (!MainProxy.checkPipesConnections(container, tile.tile, tile.orientation)) {
				iterator.remove();
			}
		}

		return adjacent;
	}

	@Override
	public int getRandomInt(int maxSize) {
		return getWorld().rand.nextInt(maxSize);
	}

	/*** -- ITrackStatistics -- ***/

	@Override
	public void recievedItem(int count) {
		stat_session_recieved += count;
		stat_lifetime_recieved += count;
		updateStats();
	}

	@Override
	public void relayedItem(int count) {
		stat_session_relayed += count;
		stat_lifetime_relayed += count;
		updateStats();
	}

	@Override
	public World getWorld() {
		return container.getWorld();
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if (mode == 0) {
			watchers.add(player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(StatUpdate.class).setPipe(this), player);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		if (mode == 0) {
			watchers.remove(player);
		}
	}

	public void updateStats() {
		if (watchers.size() > 0) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(StatUpdate.class).setPipe(this), watchers);
		}
	}

	@Override
	public void itemCouldNotBeSend(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		if (this instanceof IRequireReliableTransport) {
			((IRequireReliableTransport) this).itemLost(item, info);
		}
	}

	public boolean isLockedExit(ForgeDirection orientation) {
		return false;
	}

	public boolean logisitcsIsPipeConnected(TileEntity tile, ForgeDirection dir) {
		return false;
	}

	public boolean disconnectPipe(TileEntity tile, ForgeDirection dir) {
		return false;
	}

	@Override
	public final boolean canPipeConnect(TileEntity tile, ForgeDirection dir) {
		return canPipeConnect(tile, dir, false);
	}

	public boolean globalIgnoreConnectionDisconnection = false;

	@Override
	public final boolean canPipeConnect(TileEntity tile, ForgeDirection dir, boolean ignoreSystemDisconnection) {
		ForgeDirection side = OrientationsUtil.getOrientationOfTilewithTile(container, tile);
		if (isSideBlocked(side, ignoreSystemDisconnection)) {
			return false;
		}
		return (super.canPipeConnect(tile, dir) || logisitcsIsPipeConnected(tile, dir)) && !disconnectPipe(tile, dir);
	}

	@Override
	public final boolean isSideBlocked(ForgeDirection side, boolean ignoreSystemDisconnection) {
		if (getUpgradeManager().isSideDisconnected(side)) {
			return true;
		}
		if (container != null && side != ForgeDirection.UNKNOWN && container.tilePart.hasBlockingPluggable(side)) {
			return true;
		}
		if (!stillNeedReplace) {
			if (getRouter().isSideDisconneceted(side) && !ignoreSystemDisconnection && !globalIgnoreConnectionDisconnection) {
				return true;
			}
		}
		return false;
	}

	public void connectionUpdate() {
		if (container != null && !stillNeedReplace) {
			container.scheduleNeighborChange();
			getWorld().notifyBlockChange(getX(), getY(), getZ(), getWorld().getBlock(getX(), getY(), getZ()));
		}
	}

	public UUID getSecurityID() {
		return getOriginalUpgradeManager().getSecurityID();
	}

	public void insetSecurityID(UUID id) {
		getOriginalUpgradeManager().insetSecurityID(id);
	}

	/* Power System */

	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> getRoutedPowerProviders() {
		if (MainProxy.isClient(getWorld())) {
			return null;
		}
		if (stillNeedReplace) {
			return null;
		}
		return getRouter().getPowerProvider();
	}

	@Override
	public boolean useEnergy(int amount) {
		return useEnergy(amount, null, true);
	}

	public boolean useEnergy(int amount, boolean sparkles) {
		return useEnergy(amount, null, sparkles);
	}

	@Override
	public boolean canUseEnergy(int amount) {
		return canUseEnergy(amount, null);
	}

	@Override
	public boolean canUseEnergy(int amount, List<Object> providersToIgnore) {
		if (MainProxy.isClient(getWorld())) {
			return false;
		}
		if (Configs.LOGISTICS_POWER_USAGE_DISABLED) {
			return true;
		}
		if (amount == 0) {
			return true;
		}
		if (providersToIgnore != null && providersToIgnore.contains(this)) {
			return false;
		}
		List<Pair<ILogisticsPowerProvider, List<IFilter>>> list = getRoutedPowerProviders();
		if (list == null) {
			return false;
		}
		outer:
			for (Pair<ILogisticsPowerProvider, List<IFilter>> provider : list) {
				for (IFilter filter : provider.getValue2()) {
					if (filter.blockPower()) {
						continue outer;
					}
				}
				if (provider.getValue1().canUseEnergy(amount, providersToIgnore)) {
					return true;
				}
			}
		return false;
	}

	@Override
	public boolean useEnergy(int amount, List<Object> providersToIgnore) {
		return useEnergy(amount, providersToIgnore, false);
	}

	private boolean useEnergy(int amount, List<Object> providersToIgnore, boolean sparkles) {
		if (MainProxy.isClient(getWorld())) {
			return false;
		}
		if (Configs.LOGISTICS_POWER_USAGE_DISABLED) {
			return true;
		}
		if (amount == 0) {
			return true;
		}
		if (providersToIgnore == null) {
			providersToIgnore = new ArrayList<Object>();
		}
		if (providersToIgnore.contains(this)) {
			return false;
		}
		providersToIgnore.add(this);
		List<Pair<ILogisticsPowerProvider, List<IFilter>>> list = getRoutedPowerProviders();
		if (list == null) {
			return false;
		}
		outer:
			for (Pair<ILogisticsPowerProvider, List<IFilter>> provider : list) {
				for (IFilter filter : provider.getValue2()) {
					if (filter.blockPower()) {
						continue outer;
					}
				}
				if (provider.getValue1().canUseEnergy(amount, providersToIgnore)) {
					if (provider.getValue1().useEnergy(amount, providersToIgnore)) {
						if (sparkles) {
							int particlecount = amount;
							if (particlecount > 10) {
								particlecount = 10;
							}
							spawnParticle(Particles.GoldParticle, particlecount);
						}
						return true;
					}
				}
			}
		return false;
	}

	@Override
	public void queueEvent(String event, Object[] arguments) {
		if (container instanceof LogisticsTileGenericPipe) {
			container.queueEvent(event, arguments);
		}
	}

	public boolean stillNeedReplace() {
		return stillNeedReplace;
	}

	public boolean initialInit() {
		return _initialInit;
	}

	@Override
	public int compareTo(IRequestItems other) {
		return getID() - other.getID();
	}

	@Override
	public int getID() {
		return getRouter().getSimpleID();
	}

	public Set<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	public boolean hasGenericInterests() {
		return false;
	}

	public ISecurityProvider getSecurityProvider() {
		return SimpleServiceLocator.securityStationManager.getStation(getOriginalUpgradeManager().getSecurityID());
	}

	public boolean canBeDestroyedByPlayer(EntityPlayer entityPlayer) {
		LogisticsSecurityTileEntity station = SimpleServiceLocator.securityStationManager.getStation(getOriginalUpgradeManager().getSecurityID());
		if (station != null) {
			return station.getSecuritySettingsForPlayer(entityPlayer, true).removePipes;
		}
		return true;
	}

	@Override
	public boolean canBeDestroyed() {
		ISecurityProvider sec = getSecurityProvider();
		if (sec != null) {
			if (!sec.canAutomatedDestroy()) {
				return false;
			}
		}
		return true;
	}

	public void setDestroyByPlayer() {
		destroyByPlayer = true;
	}

	@Override
	public boolean destroyByPlayer() {
		return destroyByPlayer;
	}

	@Override
	public boolean preventRemove() {
		return preventRemove;
	}

	@CCSecurtiyCheck
	public void checkCCAccess() throws PermissionException {
		ISecurityProvider sec = getSecurityProvider();
		if (sec != null) {
			int id = -1;
			if (container instanceof LogisticsTileGenericPipe) {
				id = container.getLastCCID();
			}
			if (!sec.getAllowCC(id)) {
				throw new PermissionException();
			}
		}
	}

	public void queueUnroutedItemInformation(ItemIdentifierStack item, ItemRoutingInformation information) {
		if (item != null) {
			synchronized (queuedDataForUnroutedItems) {
				Queue<Pair<Integer, ItemRoutingInformation>> queue = queuedDataForUnroutedItems.get(item.getItem());
				if (queue == null) {
					queuedDataForUnroutedItems.put(item.getItem(), queue = new LinkedList<Pair<Integer, ItemRoutingInformation>>());
				}
				queue.add(new Pair<Integer, ItemRoutingInformation>(item.getStackSize(), information));
			}
		}
	}

	public ItemRoutingInformation getQueuedForItemStack(ItemIdentifierStack item) {
		synchronized (queuedDataForUnroutedItems) {
			Queue<Pair<Integer, ItemRoutingInformation>> queue = queuedDataForUnroutedItems.get(item.getItem());
			if (queue == null || queue.isEmpty()) {
				return null;
			}

			Pair<Integer, ItemRoutingInformation> pair = queue.peek();
			int wantItem = pair.getValue1();

			if (wantItem <= item.getStackSize()) {
				if (queue.remove() != pair) {
					LogisticsPipes.log.fatal("Item queue mismatch");
					return null;
				}
				if (queue.isEmpty()) {
					queuedDataForUnroutedItems.remove(item.getItem());
				}
				item.setStackSize(wantItem);
				return pair.getValue2();
			}
		}
		return null;
	}

	/**
	 * used as a distance offset when deciding which pipe to use NOTE: called
	 * very regularly, returning a pre-calculated int is probably appropriate.
	 * 
	 * @return
	 */
	public double getLoadFactor() {
		return 0.0;
	}

	public void notifyOfItemArival(ItemRoutingInformation information) {
		_inTransitToMe.remove(information);
		if (this instanceof IRequireReliableTransport) {
			((IRequireReliableTransport) this).itemArrived(information.getItem(), information.targetInfo);
		}
		if (this instanceof IRequireReliableFluidTransport) {
			ItemIdentifierStack stack = information.getItem();
			if (stack.getItem().isFluidContainer()) {
				FluidStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(stack);
				if (liquid != null) {
					((IRequireReliableFluidTransport) this).liquidArrived(FluidIdentifier.get(liquid), liquid.amount);
				}
			}
		}
	}

	@Override
	public int countOnRoute(ItemIdentifier it) {
		int count = 0;
		for (ItemRoutingInformation next : _inTransitToMe) {
			if (next.getItem().getItem().equals(it)) {
				count += next.getItem().getStackSize();
			}
		}
		return count;
	}

	@Override
	public final int getIconIndex(ForgeDirection connection) {
		TextureType texture = getTextureType(connection);
		if (_textureBufferPowered) {
			return texture.powered;
		} else if (Configs.LOGISTICS_POWER_USAGE_DISABLED) {
			return texture.normal;
		} else {
			return texture.unpowered;
		}
	}

	public void addCrashReport(CrashReportCategory crashReportCategory) {
		addRouterCrashReport(crashReportCategory);
		crashReportCategory.addCrashSection("stillNeedReplace", stillNeedReplace);
	}

	protected void addRouterCrashReport(CrashReportCategory crashReportCategory) {
		crashReportCategory.addCrashSection("Router", getRouter().toString());
	}

	/* --- CCCommands --- */
	@CCCommand(description = "Returns the Router UUID as an integer; all pipes have a unique ID (runtime stable)")
	public int getRouterId() {
		return getRouter().getSimpleID();
	}

	@CCCommand(description = "Returns the Router UUID; all pipes have a unique ID (lifetime stable)")
	public String getRouterUUID() {
		return getRouter().getId().toString();
	}

	@CCCommand(description = "Returns the Router UUID for the givvin router Id")
	public String getRouterUUID(Double id) {
		IRouter router = SimpleServiceLocator.routerManager.getRouter((int) ((double) id));
		if (router == null) {
			return null;
		}
		return router.getId().toString();
	}

	@CCCommand(description = "Sets the TurtleConnect flag for this Turtle on this LogisticsPipe")
	@CCDirectCall
	public void setTurtleConnect(Boolean flag) {
		if (container instanceof LogisticsTileGenericPipe) {
			container.setTurtleConnect(flag);
		}
	}

	@CCCommand(description = "Returns the TurtleConnect flag for this Turtle on this LogisticsPipe")
	@CCDirectCall
	public boolean getTurtleConnect() {
		if (container instanceof LogisticsTileGenericPipe) {
			return container.getTurtleConnect();
		}
		return false;
	}

	@CCCommand(description = "Returns true if the computer is allowed to interact with the connected pipe.", needPermission = false)
	public boolean canAccess() {
		ISecurityProvider sec = getSecurityProvider();
		if (sec != null) {
			int id = -1;
			if (container instanceof LogisticsTileGenericPipe) {
				id = container.getLastCCID();
			}
			return sec.getAllowCC(id);
		}
		return true;
	}

	@CCCommand(description = "Sends a message to the givven computerId over the LP network. Event: " + CCConstants.LP_CC_MESSAGE_EVENT)
	@CCDirectCall
	public void sendMessage(final Double computerId, final Object message) {
		int sourceId = -1;
		if (container instanceof LogisticsTileGenericPipe) {
			sourceId = SimpleServiceLocator.ccProxy.getLastCCID(container);
		}
		final int fSourceId = sourceId;
		BitSet set = new BitSet(ServerRouter.getBiggestSimpleID());
		for (ExitRoute exit : getRouter().getIRoutersByCost()) {
			if (exit.destination != null && !set.get(exit.destination.getSimpleID())) {
				exit.destination.queueTask(10, new IRouterQueuedTask() {

					@Override
					public void call(CoreRoutedPipe pipe, IRouter router) {
						pipe.handleMesssage((int) ((double) computerId), message, fSourceId);
					}
				});
				set.set(exit.destination.getSimpleID());
			}
		}
	}

	@CCCommand(description = "Sends a broadcast message to all Computer connected to this LP network. Event: " + CCConstants.LP_CC_BROADCAST_EVENT)
	@CCDirectCall
	public void sendBroadcast(final String message) {
		int sourceId = -1;
		if (container instanceof LogisticsTileGenericPipe) {
			sourceId = SimpleServiceLocator.ccProxy.getLastCCID(container);
		}
		final int fSourceId = sourceId;
		BitSet set = new BitSet(ServerRouter.getBiggestSimpleID());
		for (ExitRoute exit : getRouter().getIRoutersByCost()) {
			if (exit.destination != null && !set.get(exit.destination.getSimpleID())) {
				exit.destination.queueTask(10, new IRouterQueuedTask() {

					@Override
					public void call(CoreRoutedPipe pipe, IRouter router) {
						pipe.handleBroadcast(message, fSourceId);
					}
				});
				set.set(exit.destination.getSimpleID());
			}
		}
	}

	@CCCommand(description = "Returns the access to the pipe of the givven router UUID")
	@ModDependentMethod(modId = LPConstants.computerCraftModID)
	@CCDirectCall
	public Object getPipeForUUID(String sUuid) throws PermissionException {
		if (!getUpgradeManager().hasCCRemoteControlUpgrade()) {
			throw new PermissionException();
		}
		UUID uuid = UUID.fromString(sUuid);
		int id = SimpleServiceLocator.routerManager.getIDforUUID(uuid);
		IRouter router = SimpleServiceLocator.routerManager.getRouter(id);
		if (router == null) {
			return null;
		}
		CoreRoutedPipe pipe = router.getPipe();
		return pipe;
	}

	@CCCommand(description = "Returns the global LP object which is used to access general LP methods.", needPermission = false)
	@CCDirectCall
	public Object getLP() throws PermissionException {
		return LogisticsPipes.getComputerLP();
	}

	@CCCommand(description = "Returns true if the pipe has an internal module")
	public boolean hasLogisticsModule() {
		return getLogisticsModule() != null;
	}

	private void handleMesssage(int computerId, Object message, int sourceId) {
		if (container instanceof LogisticsTileGenericPipe) {
			container.handleMesssage(computerId, message, sourceId);
		}
	}

	private void handleBroadcast(String message, int sourceId) {
		queueEvent(CCConstants.LP_CC_BROADCAST_EVENT, new Object[] { sourceId, message });
	}

	public void onWrenchClicked(EntityPlayer entityplayer) {
		//do nothing, every pipe with a GUI should either have a LogisticsGuiModule or override this method
	}

	final void destroy() { // no overide, put code in OnBlockRemoval

	}

	public void handleRFPowerArival(float toSend) {
		powerHandler.addRFPower(toSend);
	}

	public void handleIC2PowerArival(float toSend) {
		powerHandler.addIC2Power(toSend);
	}

	/*** IInventoryProvider ***/

	@Override
	public IInventoryUtil getPointedInventory(boolean forExtraction) {
		return getSneakyInventory(getPointedOrientation().getOpposite(), forExtraction);
	}

	@Override
	public IInventoryUtil getPointedInventory(ExtractionMode mode, boolean forExtraction) {
		IInventory inv = getRealInventory();
		if (inv == null) {
			return null;
		}
		if (inv instanceof net.minecraft.inventory.ISidedInventory) {
			inv = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) inv, getPointedOrientation().getOpposite(), forExtraction);
		}
		switch (mode) {
			case LeaveFirst:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(inv, getPointedOrientation().getOpposite(), false, false, 1, 0);
			case LeaveLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(inv, getPointedOrientation().getOpposite(), false, false, 0, 1);
			case LeaveFirstAndLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(inv, getPointedOrientation().getOpposite(), false, false, 1, 1);
			case Leave1PerStack:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(inv, getPointedOrientation().getOpposite(), true, false, 0, 0);
			case Leave1PerType:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(inv, getPointedOrientation().getOpposite(), false, true, 0, 0);
			default:
				break;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(inv, getPointedOrientation().getOpposite(), false, false, 0, 0);
	}

	@Override
	public IInventoryUtil getSneakyInventory(boolean forExtraction, ModulePositionType slot, int positionInt) {
		ISlotUpgradeManager manager = getUpgradeManager(slot, positionInt);
		ForgeDirection insertion = getPointedOrientation().getOpposite();
		if (manager.hasSneakyUpgrade()) {
			insertion = manager.getSneakyOrientation();
		}
		return getSneakyInventory(insertion, forExtraction);
	}

	@Override
	public IInventoryUtil getSneakyInventory(ForgeDirection _sneakyOrientation, boolean forExtraction) {
		IInventory inv = getRealInventory();
		if (inv == null) {
			return null;
		}
		if (inv instanceof net.minecraft.inventory.ISidedInventory) {
			inv = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) inv, _sneakyOrientation, forExtraction);
		}
		return SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv, _sneakyOrientation);
	}

	@Override
	public IInventoryUtil getUnsidedInventory() {
		IInventory inv = getRealInventory();
		if (inv == null) {
			return null;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
	}

	@Override
	public IInventory getRealInventory() {
		TileEntity tile = getPointedTileEntity();
		if (tile == null) {
			return null;
		}
		if (!(tile instanceof IInventory)) {
			return null;
		}
		return InventoryHelper.getInventory((IInventory) tile);
	}

	private TileEntity getPointedTileEntity() {
		if (pointedDirection == ForgeDirection.UNKNOWN) {
			return null;
		}
		return getContainer().getTile(pointedDirection);
	}

	@Override
	public ForgeDirection inventoryOrientation() {
		return getPointedOrientation();
	}

	/*** ISendRoutedItem ***/

	public int getSourceint() {
		return getRouter().getSimpleID();
	};

	@Override
	public Triplet<Integer, SinkReply, List<IFilter>> hasDestination(ItemIdentifier stack, boolean allowDefault, List<Integer> routerIDsToExclude) {
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, allowDefault, getRouter().getSimpleID(), routerIDsToExclude);
	}

	@Override
	public IRoutedItem sendStack(ItemStack stack, Pair<Integer, SinkReply> reply, ItemSendMode mode) {
		IRoutedItem itemToSend = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
		itemToSend.setDestination(reply.getValue1());
		if (reply.getValue2().isPassive) {
			if (reply.getValue2().isDefault) {
				itemToSend.setTransportMode(TransportMode.Default);
			} else {
				itemToSend.setTransportMode(TransportMode.Passive);
			}
		}
		itemToSend.setAdditionalTargetInformation(reply.getValue2().addInfo);
		queueRoutedItem(itemToSend, getPointedOrientation(), mode);
		return itemToSend;
	}

	@Override
	public IRoutedItem sendStack(ItemStack stack, int destination, ItemSendMode mode, IAdditionalTargetInformation info) {
		IRoutedItem itemToSend = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
		itemToSend.setDestination(destination);
		itemToSend.setTransportMode(TransportMode.Active);
		itemToSend.setAdditionalTargetInformation(info);
		queueRoutedItem(itemToSend, getPointedOrientation(), mode);
		return itemToSend;
	}

	public ForgeDirection getPointedOrientation() {
		return pointedDirection;
	}

	@Override
	public LogisticsItemOrderManager getItemOrderManager() {
		_orderItemManager = _orderItemManager != null ? _orderItemManager : new LogisticsItemOrderManager(this);
		return _orderItemManager;
	}

	public LogisticsOrderManager<?, ?> getOrderManager() {
		return getItemOrderManager();
	}

	public void addPipeSign(ForgeDirection dir, IPipeSign type, EntityPlayer player) {
		if (dir.ordinal() < 6) {
			if (signItem[dir.ordinal()] == null) {
				signItem[dir.ordinal()] = type;
				signItem[dir.ordinal()].init(this, dir);
			}
			if (container != null) {
				sendSignData(player);
			}
		}
	}

	public void sendSignData(EntityPlayer player) {
		List<Integer> types = new ArrayList<Integer>();
		for (int i = 0; i < 6; i++) {
			if (signItem[i] == null) {
				types.add(-1);
			} else {
				List<Class<? extends IPipeSign>> typeClasses = ItemPipeSignCreator.signTypes;
				for (int j = 0; j < typeClasses.size(); j++) {
					if (typeClasses.get(j) == signItem[i].getClass()) {
						types.add(j);
						break;
					}
				}
			}
		}
		ModernPacket packet = PacketHandler.getPacket(PipeSignTypes.class).setTypes(types).setTilePos(container);
		MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(getWorld()), packet);
		MainProxy.sendPacketToPlayer(packet, player);
		for (int i = 0; i < 6; i++) {
			if (signItem[i] != null) {
				packet = signItem[i].getPacket();
				if (packet != null) {
					MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(getWorld()), packet);
					MainProxy.sendPacketToPlayer(packet, player);
				}
			}
		}
		refreshRender(false);
	}

	public void removePipeSign(ForgeDirection dir, EntityPlayer player) {
		if (dir.ordinal() < 6) {
			signItem[dir.ordinal()] = null;
		}
		sendSignData(player);
	}

	public boolean hasPipeSign(ForgeDirection dir) {
		if (dir.ordinal() < 6) {
			return signItem[dir.ordinal()] != null;
		}
		return false;
	}

	public void activatePipeSign(ForgeDirection dir, EntityPlayer player) {
		if (dir.ordinal() < 6) {
			if (signItem[dir.ordinal()] != null) {
				signItem[dir.ordinal()].activate(player);
			}
		}
	}

	public List<Pair<ForgeDirection, IPipeSign>> getPipeSigns() {
		List<Pair<ForgeDirection, IPipeSign>> list = new ArrayList<Pair<ForgeDirection, IPipeSign>>();
		for (int i = 0; i < 6; i++) {
			if (signItem[i] != null) {
				list.add(new Pair<ForgeDirection, IPipeSign>(ForgeDirection.getOrientation(i), signItem[i]));
			}
		}
		return list;
	}

	public void handleSignPacket(List<Integer> types) {
		if (!MainProxy.isClient(getWorld())) {
			return;
		}
		for (int i = 0; i < 6; i++) {
			int integer = types.get(i);
			if (integer >= 0) {
				Class<? extends IPipeSign> type = ItemPipeSignCreator.signTypes.get(integer);
				if (signItem[i] == null || signItem[i].getClass() != type) {
					try {
						signItem[i] = type.newInstance();
						signItem[i].init(this, ForgeDirection.getOrientation(i));
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				signItem[i] = null;
			}
		}
	}

	public IPipeSign getPipeSign(ForgeDirection dir) {
		if (dir.ordinal() < 6) {
			return signItem[dir.ordinal()];
		}
		return null;
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeBoolean(isOpaque());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		isOpaqueClientSide = data.readBoolean();
	}

	@Override
	public boolean isOpaque() {
		if (MainProxy.isClient(getWorld())) {
			return Configs.OPAQUE || isOpaqueClientSide;
		} else {
			return Configs.OPAQUE || this.getUpgradeManager().isOpaque();
		}
	}

	@Override
	public void addStatusInformation(List<StatusEntry> status) {
		StatusEntry entry = new StatusEntry();
		entry.name = "Send Queue";
		entry.subEntry = new ArrayList<StatusEntry>();
		for (Triplet<IRoutedItem, ForgeDirection, ItemSendMode> part : _sendQueue) {
			StatusEntry subEntry = new StatusEntry();
			subEntry.name = part.toString();
			entry.subEntry.add(subEntry);
		}
		status.add(entry);
		entry = new StatusEntry();
		entry.name = "In Transit To Me";
		entry.subEntry = new ArrayList<StatusEntry>();
		for (ItemRoutingInformation part : _inTransitToMe) {
			StatusEntry subEntry = new StatusEntry();
			subEntry.name = part.toString();
			entry.subEntry.add(subEntry);
		}
		status.add(entry);
	}

	@Override
	public int getSourceID() {
		return getRouterId();
	}

	@Override
	public DebugLogController getDebug() {
		return debug;
	}

	@Override
	public void setPreventRemove(boolean flag) {
		preventRemove = flag;
	}

	@Override
	public boolean isRoutedPipe() {
		return true;
	}

	@Override
	public double getDistanceTo(int destinationint, ForgeDirection ignore, ItemIdentifier ident, boolean isActive, double traveled, double max, List<LPPosition> visited) {
		if (!stillNeedReplace) {
			if (getRouterId() == destinationint) {
				return 0;
			}
			ExitRoute route = getRouter().getExitFor(destinationint, isActive, ident);
			if (route != null && route.exitOrientation != ignore) {
				if (route.distanceToDestination + traveled >= max) {
					return Integer.MAX_VALUE;
				}
				return route.distanceToDestination;
			}
		}
		return Integer.MAX_VALUE;
	}

	public void triggerConnectionCheck() {
		recheckConnections = true;
	}

	@Override
	public CacheHolder getCacheHolder() {
		if (cacheHolder == null) {
			if (container instanceof ILPTEInformation && ((ILPTEInformation) container).getObject() != null) {
				cacheHolder = ((ILPTEInformation) container).getObject().getCacheHolder();
			} else {
				cacheHolder = new CacheHolder();
			}
		}
		return cacheHolder;
	}
}
