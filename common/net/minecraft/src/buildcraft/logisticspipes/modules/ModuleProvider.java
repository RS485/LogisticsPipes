package net.minecraft.src.buildcraft.logisticspipes.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.IProvideItems;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.LogisticsOrderManager;
import net.minecraft.src.buildcraft.krapht.LogisticsPromise;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.LogisticsTransaction;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.buildcraft.logisticspipes.ExtractionMode;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.krapht.CroppedInventory;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;

public class ModuleProvider implements ILogisticsModule, ILegacyActiveModule, IClientInformationProvider {

	
	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	
	private LogisticsOrderManager _orderManager = new LogisticsOrderManager();
	
	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Items to provide (or empty for all)", 1);
	private final InventoryUtil _filterUtil = new InventoryUtil(_filterInventory, false);
	
	private final int ticksToAction = 6;
	private int currentTick = 0;
	
	private boolean isExcludeFilter = false;
	private ExtractionMode _extractionMode = ExtractionMode.Normal;
	
	public ModuleProvider() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {
		_invProvider = invProvider;
		_itemSender = itemSender;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		isExcludeFilter = nbttagcompound.getBoolean("filterisexclude");
		_extractionMode = ExtractionMode.values()[nbttagcompound.getInteger("extractionMode")];
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("filterisexclude", isExcludeFilter);
    	nbttagcompound.setInteger("extractionMode", _extractionMode.ordinal());

	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Provider_ID;
	}
	
	@Override	public SinkReply sinksItem(ItemStack item) {return null;}

	@Override	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction) return;
		currentTick = 0;
		if (!_orderManager.hasOrders()) return;
		
		LogisticsRequest order = _orderManager.getNextRequest();
		int sent = sendItem(order.getItem(), order.numberLeft(), order.getDestination().getRouter().getId());
		if (sent > 0){
			_orderManager.sendSuccessfull(sent);
		}
		else {
			_orderManager.sendFailed();
		}
		
	}

	@Override
	public void canProvide(LogisticsTransaction transaction) {
		// Check the transaction and see if we have helped already
		HashMap<ItemIdentifier, Integer> commited = transaction.getTotalPromised((IProvideItems) _itemSender);
		for (LogisticsRequest request : transaction.getRemainingRequests()){
			int canProvide = getAvailableItemCount(request.getItem());
			if (commited.containsKey(request.getItem())){
				canProvide -= commited.get(request.getItem());
			}
			if (canProvide < 1) continue;
			LogisticsPromise promise = new LogisticsPromise();
			promise.item = request.getItem();
			promise.numberOfItems = Math.min(canProvide, request.notYetAllocated());
			//TODO: FIX THIS CAST
			promise.sender = (IProvideItems) _itemSender;
			request.addPromise(promise);
			commited = transaction.getTotalPromised((IProvideItems) _itemSender);
		}
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		_orderManager.addOrder(new LogisticsRequest(promise.item, promise.numberOfItems, destination));
	}

	@Override
	public int getAvailableItemCount(ItemIdentifier item) {
		return getTotalItemCount(item) - _orderManager.totalItemsCountInOrders(item);
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getAllItems() {
		HashMap<ItemIdentifier, Integer> allItems = new HashMap<ItemIdentifier, Integer>(); 
		if (_invProvider.getInventory() == null) return allItems;
	
		InventoryUtil inv = getAdaptedUtil(_invProvider.getInventory());
		HashMap<ItemIdentifier, Integer> currentInv = inv.getItemsAndCount();
		for (ItemIdentifier currItem : currentInv.keySet()){
			if ( hasFilter() && ((isExcludeFilter && itemIsFiltered(currItem)) 
							|| (!isExcludeFilter && !itemIsFiltered(currItem)))) continue;

			if (!allItems.containsKey(currItem)){
				allItems.put(currItem, currentInv.get(currItem));
			}else {
				allItems.put(currItem, allItems.get(currItem) + currentInv.get(currItem));
			}
		}
		
		//Reduce what has been reserved.
		Iterator<ItemIdentifier> iterator = allItems.keySet().iterator();
		while(iterator.hasNext()){
			ItemIdentifier item = iterator.next();
		
			int remaining = allItems.get(item) - _orderManager.totalItemsCountInOrders(item);
			if (remaining < 1){
				iterator.remove();
			} else {
				allItems.put(item, remaining);	
			}
		}
		
		return allItems;	
	}

	@Override
	public IRouter getRouter() {
		//THIS IS NEVER SUPPOSED TO HAPPEN
		return null;
	}
	
	private int sendItem(ItemIdentifier item, int maxCount, UUID destination) {
		int sent = 0;
		if (_invProvider.getInventory() == null) return 0;
		InventoryUtil inv = getAdaptedUtil(_invProvider.getInventory());
		if (inv.itemCount(item)> 0){
			ItemStack removed = inv.getSingleItem(item);
			_itemSender.sendStack(removed, destination);
			sent++;
			maxCount--;
		}			

		return sent;
	}
	
	public int getTotalItemCount(ItemIdentifier item) {
		
		if (_invProvider.getInventory() == null) return 0;
		
		if (_filterUtil.getItemsAndCount().size() > 0
				&& ((this.isExcludeFilter && _filterUtil.getItemsAndCount().containsKey(item)) 
						|| ((!this.isExcludeFilter) && !_filterUtil.getItemsAndCount().containsKey(item)))) return 0;
		
		InventoryUtil inv = getAdaptedUtil(_invProvider.getInventory());
		return inv.itemCount(item);
	}
	
	private boolean hasFilter() {
		return _filterUtil.getItemsAndCount().size() > 0;
	}
	
	public boolean itemIsFiltered(ItemIdentifier item){
		return _filterUtil.getItemsAndCount().containsKey(item);
	}
	
	public InventoryUtil getAdaptedUtil(IInventory base){
		switch(_extractionMode){
			case LeaveFirst:
				base = new CroppedInventory(base, 1, 0);
				break;
			case LeaveLast:
				base = new CroppedInventory(base, 0, 1);
				break;
			case LeaveFirstAndLast:
				base = new CroppedInventory(base, 1, 1);
				break;
			case Leave1PerStack:
				return SimpleServiceLocator.inventoryUtilFactory.getOneHiddenInventoryUtil(base);
		}
		return SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(base);
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
}
