package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.interfaces.routing.ITargetSlotInformation;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ActiveSupplierInHand;
import logisticspipes.network.guis.module.inpipe.ActiveSupplierSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class ModuleActiveSupplier extends LogisticsModule implements IRequestItems, IRequireReliableTransport, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive, ISimpleInventoryEventHandler, Gui {

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<>();
	public int[] slotArray = new int[9];
	private boolean _lastRequestFailed = false;
	private ItemIdentifierInventory dummyInventory = new ItemIdentifierInventory(9, "", 127);
	private SupplyMode _requestMode = SupplyMode.Bulk50;
	private PatternMode _patternMode = PatternMode.Bulk50;
	@Getter
	@Setter
	private boolean isLimited = true;

	public ModuleActiveSupplier() {
		dummyInventory.addListener(this);
	}

	public static String getName() {
		return "active_supplier";
	}

	@Override
	public @Nonnull List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add("Supplied: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
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
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(dummyInventory)).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		return null;
		//return HUD;
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		dummyInventory.handleItemIdentifierList(list);
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		if (MainProxy.isServer(_world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(dummyInventory)).setModulePos(this), localModeWatchers);
		}
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	/* TRIGGER INTERFACE */
	public boolean isRequestFailed() {
		return _lastRequestFailed;
	}

	public void setRequestFailed(boolean value) {
		_lastRequestFailed = value;
	}

	/*** GUI ***/
	public ItemIdentifierInventory getDummyInventory() {
		return dummyInventory;
	}

	@Override
	public void tick() {
		if (!_service.isNthTick(100)) {
			return;
		}

		_requestedItems.values().stream().filter(amount -> amount > 0).forEach(amount -> _service.spawnParticle(Particles.VioletParticle, 2));

		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(_world.getWorld(), getBlockPos());

		worldCoordinates.connectedTileEntities(ConnectionPipeType.ITEM)
				.filter(adjacent -> !adjacent.isLogisticsPipe())
				.map(neighbor -> neighbor.sneakyInsertion().from(getUpgradeManager()))
				.map(NeighborTileEntity::getInventoryUtil)
				.filter(Objects::nonNull)
				.filter(invUtil -> invUtil.getSizeInventory() > 0)
				.forEach(invUtil -> {
					if (getUpgradeManager().hasPatternUpgrade()) {
						createPatternRequest(invUtil);
					} else {
						createSupplyRequest(invUtil);
					}
				});
	}

	private void createPatternRequest(IInventoryUtil invUtil) {
		_service.getDebug().log("Supplier: Start calculating pattern request");
		setRequestFailed(false);
		for (int i = 0; i < 9; i++) {
			ItemIdentifierStack needed = dummyInventory.getIDStackInSlot(i);
			if (needed == null) {
				continue;
			}
			if (invUtil.getSizeInventory() <= slotArray[i]) {
				continue;
			}
			ItemStack stack = invUtil.getStackInSlot(slotArray[i]);
			ItemIdentifierStack have = null;
			if (!stack.isEmpty()) {
				have = ItemIdentifierStack.getFromStack(stack);
			}
			int haveCount = 0;
			if (have != null) {
				if (!have.getItem().equals(needed.getItem())) {
					_service.getDebug().log("Supplier: Slot for " + i + ", " + needed + " already taken by " + have);
					setRequestFailed(true);
					continue;
				}
				haveCount = have.getStackSize();
			}
			if ((_patternMode == PatternMode.Bulk50 && haveCount > needed.getStackSize() / 2) || (_patternMode == PatternMode.Bulk100 && haveCount >= needed.getStackSize())) {
				continue;
			}

			Integer requestedCount = _requestedItems.get(needed.getItem());
			if (requestedCount != null) {
				haveCount += requestedCount;
			}

			int neededCount = needed.getStackSize() - haveCount;
			if (neededCount < 1) {
				continue;
			}

			ItemIdentifierStack toRequest = new ItemIdentifierStack(needed.getItem(), neededCount);

			_service.getDebug().log("Supplier: Missing for slot " + i + ": " + toRequest);

			if (!_service.useEnergy(10)) {
				break;
			}

			boolean success = false;

			IAdditionalTargetInformation targetInformation = new PatternSupplierTargetInformation(slotArray[i], needed.getStackSize());

			if (_patternMode != PatternMode.Full) {
				_service.getDebug().log("Supplier: Requesting partial: " + toRequest);
				neededCount = RequestTree.requestPartial(toRequest, this, targetInformation);
				_service.getDebug().log("Supplier: Requested: " + toRequest.getItem().makeStack(neededCount));
				if (neededCount > 0) {
					success = true;
				}
			} else {
				_service.getDebug().log("Supplier: Requesting: " + toRequest);
				success = RequestTree.request(toRequest, this, null, targetInformation);
				if (success) {
					_service.getDebug().log("Supplier: Request success");
				} else {
					_service.getDebug().log("Supplier: Request failed");
				}
			}

			if (success) {
				Integer currentRequest = _requestedItems.get(toRequest.getItem());
				if (currentRequest == null) {
					_requestedItems.put(toRequest.getItem(), neededCount);
				} else {
					_requestedItems.put(toRequest.getItem(), currentRequest + neededCount);
				}
			} else {
				setRequestFailed(true);
			}
		}
	}

	private void createSupplyRequest(IInventoryUtil invUtil) {
		_service.getDebug().log("Supplier: Start calculating supply request");
		//How many do I want?
		HashMap<ItemIdentifier, Integer> needed = new HashMap<>(dummyInventory.getItemsAndCount());
		_service.getDebug().log("Supplier: Needed: " + needed);

		//How many do I have?
		Map<ItemIdentifier, Integer> have = invUtil.getItemsAndCount();
		_service.getDebug().log("Supplier: Have:   " + have);

		//How many do I have?
		HashMap<ItemIdentifier, Integer> haveUndamaged = new HashMap<>();
		for (Entry<ItemIdentifier, Integer> item : have.entrySet()) {
			haveUndamaged.merge(item.getKey().getUndamaged(), item.getValue(), (a, b) -> a + b);
		}

		//Reduce what I have and what have been requested already
		for (Entry<ItemIdentifier, Integer> item : needed.entrySet()) {
			Integer haveCount = haveUndamaged.get(item.getKey().getUndamaged());
			if (haveCount == null) {
				haveCount = 0;
			}
			int spaceAvailable = invUtil.roomForItem(item.getKey().unsafeMakeNormalStack(Integer.MAX_VALUE));
			if (_requestMode == SupplyMode.Infinite) {
				Integer requestedCount = _requestedItems.get(item.getKey());
				if (requestedCount != null) {
					spaceAvailable -= requestedCount;
				}
				item.setValue(Math.min(item.getKey().getMaxStackSize(), Math.max(0, spaceAvailable)));
				continue;
			}
			if (spaceAvailable < 1 || (_requestMode == SupplyMode.Bulk50 && haveCount > item.getValue() / 2) || (_requestMode == SupplyMode.Bulk100 && haveCount >= item.getValue())) {
				item.setValue(0);
				continue;
			}
			if (haveCount > 0) {
				item.setValue(item.getValue() - haveCount);
				// so that 1 damaged item can't satisfy a request for 2 other damage values.
				haveUndamaged.put(item.getKey().getUndamaged(), haveCount - item.getValue());
			}
			Integer requestedCount = _requestedItems.get(item.getKey());
			if (requestedCount != null) {
				item.setValue(item.getValue() - requestedCount);
			}
		}

		_service.getDebug().log("Supplier: Missing:   " + needed);

		setRequestFailed(false);

		//Make request
		for (Entry<ItemIdentifier, Integer> need : needed.entrySet()) {
			Integer amountRequested = need.getValue();
			if (amountRequested == null || amountRequested < 1) {
				continue;
			}
			int neededCount = amountRequested;
			if (!_service.useEnergy(10)) {
				break;
			}

			boolean success = false;

			IAdditionalTargetInformation targetInformation = new SupplierTargetInformation();

			if (_requestMode != SupplyMode.Full) {
				_service.getDebug().log("Supplier: Requesting partial: " + need.getKey().makeStack(neededCount));
				neededCount = RequestTree.requestPartial(need.getKey().makeStack(neededCount), this, targetInformation);
				_service.getDebug().log("Supplier: Requested: " + need.getKey().makeStack(neededCount));
				if (neededCount > 0) {
					success = true;
				}
			} else {
				_service.getDebug().log("Supplier: Requesting: " + need.getKey().makeStack(neededCount));
				success = RequestTree.request(need.getKey().makeStack(neededCount), this, null, targetInformation);
				if (success) {
					_service.getDebug().log("Supplier: Request success");
				} else {
					_service.getDebug().log("Supplier: Request failed");
				}
			}

			if (success) {
				Integer currentRequest = _requestedItems.get(need.getKey());
				if (currentRequest == null) {
					_requestedItems.put(need.getKey(), neededCount);
					_service.getDebug().log("Supplier: Inserting Requested Items: " + neededCount);
				} else {
					_requestedItems.put(need.getKey(), currentRequest + neededCount);
					_service.getDebug().log("Supplier: Raising Requested Items from: " + currentRequest + " to: " + currentRequest + neededCount);
				}
			} else {
				setRequestFailed(true);
			}

		}
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		dummyInventory.readFromNBT(nbttagcompound, "");
		if (nbttagcompound.hasKey("requestmode")) {
			_requestMode = SupplyMode.values()[nbttagcompound.getShort("requestmode")];
		}
		if (nbttagcompound.hasKey("patternmode")) {
			_patternMode = PatternMode.values()[nbttagcompound.getShort("patternmode")];
		}
		if (nbttagcompound.hasKey("limited")) {
			setLimited(nbttagcompound.getBoolean("limited"));
		}
		if (nbttagcompound.hasKey("requestpartials")) {
			boolean oldPartials = nbttagcompound.getBoolean("requestpartials");
			if (oldPartials) {
				_requestMode = SupplyMode.Partial;
			} else {
				_requestMode = SupplyMode.Full;
			}
		}
		for (int i = 0; i < 9; i++) {
			slotArray[i] = nbttagcompound.getInteger("slotArray_" + i);
		}
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		dummyInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setShort("requestmode", (short) _requestMode.ordinal());
		nbttagcompound.setShort("patternmode", (short) _patternMode.ordinal());
		nbttagcompound.setBoolean("limited", isLimited());
		for (int i = 0; i < 9; i++) {
			nbttagcompound.setInteger("slotArray_" + i, slotArray[i]);
		}
	}

	private void decreaseRequested(ItemIdentifierStack item) {
		int remaining = item.getStackSize();
		//see if we can get an exact match
		Integer count = _requestedItems.get(item.getItem());
		if (count != null) {
			_service.getDebug().log("Supplier: Exact match. Still missing: " + Math.max(0, count - remaining));
			if (count - remaining > 0) {
				_requestedItems.put(item.getItem(), count - remaining);
			} else {
				_requestedItems.remove(item.getItem());
			}
			remaining -= count;
		}
		if (remaining <= 0) {
			return;
		}
		//still remaining... was from fuzzyMatch on a crafter
		Iterator<Entry<ItemIdentifier, Integer>> it = _requestedItems.entrySet().iterator();
		while (it.hasNext()) {
			Entry<ItemIdentifier, Integer> e = it.next();
			if (e.getKey().equalsWithoutNBT(item.getItem())) {
				int expected = e.getValue();
				_service.getDebug().log("Supplier: Fuzzy match with" + e + ". Still missing: " + Math.max(0, expected - remaining));
				if (expected - remaining > 0) {
					e.setValue(expected - remaining);
				} else {
					it.remove();
				}
				remaining -= expected;
			}
			if (remaining <= 0) {
				return;
			}
		}
		//we have no idea what this is, log it.
		_service.getDebug().log("Supplier: supplier got unexpected item " + item.toString());
	}

	@Override
	public void itemLost(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		_service.getDebug().log("Supplier: Registered Item Lost: " + item);
		decreaseRequested(item);
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		_service.getDebug().log("Supplier: Registered Item Arrived: " + item);
		decreaseRequested(item);
	}

	public SupplyMode getSupplyMode() {
		return _requestMode;
	}

	public void setSupplyMode(SupplyMode mode) {
		_requestMode = mode;
	}

	public PatternMode getPatternMode() {
		return _patternMode;
	}

	public void setPatternMode(PatternMode mode) {
		_patternMode = mode;
	}

	public int[] getSlotsForItemIdentifier(ItemIdentifier item) {
		int size = 0;
		for (int i = 0; i < 9; i++) {
			if (dummyInventory.getIDStackInSlot(i) != null && dummyInventory.getIDStackInSlot(i).getItem().equals(item)) {
				size++;
			}
		}
		int[] array = new int[size];
		int pos = 0;
		for (int i = 0; i < 9; i++) {
			if (dummyInventory.getIDStackInSlot(i) != null && dummyInventory.getIDStackInSlot(i).getItem().equals(item)) {
				array[pos++] = i;
			}
		}
		return array;
	}

	public int getInvSlotForSlot(int i) {
		return slotArray[i];
	}

	public int getAmountForSlot(int i) {
		return dummyInventory.getIDStackInSlot(i).getStackSize();
	}

	public void addStatusInformation(List<StatusEntry> status) {
		StatusEntry entry = new StatusEntry();
		entry.name = "Requested Items";
		entry.subEntry = new ArrayList<>();
		for (Entry<ItemIdentifier, Integer> part : _requestedItems.entrySet()) {
			StatusEntry subEntry = new StatusEntry();
			subEntry.name = part.toString();
			entry.subEntry.add(subEntry);
		}
		status.add(entry);
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		final boolean hasPatternUpgrade = hasPatternUpgrade();
		return NewGuiHandler.getGui(ActiveSupplierSlot.class)
				.setPatternUpgarde(hasPatternUpgrade)
				.setSlotArray(slotArray)
				.setMode((hasPatternUpgrade ? getPatternMode() : getSupplyMode()).ordinal())
				.setLimit(isLimited);
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ActiveSupplierInHand.class);
	}

	@Override
	@Nonnull
	public IRouter getRouter() {
		return _service.getRouter();
	}

	@Override
	public void itemCouldNotBeSend(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		itemLost(item, info);
	}

	@Override
	public int getID() {
		return _service.getRouter().getSimpleID();
	}

	@Override
	public int compareTo(IRequestItems value2) {
		return 0;
	}

	public boolean hasPatternUpgrade() {
		final ISlotUpgradeManager upgradeManager = getUpgradeManager();
		if (upgradeManager != null) {
			return upgradeManager.hasPatternUpgrade();
		}
		return false;
	}

	public enum SupplyMode {
		Partial,
		Full,
		Bulk50,
		Bulk100,
		Infinite
	}

	public enum PatternMode {
		Partial,
		Full,
		Bulk50,
		Bulk100
	}

	public class PatternSupplierTargetInformation extends SupplierTargetInformation implements ITargetSlotInformation {

		private final int amount;
		private final int targetSlot;

		public PatternSupplierTargetInformation(int targetSlot, int amount) {
			super();
			this.targetSlot = targetSlot;
			this.amount = amount;

		}

		@Override
		public int getTargetSlot() {
			return targetSlot;
		}

		@Override
		public int getAmount() {
			return amount;
		}

		@Override
		public boolean isLimited() {
			return ModuleActiveSupplier.this.isLimited();
		}

	}

	public class SupplierTargetInformation extends ChassiTargetInformation {

		public SupplierTargetInformation() {
			super(getPositionInt());
		}

	}
}
