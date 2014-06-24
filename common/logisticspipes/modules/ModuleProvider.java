package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.gui.hud.modules.HUDProviderModule;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILegacyActiveModule;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ProviderModuleInHand;
import logisticspipes.network.guis.module.inpipe.ProviderModuleGuiProvider;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.RequestType;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@CCType(name="Provider Module")
public class ModuleProvider extends LogisticsGuiModule implements ILegacyActiveModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive {
	
	private List<ILegacyActiveModule> _previousLegacyModules = new LinkedList<ILegacyActiveModule>();

	private final ItemIdentifierInventory _filterInventory = new ItemIdentifierInventory(9, "Items to provide (or empty for all)", 1);
	
	protected final int ticksToAction = 6;
	protected int currentTick = 0;
	
	protected boolean isExcludeFilter = false;
	protected ExtractionMode _extractionMode = ExtractionMode.Normal;
	
	private final Map<ItemIdentifier,Integer> displayMap = new HashMap<ItemIdentifier, Integer>();
	public final ArrayList<ItemIdentifierStack> displayList = new ArrayList<ItemIdentifierStack>();
	private final ArrayList<ItemIdentifierStack> oldList = new ArrayList<ItemIdentifierStack>();
	
	private IHUDModuleRenderer HUD = new HUDProviderModule(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	
	public ModuleProvider() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		isExcludeFilter = nbttagcompound.getBoolean("filterisexclude");
		_extractionMode = ExtractionMode.getMode(nbttagcompound.getInteger("extractionMode"));
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("filterisexclude", isExcludeFilter);
    	nbttagcompound.setInteger("extractionMode", _extractionMode.ordinal());

	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ProviderModuleGuiProvider.class).setExtractorMode(this.getExtractionMode().ordinal()).setExclude(isExcludeFilter);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ProviderModuleInHand.class);
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
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		//if(true) return;
		if (++currentTick < ticksToAction) return;
		currentTick = 0;
		checkUpdate(null);
		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		LogisticsOrder firstOrder = null;
		LogisticsOrder order = null;
		while (itemsleft > 0 && stacksleft > 0 && _service.getOrderManager().hasOrders(RequestType.PROVIDER) && (firstOrder == null || firstOrder != order)) {
			if(firstOrder == null)
				firstOrder = order;
			order = _service.getOrderManager().peekAtTopRequest(RequestType.PROVIDER);
			int sent = sendStack(order.getItem(), itemsleft, order.getDestination().getRouter().getSimpleID(), order.getInformation());
			if(sent < 0) break;
			_service.spawnParticle(Particles.VioletParticle, 3);
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
		while(_service.getOrderManager().hasOrders(RequestType.PROVIDER)) {
			_service.getOrderManager().sendFailed();
		}
	}

	@Override
	public void canProvide(RequestTreeNode tree, int donePromisses, List<IFilter> filters) {
		int canProvide = getAvailableItemCount(tree.getStackItem());
		canProvide -= donePromisses;
		if (canProvide < 1) return;
		LogisticsPromise promise = new LogisticsPromise(tree.getStackItem(), Math.min(canProvide, tree.getMissingItemCount()), (IProvideItems) _service);
		tree.addPromise(promise);
	}

	@Override
	public LogisticsOrder fullFill(LogisticsPromise promise, IRequestItems destination, IAdditionalTargetInformation info) {
		return _service.getOrderManager().addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination,RequestType.PROVIDER, info);
	}

	private int getAvailableItemCount(ItemIdentifier item) {
		return getTotalItemCount(item) - _service.getOrderManager().totalItemsCountInOrders(item);
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> items, List<IFilter> filters) {
		IInventoryUtil inv = _service.getPointedInventory(_extractionMode,true);
		if (inv == null) return;
		
		Map<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();

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

			int remaining = currItem.getValue() - _service.getOrderManager().totalItemsCountInOrders(currItem.getKey());
			if (remaining < 1) continue;

			items.put(currItem.getKey(), remaining);
		}
	}

	
	// returns -1 on permanently failed, don't try another stack this tick
	// returns 0 on "unable to do this delivery"
	private int sendStack(ItemIdentifierStack stack, int maxCount, int destination, IAdditionalTargetInformation info) {
		ItemIdentifier item = stack.getItem();
		IInventoryUtil inv = _service.getPointedInventory(_extractionMode,true);
		if (inv == null) {
			_service.getOrderManager().sendFailed();
			return 0;
		}
		
		int available = inv.itemCount(item);
		if (available == 0) {
			_service.getOrderManager().sendFailed();
			return 0;
		}
		int wanted = Math.min(available, stack.getStackSize());
		wanted = Math.min(wanted, maxCount);
		wanted = Math.min(wanted, item.getMaxStackSize());
		IRouter dRtr = SimpleServiceLocator.routerManager.getRouterUnsafe(destination,false);
		if(dRtr == null) {
			_service.getOrderManager().sendFailed();
			return 0;
		}
		SinkReply reply = LogisticsManager.canSink(dRtr, null, true, stack.getItem(), null, true, false);
		boolean defersend = false;
		if(reply != null) {// some pipes are not aware of the space in the adjacent inventory, so they return null
			if(reply.maxNumberOfItems < wanted) {
				wanted = reply.maxNumberOfItems;
				if(wanted <= 0) {
					_service.getOrderManager().deferSend();
					return 0;
				}
				defersend = true;
			}
		}
		if(!_service.canUseEnergy(wanted * neededEnergy())) return -1;

		ItemStack removed = inv.getMultipleItems(item, wanted);
		if(removed == null || removed.stackSize == 0) {
			_service.getOrderManager().sendFailed();
			return 0;
		}
		int sent = removed.stackSize;
		_service.useEnergy(sent * neededEnergy());

		IRoutedItem sendedItem = _service.sendStack(removed, destination, itemSendMode(), info);
		_service.getOrderManager().sendSuccessfull(sent, defersend, sendedItem);
		return sent;
	}
	
	private int getTotalItemCount(ItemIdentifier item) {
		
		IInventoryUtil inv = _service.getPointedInventory(_extractionMode,true);
		if (inv == null) return 0;
		
		if(!filterAllowsItem(item)) return 0;
		
		return inv.itemCount(item);
	}
	
	private boolean hasFilter() {
		return !_filterInventory.isEmpty();
	}
	
	private boolean itemIsFiltered(ItemIdentifier item){
		return _filterInventory.containsItem(item);
	}
	
	/*** GUI STUFF ***/

	@CCCommand(description="Returns the FilterInventory of this Module")
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

	public void setExtractionMode(int id) {
		_extractionMode = ExtractionMode.getMode(id);
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
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(displayList).setModulePos(this).setCompressable(true), localModeWatchers);
		} else if(player != null) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(displayList).setModulePos(this).setCompressable(true), (Player)player);
		}
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
		checkUpdate(player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
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
		//when filter is empty or in exclude mode, this is interested in attached inventory already
		if(this.isExcludeFilter || _filterInventory.isEmpty()) {
			return null;
		}
		// when items included this is only interested in items in the filter
		Map<ItemIdentifier, Integer> mapIC = _filterInventory.getItemsAndCount();
		List<ItemIdentifier> li= new ArrayList<ItemIdentifier>(mapIC.size());
		li.addAll(mapIC.keySet());
		return li;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return this.isExcludeFilter || _filterInventory.isEmpty(); // when items included this is only interested in items in the filter
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

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleProvider");
	}
}
