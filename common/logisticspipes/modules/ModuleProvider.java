package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDProviderModule;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILegacyActiveModule;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logistics.LogisticsManagerV2;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketModuleInvContent;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsOrderManager;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.Player;

public class ModuleProvider implements ILogisticsGuiModule, ILegacyActiveModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive {
	
	protected IInventoryProvider _invProvider;
	protected ISendRoutedItem _itemSender;
	protected IRoutedPowerProvider _power;
	
	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager();
	
	private List<ILegacyActiveModule> _previousLegacyModules = new LinkedList<ILegacyActiveModule>();

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Items to provide (or empty for all)", 1);
	
	protected final int ticksToAction = 6;
	protected int currentTick = 0;
	
	protected boolean isExcludeFilter = false;
	protected ExtractionMode _extractionMode = ExtractionMode.Normal;
	
	private int slot = 0;
	public int xCoord = 0;
	public int yCoord = 0;
	public int zCoord = 0;
	private IWorldProvider _world;

	private final Map<ItemIdentifier,Integer> displayMap = new HashMap<ItemIdentifier, Integer>();
	public final ArrayList<ItemIdentifierStack> displayList = new ArrayList<ItemIdentifierStack>();
	private final ArrayList<ItemIdentifierStack> oldList = new ArrayList<ItemIdentifierStack>();
	
