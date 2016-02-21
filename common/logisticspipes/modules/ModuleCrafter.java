package logisticspipes.modules;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.interfaces.*;
import logisticspipes.interfaces.routing.*;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.module.inhand.CraftingModuleInHand;
import logisticspipes.network.guis.module.inpipe.CraftingModuleSlot;
import logisticspipes.network.packets.block.CraftingPipeNextAdvancedSatellitePacket;
import logisticspipes.network.packets.block.CraftingPipePrevAdvancedSatellitePacket;
import logisticspipes.network.packets.cpipe.*;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.pipe.*;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IFuzzyRecipeProvider;
import logisticspipes.request.IPromise;
import logisticspipes.request.ItemCraftingTemplate;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.FluidResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LogisticsItemOrder;
import logisticspipes.utils.*;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.SinkReply.BufferMode;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraft.world.World;


import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.DelayQueue;

public class ModuleCrafter extends LogisticsGuiModule implements ICraftItems, IHUDModuleHandler, IModuleWatchReciver {

	private PipeItemsCraftingLogistics _pipe;

	private IRequestItems _invRequester;
	//private EnumFacing _sneakyDirection = UtilEnumFacing.UNKNOWN;

	public int satelliteId = 0;
	public int[] advancedSatelliteIdArray = new int[9];
	public int[] fuzzyCraftingFlagArray = new int[9];
	public int priority = 0;

	// from PipeItemsCraftingLogistics
	protected ItemIdentifierInventory _dummyInventory = new ItemIdentifierInventory(11, "Requested items", 127);
	protected ItemIdentifierInventory _liquidInventory = new ItemIdentifierInventory(ItemUpgrade.MAX_LIQUID_CRAFTER, "Fluid items", 1, true);
	protected ItemIdentifierInventory _cleanupInventory = new ItemIdentifierInventory(ItemUpgrade.MAX_CRAFTING_CLEANUP * 3, "Cleanup Filer Items", 1);

	protected int[] amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	public int[] liquidSatelliteIdArray = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	public int liquidSatelliteId = 0;

	public boolean[] craftingSigns = new boolean[6];
	public boolean waitingForCraft = false;

	public final LinkedList<LogisticsItemOrder> _extras = new LinkedList<LogisticsItemOrder>();
	private WeakReference<TileEntity> lastAccessedCrafter = new WeakReference<TileEntity>(null);

	public boolean cleanupModeIsExclude = true;
	// for reliable transport
	protected final DelayQueue<DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>>> _lostItems = new DelayQueue<DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>>>();

	protected final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	public ModuleCrafter() {}

	public ModuleCrafter(PipeItemsCraftingLogistics parent) {
		_pipe = parent;
		_service = parent;
		_invRequester = parent;
		_world = parent;
		registerPosition(ModulePositionType.IN_PIPE, 0);
	}

	/**
	 * assumes that the invProvider is also IRequest items.
	 */
	@Override
	public void registerHandler(IWorldProvider world, IPipeServiceProvider service) {
		super.registerHandler(world, service);
		_invRequester = (IRequestItems) service;
	}

