/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.DelayQueue;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.gui.hud.HUDCrafting;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.logistics.LogisticsManagerV2;
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
import logisticspipes.network.packets.cpipe.CraftingPipeOpenConnectedGuiPacket;
import logisticspipes.network.packets.gui.GuiArgument;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.module.RequestCraftingPipeUpdatePacket;
import logisticspipes.network.packets.orderer.OrdererManagerContent;
import logisticspipes.network.packets.pipe.CraftingPipePriorityDownPacket;
import logisticspipes.network.packets.pipe.CraftingPipePriorityUpPacket;
import logisticspipes.network.packets.pipe.CraftingPipeStackMovePacket;
import logisticspipes.network.packets.pipe.CraftingPriority;
import logisticspipes.network.packets.pipe.FluidCraftingAdvancedSatelliteId;
import logisticspipes.network.packets.pipe.FluidCraftingAmount;
import logisticspipes.network.packets.pipe.FluidCraftingPipeAdvancedSatelliteNextPacket;
import logisticspipes.network.packets.pipe.FluidCraftingPipeAdvancedSatellitePrevPacket;
import logisticspipes.network.packets.pipe.PipeUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsOrderManager;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.DelayedGeneric;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.utils.Utils;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;

@CCType(name = "LogisticsPipes:Crafting")
public class PipeItemsCraftingLogistics extends CoreRoutedPipe implements ICraftItems, IRequireReliableTransport, IHeadUpDisplayRendererProvider, IChangeListener, IOrderManagerContentReceiver, IHavePriority {

	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager(this);

	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final HUDCrafting HUD = new HUDCrafting(this);
	
	public final LinkedList<Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>>> _extras = new LinkedList<Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>>>();
	private boolean init = false;
	private boolean doContentUpdate = true;
	
	public boolean waitingForCraft = false;
	
	public PipeItemsCraftingLogistics(int itemID) {
		super(itemID);
		throttleTime = 40;
	}
	
