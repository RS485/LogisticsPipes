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

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.gui.hud.HUDCrafting;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.network.packets.PacketPipeUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsOrderManager;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SidedInventoryForgeAdapter;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.utils.Utils;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;

@CCType(name = "LogisticsPipes:Crafting")
public class PipeItemsCraftingLogistics extends CoreRoutedPipe implements ICraftItems, IHeadUpDisplayRendererProvider, IChangeListener, IOrderManagerContentReceiver, IHavePriority {

	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager(this);

	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	private final HUDCrafting HUD = new HUDCrafting(this);
	
	public final LinkedList<Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>>> _extras = new LinkedList<Pair3<ItemIdentifierStack, IRequestItems, List<IRelayItem>>>();
	private boolean init = false;
	private boolean doContentUpdate = true;
	
	public boolean waitingForCraft = false;
	
	public PipeItemsCraftingLogistics(int itemID) {
		super(new BaseLogicCrafting(), itemID);
		((BaseLogicCrafting)logic).setParentPipe(this);
	}
	
	public PipeItemsCraftingLogistics(PipeTransportLogistics transport, int itemID) {
		super(transport, new BaseLogicCrafting(), itemID);
		((BaseLogicCrafting)logic).setParentPipe(this);
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
		WorldUtil worldUtil = new WorldUtil(this.worldObj, this.getX(), this.getY(), this.getZ());
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
	public void onBlockRemoval() {
		super.onBlockRemoval();
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
	
	public void enableUpdateRequest() {
		init = false;
	}
	
	@Override
	public void ignoreDisableUpdateEntity() {
		if(!init) {
			if(MainProxy.isClient(worldObj)) {
				if(FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().thePlayer.sendQueue != null){
					MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.REQUEST_CRAFTING_PIPE_UPDATE, getX(), getY(), getZ()).getPacket());
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
		
		if (worldObj.getWorldTime() % 6 != 0) return;

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
		
		List<ItemIdentifier> wanteditem = providedItem();
		if(wanteditem == null) return;

		MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, getX(), getY(), getZ(), this.worldObj, 2);
		
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
				if (tile.tile instanceof ISpecialInventory) {
					extracted = extractFromISpecialInventory((ISpecialInventory) tile.tile, nextOrder.getValue1().getItem(), maxtosend);
				} else if (tile.tile instanceof net.minecraft.inventory.ISidedInventory) {
					IInventory sidedadapter = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) tile.tile, ForgeDirection.UNKNOWN);
					extracted = extractFromIInventory(sidedadapter, nextOrder.getValue1().getItem(), maxtosend);
				} else if (tile.tile instanceof net.minecraftforge.common.ISidedInventory) {
					IInventory sidedadapter = new SidedInventoryForgeAdapter((net.minecraftforge.common.ISidedInventory) tile.tile, ForgeDirection.UNKNOWN);
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
					IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stackToSend, worldObj);
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
					EntityPassiveItem entityItem = new EntityPassiveItem(worldObj, entityPos.x, entityPos.y, entityPos.z, stackToSend);
					entityItem.setSpeed(Utils.pipeNormalSpeed * Configs.LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER);
					((PipeTransportItems) transport).entityEntering(entityItem, entityPos.orientation);
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
	
	private List<ItemIdentifier> providedItem(){
		BaseLogicCrafting craftingLogic = (BaseLogicCrafting) this.logic;
		List<ItemStack> stacks = craftingLogic.getCraftedItems(); 
		if (stacks == null) return null;
		List<ItemIdentifier> l = new ArrayList<ItemIdentifier>(stacks.size());
		for(ItemStack stack:stacks){
			if(stacks != null)
				l.add(ItemIdentifier.get(stack));
		}
		return l;
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
		List<ItemIdentifier> providedItem = providedItem();
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
		
		BaseLogicCrafting craftingLogic = (BaseLogicCrafting) this.logic;
		List<ItemStack> stack = craftingLogic.getCraftedItems(); 
		if (stack == null) return null;
		boolean found = false;
		ItemIdentifierStack craftingStack = null;
		for(ItemStack craftable:stack) {
			craftingStack = ItemIdentifierStack.GetFromStack(craftable);
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

		boolean hasSatellite = craftingLogic.isSatelliteConnected();
		if(!hasSatellite) return null;
		if(!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(craftingLogic.satelliteId != 0) {
				IRequestItems sat = craftingLogic.getSatelliteRouter(-1).getPipe();
				for(int i=6;i<9;i++) {
					target[i] = sat;
				}
			}
		} else {
			for(int i=0;i<9;i++) {
				if(craftingLogic.advancedSatelliteIdArray[i] != 0) {
					target[i] = craftingLogic.getSatelliteRouter(i).getPipe();
				}
			}
		}

		CraftingTemplate template = new CraftingTemplate(craftingStack, this, craftingLogic.priority);

		//Check all materials
		for (int i = 0; i < 9; i++){
			ItemStack resourceStack = craftingLogic.getMaterials(i);
			if (resourceStack == null || resourceStack.stackSize == 0) continue;
			template.addRequirement(ItemIdentifierStack.GetFromStack(resourceStack), target[i]);
		}
		
		int liquidCrafter = this.getUpgradeManager().getLiquidCrafter();
		IRequestLiquid[] liquidTarget = new IRequestLiquid[liquidCrafter];
		
		if(!getUpgradeManager().isAdvancedSatelliteCrafter()) {
			if(craftingLogic.liquidSatelliteId != 0) {
				IRequestLiquid sat = (IRequestLiquid) craftingLogic.getLiquidSatelliteRouter(-1).getPipe();
				for(int i=0;i<liquidCrafter;i++) {
					liquidTarget[i] = sat;
				}
			}
		} else {
			for(int i=0;i<liquidCrafter;i++) {
				if(craftingLogic.liquidSatelliteIdArray[i] != 0) {
					liquidTarget[i] = (IRequestLiquid) craftingLogic.getLiquidSatelliteRouter(i).getPipe();
				}
			}
		}
		
		for (int i = 0; i < liquidCrafter; i++){
			LiquidIdentifier liquid = craftingLogic.getLiquidMaterial(i);
			int amount = craftingLogic.getLiquidAmount()[i];
			if (liquid == null || amount <= 0 || liquidTarget[i] == null) continue;
			template.addRequirement(liquid, amount, liquidTarget[i]);
		}
		
		if(this.getUpgradeManager().hasByproductExtractor()) {
			template.addByproduct(ItemIdentifierStack.GetFromStack(((BaseLogicCrafting)logic).getByproductItem()));
		}
		
		return template;
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (promise instanceof LogisticsExtraPromise) {
			removeExtras(promise.numberOfItems, promise.item);
		}
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, promise.relayPoints);
		MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, getX(), getY(), getZ(), this.worldObj, 2);
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
	public List<ItemIdentifier> getCraftedItems() {
		if (!isEnabled()){
			return null;
		}
		return providedItem();
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return new ModuleCrafter(this);
	}
	
	public boolean isAttachedSign(TileEntity entity) {
		return entity.xCoord == ((BaseLogicCrafting)logic).signEntityX && entity.yCoord == ((BaseLogicCrafting)logic).signEntityY && entity.zCoord == ((BaseLogicCrafting)logic).signEntityZ;
	}

	/*
	public void addSign(LogisticsSignTileEntity entity, EntityPlayer player) {
		if(((BaseLogicCrafting)logic).signEntityX == 0 && ((BaseLogicCrafting)logic).signEntityY == 0 && ((BaseLogicCrafting)logic).signEntityZ == 0) {
			((BaseLogicCrafting)logic).signEntityX = entity.getX();
			((BaseLogicCrafting)logic).signEntityY = entity.getY();
			((BaseLogicCrafting)logic).signEntityZ = entity.getZ();
			MainProxy.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,getX(),getY(),getZ(),getLogisticsNetworkPacket()).getPacket(), (Player)player);
			final PacketInventoryChange newpacket = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, getX(), getY(), getZ(), ((BaseLogicCrafting)logic).getDummyInventory());
			MainProxy.sendPacketToPlayer(newpacket.getPacket(), (Player)player);
		}
	}
	
	public boolean canRegisterSign() {
		return ((BaseLogicCrafting)logic).signEntityX == 0 && ((BaseLogicCrafting)logic).signEntityY == 0 && ((BaseLogicCrafting)logic).signEntityZ == 0;
	}
	
	public void removeRegisteredSign() {
		((BaseLogicCrafting)logic).signEntityX = 0;
		((BaseLogicCrafting)logic).signEntityY = 0;
		((BaseLogicCrafting)logic).signEntityZ = 0;
		MainProxy.sendToPlayerList(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,getX(),getY(),getZ(),this.getLogisticsNetworkPacket()).getPacket(), localModeWatchers);
	}
	*/

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
	public void startWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, getX(), getY(), getZ(), 1).getPacket());
	}

	@Override
	public void stopWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, getX(), getY(), getZ(), 1).getPacket());
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, getX(), getY(), getZ(), oldList).getPacket(), (Player)player);
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
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList(this.worldObj);
		if(!oldList.equals(all)) {
			oldList.clear();
			oldList.addAll(all);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, getX(), getY(), getZ(), all).getPacket(), localModeWatchers);
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
		((BaseLogicCrafting)logic).importFromCraftingTable(null);
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		List<ItemStack> result = ((BaseLogicCrafting) this.logic).getCraftedItems();
		if(result == null) return null;
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		for(ItemStack craftable:result){
			l1.add(ItemIdentifier.get(craftable));
		}
		//for(int i=0; i<9;i++)
		//	l1.add(((BaseLogicCrafting) this.logic).getMaterials(i));
		return l1;
	}

	@Override
	public int getPriority() {
		return ((BaseLogicCrafting)this.logic).priority;
	}

	public List<ForgeDirection> getCraftingSigns() {
		List<ForgeDirection> list = new ArrayList<ForgeDirection>();
		for(int i=0;i<6;i++) {
			if(((BaseLogicCrafting)logic).craftingSigns[i]) {
				list.add(ForgeDirection.VALID_DIRECTIONS[i]);
			}
		}
		return list;
	}

	public boolean setCraftingSign(ForgeDirection dir, boolean b, EntityPlayer player) {
		if(dir.ordinal() < 6) {
			if(((BaseLogicCrafting)logic).craftingSigns[dir.ordinal()] != b) {
				((BaseLogicCrafting)logic).craftingSigns[dir.ordinal()] = b;
				final Packet packetA = new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,getX(),getY(),getZ(),getLogisticsNetworkPacket()).getPacket();
				final Packet packetB = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, getX(), getY(), getZ(), ((BaseLogicCrafting)logic).getDummyInventory()).getPacket();
				if(player != null) {
					MainProxy.sendPacketToPlayer(packetA, (Player)player);
					MainProxy.sendPacketToPlayer(packetB, (Player)player);
				}
				MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(worldObj), packetA);
				MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(worldObj), packetB);
				this.refreshRender(false);
				return true;
			}
		}
		return false;
	}
}