	protected SinkReply _sinkReply;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		return new SinkReply(_sinkReply, spaceFor(item, includeInTransit), areAllOrderesToBuffer() ? BufferMode.DESTINATION_BUFFERED : BufferMode.NONE);
	}

	protected int spaceFor(ItemIdentifier item, boolean includeInTransit) {
		Pair<String, ItemIdentifier> key = new Pair<String, ItemIdentifier>("spaceFor", item);
		Object cache = _service.getCacheHolder().getCacheFor(CacheTypes.Inventory, key);
		if (cache != null) {
			int count = (Integer) cache;
			if (includeInTransit) {
				count -= _service.countOnRoute(item);
			}
			return count;
		}
		int count = 0;
		WorldUtil wUtil = new WorldUtil(getWorld(), _service.getblockpos());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)) {
			if (!(tile.tile instanceof IInventory)) {
				continue;
			}
			IInventory base = (IInventory) tile.tile;
			if (base instanceof net.minecraft.inventory.ISidedInventory) {
				base = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) base, tile.orientation.getOpposite(), false, utilEnumFacing);
			}
			EnumFacing dir = tile.orientation;
			if (getUpgradeManager().hasSneakyUpgrade()) {
				dir = getUpgradeManager().getSneakyOrientation();
			}
			IInventoryUtil inv = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(base, dir);
			count += inv.roomForItem(item, 9999);
		}
		_service.getCacheHolder().setCache(CacheTypes.Inventory, key, count);
		if (includeInTransit) {
			count -= _service.countOnRoute(item);
		}
		return count;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	public void onAllowedRemoval() {}

	@Override
	public void tick() {
		enabledUpdateEntity();
		if (_lostItems.isEmpty()) {
			return;
		}
		// if(true) return;
		DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>> lostItem = _lostItems.poll();
		while (lostItem != null) {

			Pair<ItemIdentifierStack, IAdditionalTargetInformation> pair = lostItem.get();
			if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
				SinkReply reply = LogisticsManager.canSink(getRouter(), null, true, pair.getValue1().getItem(), null, true, true);
				if (reply == null || reply.maxNumberOfItems < 1) {
					_lostItems.add(new DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>>(pair, 5000));
					lostItem = _lostItems.poll();
					continue;
				}
			}
			int received = RequestTree.requestPartial(pair.getValue1(), (CoreRoutedPipe) _service, pair.getValue2());
			if (received < pair.getValue1().getStackSize()) {
				pair.getValue1().setStackSize(pair.getValue1().getStackSize() - received);
				_lostItems.add(new DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>>(pair, 5000));
			}
			lostItem = _lostItems.poll();
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {}

	@Override
	public void itemLost(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		_lostItems.add(new DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>>(new Pair<ItemIdentifierStack, IAdditionalTargetInformation>(item, info), 5000));
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		List<ItemIdentifierStack> result = getCraftedItems();
		if (result == null) {
			return null;
		}
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		for (ItemIdentifierStack craftable : result) {
			l1.add(craftable.getItem());
		}
		/*
		for(int i=0; i<9;i++) {
			ItemIdentifierStack stack = getMaterials(i);
			if(stack != null) {
				l1.add(stack.getItem()); // needed to be interested in things for a chassi to report reliableDelivery failure.
			}
		}
		 */
		return l1;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
		// when we are default we are interested in everything anyway, otherwise we're only interested in our filter.
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return false;
	}

	@Override
	public void canProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filters) {

		if (_extras.isEmpty()) {
			return;
		}

		IResource requestedItem = tree.getRequestType();

		if (!canCraft(requestedItem)) {
			return;
		}

		for (IFilter filter : filters) {
			if (filter.isBlocked() == filter.isFilteredItem(requestedItem) || filter.blockProvider()) {
				return;
			}
		}
		int remaining = 0;
		for (LogisticsItemOrder extra : _extras) {
			if (extra.getItemStack().getItem().equals(requestedItem)) {
				remaining += extra.getItemStack().getStackSize();
			}

		}
		remaining -= root.getAllPromissesFor(this, getCraftedItem().getItem());
		if (remaining < 1) {
			return;
		}
		LogisticsExtraPromise promise = new LogisticsExtraPromise(getCraftedItem().getItem(), Math.min(remaining, tree.getMissingAmount()), this, true);
		tree.addPromise(promise);

	}

	@Override
	public LogisticsItemOrder fullFill(LogisticsPromise promise, IRequestItems destination, IAdditionalTargetInformation info) {
		if (promise instanceof LogisticsExtraPromise) {
			removeExtras(promise.numberOfItems, promise.item);
		}
		_service.spawnParticle(Particles.WhiteParticle, 2);
		return _service.getItemOrderManager().addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, ResourceType.CRAFTING, info);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list, List<IFilter> filter) {

	}

	@Override
	public IRouter getRouter() {
		return _service.getRouter();
	}

	@Override
	public void itemCouldNotBeSend(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		_invRequester.itemCouldNotBeSend(item, info);
	}

	@Override
	public int getID() {
		return _service.getRouter().getSimpleID();
	}

	@Override
	public int compareTo(IRequestItems value2) {
		return 0;
	}

	@Override
	public void registerExtras(IPromise promise) {
		ItemIdentifierStack stack = new ItemIdentifierStack(promise.getItemType(), promise.getAmount());
		_extras.add(new LogisticsItemOrder(stack, null, ResourceType.EXTRA, null));

	}

	@Override
	public ItemCraftingTemplate addCrafting(IResource toCraft) {

		List<ItemIdentifierStack> stack = getCraftedItems();
		if (stack == null) {
			return null;
		}
		boolean found = false;
		ItemIdentifierStack craftingStack = null;
		for (ItemIdentifierStack craftable : stack) {
			craftingStack = craftable;
			if (toCraft.matches(craftingStack.getItem())) {
				found = true;
				break;
			}

		}
		if (found == false) {
			return null;
		}

		IRequestItems[] target = new IRequestItems[9];
		for (int i = 0; i < 9; i++) {
			target[i] = this;
		}

		boolean hasSatellite = isSatelliteConnected();
		if (!hasSatellite) {
			return null;
		}
		if (!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if (satelliteId != 0) {
				IRouter r = getSatelliteRouter(-1);
				if (r != null) {
					IRequestItems sat = r.getPipe();
					for (int i = 6; i < 9; i++) {
						target[i] = sat;
					}
				}
			}
		} else {
			for (int i = 0; i < 9; i++) {
				if (advancedSatelliteIdArray[i] != 0) {
					IRouter r = getSatelliteRouter(i);
					if (r != null) {
						target[i] = r.getPipe();
					}
				}
			}
		}

		ItemCraftingTemplate template = new ItemCraftingTemplate(craftingStack, this, priority);

		//Check all materials
		for (int i = 0; i < 9; i++) {
			ItemIdentifierStack resourceStack = getMaterials(i);
			if (resourceStack == null || resourceStack.getStackSize() == 0) {
				continue;
			}
			IResource req = null;
			if (getUpgradeManager().isFuzzyUpgrade() && fuzzyCraftingFlagArray[i] != 0) {
				DictResource dict;
				req = dict = new DictResource(resourceStack, target[i]);
				if ((fuzzyCraftingFlagArray[i] & 0x1) != 0) {
					dict.use_od = true;
				}
				if ((fuzzyCraftingFlagArray[i] & 0x2) != 0) {
					dict.ignore_dmg = true;
				}
				if ((fuzzyCraftingFlagArray[i] & 0x4) != 0) {
					dict.ignore_nbt = true;
				}
				if ((fuzzyCraftingFlagArray[i] & 0x8) != 0) {
					dict.use_category = true;
				}
			} else {
				req = new ItemResource(resourceStack, target[i]);
			}
			template.addRequirement(req, new CraftingChassieInformation(i, getPositionInt()));
		}

		int liquidCrafter = getUpgradeManager().getFluidCrafter();
		IRequestFluid[] liquidTarget = new IRequestFluid[liquidCrafter];

		if (!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if (liquidSatelliteId != 0) {
				IRouter r = getFluidSatelliteRouter(-1);
				if (r != null) {
					IRequestFluid sat = (IRequestFluid) r.getPipe();
					for (int i = 0; i < liquidCrafter; i++) {
						liquidTarget[i] = sat;
					}
				}
			}
		} else {
			for (int i = 0; i < liquidCrafter; i++) {
				if (liquidSatelliteIdArray[i] != 0) {
					IRouter r = getFluidSatelliteRouter(i);
					if (r != null) {
						liquidTarget[i] = (IRequestFluid) r.getPipe();
					}
				}
			}
		}

		for (int i = 0; i < liquidCrafter; i++) {
			FluidIdentifier liquid = getFluidMaterial(i);
			int amount = getFluidAmount()[i];
			if (liquid == null || amount <= 0 || liquidTarget[i] == null) {
				continue;
			}
			template.addRequirement(new FluidResource(liquid, amount, liquidTarget[i]), null);
		}

		if (getUpgradeManager().hasByproductExtractor() && getByproductItem() != null) {
			template.addByproduct(getByproductItem());
		}

		return template;
	}

	protected ISlotUpgradeManager getUpgradeManager() {
		if (_service == null) {
			return null;
		}
		return _service.getUpgradeManager(slot, positionInt);
	}

	public boolean isSatelliteConnected() {
		final List<ExitRoute> routes = getRouter().getIRoutersByCost();
		if (!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if (satelliteId == 0) {
				return true;
			}
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if (satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null) {
						continue;
					}
					IRouter satRouter = satPipe.getRouter();
					for (ExitRoute route : routes) {
						if (route.destination == satRouter) {
							return true;
						}
					}
				}
			}
		} else {
			boolean foundAll = true;
			for (int i = 0; i < 9; i++) {
				boolean foundOne = false;
				if (advancedSatelliteIdArray[i] == 0) {
					continue;
				}
				for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
					if (satellite.satelliteId == advancedSatelliteIdArray[i]) {
						CoreRoutedPipe satPipe = satellite;
						if (satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null) {
							continue;
						}
						IRouter satRouter = satPipe.getRouter();
						for (ExitRoute route : routes) {
							if (route.destination == satRouter) {
								foundOne = true;
								break;
							}
						}
					}
				}
				foundAll &= foundOne;
			}
			return foundAll;
		}
		//TODO check for FluidCrafter
		return false;
	}

	@Override
	public boolean canCraft(IResource toCraft) {
		if (getCraftedItem() == null) {
			return false;
		}
		if (toCraft instanceof ItemResource || toCraft instanceof DictResource) {
			return toCraft.matches(getCraftedItem().getItem());
		}
		return false;
	}

	@Override
	public List<ItemIdentifierStack> getCraftedItems() {
		List<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>(1);
		if (getCraftedItem() != null) {
			list.add(getCraftedItem());
		}
		return list;
	}

	public ItemIdentifierStack getCraftedItem() {
		return _dummyInventory.getIDStackInSlot(9);
	}

	@Override
	public int getTodo() {
		return _service.getItemOrderManager().totalAmountCountInAllOrders();
	}

	protected int getNextConnectSatelliteId(boolean prev, int x) {
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
			CoreRoutedPipe satPipe = satellite;
			if (satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null || satPipe.isFluidPipe()) {
				continue;
			}
			IRouter satRouter = satPipe.getRouter();
			List<ExitRoute> routes = getRouter().getDistanceTo(satRouter);
			if (routes != null && !routes.isEmpty()) {
				boolean filterFree = false;
				for (ExitRoute route : routes) {
					if (route.filters.isEmpty()) {
						filterFree = true;
						break;
					}
				}
				if (!filterFree) {
					continue;
				}
				if (x == -1) {
					if (!prev && satellite.satelliteId > satelliteId && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < satelliteId && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				} else {
					if (!prev && satellite.satelliteId > advancedSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < advancedSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if (x == -1) {
				return satelliteId;
			} else {
				return advancedSatelliteIdArray[x];
			}
		}
		return closestIdFound;
	}

	protected int getNextConnectFluidSatelliteId(boolean prev, int x) {
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
			CoreRoutedPipe satPipe = satellite;
			if (satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null || !satPipe.isFluidPipe()) {
				continue;
			}
			IRouter satRouter = satPipe.getRouter();
			List<ExitRoute> routes = getRouter().getDistanceTo(satRouter);
			if (routes != null && !routes.isEmpty()) {
				boolean filterFree = false;
				for (ExitRoute route : routes) {
					if (route.filters.isEmpty()) {
						filterFree = true;
						break;
					}
				}
				if (!filterFree) {
					continue;
				}
				if (x == -1) {
					if (!prev && satellite.satelliteId > liquidSatelliteId && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < liquidSatelliteId && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				} else {
					if (!prev && satellite.satelliteId > liquidSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < liquidSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if (x == -1) {
				return liquidSatelliteId;
			} else {
				return liquidSatelliteIdArray[x];
			}
		}
		return closestIdFound;
	}

	public void setNextSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeNextSatellite.class).setModulePos(this);
			MainProxy.sendPacketToServer(packet);
		} else {
			satelliteId = getNextConnectSatelliteId(false, -1);
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteId.class).setPipeId(satelliteId).setModulePos(this);
			MainProxy.sendPacketToPlayer(packet, player);
		}

	}

	// This is called by the packet PacketCraftingPipeSatelliteId
	public void setSatelliteId(int satelliteId, int x) {
		if (x == -1) {
			this.satelliteId = satelliteId;
		} else {
			advancedSatelliteIdArray[x] = satelliteId;
		}
	}

	public void setPrevSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipePrevSatellite.class).setModulePos(this);
			MainProxy.sendPacketToServer(packet);
		} else {
			satelliteId = getNextConnectSatelliteId(true, -1);
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteId.class).setPipeId(satelliteId).setModulePos(this);
			MainProxy.sendPacketToPlayer(packet, player);
		}
	}

	public IRouter getSatelliteRouter(int x) {
		if (x == -1) {
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if (satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null) {
						continue;
					}
					return satPipe.getRouter();
				}
			}
		} else {
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == advancedSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite;
					if (satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null) {
						continue;
					}
					return satPipe.getRouter();
				}
			}
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		//		super.readFromNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
		_liquidInventory.readFromNBT(nbttagcompound, "FluidInv");
		_cleanupInventory.readFromNBT(nbttagcompound, "CleanupInv");
		satelliteId = nbttagcompound.getInteger("satelliteid");

		priority = nbttagcompound.getInteger("priority");
		for (int i = 0; i < 9; i++) {
			advancedSatelliteIdArray[i] = nbttagcompound.getInteger("advancedSatelliteId" + i);
		}
		for (int i = 0; i < 9; i++) {
			fuzzyCraftingFlagArray[i] = nbttagcompound.getByte("fuzzyCraftingFlag" + i);
		}
		for (int i = 0; i < 6; i++) {
			craftingSigns[i] = nbttagcompound.getBoolean("craftingSigns" + i);
		}
		if (nbttagcompound.hasKey("FluidAmount")) {
			amount = nbttagcompound.getIntArray("FluidAmount");
		}
		if (amount.length < ItemUpgrade.MAX_LIQUID_CRAFTER) {
			amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
		}
		for (int i = 0; i < ItemUpgrade.MAX_LIQUID_CRAFTER; i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		for (int i = 0; i < ItemUpgrade.MAX_LIQUID_CRAFTER; i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		liquidSatelliteId = nbttagcompound.getInteger("liquidSatelliteId");
		cleanupModeIsExclude = nbttagcompound.getBoolean("cleanupModeIsExclude");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		//	super.writeToNBT(nbttagcompound);
		_dummyInventory.writeToNBT(nbttagcompound, "");
		_liquidInventory.writeToNBT(nbttagcompound, "FluidInv");
		_cleanupInventory.writeToNBT(nbttagcompound, "CleanupInv");
		nbttagcompound.setInteger("satelliteid", satelliteId);

		nbttagcompound.setInteger("priority", priority);
		for (int i = 0; i < 9; i++) {
			nbttagcompound.setInteger("advancedSatelliteId" + i, advancedSatelliteIdArray[i]);
		}
		for (int i = 0; i < 9; i++) {
			nbttagcompound.setByte("fuzzyCraftingFlag" + i, (byte) fuzzyCraftingFlagArray[i]);
		}
		for (int i = 0; i < 6; i++) {
			nbttagcompound.setBoolean("craftingSigns" + i, craftingSigns[i]);
		}
		for (int i = 0; i < ItemUpgrade.MAX_LIQUID_CRAFTER; i++) {
			nbttagcompound.setInteger("liquidSatelliteIdArray" + i, liquidSatelliteIdArray[i]);
		}
		nbttagcompound.setIntArray("FluidAmount", amount);
		nbttagcompound.setInteger("liquidSatelliteId", liquidSatelliteId);
		nbttagcompound.setBoolean("cleanupModeIsExclude", cleanupModeIsExclude);
	}

	public ModernPacket getCPipePacket() {
		return PacketHandler.getPacket(CraftingPipeUpdatePacket.class).setAmount(amount).setLiquidSatelliteIdArray(liquidSatelliteIdArray).setLiquidSatelliteId(liquidSatelliteId).setSatelliteId(satelliteId).setAdvancedSatelliteIdArray(advancedSatelliteIdArray).setFuzzyCraftingFlagArray(fuzzyCraftingFlagArray)
				.setPriority(priority).setModulePos(this);
	}

	public void handleCraftingUpdatePacket(CraftingPipeUpdatePacket packet) {
		amount = packet.getAmount();
		liquidSatelliteIdArray = packet.getLiquidSatelliteIdArray();
		liquidSatelliteId = packet.getLiquidSatelliteId();
		satelliteId = packet.getSatelliteId();
		advancedSatelliteIdArray = packet.getAdvancedSatelliteIdArray();
		fuzzyCraftingFlagArray = packet.getFuzzyCraftingFlagArray();
		priority = packet.getPriority();
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(CraftingModuleSlot.class).setAdvancedSat(getUpgradeManager().isAdvancedSatelliteCrafter()).setLiquidCrafter(getUpgradeManager().getFluidCrafter()).setAmount(amount).setHasByproductExtractor(getUpgradeManager().hasByproductExtractor()).setFuzzy(getUpgradeManager().isFuzzyUpgrade())
				.setCleanupSize(getUpgradeManager().getCrafterCleanup()).setCleanupExclude(cleanupModeIsExclude);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(CraftingModuleInHand.class).setAmount(amount).setCleanupExclude(cleanupModeIsExclude);
	}

	@Override
	public BlockPos getblockpos() {
		return null;
	}

	/**
	 * Simply get the dummy inventory
	 * 
	 * @return the dummy inventory
	 */
	public ItemIdentifierInventory getDummyInventory() {
		return _dummyInventory;
	}

	public ItemIdentifierInventory getFluidInventory() {
		return _liquidInventory;
	}

	public IInventory getCleanupInventory() {
		return _cleanupInventory;
	}

	public void setDummyInventorySlot(int slot, ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}

	public void importFromCraftingTable(EntityPlayer player) {
		if (MainProxy.isClient(getWorld())) {
			// Send packet asking for import
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImport.class).setModulePos(this);
			MainProxy.sendPacketToServer(packet);
		} else {
			boolean fuzzyFlagsChanged = false;
			final WorldUtil worldUtil = new WorldUtil(getWorld(), getblockpos());
			for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
				for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
					if (provider.importRecipe(tile.tile, _dummyInventory)) {
						if (provider instanceof IFuzzyRecipeProvider) {
							fuzzyFlagsChanged = ((IFuzzyRecipeProvider) provider).importFuzzyFlags(tile.tile, _dummyInventory, fuzzyCraftingFlagArray);
						}
						break;
					}
				}
			}
			// Send inventory as packet
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(_dummyInventory).setModulePos(this);
			if (player != null) {
				MainProxy.sendPacketToPlayer(packet, player);
			}
			MainProxy.sendPacketToAllWatchingChunk(getblockpos().getX(), getblockpos().getZ(), MainProxy.getDimensionForWorld(getWorld()), packet);

			if (fuzzyFlagsChanged && getUpgradeManager().isFuzzyUpgrade()) {
				for (int i = 0; i < 9; i++) {
					final ModernPacket pak = PacketHandler.getPacket(CraftingFuzzyFlag.class).setInteger2(fuzzyCraftingFlagArray[i]).setInteger(i).setModulePos(this);
					if (player != null) {
						MainProxy.sendPacketToPlayer(pak, player);
					}
					MainProxy.sendPacketToAllWatchingChunk(getblockpos().getX(), getblockpos().getZ(), MainProxy.getDimensionForWorld(getWorld()), pak);
				}
			}
		}
	}

	protected World getWorld() {
		return _world.getWorld();
	}

	public void priorityUp(EntityPlayer player) {
		priority++;
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePriorityUpPacket.class).setModulePos(this));
		} else if (player != null && MainProxy.isServer(player.worldObj)) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPriority.class).setInteger(priority).setModulePos(this), player);
		}
	}

	public void priorityDown(EntityPlayer player) {
		priority--;
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePriorityDownPacket.class).setModulePos(this));
		} else if (player != null && MainProxy.isServer(player.worldObj)) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPriority.class).setInteger(priority).setModulePos(this), player);
		}
	}

	public void setPriority(int amount) {
		priority = amount;
	}

	public ItemIdentifierStack getByproductItem() {
		return _dummyInventory.getIDStackInSlot(10);
	}

	public ItemIdentifierStack getMaterials(int slotnr) {
		return _dummyInventory.getIDStackInSlot(slotnr);
	}

	public FluidIdentifier getFluidMaterial(int slotnr) {
		ItemIdentifierStack stack = _liquidInventory.getIDStackInSlot(slotnr);
		if (stack == null) {
			return null;
		}
		return FluidIdentifier.get(stack.getItem());
	}

	public void setNextSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeNextAdvancedSatellitePacket.class).setInteger(i).setModulePos(this));
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(false, i);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(advancedSatelliteIdArray[i]).setModulePos(this), player);
		}
	}

	public void setPrevSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePrevAdvancedSatellitePacket.class).setInteger(i).setModulePos(this));
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(true, i);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(advancedSatelliteIdArray[i]).setModulePos(this), player);
		}
	}

	public void changeFluidAmount(int change, int slot, EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(change).setModulePos(this));
		} else {
			amount[slot] += change;
			if (amount[slot] <= 0) {
				amount[slot] = 0;
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(amount[slot]).setModulePos(this), player);
		}
	}

	public void setPrevFluidSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingPipeAdvancedSatellitePrevPacket.class).setInteger(i).setModulePos(this));
		} else {
			if (i == -1) {
				liquidSatelliteId = getNextConnectFluidSatelliteId(true, i);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteId).setModulePos(this), player);
			} else {
				liquidSatelliteIdArray[i] = getNextConnectFluidSatelliteId(true, i);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteIdArray[i]).setModulePos(this), player);
			}
		}
	}

	public void setNextFluidSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingPipeAdvancedSatelliteNextPacket.class).setInteger(i).setModulePos(this));
		} else {
			if (i == -1) {
				liquidSatelliteId = getNextConnectFluidSatelliteId(false, i);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteId).setModulePos(this), player);
			} else {
				liquidSatelliteIdArray[i] = getNextConnectFluidSatelliteId(false, i);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteIdArray[i]).setModulePos(this), player);
			}
		}
	}

	public void setFluidAmount(int[] amount) {
		if (MainProxy.isClient(getWorld())) {
			this.amount = amount;
		}
	}

	public void defineFluidAmount(int integer, int slot) {
		if (MainProxy.isClient(getWorld())) {
			amount[slot] = integer;
		}
	}

	public int[] getFluidAmount() {
		return amount;
	}

	public void setFluidSatelliteId(int integer, int slot) {
		if (slot == -1) {
			liquidSatelliteId = integer;
		} else {
			liquidSatelliteIdArray[slot] = integer;
		}
	}

	public IRouter getFluidSatelliteRouter(int x) {
		if (x == -1) {
			for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
				if (satellite.satelliteId == liquidSatelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if (satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null) {
						continue;
					}
					return satPipe.getRouter();
				}
			}
		} else {
			for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
				if (satellite.satelliteId == liquidSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite;
					if (satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null) {
						continue;
					}
					return satPipe.getRouter();
				}
			}
		}
		return null;
	}

	public void setFuzzyCraftingFlag(int slot, int flag, EntityPlayer player) {
		if (slot < 0 || slot >= 9) {
			return;
		}
		if (MainProxy.isClient(getWorld())) {
			if (player == null) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingFuzzyFlag.class).setInteger2(flag).setInteger(slot).setModulePos(this));
			} else {
				fuzzyCraftingFlagArray[slot] = flag;
			}
		} else {
			fuzzyCraftingFlagArray[slot] ^= 1 << flag;
			ModernPacket pak = PacketHandler.getPacket(CraftingFuzzyFlag.class).setInteger2(fuzzyCraftingFlagArray[slot]).setInteger(slot).setModulePos(this);
			if (player != null) {
				MainProxy.sendPacketToPlayer(pak, player);
			}
			MainProxy.sendPacketToAllWatchingChunk(getblockpos().getX(), getblockpos().getZ(), MainProxy.getDimensionForWorld(getWorld()), pak);
		}
	}

	public void openAttachedGui(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP) player).closeScreen();
			} else if (player instanceof EntityPlayerSP) {
				((EntityPlayerSP) player).closeScreen();
			}
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeOpenConnectedGuiPacket.class).setModulePos(this));
			return;
		}

		// hack to avoid wrenching blocks
		int savedEquipped = player.inventory.currentItem;
		boolean foundSlot = false;
		// try to find a empty slot
		for (int i = 0; i < 9; i++) {
			if (player.inventory.getStackInSlot(i) == null) {
				foundSlot = true;
				player.inventory.currentItem = i;
				break;
			}
		}
		// okay, anything that's a block?
		if (!foundSlot) {
			for (int i = 0; i < 9; i++) {
				ItemStack is = player.inventory.getStackInSlot(i);
				if (is.getItem() instanceof ItemBlock) {
					foundSlot = true;
					player.inventory.currentItem = i;
					break;
				}
			}
		}
		// give up and select whatever is right of the current slot
		if (!foundSlot) {
			player.inventory.currentItem = (player.inventory.currentItem + 1) % 9;
		}

		final WorldUtil worldUtil = new WorldUtil(getWorld(), getblockpos());
		boolean found = false;
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
				if (provider.canOpenGui(tile.tile)) {
					found = true;
					break;
				}
			}

			if (!found) {
				found = (tile.tile instanceof IInventory);
			}

			if (found) {
				Block block = utilWorld.getBlock(tile.tile.getPos(),tile.tile.getWorld());
				if (block != null) {
					if (block.onBlockActivated(getWorld(), tile.tile.getPos(),tile.tile, player, 0, 0, 0, 0)) {
						break;
					}
				}
			}
		}
		player.inventory.currentItem = savedEquipped;
	}

	public void enabledUpdateEntity() {
		if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
			if (_service.isNthTick(6)) {
				cacheAreAllOrderesToBuffer();
			}
			if (_service.getItemOrderManager().isFirstOrderWatched()) {
				TileEntity tile = lastAccessedCrafter.get();
				if (tile != null) {
					_service.getItemOrderManager().setMachineProgress(SimpleServiceLocator.machineProgressProvider.getProgressForTile(tile));
				} else {
					_service.getItemOrderManager().setMachineProgress((byte) 0);
				}
			}
		} else {
			cachedAreAllOrderesToBuffer = false;
		}

		if (!_service.isNthTick(6)) {
			return;
		}

		waitingForCraft = false;

		if ((!_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING) && _extras.isEmpty())) {
			if (getUpgradeManager().getCrafterCleanup() > 0) {
				List<AdjacentTile> crafters = locateCrafters();
				ItemStack extracted = null;
				AdjacentTile tile = null;
				for (Iterator<AdjacentTile> it = crafters.iterator(); it.hasNext();) {
					tile = it.next();
					extracted = extractFiltered(tile, _cleanupInventory, cleanupModeIsExclude, getUpgradeManager().getCrafterCleanup() * 3);
					if (extracted != null && extracted.stackSize > 0) {
						break;
					}
				}
				if (extracted != null && extracted.stackSize > 0) {
					_service.queueRoutedItem(SimpleServiceLocator.routedItemHelper.createNewTravelItem(extracted), EnumFacing.UP);
					_service.getCacheHolder().trigger(CacheTypes.Inventory);
				}
			}
			return;
		}

		waitingForCraft = true;

		List<AdjacentTile> crafters = locateCrafters();
		if (crafters.size() < 1) {
			if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
				_service.getItemOrderManager().sendFailed();
			} else {
				_extras.clear();
			}
			return;
		}

		List<ItemIdentifierStack> wanteditem = getCraftedItems();
		if (wanteditem == null || wanteditem.isEmpty()) {
			return;
		}

		_service.spawnParticle(Particles.VioletParticle, 2);

		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		while (itemsleft > 0 && stacksleft > 0 && (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING) || !_extras.isEmpty())) {
			LogisticsItemOrder nextOrder;
			boolean processingOrder = false;
			if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
				nextOrder = _service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING); // fetch but not remove.
				processingOrder = true;
			} else {
				nextOrder = _extras.getFirst(); // fetch but not remove.
			}
			int maxtosend = Math.min(itemsleft, nextOrder.getItemStack().getStackSize());
			maxtosend = Math.min(nextOrder.getItemStack().getItem().getMaxStackSize(), maxtosend);
			// retrieve the new crafted items
			ItemStack extracted = null;
			AdjacentTile tile = null;
			for (Iterator<AdjacentTile> it = crafters.iterator(); it.hasNext();) {
				tile = it.next();
				extracted = extract(tile, nextOrder.getItemStack().getItem(), maxtosend);
				if (extracted != null && extracted.stackSize > 0) {
					break;
				}
			}
			if (extracted == null || extracted.stackSize == 0) {
				if (processingOrder) {
					_service.getItemOrderManager().deferSend();
				}
				break;
			}
			_service.getCacheHolder().trigger(CacheTypes.Inventory);
			lastAccessedCrafter = new WeakReference<TileEntity>(tile.tile);
			// send the new crafted items to the destination
			ItemIdentifier extractedID = ItemIdentifier.get(extracted);
			while (extracted.stackSize > 0) {
				if (!nextOrder.getItemStack().getItem().equals(extractedID)) {
					LogisticsItemOrder startOrder = nextOrder;
					if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
						do {
							_service.getItemOrderManager().deferSend();
							nextOrder = _service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING);
						} while (!nextOrder.getItemStack().getItem().equals(extractedID) && startOrder != nextOrder);
					}
					if (startOrder == nextOrder) {
						int numtosend = Math.min(extracted.stackSize, extractedID.getMaxStackSize());
						if (numtosend == 0) {
							break;
						}
						stacksleft -= 1;
						itemsleft -= numtosend;
						ItemStack stackToSend = extracted.splitStack(numtosend);
						//Route the unhandled item

						_service.sendStack(stackToSend, -1, ItemSendMode.Normal, null);
						continue;
					}
				}
				int numtosend = Math.min(extracted.stackSize, extractedID.getMaxStackSize());
				numtosend = Math.min(numtosend, nextOrder.getItemStack().getStackSize());
				if (numtosend == 0) {
					break;
				}
				stacksleft -= 1;
				itemsleft -= numtosend;
				ItemStack stackToSend = extracted.splitStack(numtosend);
				if (processingOrder) {
					SinkReply reply = LogisticsManager.canSink(nextOrder.getDestination().getRouter(), null, true, ItemIdentifier.get(stackToSend), null, true, false);
					boolean defersend = false;
					if (reply == null || reply.bufferMode != BufferMode.NONE || reply.maxNumberOfItems < 1) {
						defersend = true;
					}
					IRoutedItem item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stackToSend);
					item.setDestination(nextOrder.getDestination().getRouter().getSimpleID());
					item.setTransportMode(TransportMode.Active);
					item.setAdditionalTargetInformation(nextOrder.getInformation());
					_service.queueRoutedItem(item, tile.orientation);
					_service.getItemOrderManager().sendSuccessfull(stackToSend.stackSize, defersend, item);
					if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
						nextOrder = _service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING); // fetch but not remove.
					} else {
						processingOrder = false;
						if (!_extras.isEmpty()) {
							nextOrder = _extras.getFirst();
						}
					}
				} else {
					removeExtras(numtosend, nextOrder.getItemStack().getItem());
					_service.sendStack(stackToSend, -1, ItemSendMode.Normal, nextOrder.getInformation());
				}
			}
		}

	}

	private boolean cachedAreAllOrderesToBuffer;

	public boolean areAllOrderesToBuffer() {
		return cachedAreAllOrderesToBuffer;
	}

	public void cacheAreAllOrderesToBuffer() {
		boolean result = true;
		for (LogisticsItemOrder order : _service.getItemOrderManager()) {
			if (order.getDestination() instanceof IItemSpaceControl) {
				SinkReply reply = LogisticsManager.canSink(order.getDestination().getRouter(), null, true, order.getItemStack().getItem(), null, true, false);
				if (reply != null && reply.bufferMode == BufferMode.NONE && reply.maxNumberOfItems >= 1) {
					result = false;
					break;
				}
			} else { // No Space control
				result = false;
				break;
			}
		}
		cachedAreAllOrderesToBuffer = result;
	}

	private void removeExtras(int numToSend, ItemIdentifier item) {
		Iterator<LogisticsItemOrder> i = _extras.iterator();
		while (i.hasNext()) {
			ItemIdentifierStack e = i.next().getItemStack();
			if (e.getItem().equals(item)) {
				if (numToSend >= e.getStackSize()) {
					numToSend -= e.getStackSize();
					i.remove();
					if (numToSend == 0) {
						return;
					}
				} else {
					e.setStackSize(e.getStackSize() - numToSend);
					break;
				}
			}
		}
	}

	private ItemStack extract(AdjacentTile tile, ItemIdentifier item, int amount) {
		if (tile.tile instanceof LogisticsCraftingTableTileEntity) {
			return extractFromLogisticsCraftingTable((LogisticsCraftingTableTileEntity) tile.tile, item, amount, tile.orientation);
		} else if (tile.tile instanceof net.minecraft.inventory.ISidedInventory) {
			IInventory sidedadapter = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) tile.tile, UtilEnumFacing.UNKNOWN, true, utilEnumFacing);
			return extractFromIInventory(sidedadapter, item, amount, tile.orientation);
		} else if (tile.tile instanceof IInventory) {
			return extractFromIInventory((IInventory) tile.tile, item, amount, tile.orientation);
		}
		return null;
	}

	private ItemStack extractFiltered(AdjacentTile tile, ItemIdentifierInventory inv, boolean isExcluded, int filterInvLimit) {
		if (tile.tile instanceof net.minecraft.inventory.ISidedInventory) {
			IInventory sidedadapter = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) tile.tile, UtilEnumFacing.UNKNOWN, true, utilEnumFacing);
			return extractFromIInventoryFiltered(sidedadapter, inv, isExcluded, filterInvLimit, tile.orientation);
		} else if (tile.tile instanceof IInventory) {
			return extractFromIInventoryFiltered((IInventory) tile.tile, inv, isExcluded, filterInvLimit, tile.orientation);
		}
		return null;
	}

	private ItemStack extractFromIInventory(IInventory inv, ItemIdentifier wanteditem, int count, EnumFacing dir) {
		IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv, dir);
		int available = invUtil.itemCount(wanteditem);
		if (available == 0) {
			return null;
		}
		if (!_service.useEnergy(neededEnergy() * Math.min(count, available))) {
			return null;
		}
		return invUtil.getMultipleItems(wanteditem, Math.min(count, available));
	}

	private ItemStack extractFromIInventoryFiltered(IInventory inv, ItemIdentifierInventory filter, boolean isExcluded, int filterInvLimit, EnumFacing dir) {
		IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv, dir);
		ItemIdentifier wanteditem = null;
		for (ItemIdentifier item : invUtil.getItemsAndCount().keySet()) {
			if (isExcluded) {
				boolean found = false;
				for (int i = 0; i < filter.getSizeInventory() && i < filterInvLimit; i++) {
					ItemIdentifierStack identStack = filter.getIDStackInSlot(i);
					if (identStack == null) {
						continue;
					}
					if (identStack.getItem().equalsWithoutNBT(item)) {
						found = true;
						break;
					}
				}
				if (!found) {
					wanteditem = item;
				}
			} else {
				boolean found = false;
				for (int i = 0; i < filter.getSizeInventory() && i < filterInvLimit; i++) {
					ItemIdentifierStack identStack = filter.getIDStackInSlot(i);
					if (identStack == null) {
						continue;
					}
					if (identStack.getItem().equalsWithoutNBT(item)) {
						found = true;
						break;
					}
				}
				if (found) {
					wanteditem = item;
				}
			}
		}
		if (wanteditem == null) {
			return null;
		}
		int available = invUtil.itemCount(wanteditem);
		if (available == 0) {
			return null;
		}
		if (!_service.useEnergy(neededEnergy() * Math.min(64, available))) {
			return null;
		}
		return invUtil.getMultipleItems(wanteditem, Math.min(64, available));
	}

	private ItemStack extractFromLogisticsCraftingTable(LogisticsCraftingTableTileEntity tile, ItemIdentifier wanteditem, int count, EnumFacing dir) {
		ItemStack extracted = extractFromIInventory(tile, wanteditem, count, dir);
		if (extracted != null) {
			return extracted;
		}
		ItemStack retstack = null;
		while (count > 0) {
			ItemStack stack = tile.getOutput(wanteditem, _service);
			if (stack == null || stack.stackSize == 0) {
				break;
			}
			if (retstack == null) {
				if (!wanteditem.equalsWithoutNBT(stack)) {
					break;
				}
			} else {
				if (!retstack.isItemEqual(stack)) {
					break;
				}
				if (!ItemStack.areItemStackTagsEqual(retstack, stack)) {
					break;
				}
			}
			if (!_service.useEnergy(neededEnergy() * stack.stackSize)) {
				break;
			}

			if (retstack == null) {
				retstack = stack;
			} else {
				retstack.stackSize += stack.stackSize;
			}
			count -= stack.stackSize;
		}
		return retstack;
	}

	protected int neededEnergy() {
		return 10;
	}

	protected int itemsToExtract() {
		return 1;
	}

	protected int stacksToExtract() {
		return 1;
	}

	private List<AdjacentTile> _cachedCrafters = null;

	public List<AdjacentTile> locateCrafters() {
		if (_cachedCrafters != null) {
			return _cachedCrafters;
		}
		WorldUtil worldUtil = new WorldUtil(getWorld(), getblockpos());
		LinkedList<AdjacentTile> crafters = new LinkedList<AdjacentTile>();
		for (AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			if (!(tile.tile instanceof IInventory)) {
				continue;
			}
			crafters.add(tile);
		}
		_cachedCrafters = crafters;
		return _cachedCrafters;
	}

	public void clearCraftersCache() {
		_cachedCrafters = null;
	}

	@Override
	public void clearCache() {
		clearCraftersCache();
	}

	public void importCleanup() {
		for (int i = 0; i < 10; i++) {
			_cleanupInventory.setInventorySlotContents(i, _dummyInventory.getStackInSlot(i));
		}
		for (int i = 10; i < _cleanupInventory.getSizeInventory(); i++) {
			_cleanupInventory.setInventorySlotContents(i, (ItemStack) null);
		}
		_cleanupInventory.compact_first(10);
		_cleanupInventory.recheckStackLimit();
		cleanupModeIsExclude = false;
	}

	public void toogleCleaupMode() {
		cleanupModeIsExclude = !cleanupModeIsExclude;
	}

	@Override
	public void startHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void stopHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class CraftingChassieInformation extends ChassiTargetInformation {

		@Getter
		private final int craftingSlot;

		public CraftingChassieInformation(int craftingSlot, int moduleSlot) {
			super(moduleSlot);
			this.craftingSlot = craftingSlot;
		}
	}
}
