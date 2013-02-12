package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import cpw.mods.fml.common.network.Player;

import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.transport.TileGenericPipe;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.gui.hud.modules.HUDCraftingModule;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketModuleInvContent;
import logisticspipes.network.packets.PacketModuleInventoryChange;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.RequestManager;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsOrderManager;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.SearchNode;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ISidedInventory;

public class ModuleCrafting implements ILogisticsGuiModule, ICraftItems,
		IClientInformationProvider, IRequireReliableTransport, IHUDModuleHandler, IModuleWatchReciver, IOrderManagerContentReceiver {
	protected IInventoryProvider _invProvider;
	protected ISendRoutedItem _itemSender;
	protected IChassiePowerProvider _power;
	protected IWorldProvider _world;
	
	public int slot = 0;
	public int xCoord = 0;
	public int yCoord = 0;
	public int zCoord = 0;
	protected int itemsToExtract = 1;
	protected int stacksToExtract = 1;
	protected int neededEnergy = 10;
	
	public SimpleInventory _dummyInventory = new SimpleInventory(10, "Items", 127);

	protected final LinkedList<ItemIdentifierStack> _lostItems = new LinkedList<ItemIdentifierStack>();

	public int satelliteId = 0;

	public int priority = 0;
	
	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	
	private IHUDModuleRenderer HUD = new HUDCraftingModule(this);
	
	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	
	protected int _extras;
	private boolean doContentUpdate = false;
	private int throttleTick = 0;
	
	@Override
	public void registerHandler(IInventoryProvider invProvider,
			ISendRoutedItem itemSender, IWorldProvider world,
			IChassiePowerProvider powerProvider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerProvider;
		_world = world;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.slot = slot;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority) {
		for(int i= 0; i<9; i++)
		{
			if(i>=6 && isSatelliteConnected())
				break;
			if(getMaterials(i) != null)
			{
				ItemIdentifier wanted = ItemIdentifier.get(getMaterials(i));
				
				if(stack.equals(wanted))
				{
					return new SinkReply(SinkReply.FixedPriority.ItemSink, 99, false, false, 0, 99);
				}
			}
		}
		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		throttleTick++;
		if(throttleTick % 20 == 0)
		{
			throttleTick = 0;
			if (_lostItems.isEmpty()) {
				return;
			}
			final Iterator<ItemIdentifierStack> iterator = _lostItems.iterator();
			while (iterator.hasNext()) {
				// FIXME try partial requests
				if (RequestManager.request(iterator.next(), this, null)) {
					iterator.remove();
				}
			}
		}
		
		if (doContentUpdate) {
			checkContentUpdate();
		}
		
		if ((!_orderManager.hasOrders() && _extras < 1) || _world.getWorld().getWorldTime() % 6 != 0) return;
		
		ItemIdentifier wanteditem = getCraftedItem();
		if(wanteditem == null) return;

		MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, xCoord, yCoord, zCoord, _world.getWorld(), 2);
		
		int itemsleft = itemsToExtract;
		int stacksleft = stacksToExtract;
		while (itemsleft > 0 && stacksleft > 0 && (_orderManager.hasOrders() || _extras > 0)) {
			int maxtosend = Math.min(itemsleft, wanteditem.getMaxStackSize() * stacksleft);
			if(_orderManager.hasOrders()){
				maxtosend = Math.min(maxtosend, _orderManager.getNextRequest().getValue1().stackSize);
			} else {
				maxtosend = Math.min(maxtosend, _extras);
			}
			
			ItemStack extracted = null;
			IInventory inv = _invProvider.getRawInventory();
			if (inv instanceof ISpecialInventory) 
			{
				extracted = extractFromISpecialInventory((ISpecialInventory) inv, wanteditem, maxtosend);
			} 
			else if (inv instanceof ISidedInventory) 
			{
				IInventory sidedadapter = new SidedInventoryAdapter((ISidedInventory) inv, _invProvider.inventoryOrientation());
				extracted = extractFromIInventory(sidedadapter, wanteditem, maxtosend);
			}
			else if (inv instanceof IInventory) 
			{
				extracted = extractFromIInventory((IInventory) inv, wanteditem, maxtosend);
			}
			if(extracted == null) break;
			
			while (extracted.stackSize > 0) {
				int numtosend = Math.min(extracted.stackSize, ItemIdentifier.get(extracted).getMaxStackSize());
				if (_orderManager.hasOrders()) {
					Pair3<ItemIdentifierStack,IRequestItems,List<IRelayItem>> order = _orderManager.getNextRequest();
					numtosend = Math.min(numtosend, order.getValue1().stackSize);
					ItemStack stackToSend = extracted.splitStack(numtosend);
					itemsleft -= numtosend;
					stacksleft -= 1;
					_itemSender.sendStack(stackToSend, order.getValue2().getRouter().getSimpleID(),CoreRoutedPipe.ItemSendMode.Normal, order.getValue3());
					_orderManager.sendSuccessfull(stackToSend.stackSize);
				} else {
					ItemStack stackToSend = extracted.splitStack(numtosend);
					_extras = Math.max(_extras - numtosend, 0);
					itemsleft -= numtosend;
					stacksleft -= 1;
					_itemSender.sendStack(stackToSend, new Pair3<Integer, SinkReply, List<IFilter>>(-1, new SinkReply(SinkReply.FixedPriority.DefaultRoute, 0, true, false, 0, 1), null));
				}
			}
		}

	}
	
	private ItemStack extractFromISpecialInventory(ISpecialInventory inv, ItemIdentifier wanteditem, int count){
		ItemStack retstack = null;
		while(count > 0) {
			ItemStack[] stacks = inv.extractItem(false, _invProvider.inventoryOrientation(), 1);
			if(stacks == null || stacks.length < 1 || stacks[0] == null) break;
			ItemStack stack = stacks[0];
			if(stack.stackSize == 0) break;
			if(retstack == null) {
				if(!wanteditem.fuzzyMatch(stack)) break;
			} else {
				if(!retstack.isItemEqual(stack)) break;
				if(!ItemStack.areItemStackTagsEqual(retstack, stack)) break;
			}
			if(!_power.useEnergy(neededEnergy * stack.stackSize)) break;
			
			stacks = inv.extractItem(true, _invProvider.inventoryOrientation(), 1);
			if(stacks == null || stacks.length < 1 || stacks[0] == null) {
				LogisticsPipes.requestLog.info("crafting extractItem(true) got nothing from " + ((TileEntity)inv).toString());
				break;
			}
			if(!ItemStack.areItemStacksEqual(stack, stacks[0])) {
				LogisticsPipes.requestLog.info("crafting extract got a unexpected item from " + ((TileEntity)inv).toString());
			}
			if(retstack == null) {
				retstack = stack;
			} else {
				retstack.stackSize += stack.stackSize;
			}
			count -= stack.stackSize;
		}
		return retstack;
	}
	
	private ItemStack extractFromIInventory(IInventory inv, ItemIdentifier wanteditem, int count){
		IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
		int available = invUtil.itemCount(wanteditem);
		if(available == 0) return null;
		if(!_power.useEnergy(neededEnergy * Math.min(count, available))) {
			return null;
		}
		return invUtil.getMultipleItems(wanteditem, Math.min(count, available));
	}

	
	private void checkContentUpdate() {
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList();
		if(!oldList.equals(all)) {
			oldList.clear();
			oldList.addAll(all);
			MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord,slot, all).getPacket(), localModeWatchers);
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Satellite: "+ this.satelliteId);
		list.add("Priority: " + this.priority);
		list.add("Inventory: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}


	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Crafting_ID;
	}
	
	public SimpleInventory getDummyInventory() {
		return _dummyInventory;
	}
	public int getNextConnectSatelliteId(boolean prev) {
		final List<ExitRoute> routes = getRouter().getIRoutersByCost();
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
			IRouter satRouter = satellite.getRoutedPipe().getRouter();
			for (ExitRoute route:routes){
				if (route.destination == satRouter) {
					if (!prev && satellite.satelliteId > satelliteId && satellite.satelliteId < closestIdFound) { //
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < satelliteId && satellite.satelliteId > closestIdFound ) { 
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			return satelliteId;
		}
		return closestIdFound;

	}
	
	// This is called by the packet PacketCraftingPipeSatelliteId
	public void setSatelliteId(int satelliteId) {
		this.satelliteId = satelliteId;
	}

	public boolean isSatelliteConnected() {
		if(satelliteId == 0)
			return false;
		if(MainProxy.isClient())
		{
			return true;
		}
		final List<ExitRoute> routes = getRouter().getIRoutersByCost();
		for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
			if (satellite.satelliteId == satelliteId) {
				IRouter satRouter = satellite.getRoutedPipe().getRouter();
				for (ExitRoute route:routes) {
					if (route.destination == satRouter) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public IRouter getSatelliteRouter() {
		for (final BaseLogicSatellite satellite : BaseLogicSatellite.AllSatellites) {
			if (satellite.satelliteId == satelliteId) {
				return satellite.getRoutedPipe().getRouter();
			}
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_dummyInventory.readFromNBT(nbttagcompound, "");
		satelliteId = nbttagcompound.getInteger("satelliteid");	
		priority = nbttagcompound.getInteger("priority");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		_dummyInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setInteger("satelliteid", satelliteId);	
		nbttagcompound.setInteger("priority", priority);
	}


	@Override
	public void itemArrived(ItemIdentifierStack item) {
	}

	@Override
	public void itemLost(ItemIdentifierStack item) {
		_lostItems.add(item);
	}

	public void openAttachedGui(EntityPlayer player) {
		final WorldUtil worldUtil = new WorldUtil(_world.getWorld(), xCoord, yCoord, zCoord);
		boolean found = false;
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
				if (provider.canOpenGui(tile.tile)) {
					found = true;
					break;
				}
			}

			if (!found)
				found = (tile.tile instanceof IInventory && !(tile.tile instanceof TileGenericPipe));

			if (found) {
				Block block = _world.getWorld().getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord) < Block.blocksList.length ? Block.blocksList[_world.getWorld().getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord)] : null;
				if(block != null) {
					if(block.onBlockActivated(_world.getWorld(), tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, player, 0, 0, 0, 0)){
						break;
					}
				}
			}
		}
	}

	public void importFromCraftingTable() {
		final WorldUtil worldUtil = new WorldUtil(_world.getWorld(), xCoord, yCoord, zCoord);
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
				if (provider.importRecipe(tile.tile, _dummyInventory))
					break;
			}
		}
		
	}

	public void handleStackMove(int number) {
		if(MainProxy.isClient()) {
			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_STACK_MOVE,xCoord,yCoord,zCoord,number).getPacket());
		}
		ItemStack stack = _dummyInventory.getStackInSlot(number);
		if(stack == null ) return;
		for(int i = 6;i < 9;i++) {
			ItemStack stackb = _dummyInventory.getStackInSlot(i);
			if(stackb == null) {
				_dummyInventory.setInventorySlotContents(i, stack);
				_dummyInventory.setInventorySlotContents(number, null);
				break;
			}
		}
	}
	
	public void priorityUp() {
		priority++;
	}
	
	public void priorityDown() {
		priority--;
	}
	
	
	@Override
	public void canProvide(RequestTreeNode tree, Map<ItemIdentifier, Integer> donePromisses, List<IFilter> filters) {

		if (_extras < 1) return;
		ItemIdentifier providedItem = getCraftedItem();
		if (tree.getStack().getItem() != providedItem) return;

		
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.isFilteredItem(tree.getStack().getItem()) || filter.blockProvider()) return;
		}
		
		int alreadyPromised = donePromisses.containsKey(providedItem) ? donePromisses.get(providedItem) : 0; 
		if (alreadyPromised >= _extras) return;
		int remaining = _extras - alreadyPromised;
		LogisticsExtraPromise promise = new LogisticsExtraPromise();
		promise.item = providedItem;
		promise.numberOfItems = Math.min(remaining, tree.getMissingItemCount());
		promise.sender = this;
		promise.provided = true;
		List<IRelayItem> relays = new LinkedList<IRelayItem>();
		for(IFilter filter:filters) {
			relays.add(filter);
		}
		promise.relayPoints = relays;
		tree.addPromise(promise);
	}

	@Override
	public CraftingTemplate addCrafting() {
		
		ItemIdentifierStack stack = getCraftedItemStack(); 
		if ( stack == null) return null;
		
		CraftingTemplate template = new CraftingTemplate(stack, this, priority);

		//Check all materials
		boolean hasSatellite = isSatelliteConnected(); 
		for (int i = 0; i < 9; i++){
			ItemStack resourceStack = getMaterials(i);
			if (resourceStack == null || resourceStack.stackSize == 0) continue;
			if (i < 6 || !hasSatellite){
				template.addRequirement(ItemIdentifierStack.GetFromStack(resourceStack), this);
			}
			else{
				template.addRequirement(ItemIdentifierStack.GetFromStack(resourceStack), getSatelliteRouter().getPipe());
			}
				
		}
		return template;
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (promise instanceof LogisticsExtraPromise && ((LogisticsExtraPromise)promise).provided) {
			_extras -= promise.numberOfItems;
		}
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, promise.relayPoints);
		MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, xCoord, yCoord, zCoord, _world.getWorld(), 2);
	}

	public int getAvailableItemCount(ItemIdentifier item) {
		return 0;
	}

	@Override
	public void registerExtras(int count) {
		_extras += count;
		LogisticsPipes.requestLog.info(count + " extras registered");
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list,List<IFilter> filters) {}

	public int compareTo(IRequestItems other){
		return this.getID()-other.getID();
	}
	
	@Override
	public int getID(){
		return Configs.ItemModuleId;
	}
	
	public void itemCouldNotBeSend(ItemIdentifierStack item) {
		itemLost(item);
	}

	
	/* ** INTERFACE TO PIPE ** */
	public ItemIdentifier getCraftedItem() {
		if(_dummyInventory.getStackInSlot(9) == null) return null;
		return ItemIdentifier.get(_dummyInventory.getStackInSlot(9));
	}
	
	public ItemIdentifierStack getCraftedItemStack() {
		if(_dummyInventory.getStackInSlot(9) == null) return null;
		return ItemIdentifierStack.GetFromStack(_dummyInventory.getStackInSlot(9));
	}

	public ItemStack getMaterials(int slotnr) {
		return _dummyInventory.getStackInSlot(slotnr);
	}

	
	public void setDummyInventorySlot(int slot, ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}

	public IRouter getRouter() {
		return _itemSender.getRouter();
	}
	
	public IHUDModuleRenderer getRenderer() {
		return HUD;
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
		doContentUpdate = true;
		MainProxy.sendPacketToPlayer(new PacketModuleInventoryChange(NetworkConstants.CRAFTING_MODULE_IMPORT_BACK, xCoord, yCoord, zCoord, slot, _dummyInventory).getPacket(),(Player) player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
		if(localModeWatchers.isEmpty())
			doContentUpdate = false;
	}
	
	/* ** NON NETWORKING ** */
	@SuppressWarnings("deprecation")
	public void paintPathToSatellite() {
		final IRouter satelliteRouter = getSatelliteRouter();
		if (satelliteRouter == null) {
			return;
		}

		getRouter().displayRouteTo(satelliteRouter.getSimpleID());
	}

	@Override
	public void setOrderManagerContent(Collection<ItemIdentifierStack> _allItems) {
		displayList.clear();
		displayList.addAll(_allItems);
		
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		List<ItemIdentifier> lst = new LinkedList<ItemIdentifier>();
		lst.addAll(_dummyInventory.getItemsAndCount().keySet());
		return lst;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

}