	public PipeItemsCraftingLogistics(PipeTransportLogistics transport, int itemID) {
		super(transport, itemID);
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
//TODO 				MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.REQUEST_CRAFTING_PIPE_UPDATE, getX(), getY(), getZ()).getPacket());
					MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestCraftingPipeUpdatePacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
				}
			}
			init = true;
		}
	}

	@Override
	public void enabledUpdateEntity() {
		if (doContentUpdate) {
			checkContentUpdate();
		}
		
		if (getWorld().getWorldTime() % 6 != 0) return;

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
			Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>> nextOrder;
			boolean processingOrder=false;
			if(_orderManager.hasOrders()){
				nextOrder = _orderManager.peekAtTopRequest(); // fetch but not remove.
				processingOrder=true;
			} else {
				nextOrder = _extras.getFirst(); // fetch but not remove.
			}
			int maxtosend = Math.min(itemsleft, nextOrder.getValue1().stackSize);
			maxtosend = Math.min(nextOrder.getValue1().getItem().getMaxStackSize(), maxtosend);
			// retrieve the new crafted items
			ItemStack extracted = null;
			AdjacentTile tile = null;
			for (Iterator<AdjacentTile> it = crafters.iterator(); it.hasNext();) {
				tile = it.next();
				if (tile.tile instanceof LogisticsCraftingTableTileEntity) {
					extracted = extractFromLogisticsCraftingTable((LogisticsCraftingTableTileEntity)tile.tile, nextOrder.getValue1().getItem(), maxtosend);
				} else if (tile.tile instanceof ISpecialInventory) {
					extracted = extractFromISpecialInventory((ISpecialInventory) tile.tile, nextOrder.getValue1().getItem(), maxtosend);
				} else if (tile.tile instanceof net.minecraft.inventory.ISidedInventory) {
					IInventory sidedadapter = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) tile.tile, ForgeDirection.UNKNOWN,true);
					extracted = extractFromIInventory(sidedadapter, nextOrder.getValue1().getItem(), maxtosend);
				} else if (tile.tile instanceof IInventory) {
					extracted = extractFromIInventory((IInventory)tile.tile, nextOrder.getValue1().getItem(), maxtosend);
				}
				if (extracted != null && extracted.stackSize > 0) {
					break;
				}
			}
			if(extracted == null || extracted.stackSize == 0) break;
			
			// send the new crafted items to the destination
			ItemIdentifier extractedID = ItemIdentifier.get(extracted);
			while (extracted.stackSize > 0) {
				int numtosend = Math.min(extracted.stackSize, extractedID.getMaxStackSize());
				numtosend = Math.min(numtosend, nextOrder.getValue1().stackSize); 
				if(numtosend == 0)
					break;
				stacksleft -= 1;
				itemsleft -= numtosend;
				ItemStack stackToSend = extracted.splitStack(numtosend);
				if (processingOrder) {
					IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(this.container, stackToSend);
					item.setDestination(nextOrder.getValue2().getRouter().getSimpleID());
					item.setTransportMode(TransportMode.Active);
					item.addRelayPoints(nextOrder.getValue3());
					super.queueRoutedItem(item, tile.orientation);
					_orderManager.sendSuccessfull(stackToSend.stackSize, false);
					if(_orderManager.hasOrders()){
						nextOrder = _orderManager.peekAtTopRequest(); // fetch but not remove.
					} else {
						processingOrder = false;
						if(!_extras.isEmpty())
						nextOrder = _extras.getFirst();
					}
					
				} else {
					removeExtras(numtosend,nextOrder.getValue1().getItem());

					Position p = new Position(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, tile.orientation);
					LogisticsPipes.requestLog.info(stackToSend.stackSize + " extras dropped, " + countExtras() + " remaining");
 					Position entityPos = new Position(p.x + 0.5, p.y + Utils.getPipeFloorOf(stackToSend), p.z + 0.5, p.orientation.getOpposite());
					entityPos.moveForwards(0.5);
					TravelingItem entityItem = new TravelingItem(entityPos.x, entityPos.y, entityPos.z, stackToSend);
					entityItem.setSpeed(Utils.pipeNormalSpeed * Configs.LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER);
					((PipeTransportItems) transport).injectItem(entityItem, entityPos.orientation);
				}
			}
		}
	}
	
	private void removeExtras(int numToSend, ItemIdentifier item) {
		Iterator<Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>>> i = _extras.iterator();
		while(i.hasNext()){
			ItemIdentifierStack e = i.next().getValue1();
			if(e.getItem()== item) {
				if(numToSend >= e.stackSize) {
					numToSend -= e.stackSize;
					i.remove();
					if(numToSend == 0) {
						return;
					}
				} else {
					e.stackSize -= numToSend;
					break;
				}
			}
		}
	}

	private int countExtras(){
		if(_extras == null)
			return 0;
		int count = 0;
		for(Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>> e : _extras){
			count += e.getValue1().stackSize;
		}
		return count;
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
		for(Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>> extra:_extras){
			if(extra.getValue1().getItem()==requestedItem){
				remaining += extra.getValue1().stackSize;
			}
				
		}
		remaining -= donePromisses;
		if (remaining < 1) return;
		LogisticsExtraPromise promise = new LogisticsExtraPromise();
		promise.item = requestedItem;
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
	public CraftingTemplate addCrafting(ItemIdentifier toCraft) {
		
		if (!isEnabled()){
			return null;
		}
		
		List<ItemIdentifierStack> stack = getCraftedItems(); 
		if (stack == null) return null;
		boolean found = false;
		ItemIdentifierStack craftingStack = null;
		for(ItemIdentifierStack craftable:stack) {
			craftingStack = craftable;
			if(craftingStack.getItem().equals(toCraft)) {
				found = true;
				break;
			}
				
		}
		if(found == false)
			return null;

		IRequestItems[] target = new IRequestItems[9];
		for(int i=0;i<9;i++) {
			target[i] = this;
		}

		boolean hasSatellite = isSatelliteConnected();
		if(!hasSatellite) return null;
		if(!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(satelliteId != 0) {
				IRouter r = getSatelliteRouter(-1);
				if(r != null) {
					IRequestItems sat = r.getPipe();
					for(int i=6;i<9;i++) {
						target[i] = sat;
					}
				}
			}
		} else {
			for(int i=0;i<9;i++) {
				if(advancedSatelliteIdArray[i] != 0) {
					IRouter r = getSatelliteRouter(i);
					if(r != null) target[i] = r.getPipe();
				}
			}
		}

		CraftingTemplate template = new CraftingTemplate(craftingStack, this, priority);

		//Check all materials
		for (int i = 0; i < 9; i++){
			ItemIdentifierStack resourceStack = getMaterials(i);
			if (resourceStack == null || resourceStack.stackSize == 0) continue;
			template.addRequirement(resourceStack, target[i]);
		}
		
		int liquidCrafter = this.getUpgradeManager().getFluidCrafter();
		IRequestFluid[] liquidTarget = new IRequestFluid[liquidCrafter];
		
		if(!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(liquidSatelliteId != 0) {
				IRouter r = getFluidSatelliteRouter(-1);
				if(r != null) {
					IRequestFluid sat = (IRequestFluid) r.getPipe();
					for(int i=0;i<liquidCrafter;i++) {
					liquidTarget[i] = sat;
				}
			}
			}
		} else {
			for(int i=0;i<liquidCrafter;i++) {
				if(liquidSatelliteIdArray[i] != 0) {
					IRouter r = getFluidSatelliteRouter(i);
					if(r != null) liquidTarget[i] = (IRequestFluid) r.getPipe();
				}
			}
		}
		
		for (int i = 0; i < liquidCrafter; i++){
			FluidIdentifier liquid = getFluidMaterial(i);
			int amount = getFluidAmount()[i];
			if (liquid == null || amount <= 0 || liquidTarget[i] == null) continue;
			template.addRequirement(liquid, amount, liquidTarget[i]);
		}
		
		if(this.getUpgradeManager().hasByproductExtractor()) {
			template.addByproduct(getByproductItem());
		}
		
		return template;
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (promise instanceof LogisticsExtraPromise) {
			removeExtras(promise.numberOfItems, promise.item);
		}
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, promise.relayPoints);
		MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, getX(), getY(), getZ(), this.getWorld(), 2);
	}

	@Override
	public void registerExtras(LogisticsPromise promise) {		
		ItemIdentifierStack stack = new ItemIdentifierStack(promise.item,promise.numberOfItems);
		_extras.add(new Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>>(stack,null,null));
		LogisticsPipes.requestLog.info(stack.stackSize + " extras registered");
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list,List<IFilter> filters) {}

	@Override
	public List<ItemIdentifierStack> getCraftedItems() {
		//TODO: AECrafting check.
		List<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>(1);
		if(_dummyInventory.getIDStackInSlot(9)!=null)
			list.add(_dummyInventory.getIDStackInSlot(9));
		return list;
	}
	@Override
	public LogisticsModule getLogisticsModule() {
		return new ModuleCrafter(this);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	public boolean hasOrder() {
		return _orderManager.hasOrders();
	}
	
	@Override
	public int getTodo() {
		return _orderManager.totalItemsCountInAllOrders();
	}

	@Override
	public void startWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, getX(), getY(), getZ(), 1).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, getX(), getY(), getZ(), 1).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
