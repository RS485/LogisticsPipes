/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import logisticspipes.LPConstants;
import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.asm.te.ILPTEInformation;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.interfaces.IClientState;
import logisticspipes.interfaces.ILPPositionProvider;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.interfaces.IQueueCCEvent;
import logisticspipes.interfaces.ISecurityProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.SecurityStationManager;
import logisticspipes.interfaces.SlotUpgradeManager;
import logisticspipes.interfaces.WrappedInventory;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.interfaces.routing.ItemRequester;
import logisticspipes.items.ItemPipeSignCreator;
import logisticspipes.logistics.LogisticsFluidManager;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.logisticspipes.ExtractionMode;
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
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.pipe.PipeController;
import logisticspipes.network.packets.pipe.ParticleFX;
import logisticspipes.network.packets.pipe.PipeSignTypes;
import logisticspipes.network.packets.pipe.RequestSignPacket;
import logisticspipes.network.packets.pipe.StatUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.pipes.signs.PipeSign;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.ConfigToolHandler;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.computers.interfaces.CCSecurtiyCheck;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.Router;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LogisticsItemOrderManager;
import logisticspipes.routing.order.LogisticsOrderManager;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.routing.pathfinder.PipeInformationManager;
import logisticspipes.security.PermissionException;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.EnumFacingUtil;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.RoutedItemHelper;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Tuple2;
import logisticspipes.utils.tuples.Tuple3;
import network.rs485.logisticspipes.config.LPConfiguration;
import network.rs485.logisticspipes.connection.NeighborBlockEntity;
import network.rs485.logisticspipes.util.ItemVariant;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

