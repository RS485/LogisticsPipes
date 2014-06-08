/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.DelayQueue;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.gui.hud.HUDCrafting;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.logistics.LogisticsManager;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.block.CraftingPipeNextAdvancedSatellitePacket;
import logisticspipes.network.packets.block.CraftingPipePrevAdvancedSatellitePacket;
import logisticspipes.network.packets.cpipe.CPipeNextSatellite;
import logisticspipes.network.packets.cpipe.CPipePrevSatellite;
import logisticspipes.network.packets.cpipe.CPipeSatelliteId;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImport;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.network.packets.cpipe.CraftingAdvancedSatelliteId;
import logisticspipes.network.packets.cpipe.CraftingFuzzyFlag;
import logisticspipes.network.packets.cpipe.CraftingPipeOpenConnectedGuiPacket;
import logisticspipes.network.packets.gui.GuiArgument;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.module.RequestCraftingPipeUpdatePacket;
import logisticspipes.network.packets.orderer.OrdererManagerContent;
import logisticspipes.network.packets.pipe.CraftingPipePriorityDownPacket;
import logisticspipes.network.packets.pipe.CraftingPipePriorityUpPacket;
import logisticspipes.network.packets.pipe.CraftingPipeStackMovePacket;
import logisticspipes.network.packets.pipe.CraftingPipeUpdatePacket;
import logisticspipes.network.packets.pipe.CraftingPriority;
import logisticspipes.network.packets.pipe.FluidCraftingAdvancedSatelliteId;
import logisticspipes.network.packets.pipe.FluidCraftingAmount;
import logisticspipes.network.packets.pipe.FluidCraftingPipeAdvancedSatelliteNextPacket;
import logisticspipes.network.packets.pipe.FluidCraftingPipeAdvancedSatellitePrevPacket;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.signs.CraftingPipeSign;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IFuzzyRecipeProvider;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.RequestType;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.routing.order.LogisticsOrderManager;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.CraftingRequirement;
import logisticspipes.utils.DelayedGeneric;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.BufferMode;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;

@CCType(name = "LogisticsPipes:Crafting")
public class PipeItemsCraftingLogistics extends CoreRoutedPipe implements ICraftItems, IRequireReliableTransport, IHeadUpDisplayRendererProvider, IChangeListener, IOrderManagerContentReceiver, IHavePriority {

	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager(this);
	protected ModuleCrafter craftingModule;
	
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final HUDCrafting HUD = new HUDCrafting(this);
	
	public final LinkedList<LogisticsOrder> _extras = new LinkedList<LogisticsOrder>();
	private boolean init = false;
	private boolean doContentUpdate = true;
	private WeakReference<TileEntity> lastAccessedCrafter = new WeakReference<TileEntity>(null);
	
	public boolean waitingForCraft = false;
	
	public PipeItemsCraftingLogistics(int itemID) {
		super(itemID);
		// module still relies on this for some code
		craftingModule = new ModuleCrafter(this);
		
//		craftingModule.registerHandler(this, this, this);
		throttleTime = 40;
	}
	
	public PipeItemsCraftingLogistics(PipeTransportLogistics transport, int itemID) {
		super(transport, itemID);
		craftingModule = new ModuleCrafter(this);
//		craftingModule.registerHandler(this, this, this);
		throttleTime = 40;
	}

	protected int neededEnergy() {
		return 10;
	}
	
	protected int itemsToExtract() {
		return 1;
	}
	
	protected int stacksToExtract() {
		return 1;
	}
	
