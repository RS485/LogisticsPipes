package logisticspipes.modules;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.interfaces.IGuiOpenController;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.WrappedInventory;
import logisticspipes.interfaces.routing.FluidRequester;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.ItemCrafter;
import logisticspipes.interfaces.routing.ItemRequester;
import logisticspipes.interfaces.routing.ItemSpaceControl;
import logisticspipes.logistics.LogisticsManagerImpl;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.module.inhand.CraftingModuleInHand;
import logisticspipes.network.guis.module.inpipe.CraftingModuleSlot;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImport;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.network.packets.cpipe.CraftingPipeOpenConnectedGuiPacket;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.pipe.CraftingPipePriorityDownPacket;
import logisticspipes.network.packets.pipe.CraftingPipePriorityUpPacket;
import logisticspipes.network.packets.pipe.CraftingPipeUpdatePacket;
import logisticspipes.network.packets.pipe.CraftingPriority;
import logisticspipes.network.packets.pipe.FluidCraftingAmount;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.CraftingRecipeProvider;
import logisticspipes.proxy.interfaces.FuzzyRecipeProvider;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.DictCraftingTemplate;
import logisticspipes.request.ItemCraftingTemplate;
import logisticspipes.request.Promise;
import logisticspipes.request.ReqCraftingTemplate;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.request.resources.FluidResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.request.resources.Resource;
import logisticspipes.request.resources.Resource.Dict;
import logisticspipes.routing.LogisticsDictPromise;
import logisticspipes.routing.LogisticsExtraDictPromise;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.Router;
import logisticspipes.routing.RouterManager;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LogisticsItemOrder;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.DelayedGeneric;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.InventoryUtilFactory;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.RoutedItemHelper;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.BufferMode;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.logisticspipes.connection.NeighborBlockEntity;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class ModuleCrafter extends LogisticsGuiModule implements ItemCrafter, IHUDModuleHandler, IModuleWatchReciver, IGuiOpenController {

	public static final int MAX_LIQUID_CRAFTER = 3;
	public static final int MAX_CRAFTING_CLEANUP = 4;

	// for reliable transport
	protected final DelayQueue<DelayedGeneric<Tuple2<ItemStack, IAdditionalTargetInformation>>> _lostItems = new DelayQueue<>();
	protected final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	protected final PlayerCollectionList guiWatcher = new PlayerCollectionList();

	public UUID satelliteUUID = null;
	public UUID[] advancedSatelliteUUIDArray = new UUID[9];
	public UUID liquidSatelliteUUID = null;
	// Values
	public UUID[] liquidSatelliteUUIDArray = new UUID[MAX_LIQUID_CRAFTER];

	public Resource.Dict[] fuzzyCraftingFlagArray = new Resource.Dict[9];
	public Resource.Dict outputFuzzyFlags = new Resource.Dict(null, null);
	public int priority = 0;

	public boolean[] craftingSigns = new boolean[6];
	public boolean waitingForCraft = false;
	public boolean cleanupModeIsExclude = true;
	// from PipeItemsCraftingLogistics
	protected ItemIdentifierInventory _dummyInventory = new ItemIdentifierInventory(11, "Requested items", 127);
	protected ItemIdentifierInventory _liquidInventory = new ItemIdentifierInventory(MAX_LIQUID_CRAFTER, "Fluid items", 1, true);
	protected ItemIdentifierInventory _cleanupInventory = new ItemIdentifierInventory(MAX_CRAFTING_CLEANUP * 3, "Cleanup Filer Items", 1);
	protected int[] amount = new int[MAX_LIQUID_CRAFTER];
	protected SinkReply _sinkReply;
	private ItemRequester _invRequester;
	private WeakReference<BlockEntity> lastAccessedCrafter = new WeakReference<BlockEntity>(null);
	private boolean cachedAreAllOrderesToBuffer;
	private List<NeighborBlockEntity<BlockEntity>> cachedCrafters = null;

	public ClientSideSatelliteNames clientSideSatelliteNames = new ClientSideSatelliteNames();
	private UpgradeSatelliteFromIDs updateSatelliteFromIDs = null;

	public ModuleCrafter() {
		for (int i = 0; i < fuzzyCraftingFlagArray.length; i++) {
			fuzzyCraftingFlagArray[i] = new Resource.Dict(null, null);
		}
	}

	public ModuleCrafter(PipeItemsCraftingLogistics parent) {
		service = parent;
		_invRequester = parent;
		world = parent;
		registerPosition(ModulePositionType.IN_PIPE, 0);
		for (int i = 0; i < fuzzyCraftingFlagArray.length; i++) {
			fuzzyCraftingFlagArray[i] = new Resource.Dict(null, null);
		}
	}

	/**
	 * assumes that the invProvider is also IRequest items.
	 */
	@Override
	public void registerHandler(IWorldProvider world, IPipeServiceProvider service) {
		super.registerHandler(world, service);
		_invRequester = (ItemRequester) service;
	}

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		return new SinkReply(_sinkReply, spaceFor(item, includeInTransit), areAllOrderesToBuffer() ? BufferMode.DESTINATION_BUFFERED : BufferMode.NONE);
	}

	protected int spaceFor(ItemIdentifier item, boolean includeInTransit) {
		Tuple2<String, ItemIdentifier> key = new Tuple2<>("spaceFor", item);
		Object cache = service.getCacheHolder().getCacheFor(CacheTypes.Inventory, key);
		if (cache != null) {
			int count = (Integer) cache;
			if (includeInTransit) {
				count -= service.countOnRoute(item);
			}
			return count;
		}
		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(getWorld(), service.getPos());

		int count = worldCoordinates
				.connectedTileEntities(ConnectionPipeType.ITEM)
				.map(neighbor -> neighbor.sneakyInsertion().from(getUpgradeManager()))
				.filter(NeighborBlockEntity::isItemHandler)
				.map(NeighborBlockEntity::getUtilForItemHandler)
				.map(invUtil -> invUtil.roomForItem(item, 9999)) // ToDo: Magic number
				.reduce(Integer::sum).orElse(0);

		service.getCacheHolder().setCache(CacheTypes.Inventory, key, count);
		if (includeInTransit) {
			count -= service.countOnRoute(item);
		}
		return count;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int amount) {
		priority = amount;
	}

	public void onAllowedRemoval() {}

	private UUID getUUIDForSatelliteName(String name) {
		for (PipeItemsSatelliteLogistics pipe : PipeItemsSatelliteLogistics.AllSatellites) {
			if (pipe.getSatellitePipeName().equals(name)) {
				return pipe.getRouter().getId();
			}
		}
		return null;
	}

	private UUID getUUIDForFluidSatelliteName(String name) {
		for (PipeFluidSatellite pipe : PipeFluidSatellite.AllSatellites) {
			if (pipe.getSatellitePipeName().equals(name)) {
				return pipe.getRouter().getId();
			}
		}
		return null;
	}

	@Override
	public void tick() {
		enabledUpdateEntity();
		if (updateSatelliteFromIDs != null && service.isNthTick(100)) {
			if (updateSatelliteFromIDs.advancedSatelliteIdArray != null) {
				boolean canBeRemoved = true;
				for (int i = 0; i < updateSatelliteFromIDs.advancedSatelliteIdArray.length; i++) {
					if (updateSatelliteFromIDs.advancedSatelliteIdArray[i] != -1) {
						UUID uuid = getUUIDForSatelliteName(Integer.toString(updateSatelliteFromIDs.advancedSatelliteIdArray[i]));
						if (uuid != null) {
							updateSatelliteFromIDs.advancedSatelliteIdArray[i] = -1;
							advancedSatelliteUUIDArray[i] = uuid;
						} else {
							canBeRemoved = false;
						}
					}
				}
				if (canBeRemoved) {
					updateSatelliteFromIDs.advancedSatelliteIdArray = null;
				}
			}
			if (updateSatelliteFromIDs.liquidSatelliteIdArray != null) {
				boolean canBeRemoved = true;
				for (int i = 0; i < updateSatelliteFromIDs.liquidSatelliteIdArray.length; i++) {
					if (updateSatelliteFromIDs.liquidSatelliteIdArray[i] != -1) {
						UUID uuid = getUUIDForFluidSatelliteName(Integer.toString(updateSatelliteFromIDs.liquidSatelliteIdArray[i]));
						if (uuid != null) {
							updateSatelliteFromIDs.liquidSatelliteIdArray[i] = -1;
							liquidSatelliteUUIDArray[i] = uuid;
						} else {
							canBeRemoved = false;
						}
					}
				}
				if (canBeRemoved) {
					updateSatelliteFromIDs.liquidSatelliteIdArray = null;
				}
			}
			if (updateSatelliteFromIDs.liquidSatelliteId != -1) {
				UUID uuid = getUUIDForFluidSatelliteName(Integer.toString(updateSatelliteFromIDs.liquidSatelliteId));
				if (uuid != null) {
					updateSatelliteFromIDs.liquidSatelliteId = -1;
					liquidSatelliteUUID = uuid;
				}
			}
			if (updateSatelliteFromIDs.satelliteId != -1) {
				UUID uuid = getUUIDForFluidSatelliteName(Integer.toString(updateSatelliteFromIDs.satelliteId));
				if (uuid != null) {
					updateSatelliteFromIDs.satelliteId = -1;
					satelliteUUID = uuid;
				}
			}
			if (updateSatelliteFromIDs.advancedSatelliteIdArray == null
					&& updateSatelliteFromIDs.liquidSatelliteId == -1
					&& updateSatelliteFromIDs.liquidSatelliteIdArray == null
					&& updateSatelliteFromIDs.satelliteId == -1) {
				updateSatelliteFromIDs = null;
			}
		}
		if (_lostItems.isEmpty()) {
			return;
		}
		// if(true) return;
		DelayedGeneric<Tuple2<ItemStack, IAdditionalTargetInformation>> lostItem = _lostItems.poll();
		int rerequested = 0;
		while (lostItem != null && rerequested < 100) {
			Tuple2<ItemStack, IAdditionalTargetInformation> tuple = lostItem.get();
			if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
				SinkReply reply = LogisticsManagerImpl.canSink(getRouter(), null, true, tuple.getValue1().getItem(), null, true, true);
				if (reply == null || reply.maxNumberOfItems < 1) {
					_lostItems.add(new DelayedGeneric<>(tuple, 9000 + (int) (Math.random() * 2000)));
					lostItem = _lostItems.poll();
					continue;
				}
			}
			int received = RequestTree.requestPartial(tuple.getValue1(), (CoreRoutedPipe) service, tuple.getValue2());
			rerequested++;
			if (received < tuple.getValue1().getCount()) {
				tuple.getValue1().setStackSize(tuple.getValue1().getCount() - received);
				_lostItems.add(new DelayedGeneric<>(tuple, 4500 + (int) (Math.random() * 1000)));
			}
			lostItem = _lostItems.poll();
		}
	}

	@Override
	public void itemArrived(ItemStack item, IAdditionalTargetInformation info) {}

	@Override
	public void itemLost(ItemStack item, IAdditionalTargetInformation info) {
		_lostItems.add(new DelayedGeneric<>(new Tuple2<>(item, info), 5000));
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		List<ItemStack> result = getCraftedItems();
		if (result == null) {
			return null;
		}
		Set<ItemIdentifier> l1 = result.stream()
				.map(ItemStack::getItem)
				.collect(Collectors.toCollection(TreeSet::new));
		/*
		for(int i=0; i<9;i++) {
			ItemStack stack = getMaterials(i);
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
	public boolean receivePassive() {
		return false;
	}

	@Override
	public void tryProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filters) {
		if (!service.getItemOrderManager().hasExtras() || tree.hasBeenQueried(service.getItemOrderManager())) {
			return;
		}

		Resource requestedItem = tree.getRequestType();

		if (!canCraft(requestedItem)) {
			return;
		}

		for (IFilter filter : filters) {
			if (filter.isBlocked() == filter.isFilteredItem(requestedItem) || filter.blockProvider()) {
				return;
			}
		}
		int remaining = 0;
		for (LogisticsItemOrder extra : service.getItemOrderManager()) {
			if (extra.getType() == ResourceType.EXTRA) {
				if (extra.getResource().getItem().equals(requestedItem.getAsItem())) {
					remaining += extra.getResource().stack.getCount();
				}
			}
		}
		remaining -= root.getAllPromissesFor(this, getCraftedItem().getItem());
		if (remaining < 1) {
			return;
		}
		if (this.getUpgradeManager().isFuzzyUpgrade() && outputFuzzyFlags.getBitSet().nextSetBit(0) != -1) {
			Resource.Dict dict = new Resource.Dict(getCraftedItem(), null).loadFromBitSet(outputFuzzyFlags.getBitSet());
			LogisticsExtraDictPromise promise = new LogisticsExtraDictPromise(dict, Math.min(remaining, tree.getMissingAmount()), this, true);
			tree.addPromise(promise);
		} else {
			LogisticsExtraPromise promise = new LogisticsExtraPromise(getCraftedItem().getItem(), Math.min(remaining, tree.getMissingAmount()), this, true);
			tree.addPromise(promise);
		}
		tree.setQueried(service.getItemOrderManager());
	}

	@Override
	public LogisticsItemOrder fulfill(LogisticsPromise promise, ItemRequester destination, IAdditionalTargetInformation info) {
		if (promise instanceof LogisticsExtraDictPromise) {
			service.getItemOrderManager().removeExtras(((LogisticsExtraDictPromise) promise).getResource());
		}
		if (promise instanceof LogisticsExtraPromise) {
			service.getItemOrderManager()
					.removeExtras(new Resource.Dict(new ItemStack(promise.item, promise.numberOfItems), null));
		}
		if (promise instanceof LogisticsDictPromise) {
			service.spawnParticle(Particles.WhiteParticle, 2);
			return service.getItemOrderManager().addOrder(((LogisticsDictPromise) promise)
					.getResource(), destination, ResourceType.CRAFTING, info);
		}
		service.spawnParticle(Particles.WhiteParticle, 2);
		return service.getItemOrderManager()
				.addOrder(new ItemStack(promise.item, promise.numberOfItems), destination, ResourceType.CRAFTING, info);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list, List<IFilter> filter) {

	}

	@Override
	public Router getRouter() {
		return service.getRouter();
	}

	@Override
	public void itemCouldNotBeSend(ItemStack item, IAdditionalTargetInformation info) {
		_invRequester.itemCouldNotBeSend(item, info);
	}

	@Override
	public int getID() {
		return service.getRouter().getSimpleId();
	}

	@Override
	public int compareTo(@NotNull ItemRequester other) {
		return 0;
	}

	@Override
	public void registerExtras(Promise promise) {
		if (promise instanceof LogisticsDictPromise) {
			service.getItemOrderManager().addExtra(((LogisticsDictPromise) promise).getResource());
			return;
		} else {
			ItemStack stack = new ItemStack(promise.getItemType(), promise.getAmount());
			service.getItemOrderManager().addExtra(new Resource.Dict(stack, null));
		}
	}

	@Override
	public CraftingTemplate addCrafting(Resource toCraft) {

		List<ItemStack> stack = getCraftedItems();
		if (stack == null) {
			return null;
		}
		ReqCraftingTemplate template = null;
		if (this.getUpgradeManager().isFuzzyUpgrade() && outputFuzzyFlags.getBitSet().nextSetBit(0) != -1) {
			if (toCraft instanceof Resource.Dict) {
				for (ItemStack craftable : stack) {
					Resource.Dict dict = new Resource.Dict(craftable, null);
					dict.loadFromBitSet(outputFuzzyFlags.getBitSet());
					if (toCraft.matches(craftable.getItem(), Resource.MatchSettings.NORMAL) && dict.matches(((Resource.Dict) toCraft).getItem(), Resource.MatchSettings.NORMAL) && dict.getBitSet().equals(((Resource.Dict) toCraft).getBitSet())) {
						template = new DictCraftingTemplate(dict, this, priority);
						break;
					}
				}
			}
		} else {
			for (ItemStack craftable : stack) {
				if (toCraft.matches(craftable.getItem(), Resource.MatchSettings.NORMAL)) {
					template = new ItemCraftingTemplate(craftable, this, priority);
					break;
				}
			}
		}
		if (template == null) {
			return null;
		}

		ItemRequester[] target = new ItemRequester[9];
		for (int i = 0; i < 9; i++) {
			target[i] = this;
		}

		boolean hasSatellite = isSatelliteConnected();
		if (!hasSatellite) {
			return null;
		}
		if (!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			Router r = getSatelliteRouter(-1);
			if (r != null) {
				ItemRequester sat = r.getPipe();
				for (int i = 6; i < 9; i++) {
					target[i] = sat;
				}
			}
		} else {
			for (int i = 0; i < 9; i++) {
				Router r = getSatelliteRouter(i);
				if (r != null) {
					target[i] = r.getPipe();
				}
			}
		}

		// Check all materials
		for (int i = 0; i < 9; i++) {
			ItemStack resourceStack = getMaterials(i);
			if (resourceStack == null || resourceStack.getCount() == 0) {
				continue;
			}
			Resource req;
			if (getUpgradeManager().isFuzzyUpgrade() && fuzzyCraftingFlagArray[i].getBitSet().nextSetBit(0) != -1) {
				Resource.Dict dict;
				req = dict = new Resource.Dict(resourceStack, target[i]);
				dict.loadFromBitSet(fuzzyCraftingFlagArray[i].getBitSet());
			} else {
				req = new ItemResource(resourceStack, target[i]);
			}
			template.addRequirement(req, new CraftingChassieInformation(i, getPositionInt()));
		}

		int liquidCrafter = getUpgradeManager().getFluidCrafter();
		FluidRequester[] liquidTarget = new FluidRequester[liquidCrafter];

		if (!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			Router r = getFluidSatelliteRouter(-1);
			if (r != null) {
				FluidRequester sat = (FluidRequester) r.getPipe();
				for (int i = 0; i < liquidCrafter; i++) {
					liquidTarget[i] = sat;
				}
			}
		} else {
			for (int i = 0; i < liquidCrafter; i++) {
				Router r = getFluidSatelliteRouter(i);
				if (r != null) {
					liquidTarget[i] = (FluidRequester) r.getPipe();
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

	public boolean isSatelliteConnected() {
		// final List<ExitRoute> routes = getRouter().getIRoutersByCost();
		if (!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if (satelliteUUID == null) {
				return true;
			}
			int satelliteRouterId = RouterManager.getInstance().getIdForUuid(satelliteUUID);
			if (satelliteRouterId != -1) {
				return !getRouter().getRouteTable().get(satelliteRouterId).isEmpty();
			}
		} else {
			boolean foundAll = true;
			for (int i = 0; i < 9; i++) {
				boolean foundOne = false;
				if (advancedSatelliteUUIDArray[i] == null) {
					continue;
				}

				int satelliteRouterId = RouterManager.getInstance().getIdForUuid(advancedSatelliteUUIDArray[i]);
				if (satelliteRouterId != -1) {
					if (!getRouter().getRouteTable().get(satelliteRouterId).isEmpty()) {
						foundOne = true;
					}
				}

				foundAll &= foundOne;
			}
			return foundAll;
		}
		// TODO check for FluidCrafter
		return false;
	}

	@Override
	public boolean canCraft(Resource toCraft) {
		if (getCraftedItem() == null) {
			return false;
		}
		if (toCraft instanceof ItemResource || toCraft instanceof Resource.Dict) {
			return toCraft.matches(getCraftedItem().getItem(), Resource.MatchSettings.NORMAL);
		}
		return false;
	}

	@Override
	public List<ItemStack> getCraftedItems() {
		List<ItemStack> list = new ArrayList<>(1);
		if (getCraftedItem() != null) {
			list.add(getCraftedItem());
		}
		return list;
	}

	public ItemStack getCraftedItem() {
		return _dummyInventory.getIDStackInSlot(9);
	}

	@Override
	public int getTodo() {
		return service.getItemOrderManager().totalAmountCountInAllOrders();
	}

	private Router getSatelliteRouter(int x) {
		if (x == -1) {
			int satelliteRouterId = RouterManager.getInstance().getIdForUuid(satelliteUUID);
			return RouterManager.getInstance().getRouter(satelliteRouterId);
		} else {
			int satelliteRouterId = RouterManager.getInstance().getIdForUuid(advancedSatelliteUUIDArray[x]);
			return RouterManager.getInstance().getRouter(satelliteRouterId);
		}
	}

	@Override
	public void readFromNBT(CompoundTag nbttagcompound) {
		//		super.readFromNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
		_liquidInventory.readFromNBT(nbttagcompound, "FluidInv");
		_cleanupInventory.readFromNBT(nbttagcompound, "CleanupInv");

		String satelliteUUIDString = nbttagcompound.getString("satelliteUUID");
		satelliteUUID = satelliteUUIDString.isEmpty() ? null : UUID.fromString(satelliteUUIDString);

		priority = nbttagcompound.getInteger("priority");

		for (int i = 0; i < 9; i++) {
			String advancedSatelliteUUIDArrayString = nbttagcompound.getString("advancedSatelliteUUID" + i);
			advancedSatelliteUUIDArray[i] = advancedSatelliteUUIDArrayString.isEmpty() ? null : UUID.fromString(advancedSatelliteUUIDArrayString);
		}

		if (nbttagcompound.hasKey("fuzzyCraftingFlag0")) {
			for (int i = 0; i < 9; i++) {
				int flags = nbttagcompound.getByte("fuzzyCraftingFlag" + i);
				Resource.Dict dict = fuzzyCraftingFlagArray[i];
				if ((flags & 0x1) != 0) {
					dict.use_od = true;
				}
				if ((flags & 0x2) != 0) {
					dict.ignore_dmg = true;
				}
				if ((flags & 0x4) != 0) {
					dict.ignore_nbt = true;
				}
				if ((flags & 0x8) != 0) {
					dict.use_category = true;
				}
			}
		}
		if (nbttagcompound.hasKey("fuzzyFlags")) {
			ListTag lst = nbttagcompound.getTagList("fuzzyFlags", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < 9; i++) {
				CompoundTag comp = lst.getCompoundTagAt(i);
				fuzzyCraftingFlagArray[i].ignore_dmg = comp.getBoolean("ignore_dmg");
				fuzzyCraftingFlagArray[i].ignore_nbt = comp.getBoolean("ignore_nbt");
				fuzzyCraftingFlagArray[i].use_od = comp.getBoolean("use_od");
				fuzzyCraftingFlagArray[i].use_category = comp.getBoolean("use_category");
			}
		}
		if (nbttagcompound.hasKey("outputFuzzyFlags")) {
			CompoundTag comp = nbttagcompound.getCompoundTag("outputFuzzyFlags");
			outputFuzzyFlags.ignore_dmg = comp.getBoolean("ignore_dmg");
			outputFuzzyFlags.ignore_nbt = comp.getBoolean("ignore_nbt");
			outputFuzzyFlags.use_od = comp.getBoolean("use_od");
			outputFuzzyFlags.use_category = comp.getBoolean("use_category");
		}
		for (int i = 0; i < 6; i++) {
			craftingSigns[i] = nbttagcompound.getBoolean("craftingSigns" + i);
		}

		for (int i = 0; i < MAX_LIQUID_CRAFTER; i++) {
			String liquidSatelliteUUIDArrayString = nbttagcompound.getString("liquidSatelliteUUIDArray" + i);
			liquidSatelliteUUIDArray[i] = liquidSatelliteUUIDArrayString.isEmpty() ? null : UUID.fromString(liquidSatelliteUUIDArrayString);
		}
		if (nbttagcompound.hasKey("FluidAmount")) {
			amount = nbttagcompound.getIntArray("FluidAmount");
		}
		if (amount.length < MAX_LIQUID_CRAFTER) {
			amount = new int[MAX_LIQUID_CRAFTER];
		}

		String liquidSatelliteUUIDString = nbttagcompound.getString("liquidSatelliteId");
		liquidSatelliteUUID = liquidSatelliteUUIDString.isEmpty() ? null : UUID.fromString(liquidSatelliteUUIDString);
		cleanupModeIsExclude = nbttagcompound.getBoolean("cleanupModeIsExclude");

		if (nbttagcompound.hasKey("satelliteid")) {
			updateSatelliteFromIDs = new UpgradeSatelliteFromIDs();
			updateSatelliteFromIDs.satelliteId = nbttagcompound.getInteger("satelliteid");
			for (int i = 0; i < 9; i++) {
				updateSatelliteFromIDs.advancedSatelliteIdArray[i] = nbttagcompound.getInteger("advancedSatelliteId" + i);
			}
			for (int i = 0; i < MAX_LIQUID_CRAFTER; i++) {
				updateSatelliteFromIDs.liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
			}
			updateSatelliteFromIDs.liquidSatelliteId = nbttagcompound.getInteger("liquidSatelliteId");
		}
	}

	@Override
	public void writeToNBT(CompoundTag nbttagcompound) {
		//	super.writeToNBT(nbttagcompound);
		_dummyInventory.writeToNBT(nbttagcompound, "");
		_liquidInventory.writeToNBT(nbttagcompound, "FluidInv");
		_cleanupInventory.writeToNBT(nbttagcompound, "CleanupInv");

		nbttagcompound.setString("satelliteUUID", satelliteUUID == null ? "" : satelliteUUID.toString());

		nbttagcompound.setInteger("priority", priority);
		for (int i = 0; i < 9; i++) {
			nbttagcompound.setString("advancedSatelliteUUID" + i, advancedSatelliteUUIDArray[i] == null ? "" : advancedSatelliteUUIDArray[i].toString());
		}
		ListTag lst = new ListTag();
		for (int i = 0; i < 9; i++) {
			CompoundTag comp = new CompoundTag();
			comp.setBoolean("ignore_dmg", fuzzyCraftingFlagArray[i].ignore_dmg);
			comp.setBoolean("ignore_nbt", fuzzyCraftingFlagArray[i].ignore_nbt);
			comp.setBoolean("use_od", fuzzyCraftingFlagArray[i].use_od);
			comp.setBoolean("use_category", fuzzyCraftingFlagArray[i].use_category);
			lst.appendTag(comp);
		}
		nbttagcompound.setTag("fuzzyFlags", lst);
		{
			CompoundTag comp = new CompoundTag();
			comp.setBoolean("ignore_dmg", outputFuzzyFlags.ignore_dmg);
			comp.setBoolean("ignore_nbt", outputFuzzyFlags.ignore_nbt);
			comp.setBoolean("use_od", outputFuzzyFlags.use_od);
			comp.setBoolean("use_category", outputFuzzyFlags.use_category);
			nbttagcompound.setTag("outputFuzzyFlags", comp);
		}
		for (int i = 0; i < 6; i++) {
			nbttagcompound.setBoolean("craftingSigns" + i, craftingSigns[i]);
		}
		for (int i = 0; i < MAX_LIQUID_CRAFTER; i++) {
			nbttagcompound.setString("liquidSatelliteUUIDArray" + i, liquidSatelliteUUIDArray[i] == null ? "" : liquidSatelliteUUIDArray[i].toString());
		}
		nbttagcompound.setIntArray("FluidAmount", amount);
		nbttagcompound.setString("liquidSatelliteId", liquidSatelliteUUID == null ? "" : liquidSatelliteUUID.toString());
		nbttagcompound.setBoolean("cleanupModeIsExclude", cleanupModeIsExclude);
	}

	public ModernPacket getCPipePacket() {
		return PacketHandler.getPacket(CraftingPipeUpdatePacket.class)
				.setAmount(amount)
				.setLiquidSatelliteNameArray(getSatelliteNamesForUUIDs(liquidSatelliteUUIDArray))
				.setLiquidSatelliteName(getSatelliteNameForUUID(liquidSatelliteUUID))
				.setSatelliteName(getSatelliteNameForUUID(satelliteUUID))
				.setAdvancedSatelliteNameArray(getSatelliteNamesForUUIDs(advancedSatelliteUUIDArray))
				.setPriority(priority)
				.setModulePos(this);
	}

	private String getSatelliteNameForUUID(UUID id) {
		if (id == null) {
			return "";
		}
		int simpleId = RouterManager.getInstance().getIdForUuid(id);
		Router router = RouterManager.getInstance().getRouter(simpleId);
		if (router != null) {
			CoreRoutedPipe pipe = router.getPipe();
			if (pipe instanceof PipeItemsSatelliteLogistics) {
				return ((PipeItemsSatelliteLogistics) pipe).getSatellitePipeName();
			} else if (pipe instanceof PipeFluidSatellite) {
				return ((PipeFluidSatellite) pipe).getSatellitePipeName();
			}
		}
		return "UNKNOWN NAME";
	}

	private String[] getSatelliteNamesForUUIDs(UUID[] ids) {
		return Arrays.stream(ids).map(this::getSatelliteNameForUUID).toArray(String[]::new);
	}

	public void handleCraftingUpdatePacket(CraftingPipeUpdatePacket packet) {
		if (MainProxy.isClient(getWorld())) {
			amount = packet.getAmount();
			clientSideSatelliteNames.liquidSatelliteNameArray = packet.getLiquidSatelliteNameArray();
			clientSideSatelliteNames.liquidSatelliteName = packet.getLiquidSatelliteName();
			clientSideSatelliteNames.satelliteName = packet.getSatelliteName();
			clientSideSatelliteNames.advancedSatelliteNameArray = packet.getAdvancedSatelliteNameArray();
			priority = packet.getPriority();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(CraftingModuleSlot.class).setAdvancedSat(getUpgradeManager().isAdvancedSatelliteCrafter()).setLiquidCrafter(getUpgradeManager().getFluidCrafter()).setAmount(amount).setHasByproductExtractor(getUpgradeManager().hasByproductExtractor()).setFuzzy(
				getUpgradeManager().isFuzzyUpgrade())
				.setCleanupSize(getUpgradeManager().getCrafterCleanup()).setCleanupExclude(cleanupModeIsExclude);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(CraftingModuleInHand.class).setAmount(amount).setCleanupExclude(cleanupModeIsExclude);
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
			WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(getWorld(), getX(), getY(), getZ());

			for (NeighborBlockEntity adjacent : worldCoordinates.connectedTileEntities(ConnectionPipeType.ITEM).collect(Collectors.toList())) {
				for (CraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
					if (provider.importRecipe(adjacent.getBlockEntity(), _dummyInventory)) {
						if (provider instanceof FuzzyRecipeProvider) {
							((FuzzyRecipeProvider) provider).importFuzzyFlags(adjacent.getBlockEntity(), _dummyInventory, fuzzyCraftingFlagArray, outputFuzzyFlags);
						}
						// ToDo: break only out of the inner loop?
						break;
					}
				}
			}
			// Send inventory as packet
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(_dummyInventory).setModulePos(this);
			if (player != null) {
				MainProxy.sendPacketToPlayer(packet, player);
			}
			MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), getWorld().provider.getDimension(), packet);
		}
	}

	protected World getWorld() {
		return world.getWorld();
	}

	public void priorityUp(EntityPlayer player) {
		priority++;
		if (MainProxy.isClient(player.world)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePriorityUpPacket.class).setModulePos(this));
		} else if (MainProxy.isServer(player.world)) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPriority.class).setInteger(priority).setModulePos(this), player);
		}
	}

	public void priorityDown(EntityPlayer player) {
		priority--;
		if (MainProxy.isClient(player.world)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePriorityDownPacket.class).setModulePos(this));
		} else if (MainProxy.isServer(player.world)) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPriority.class).setInteger(priority).setModulePos(this), player);
		}
	}

	public ItemStack getByproductItem() {
		return _dummyInventory.getIDStackInSlot(10);
	}

	public ItemStack getMaterials(int slotnr) {
		return _dummyInventory.getIDStackInSlot(slotnr);
	}

	public FluidIdentifier getFluidMaterial(int slotnr) {
		ItemStack stack = _liquidInventory.getIDStackInSlot(slotnr);
		if (stack == null) {
			return null;
		}
		return FluidIdentifier.get(stack.getItem());
	}

	public void changeFluidAmount(int change, int slot, EntityPlayer player) {
		if (MainProxy.isClient(player.world)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(change).setModulePos(this));
		} else {
			amount[slot] += change;
			if (amount[slot] <= 0) {
				amount[slot] = 0;
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(amount[slot]).setModulePos(this), player);
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

	public void setFluidAmount(int[] amount) {
		if (MainProxy.isClient(getWorld())) {
			this.amount = amount;
		}
	}

	private Router getFluidSatelliteRouter(int x) {
		if (x == -1) {
			int satelliteRouterId = RouterManager.getInstance().getIdForUuid(liquidSatelliteUUID);
			return RouterManager.getInstance().getRouter(satelliteRouterId);
		} else {
			int satelliteRouterId = RouterManager.getInstance().getIdForUuid(liquidSatelliteUUIDArray[x]);
			return RouterManager.getInstance().getRouter(satelliteRouterId);
		}
	}

	public void openAttachedGui(EntityPlayer player) {
		if (MainProxy.isClient(player.world)) {
			if (player instanceof EntityPlayerMP) {
				player.closeScreen();
			} else if (player instanceof EntityPlayerSP) {
				player.closeScreen();
			}
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeOpenConnectedGuiPacket.class).setModulePos(this));
			return;
		}

		// hack to avoid wrenching blocks
		int savedEquipped = player.inventory.currentItem;
		boolean foundSlot = false;
		// try to find a empty slot
		for (int i = 0; i < 9; i++) {
			if (player.inventory.getStackInSlot(i).isEmpty()) {
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

		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(getWorld(), getX(), getY(), getZ());

		worldCoordinates.connectedTileEntities(ConnectionPipeType.ITEM).anyMatch(adjacent -> {
			boolean found = SimpleServiceLocator.craftingRecipeProviders.stream()
					.anyMatch(provider -> provider.canOpenGui(adjacent.getBlockEntity()));

			if (!found) {
				found = InventoryUtilFactory.INSTANCE.getInventoryUtil(adjacent) != null;
			}

			if (found) {
				final BlockPos pos = adjacent.getBlockEntity().getPos();
				BlockState blockState = getWorld().getBlockState(pos);
				return !blockState.getBlock().isAir(blockState, getWorld(), pos) && blockState.getBlock()
						.onBlockActivated(getWorld(), pos, adjacent.getBlockEntity().getWorld().getBlockState(pos),
								player, EnumHand.MAIN_HAND, Direction.UP, 0, 0, 0);
			}
			return false;
		});
		player.inventory.currentItem = savedEquipped;
	}

	public void enabledUpdateEntity() {
		if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
			if (service.isNthTick(6)) {
				cacheAreAllOrderesToBuffer();
			}
			if (service.getItemOrderManager().isFirstOrderWatched()) {
				BlockEntity tile = lastAccessedCrafter.get();
				if (tile != null) {
					service.getItemOrderManager().setMachineProgress(SimpleServiceLocator.machineProgressProvider.getProgressForTile(tile));
				} else {
					service.getItemOrderManager().setMachineProgress((byte) 0);
				}
			}
		} else {
			cachedAreAllOrderesToBuffer = false;
		}

		if (!service.isNthTick(6)) {
			return;
		}

		waitingForCraft = false;

		if ((!service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA))) {
			if (getUpgradeManager().getCrafterCleanup() > 0) {
				final List<NeighborBlockEntity<BlockEntity>> crafters = locateCraftersForExtraction();
				ItemStack extracted = null;
				for (NeighborBlockEntity<BlockEntity> adjacentCrafter : crafters) {
					extracted = extractFiltered(adjacentCrafter, _cleanupInventory, cleanupModeIsExclude, getUpgradeManager().getCrafterCleanup() * 3);
					if (extracted != null && !extracted.isEmpty()) {
						break;
					}
				}
				if (extracted != null && !extracted.isEmpty()) {
					service.queueRoutedItem(RoutedItemHelper.INSTANCE.createNewTravelItem(extracted), Direction.UP);
					service.getCacheHolder().trigger(CacheTypes.Inventory);
				}
			}
			return;
		}

		waitingForCraft = true;

		List<NeighborBlockEntity<BlockEntity>> adjacentCrafters = locateCraftersForExtraction();
		if (adjacentCrafters.size() < 1) {
			if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
				service.getItemOrderManager().sendFailed();
			}
			return;
		}

		List<ItemStack> wanteditem = getCraftedItems();
		if (wanteditem == null || wanteditem.isEmpty()) {
			return;
		}

		service.spawnParticle(Particles.VioletParticle, 2);

		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		while (itemsleft > 0 && stacksleft > 0 && (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA))) {
			LogisticsItemOrder nextOrder = service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA); // fetch but not remove.
			int maxtosend = Math.min(itemsleft, nextOrder.getResource().stack.getCount());
			maxtosend = Math.min(nextOrder.getResource().getItem().getMaxStackSize(), maxtosend);
			// retrieve the new crafted items
			ItemStack extracted = null;
			NeighborBlockEntity<BlockEntity> adjacent = null; // there has to be at least one adjacentCrafter at this point; adjacent wont stay null
			for (NeighborBlockEntity<BlockEntity> adjacentCrafter : adjacentCrafters) {
				adjacent = adjacentCrafter;
				extracted = extract(adjacent, nextOrder.getResource(), maxtosend);
				if (extracted != null && !extracted.isEmpty()) {
					break;
				}
			}
			if (extracted == null || extracted.isEmpty()) {
				service.getItemOrderManager().deferSend();
				break;
			}
			service.getCacheHolder().trigger(CacheTypes.Inventory);
			lastAccessedCrafter = new WeakReference<>(adjacent.getBlockEntity());
			// send the new crafted items to the destination
			ItemIdentifier extractedID = ItemIdentifier.get(extracted);
			while (!extracted.isEmpty()) {
				if (!doesExtractionMatch(nextOrder, extractedID)) {
					LogisticsItemOrder startOrder = nextOrder;
					if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
						do {
							service.getItemOrderManager().deferSend();
							nextOrder = service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA);
						} while (!doesExtractionMatch(nextOrder, extractedID) && startOrder != nextOrder);
					}
					if (startOrder == nextOrder) {
						int numtosend = Math.min(extracted.getCount(), extractedID.getMaxStackSize());
						if (numtosend == 0) {
							break;
						}
						stacksleft -= 1;
						itemsleft -= numtosend;
						ItemStack stackToSend = extracted.splitStack(numtosend);
						// Route the unhandled item

						service.sendStack(stackToSend, -1, ItemSendMode.Normal, null);
						continue;
					}
				}
				int numtosend = Math.min(extracted.getCount(), extractedID.getMaxStackSize());
				numtosend = Math.min(numtosend, nextOrder.getResource().stack.getCount());
				if (numtosend == 0) {
					break;
				}
				stacksleft -= 1;
				itemsleft -= numtosend;
				ItemStack stackToSend = extracted.splitStack(numtosend);
				if (nextOrder.getDestination() != null) {
					SinkReply reply = LogisticsManagerImpl.canSink(nextOrder.getDestination().getRouter(), null, true, ItemIdentifier.get(stackToSend), null, true,
							false);
					boolean defersend = false;
					if (reply == null || reply.bufferMode != BufferMode.NONE || reply.maxNumberOfItems < 1) {
						defersend = true;
					}
					IRoutedItem item = RoutedItemHelper.INSTANCE.createNewTravelItem(stackToSend);
					item.setDestination(nextOrder.getDestination().getRouter().getSimpleId());
					item.setTransportMode(TransportMode.Active);
					item.setAdditionalTargetInformation(nextOrder.getInformation());
					service.queueRoutedItem(item, adjacent.getDirection());
					service.getItemOrderManager().sendSuccessfull(stackToSend.getCount(), defersend, item);
				} else {
					service.sendStack(stackToSend, -1, ItemSendMode.Normal, nextOrder.getInformation());
					service.getItemOrderManager().sendSuccessfull(stackToSend.getCount(), false, null);
				}
				if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
					nextOrder = service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA); // fetch but not remove.
				}
			}
		}

	}

	private boolean doesExtractionMatch(LogisticsItemOrder nextOrder, ItemIdentifier extractedID) {
		return nextOrder.getResource().getItem().equals(extractedID) || (this.getUpgradeManager().isFuzzyUpgrade() && nextOrder.getResource().getBitSet().nextSetBit(0) != -1 && nextOrder.getResource().matches(extractedID, Resource.MatchSettings.NORMAL));
	}

	public boolean areAllOrderesToBuffer() {
		return cachedAreAllOrderesToBuffer;
	}

	public void cacheAreAllOrderesToBuffer() {
		boolean result = true;
		for (LogisticsItemOrder order : service.getItemOrderManager()) {
			if (order.getDestination() instanceof ItemSpaceControl) {
				SinkReply reply = LogisticsManagerImpl.canSink(order.getDestination().getRouter(), null, true, order.getResource().getItem(), null, true, false);
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

	private ItemStack extract(NeighborBlockEntity<BlockEntity> adjacent, Resource item, int amount) {
		return adjacent.getJavaInstanceOf(LogisticsCraftingTableTileEntity.class)
				.map(adjacentCraftingTable -> extractFromLogisticsCraftingTable(adjacentCraftingTable, item, amount))
				.orElseGet(() -> adjacent.isItemHandler() ? extractFromInventory(adjacent.getUtilForItemHandler(), item, amount) : ItemStack.EMPTY);
	}

	private ItemStack extractFiltered(NeighborBlockEntity<BlockEntity> adjacent, ItemIdentifierInventory inv, boolean isExcluded, int filterInvLimit) {
		return adjacent.isItemHandler() ? extractFromInventoryFiltered(adjacent.getUtilForItemHandler(), inv, isExcluded, filterInvLimit) : null;
	}

	private ItemStack extractFromInventory(@Nonnull WrappedInventory invUtil, Resource wanteditem, int count) {
		ItemIdentifier itemToExtract = null;
		if (wanteditem instanceof ItemResource) {
			itemToExtract = ((ItemResource) wanteditem).getItem();
		} else if (wanteditem instanceof Resource.Dict) {
			int max = Integer.MIN_VALUE;
			ItemIdentifier toExtract = null;
			for (Map.Entry<ItemIdentifier, Integer> content : invUtil.getItemsAndCount().entrySet()) {
				if (wanteditem.matches(content.getKey(), Resource.MatchSettings.NORMAL)) {
					if (content.getValue() > max) {
						max = content.getValue();
						toExtract = content.getKey();
					}
				}
			}
			if (toExtract == null) {
				return null;
			}
			itemToExtract = toExtract;
		}
		int available = invUtil.itemCount(itemToExtract);
		if (available == 0) {
			return null;
		}
		if (!service.useEnergy(neededEnergy() * Math.min(count, available))) {
			return null;
		}
		return invUtil.getMultipleItems(itemToExtract, Math.min(count, available));
	}

	private ItemStack extractFromInventoryFiltered(@Nonnull WrappedInventory invUtil, ItemIdentifierInventory filter, boolean isExcluded, int filterInvLimit) {
		ItemIdentifier wanteditem = null;
		for (ItemIdentifier item : invUtil.getItemsAndCount().keySet()) {
			if (isExcluded) {
				boolean found = false;
				for (int i = 0; i < filter.getSizeInventory() && i < filterInvLimit; i++) {
					ItemStack identStack = filter.getIDStackInSlot(i);
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
					ItemStack identStack = filter.getIDStackInSlot(i);
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
		if (!service.useEnergy(neededEnergy() * Math.min(64, available))) {
			return null;
		}
		return invUtil.getMultipleItems(wanteditem, Math.min(64, available));
	}

	private ItemStack extractFromLogisticsCraftingTable(
			NeighborBlockEntity<LogisticsCraftingTableTileEntity> adjacentCraftingTable,
			Resource wanteditem, int count) {
		ItemStack extracted = extractFromInventory(
				Objects.requireNonNull(adjacentCraftingTable.getInventoryUtil()),
				wanteditem, count);
		if (extracted != null && !extracted.isEmpty()) {
			return extracted;
		}
		ItemStack retstack = null;
		while (count > 0) {
			ItemStack stack = adjacentCraftingTable.getBlockEntity().getOutput(wanteditem, service);
			if (stack == null || stack.getCount() == 0) {
				break;
			}
			if (retstack == null) {
				if (!wanteditem.matches(ItemIdentifier.get(stack), wanteditem instanceof ItemResource ? Resource.MatchSettings.WITHOUT_NBT : Resource.MatchSettings.NORMAL)) {
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
			if (!service.useEnergy(neededEnergy() * stack.getCount())) {
				break;
			}

			if (retstack == null) {
				retstack = stack;
			} else {
				retstack.grow(stack.getCount());
			}
			count -= stack.getCount();
			if (getUpgradeManager().isFuzzyUpgrade()) {
				break;
			}
		}
		return retstack;
	}

	protected int neededEnergy() {
		return (int) (10 * Math.pow(1.1, getUpgradeManager().getItemExtractionUpgrade()) * Math.pow(1.2, getUpgradeManager().getItemStackExtractionUpgrade()));
	}

	protected int itemsToExtract() {
		return (int) Math.pow(2, getUpgradeManager().getItemExtractionUpgrade());
	}

	protected int stacksToExtract() {
		return 1 + getUpgradeManager().getItemStackExtractionUpgrade();
	}

	public List<NeighborBlockEntity<BlockEntity>> locateCraftersForExtraction() {
		if (cachedCrafters == null) {
			cachedCrafters = new WorldCoordinatesWrapper(getWorld(), getX(), getY(), getZ())
					.connectedTileEntities(ConnectionPipeType.ITEM)
					.filter(neighbor -> neighbor.isItemHandler() || neighbor.getInventoryUtil() != null)
					.collect(Collectors.toList());
		}
		return cachedCrafters;
	}

	@Override
	public void clearCache() {
		cachedCrafters = null;
	}

	public void importCleanup() {
		for (int i = 0; i < 10; i++) {
			_cleanupInventory.setInventorySlotContents(i, _dummyInventory.getStackInSlot(i));
		}
		for (int i = 10; i < _cleanupInventory.getSizeInventory(); i++) {
			_cleanupInventory.setInventorySlotContents(i, (ItemStack) null);
		}
		_cleanupInventory.compactFirst(10);
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

	private void updateSatellitesOnClient() {
		MainProxy.sendToPlayerList(getCPipePacket(), guiWatcher);
	}

	public void setSatelliteUUID(UUID pipeID) {
		this.satelliteUUID = pipeID;
		updateSatellitesOnClient();
		updateSatelliteFromIDs = null;
	}

	public void setAdvancedSatelliteUUID(int i, UUID pipeID) {
		this.advancedSatelliteUUIDArray[i] = pipeID;
		updateSatellitesOnClient();
		updateSatelliteFromIDs = null;
	}

	public void setFluidSatelliteUUID(UUID pipeID) {
		this.liquidSatelliteUUID = pipeID;
		updateSatellitesOnClient();
		updateSatelliteFromIDs = null;
	}

	public void setAdvancedFluidSatelliteUUID(int i, UUID pipeID) {
		this.liquidSatelliteUUIDArray[i] = pipeID;
		updateSatellitesOnClient();
		updateSatelliteFromIDs = null;
	}

	@Override
	public void guiOpenedByPlayer(PlayerEntity player) {
		guiWatcher.add(player);
	}

	@Override
	public void guiClosedByPlayer(PlayerEntity player) {
		guiWatcher.remove(player);
	}

	public static class CraftingChassieInformation extends ChassiTargetInformation {

		@Getter
		private final int craftingSlot;

		public CraftingChassieInformation(int craftingSlot, int moduleSlot) {
			super(moduleSlot);
			this.craftingSlot = craftingSlot;
		}
	}

	private static class UpgradeSatelliteFromIDs {

		public int satelliteId;
		public int[] advancedSatelliteIdArray = new int[9];
		public int[] liquidSatelliteIdArray = new int[MAX_LIQUID_CRAFTER];
		public int liquidSatelliteId;
	}

	public static class ClientSideSatelliteNames {

		public @Nonnull
		String satelliteName = "";
		public @Nonnull
		String[] advancedSatelliteNameArray = {};
		public @Nonnull
		String liquidSatelliteName = "";
		public @Nonnull
		String[] liquidSatelliteNameArray = {};
	}
}
