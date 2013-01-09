package logisticspipes.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import logisticspipes.gui.hud.modules.HUDProviderModule;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.ILegacyActiveModule;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketModuleInvContent;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsOrderManager;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.CroppedInventory;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
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
	protected IChassiePowerProvider _power;
	
	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager();
	
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

	public LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	
	private IHUDModuleRenderer HUD = new HUDProviderModule(this);

	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	
	public ModuleProvider() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
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
	
	@Override	public SinkReply sinksItem(ItemStack item) {return null;}

	@Override	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {
		if(MainProxy.isClient()) return;
		if (++currentTick < ticksToAction) return;
		currentTick = 0;
		checkUpdate(null);
		while (_orderManager.hasOrders()) {
			Pair<ItemIdentifierStack,IRequestItems> order = _orderManager.getNextRequest();
			int sent = sendItem(order.getValue1().getItem(), order.getValue1().stackSize, order.getValue2().getRouter().getId());
			
			if(!_power.useEnergy(neededEnergy())) break;
			MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, xCoord, yCoord, this.zCoord, _world.getWorld(), 3);
			if (sent > 0) {
				_orderManager.sendSuccessfull(sent);
			} else {
				_orderManager.sendFailed();
				break;
			}
		}
	}

	@Override
	public void canProvide(RequestTreeNode tree, Map<ItemIdentifier, Integer> donePromisses) {
		int canProvide = getAvailableItemCount(tree.getStack().getItem());
		if (donePromisses.containsKey(tree.getStack().getItem())) {
			canProvide -= donePromisses.get(tree.getStack().getItem());
		}
		if (canProvide < 1) return;
		LogisticsPromise promise = new LogisticsPromise();
		promise.item = tree.getStack().getItem();
		promise.numberOfItems = Math.min(canProvide, tree.getMissingItemCount());
		//TODO: FIX THIS CAST
		promise.sender = (IProvideItems) _itemSender;
		tree.addPromise(promise);
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination);
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
	
	protected int sendItem(ItemIdentifier item, int maxCount, UUID destination) {
		int sent = 0;
		if (_invProvider.getInventory() == null) return 0;
		InventoryUtil inv = getAdaptedUtil(_invProvider.getInventory());
		if (inv.itemCount(item)> 0){
			ItemStack removed = inv.getSingleItem(item);
			if(removed != null) {
				_itemSender.sendStack(removed, destination);
				sent++;
				maxCount--;
			}
		}			

		return sent;
	}
	
	public int getTotalItemCount(ItemIdentifier item) {
		
		if (_invProvider.getInventory() == null) return 0;
		
		if (!_filterInventory.isEmpty()
				&& ((this.isExcludeFilter && _filterInventory.containsItem(item)) 
						|| ((!this.isExcludeFilter) && !_filterInventory.containsItem(item)))) return 0;
		
		InventoryUtil inv = getAdaptedUtil(_invProvider.getInventory());
		return inv.itemCount(item);
	}
	
	private boolean hasFilter() {
		return !_filterInventory.isEmpty();
	}
	
	public boolean itemIsFiltered(ItemIdentifier item){
		return _filterInventory.containsItem(item);
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
			default:
				break;
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

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.slot = slot;
	}
	
	private void checkUpdate(EntityPlayer player) {
		displayList.clear();
		HashMap<ItemIdentifier, Integer> list = getAllItems();
		for(ItemIdentifier item :list.keySet()) {
			displayList.add(new ItemIdentifierStack(item, list.get(item)));
		}
		if(!oldList.equals(displayList)) {
			MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, xCoord, yCoord, zCoord, slot, displayList).getPacket(), localModeWatchers);
			oldList.clear();
			oldList.addAll(displayList);
		}
		if(player != null) {
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
	public void handleInvContent(LinkedList<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}
}
