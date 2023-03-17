package logisticspipes.modules;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;

import com.google.common.collect.ImmutableList;
import lombok.Getter;

import logisticspipes.LogisticsPipes;
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
import logisticspipes.network.packets.pipe.CraftingPipeUpdatePacket;
import logisticspipes.network.packets.pipe.FluidCraftingAmount;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
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
import network.rs485.logisticspipes.connection.AdjacentUtilKt;
import network.rs485.logisticspipes.connection.LPNeighborTileEntityKt;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.property.BitSetProperty;
import network.rs485.logisticspipes.property.BooleanProperty;
import network.rs485.logisticspipes.property.IBitSet;
import network.rs485.logisticspipes.property.IntListProperty;
import network.rs485.logisticspipes.property.IntegerProperty;
import network.rs485.logisticspipes.property.InventoryProperty;
import network.rs485.logisticspipes.property.Property;
import network.rs485.logisticspipes.property.UUIDListProperty;
import network.rs485.logisticspipes.property.UUIDProperty;
import network.rs485.logisticspipes.property.UUIDPropertyKt;
import network.rs485.logisticspipes.util.FuzzyUtil;

public class ModuleCrafter extends LogisticsModule
		implements ICraftItems, IHUDModuleHandler, IModuleWatchReciver, IGuiOpenControler, Gui {

	// TODO: after 1.12.2 add a tagKey
	public final InventoryProperty dummyInventory = new InventoryProperty(
			new ItemIdentifierInventory(11, "Requested items", 127), "");
	public final InventoryProperty liquidInventory = new InventoryProperty(
			new ItemIdentifierInventory(ItemUpgrade.MAX_LIQUID_CRAFTER, "Fluid items", 1, true), "FluidInv");
	public final InventoryProperty cleanupInventory = new InventoryProperty(
			new ItemIdentifierInventory(ItemUpgrade.MAX_CRAFTING_CLEANUP * 3, "Cleanup Filter Items", 1), "CleanupInv");
	public final UUIDProperty satelliteUUID = new UUIDProperty(null, "satelliteUUID");
	public final UUIDListProperty advancedSatelliteUUIDList = new UUIDListProperty("advancedSatelliteUUIDList");
	public final UUIDProperty liquidSatelliteUUID = new UUIDProperty(null, "liquidSatelliteId");
	public final UUIDListProperty liquidSatelliteUUIDList = new UUIDListProperty("liquidSatelliteUUIDList");
	public final IntegerProperty priority = new IntegerProperty(0, "priority");
	public final IntListProperty liquidAmounts = new IntListProperty("FluidAmount");
	public final BooleanProperty cleanupModeIsExclude = new BooleanProperty(true, "cleanupModeIsExclude");
	public final BitSetProperty fuzzyFlags = new BitSetProperty(new BitSet(4 * (9 + 1)), "fuzzyBitSet");
	private final List<Property<?>> properties = ImmutableList.<Property<?>>builder()
			.add(dummyInventory)
			.add(liquidInventory)
			.add(cleanupInventory)
			.add(satelliteUUID)
			.add(advancedSatelliteUUIDList)
			.add(liquidSatelliteUUID)
			.add(liquidSatelliteUUIDList)
			.add(priority)
			.add(liquidAmounts)
			.add(cleanupModeIsExclude)
			.add(fuzzyFlags)
			.build();

	// for reliable transport
	protected final DelayQueue<DelayedGeneric<Pair<ItemIdentifierStack, IAdditionalTargetInformation>>> _lostItems = new DelayQueue<>();
	protected final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	protected final PlayerCollectionList guiWatcher = new PlayerCollectionList();

	public ClientSideSatelliteNames clientSideSatelliteNames = new ClientSideSatelliteNames();

	protected SinkReply _sinkReply;

	@Nullable
	private IRequestItems _invRequester;
	private WeakReference<TileEntity> lastAccessedCrafter = new WeakReference<>(null);
	private boolean cachedAreAllOrderesToBuffer;
	private UpgradeSatelliteFromIDs updateSatelliteFromIDs = null;

	public ModuleCrafter() {
		advancedSatelliteUUIDList.ensureSize(9);
		liquidAmounts.ensureSize(ItemUpgrade.MAX_LIQUID_CRAFTER);
		liquidSatelliteUUIDList.ensureSize(ItemUpgrade.MAX_LIQUID_CRAFTER);
	}

	public static String getName() {
		return "crafter";
	}

	@Nonnull
	@Override
	public String getLPName() {
		return getName();
	}

	@Nonnull
	@Override
	public List<Property<?>> getProperties() {
		return properties;
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
	public void registerPosition(@Nonnull ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0,
				new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal()
				&& bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		final int itemCount = spaceFor(stack, item, includeInTransit);
		if (itemCount > 0) {
			return new SinkReply(_sinkReply, itemCount,
					areAllOrderesToBuffer() ? BufferMode.DESTINATION_BUFFERED : BufferMode.NONE);
		} else {
			return null;
		}
	}

	protected int spaceFor(@Nonnull ItemStack stack, ItemIdentifier item, boolean includeInTransit) {
		Pair<String, ItemIdentifier> key = new Pair<>("spaceFor", item);
		final IPipeServiceProvider service = _service;
		if (service == null) return 0;
		Object cache = service.getCacheHolder().getCacheFor(CacheTypes.Inventory, key);
		int onRoute = 0;
		if (includeInTransit) {
			onRoute = service.countOnRoute(item);
		}
		if (cache != null) {
			return ((Integer) cache) - onRoute;
		}

		if (includeInTransit) {
			stack = stack.copy();
			stack.grow(onRoute);
		}
		final ISlotUpgradeManager upgradeManager = Objects.requireNonNull(getUpgradeManager());
		final ItemStack finalStack = stack;
		final Integer count = AdjacentUtilKt.sneakyInventoryUtils(service.getAvailableAdjacent(), upgradeManager)
				.stream().map(invUtil -> invUtil.roomForItem(finalStack)).reduce(Integer::sum).orElse(0);

		service.getCacheHolder().setCache(CacheTypes.Inventory, key, count);
		return count - onRoute;
	}

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
		final IPipeServiceProvider service = _service;
		if (service == null) return;
		enabledUpdateEntity();
		if (updateSatelliteFromIDs != null && service.isNthTick(100)) {
			if (updateSatelliteFromIDs.advancedSatelliteIdArray != null) {
				boolean canBeRemoved = true;
				for (int i = 0; i < updateSatelliteFromIDs.advancedSatelliteIdArray.length; i++) {
					if (updateSatelliteFromIDs.advancedSatelliteIdArray[i] != -1) {
						UUID uuid = getUUIDForSatelliteName(
								Integer.toString(updateSatelliteFromIDs.advancedSatelliteIdArray[i]));
						if (uuid != null) {
							updateSatelliteFromIDs.advancedSatelliteIdArray[i] = -1;
							advancedSatelliteUUIDList.set(i, uuid);
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
						UUID uuid = getUUIDForFluidSatelliteName(
								Integer.toString(updateSatelliteFromIDs.liquidSatelliteIdArray[i]));
						if (uuid != null) {
							updateSatelliteFromIDs.liquidSatelliteIdArray[i] = -1;
							liquidSatelliteUUIDList.set(i, uuid);
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
					liquidSatelliteUUID.setValue(uuid);
				}
			}
			if (updateSatelliteFromIDs.satelliteId != -1) {
				UUID uuid = getUUIDForFluidSatelliteName(Integer.toString(updateSatelliteFromIDs.satelliteId));
				if (uuid != null) {
					updateSatelliteFromIDs.satelliteId = -1;
					satelliteUUID.setValue(uuid);
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
			if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
				SinkReply reply = LogisticsManager.canSink(pair.getValue1().makeNormalStack(), getRouter(), null, true,
						pair.getValue1().getItem(), null, true, true, false);
				if (reply == null || reply.maxNumberOfItems < 1) {
					_lostItems.add(new DelayedGeneric<>(pair, 9000 + (int) (Math.random() * 2000)));
					lostItem = _lostItems.poll();
					continue;
				}
			}
			int received = RequestTree.requestPartial(pair.getValue1(), (CoreRoutedPipe) service, pair.getValue2());
			rerequested++;
			if (received < pair.getValue1().getStackSize()) {
				pair.getValue1().setStackSize(pair.getValue1().getStackSize() - received);
				_lostItems.add(new DelayedGeneric<>(pair, 4500 + (int) (Math.random() * 1000)));
			}
			lostItem = _lostItems.poll();
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {
	}

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
		final IPipeServiceProvider service = _service;
		if (service == null) return;
		if (!service.getItemOrderManager().hasExtras() || tree.hasBeenQueried(service.getItemOrderManager())) {
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
		for (LogisticsItemOrder extra : service.getItemOrderManager()) {
			if (extra.getType() == ResourceType.EXTRA) {
				if (extra.getResource().getItem().equals(requestedItem.getAsItem())) {
					remaining += extra.getResource().stack.getStackSize();
				}
			}
		}
		final ItemIdentifierStack craftedItem = getCraftedItem();
		if (craftedItem == null) return;
		remaining -= root.getAllPromissesFor(this, craftedItem.getItem());
		if (remaining < 1) {
			return;
		}
		if (getUpgradeManager().isFuzzyUpgrade() && outputFuzzy().nextSetBit(0) != -1) {
			DictResource dict = new DictResource(craftedItem, null).loadFromBitSet(outputFuzzy().copyValue());
			LogisticsExtraDictPromise promise = new LogisticsExtraDictPromise(dict,
					Math.min(remaining, tree.getMissingAmount()), this, true);
			tree.addPromise(promise);
		} else {
			LogisticsExtraPromise promise = new LogisticsExtraPromise(craftedItem.getItem(),
					Math.min(remaining, tree.getMissingAmount()), this, true);
			tree.addPromise(promise);
		}
		tree.setQueried(service.getItemOrderManager());
	}

	@Override
	public LogisticsItemOrder fullFill(LogisticsPromise promise, IRequestItems destination,
			IAdditionalTargetInformation info) {
		final IPipeServiceProvider service = _service;
		if (service == null) return null;
		if (promise instanceof LogisticsExtraDictPromise) {
			service.getItemOrderManager().removeExtras(((LogisticsExtraDictPromise) promise).getResource());
		}
		if (promise instanceof LogisticsExtraPromise) {
			service.getItemOrderManager()
					.removeExtras(new DictResource(new ItemIdentifierStack(promise.item, promise.numberOfItems), null));
		}
		if (promise instanceof LogisticsDictPromise) {
			service.spawnParticle(Particles.WhiteParticle, 2);
			return service.getItemOrderManager()
					.addOrder(((LogisticsDictPromise) promise).getResource(), destination, ResourceType.CRAFTING, info);
		}
		service.spawnParticle(Particles.WhiteParticle, 2);
		return service.getItemOrderManager()
				.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination,
						ResourceType.CRAFTING, info);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list, List<IFilter> filter) {
	}

	@Override
	@Nonnull
	public IRouter getRouter() {
		return Objects.requireNonNull(_service, "service was null").getRouter();
	}

	@Override
	public void itemCouldNotBeSend(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		Objects.requireNonNull(_invRequester).itemCouldNotBeSend(item, info);
	}

	@Override
	public int compareTo(@Nonnull IRequestItems other) {
		return Integer.compare(getID(), other.getID());
	}

	@Override
	public int getID() {
		final IPipeServiceProvider service = _service;
		if (service == null) return -1;
		return service.getRouter().getSimpleID();
	}

	@Override
	public void registerExtras(IPromise promise) {
		final IPipeServiceProvider service = _service;
		if (service == null) return;
		if (promise instanceof LogisticsDictPromise) {
			service.getItemOrderManager().addExtra(((LogisticsDictPromise) promise).getResource());
		} else {
			ItemIdentifierStack stack = new ItemIdentifierStack(promise.getItemType(), promise.getAmount());
			service.getItemOrderManager().addExtra(new DictResource(stack, null));
		}
	}

	@Override
	public ICraftingTemplate addCrafting(IResource toCraft) {

		List<ItemIdentifierStack> stack = getCraftedItems();
		if (stack == null) {
			return null;
		}
		IReqCraftingTemplate template = null;
		if (getUpgradeManager().isFuzzyUpgrade() && outputFuzzy().nextSetBit(0) != -1) {
			for (ItemIdentifierStack craftable : stack) {
				DictResource dict = new DictResource(craftable, null);
				dict.loadFromBitSet(outputFuzzy().copyValue());
				if (toCraft.matches(dict, IResource.MatchSettings.NORMAL)) {
					template = new DictCraftingTemplate(dict, this, priority.getValue());
					break;
				}
			}
		} else {
			for (ItemIdentifierStack craftable : stack) {
				if (toCraft.matches(craftable.getItem(), IResource.MatchSettings.NORMAL)) {
					template = new ItemCraftingTemplate(craftable, this, priority.getValue());
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

		if (!isSatelliteConnected()) {
			// has a satellite configured and that one is unreachable
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
			ItemIdentifierStack resourceStack = dummyInventory.getIDStackInSlot(i);
			if (resourceStack == null || resourceStack.getStackSize() == 0) {
				continue;
			}
			IResource req;
			if (getUpgradeManager().isFuzzyUpgrade() && inputFuzzy(i).nextSetBit(0) != -1) {
				DictResource dict;
				req = dict = new DictResource(resourceStack, target[i]);
				dict.loadFromBitSet(inputFuzzy(i).copyValue());
			} else {
				req = new ItemResource(resourceStack, target[i]);
			}
			template.addRequirement(req, new CraftingChassisInformation(i, getPositionInt()));
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
			int amount = liquidAmounts.get(i);
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
			if (satelliteUUID.isZero()) {
				return true;
			}
			int satelliteRouterId = SimpleServiceLocator.routerManager.getIDforUUID(satelliteUUID.getValue());
			if (satelliteRouterId != -1) {
				List<ExitRoute> rt = getRouter().getRouteTable().get(satelliteRouterId);
				return rt != null && !rt.isEmpty();
			}
		} else {
			boolean foundAll = true;
			for (int i = 0; i < 9; i++) {
				boolean foundOne = false;
				if (advancedSatelliteUUIDList.isZero(i)) {
					continue;
				}

				int satelliteRouterId = SimpleServiceLocator.routerManager
						.getIDforUUID(advancedSatelliteUUIDList.get(i));
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

	@Deprecated
	@Override
	public List<ItemIdentifierStack> getCraftedItems() {
		List<ItemIdentifierStack> list = new ArrayList<>(1);
		if (getCraftedItem() != null) {
			list.add(getCraftedItem());
		}
		return list;
	}

	@Nullable
	public ItemIdentifierStack getCraftedItem() {
		return dummyInventory.getIDStackInSlot(9);
	}

	@Override
	public int getTodo() {
		final IPipeServiceProvider service = _service;
		if (service == null) return 0;
		return service.getItemOrderManager().totalAmountCountInAllOrders();
	}

	private IRouter getSatelliteRouter(int x) {
		final UUID satelliteUUID = x == -1 ? this.satelliteUUID.getValue() : advancedSatelliteUUIDList.get(x);
		final int satelliteRouterId = SimpleServiceLocator.routerManager.getIDforUUID(satelliteUUID);
		return SimpleServiceLocator.routerManager.getRouter(satelliteRouterId);
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound tag) {
		super.readFromNBT(tag);

		// FIXME: remove after 1.12
		for (int i = 0; i < 9; i++) {
			String advancedSatelliteUUIDArrayString = tag.getString("advancedSatelliteUUID" + i);
			if (!advancedSatelliteUUIDArrayString.isEmpty()) {
				advancedSatelliteUUIDList.set(i, UUID.fromString(advancedSatelliteUUIDArrayString));
			}
		}

		// FIXME: remove after 1.12
		for (int i = 0; i < ItemUpgrade.MAX_LIQUID_CRAFTER; i++) {
			String liquidSatelliteUUIDArrayString = tag.getString("liquidSatelliteUUIDArray" + i);
			if (!liquidSatelliteUUIDArrayString.isEmpty()) {
				liquidSatelliteUUIDList.set(i, UUID.fromString(liquidSatelliteUUIDArrayString));
			}
		}

		// FIXME: remove after 1.12
		if (tag.hasKey("fuzzyFlags")) {
			NBTTagList lst = tag.getTagList("fuzzyFlags", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < 9; i++) {
				FuzzyUtil.INSTANCE.readFromNBT(inputFuzzy(i), lst.getCompoundTagAt(i));
			}
		}
		// FIXME: remove after 1.12
		if (tag.hasKey("outputFuzzyFlags")) {
			FuzzyUtil.INSTANCE.readFromNBT(outputFuzzy(), tag.getCompoundTag("outputFuzzyFlags"));
		}

		// FIXME: remove after 1.12
		if (tag.hasKey("satelliteid")) {
			updateSatelliteFromIDs = new UpgradeSatelliteFromIDs();
			updateSatelliteFromIDs.satelliteId = tag.getInteger("satelliteid");
			for (int i = 0; i < 9; i++) {
				updateSatelliteFromIDs.advancedSatelliteIdArray[i] = tag.getInteger("advancedSatelliteId" + i);
			}
			for (int i = 0; i < ItemUpgrade.MAX_LIQUID_CRAFTER; i++) {
				updateSatelliteFromIDs.liquidSatelliteIdArray[i] = tag.getInteger("liquidSatelliteIdArray" + i);
			}
			updateSatelliteFromIDs.liquidSatelliteId = tag.getInteger("liquidSatelliteId");
		}
	}

	public IBitSet outputFuzzy() {
		final int startIdx = 4 * 9; // after the 9th slot
		return fuzzyFlags.get(startIdx, startIdx + 3);
	}

	public IBitSet inputFuzzy(int slot) {
		final int startIdx = 4 * slot;
		return fuzzyFlags.get(startIdx, startIdx + 3);
	}

	public ModernPacket getCPipePacket() {
		return PacketHandler.getPacket(CraftingPipeUpdatePacket.class).setAmount(liquidAmounts.getArray())
				.setLiquidSatelliteNameArray(getSatelliteNamesForUUIDs(liquidSatelliteUUIDList))
				.setLiquidSatelliteName(getSatelliteNameForUUID(liquidSatelliteUUID.getValue()))
				.setSatelliteName(getSatelliteNameForUUID(satelliteUUID.getValue()))
				.setAdvancedSatelliteNameArray(getSatelliteNamesForUUIDs(advancedSatelliteUUIDList))
				.setPriority(priority.getValue()).setModulePos(this);
	}

	private String getSatelliteNameForUUID(UUID uuid) {
		if (UUIDPropertyKt.isZero(uuid)) {
			return "";
		}
		int simpleId = SimpleServiceLocator.routerManager.getIDforUUID(uuid);
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

	private String[] getSatelliteNamesForUUIDs(UUIDListProperty list) {
		return list.stream().map(this::getSatelliteNameForUUID).toArray(String[]::new);
	}

	public void handleCraftingUpdatePacket(CraftingPipeUpdatePacket packet) {
		if (MainProxy.isClient(getWorld())) {
			liquidAmounts.replaceContent(packet.getAmount());
			clientSideSatelliteNames.liquidSatelliteNameArray = packet.getLiquidSatelliteNameArray();
			clientSideSatelliteNames.liquidSatelliteName = packet.getLiquidSatelliteName();
			clientSideSatelliteNames.satelliteName = packet.getSatelliteName();
			clientSideSatelliteNames.advancedSatelliteNameArray = packet.getAdvancedSatelliteNameArray();
			priority.setValue(packet.getPriority());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(CraftingModuleSlot.class)
				.setAdvancedSat(getUpgradeManager().isAdvancedSatelliteCrafter())
				.setLiquidCrafter(getUpgradeManager().getFluidCrafter())
				.setAmount(liquidAmounts.getArray())
				.setHasByproductExtractor(getUpgradeManager().hasByproductExtractor())
				.setFuzzy(getUpgradeManager().isFuzzyUpgrade())
				.setCleanupSize(getUpgradeManager().getCrafterCleanup())
				.setCleanupExclude(cleanupModeIsExclude.getValue());
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(CraftingModuleInHand.class).setAmount(liquidAmounts.getArray())
				.setCleanupExclude(cleanupModeIsExclude.getValue());
	}

	public void importFromCraftingTable(@Nullable EntityPlayer player) {
		if (MainProxy.isClient(getWorld())) {
			// Send packet asking for import
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImport.class).setModulePos(this);
			MainProxy.sendPacketToServer(packet);
		} else {
			final IPipeServiceProvider service = _service;
			if (service == null) return;
			service.getAvailableAdjacent().neighbors().keySet().stream().flatMap(
					neighbor -> SimpleServiceLocator.craftingRecipeProviders.stream()
							.filter(provider -> provider.importRecipe(neighbor.getTileEntity(), dummyInventory))
							.map(provider1 -> new Pair<>(neighbor, provider1))).findFirst()
					.ifPresent(neighborProviderPair -> {
						if (neighborProviderPair.getValue2() instanceof IFuzzyRecipeProvider) {
							((IFuzzyRecipeProvider) neighborProviderPair.getValue2())
									.importFuzzyFlags(neighborProviderPair.getValue1().getTileEntity(),
											dummyInventory.getSlotAccess(), fuzzyFlags);
						}
					});

			// Send inventory as packet
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImportBack.class)
					.setInventory(dummyInventory).setModulePos(this);
			if (player != null) {
				MainProxy.sendPacketToPlayer(packet, player);
			}
			MainProxy.sendPacketToAllWatchingChunk(this, packet);
		}
	}

	public ItemIdentifierStack getByproductItem() {
		return dummyInventory.getIDStackInSlot(10);
	}

	public FluidIdentifier getFluidMaterial(int slotnr) {
		ItemIdentifierStack stack = liquidInventory.getIDStackInSlot(slotnr);
		if (stack == null) {
			return null;
		}
		return FluidIdentifier.get(stack.getItem());
	}

	public void changeFluidAmount(int change, int slot, EntityPlayer player) {
		if (MainProxy.isClient(player.world)) {
			MainProxy.sendPacketToServer(
					PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(change)
							.setModulePos(this));
		} else {
			liquidAmounts.increase(slot, change);
			if (liquidAmounts.get(slot) <= 0) {
				liquidAmounts.set(slot, 0);
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot)
					.setInteger(liquidAmounts.get(slot)).setModulePos(this), player);
		}
	}

	private IRouter getFluidSatelliteRouter(int x) {
		final UUID liquidSatelliteUUID = x == -1 ? this.liquidSatelliteUUID.getValue() : liquidSatelliteUUIDList.get(x);
		final int satelliteRouterId = SimpleServiceLocator.routerManager.getIDforUUID(liquidSatelliteUUID);
		return SimpleServiceLocator.routerManager.getRouter(satelliteRouterId);
	}

	/**
	 * Triggers opening the first possible crafting provider or inventory GUI by using onBlockActivated.
	 *
	 * @return true, if a GUI was opened (server-side only)
	 */
	public boolean openAttachedGui(EntityPlayer player) {
		if (MainProxy.isClient(player.world)) {
			if (player instanceof EntityPlayerMP) {
				player.closeScreen();
			} else if (player instanceof EntityPlayerSP) {
				player.closeScreen();
			}
			MainProxy.sendPacketToServer(
					PacketHandler.getPacket(CraftingPipeOpenConnectedGuiPacket.class).setModulePos(this));
			return false;
		}

		final IPipeServiceProvider service = _service;
		if (service == null) return false;
		final IWorldProvider worldProvider = _world;
		if (worldProvider == null) return false;

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

		final boolean guiOpened = service.getAvailableAdjacent().neighbors().keySet().stream().anyMatch(neighbor -> {
			if (neighbor.canHandleItems() || SimpleServiceLocator.craftingRecipeProviders.stream()
					.anyMatch(provider -> provider.canOpenGui(neighbor.getTileEntity()))) {
				final BlockPos pos = neighbor.getTileEntity().getPos();
				IBlockState blockState = worldProvider.getWorld().getBlockState(pos);
				return !blockState.getBlock().isAir(blockState, worldProvider.getWorld(), pos) && blockState.getBlock()
						.onBlockActivated(worldProvider.getWorld(), pos,
								neighbor.getTileEntity().getWorld().getBlockState(pos), player, EnumHand.MAIN_HAND,
								EnumFacing.UP, 0, 0, 0);
			} else {
				return false;
			}
		});
		if (!guiOpened) {
			LogisticsPipes.log.warn("Ignored open attached GUI request at " + player.world + " @ " + getBlockPos());
		}
		player.inventory.currentItem = savedEquipped;
		return guiOpened;
	}

	public void enabledUpdateEntity() {
		final IPipeServiceProvider service = _service;
		if (service == null) return;

		if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
			if (service.isNthTick(6)) {
				cacheAreAllOrderesToBuffer();
			}
			if (service.getItemOrderManager().isFirstOrderWatched()) {
				TileEntity tile = lastAccessedCrafter.get();
				if (tile != null) {
					service.getItemOrderManager()
							.setMachineProgress(SimpleServiceLocator.machineProgressProvider.getProgressForTile(tile));
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

		final List<NeighborTileEntity<TileEntity>> adjacentInventories = service.getAvailableAdjacent().inventories();

		if (!service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
			final ISlotUpgradeManager upgradeManager = Objects.requireNonNull(getUpgradeManager());
			if (upgradeManager.getCrafterCleanup() > 0) {
				adjacentInventories.stream()
						.map(neighbor -> extractFiltered(neighbor, cleanupInventory, cleanupModeIsExclude.getValue(),
								upgradeManager.getCrafterCleanup() * 3)).filter(stack -> !stack.isEmpty()).findFirst()
						.ifPresent(extracted -> {
							service.queueRoutedItem(
									SimpleServiceLocator.routedItemHelper.createNewTravelItem(extracted),
									EnumFacing.UP);
							service.getCacheHolder().trigger(CacheTypes.Inventory);
						});
			}
			return;
		}

		if (adjacentInventories.size() < 1) {
			if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
				service.getItemOrderManager().sendFailed();
			}
			return;
		}

		List<ItemIdentifierStack> wanteditem = getCraftedItems();
		if (wanteditem == null || wanteditem.isEmpty()) {
			return;
		}

		service.spawnParticle(Particles.VioletParticle, 2);

		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		while (itemsleft > 0 && stacksleft > 0 && (service.getItemOrderManager()
				.hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA))) {
			LogisticsItemOrder nextOrder = service.getItemOrderManager()
					.peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA); // fetch but not remove.
			int maxtosend = Math.min(itemsleft, nextOrder.getResource().stack.getStackSize());
			maxtosend = Math.min(nextOrder.getResource().getItem().getMaxStackSize(), maxtosend);
			// retrieve the new crafted items
			ItemStack extracted = ItemStack.EMPTY;
			NeighborTileEntity<TileEntity> adjacent = null; // there has to be at least one adjacentCrafter at this point; adjacent wont stay null
			for (NeighborTileEntity<TileEntity> adjacentCrafter : adjacentInventories) {
				adjacent = adjacentCrafter;
				extracted = extract(adjacent, nextOrder.getResource(), maxtosend);
				if (!extracted.isEmpty()) {
					break;
				}
			}
			if (extracted.isEmpty()) {
				service.getItemOrderManager().deferSend();
				break;
			}
			service.getCacheHolder().trigger(CacheTypes.Inventory);
			Objects.requireNonNull(adjacent);
			lastAccessedCrafter = new WeakReference<>(adjacent.getTileEntity());
			// send the new crafted items to the destination
			ItemIdentifier extractedID = ItemIdentifier.get(extracted);
			while (!extracted.isEmpty()) {
				if (isExtractedMismatch(nextOrder, extractedID)) {
					LogisticsItemOrder startOrder = nextOrder;
					if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
						do {
							service.getItemOrderManager().deferSend();
							nextOrder = service.getItemOrderManager()
									.peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA);
						} while (isExtractedMismatch(nextOrder, extractedID) && startOrder != nextOrder);
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

						service.sendStack(stackToSend, -1, ItemSendMode.Normal, null, adjacent.getDirection());
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
					SinkReply reply = LogisticsManager
							.canSink(stackToSend, nextOrder.getDestination().getRouter(), null, true,
									ItemIdentifier.get(stackToSend), null, true, false);
					boolean defersend = (reply == null || reply.bufferMode != BufferMode.NONE
							|| reply.maxNumberOfItems < 1);
					IRoutedItem item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stackToSend);
					item.setDestination(nextOrder.getDestination().getRouter().getSimpleID());
					item.setTransportMode(TransportMode.Active);
					item.setAdditionalTargetInformation(nextOrder.getInformation());
					service.queueRoutedItem(item, adjacent.getDirection());
					service.getItemOrderManager().sendSuccessfull(stackToSend.getCount(), defersend, item);
				} else {
					service.sendStack(stackToSend, -1, ItemSendMode.Normal, nextOrder.getInformation(),
							adjacent.getDirection());
					service.getItemOrderManager().sendSuccessfull(stackToSend.getCount(), false, null);
				}
				if (service.getItemOrderManager().hasOrders(ResourceType.CRAFTING, ResourceType.EXTRA)) {
					nextOrder = service.getItemOrderManager()
							.peekAtTopRequest(ResourceType.CRAFTING, ResourceType.EXTRA); // fetch but not remove.
				}
			}
		}

	}

	private boolean isExtractedMismatch(LogisticsItemOrder nextOrder, ItemIdentifier extractedID) {
		return !nextOrder.getResource().getItem().equals(extractedID) && (!getUpgradeManager().isFuzzyUpgrade() || (
				nextOrder.getResource().getBitSet().nextSetBit(0) == -1) || !nextOrder.getResource()
				.matches(extractedID, IResource.MatchSettings.NORMAL));
	}

	public boolean areAllOrderesToBuffer() {
		return cachedAreAllOrderesToBuffer;
	}

	public void cacheAreAllOrderesToBuffer() {
		final IPipeServiceProvider service = _service;
		if (service == null) return;
		boolean result = true;
		for (LogisticsItemOrder order : service.getItemOrderManager()) {
			if (order.getDestination() instanceof IItemSpaceControl) {
				SinkReply reply = LogisticsManager
						.canSink(order.getResource().stack.makeNormalStack(), order.getDestination().getRouter(), null,
								true, order.getResource().getItem(), null, true, false);
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
		return LPNeighborTileEntityKt.optionalIs(adjacent, LogisticsCraftingTableTileEntity.class)
				.map(adjacentCraftingTable -> extractFromLogisticsCraftingTable(adjacentCraftingTable, item, amount))
				.orElseGet(() -> {
					final IInventoryUtil invUtil = LPNeighborTileEntityKt.getInventoryUtil(adjacent);
					if (invUtil == null) return ItemStack.EMPTY;
					return extractFromInventory(invUtil, item, amount);
				});
	}

	@Nonnull
	private ItemStack extractFiltered(NeighborTileEntity<TileEntity> neighbor, IItemIdentifierInventory inv,
			boolean isExcluded, int filterInvLimit) {
		final IInventoryUtil invUtil = LPNeighborTileEntityKt.getInventoryUtil(neighbor);
		if (invUtil == null) return ItemStack.EMPTY;
		return extractFromInventoryFiltered(invUtil, inv, isExcluded, filterInvLimit);
	}

	@Nonnull
	private ItemStack extractFromInventory(@Nonnull IInventoryUtil invUtil, IResource wanteditem, int count) {
		final IPipeServiceProvider service = _service;
		if (service == null) return ItemStack.EMPTY;
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
		if (itemToExtract == null) return ItemStack.EMPTY;
		int available = invUtil.itemCount(itemToExtract);
		if (available == 0 || !service.canUseEnergy(neededEnergy() * Math.min(count, available))) {
			return ItemStack.EMPTY;
		}
		ItemStack extracted = invUtil.getMultipleItems(itemToExtract, Math.min(count, available));
		service.useEnergy(neededEnergy() * extracted.getCount());
		return extracted;
	}

	@Nonnull
	private ItemStack extractFromInventoryFiltered(@Nonnull IInventoryUtil invUtil, IItemIdentifierInventory filter,
			boolean isExcluded, int filterInvLimit) {
		final IPipeServiceProvider service = _service;
		if (service == null) return ItemStack.EMPTY;

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
		if (available == 0 || !service.canUseEnergy(neededEnergy() * Math.min(64, available))) {
			return ItemStack.EMPTY;
		}
		ItemStack extracted = invUtil.getMultipleItems(wanteditem, Math.min(64, available));
		service.useEnergy(neededEnergy() * extracted.getCount());
		return extracted;
	}

	private boolean isFiltered(IItemIdentifierInventory filter, int filterInvLimit, ItemIdentifier item,
			boolean found) {
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
			NeighborTileEntity<LogisticsCraftingTableTileEntity> adjacentCraftingTable, IResource wanteditem,
			int count) {
		final IPipeServiceProvider service = _service;
		if (service == null) return ItemStack.EMPTY;
		ItemStack extracted = extractFromInventory(
				Objects.requireNonNull(LPNeighborTileEntityKt.getInventoryUtil(adjacentCraftingTable)), wanteditem,
				count);
		if (!extracted.isEmpty()) {
			return extracted;
		}
		ItemStack retstack = ItemStack.EMPTY;
		while (count > 0) {
			ItemStack stack = adjacentCraftingTable.getTileEntity().getOutput(wanteditem, service);
			if (stack.isEmpty()) {
				break;
			}
			if (retstack.isEmpty()) {
				if (!wanteditem.matches(ItemIdentifier.get(stack), wanteditem instanceof ItemResource ?
						IResource.MatchSettings.WITHOUT_NBT :
						IResource.MatchSettings.NORMAL)) {
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
		return (int) (10 * Math.pow(1.1, getUpgradeManager().getItemExtractionUpgrade()) * Math
				.pow(1.2, getUpgradeManager().getItemStackExtractionUpgrade()));
	}

	protected int itemsToExtract() {
		return (int) Math.pow(2, getUpgradeManager().getItemExtractionUpgrade());
	}

	protected int stacksToExtract() {
		return 1 + getUpgradeManager().getItemStackExtractionUpgrade();
	}

	public void importCleanup() {
		for (int i = 0; i < 10; i++) {
			final ItemIdentifierStack identStack = dummyInventory.getIDStackInSlot(i);
			if (identStack == null) {
				cleanupInventory.clearInventorySlotContents(i);
			} else {
				cleanupInventory.setInventorySlotContents(i, new ItemIdentifierStack(identStack));
			}
		}
		for (int i = 10; i < cleanupInventory.getSizeInventory(); i++) {
			cleanupInventory.clearInventorySlotContents(i);
		}
		cleanupInventory.getSlotAccess().compactFirst(10);
		cleanupInventory.recheckStackLimit();
		cleanupModeIsExclude.setValue(false);
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

	public void setSatelliteUUID(@Nullable UUID pipeID) {
		if (pipeID == null) {
			satelliteUUID.zero();
		} else {
			satelliteUUID.setValue(pipeID);
		}
		updateSatellitesOnClient();
		updateSatelliteFromIDs = null;
	}

	public void setAdvancedSatelliteUUID(int i, @Nullable UUID pipeID) {
		if (pipeID == null) {
			advancedSatelliteUUIDList.zero(i);
		} else {
			advancedSatelliteUUIDList.set(i, pipeID);
		}
		updateSatellitesOnClient();
		updateSatelliteFromIDs = null;
	}

	public void setFluidSatelliteUUID(@Nullable UUID pipeID) {
		if (pipeID == null) {
			liquidSatelliteUUID.zero();
		} else {
			liquidSatelliteUUID.setValue(pipeID);
		}
		updateSatellitesOnClient();
		updateSatelliteFromIDs = null;
	}

	public void setAdvancedFluidSatelliteUUID(int i, @Nullable UUID pipeID) {
		if (pipeID == null) {
			liquidSatelliteUUIDList.zero(i);
		} else {
			liquidSatelliteUUIDList.set(i, pipeID);
		}
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

	public static class CraftingChassisInformation extends ChassiTargetInformation {

		@Getter
		private final int craftingSlot;

		public CraftingChassisInformation(int craftingSlot, int moduleSlot) {
			super(moduleSlot);
			this.craftingSlot = craftingSlot;
		}
	}

	// FIXME: Remove after 1.12
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

	public boolean hasByproductUpgrade() {
		return getUpgradeManager().hasByproductExtractor();
	}

	public boolean hasFuzzyUpgrade() { return getUpgradeManager().isFuzzyUpgrade(); }
}
