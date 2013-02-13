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
import logisticspipes.blocks.LogisticsSignTileEntity;
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
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.network.packets.PacketPipeUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.RoutedPipe;
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
import logisticspipes.security.PermissionException;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.utils.Utils;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;

@CCType(name = "LogisticsPipes:Crafting")
public class PipeItemsCraftingLogistics extends RoutedPipe implements ICraftItems, IHeadUpDisplayRendererProvider, IChangeListener, IOrderManagerContentReceiver {

	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager(this);

	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	private final HUDCrafting HUD = new HUDCrafting(this);
	
	protected int _extras;
	private boolean init = false;
	private boolean doContentUpdate = true;
	
	public PipeItemsCraftingLogistics(int itemID) {
		super(new BaseLogicCrafting(), itemID);
	}
	
	public PipeItemsCraftingLogistics(PipeTransportLogistics transport, int itemID) {
		super(transport, new BaseLogicCrafting(), itemID);
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
		WorldUtil worldUtil = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
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
					MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.REQUEST_CRAFTING_PIPE_UPDATE, xCoord, yCoord, zCoord).getPacket());
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
		
		if ((!_orderManager.hasOrders() && _extras < 1) || worldObj.getWorldTime() % 6 != 0) return;
		
		List<AdjacentTile> crafters = locateCrafters();
		if (crafters.size() < 1 ) {
			if (_orderManager.hasOrders()) {
				_orderManager.sendFailed();
			} else {
				_extras = 0;
			}
			return;
		}
		
		ItemIdentifier wanteditem = providedItem();
		if(wanteditem == null) return;

		MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, xCoord, yCoord, zCoord, this.worldObj, 2);
		
		int itemsleft = itemsToExtract();
		int stacksleft = stacksToExtract();
		while (itemsleft > 0 && stacksleft > 0 && (_orderManager.hasOrders() || _extras > 0)) {
			int maxtosend = Math.min(itemsleft, wanteditem.getMaxStackSize() * stacksleft);
			if(_orderManager.hasOrders()){
				maxtosend = Math.min(maxtosend, _orderManager.getNextRequest().getValue1().stackSize);
			} else {
				maxtosend = Math.min(maxtosend, _extras);
			}
			
			ItemStack extracted = null;
			AdjacentTile tile = null;
			for (Iterator<AdjacentTile> it = crafters.iterator(); it.hasNext();) {
				tile = it.next();
				if (tile.tile instanceof ISpecialInventory) {
					extracted = extractFromISpecialInventory((ISpecialInventory) tile.tile, wanteditem, maxtosend);
				} else if (tile.tile instanceof ISidedInventory) {
					IInventory sidedadapter = new SidedInventoryAdapter((ISidedInventory) tile.tile, ForgeDirection.UNKNOWN);
					extracted = extractFromIInventory(sidedadapter, wanteditem, maxtosend);
				} else if (tile.tile instanceof IInventory) {
					extracted = extractFromIInventory((IInventory)tile.tile, wanteditem, maxtosend);
				}
				if (extracted != null) {
					break;
				}
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
					
					IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stackToSend, worldObj);
					item.setDestination(order.getValue2().getRouter().getSimpleID());
					item.setTransportMode(TransportMode.Active);
					item.addRelayPoints(order.getValue3());
					super.queueRoutedItem(item, tile.orientation);
					_orderManager.sendSuccessfull(stackToSend.stackSize);
				} else {
					ItemStack stackToSend = extracted.splitStack(numtosend);
					_extras = Math.max(_extras - numtosend, 0);
					itemsleft -= numtosend;
					stacksleft -= 1;
					
					Position p = new Position(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, tile.orientation);
					LogisticsPipes.requestLog.info(stackToSend.stackSize + " extras dropped, " + _extras + " remaining");
 					Position entityPos = new Position(p.x + 0.5, p.y + Utils.getPipeFloorOf(stackToSend), p.z + 0.5, p.orientation.getOpposite());
					entityPos.moveForwards(0.5);
					EntityPassiveItem entityItem = new EntityPassiveItem(worldObj, entityPos.x, entityPos.y, entityPos.z, stackToSend);
					entityItem.setSpeed(Utils.pipeNormalSpeed * Configs.LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER);
					((PipeTransportItems) transport).entityEntering(entityItem, entityPos.orientation);
				}
			}
		}
	}
	
	private ItemIdentifier providedItem(){
		BaseLogicCrafting craftingLogic = (BaseLogicCrafting) this.logic;
		ItemStack stack = craftingLogic.getCraftedItem(); 
		if (stack == null) return null;
		return ItemIdentifier.get(stack);
	}
	

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CRAFTER_TEXTURE;
	}

	@Override
	public void canProvide(RequestTreeNode tree, Map<ItemIdentifier, Integer> donePromisses, List<IFilter> filters) {
		
		if (!isEnabled()){
			return;
		}
		
		if (_extras < 1) return;
		ItemIdentifier providedItem = providedItem();
		if (tree.getStack().getItem() != providedItem) return;

		
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.isFilteredItem(tree.getStack().getItem().getUndamaged()) || filter.blockProvider()) return;
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
		
		if (!isEnabled()){
			return null;
		}
		
		BaseLogicCrafting craftingLogic = (BaseLogicCrafting) this.logic;
		ItemStack stack = craftingLogic.getCraftedItem(); 
		if ( stack == null) return null;

		boolean hasSatellite = craftingLogic.isSatelliteConnected();
		if(craftingLogic.satelliteId != 0 && !hasSatellite) return null;

		CraftingTemplate template = new CraftingTemplate(ItemIdentifierStack.GetFromStack(stack), this, craftingLogic.priority);

		//Check all materials
		for (int i = 0; i < 9; i++){
			ItemStack resourceStack = craftingLogic.getMaterials(i);
			if (resourceStack == null || resourceStack.stackSize == 0) continue;
			if (i < 6 || !hasSatellite){
				template.addRequirement(ItemIdentifierStack.GetFromStack(resourceStack), this);
			}
			else{
				template.addRequirement(ItemIdentifierStack.GetFromStack(resourceStack), (IRequestItems)craftingLogic.getSatelliteRouter().getPipe());
			}
				
		}
		return template;
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (promise instanceof LogisticsExtraPromise) {
			_extras -= promise.numberOfItems;
		}
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination, promise.relayPoints);
		MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, xCoord, yCoord, zCoord, this.worldObj, 2);
	}

	@Override
	public void registerExtras(int count) {
		_extras += count;
		LogisticsPipes.requestLog.info(count + " extras registered");
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list,List<IFilter> filters) {}

	@Override
	public ItemIdentifier getCraftedItem() {
		if (!isEnabled()){
			return null;
		}
		return providedItem();
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}
	
	public boolean isAttachedSign(TileEntity entity) {
		return entity.xCoord == ((BaseLogicCrafting)logic).signEntityX && entity.yCoord == ((BaseLogicCrafting)logic).signEntityY && entity.zCoord == ((BaseLogicCrafting)logic).signEntityZ;
	}
	
	public void addSign(LogisticsSignTileEntity entity, EntityPlayer player) {
		if(((BaseLogicCrafting)logic).signEntityX == 0 && ((BaseLogicCrafting)logic).signEntityY == 0 && ((BaseLogicCrafting)logic).signEntityZ == 0) {
			((BaseLogicCrafting)logic).signEntityX = entity.xCoord;
			((BaseLogicCrafting)logic).signEntityY = entity.yCoord;
			((BaseLogicCrafting)logic).signEntityZ = entity.zCoord;
			MainProxy.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,xCoord,yCoord,zCoord,getLogisticsNetworkPacket()).getPacket(), (Player)player);
			final PacketInventoryChange newpacket = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, xCoord, yCoord, zCoord, ((BaseLogicCrafting)logic).getDummyInventory());
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
		MainProxy.sendToPlayerList(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,xCoord,yCoord,zCoord,this.getLogisticsNetworkPacket()).getPacket(), localModeWatchers);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	public boolean hasOrder() {
		return _orderManager.hasOrders();
	}
	
	@Override
	public int getX() {
		return xCoord;
	}

	@Override
	public int getY() {
		return yCoord;
	}

	@Override
	public int getZ() {
		return zCoord;
	}

	@Override
	public void startWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, xCoord, yCoord, zCoord, 1).getPacket());
	}

	@Override
	public void stopWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, xCoord, yCoord, zCoord, 1).getPacket());
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, oldList).getPacket(), (Player)player);
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
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList();
		if(!oldList.equals(all)) {
			oldList.clear();
			oldList.addAll(all);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, all).getPacket(), localModeWatchers);
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
	
	/* ComputerCraftCommands */
	@CCCommand(description="Imports the crafting recipe from the connected machine/crafter")
	@CCQueued(prefunction="testImportAccess")
	public void reimport() throws Exception {
		checkCCAccess();
		((BaseLogicCrafting)logic).importFromCraftingTable(null);
	}
	
	public void testImportAccess() throws PermissionException {
		checkCCAccess();
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		ItemStack result = ((BaseLogicCrafting) this.logic).getCraftedItem();
		if(result == null) return null;
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		l1.add(ItemIdentifier.get(result));
		//for(int i=0; i<9;i++)
		//	l1.add(((BaseLogicCrafting) this.logic).getMaterials(i));
		return l1;
	}

}
