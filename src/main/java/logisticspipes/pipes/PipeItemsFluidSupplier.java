package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ITankUtil;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleFluidSupplier;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.connection.LPNeighborTileEntityKt;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;

public class PipeItemsFluidSupplier extends CoreRoutedPipe implements IRequestItems, IRequireReliableTransport {

	private boolean _lastRequestFailed = false;

	private final ModuleFluidSupplier moduleFluidSupplier;

	public PipeItemsFluidSupplier(Item item) {
		super(new PipeTransportLogistics(true) {

			@Override
			public boolean canPipeConnect(TileEntity tile, EnumFacing dir) {
				if (super.canPipeConnect(tile, dir)) {
					return true;
				}
				if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
					return false;
				}
				ITankUtil tank = PipeFluidUtil.INSTANCE.getTankUtilForTE(tile, dir.getOpposite());
				return tank != null && tank.containsTanks();
			}
		}, item);

		throttleTime = 100;
		moduleFluidSupplier = new ModuleFluidSupplier();
		moduleFluidSupplier.registerHandler(this, this);
		moduleFluidSupplier.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE;
	}

	/* TRIGGER INTERFACE */
	public boolean isRequestFailed() {
		return _lastRequestFailed;
	}

	public void setRequestFailed(boolean value) {
		_lastRequestFailed = value;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return moduleFluidSupplier;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}

	public void endReached(LPTravelingItemServer data, TileEntity tile) {
		getCacheHolder().trigger(CacheTypes.Inventory);
		transport.markChunkModified(tile);
		notifyOfItemArival(data.getInfo());
		EnumFacing orientation = data.output.getOpposite();
		if (getOriginalUpgradeManager().hasSneakyUpgrade()) {
			orientation = getOriginalUpgradeManager().getSneakyOrientation();
		}
		ITankUtil util = PipeFluidUtil.INSTANCE.getTankUtilForTE(tile, orientation);
		if (util == null) {
			return;
		}
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			return;
		}
		final ItemIdentifierStack idStack = data.getItemIdentifierStack();
		if (idStack == null) {
			return;
		}
		FluidIdentifierStack liquidId = FluidIdentifierStack.getFromStack(FluidUtil.getFluidContained(idStack.makeNormalStack()));
		if (liquidId == null) {
			return;
		}
		while (idStack.getStackSize() > 0 && util.fill(liquidId, false) == liquidId.getAmount() && this.useEnergy(5)) {
			util.fill(liquidId, true);
			idStack.lowerStackSize(1);
			Item item = idStack.getItem().item;
			if (item.hasContainerItem(idStack.makeNormalStack())) {
				Item containerItem = Objects.requireNonNull(item.getContainerItem());
				transport.sendItem(new ItemStack(containerItem, 1));
			}
		}
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public void throttledUpdateEntity() {
		if (!isEnabled()) {
			return;
		}

		if (MainProxy.isClient(getWorld())) {
			return;
		}
		super.throttledUpdateEntity();

		for (NeighborTileEntity<TileEntity> neighbor : getAdjacent().fluidTanks()) {
			final ITankUtil tankUtil = LPNeighborTileEntityKt.getTankUtil(neighbor);
			if (tankUtil == null || !tankUtil.containsTanks()) {
				continue;
			}

			//How much do I want?
			Map<ItemIdentifier, Integer> wantContainers = moduleFluidSupplier.filterInventory.getItemsAndCount();
			HashMap<FluidIdentifier, Integer> wantFluids = new HashMap<>();
			for (Map.Entry<ItemIdentifier, Integer> item : wantContainers.entrySet()) {
				ItemStack wantItem = item.getKey().unsafeMakeNormalStack(1);
				FluidStack liquidStack = FluidUtil.getFluidContained(wantItem);
				if (liquidStack == null) {
					continue;
				}
				wantFluids.put(FluidIdentifier.get(liquidStack), item.getValue() * liquidStack.amount);
			}

			//How much do I have?
			HashMap<FluidIdentifier, Integer> haveFluids = new HashMap<>();

			tankUtil.tanks()
					.map(tank -> FluidIdentifierStack.getFromStack(tank.getContents()))
					.filter(Objects::nonNull)
					.forEach(fluid -> {
						if (wantFluids.containsKey(fluid.getFluid())) {
							haveFluids.merge(fluid.getFluid(), fluid.getAmount(), Integer::sum);
						}
					});

			//HashMap<Integer, Integer> needFluids = new HashMap<Integer, Integer>();
			//Reduce what I have and what have been requested already
			for (Map.Entry<FluidIdentifier, Integer> liquidId : wantFluids.entrySet()) {
				Integer haveCount = haveFluids.get(liquidId.getKey());
				if (haveCount != null) {
					liquidId.setValue(liquidId.getValue() - haveCount);
				}
			}
			for (Map.Entry<ItemIdentifier, Integer> requestedItem : moduleFluidSupplier._requestedItems.entrySet()) {
				ItemStack wantItem = requestedItem.getKey().unsafeMakeNormalStack(1);
				FluidStack requestedFluidId = FluidUtil.getFluidContained(wantItem);
				if (requestedFluidId == null) {
					continue;
				}
				FluidIdentifier requestedFluid = FluidIdentifier.get(requestedFluidId);
				Integer want = wantFluids.get(requestedFluid);
				if (want != null) {
					wantFluids.put(requestedFluid, want - requestedItem.getValue() * requestedFluidId.amount);
				}
			}

			((PipeItemsFluidSupplier) Objects.requireNonNull(container).pipe).setRequestFailed(false);

			//Make request

			for (ItemIdentifier need : wantContainers.keySet()) {
				FluidStack requestedFluidId = FluidUtil.getFluidContained(need.unsafeMakeNormalStack(1));
				if (requestedFluidId == null) {
					continue;
				}
				if (!wantFluids.containsKey(FluidIdentifier.get(requestedFluidId))) {
					continue;
				}
				int countToRequest = wantFluids.get(FluidIdentifier.get(requestedFluidId)) / requestedFluidId.amount;
				if (countToRequest < 1) {
					continue;
				}

				if (!useEnergy(11)) {
					break;
				}

				boolean success = false;

				if (moduleFluidSupplier._requestPartials.getValue()) {
					countToRequest = RequestTree.requestPartial(need.makeStack(countToRequest), (IRequestItems) container.pipe, null);
					if (countToRequest > 0) {
						success = true;
					}
				} else {
					success = RequestTree.request(need.makeStack(countToRequest), (IRequestItems) container.pipe, null, null);
				}

				if (success) {
					Integer currentRequest = moduleFluidSupplier._requestedItems.get(need);
					if (currentRequest == null) {
						moduleFluidSupplier._requestedItems.put(need, countToRequest);
					} else {
						moduleFluidSupplier._requestedItems.put(need, currentRequest + countToRequest);
					}
				} else {
					((PipeItemsFluidSupplier) container.pipe).setRequestFailed(true);
				}
			}
		}
	}

	@Override
	public void itemLost(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		moduleFluidSupplier.decreaseRequested(item);
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		moduleFluidSupplier.decreaseRequested(item);
		delayThrottle();
	}

	public boolean isRequestingPartials() {
		return this.moduleFluidSupplier._requestPartials.getValue();
	}

	public void setRequestingPartials(boolean value) {
		this.moduleFluidSupplier._requestPartials.setValue(value);
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_FluidSupplier_ID, getWorld(), getX(), getY(), getZ());
	}

	/*** GUI ***/
	public IItemIdentifierInventory getDummyInventory() {
		return this.moduleFluidSupplier.filterInventory;
	}
}
