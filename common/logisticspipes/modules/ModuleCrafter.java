package logisticspipes.modules;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;

import lombok.Getter;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IItemSpaceControl;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
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
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IFuzzyRecipeProvider;
import logisticspipes.request.DictCraftingTemplate;
import logisticspipes.request.ICraftingTemplate;
import logisticspipes.request.IPromise;
import logisticspipes.request.IReqCraftingTemplate;
import logisticspipes.request.ItemCraftingTemplate;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.FluidResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsDictPromise;
import logisticspipes.routing.LogisticsExtraDictPromise;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LogisticsItemOrder;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.DelayedGeneric;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.BufferMode;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class ModuleCrafter extends LogisticsModule implements ICraftItems, IHUDModuleHandler, IModuleWatchReciver, IGuiOpenControler, Gui {

	// for reliable transport
	protected final DelayQueue<DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>>> _lostItems = new DelayQueue<>();
	protected final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	protected final PlayerCollectionList guiWatcher = new PlayerCollectionList();

	public UUID satelliteUUID = null;
	public UUID[] advancedSatelliteUUIDArray = new UUID[9];
	public UUID liquidSatelliteUUID = null;
	public UUID[] liquidSatelliteUUIDArray = new UUID[ItemUpgrade.MAX_LIQUID_CRAFTER];

	public DictResource[] fuzzyCraftingFlagArray = new DictResource[9];
	public DictResource outputFuzzyFlags = new DictResource(null, null);
	public int priority = 0;

	public boolean[] craftingSigns = new boolean[6];
	public boolean waitingForCraft = false;
	public boolean cleanupModeIsExclude = true;
	public ClientSideSatelliteNames clientSideSatelliteNames = new ClientSideSatelliteNames();
	// from PipeItemsCraftingLogistics
	protected ItemIdentifierInventory _dummyInventory = new ItemIdentifierInventory(11, "Requested items", 127);
	protected ItemIdentifierInventory _liquidInventory = new ItemIdentifierInventory(ItemUpgrade.MAX_LIQUID_CRAFTER, "Fluid items", 1, true);
	protected ItemIdentifierInventory _cleanupInventory = new ItemIdentifierInventory(ItemUpgrade.MAX_CRAFTING_CLEANUP * 3, "Cleanup Filer Items", 1);
	protected int[] amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	protected SinkReply _sinkReply;
	private IRequestItems _invRequester;
	private WeakReference<TileEntity> lastAccessedCrafter = new WeakReference<TileEntity>(null);
	private boolean cachedAreAllOrderesToBuffer;
	private List<NeighborTileEntity<TileEntity>> cachedCrafters = null;
	private UpgradeSatelliteFromIDs updateSatelliteFromIDs = null;

	public ModuleCrafter() {
		for (int i = 0; i < fuzzyCraftingFlagArray.length; i++) {
			fuzzyCraftingFlagArray[i] = new DictResource(null, null);
		}
	}

	public ModuleCrafter(PipeItemsCraftingLogistics parent) {
		_service = parent;
		_invRequester = parent;
		_world = parent;
		registerPosition(ModulePositionType.IN_PIPE, 0);
		for (int i = 0; i < fuzzyCraftingFlagArray.length; i++) {
			fuzzyCraftingFlagArray[i] = new DictResource(null, null);
		}
	}

	public static String getName() {
		return "crafter";
	}

	/**
	 * assumes that the invProvider is also IRequest items.
	 */
	@Override
	public void registerHandler(IWorldProvider world, IPipeServiceProvider service) {
		super.registerHandler(world, service);
		_invRequester = (IRequestItems) service;
	}

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		final int itemCount = spaceFor(stack, item, includeInTransit);
		if (itemCount > 0) {
			return new SinkReply(_sinkReply, itemCount, areAllOrderesToBuffer() ? BufferMode.DESTINATION_BUFFERED : BufferMode.NONE);
		} else {
			return null;
		}
	}

	protected int spaceFor(@Nonnull ItemStack stack, ItemIdentifier item, boolean includeInTransit) {
		Pair<String, ItemIdentifier> key = new Pair<>("spaceFor", item);
		Object cache = _service.getCacheHolder().getCacheFor(CacheTypes.Inventory, key);
		int onRoute = 0;
		if (includeInTransit) {
			onRoute = _service.countOnRoute(item);
		}
		if (cache != null) {
			return ((Integer) cache) - onRoute;
		}
		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(_world.getWorld(), getBlockPos());

		if (includeInTransit) {
			stack = stack.copy();
			stack.grow(onRoute);
		}
		final ItemStack finalStack = stack;
		int count = worldCoordinates
				.connectedTileEntities(ConnectionPipeType.ITEM)
				.map(neighbor -> neighbor.sneakyInsertion().from(getUpgradeManager()))
				.filter(NeighborTileEntity::isItemHandler)
				.map(NeighborTileEntity::getUtilForItemHandler)
				.map(invUtil -> invUtil.roomForItem(finalStack))
				.reduce(Integer::sum).orElse(0);

		_service.getCacheHolder().setCache(CacheTypes.Inventory, key, count);
		return count - onRoute;
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
		if (updateSatelliteFromIDs != null && _service.isNthTick(100)) {
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
		DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>> lostItem = _lostItems.poll();
		int rerequested = 0;
		while (lostItem != null && rerequested < 100) {
			Pair<ItemIdentifierStack, IAdditionalTargetInformation> pair = lostItem.get();
			if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
				SinkReply reply = LogisticsManager.canSink(pair.getValue1().makeNormalStack(), getRouter(), null, true, pair.getValue1().getItem(), null, true, true, false);
				if (reply == null || reply.maxNumberOfItems < 1) {
					_lostItems.add(new DelayedGeneric<>(pair, 9000 + (int) (Math.random() * 2000)));
					lostItem = _lostItems.poll();
					continue;
				}
			}
			int received = RequestTree.requestPartial(pair.getValue1(), (CoreRoutedPipe) _service, pair.getValue2());
			rerequested++;
			if (received < pair.getValue1().getStackSize()) {
				pair.getValue1().setStackSize(pair.getValue1().getStackSize() - received);
				_lostItems.add(new DelayedGeneric<>(pair, 4500 + (int) (Math.random() * 1000)));
			}
			lostItem = _lostItems.poll();
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {}

	@Override
	public void itemLost(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		_lostItems.add(new DelayedGeneric<>(new Pair<>(item, info), 5000));
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
		List<ItemIdentifierStack> result = getCraftedItems();
		if (result == null) {
			return;
		}

		result.stream().map(ItemIdentifierStack::getItem).forEach(itemidCollection::add);
		/*
		for(int i=0; i<9;i++) {
			ItemIdentifierStack stack = getMaterials(i);
			if(stack != null) {
				itemidCollection.add(stack.getItem()); // needed to be interested in things for a chassi to report reliableDelivery failure.
			}
		}
		 */
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
		if (!_service.getItemOrderManager().hasExtras() || tree.hasBeenQueried(_service.getItemOrderManager())) {
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
		for (LogisticsItemOrder extra : _service.getItemOrderManager()) {
			if (extra.getType() == ResourceType.EXTRA) {
				if (extra.getResource().getItem().equals(requestedItem.getAsItem())) {
					remaining += extra.getResource().stack.getStackSize();
				}
			}
		}
		remaining -= root.getAllPromissesFor(this, getCraftedItem().getItem());
		if (remaining < 1) {
			return;
		}
		if (this.getUpgradeManager().isFuzzyUpgrade() && outputFuzzyFlags.getBitSet().nextSetBit(0) != -1) {
			DictResource dict = new DictResource(getCraftedItem(), null).loadFromBitSet(outputFuzzyFlags.getBitSet());
			LogisticsExtraDictPromise promise = new LogisticsExtraDictPromise(dict, Math.min(remaining, tree.getMissingAmount()), this, true);
			tree.addPromise(promise);
		} else {
			LogisticsExtraPromise promise = new LogisticsExtraPromise(getCraftedItem().getItem(), Math.min(remaining, tree.getMissingAmount()), this, true);
			tree.addPromise(promise);
		}
		tree.setQueried(_service.getItemOrderManager());
	}

	@Override
	public LogisticsItemOrder fullFill(LogisticsPromise promise, IRequestItems destination, IAdditionalTargetInformation info) {
		if (promise instanceof LogisticsExtraDictPromise) {
			_service.getItemOrderManager().removeExtras(((LogisticsExtraDictPromise) promise).getResource());
		}
		if (promise instanceof LogisticsExtraPromise) {
			_service.getItemOrderManager()
					.removeExtras(new DictResource(new ItemIdentifierStack(promise.item, promise.numberOfItems), null));
		}
		if (promise instanceof LogisticsDictPromise) {
			_service.spawnParticle(Particles.WhiteParticle, 2);
			return _service.getItemOrderManager().addOrder(((LogisticsDictPromise) promise)
					.getResource(), destination, ResourceType.CRAFTING, info);
		}
		_service.spawnParticle(Particles.WhiteParticle, 2);
		return _service.getItemOrderManager()
				.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, ResourceType.CRAFTING, info);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list, List<IFilter> filter) {

	}

	@Override
	@Nonnull
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
		if (promise instanceof LogisticsDictPromise) {
			_service.getItemOrderManager().addExtra(((LogisticsDictPromise) promise).getResource());
			return;
		} else {
			ItemIdentifierStack stack = new ItemIdentifierStack(promise.getItemType(), promise.getAmount());
			_service.getItemOrderManager().addExtra(new DictResource(stack, null));
		}
	}

	@Override
	public ICraftingTemplate addCrafting(IResource toCraft) {

		List<ItemIdentifierStack> stack = getCraftedItems();
		if (stack == null) {
			return null;
		}
		IReqCraftingTemplate template = null;
		if (this.getUpgradeManager().isFuzzyUpgrade() && outputFuzzyFlags.getBitSet().nextSetBit(0) != -1) {
			if (toCraft instanceof DictResource) {
				for (ItemIdentifierStack craftable : stack) {
					DictResource dict = new DictResource(craftable, null);
					dict.loadFromBitSet(outputFuzzyFlags.getBitSet());
					if (toCraft.matches(craftable.getItem(), IResource.MatchSettings.NORMAL) && dict.matches(((DictResource) toCraft).getItem(), IResource.MatchSettings.NORMAL) && dict.getBitSet().equals(((DictResource) toCraft).getBitSet())) {
						template = new DictCraftingTemplate(dict, this, priority);
						break;
					}
				}
			}
		} else {
			for (ItemIdentifierStack craftable : stack) {
				if (toCraft.matches(craftable.getItem(), IResource.MatchSettings.NORMAL)) {
					template = new ItemCraftingTemplate(craftable, this, priority);
					break;
				}
			}
		}
		if (template == null) {
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
			IRouter r = getSatelliteRouter(-1);
			if (r != null) {
				IRequestItems sat = r.getPipe();
				for (int i = 6; i < 9; i++) {
					target[i] = sat;
				}
			}
		} else {
			for (int i = 0; i < 9; i++) {
				IRouter r = getSatelliteRouter(i);
				if (r != null) {
					target[i] = r.getPipe();
				}
			}
		}

		//Check all materials
		for (int i = 0; i < 9; i++) {
			ItemIdentifierStack resourceStack = getMaterials(i);
			if (resourceStack == null || resourceStack.getStackSize() == 0) {
				continue;
			}
			IResource req;
			if (getUpgradeManager().isFuzzyUpgrade() && fuzzyCraftingFlagArray[i].getBitSet().nextSetBit(0) != -1) {
				DictResource dict;
				req = dict = new DictResource(resourceStack, target[i]);
				dict.loadFromBitSet(fuzzyCraftingFlagArray[i].getBitSet());
			} else {
				req = new ItemResource(resourceStack, target[i]);
			}
			template.addRequirement(req, new CraftingChassieInformation(i, getPositionInt()));
		}

		int liquidCrafter = getUpgradeManager().getFluidCrafter();
		IRequestFluid[] liquidTarget = new IRequestFluid[liquidCrafter];

		if (!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			IRouter r = getFluidSatelliteRouter(-1);
			if (r != null) {
				IRequestFluid sat = (IRequestFluid) r.getPipe();
				for (int i = 0; i < liquidCrafter; i++) {
					liquidTarget[i] = sat;
				}
			}
		} else {
			for (int i = 0; i < liquidCrafter; i++) {
				IRouter r = getFluidSatelliteRouter(i);
				if (r != null) {
					liquidTarget[i] = (IRequestFluid) r.getPipe();
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
		//final List<ExitRoute> routes = getRouter().getIRoutersByCost();
		if (!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if (satelliteUUID == null) {
				return true;
			}
			int satelliteRouterId = SimpleServiceLocator.routerManager.getIDforUUID(satelliteUUID);
			if (satelliteRouterId != -1) {
				List<ExitRoute> rt = getRouter().getRouteTable().get(satelliteRouterId);
				return rt != null && !rt.isEmpty();
			}
		} else {
			boolean foundAll = true;
			for (int i = 0; i < 9; i++) {
				boolean foundOne = false;
				if (advancedSatelliteUUIDArray[i] == null) {
					continue;
				}

				int satelliteRouterId = SimpleServiceLocator.routerManager.getIDforUUID(advancedSatelliteUUIDArray[i]);
				if (satelliteRouterId != -1) {
					List<ExitRoute> rt = getRouter().getRouteTable().get(satelliteRouterId);
					if (rt != null && !rt.isEmpty()) {
						foundOne = true;
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
			return toCraft.matches(getCraftedItem().getItem(), IResource.MatchSettings.NORMAL);
		}
		return false;
	}

	@Override
	public List<ItemIdentifierStack> getCraftedItems() {
		List<ItemIdentifierStack> list = new ArrayList<>(1);
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

	private IRouter getSatelliteRouter(int x) {
		final UUID satelliteUUID = x == -1 ? this.satelliteUUID : advancedSatelliteUUIDArray[x];
		final int satelliteRouterId = SimpleServiceLocator.routerManager.getIDforUUID(satelliteUUID);
		return SimpleServiceLocator.routerManager.getRouter(satelliteRouterId);
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
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
				DictResource dict = fuzzyCraftingFlagArray[i];
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
			NBTTagList lst = nbttagcompound.getTagList("fuzzyFlags", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < 9; i++) {
				NBTTagCompound comp = lst.getCompoundTagAt(i);
				fuzzyCraftingFlagArray[i].ignore_dmg = comp.getBoolean("ignore_dmg");
				fuzzyCraftingFlagArray[i].ignore_nbt = comp.getBoolean("ignore_nbt");
				fuzzyCraftingFlagArray[i].use_od = comp.getBoolean("use_od");
				fuzzyCraftingFlagArray[i].use_category = comp.getBoolean("use_category");
			}
		}
		if (nbttagcompound.hasKey("outputFuzzyFlags")) {
			NBTTagCompound comp = nbttagcompound.getCompoundTag("outputFuzzyFlags");
			outputFuzzyFlags.ignore_dmg = comp.getBoolean("ignore_dmg");
			outputFuzzyFlags.ignore_nbt = comp.getBoolean("ignore_nbt");
			outputFuzzyFlags.use_od = comp.getBoolean("use_od");
			outputFuzzyFlags.use_category = comp.getBoolean("use_category");
		}
		for (int i = 0; i < 6; i++) {
			craftingSigns[i] = nbttagcompound.getBoolean("craftingSigns" + i);
		}

		for (int i = 0; i < ItemUpgrade.MAX_LIQUID_CRAFTER; i++) {
			String liquidSatelliteUUIDArrayString = nbttagcompound.getString("liquidSatelliteUUIDArray" + i);
			liquidSatelliteUUIDArray[i] = liquidSatelliteUUIDArrayString.isEmpty() ? null : UUID.fromString(liquidSatelliteUUIDArrayString);
		}
		if (nbttagcompound.hasKey("FluidAmount")) {
			amount = nbttagcompound.getIntArray("FluidAmount");
		}
		if (amount.length < ItemUpgrade.MAX_LIQUID_CRAFTER) {
			amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
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
			for (int i = 0; i < ItemUpgrade.MAX_LIQUID_CRAFTER; i++) {
				updateSatelliteFromIDs.liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
			}
			updateSatelliteFromIDs.liquidSatelliteId = nbttagcompound.getInteger("liquidSatelliteId");
		}
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		//	super.writeToNBT(nbttagcompound);
		_dummyInventory.writeToNBT(nbttagcompound, "");
		_liquidInventory.writeToNBT(nbttagcompound, "FluidInv");
		_cleanupInventory.writeToNBT(nbttagcompound, "CleanupInv");

		nbttagcompound.setString("satelliteUUID", satelliteUUID == null ? "" : satelliteUUID.toString());

		nbttagcompound.setInteger("priority", priority);
		for (int i = 0; i < 9; i++) {
			nbttagcompound.setString("advancedSatelliteUUID" + i, advancedSatelliteUUIDArray[i] == null ? "" : advancedSatelliteUUIDArray[i].toString());
		}
		NBTTagList lst = new NBTTagList();
		for (int i = 0; i < 9; i++) {
			NBTTagCompound comp = new NBTTagCompound();
			comp.setBoolean("ignore_dmg", fuzzyCraftingFlagArray[i].ignore_dmg);
			comp.setBoolean("ignore_nbt", fuzzyCraftingFlagArray[i].ignore_nbt);
			comp.setBoolean("use_od", fuzzyCraftingFlagArray[i].use_od);
			comp.setBoolean("use_category", fuzzyCraftingFlagArray[i].use_category);
			lst.appendTag(comp);
		}
		nbttagcompound.setTag("fuzzyFlags", lst);
		{
			NBTTagCompound comp = new NBTTagCompound();
			comp.setBoolean("ignore_dmg", outputFuzzyFlags.ignore_dmg);
			comp.setBoolean("ignore_nbt", outputFuzzyFlags.ignore_nbt);
			comp.setBoolean("use_od", outputFuzzyFlags.use_od);
			comp.setBoolean("use_category", outputFuzzyFlags.use_category);
			nbttagcompound.setTag("outputFuzzyFlags", comp);
		}
		for (int i = 0; i < 6; i++) {
			nbttagcompound.setBoolean("craftingSigns" + i, craftingSigns[i]);
		}
		for (int i = 0; i < ItemUpgrade.MAX_LIQUID_CRAFTER; i++) {
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
		int simpleId = SimpleServiceLocator.routerManager.getIDforUUID(id);
		IRouter router = SimpleServiceLocator.routerManager.getRouter(simpleId);
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
		if (MainProxy.isClient(_world.getWorld())) {
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
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(CraftingModuleSlot.class).setAdvancedSat(getUpgradeManager().isAdvancedSatelliteCrafter()).setLiquidCrafter(getUpgradeManager().getFluidCrafter()).setAmount(amount).setHasByproductExtractor(getUpgradeManager().hasByproductExtractor()).setFuzzy(
				getUpgradeManager().isFuzzyUpgrade())
				.setCleanupSize(getUpgradeManager().getCrafterCleanup()).setCleanupExclude(cleanupModeIsExclude);
	}

	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
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

	public void setDummyInventorySlot(int slot, @Nonnull ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}

	public void importFromCraftingTable(EntityPlayer player) {
		if (MainProxy.isClient(_world.getWorld())) {
			// Send packet asking for import
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImport.class).setModulePos(this);
			MainProxy.sendPacketToServer(packet);
		} else {
			WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(_world.getWorld(), getBlockPos());

			for (NeighborTileEntity adjacent : worldCoordinates.connectedTileEntities(ConnectionPipeType.ITEM).collect(Collectors.toList())) {
				for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
					if (provider.importRecipe(adjacent.getTileEntity(), _dummyInventory)) {
						if (provider instanceof IFuzzyRecipeProvider) {
							((IFuzzyRecipeProvider) provider).importFuzzyFlags(adjacent.getTileEntity(), _dummyInventory, fuzzyCraftingFlagArray, outputFuzzyFlags);
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
			MainProxy.sendPacketToAllWatchingChunk(this, packet);
		}
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
		if (MainProxy.isClient(_world.getWorld())) {
			amount[slot] = integer;
		}
	}

	public int[] getFluidAmount() {
		return amount;
	}

	public void setFluidAmount(int[] amount) {
		if (MainProxy.isClient(_world.getWorld())) {
			this.amount = amount;
		}
	}

	private IRouter getFluidSatelliteRouter(int x) {
		final UUID liquidSatelliteUUID = x == -1 ? this.liquidSatelliteUUID : liquidSatelliteUUIDArray[x];
		final int satelliteRouterId = SimpleServiceLocator.routerManager.getIDforUUID(liquidSatelliteUUID);
		return SimpleServiceLocator.routerManager.getRouter(satelliteRouterId);
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

		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(_world.getWorld(), getBlockPos());

		worldCoordinates.connectedTileEntities(ConnectionPipeType.ITEM).anyMatch(adjacent -> {
			boolean found = SimpleServiceLocator.craftingRecipeProviders.stream()
					.anyMatch(provider -> provider.canOpenGui(adjacent.getTileEntity()));

			if (!found) {
				found = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(adjacent) != null;
			}

			if (found) {
				final BlockPos pos = adjacent.getTileEntity().getPos();
				IBlockState blockState = _world.getWorld().getBlockState(pos);
				return !blockState.getBlock().isAir(blockState, _world.getWorld(), pos) && blockState.getBlock()
						.onBlockActivated(_world.getWorld(), pos, adjacent.getTileEntity().getWorld().getBlockState(pos),
								player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);
			}
			return false;
		});
		player.inventory.currentItem = savedEquipped;
	}

	public void enabledUpdateEntity() {
		if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
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

		if ((!_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA))) {
			final ISlotUpgradeManager upgradeManager = Objects.requireNonNull(getUpgradeManager());
			if (upgradeManager.getCrafterCleanup() > 0) {
				final List<NeighborTileEntity<TileEntity>> crafters = locateCraftersForExtraction();
				ItemStack extracted = ItemStack.EMPTY;
				for (NeighborTileEntity<TileEntity> adjacentCrafter : crafters) {
					extracted = extractFiltered(adjacentCrafter, _cleanupInventory, cleanupModeIsExclude, upgradeManager.getCrafterCleanup() * 3);
					if (!extracted.isEmpty()) {
						break;
					}
				}
				if (!extracted.isEmpty()) {
					_service.queueRoutedItem(SimpleServiceLocator.routedItemHelper.createNewTravelItem(extracted), EnumFacing.UP);
					_service.getCacheHolder().trigger(CacheTypes.Inventory);
				}
			}
			return;
		}

		waitingForCraft = true;

		List<NeighborTileEntity<TileEntity>> adjacentCrafters = locateCraftersForExtraction();
		if (adjacentCrafters.size() < 1) {
			if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
				_service.getItemOrderManager().sendFailed();
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
		while (itemsleft > 0 && stacksleft > 0 && (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA))) {
			LogisticsItemOrder nextOrder = _service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA); // fetch but not remove.
			int maxtosend = Math.min(itemsleft, nextOrder.getResource().stack.getStackSize());
			maxtosend = Math.min(nextOrder.getResource().getItem().getMaxStackSize(), maxtosend);
			// retrieve the new crafted items
			ItemStack extracted = ItemStack.EMPTY;
			NeighborTileEntity<TileEntity> adjacent = null; // there has to be at least one adjacentCrafter at this point; adjacent wont stay null
			for (NeighborTileEntity<TileEntity> adjacentCrafter : adjacentCrafters) {
				adjacent = adjacentCrafter;
				extracted = extract(adjacent, nextOrder.getResource(), maxtosend);
				if (!extracted.isEmpty()) {
					break;
				}
			}
			if (extracted.isEmpty()) {
				_service.getItemOrderManager().deferSend();
				break;
			}
			_service.getCacheHolder().trigger(CacheTypes.Inventory);
			Objects.requireNonNull(adjacent);
			lastAccessedCrafter = new WeakReference<>(adjacent.getTileEntity());
			// send the new crafted items to the destination
			ItemIdentifier extractedID = ItemIdentifier.get(extracted);
			while (!extracted.isEmpty()) {
				if (!doesExtractionMatch(nextOrder, extractedID)) {
					LogisticsItemOrder startOrder = nextOrder;
					if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
						do {
							_service.getItemOrderManager().deferSend();
							nextOrder = _service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA);
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
						//Route the unhandled item

						_service.sendStack(stackToSend, -1, ItemSendMode.Normal, null);
						continue;
					}
				}
				int numtosend = Math.min(extracted.getCount(), extractedID.getMaxStackSize());
				numtosend = Math.min(numtosend, nextOrder.getResource().stack.getStackSize());
				if (numtosend == 0) {
					break;
				}
				stacksleft -= 1;
				itemsleft -= numtosend;
				ItemStack stackToSend = extracted.splitStack(numtosend);
				if (nextOrder.getDestination() != null) {
					SinkReply reply = LogisticsManager.canSink(stackToSend, nextOrder.getDestination().getRouter(), null, true, ItemIdentifier.get(stackToSend), null, true, false);
					boolean defersend = false;
					if (reply == null || reply.bufferMode != BufferMode.NONE || reply.maxNumberOfItems < 1) {
						defersend = true;
					}
					IRoutedItem item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stackToSend);
					item.setDestination(nextOrder.getDestination().getRouter().getSimpleID());
					item.setTransportMode(TransportMode.Active);
					item.setAdditionalTargetInformation(nextOrder.getInformation());
					_service.queueRoutedItem(item, adjacent.getDirection());
					_service.getItemOrderManager().sendSuccessfull(stackToSend.getCount(), defersend, item);
				} else {
					_service.sendStack(stackToSend, -1, ItemSendMode.Normal, nextOrder.getInformation());
					_service.getItemOrderManager().sendSuccessfull(stackToSend.getCount(), false, null);
				}
				if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
					nextOrder = _service.getItemOrderManager().peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA); // fetch but not remove.
				}
			}
		}

	}

	private boolean doesExtractionMatch(LogisticsItemOrder nextOrder, ItemIdentifier extractedID) {
		return nextOrder.getResource().getItem().equals(extractedID) || (this.getUpgradeManager().isFuzzyUpgrade() && nextOrder.getResource().getBitSet().nextSetBit(0) != -1 && nextOrder.getResource().matches(extractedID, IResource.MatchSettings.NORMAL));
	}

	public boolean areAllOrderesToBuffer() {
		return cachedAreAllOrderesToBuffer;
	}

	public void cacheAreAllOrderesToBuffer() {
		boolean result = true;
		for (LogisticsItemOrder order : _service.getItemOrderManager()) {
			if (order.getDestination() instanceof IItemSpaceControl) {
				SinkReply reply = LogisticsManager.canSink(order.getResource().stack.makeNormalStack(), order.getDestination().getRouter(), null, true, order.getResource().getItem(), null, true, false);
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

	@Nonnull
	private ItemStack extract(NeighborTileEntity<TileEntity> adjacent, IResource item, int amount) {
		return adjacent.getJavaInstanceOf(LogisticsCraftingTableTileEntity.class)
				.map(adjacentCraftingTable -> extractFromLogisticsCraftingTable(adjacentCraftingTable, item, amount))
				.orElseGet(() -> adjacent.isItemHandler() ? extractFromInventory(adjacent.getUtilForItemHandler(), item, amount) : ItemStack.EMPTY);
	}

	@Nonnull
	private ItemStack extractFiltered(NeighborTileEntity<TileEntity> adjacent, ItemIdentifierInventory inv, boolean isExcluded, int filterInvLimit) {
		return adjacent.isItemHandler() ? extractFromInventoryFiltered(adjacent.getUtilForItemHandler(), inv, isExcluded, filterInvLimit) : ItemStack.EMPTY;
	}

	@Nonnull
	private ItemStack extractFromInventory(@Nonnull IInventoryUtil invUtil, IResource wanteditem, int count) {
		ItemIdentifier itemToExtract = null;
		if (wanteditem instanceof ItemResource) {
			itemToExtract = ((ItemResource) wanteditem).getItem();
		} else if (wanteditem instanceof DictResource) {
			int max = Integer.MIN_VALUE;
			ItemIdentifier toExtract = null;
			for (Map.Entry<ItemIdentifier, Integer> content : invUtil.getItemsAndCount().entrySet()) {
				if (wanteditem.matches(content.getKey(), IResource.MatchSettings.NORMAL)) {
					if (content.getValue() > max) {
						max = content.getValue();
						toExtract = content.getKey();
					}
				}
			}
			if (toExtract == null) {
				return ItemStack.EMPTY;
			}
			itemToExtract = toExtract;
		}
		int available = invUtil.itemCount(itemToExtract);
		if (available == 0 || !_service.canUseEnergy(neededEnergy() * Math.min(count, available))) {
			return ItemStack.EMPTY;
		}
		ItemStack extracted = invUtil.getMultipleItems(itemToExtract, Math.min(count, available));
		_service.useEnergy(neededEnergy() * extracted.getCount());
		return extracted;
	}

	@Nonnull
	private ItemStack extractFromInventoryFiltered(@Nonnull IInventoryUtil invUtil, ItemIdentifierInventory filter, boolean isExcluded, int filterInvLimit) {
		ItemIdentifier wanteditem = null;
		boolean found = false;
		for (ItemIdentifier item : invUtil.getItemsAndCount().keySet()) {
			found = isFiltered(filter, filterInvLimit, item, found);
			if (isExcluded != found) {
				wanteditem = item;
				break;
			}
		}
		if (wanteditem == null) {
			return ItemStack.EMPTY;
		}
		int available = invUtil.itemCount(wanteditem);
		if (available == 0 || !_service.canUseEnergy(neededEnergy() * Math.min(64, available))) {
			return ItemStack.EMPTY;
		}
		ItemStack extracted = invUtil.getMultipleItems(wanteditem, Math.min(64, available));
		_service.useEnergy(neededEnergy() * extracted.getCount());
		return extracted;
	}

	private boolean isFiltered(ItemIdentifierInventory filter, int filterInvLimit, ItemIdentifier item, boolean found) {
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
		return found;
	}

	@Nonnull
	private ItemStack extractFromLogisticsCraftingTable(
			NeighborTileEntity<LogisticsCraftingTableTileEntity> adjacentCraftingTable,
			IResource wanteditem, int count) {
		ItemStack extracted = extractFromInventory(
				Objects.requireNonNull(adjacentCraftingTable.getInventoryUtil()),
				wanteditem, count);
		if (!extracted.isEmpty()) {
			return extracted;
		}
		ItemStack retstack = ItemStack.EMPTY;
		while (count > 0) {
			ItemStack stack = adjacentCraftingTable.getTileEntity().getOutput(wanteditem, _service);
			if (stack.isEmpty()) {
				break;
			}
			if (retstack.isEmpty()) {
				if (!wanteditem.matches(ItemIdentifier.get(stack), wanteditem instanceof ItemResource ? IResource.MatchSettings.WITHOUT_NBT : IResource.MatchSettings.NORMAL)) {
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
			if (!_service.useEnergy(neededEnergy() * stack.getCount())) {
				break;
			}

			if (retstack.isEmpty()) {
				retstack = stack;
			} else {
				retstack.grow(stack.getCount());
			}
			count -= stack.getCount();
			if (Objects.requireNonNull(getUpgradeManager()).isFuzzyUpgrade()) {
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

	public List<NeighborTileEntity<TileEntity>> locateCraftersForExtraction() {
		if (cachedCrafters == null) {
			cachedCrafters = new WorldCoordinatesWrapper(_world.getWorld(), getBlockPos())
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
			_cleanupInventory.setInventorySlotContents(i, ItemStack.EMPTY);
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
	public void guiOpenedByPlayer(EntityPlayer player) {
		guiWatcher.add(player);
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
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
		public int[] liquidSatelliteIdArray = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
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