@CCType(name = "LogisticsPipes:Normal")
public abstract class CoreRoutedPipe extends CoreUnroutedPipe
		implements IClientState, ItemRequester, ITrackStatistics, IWorldProvider, IWatchingHandler, IPipeServiceProvider, IQueueCCEvent, ILPPositionProvider {

	private static int pipecount = 0;
	public final PlayerCollectionList watchers = new PlayerCollectionList();
	protected final PriorityBlockingQueue<ItemRoutingInformation> _inTransitToMe = new PriorityBlockingQueue<>(10,
			new ItemRoutingInformation.DelayComparator());
	protected final LinkedList<Tuple3<IRoutedItem, Direction, ItemSendMode>> _sendQueue = new LinkedList<>();
	protected final Map<ItemIdentifier, Queue<Tuple2<Integer, ItemRoutingInformation>>> queuedDataForUnroutedItems = Collections.synchronizedMap(new TreeMap<>());
	public boolean _textureBufferPowered;
	public long delayTo = 0;
	public int repeatFor = 0;
	public int stat_session_sent;
	public int stat_session_recieved;
	public int stat_session_relayed;
	public long stat_lifetime_sent;
	public long stat_lifetime_recieved;
	public long stat_lifetime_relayed;
	public int server_routing_table_size = 0;
	public boolean globalIgnoreConnectionDisconnection = false;
	protected boolean stillNeedReplace = true;
	protected Router router;
	protected String routerId;
	protected Object routerIdLock = new Object();
	protected int _delayOffset;
	protected boolean _initialInit = true;
	protected RouteLayer _routeLayer;
	protected TransportLayer _transportLayer;
	protected UpgradeManager upgradeManager = new UpgradeManager(this);
	protected LogisticsItemOrderManager _orderItemManager = null;
	protected List<BlockEntity> _cachedAdjacentInventories;
	protected Direction pointedDirection = null;
	// public BaseRoutingLogic logic;
	// from BaseRoutingLogic
	protected int throttleTime = 20;
	protected Map<Direction, PipeSign> signItem = new EnumMap<>(Direction.class);
	private boolean recheckConnections = false;
	private boolean enabled = true;
	private boolean preventRemove = false;
	private boolean destroyByPlayer = false;
	private PowerSupplierHandler powerHandler = new PowerSupplierHandler(this);
	@Getter
	private List<IOrderInfoProvider> clientSideOrderManager = new ArrayList<>();
	private int throttleTimeLeft = 20 + new Random().nextInt(LPConfiguration.INSTANCE.getPipeDetectionFrequency());
	private int[] queuedParticles = new int[Particles.values().length];
	private boolean hasQueuedParticles = false;
	private boolean isOpaqueClientSide = false;

	private CacheHolder cacheHolder;

	public CoreRoutedPipe(Item item) {
		this(new PipeTransportLogistics(true), item);
	}

	public CoreRoutedPipe(PipeTransportLogistics transport, Item item) {
		super(transport, item);

		CoreRoutedPipe.pipecount++;

		// Roughly spread pipe updates throughout the frequency, no need to maintain balance
		_delayOffset = CoreRoutedPipe.pipecount % LPConfiguration.INSTANCE.getPipeDetectionFrequency();
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
	public SlotUpgradeManager getUpgradeManager(ModulePositionType slot, int positionInt) {
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
	public void queueRoutedItem(IRoutedItem routedItem, Direction from) {
		if (from == null) {
			throw new NullPointerException();
		}
		_sendQueue.addLast(new Tuple3<>(routedItem, from, ItemSendMode.Normal));
		sendQueueChanged(false);
	}

	public void queueRoutedItem(IRoutedItem routedItem, Direction from, ItemSendMode mode) {
		if (from == null) {
			throw new NullPointerException();
		}
		_sendQueue.addLast(new Tuple3<>(routedItem, from, mode));
		sendQueueChanged(false);
	}

	/**
	 * @param force == true never delegates to a thread
	 * @return number of things sent.
	 */
	public int sendQueueChanged(boolean force) {
		return 0;
	}

	private void sendRoutedItem(IRoutedItem routedItem, Direction from) {

		if (from == null) {
			throw new NullPointerException();
		}

		transport.injectItem(routedItem, from.getOpposite());

		Router r = RouterManager.getInstance().getRouterUnsafe(routedItem.getDestination(), false);
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
		// LogisticsPipes.log.info("Sending: "+routedItem.getIDStack().getItem().getFriendlyName());
	}

	public void notifyOfReroute(ItemRoutingInformation routedItem) {
		_inTransitToMe.remove(routedItem);
	}

	// When Recreating the Item from the TE version we have the same hashCode but a different instance so we need to refresh this
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
		List<BlockEntity> others = other.getConnectedRawInventories();
		if (others == null || others.size() == 0) {
			return false;
		}
		for (BlockEntity i : getConnectedRawInventories()) {
			if (others.contains(i)) {
				return true;
			}
		}
		return false;
	}

	protected List<BlockEntity> getConnectedRawInventories() {
		if (_cachedAdjacentInventories == null) {
			_cachedAdjacentInventories = new WorldCoordinatesWrapper(container)
					.connectedTileEntities(ConnectionPipeType.ITEM)
					.filter(adjacent -> adjacent.isItemHandler() && !adjacent.isLogisticsPipe())
					.map(NeighborBlockEntity::getBlockEntity)
					.collect(Collectors.toList());
		}
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
			// BlockState state = getWorld().getBlockState(getPos());
			// getWorld().notifyNeighborsOfStateChange(getPos(), state == null ? null : state.getBlock(), true);
			/* TravelingItems are just held by a pipe, they don't need to know their world
			 * for(Triplet<IRoutedItem, Direction, ItemSendMode> item : _sendQueue) {
				// assign world to any entityitem we created in readfromnbt
				item.getValue1().getTravelingItem().setWorld(getWorld());
			}*/
			// first tick just create a router and do nothing.
			firstInitialiseTick();
			return;
		}
		if (repeatFor > 0) {
			if (delayTo < System.currentTimeMillis()) {
				delayTo = System.currentTimeMillis() + 200;
				repeatFor--;
				BlockState state = getWorld().getBlockState(getPos());
				getWorld().notifyNeighborsOfStateChange(getPos(), state == null ? null : state.getBlock(), true);
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
		// update router before ticking logic/transport
		getRouter().update(getWorld().getTotalWorldTime() % LPConfiguration.INSTANCE.getPipeDetectionFrequency() == _delayOffset || _initialInit || recheckConnections, this);
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
				Tuple3<IRoutedItem, Direction, ItemSendMode> itemToSend = _sendQueue.getFirst();
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
						Tuple3<IRoutedItem, Direction, ItemSendMode> itemToSend = _sendQueue.getFirst();
						sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
						_sendQueue.removeFirst();
					}
				}
				sendQueueChanged(false);
			} else if (getItemSendMode() == null) {
				throw new UnsupportedOperationException("getItemSendMode() can't return null. " + this.getClass().getName());
			} else {
				throw new UnsupportedOperationException(
						"getItemSendMode() returned unhandled value. " + getItemSendMode().name() + " in " + this.getClass().getName());
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

		LogisticsModule module = getLogisticsModule();
		if (module instanceof Tickable) {
			((Tickable) getLogisticsModule()).tick();
		}
	}

	protected void onAllowedRemoval() {}

	// From BaseRoutingLogic
	public void throttledUpdateEntity() {}

	protected void delayThrottle() {
		// delay 6(+1) ticks to prevent suppliers from ticking between a item arriving at them and the item hitting their adj. inv
		if (throttleTimeLeft < 7) {
			throttleTimeLeft = 7;
		}
	}

	@Override
	public boolean isNthTick(int n) {
		return ((getWorld().getTotalWorldTime() + _delayOffset) % n == 0);
	}

	private void doDebugStuff(EntityPlayer entityplayer) {
		// entityplayer.world.setWorldTime(4951);
		Router r = getRouter();
		if (!(r instanceof ServerRouter)) {
			return;
		}
		System.out.println("***");
		System.out.println("---------Interests---------------");
		for (Entry<ItemIdentifier, Set<Router>> i : ServerRouter.getInterestedInSpecifics().entrySet()) {
			System.out.print(i.getKey().getFriendlyName() + ":");
			for (Router j : i.getValue()) {
				System.out.print(j.getSimpleId() + ",");
			}
			System.out.println();
		}

		System.out.print("ALL ITEMS:");
		for (Router j : ServerRouter.getInterestedInGeneral()) {
			System.out.print(j.getSimpleId() + ",");
		}
		System.out.println();

		ServerRouter sr = (ServerRouter) r;

		System.out.println(r.toString());
		System.out.println("---------CONNECTED TO---------------");
		for (CoreRoutedPipe adj : sr.adjacent.keySet()) {
			System.out.println(adj.getRouter().getSimpleId());
		}
		System.out.println();
		System.out.println("========DISTANCE TABLE==============");
		for (ExitRoute n : r.getIRoutersByCost()) {
			System.out
					.println(n.destination.getSimpleId() + " @ " + n.distanceToDestination + " -> " + n.connectionDetails + "(" + n.destination.getId() + ")");
		}
		System.out.println();
		System.out.println("*******EXIT ROUTE TABLE*************");
		List<List<ExitRoute>> table = r.getRouteTable();
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i) != null) {
				if (table.get(i).size() > 0) {
					System.out.println(i + " -> " + table.get(i).get(0).destination.getSimpleId());
					for (ExitRoute route : table.get(i)) {
						System.out.println("\t\t via " + route.exitOrientation + "(" + route.distanceToDestination + " distance)");
					}
				}
			}
		}
		System.out.println();
		System.out.println("++++++++++CONNECTIONS+++++++++++++++");
		System.out.println(Arrays.toString(Direction.values()));
		System.out.println(Arrays.toString(sr.sideDisconnected));
		System.out.println(Arrays.toString(container.pipeConnectionsBuffer));
		System.out.println();
		System.out.println("~~~~~~~~~~~~~~~POWER~~~~~~~~~~~~~~~~");
		System.out.println(r.getPowerProvider());
		System.out.println();
		System.out.println("~~~~~~~~~~~SUBSYSTEMPOWER~~~~~~~~~~~");
		System.out.println(r.getSubSystemPowerProvider());
		System.out.println();
		if (_orderItemManager != null) {
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
			// invalidate() removes the router
			//				if (logic instanceof BaseRoutingLogic){
			//					((BaseRoutingLogic)logic).destroy();
			//				}
			// Just in case
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
		if (LPConfiguration.INSTANCE.getPowerUsageMultiplier() <= 0) {
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

	public TextureType getTextureType(Direction connection) {
		if (stillNeedReplace || _initialInit) {
			return getCenterTexture();
		}

		if (connection == null) {
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

	public TextureType getRoutedTexture(Direction connection) {
		if (getRouter().isSubPoweredExit(connection)) {
			return Textures.LOGISTICSPIPE_SUBPOWER_TEXTURE;
		} else {
			return Textures.LOGISTICSPIPE_ROUTED_TEXTURE;
		}
	}

	public TextureType getNonRoutedTexture(Direction connection) {
		if (isPowerProvider(connection)) {
			return Textures.LOGISTICSPIPE_POWERED_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_NOTROUTED_TEXTURE;
	}

	@Override
	public void spawnParticle(Particles particle, int amount) {
		if (!LPConfiguration.INSTANCE.getEnableParticleFx()) {
			return;
		}
		queuedParticles[particle.ordinal()] += amount;
		hasQueuedParticles = true;
	}

	private void spawnParticleTick() {
		if (!hasQueuedParticles) {
			return;
		}
		if (!getWorld().isClient()) {
			ArrayList<ParticleCount> tosend = new ArrayList<>(queuedParticles.length);
			for (int i = 0; i < queuedParticles.length; i++) {
				if (queuedParticles[i] > 0) {
					tosend.add(new ParticleCount(Particles.values()[i], queuedParticles[i]));
				}
			}
			MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), getWorld().provider.getDimension(),
					PacketHandler.getPacket(ParticleFX.class).setParticles(tosend).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			if (Minecraft.isFancyGraphicsEnabled()) {
				for (int i = 0; i < queuedParticles.length; i++) {
					if (queuedParticles[i] > 0) {
						PipeFXRenderHandler.spawnGenericParticle(Particles.values()[i], getX(), getY(), getZ(), queuedParticles[i]);
					}
				}
			}
		}
		Arrays.fill(queuedParticles, 0);
		hasQueuedParticles = false;
	}

	protected boolean isPowerProvider(Direction ori) {
		BlockEntity tilePipe = container.getTile(ori);
		if (tilePipe == null || !container.canPipeConnect(tilePipe, ori)) {
			return false;
		}

		return tilePipe instanceof ILogisticsPowerProvider || tilePipe instanceof ISubSystemPowerProvider;
	}

	@Override
	public void writeToNBT(CompoundTag nbttagcompound) {
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
		CompoundTag upgradeNBT = new CompoundTag();
		upgradeManager.writeToNBT(upgradeNBT);
		nbttagcompound.setTag("upgradeManager", upgradeNBT);

		CompoundTag powerNBT = new CompoundTag();
		powerHandler.writeToNBT(powerNBT);
		if (!powerNBT.hasNoTags()) {
			nbttagcompound.setTag("powerHandler", powerNBT);
		}

		ListTag sendqueue = new ListTag();
		for (Tuple3<IRoutedItem, Direction, ItemSendMode> p : _sendQueue) {
			CompoundTag tagentry = new CompoundTag();
			CompoundTag tagentityitem = new CompoundTag();
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
				List<Class<? extends PipeSign>> typeClasses = ItemPipeSignCreator.signTypes;
				for (int j = 0; j < typeClasses.size(); j++) {
					if (typeClasses.get(j) == signItem[i].getClass()) {
						signType = j;
						break;
					}
				}
				nbttagcompound.setInteger("PipeSign_" + i + "_type", signType);
				CompoundTag tag = new CompoundTag();
				signItem[i].writeToNBT(tag);
				nbttagcompound.setTag("PipeSign_" + i + "_tags", tag);
			} else {
				nbttagcompound.setBoolean("PipeSign_" + i, false);
			}
		}
	}

	@Override
	public void readFromNBT(CompoundTag nbttagcompound) {
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
		ListTag sendqueue = nbttagcompound.getTagList("sendqueue", nbttagcompound.getId());
		for (int i = 0; i < sendqueue.tagCount(); i++) {
			CompoundTag tagentry = sendqueue.getCompoundTagAt(i);
			CompoundTag tagentityitem = tagentry.getCompoundTag("entityitem");
			LPTravelingItemServer item = new LPTravelingItemServer(tagentityitem);
			Direction from = Direction.values()[tagentry.getByte("from")];
			ItemSendMode mode = ItemSendMode.values()[tagentry.getByte("mode")];
			_sendQueue.add(new Tuple3<>(item, from, mode));
		}
		for (int i = 0; i < 6; i++) {
			if (nbttagcompound.getBoolean("PipeSign_" + i)) {
				int type = nbttagcompound.getInteger("PipeSign_" + i + "_type");
				Class<? extends PipeSign> typeClass = ItemPipeSignCreator.signTypes.get(type);
				try {
					signItem[i] = typeClass.newInstance();
					signItem[i].init(this, EnumFacingUtil.getOrientation(i));
					signItem[i].readFromNBT(nbttagcompound.getCompoundTag("PipeSign_" + i + "_tags"));
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public Router getRouter() {
		if (stillNeedReplace) {
			System.out.format("Hey, don't get routers for pipes that aren't ready (%d, %d, %d, '%s')", getPos(),
					this.getWorld().dimension.getType());
			new Throwable().printStackTrace();
		}
		if (router == null) {
			synchronized (routerIdLock) {

				UUID routerIntId = null;
				if (routerId != null && !routerId.isEmpty()) {
					routerIntId = UUID.fromString(routerId);
				}
				router = RouterManager.getInstance()
						.getOrCreateRouter(routerIntId, getWorld().provider.getDimension(), getX(), getY(), getZ(), false);
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
	public void onNeighborBlockChange() {
		super.onNeighborBlockChange();
		clearCache();
		if (!stillNeedReplace && !getWorld().isClient()) {
			onNeighborBlockChange_Logistics();
		}
	}

	public void onNeighborBlockChange_Logistics() {}

	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();
	}

	public abstract LogisticsModule getLogisticsModule();

	@Override
	public final boolean blockActivated(EntityPlayer entityplayer) {
		SecuritySettings settings = null;
		if (MainProxy.isServer(entityplayer.world)) {
			LogisticsSecurityTileEntity station = SecurityStationManager.getInstance().getStation(getOriginalUpgradeManager().getSecurityID());
			if (station != null) {
				settings = station.getSecuritySettingsForPlayer(entityplayer, true);
			}
		}

		if (MainProxy.isPipeControllerEquipped(entityplayer)) {
			if (MainProxy.isServer(entityplayer.world)) {
				if (settings == null || settings.openNetworkMonitor) {
					NewGuiHandler.getGui(PipeController.class).setTilePos(container).open(entityplayer);
				} else {
					entityplayer.sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			return true;
		}

		if (handleClick(entityplayer, settings)) {
			return true;
		}

		if (entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty()) {
			if (!entityplayer.isSneaking()) {
				return false;
			}
			/*
			if (MainProxy.isClient(entityplayer.world)) {
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

		if (entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() == LPItems.remoteOrderer) {
			if (MainProxy.isServer(entityplayer.world)) {
				if (settings == null || settings.openRequest) {
					entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, getWorld(), getX(), getY(), getZ());
				} else {
					entityplayer.sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			return true;
		}

		if (ConfigToolHandler.INSTANCE.canWrench(entityplayer, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), container)) {
			if (MainProxy.isServer(entityplayer.world)) {
				if (settings == null || settings.openGui) {
					if (getLogisticsModule() != null && getLogisticsModule() instanceof LogisticsGuiModule) {
						((LogisticsGuiModule) getLogisticsModule()).getPipeGuiProviderForModule().setTilePos(container).open(entityplayer);
					} else {
						onWrenchClicked(entityplayer);
					}
				} else {
					entityplayer.sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			ConfigToolHandler.INSTANCE.wrenchUsed(entityplayer, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), container);
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

	/* ITrackStatistics */

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
	public void itemCouldNotBeSend(ItemStack item, IAdditionalTargetInformation info) {
		if (this instanceof IRequireReliableTransport) {
			((IRequireReliableTransport) this).itemLost(item, info);
		}
	}

	public boolean isLockedExit(Direction orientation) {
		return false;
	}

	public boolean logisitcsIsPipeConnected(BlockEntity tile, Direction dir) {
		return false;
	}

	public boolean disconnectPipe(BlockEntity tile, Direction dir) {
		return false;
	}

	@Override
	public final boolean canPipeConnect(BlockEntity tile, Direction dir) {
		return canPipeConnect(tile, dir, false);
	}

	@Override
	public final boolean canPipeConnect(BlockEntity tile, Direction dir, boolean ignoreSystemDisconnection) {
		Direction side = OrientationsUtil.getOrientationOfTileWithTile(container, tile);
		if (isSideBlocked(side, ignoreSystemDisconnection)) {
			return false;
		}
		return (super.canPipeConnect(tile, dir) || logisitcsIsPipeConnected(tile, dir)) && !disconnectPipe(tile, dir);
	}

	@Override
	public final boolean isSideBlocked(Direction side, boolean ignoreSystemDisconnection) {
		if (getUpgradeManager().isSideDisconnected(side)) {
			return true;
		}
		if (!stillNeedReplace) {
			if (getRouter().isSideDisconnected(side) && !ignoreSystemDisconnection && !globalIgnoreConnectionDisconnection) {
				return true;
			}
		}
		return false;
	}

	public void connectionUpdate() {
		if (container != null && !stillNeedReplace) {
			container.scheduleNeighborChange();
			BlockState state = getWorld().getBlockState(getPos());
			getWorld().notifyNeighborsOfStateChange(getPos(), state == null ? null : state.getBlock(), true);
		}
	}

	public UUID getSecurityID() {
		return getOriginalUpgradeManager().getSecurityID();
	}

	public void insetSecurityID(UUID id) {
		getOriginalUpgradeManager().insetSecurityID(id);
	}

	public List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> getRoutedPowerProviders() {
		if (MainProxy.isClient(getWorld())) {
			return null;
		}
		if (stillNeedReplace) {
			return null;
		}
		return getRouter().getPowerProvider();
	}

	/* Power System */

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
		if (LPConfiguration.INSTANCE.getPowerUsageMultiplier() <= 0) {
			return true;
		}
		if (amount == 0) {
			return true;
		}
		if (providersToIgnore != null && providersToIgnore.contains(this)) {
			return false;
		}
		List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> list = getRoutedPowerProviders();
		if (list == null) {
			return false;
		}
		outer:
		for (Tuple2<ILogisticsPowerProvider, List<IFilter>> provider : list) {
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
		if (LPConfiguration.INSTANCE.getPowerUsageMultiplier() <= 0) {
			return true;
		}
		if (amount == 0) {
			return true;
		}
		if (providersToIgnore == null) {
			providersToIgnore = new ArrayList<>();
		}
		if (providersToIgnore.contains(this)) {
			return false;
		}
		providersToIgnore.add(this);
		List<Tuple2<ILogisticsPowerProvider, List<IFilter>>> list = getRoutedPowerProviders();
		if (list == null) {
			return false;
		}
		outer:
		for (Tuple2<ILogisticsPowerProvider, List<IFilter>> provider : list) {
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
	public int compareTo(@NotNull ItemRequester other) {
		return getID() - other.getID();
	}

	@Override
	public int getID() {
		return getRouter().getSimpleId();
	}

	public Set<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	public boolean hasGenericInterests() {
		return false;
	}

	public ISecurityProvider getSecurityProvider() {
		return SecurityStationManager.getInstance().getStation(getOriginalUpgradeManager().getSecurityID());
	}

	public boolean canBeDestroyedByPlayer(EntityPlayer entityPlayer) {
		LogisticsSecurityTileEntity station = SecurityStationManager.getInstance().getStation(getOriginalUpgradeManager().getSecurityID());
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

	public void queueUnroutedItemInformation(ItemStack item, ItemRoutingInformation information) {
		if (item != null) {
			synchronized (queuedDataForUnroutedItems) {
				Queue<Tuple2<Integer, ItemRoutingInformation>> queue = queuedDataForUnroutedItems.get(item.getItem());
				if (queue == null) {
					queuedDataForUnroutedItems.put(item.getItem(), queue = new LinkedList<>());
				}
				queue.add(new Tuple2<>(item.getCount(), information));
			}
		}
	}

	public ItemRoutingInformation getQueuedForItemStack(ItemStack item) {
		synchronized (queuedDataForUnroutedItems) {
			Queue<Tuple2<Integer, ItemRoutingInformation>> queue = queuedDataForUnroutedItems.get(item.getItem());
			if (queue == null || queue.isEmpty()) {
				return null;
			}

			Tuple2<Integer, ItemRoutingInformation> tuple = queue.peek();
			int wantItem = tuple.getValue1();

			if (wantItem <= item.getCount()) {
				if (queue.remove() != tuple) {
					LogisticsPipes.log.fatal("Item queue mismatch");
					return null;
				}
				if (queue.isEmpty()) {
					queuedDataForUnroutedItems.remove(item.getItem());
				}
				item.setStackSize(wantItem);
				return tuple.getValue2();
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
			ItemStack stack = information.getItem();
			if (stack.getItem().isFluidContainer()) {
				FluidIdentifierStack liquid = LogisticsFluidManager.getInstance().getFluidFromContainer(stack);
				if (liquid != null) {
					((IRequireReliableFluidTransport) this).liquidArrived(liquid.getFluid(), liquid.getAmount());
				}
			}
		}
	}

	@Override
	public int countOnRoute(ItemIdentifier it) {
		int count = 0;
		for (ItemRoutingInformation next : _inTransitToMe) {
			if (next.getItem().getItem().equals(it)) {
				count += next.getItem().getCount();
			}
		}
		return count;
	}

	public void addCrashReport(CrashReportSection crashReportSection) {
		addRouterCrashReport(crashReportSection);
		crashReportSection.add("stillNeedReplace", stillNeedReplace);
	}

	protected void addRouterCrashReport(CrashReportSection crashReportSection) {
		crashReportSection.add("Router", getRouter().toString());
	}

	public void onWrenchClicked(PlayerEntity entityplayer) {
		// do nothing, every pipe with a GUI should either have a LogisticsGuiModule or override this method
	}

	/* IInventoryProvider */

	@Nullable
	@Override
	public WrappedInventory getPointedInventory() {
		final NeighborBlockEntity<BlockEntity> pointedItemHandler = getPointedItemHandler();
		if (pointedItemHandler == null) return null;
		return pointedItemHandler.getUtilForItemHandler();
	}

	@Nullable
	@Override
	public WrappedInventory getPointedInventory(ExtractionMode mode) {
		final NeighborBlockEntity<BlockEntity> neighborItemHandler = getPointedItemHandler();
		if (neighborItemHandler == null) {
			return null;
		}
		return getInventoryForExtractionMode(mode, neighborItemHandler);
	}

	public static WrappedInventory getInventoryForExtractionMode(ExtractionMode mode, World world, BlockPos pos, Direction side) {
		switch (mode) {
			case LeaveFirst:
				return InventoryUtilFactory.INSTANCE.getHidingInventoryUtil(
						world, pos, side, false, false, 1, 0);
			case LeaveLast:
				return InventoryUtilFactory.INSTANCE.getHidingInventoryUtil(
						world, pos, side, false, false, 0, 1);
			case LeaveFirstAndLast:
				return InventoryUtilFactory.INSTANCE.getHidingInventoryUtil(
						world, pos, side, false, false, 1, 1);
			case Leave1PerStack:
				return InventoryUtilFactory.INSTANCE.getHidingInventoryUtil(
						world, pos, side, true, false, 0, 0);
			case Leave1PerType:
				return InventoryUtilFactory.INSTANCE.getHidingInventoryUtil(
						world, pos, side,
						false, true, 0, 0);
			case Normal:
				return InventoryUtilFactory.INSTANCE.getHidingInventoryUtil(
						world, pos, side, false, false, 0, 0);
			default:
				throw new IllegalArgumentException(String.format("Don't know how to handle extraction mode %s", mode));
		}
	}

	@Nullable
	@Override
	public WrappedInventory getSneakyInventory(ModulePositionType slot, int positionInt) {
		final NeighborBlockEntity<BlockEntity> pointedItemHandler = getPointedItemHandler();
		if (pointedItemHandler == null) {
			return null;
		}
		return pointedItemHandler.sneakyInsertion().from(getUpgradeManager(slot, positionInt)).getUtilForItemHandler();
	}

	@Nullable
	@Override
	public WrappedInventory getSneakyInventory(@Nonnull Direction direction) {
		final NeighborBlockEntity<BlockEntity> pointedItemHandler = getPointedItemHandler();
		if (pointedItemHandler == null) {
			return null;
		}
		return pointedItemHandler.sneakyInsertion().from(direction).getUtilForItemHandler();
	}

	@Nullable
	@Override
	public NeighborBlockEntity<BlockEntity> getPointedItemHandler() {
		final Direction pointedOrientation = getPointedOrientation();
		if (pointedOrientation == null) return null;
		final BlockEntity tile = getContainer().getTile(pointedOrientation);
		if (tile == null) return null;
		final NeighborBlockEntity<BlockEntity> neighbor = new NeighborBlockEntity<>(tile, pointedOrientation);
		if (!neighbor.isItemHandler()) return null;
		return neighbor;
	}

	@Override
	public Tuple3<Integer, SinkReply, List<IFilter>> hasDestination(ItemStack stack, boolean allowDefault, List<Integer> routerIDsToExclude) {
		return LogisticsManager.getInstance().hasDestination(stack, allowDefault, getRouter().getSimpleId(), routerIDsToExclude);
	}

	@Override
	public IRoutedItem sendStack(ItemStack stack, Tuple2<Integer, SinkReply> reply, ItemSendMode mode) {
		IRoutedItem itemToSend = RoutedItemHelper.INSTANCE.createNewTravelItem(stack);
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
		IRoutedItem itemToSend = RoutedItemHelper.INSTANCE.createNewTravelItem(stack);
		itemToSend.setDestination(destination);
		itemToSend.setTransportMode(TransportMode.Active);
		itemToSend.setAdditionalTargetInformation(info);
		queueRoutedItem(itemToSend, getPointedOrientation(), mode);
		return itemToSend;
	}

	@Nullable
	@Override
	public Direction getPointedOrientation() {
		if (pointedDirection == null) {
			final Optional<Direction> firstDirection = new WorldCoordinatesWrapper(container)
					.connectedTileEntities(ConnectionPipeType.ITEM)
					.filter(adjacent -> !PipeInformationManager.INSTANCE.isPipe(adjacent.getBlockEntity()))
					.map(NeighborBlockEntity::getDirection)
					.findFirst();
			firstDirection.ifPresent(enumFacing -> pointedDirection = enumFacing);
		}
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

	public boolean addPipeSign(Direction dir, PipeSign sign, PlayerEntity player) {
		if (signItem.containsKey(dir)) return false;
		if (!sign.getType().isAllowedFor(this)) return false;
		sign.init(this, dir, player);
		signItem.put(dir, sign);
		sendSignData(player, true);
		refreshRender(false);
		return true;
	}

	public void sendSignData(PlayerEntity player, boolean sendToAll) {
		List<Integer> types = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			if (signItem[i] == null) {
				types.add(-1);
			} else {
				List<Class<? extends PipeSign>> typeClasses = ItemPipeSignCreator.signTypes;
				for (int j = 0; j < typeClasses.size(); j++) {
					if (typeClasses.get(j) == signItem[i].getClass()) {
						types.add(j);
						break;
					}
				}
			}
		}
		ModernPacket packet = PacketHandler.getPacket(PipeSignTypes.class).setTypes(types).setTilePos(container);
		if (sendToAll) {
			MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), getWorld().provider.getDimension(), packet);
		}
		MainProxy.sendPacketToPlayer(packet, player);
		for (int i = 0; i < 6; i++) {
			if (signItem[i] != null) {
				packet = signItem[i].getPacket();
				if (packet != null) {
					MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), getWorld().provider.getDimension(), packet);
					MainProxy.sendPacketToPlayer(packet, player);
				}
			}
		}
	}

	public boolean removePipeSign(Direction dir, PlayerEntity player) {
		if (signItem.remove(dir) != null) {
			sendSignData(player, true);
			refreshRender(false);
		}
	}

	public boolean hasPipeSign(Direction dir) {
		return signItem.containsKey(dir);
	}

	public boolean activatePipeSign(Direction side, PlayerEntity player) {
		if (signItem.get(side) != null) {
			signItem.get(side).activate(player);
			return true;
		}
		return false;
	}

	public List<Tuple2<Direction, PipeSign>> getPipeSigns() {
		List<Tuple2<Direction, PipeSign>> list = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			if (signItem[i] != null) {
				list.add(new Tuple2<>(EnumFacingUtil.getOrientation(i), signItem[i]));
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
				Class<? extends PipeSign> type = ItemPipeSignCreator.signTypes.get(integer);
				if (signItem[i] == null || signItem[i].getClass() != type) {
					try {
						signItem[i] = type.newInstance();
						signItem[i].init(this, EnumFacingUtil.getOrientation(i));
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				signItem[i] = null;
			}
		}
	}

	@Nullable
	public PipeSign getPipeSign(@Nullable Direction dir) {
		if (dir == null) return null;
		return signItem[dir.ordinal()];
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeBoolean(isOpaque());
	}

	@Override
	public void readData(LPDataInput input) {
		isOpaqueClientSide = input.readBoolean();
	}

	@Override
	public boolean isOpaque() {
		if (MainProxy.isClient(getWorld())) {
			return LPConfiguration.INSTANCE.getOpaquePipes() || isOpaqueClientSide;
		} else {
			return LPConfiguration.INSTANCE.getOpaquePipes() || this.getUpgradeManager().isOpaque();
		}
	}

	@Override
	public void addStatusInformation(List<StatusEntry> status) {
		StatusEntry entry = new StatusEntry();
		entry.name = "Send Queue";
		entry.subEntry = new ArrayList<>();
		for (Tuple3<IRoutedItem, Direction, ItemSendMode> part : _sendQueue) {
			StatusEntry subEntry = new StatusEntry();
			subEntry.name = part.toString();
			entry.subEntry.add(subEntry);
		}
		status.add(entry);
		entry = new StatusEntry();
		entry.name = "In Transit To Me";
		entry.subEntry = new ArrayList<>();
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
	public double getDistanceTo(UUID destination, Direction ignore, ItemVariant ident, boolean isActive, double traveled, double max, List<BlockPos> visited) {
		if (!stillNeedReplace) {
			if (getRouter().getId().equals(destination)) {
				return 0;
			}
			ExitRoute route = getRouter().getExitFor(destination, isActive, ident.makeStack());
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

	@Override
	public IHighlightPlacementRenderer getHighlightRenderer() {
		return LogisticsRenderPipe.secondRenderer;
	}

	public enum ItemSendMode {
		Normal,
		Fast
	}
}
