/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import lombok.Getter;

import logisticspipes.LPConstants;
import logisticspipes.LPItems;
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
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.ITrackStatistics;
import logisticspipes.logisticspipes.PipeTransportLayer;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
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
import logisticspipes.pipes.signs.IPipeSign;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.CCConstants;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCDirectCall;
import logisticspipes.proxy.computers.interfaces.CCSecurtiyCheck;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LogisticsItemOrderManager;
import logisticspipes.routing.order.LogisticsOrderManager;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.security.PermissionException;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.EnumFacingUtil;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

@CCType(name = "LogisticsPipes:Normal")
public abstract class CoreRoutedPipe extends CoreUnroutedPipe
		implements IClientState, IRequestItems, ITrackStatistics, IWorldProvider, IWatchingHandler, IPipeServiceProvider, IQueueCCEvent, ILPPositionProvider {

	private static int pipecount = 0;
	public final PlayerCollectionList watchers = new PlayerCollectionList();
	protected final PriorityBlockingQueue<ItemRoutingInformation> _inTransitToMe = new PriorityBlockingQueue<>(10,
			new ItemRoutingInformation.DelayComparator());
	protected final LinkedList<Triplet<IRoutedItem, EnumFacing, ItemSendMode>> _sendQueue = new LinkedList<>();
	protected final Map<ItemIdentifier, Queue<Pair<Integer, ItemRoutingInformation>>> queuedDataForUnroutedItems = Collections.synchronizedMap(new TreeMap<>());
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
	protected IRouter router;
	protected String routerId;
	protected final Object routerIdLock = new Object();
	protected int _delayOffset;
	protected boolean _initialInit = true;
	protected RouteLayer _routeLayer;
	protected TransportLayer _transportLayer;
	protected UpgradeManager upgradeManager = new UpgradeManager(this);
	protected LogisticsItemOrderManager _orderItemManager = null;
	protected List<TileEntity> _cachedAdjacentInventories;
	protected EnumFacing pointedDirection = null;
	//public BaseRoutingLogic logic;
	// from BaseRoutingLogic
	protected int throttleTime = 20;
	protected IPipeSign[] signItem = new IPipeSign[6];
	private boolean recheckConnections = false;
	private boolean enabled = true;
	private boolean preventRemove = false;
	private boolean destroyByPlayer = false;
	private PowerSupplierHandler powerHandler = new PowerSupplierHandler(this);
	@Getter
	private List<IOrderInfoProvider> clientSideOrderManager = new ArrayList<>();
	private int throttleTimeLeft = 20 + new Random().nextInt(Configs.LOGISTICS_DETECTION_FREQUENCY);
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

		//Roughly spread pipe updates throughout the frequency, no need to maintain balance
		_delayOffset = CoreRoutedPipe.pipecount % Configs.LOGISTICS_DETECTION_FREQUENCY;
	}

	@Nonnull
	public RouteLayer getRouteLayer() {
		if (_routeLayer == null) {
			_routeLayer = new RouteLayer(getRouter(), getTransportLayer(), this);
		}
		return _routeLayer;
	}

	@Nonnull
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
	public void queueRoutedItem(IRoutedItem routedItem, EnumFacing from) {
		if (from == null) {
			throw new NullPointerException();
		}
		_sendQueue.addLast(new Triplet<>(routedItem, from, ItemSendMode.Normal));
		sendQueueChanged(false);
	}

	public void queueRoutedItem(IRoutedItem routedItem, EnumFacing from, ItemSendMode mode) {
		if (from == null) {
			throw new NullPointerException();
		}
		_sendQueue.addLast(new Triplet<>(routedItem, from, mode));
		sendQueueChanged(false);
	}

	/**
	 * @param force == true never delegates to a thread
	 * @return number of things sent.
	 */
	public int sendQueueChanged(boolean force) {
		return 0;
	}

	private void sendRoutedItem(IRoutedItem routedItem, EnumFacing from) {

		if (from == null) {
			throw new NullPointerException();
		}

		transport.injectItem(routedItem, from.getOpposite());

		IRouter r = SimpleServiceLocator.routerManager.getServerRouter(routedItem.getDestination());
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
		List<TileEntity> others = other.getConnectedRawInventories();
		if (others == null || others.size() == 0) {
			return false;
		}
		for (TileEntity i : getConnectedRawInventories()) {
			if (others.contains(i)) {
				return true;
			}
		}
		return false;
	}

	protected List<TileEntity> getConnectedRawInventories() {
		if (_cachedAdjacentInventories == null) {
			_cachedAdjacentInventories = new WorldCoordinatesWrapper(container)
					.connectedTileEntities(ConnectionPipeType.ITEM)
					.filter(adjacent -> adjacent.isItemHandler() && !adjacent.isLogisticsPipe())
					.map(NeighborTileEntity::getTileEntity)
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
			//IBlockState state = getWorld().getBlockState(getPos());
			//getWorld().notifyNeighborsOfStateChange(getPos(), state == null ? null : state.getBlock(), true);
			/* TravelingItems are just held by a pipe, they don't need to know their world
			 * for(Triplet<IRoutedItem, EnumFacing, ItemSendMode> item : _sendQueue) {
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
				IBlockState state = getWorld().getBlockState(getPos());
				getWorld().notifyNeighborsOfStateChange(getPos(), state == null ? null : state.getBlock(), true);
			}
		}

		// remove old items _inTransit -- these should have arrived, but have probably been lost instead. In either case, it will allow a re-send so that another attempt to re-fill the inventory can be made.
		while (_inTransitToMe.peek() != null && _inTransitToMe.peek().getTickToTimeOut() <= 0) {
			final ItemRoutingInformation p = _inTransitToMe.poll();
			if (LogisticsPipes.isDEBUG()) {
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
				Triplet<IRoutedItem, EnumFacing, ItemSendMode> itemToSend = _sendQueue.getFirst();
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
						Triplet<IRoutedItem, EnumFacing, ItemSendMode> itemToSend = _sendQueue.getFirst();
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
		//entityplayer.world.setWorldTime(4951);
		if (!MainProxy.isServer(entityplayer.world)) {
			return;
		}
		ServerRouter router = (ServerRouter) getRouter();

		System.out.println("***");
		System.out.println("---------Interests---------------");
		ServerRouter.forEachGlobalSpecificInterest((itemIdentifier, serverRouters) -> {
			System.out.print(itemIdentifier.getFriendlyName() + ":");
			for (IRouter j : serverRouters) {
				System.out.print(j.getSimpleID() + ",");
			}
			System.out.println();
		});

		System.out.print("ALL ITEMS:");
		for (IRouter j : ServerRouter.getInterestedInGeneral()) {
			System.out.print(j.getSimpleID() + ",");
		}
		System.out.println();

		System.out.println(router.toString());
		System.out.println("---------CONNECTED TO---------------");
		for (CoreRoutedPipe adj : router._adjacent.keySet()) {
			System.out.println(adj.getRouter().getSimpleID());
		}
		System.out.println();
		System.out.println("========DISTANCE TABLE==============");
		for (ExitRoute n : router.getIRoutersByCost()) {
			System.out
					.println(n.destination.getSimpleID() + " @ " + n.distanceToDestination + " -> " + n.connectionDetails + "(" + n.destination.getId() + ")");
		}
		System.out.println();
		System.out.println("*******EXIT ROUTE TABLE*************");
		List<List<ExitRoute>> table = router.getRouteTable();
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
		System.out.println(Arrays.toString(EnumFacing.VALUES));
		System.out.println(Arrays.toString(router.sideDisconnected));
		System.out.println(Arrays.toString(container.pipeConnectionsBuffer));
		System.out.println();
		System.out.println("~~~~~~~~~~~~~~~POWER~~~~~~~~~~~~~~~~");
		System.out.println(router.getPowerProvider());
		System.out.println();
		System.out.println("~~~~~~~~~~~SUBSYSTEMPOWER~~~~~~~~~~~");
		System.out.println(router.getSubSystemPowerProvider());
		System.out.println();
		if (_orderItemManager != null) {
			System.out.println("################ORDERDUMP#################");
			_orderItemManager.dump();
		}
		System.out.println("################END#################");
		refreshConnectionAndRender(true);
		System.out.print("");
		router.CreateRouteTable(Integer.MAX_VALUE);
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

	public TextureType getTextureType(EnumFacing connection) {
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

	public TextureType getRoutedTexture(EnumFacing connection) {
		if (getRouter().isSubPoweredExit(connection)) {
			return Textures.LOGISTICSPIPE_SUBPOWER_TEXTURE;
		} else {
			return Textures.LOGISTICSPIPE_ROUTED_TEXTURE;
		}
	}

	public TextureType getNonRoutedTexture(EnumFacing connection) {
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
			ArrayList<ParticleCount> tosend = new ArrayList<>(queuedParticles.length);
			for (int i = 0; i < queuedParticles.length; i++) {
				if (queuedParticles[i] > 0) {
					tosend.add(new ParticleCount(Particles.values()[i], queuedParticles[i]));
				}
			}
			MainProxy.sendPacketToAllWatchingChunk(container, PacketHandler.getPacket(ParticleFX.class).setParticles(tosend).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
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

	protected boolean isPowerProvider(EnumFacing ori) {
		TileEntity tilePipe = container.getTile(ori);
		if (tilePipe == null || !container.canPipeConnect(tilePipe, ori)) {
			return false;
		}

		return tilePipe instanceof ILogisticsPowerProvider || tilePipe instanceof ISubSystemPowerProvider;
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
		for (Triplet<IRoutedItem, EnumFacing, ItemSendMode> p : _sendQueue) {
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
			EnumFacing from = EnumFacing.values()[tagentry.getByte("from")];
			ItemSendMode mode = ItemSendMode.values()[tagentry.getByte("mode")];
			_sendQueue.add(new Triplet<>(item, from, mode));
		}
		for (int i = 0; i < 6; i++) {
			if (nbttagcompound.getBoolean("PipeSign_" + i)) {
				int type = nbttagcompound.getInteger("PipeSign_" + i + "_type");
				Class<? extends IPipeSign> typeClass = ItemPipeSignCreator.signTypes.get(type);
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
	@Nonnull
	public IRouter getRouter() {
		if (stillNeedReplace) {
			System.out.format("Hey, don't get routers for pipes that aren't ready (%d, %d, %d, '%s')", this.getX(), this.getY(), this.getZ(),
					this.getWorld().getWorldInfo().getWorldName());
			new Throwable().printStackTrace();
		}
		if (router == null) {
			synchronized (routerIdLock) {

				UUID routerIntId = null;
				if (routerId != null && !routerId.isEmpty()) {
					routerIntId = UUID.fromString(routerId);
				}
				router = SimpleServiceLocator.routerManager.getOrCreateRouter(routerIntId, getWorld(), getX(), getY(), getZ());
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
		if (MainProxy.isServer(entityplayer.world)) {
			LogisticsSecurityTileEntity station = SimpleServiceLocator.securityStationManager.getStation(getOriginalUpgradeManager().getSecurityID());
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
			if (LogisticsPipes.isDEBUG()) {
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

		if (SimpleServiceLocator.configToolHandler.canWrench(entityplayer, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), container)) {
			if (MainProxy.isServer(entityplayer.world)) {
				if (settings == null || settings.openGui) {
					if (getLogisticsModule() != null && getLogisticsModule() instanceof Gui) {
						Gui.getPipeGuiProvider((Gui) getLogisticsModule()).setTilePos(container).open(entityplayer);
					} else {
						onWrenchClicked(entityplayer);
					}
				} else {
					entityplayer.sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			SimpleServiceLocator.configToolHandler.wrenchUsed(entityplayer, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), container);
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
	public void itemCouldNotBeSend(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		if (this instanceof IRequireReliableTransport) {
			((IRequireReliableTransport) this).itemLost(item, info);
		}
	}

	public boolean isLockedExit(EnumFacing orientation) {
		return false;
	}

	public boolean logisitcsIsPipeConnected(TileEntity tile, EnumFacing dir) {
		return false;
	}

	public boolean disconnectPipe(TileEntity tile, EnumFacing dir) {
		return false;
	}

	@Override
	public final boolean canPipeConnect(TileEntity tile, EnumFacing dir) {
		return canPipeConnect(tile, dir, false);
	}

	@Override
	public final boolean canPipeConnect(TileEntity tile, EnumFacing dir, boolean ignoreSystemDisconnection) {
		EnumFacing side = OrientationsUtil.getOrientationOfTilewithTile(container, tile);
		if (isSideBlocked(side, ignoreSystemDisconnection)) {
			return false;
		}
		return (super.canPipeConnect(tile, dir) || logisitcsIsPipeConnected(tile, dir)) && !disconnectPipe(tile, dir);
	}

	@Override
	public final boolean isSideBlocked(EnumFacing side, boolean ignoreSystemDisconnection) {
		if (getUpgradeManager().isSideDisconnected(side)) {
			return true;
		}
		return !stillNeedReplace && getRouter().isSideDisconnected(side) && !ignoreSystemDisconnection && !globalIgnoreConnectionDisconnection;
	}

	public void connectionUpdate() {
		if (container != null && !stillNeedReplace) {
			container.scheduleNeighborChange();
			IBlockState state = getWorld().getBlockState(getPos());
			getWorld().notifyNeighborsOfStateChange(getPos(), state.getBlock(), true);
		}
	}

	public UUID getSecurityID() {
		return getOriginalUpgradeManager().getSecurityID();
	}

	public void insetSecurityID(UUID id) {
		getOriginalUpgradeManager().insetSecurityID(id);
	}

	public List<Pair<ILogisticsPowerProvider, List<IFilter>>> getRoutedPowerProviders() {
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
			providersToIgnore = new ArrayList<>();
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

	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {}

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
					queuedDataForUnroutedItems.put(item.getItem(), queue = new LinkedList<>());
				}
				queue.add(new Pair<>(item.getStackSize(), information));
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
				FluidIdentifierStack liquid = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(stack);
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
				count += next.getItem().getStackSize();
			}
		}
		return count;
	}

	@Override
	public final int getIconIndex(EnumFacing connection) {
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
		IRouter router = SimpleServiceLocator.routerManager.getRouter(id.intValue());
		if (router == null) {
			return null;
		}
		return router.getId().toString();
	}

	@CCCommand(description = "Returns the TurtleConnect targeted for this Turtle on this LogisticsPipe")
	@CCDirectCall
	public boolean getTurtleConnect() {
		if (container instanceof LogisticsTileGenericPipe) {
			return container.getTurtleConnect();
		}
		return false;
	}

	@CCCommand(description = "Sets the TurtleConnect targeted for this Turtle on this LogisticsPipe")
	@CCDirectCall
	public void setTurtleConnect(Boolean flag) {
		if (container instanceof LogisticsTileGenericPipe) {
			container.setTurtleConnect(flag);
		}
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

	@CCCommand(description = "Sends a message to the given computerId over the LP network. Event: " + CCConstants.LP_CC_MESSAGE_EVENT)
	@CCDirectCall
	public void sendMessage(final Double computerId, final Object message) {
		int sourceId = -1;
		if (container instanceof LogisticsTileGenericPipe) {
			sourceId = SimpleServiceLocator.ccProxy.getLastCCID(container);
		}
		final int fSourceId = sourceId;
		BitSet set = new BitSet(ServerRouter.getBiggestSimpleID());
		getRouter().getIRoutersByCost().stream()
				.filter(exit -> !set.get(exit.destination.getSimpleID()))
				.forEach(exit -> {
					exit.destination.queueTask(10, (pipe, router1) -> pipe.handleMesssage(computerId.intValue(), message, fSourceId));
					set.set(exit.destination.getSimpleID());
				});
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
		getRouter().getIRoutersByCost().stream()
				.filter(exit -> !set.get(exit.destination.getSimpleID()))
				.forEach(exit -> {
					exit.destination.queueTask(10, (pipe, router1) -> pipe.handleBroadcast(message, fSourceId));
					set.set(exit.destination.getSimpleID());
				});
	}

	@CCCommand(description = "Returns the access to the pipe of the given router UUID")
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
		return router.getPipe();
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

	public void handleRFPowerArival(double toSend) {
		powerHandler.addRFPower(toSend);
	}

	public void handleIC2PowerArival(double toSend) {
		powerHandler.addIC2Power(toSend);
	}

	/* IInventoryProvider */

	@Nullable
	@Override
	public IInventoryUtil getPointedInventory() {
		final NeighborTileEntity<TileEntity> pointedItemHandler = getPointedItemHandler();
		if (pointedItemHandler == null) return null;
		return pointedItemHandler.getUtilForItemHandler();
	}

	@Nullable
	@Override
	public IInventoryUtil getPointedInventory(ExtractionMode mode) {
		final NeighborTileEntity<TileEntity> neighborItemHandler = getPointedItemHandler();
		if (neighborItemHandler == null) {
			return null;
		}
		return getInventoryForExtractionMode(mode, neighborItemHandler);
	}

	@Nullable
	public static IInventoryUtil getInventoryForExtractionMode(ExtractionMode mode, NeighborTileEntity<TileEntity> neighborItemHandler) {
		switch (mode) {
			case LeaveFirst:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(
						neighborItemHandler.getTileEntity(), neighborItemHandler.getOurDirection(),
						false, false, 1, 0);
			case LeaveLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(
						neighborItemHandler.getTileEntity(), neighborItemHandler.getOurDirection(),
						false, false, 0, 1);
			case LeaveFirstAndLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(
						neighborItemHandler.getTileEntity(), neighborItemHandler.getOurDirection(),
						false, false, 1, 1);
			case Leave1PerStack:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(
						neighborItemHandler.getTileEntity(), neighborItemHandler.getOurDirection(),
						true, false, 0, 0);
			case Leave1PerType:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(
						neighborItemHandler.getTileEntity(), neighborItemHandler.getOurDirection(),
						false, true, 0, 0);
			default:
				break;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(
				neighborItemHandler.getTileEntity(), neighborItemHandler.getOurDirection(),
				false, false, 0, 0);
	}

	@Nullable
	@Override
	public IInventoryUtil getSneakyInventory(ModulePositionType slot, int positionInt) {
		final NeighborTileEntity<TileEntity> pointedItemHandler = getPointedItemHandlerCustom(
				(tile, pointed) -> new NeighborTileEntity<>(tile, pointed).sneakyInsertion().from(getUpgradeManager(slot, positionInt)));
		if (pointedItemHandler == null) {
			return null;
		}
		return pointedItemHandler.getUtilForItemHandler();
	}

	@Nullable
	@Override
	public IInventoryUtil getSneakyInventory(@Nonnull EnumFacing direction) {
		final NeighborTileEntity<TileEntity> pointedItemHandler = getPointedItemHandlerCustom(
				(tile, pointed) -> new NeighborTileEntity<>(tile, pointed).sneakyInsertion().from(direction));
		if (pointedItemHandler == null) {
			return null;
		}
		return pointedItemHandler.getUtilForItemHandler();
	}

	@Nullable
	public NeighborTileEntity<TileEntity> getPointedItemHandlerCustom(@Nonnull BiFunction<TileEntity, EnumFacing, NeighborTileEntity<TileEntity>> neighbourFactory) {
		final EnumFacing pointedOrientation = getPointedOrientation();
		if (pointedOrientation == null) return null;
		final TileEntity tile = getContainer().getTile(pointedOrientation);
		if (tile == null) return null;
		final NeighborTileEntity<TileEntity> neighbor = neighbourFactory.apply(tile, pointedOrientation);
		if (!neighbor.isItemHandler()) return null;
		return neighbor;
	}

	@Nullable
	@Override
	public NeighborTileEntity<TileEntity> getPointedItemHandler() {
		return getPointedItemHandlerCustom(NeighborTileEntity::new);
	}

	/* ISendRoutedItem */

	@Override
	public IRoutedItem sendStack(@Nonnull ItemStack stack, Pair<Integer, SinkReply> reply, ItemSendMode mode) {
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
	public IRoutedItem sendStack(@Nonnull ItemStack stack, int destination, ItemSendMode mode, IAdditionalTargetInformation info) {
		IRoutedItem itemToSend = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stack);
		itemToSend.setDestination(destination);
		itemToSend.setTransportMode(TransportMode.Active);
		itemToSend.setAdditionalTargetInformation(info);
		queueRoutedItem(itemToSend, getPointedOrientation(), mode);
		return itemToSend;
	}

	@Nullable
	@Override
	public EnumFacing getPointedOrientation() {
		if (pointedDirection == null) {
			final Optional<EnumFacing> firstDirection = new WorldCoordinatesWrapper(container)
					.connectedTileEntities(ConnectionPipeType.ITEM)
					.filter(adjacent -> !SimpleServiceLocator.pipeInformationManager.isPipe(adjacent.getTileEntity()))
					.map(NeighborTileEntity::getDirection)
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

	public void addPipeSign(EnumFacing dir, IPipeSign type, EntityPlayer player) {
		if (dir.ordinal() < 6) {
			if (signItem[dir.ordinal()] == null) {
				signItem[dir.ordinal()] = type;
				signItem[dir.ordinal()].init(this, dir);
			}
			if (container != null) {
				sendSignData(player, true);
				refreshRender(false);
			}
		}
	}

	public void sendSignData(EntityPlayer player, boolean sendToAll) {
		List<Integer> types = new ArrayList<>();
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
		if (sendToAll) {
			MainProxy.sendPacketToAllWatchingChunk(container, packet);
		}
		MainProxy.sendPacketToPlayer(packet, player);
		for (int i = 0; i < 6; i++) {
			if (signItem[i] != null) {
				packet = signItem[i].getPacket();
				if (packet != null) {
					MainProxy.sendPacketToAllWatchingChunk(container, packet);
					MainProxy.sendPacketToPlayer(packet, player);
				}
			}
		}
	}

	public void removePipeSign(EnumFacing dir, EntityPlayer player) {
		if (dir.ordinal() < 6) {
			signItem[dir.ordinal()] = null;
		}
		sendSignData(player, true);
		refreshRender(false);
	}

	public boolean hasPipeSign(EnumFacing dir) {
		if (dir.ordinal() < 6) {
			return signItem[dir.ordinal()] != null;
		}
		return false;
	}

	public void activatePipeSign(EnumFacing dir, EntityPlayer player) {
		if (dir.ordinal() < 6) {
			if (signItem[dir.ordinal()] != null) {
				signItem[dir.ordinal()].activate(player);
			}
		}
	}

	public List<Pair<EnumFacing, IPipeSign>> getPipeSigns() {
		List<Pair<EnumFacing, IPipeSign>> list = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			if (signItem[i] != null) {
				list.add(new Pair<>(EnumFacingUtil.getOrientation(i), signItem[i]));
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
	public IPipeSign getPipeSign(@Nullable EnumFacing dir) {
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
			return Configs.OPAQUE || isOpaqueClientSide;
		} else {
			return Configs.OPAQUE || this.getUpgradeManager().isOpaque();
		}
	}

	@Override
	public void addStatusInformation(List<StatusEntry> status) {
		StatusEntry entry = new StatusEntry();
		entry.name = "Send Queue";
		entry.subEntry = new ArrayList<>();
		for (Triplet<IRoutedItem, EnumFacing, ItemSendMode> part : _sendQueue) {
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
	public double getDistanceTo(int destinationint, EnumFacing ignore, ItemIdentifier ident, boolean isActive, double traveled, double max,
			List<DoubleCoordinates> visited) {
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

	@Override
	public IHighlightPlacementRenderer getHighlightRenderer() {
		return LogisticsRenderPipe.secondRenderer;
	}

	public enum ItemSendMode {
		Normal,
		Fast
	}
}