//TODO 		MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, getX(), getY(), getZ(), oldList).getPacket(), (Player)player);
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
//TODO 		MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, getX(), getY(), getZ(), all).getPacket(), localModeWatchers);
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
		importFromCraftingTable(null);
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		List<ItemIdentifierStack> result = getCraftedItems();
		if(result == null) return null;
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		for(ItemIdentifierStack craftable:result){
			l1.add(craftable.getItem());
		}
		//for(int i=0; i<9;i++)
		//	l1.add(getMaterials(i));
		return l1;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public List<ForgeDirection> getCraftingSigns() {
		List<ForgeDirection> list = new ArrayList<ForgeDirection>();
		for(int i=0;i<6;i++) {
			if(craftingSigns[i]) {
				list.add(ForgeDirection.VALID_DIRECTIONS[i]);
			}
		}
		return list;
	}

	public boolean setCraftingSign(ForgeDirection dir, boolean b, EntityPlayer player) {
		if(dir.ordinal() < 6) {
			if(craftingSigns[dir.ordinal()] != b) {
				craftingSigns[dir.ordinal()] = b;
				final ModernPacket packetA = PacketHandler.getPacket(PipeUpdate.class).setPayload(getLogisticsNetworkPacket()).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
				final ModernPacket packetB = PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(getDummyInventory()).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
				if(player != null) {
					MainProxy.sendPacketToPlayer(packetA, (Player)player);
					MainProxy.sendPacketToPlayer(packetB, (Player)player);
				}
				MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(getWorld()), packetA);
				MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(getWorld()), packetB);
				this.refreshRender(false);
				return true;
			}
		}
		return false;
	}
	
	// from PipeItemsCraftingLogistics
	protected SimpleInventory _dummyInventory = new SimpleInventory(11, "Requested items", 127);
	protected SimpleInventory _liquidInventory = new SimpleInventory(ItemUpgrade.MAX_LIQUID_CRAFTER, "Fluid items", 1);
	
	@TileNetworkData(staticSize=ItemUpgrade.MAX_LIQUID_CRAFTER)
	protected int[] amount = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	@TileNetworkData(staticSize=ItemUpgrade.MAX_LIQUID_CRAFTER)
	public int liquidSatelliteIdArray[] = new int[ItemUpgrade.MAX_LIQUID_CRAFTER];
	@TileNetworkData
	public int liquidSatelliteId = 0;

	@TileNetworkData(staticSize=6)
	public boolean[] craftingSigns = new boolean[6];
	
	protected final DelayQueue< DelayedGeneric<ItemIdentifierStack>> _lostItems = new DelayQueue< DelayedGeneric<ItemIdentifierStack>>();
	
	@TileNetworkData
	public int satelliteId = 0;

	@TileNetworkData(staticSize=9)
	public int advancedSatelliteIdArray[] = new int[9];

	@TileNetworkData
	public int priority = 0;

	/* ** SATELLITE CODE ** */
	protected int getNextConnectSatelliteId(boolean prev, int x) {
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
			CoreRoutedPipe satPipe = satellite;
			if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null || satPipe.isFluidPipe()) continue;
			IRouter satRouter = satPipe.getRouter();
			ExitRoute route = getRouter().getDistanceTo(satRouter);
			if(route != null) {
				if(x == -1) {
					if (!prev && satellite.satelliteId > satelliteId && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < satelliteId && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				} else {
					if (!prev && satellite.satelliteId > advancedSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < advancedSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if(x == -1) {
				return satelliteId;
			} else {
				return advancedSatelliteIdArray[x];
			}
		}
		return closestIdFound;
	}
	
	protected int getNextConnectFluidSatelliteId(boolean prev, int x) {
		int closestIdFound = prev ? 0 : Integer.MAX_VALUE;
		for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
			CoreRoutedPipe satPipe = satellite;
			if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null || !satPipe.isFluidPipe()) continue;
			IRouter satRouter = satPipe.getRouter();
			ExitRoute route = getRouter().getDistanceTo(satRouter);
			if(route != null) {
				if(x == -1) {
					if (!prev && satellite.satelliteId > liquidSatelliteId && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < liquidSatelliteId && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				} else {
					if (!prev && satellite.satelliteId > liquidSatelliteIdArray[x] && satellite.satelliteId < closestIdFound) {
						closestIdFound = satellite.satelliteId;
					} else if (prev && satellite.satelliteId < liquidSatelliteIdArray[x] && satellite.satelliteId > closestIdFound) {
						closestIdFound = satellite.satelliteId;
					}
				}
			}
		}
		if (closestIdFound == Integer.MAX_VALUE) {
			if(x == -1) {
				return liquidSatelliteId;
			} else {
				return liquidSatelliteIdArray[x];
			}
		}
		return closestIdFound;
	}

	public void setNextSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeNextSatellite.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToServer(packet);
		} else {
			satelliteId = getNextConnectSatelliteId(false, -1);
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteId.class).setPipeId(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToPlayer(packet, (Player)player);
		}

	}
	
	// This is called by the packet PacketCraftingPipeSatelliteId
	public void setSatelliteId(int satelliteId, int x) {
		if(x == -1) {
			this.satelliteId = satelliteId;
		} else {
			advancedSatelliteIdArray[x] = satelliteId;
		}
	}

	public void setPrevSatellite(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipePrevSatellite.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToServer(packet);
		} else {
			satelliteId = getNextConnectSatelliteId(true, -1);
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteId.class).setPipeId(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToPlayer(packet, (Player)player);
		}
	}

	public boolean isSatelliteConnected() {
		final List<ExitRoute> routes = getRouter().getIRoutersByCost();
		if(!((CoreRoutedPipe)this.container.pipe).getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(satelliteId == 0) return true;
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					IRouter satRouter = satPipe.getRouter();
					for (ExitRoute route:routes) {
						if (route.destination == satRouter) {
							return true;
						}
					}
				}
			}
		} else {
			boolean foundAll = true;
			for(int i=0;i<9;i++) {
				boolean foundOne = false;
				if(advancedSatelliteIdArray[i] == 0) {
					continue;
				}
				for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
					if (satellite.satelliteId == advancedSatelliteIdArray[i]) {
						CoreRoutedPipe satPipe = satellite;
						if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
							continue;
						IRouter satRouter = satPipe.getRouter();
						for (ExitRoute route:routes) {
							if (route.destination == satRouter) {
								foundOne = true;
								break;
							}
						}
					}
				}
				foundAll &= foundOne;
			}
			return foundAll;
		}
		//TODO check for FluidCrafter
		return false;
	}

	public IRouter getSatelliteRouter(int x) {
		if(x == -1) {
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == satelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		} else {
			for (final PipeItemsSatelliteLogistics satellite : PipeItemsSatelliteLogistics.AllSatellites) {
				if (satellite.satelliteId == advancedSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		}
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		_dummyInventory.readFromNBT(nbttagcompound, "");
		_liquidInventory.readFromNBT(nbttagcompound, "FluidInv");
		satelliteId = nbttagcompound.getInteger("satelliteid");
		
		priority = nbttagcompound.getInteger("priority");
		for(int i=0;i<9;i++) {
			advancedSatelliteIdArray[i] = nbttagcompound.getInteger("advancedSatelliteId" + i);
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
		liquidSatelliteId = nbttagcompound.getInteger("liquidSatelliteId");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		_dummyInventory.writeToNBT(nbttagcompound, "");
		_liquidInventory.writeToNBT(nbttagcompound, "FluidInv");
		nbttagcompound.setInteger("satelliteid", satelliteId);
		
		nbttagcompound.setInteger("priority", priority);
		for(int i=0;i<9;i++) {
			nbttagcompound.setInteger("advancedSatelliteId" + i, advancedSatelliteIdArray[i]);
		}
		for(int i=0;i<6;i++) {
			nbttagcompound.setBoolean("craftingSigns" + i, craftingSigns[i]);
		}
		for(int i=0;i<ItemUpgrade.MAX_LIQUID_CRAFTER;i++) {
			nbttagcompound.setInteger("liquidSatelliteIdArray" + i, liquidSatelliteIdArray[i]);
		}
		nbttagcompound.setIntArray("FluidAmount", amount);
		nbttagcompound.setInteger("liquidSatelliteId", liquidSatelliteId);
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if (MainProxy.isServer(entityplayer.worldObj)) {
//TODO 		MainProxy.sendPacketToPlayer(new PacketGuiArgument(NetworkConstants.GUI_ARGUMENT_PACKET, GuiIDs.GUI_CRAFTINGPIPE_ID, ).getPacket(),  (Player) entityplayer);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(GuiArgument.class).setGuiID(GuiIDs.GUI_CRAFTINGPIPE_ID).setArgs(new Object[]{((CoreRoutedPipe)this.container.pipe).getUpgradeManager().isAdvancedSatelliteCrafter(), ((CoreRoutedPipe)this.container.pipe).getUpgradeManager().getFluidCrafter(), amount, ((CoreRoutedPipe)this.container.pipe).getUpgradeManager().hasByproductExtractor()}),  (Player) entityplayer);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_CRAFTINGPIPE_ID, getWorld(), getX(), getY(), getZ());
		}
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
			if( hasOrder()) { 
				SinkReply reply = LogisticsManagerV2.canSink(getRouter(), null, true, stack.getItem(), null, true,true);
				if(reply == null || reply.maxNumberOfItems <1) {
					lostItem = _lostItems.poll();
					//iterator.remove(); // if we have no space for this and nothing to do, don't bother re-requesting the item.
					continue;
				}
			}
			int received = RequestTree.requestPartial(stack, (CoreRoutedPipe) container.pipe);
			if(received < stack.stackSize) {
				stack.stackSize -= received;
				_lostItems.add(new DelayedGeneric<ItemIdentifierStack>(stack,5000));
			}
			lostItem = _lostItems.poll();
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item) {
	}

	@Override
	public void itemLost(ItemIdentifierStack item) {
		_lostItems.add(new DelayedGeneric<ItemIdentifierStack>(item,5000));
	}

	public void openAttachedGui(EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
			if(player instanceof EntityPlayerMP) {
				((EntityPlayerMP)player).closeScreen();
			} else if(player instanceof EntityPlayerSP) {
				((EntityPlayerSP)player).closeScreen();
			}
//TODO		MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_OPEN_CONNECTED_GUI, getX(), getY(), getZ()).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeOpenConnectedGuiPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
			return;
		}

		//hack to avoid wrenching blocks
		int savedEquipped = player.inventory.currentItem;
		boolean foundSlot = false;
		//try to find a empty slot
		for(int i = 0; i < 9; i++) {
			if(player.inventory.getStackInSlot(i) == null) {
				foundSlot = true;
				player.inventory.currentItem = i;
				break;
			}
		}
		//okay, anything that's a block?
		if(!foundSlot) {
			for(int i = 0; i < 9; i++) {
				ItemStack is = player.inventory.getStackInSlot(i);
				if(is.getItem() instanceof ItemBlock) {
					foundSlot = true;
					player.inventory.currentItem = i;
					break;
				}
			}
		}
		//give up and select whatever is right of the current slot
		if(!foundSlot) {
			player.inventory.currentItem = (player.inventory.currentItem + 1) % 9;
		}

		final WorldUtil worldUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
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
				Block block = getWorld().getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord) < Block.blocksList.length ? Block.blocksList[getWorld().getBlockId(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord)] : null;
				if(block != null) {
					if(block.onBlockActivated(getWorld(), tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, player, 0, 0, 0, 0)){
						break;
					}
				}
			}
		}

		player.inventory.currentItem = savedEquipped;
	}

	public void importFromCraftingTable(EntityPlayer player) {
		final WorldUtil worldUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (final AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)) {
			for (ICraftingRecipeProvider provider : SimpleServiceLocator.craftingRecipeProviders) {
				if (provider.importRecipe(tile.tile, _dummyInventory))
					break;
			}
		}
		
		if(player == null) return;
		
		if (MainProxy.isClient(player.worldObj)) {
			// Send packet asking for import
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImport.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToServer(packet);
		} else{
			// Send inventory as packet
			final CoordinatesPacket packet = PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(_dummyInventory).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToPlayer(packet, (Player)player);
			MainProxy.sendPacketToAllWatchingChunk(this.getX(), this.getZ(), MainProxy.getDimensionForWorld(getWorld()), packet);
		}
	}

	public void handleStackMove(int number) {
		if(MainProxy.isClient(this.getWorld())) {
//TODO 		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_STACK_MOVE,getX(),getY(),getZ(),number).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeStackMovePacket.class).setInteger(number).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		}
		ItemStack stack = _dummyInventory.getStackInSlot(number);
		if(stack == null ) return;
		for(int i = 6;i < 9;i++) {
			ItemStack stackb = _dummyInventory.getStackInSlot(i);
			if(stackb == null) {
				_dummyInventory.setInventorySlotContents(i, stack);
				_dummyInventory.clearInventorySlotContents(number);
				break;
			}
		}
	}
	
	public void priorityUp(EntityPlayer player) {
		priority++;
		if(MainProxy.isClient(player.worldObj)) {
//TODO 		MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_PRIORITY_UP, getX(), getY(), getZ()).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePriorityUpPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else if(player != null && MainProxy.isServer(player.worldObj)) {
//TODO 		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_PRIORITY, getX(), getY(), getZ(), priority).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPriority.class).setInteger(priority).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}
	
	public void priorityDown(EntityPlayer player) {
		priority--;
		if(MainProxy.isClient(player.worldObj)) {
//TODO 		MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.CRAFTING_PIPE_PRIORITY_DOWN, getX(), getY(), getZ()).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePriorityDownPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else if(player != null && MainProxy.isServer(player.worldObj)) {
//TODO 		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_PRIORITY, getX(), getY(), getZ(), priority).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPriority.class).setInteger(priority).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}
	
	public void setPriority(int amount) {
		priority = amount;
	}

	public ItemIdentifierStack getByproductItem() {
		return _dummyInventory.getIDStackInSlot(10);
	}
	
	public ItemIdentifierStack getMaterials(int slotnr) {
		return _dummyInventory.getIDStackInSlot(slotnr);
	}

	public FluidIdentifier getFluidMaterial(int slotnr) {
		ItemIdentifierStack stack = _liquidInventory.getIDStackInSlot(slotnr);
		if(stack == null) return null;
		return stack.getItem().getFluidIdentifier();
	}

	/**
	 * Simply get the dummy inventory
	 * 
	 * @return the dummy inventory
	 */
	public SimpleInventory getDummyInventory() {
		return _dummyInventory;
	}

	public SimpleInventory getFluidInventory() {
		return _liquidInventory;
	}
	
	public void setDummyInventorySlot(int slot, ItemStack itemstack) {
		_dummyInventory.setInventorySlotContents(slot, itemstack);
	}

	public void setNextSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
//TODO 		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE_ADVANCED, getX(), getY(), getZ(), i).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeNextAdvancedSatellitePacket.class).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(false, i);
//TODO 		MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID_ADVANCED, getX(), getY(), getZ(), i, advancedSatelliteIdArray[i]).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(advancedSatelliteIdArray[i]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}

	public void setPrevSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
//TODO 		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE_ADVANCED, getX(), getY(), getZ(), i).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipePrevAdvancedSatellitePacket.class).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			advancedSatelliteIdArray[i] = getNextConnectSatelliteId(true, i);
//TODO 		MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.CRAFTING_PIPE_SATELLITE_ID_ADVANCED, getX(), getY(), getZ(), i, advancedSatelliteIdArray[i]).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(advancedSatelliteIdArray[i]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}

	public void changeFluidAmount(int change, int slot, EntityPlayer player) {
		if (MainProxy.isClient(player.worldObj)) {
//TODO 		MainProxy.sendPacketToServer(new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_AMOUNT, getX(), getY(), getZ(), slot, change).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(change).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			amount[slot] += change;
			if(amount[slot] <= 0) {
				amount[slot] = 0;
			}
//TODO 		MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_AMOUNT, getX(), getY(), getZ(), slot, amount[slot]).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAmount.class).setInteger2(slot).setInteger(amount[slot]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}

	public void setPrevFluidSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
//TODO 		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_PREV_SATELLITE_ADVANCED, getX(), getY(), getZ(), i).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingPipeAdvancedSatellitePrevPacket.class).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			if(i == -1) {
				liquidSatelliteId = getNextConnectFluidSatelliteId(true, i);
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED, getX(), getY(), getZ(), i, liquidSatelliteId).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			} else {
				liquidSatelliteIdArray[i] = getNextConnectFluidSatelliteId(true, i);
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED, getX(), getY(), getZ(), i, liquidSatelliteIdArray[i]).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteIdArray[i]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			}
		}
	}

	public void setNextFluidSatellite(EntityPlayer player, int i) {
		if (MainProxy.isClient(player.worldObj)) {
//TODO 		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_NEXT_SATELLITE_ADVANCED, getX(), getY(), getZ(), i).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidCraftingPipeAdvancedSatelliteNextPacket.class).setInteger(i).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		} else {
			if(i == -1) {
				liquidSatelliteId = getNextConnectFluidSatelliteId(false, i);
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED, getX(), getY(), getZ(), i, liquidSatelliteId).getPacket(), (Player)player);		
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			} else {
				liquidSatelliteIdArray[i] = getNextConnectFluidSatelliteId(false, i);
//TODO 			MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.LIQUID_CRAFTING_PIPE_SATELLITE_ID_ADVANCED, getX(), getY(), getZ(), i, liquidSatelliteIdArray[i]).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidCraftingAdvancedSatelliteId.class).setInteger2(i).setInteger(liquidSatelliteIdArray[i]).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			}
		}
	}

	public void setFluidAmount(int[] amount) {
		if(MainProxy.isClient(getWorld())) {
			this.amount = amount;
		}
	}

	public void defineFluidAmount(int integer, int slot) {
		if(MainProxy.isClient(getWorld())) {
			amount[slot] = integer;
		}
	}
	
	public int[] getFluidAmount() {
		return amount;
	}

	public void setFluidSatelliteId(int integer, int slot) {
		if(slot == -1) {
			liquidSatelliteId = integer;
		} else {
			liquidSatelliteIdArray[slot] = integer;
		}	
	}

	public IRouter getFluidSatelliteRouter(int x) {
		if(x == -1) {
			for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
				if (satellite.satelliteId == liquidSatelliteId) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		} else {
			for (final PipeFluidSatellite satellite : PipeFluidSatellite.AllSatellites) {
				if (satellite.satelliteId == liquidSatelliteIdArray[x]) {
					CoreRoutedPipe satPipe = satellite;
					if(satPipe == null || satPipe.stillNeedReplace() || satPipe.getRouter() == null)
						continue;
					return satPipe.getRouter();
				}
			}
		}
		return null;
	}

}