	private IHUDModuleRenderer HUD = new HUDProviderModule(this);

	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	
	public ModuleProvider() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
		_world = world;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		isExcludeFilter = nbttagcompound.getBoolean("filterisexclude");
		_extractionMode = ExtractionMode.values()[nbttagcompound.getInteger("extractionMode")];
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("filterisexclude", isExcludeFilter);
    	nbttagcompound.setInteger("extractionMode", _extractionMode.ordinal());

	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Provider_ID;
	}
	
	protected int neededEnergy() {
		return 1;
	}
	
	protected ItemSendMode itemSendMode() {
		return ItemSendMode.Normal;
	}
	
	protected int itemsToExtract() {
		return 8;
	}

	protected int stacksToExtract() {
		return 1;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority) {
		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction) return;
		currentTick = 0;
		checkUpdate(null);
		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		Pair3<ItemIdentifierStack,IRequestItems, List<IRelayItem>> firstOrder = null;
		Pair3<ItemIdentifierStack,IRequestItems, List<IRelayItem>> order = null;
		while (itemsleft > 0 && stacksleft > 0 && _orderManager.hasOrders() && (firstOrder == null || firstOrder != order)) {
			if(firstOrder == null)
				firstOrder = order;
			order = _orderManager.getNextRequest();
			int sent = sendStack(order.getValue1(), itemsleft, order.getValue2().getRouter().getSimpleID(), order.getValue3());
			if(sent < 0) break;
			MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, xCoord, yCoord, zCoord, _world.getWorld(), 3);
			stacksleft -= 1;
			itemsleft -= sent;
		}
	}

	@Override
	public void registerPreviousLegacyModules(List<ILegacyActiveModule> previousModules) {
		_previousLegacyModules = previousModules;
	}

	@Override
	public boolean filterAllowsItem(ItemIdentifier item) {
		if(!hasFilter()) return true;
		boolean isFiltered = itemIsFiltered(item);
		return isExcludeFilter ^ isFiltered;
	}

	@Override
	public void onBlockRemoval() {
		while(_orderManager.hasOrders()) {
			_orderManager.sendFailed();
		}
	}

	@Override
	public void canProvide(RequestTreeNode tree, int donePromisses, List<IFilter> filters) {
		int canProvide = getAvailableItemCount(tree.getStackItem());
		canProvide -= donePromisses;
		if (canProvide < 1) return;
		LogisticsPromise promise = new LogisticsPromise();
		promise.item = tree.getStackItem();
		promise.numberOfItems = Math.min(canProvide, tree.getMissingItemCount());
		promise.sender = (IProvideItems) _itemSender;
		promise.relayPoints = new LinkedList<IRelayItem>(filters);
		tree.addPromise(promise);
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, promise.relayPoints);
	}

	private int getAvailableItemCount(ItemIdentifier item) {
		return getTotalItemCount(item) - _orderManager.totalItemsCountInOrders(item);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> items, List<IFilter> filters) {
		if (_invProvider.getPointedInventory() == null) return;
		
		IInventoryUtil inv = getAdaptedUtil(_invProvider.getPointedInventory());
		HashMap<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();

		//Skip already added items from this provider, skip filtered items, Reduce what has been reserved, add.
outer:
		for (Entry<ItemIdentifier, Integer> currItem : currentInv.entrySet()) {
			if(items.containsKey(currItem.getKey())) continue;
			
			if(!filterAllowsItem(currItem.getKey())) continue;

			for(ILegacyActiveModule m:_previousLegacyModules) {
				if(m.filterAllowsItem(currItem.getKey())) continue outer;
			}
			
			for(IFilter filter:filters) {
				if(filter.isBlocked() == filter.isFilteredItem(currItem.getKey().getUndamaged()) || filter.blockProvider()) continue outer;
			}

			int remaining = currItem.getValue() - _orderManager.totalItemsCountInOrders(currItem.getKey());
			if (remaining < 1) continue;

			items.put(currItem.getKey(), remaining);
		}
	}

	
	// returns -1 on perminatly failed, don't try another stack this tick
	// returns 0 on "unable to do this delivery"
	private int sendStack(ItemIdentifierStack stack, int maxCount, int destination, List<IRelayItem> relays) {
		ItemIdentifier item = stack.getItem();
		if (_invProvider.getPointedInventory() == null) {
			_orderManager.sendFailed();
			return 0;
		}
		IInventoryUtil inv = getAdaptedUtil(_invProvider.getPointedInventory());
		
		int available = inv.itemCount(item);
		if (available == 0) {
			_orderManager.sendFailed();
			return 0;
		}
		int wanted = Math.min(available, stack.stackSize);
		wanted = Math.min(wanted, maxCount);
		wanted = Math.min(wanted, item.getMaxStackSize());
		IRouter dRtr = SimpleServiceLocator.routerManager.getRouterUnsafe(destination,false);
		if(dRtr == null) {
			_orderManager.sendFailed();
			return 0;
		}
		SinkReply reply = LogisticsManagerV2.canSink(dRtr, null, true, stack.getItem(), null, true);
		boolean defersend = false;
		if(reply != null) {// some pipes are not aware of the space in the adjacent inventory, so they return null
			if(reply.maxNumberOfItems < wanted) {
				wanted = reply.maxNumberOfItems;
				if(wanted <= 0) {
					_orderManager.deferSend();
					return 0;
				}
				defersend = true;
			}
		}
		if(!_power.canUseEnergy(wanted * neededEnergy())) return -1;

		ItemStack removed = inv.getMultipleItems(item, wanted);
		int sent = removed.stackSize;
		_power.useEnergy(sent * neededEnergy());

		_itemSender.sendStack(removed, destination, itemSendMode(), relays);
		_orderManager.sendSuccessfull(sent, defersend);
		return sent;
	}
	
	private int getTotalItemCount(ItemIdentifier item) {
		
		if (_invProvider.getPointedInventory() == null) return 0;
		
		if(!filterAllowsItem(item)) return 0;
		
		IInventoryUtil inv = getAdaptedUtil(_invProvider.getPointedInventory());
		return inv.itemCount(item);
	}
	
	private boolean hasFilter() {
		return !_filterInventory.isEmpty();
	}
	
	private boolean itemIsFiltered(ItemIdentifier item){
		return _filterInventory.containsItem(item);
	}
	
	private IInventoryUtil getAdaptedUtil(IInventory base){
		switch(_extractionMode){
			case LeaveFirst:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 1, 0);
			case LeaveLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 0, 1);
			case LeaveFirstAndLast:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 1, 1);
			case Leave1PerStack:
				return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, true, false, 0, 0);
			default:
				break;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getHidingInventoryUtil(base, false, false, 0, 0);
	}


	
	/*** GUI STUFF ***/
	
	public IInventory getFilterInventory() {
		return _filterInventory;
	}

	public boolean isExcludeFilter() {
		return isExcludeFilter;
	}

	public void setFilterExcluded(boolean isExcludeFilter) {
		this.isExcludeFilter = isExcludeFilter;
	}

	public ExtractionMode getExtractionMode(){
		return _extractionMode;
	}

	public void nextExtractionMode() {
		_extractionMode = _extractionMode.next();
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add(!isExcludeFilter ? "Included" : "Excluded");
		list.add("Mode: " + _extractionMode.getExtractionModeString());
		list.add("Filter: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.slot = slot;
	}
	
	private void checkUpdate(EntityPlayer player) {
		if(localModeWatchers.size() == 0 && player == null)
			return;
		displayList.clear();
		displayMap.clear();
		getAllItems(displayMap, new ArrayList<IFilter>(0));
		displayList.ensureCapacity(displayMap.size());
		for(Entry<ItemIdentifier, Integer> item :displayMap.entrySet()) {
			displayList.add(new ItemIdentifierStack(item.getKey(), item.getValue()));
		}
		if(!oldList.equals(displayList)) {
			oldList.clear();
			oldList.ensureCapacity(displayList.size());
			oldList.addAll(displayList);
			MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, xCoord, yCoord, zCoord, slot, displayList).getPacket(), localModeWatchers);
		} else if(player != null) {
			MainProxy.sendPacketToPlayer(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, xCoord, yCoord, zCoord, slot, displayList).getPacket(), (Player)player);
		}
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, xCoord, yCoord, zCoord, slot).getPacket());
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, xCoord, yCoord, zCoord, slot).getPacket());
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		checkUpdate(player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getRenderer() {
		return HUD;
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		if(!this.isExcludeFilter && !_filterInventory.isEmpty()){
			Map<ItemIdentifier, Integer> mapIC = _filterInventory.getItemsAndCount();
			List<ItemIdentifier> li= new ArrayList<ItemIdentifier>(mapIC.size());
			li.addAll(mapIC.keySet());
			return li;
		} else {
			IInventoryUtil inv = getAdaptedUtil(_invProvider.getPointedInventory());
			Set<ItemIdentifier> setI = inv.getItems();
			List<ItemIdentifier> li = new ArrayList<ItemIdentifier>(setI.size());
outer:
			for (ItemIdentifier currItem : setI) {
				if(!filterAllowsItem(currItem)) continue;

				for(ILegacyActiveModule m:_previousLegacyModules) {
					if(m.filterAllowsItem(currItem)) continue outer;
				}
				li.add(currItem);
			}
			return li;
		}
	}

	@Override
	public boolean interestedInAttachedInventory() {
		//do it the dumb way for now and use invutil in getSpecificInterests
		return false;
		//return _filterInventory.isEmpty() || this.isExcludeFilter; // when items included this is only interested in items in the filter
		// when items not included, we can only serve those items in the filter.
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return false;
	}
}