	private List<AdjacentTile> _cachedCrafters = null;
	protected List<AdjacentTile> locateCrafters()	{
		if(_cachedCrafters !=null)
			return _cachedCrafters;
		WorldUtil worldUtil = new WorldUtil(this.getWorld(), this.getX(), this.getY(), this.getZ());
		LinkedList<AdjacentTile> crafters = new LinkedList<AdjacentTile>();
		for (AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)){
			if (tile.tile instanceof TileGenericPipe) continue;
			if (!(tile.tile instanceof IInventory)) continue;
			crafters.add(tile);
		}
		_cachedCrafters=crafters;
		return _cachedCrafters;
	}
	
	public void clearCraftersCache() {
		_cachedCrafters = null;
	}
	
	
	@Override
	public void onNeighborBlockChange(int blockId) {
		clearCraftersCache();
		super.onNeighborBlockChange(blockId);
	}
	
	@Override
	public void onAllowedRemoval() {
		while(_orderManager.hasOrders()) {
			_orderManager.sendFailed();
		}
	}

	private ItemStack extractFromISpecialInventory(ISpecialInventory inv, ItemIdentifier wanteditem, int count){
		ItemStack retstack = null;
		while(count > 0) {
			ItemStack[] stacks = inv.extractItem(false, ForgeDirection.UNKNOWN, 1);
			if(stacks == null || stacks.length < 1 || stacks[0] == null) break;
			ItemStack stack = stacks[0];
			if(stack.stackSize == 0) break;
			if(retstack == null) {
				if(!wanteditem.fuzzyMatch(stack)) break;
			} else {
				if(!retstack.isItemEqual(stack)) break;
				if(!ItemStack.areItemStackTagsEqual(retstack, stack)) break;
			}
			if(!useEnergy(neededEnergy() * stack.stackSize)) break;
			
			stacks = inv.extractItem(true, ForgeDirection.UNKNOWN, 1);
			if(stacks == null || stacks.length < 1 || stacks[0] == null) {
				LogisticsPipes.requestLog.info("crafting extractItem(true) got nothing from " + ((Object)inv).toString());
				break;
			}
			if(!ItemStack.areItemStacksEqual(stack, stacks[0])) {
				LogisticsPipes.requestLog.info("crafting extract got a unexpected item from " + ((Object)inv).toString());
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
		if(!useEnergy(neededEnergy() * Math.min(count, available))) {
			return null;
		}
		return invUtil.getMultipleItems(wanteditem, Math.min(count, available));
	}
	
	private ItemStack extractFromLogisticsCraftingTable(LogisticsCraftingTableTileEntity tile, ItemIdentifier wanteditem, int count) {
		ItemStack extracted = extractFromIInventory(tile, wanteditem, count);
		if(extracted != null) {
			return extracted;
		}
		ItemStack retstack = null;
		while(count > 0) {
			ItemStack stack = tile.getOutput(wanteditem, this);
			if(stack == null || stack.stackSize == 0) break;
			if(retstack == null) {
				if(!wanteditem.fuzzyMatch(stack)) break;
			} else {
				if(!retstack.isItemEqual(stack)) break;
				if(!ItemStack.areItemStackTagsEqual(retstack, stack)) break;
			}
			if(!useEnergy(neededEnergy() * stack.stackSize)) break;
			
			if(retstack == null) {
				retstack = stack;
			} else {
				retstack.stackSize += stack.stackSize;
			}
			count -= stack.stackSize;
		}
		return retstack;		
	}
	
	public void enableUpdateRequest() {
		init = false;
	}
	
	@Override
	public void ignoreDisableUpdateEntity() {
		if(!init) {
			if(MainProxy.isClient(getWorld())) {
				if(FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().thePlayer.sendQueue != null){
					MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestCraftingPipeUpdatePacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
				}
			}
			init = true;
		}
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (doContentUpdate) {
			checkContentUpdate();
		}
		
		if(_orderManager.hasOrders()) {
			cacheAreAllOrderesToBuffer();
			if(_orderManager.isFirstOrderWatched()) {
				TileEntity tile = lastAccessedCrafter.get();
				if(tile != null) {
					_orderManager.setMachineProgress(SimpleServiceLocator.machineProgressProvider.getProgressForTile(tile));
				} else {
					_orderManager.setMachineProgress((byte) 0);
				}
			}
		} else {
			cachedAreAllOrderesToBuffer = false;
		}
		
		if (getWorld().getTotalWorldTime() % 6 != 0) return;

		waitingForCraft = false;
		
		if((!_orderManager.hasOrders() && _extras.isEmpty())) return;
		
		waitingForCraft = true;
		
		List<AdjacentTile> crafters = locateCrafters();
		if (crafters.size() < 1 ) {
			if (_orderManager.hasOrders()) {
				_orderManager.sendFailed();
			} else {
				_extras.clear();
			}
			return;
		}
		
		List<ItemIdentifierStack> wanteditem = getCraftedItems();
		if(wanteditem == null || wanteditem.isEmpty()) return;

		MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, getX(), getY(), getZ(), this.getWorld(), 2);
		
		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		while (itemsleft > 0 && stacksleft > 0 && (_orderManager.hasOrders() || !_extras.isEmpty())) {
			LogisticsOrder nextOrder;
			boolean processingOrder=false;
			if(_orderManager.hasOrders()){
				nextOrder = _orderManager.peekAtTopRequest(RequestType.CRAFTING); // fetch but not remove.
				processingOrder=true;
			} else {
				nextOrder = _extras.getFirst(); // fetch but not remove.
			}
			int maxtosend = Math.min(itemsleft, nextOrder.getItem().getStackSize());
			maxtosend = Math.min(nextOrder.getItem().getItem().getMaxStackSize(), maxtosend);
			// retrieve the new crafted items
			ItemStack extracted = null;
			AdjacentTile tile = null;
			for (Iterator<AdjacentTile> it = crafters.iterator(); it.hasNext();) {
				tile = it.next();
				if (tile.tile instanceof LogisticsCraftingTableTileEntity) {
					extracted = extractFromLogisticsCraftingTable((LogisticsCraftingTableTileEntity)tile.tile, nextOrder.getItem().getItem(), maxtosend);
				} else if (tile.tile instanceof ISpecialInventory) {
					extracted = extractFromISpecialInventory((ISpecialInventory) tile.tile, nextOrder.getItem().getItem(), maxtosend);
				} else if (tile.tile instanceof net.minecraft.inventory.ISidedInventory) {
					IInventory sidedadapter = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) tile.tile, ForgeDirection.UNKNOWN,true);
					extracted = extractFromIInventory(sidedadapter, nextOrder.getItem().getItem(), maxtosend);
				} else if (tile.tile instanceof IInventory) {
					extracted = extractFromIInventory((IInventory)tile.tile, nextOrder.getItem().getItem(), maxtosend);
				}
				if (extracted != null && extracted.stackSize > 0) {
					break;
				}
			}
			if(extracted == null || extracted.stackSize == 0) break;
			lastAccessedCrafter = new WeakReference<TileEntity>(tile.tile);
			// send the new crafted items to the destination
			ItemIdentifier extractedID = ItemIdentifier.get(extracted);
			while (extracted.stackSize > 0) {
				if(nextOrder.getItem().getItem() != extractedID) {
					LogisticsOrder startOrder = nextOrder;
					if(_orderManager.hasOrders()) {
					do {
						_orderManager.deferSend();
						nextOrder = _orderManager.peekAtTopRequest(RequestType.CRAFTING);
					} while(nextOrder.getItem().getItem() != extractedID && startOrder != nextOrder);
					}
					if(startOrder == nextOrder) {
						int numtosend = Math.min(extracted.stackSize, extractedID.getMaxStackSize());
						if(numtosend == 0)
							break;
						stacksleft -= 1;
						itemsleft -= numtosend;
						ItemStack stackToSend = extracted.splitStack(numtosend);
						//Route the unhandled item
						transport.sendItem(stackToSend);
						continue;
					}
				}
				int numtosend = Math.min(extracted.stackSize, extractedID.getMaxStackSize());
				numtosend = Math.min(numtosend, nextOrder.getItem().getStackSize()); 
				if(numtosend == 0)
					break;
				stacksleft -= 1;
				itemsleft -= numtosend;
				ItemStack stackToSend = extracted.splitStack(numtosend);
				if (processingOrder) {
					SinkReply reply = LogisticsManager.canSink(nextOrder.getDestination().getRouter(), null, true, ItemIdentifier.get(stackToSend), null, true, false);
					boolean defersend = false;
					if(reply == null || reply.bufferMode != BufferMode.NONE || reply.maxNumberOfItems < 1) {
						defersend = true;
					}
					IRoutedItem item = SimpleServiceLocator.routedItemHelper.createNewTravelItem(stackToSend);
					item.setDestination(nextOrder.getDestination().getRouter().getSimpleID());
					item.setTransportMode(TransportMode.Active);
					super.queueRoutedItem(item, tile.orientation);
					_orderManager.sendSuccessfull(stackToSend.stackSize, defersend, item);
					if(_orderManager.hasOrders()){
						nextOrder = _orderManager.peekAtTopRequest(RequestType.CRAFTING); // fetch but not remove.
					} else {
						processingOrder = false;
						if(!_extras.isEmpty())
						nextOrder = _extras.getFirst();
					}
				} else {
					removeExtras(numtosend,nextOrder.getItem().getItem());
					transport.sendItem(stackToSend);
				}
			}
		}
	}
	
	private boolean cachedAreAllOrderesToBuffer;
	
	public boolean areAllOrderesToBuffer() {
		return cachedAreAllOrderesToBuffer;
	}
	
	public void cacheAreAllOrderesToBuffer() {
		boolean result = true;
		for(LogisticsOrder order:_orderManager) {
			SinkReply reply = LogisticsManager.canSink(order.getDestination().getRouter(), null, true, order.getItem().getItem(), null, true, false);
			if(reply != null && reply.bufferMode != BufferMode.BUFFERED && reply.maxNumberOfItems >= 1) {
				result = false;
			}
		}
		cachedAreAllOrderesToBuffer = result;
	}
	
	private void removeExtras(int numToSend, ItemIdentifier item) {
		Iterator<LogisticsOrder> i = _extras.iterator();
		while(i.hasNext()){
			ItemIdentifierStack e = i.next().getItem();
			if(e.getItem()== item) {
				if(numToSend >= e.getStackSize()) {
					numToSend -= e.getStackSize();
					i.remove();
					if(numToSend == 0) {
						return;
					}
				} else {
					e.setStackSize(e.getStackSize() - numToSend);
					break;
				}
			}
		}
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CRAFTER_TEXTURE;
	}

	@Override
	public void canProvide(RequestTreeNode tree, int donePromisses, List<IFilter> filters) {
		
		if (!isEnabled()){
			return;
		}
		
		if (_extras.isEmpty()) return;
		
		ItemIdentifier requestedItem = tree.getStackItem();
		List<ItemIdentifierStack> providedItem = getCraftedItems();
		for(ItemIdentifierStack item:providedItem) {
			if(item.getItem() == requestedItem) {
				return;
			}
		}
		if (!providedItem.contains(requestedItem)) return;

		
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.isFilteredItem(requestedItem.getUndamaged()) || filter.blockProvider()) return;
		}
		int remaining = 0;
		for(LogisticsOrder extra:_extras){
			if(extra.getItem().getItem()==requestedItem){
				remaining += extra.getItem().getStackSize();
			}
				
		}
		remaining -= donePromisses;
		if (remaining < 1) return;
		LogisticsExtraPromise promise = new LogisticsExtraPromise();
		promise.item = requestedItem;
		promise.numberOfItems = Math.min(remaining, tree.getMissingItemCount());
		promise.sender = this;
		promise.provided = true;
		tree.addPromise(promise);
	}

	@Override
	public CraftingTemplate addCrafting(ItemIdentifier toCraft) {
		
		if (!isEnabled()){
			return null;
		}		
		
		return craftingModule.addCrafting(toCraft);
	}

	@Override
	public LogisticsOrder fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (promise instanceof LogisticsExtraPromise) {
			removeExtras(promise.numberOfItems, promise.item);
		}
		MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, getX(), getY(), getZ(), this.getWorld(), 2);
		return _orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination,RequestType.CRAFTING);
	}

	@Override
	public void registerExtras(LogisticsPromise promise) {		
		ItemIdentifierStack stack = new ItemIdentifierStack(promise.item,promise.numberOfItems);
		_extras.add(new LogisticsOrder(stack, null, RequestType.EXTRA));
		LogisticsPipes.requestLog.info(stack.getStackSize() + " extras registered");
	}

	
	
	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list,List<IFilter> filters) {
		craftingModule.getAllItems(list, filters);
	}

	@Override
	public List<ItemIdentifierStack> getCraftedItems() {
		return craftingModule.getCraftedItems();
	}
	@Override
	public ModuleCrafter getLogisticsModule() {
		return craftingModule;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	@Override
	public int getTodo() {
		return _orderManager.totalItemsCountInAllOrders();
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererManagerContent.class).setIdentList(oldList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		} else {
			super.playerStartWatching(player, mode);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		super.playerStopWatching(player, mode);
		localModeWatchers.remove(player);
	}

	@Override
	public void listenedChanged() {
		doContentUpdate = true;
	}

	private void checkContentUpdate() {
		doContentUpdate = false;
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList(this.getWorld());
		if(!oldList.equals(all)) {
			oldList.clear();
			oldList.addAll(all);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(OrdererManagerContent.class).setIdentList(all).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		}
	}

	@Override
	public void setOrderManagerContent(Collection<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}
	
	@Override
	public double getLoadFactor() {
		return (_orderManager.totalItemsCountInAllOrders()+63.0)/64.0;
	}
	
	/* ComputerCraftCommands */
	@CCCommand(description="Imports the crafting recipe from the connected machine/crafter")
	@CCQueued(prefunction="testImportAccess")
	public void reimport() {
		craftingModule.importFromCraftingTable(null);
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		return craftingModule.getSpecificInterests();
	}

	@Override
	public int getPriority() {
		return craftingModule.getPriority();
	}

	public List<ForgeDirection> getCraftingSigns() {
		return craftingModule.getCraftingSigns();
	}

	public boolean setCraftingSign(ForgeDirection dir, boolean b, EntityPlayer player) {
		return craftingModule.setCraftingSign(dir, b, player);
	}
	
	public ModernPacket getCPipePacket() {
		return craftingModule.getCPipePacket();
		
	}
	
	public void handleCraftingUpdatePacket(CraftingPipeUpdatePacket packet) {
		craftingModule.handleCraftingUpdatePacket(packet);
	}
	

	
	protected final DelayQueue< DelayedGeneric<ItemIdentifierStack>> _lostItems = new DelayQueue< DelayedGeneric<ItemIdentifierStack>>();
	
	/* ** SATELLITE CODE ** */

	public boolean isSatelliteConnected() {
		return craftingModule.isSatelliteConnected();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		craftingModule.readFromNBT(nbttagcompound);
		/*
		_dummyInventory.readFromNBT(nbttagcompound, "");
		_liquidInventory.readFromNBT(nbttagcompound, "FluidInv");
		satelliteId = nbttagcompound.getInteger("satelliteid");
		
		priority = nbttagcompound.getInteger("priority");
		for(int i=0;i<9;i++) {
			advancedSatelliteIdArray[i] = nbttagcompound.getInteger("advancedSatelliteId" + i);
		}
		for(int i=0;i<9;i++) {
			fuzzyCraftingFlagArray[i] = nbttagcompound.getByte("fuzzyCraftingFlag" + i);
		}
		for(int i=0;i<6;i++) {
			craftingSigns[i] = nbttagcompound.getBoolean("craftingSigns" + i);
		}
		if(nbttagcompound.hasKey("FluidAmount")) {
			amount = nbttagcompound.getIntArray("FluidAmount");
		}
		if(amount.length < ItemUpgrade.MAX_LIQUID_CRAFTER) {
			amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			liquidSatelliteIdArray[i] = nbttagcompound.getInteger("liquidSatelliteIdArray" + i);
		}
		liquidSatelliteId = nbttagcompound.getInteger("liquidSatelliteId"); */
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		craftingModule.writeToNBT(nbttagcompound);
		/*
		_dummyInventory.writeToNBT(nbttagcompound, "");
		_liquidInventory.writeToNBT(nbttagcompound, "FluidInv");
		nbttagcompound.setInteger("satelliteid", satelliteId);
		
		nbttagcompound.setInteger("priority", priority);
		for(int i=0;i<9;i++) {
			nbttagcompound.setInteger("advancedSatelliteId" + i, advancedSatelliteIdArray[i]);
		}
		for(int i=0;i<9;i++) {
			nbttagcompound.setByte("fuzzyCraftingFlag" + i, (byte)fuzzyCraftingFlagArray[i]);
		}
		for(int i=0;i<6;i++) {
			nbttagcompound.setBoolean("craftingSigns" + i, craftingSigns[i]);
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			nbttagcompound.setInteger("liquidSatelliteIdArray" + i, liquidSatelliteIdArray[i]);
		}
		nbttagcompound.setIntArray("FluidAmount", amount);
		nbttagcompound.setInteger("liquidSatelliteId", liquidSatelliteId);*/
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(GuiArgument.class)
				.setGuiID(GuiIDs.GUI_CRAFTINGPIPE_ID)
				.setArgs(new Object[]{((CoreRoutedPipe)this.container.pipe).getUpgradeManager().isAdvancedSatelliteCrafter(),
						((CoreRoutedPipe)this.container.pipe).getUpgradeManager().getFluidCrafter(),
						amount,
						((CoreRoutedPipe)this.container.pipe).getUpgradeManager().hasByproductExtractor(),
						((CoreRoutedPipe)this.container.pipe).getUpgradeManager().isFuzzyCrafter()}),
						(Player) entityplayer);
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_CRAFTINGPIPE_ID, getWorld(), getX(), getY(), getZ());
	}

	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		if (_lostItems.isEmpty()) {
			return;
		}
		DelayedGeneric<ItemIdentifierStack> lostItem = _lostItems.poll();
		while (lostItem != null) {
			
			ItemIdentifierStack stack = lostItem.get();
			if(_orderManager.hasOrders()) { 
				SinkReply reply = LogisticsManager.canSink(getRouter(), null, true, stack.getItem(), null, true, true);
				if(reply == null || reply.maxNumberOfItems < 1) {
					_lostItems.add(new DelayedGeneric<ItemIdentifierStack>(stack, 5000));
					lostItem = _lostItems.poll();
					continue;
				}
			}
			int received = RequestTree.requestPartial(stack, (CoreRoutedPipe) container.pipe);
			if(received < stack.getStackSize()) {
				stack.setStackSize(stack.getStackSize() - received);
				_lostItems.add(new DelayedGeneric<ItemIdentifierStack>(stack, 5000));
			}
			lostItem = _lostItems.poll();
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item) {
	}

	@Override
	public void itemLost(ItemIdentifierStack item) {
		_lostItems.add(new DelayedGeneric<ItemIdentifierStack>(item, 5000));
	}

	public IInventory getDummyInventory() {
		return craftingModule.getDummyInventory();
	}

	public IInventory getFluidInventory() {
		return craftingModule.getFluidInventory();
	}

	public void setNextSatellite(EntityPlayer player, int integer) {
		craftingModule.setNextSatellite(player, integer);
	}

	public void setPrevSatellite(EntityPlayer player, int integer) {
		craftingModule.setPrevSatellite(player, integer);
	}
	
	public boolean hasCraftingSign() {
		for(int i=0;i<6;i++) {
			if(signItem[i] instanceof CraftingPipeSign) {
				return true;
			}
		}
		return false;
	}

}
