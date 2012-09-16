/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.config.Textures;
import logisticspipes.gui.hud.HUDCrafting;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.main.CraftingTemplate;
import logisticspipes.main.LogisticsOrderManager;
import logisticspipes.main.LogisticsPromise;
import logisticspipes.main.LogisticsRequest;
import logisticspipes.main.LogisticsTransaction;
import logisticspipes.main.RoutedPipe;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.network.packets.PacketPipeUpdate;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import logisticspipes.utils.WorldUtil;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.utils.Utils;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PipeItemsCraftingLogistics extends RoutedPipe implements ICraftItems, IHeadUpDisplayRendererProvider, IChangeListener, IOrderManagerContentReceiver {

	protected LogisticsOrderManager _orderManager = new LogisticsOrderManager(this);

	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	private final HUDCrafting HUD = new HUDCrafting(this);
	
	protected int _extras;
	private boolean init = false;
	
	public PipeItemsCraftingLogistics(int itemID) {
		super(new BaseLogicCrafting(), itemID);
	}
	
	protected LinkedList<AdjacentTile> locateCrafters()	{
		WorldUtil worldUtil = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> crafters = new LinkedList<AdjacentTile>();
		for (AdjacentTile tile : worldUtil.getAdjacentTileEntities()){
			if (tile.tile instanceof TileGenericPipe) continue;
			if (!(tile.tile instanceof IInventory)) continue;
			crafters.add(tile);
		}
		return crafters;
	}
	
	protected ItemStack extractFromISpecialInventory(ISpecialInventory inv){
		ItemStack[] stack = inv.extractItem(true, Orientations.Unknown, 1);
		if(stack.length < 1) return null;
		return stack[0];
	}
	
	protected ItemStack extractFromIInventory(IInventory inv){
		
		InventoryUtil invUtil = new InventoryUtil(inv, false);
		BaseLogicCrafting craftingLogic = (BaseLogicCrafting) this.logic;
		ItemStack itemstack = craftingLogic.getCraftedItem();
		if (itemstack == null) return null;
		
		ItemIdentifierStack targetItemStack = ItemIdentifierStack.GetFromStack(itemstack);
		//return invUtil.getMultipleItems(targetItemStack.getItem(), targetItemStack.stackSize);
		return invUtil.getSingleItem(targetItemStack.getItem());
	}
	
	public void enableUpdateRequest() {
		init = false;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!init) {
			if(MainProxy.isClient()) {
				if(FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().thePlayer.sendQueue != null){
					PacketDispatcher.sendPacketToServer(new PacketCoordinates(NetworkConstants.REQUEST_CRAFTING_PIPE_UPDATE, xCoord, yCoord, zCoord).getPacket());
				}
			}
			init = true;
		}
		if(this instanceof PipeItemsCraftingLogisticsMk2) {
			return;
		}
		
		if ((!_orderManager.hasOrders() && _extras < 1) || worldObj.getWorldTime() % 6 != 0) return;
		
		LinkedList<AdjacentTile> crafters = locateCrafters();
		if (crafters.size() < 1 ){
			_orderManager.sendFailed();
			return;
		}
		
		for (AdjacentTile tile : locateCrafters()){
			ItemStack extracted = null; 
			if (tile.tile instanceof ISpecialInventory){
				extracted = extractFromISpecialInventory((ISpecialInventory) tile.tile);
			}else if (tile.tile instanceof IInventory) {
				extracted = extractFromIInventory((IInventory)tile.tile);
			}
			if (extracted == null) continue;
			while (extracted.stackSize > 0){
				ItemStack stackToSend = extracted.splitStack(1);
				Position p = new Position(tile.tile.xCoord, tile.tile.yCoord, tile.tile.zCoord, tile.orientation);
				if (_orderManager.hasOrders()){
					Pair<ItemIdentifierStack,IRequestItems> order = _orderManager.getNextRequest();
					IRoutedItem item = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stackToSend, worldObj);
					item.setSource(this.getRouter().getId());
					item.setDestination(order.getValue2().getRouter().getId());
					item.setTransportMode(TransportMode.Active);
					super.queueRoutedItem(item, tile.orientation);
					//super.sendRoutedItem(stackToSend, order.getDestination().getRouter().getId(), p);
					_orderManager.sendSuccessfull(1);
				}else{
					_extras--;
					if(LogisticsPipes.DisplayRequests)System.out.println("Extra dropped, " + _extras + " remaining");
					Position entityPos = new Position(p.x + 0.5, p.y + Utils.getPipeFloorOf(stackToSend), p.z + 0.5, p.orientation.reverse());
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
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_CRAFTER_TEXTURE;
	}
	
	public void canProvide(LogisticsTransaction transaction){
		
		if (!isEnabled()){
			return;
		}
		
		if (_extras < 1) return;
		for (LogisticsRequest request : transaction.getRemainingRequests()){
			ItemIdentifier providedItem = providedItem();
			if (request.getItem() != providedItem) continue;
			HashMap<ItemIdentifier, Integer> promised = transaction.getTotalPromised(this);
			int alreadyPromised = promised.containsKey(providedItem) ? promised.get(providedItem) : 0; 
			if (alreadyPromised >= _extras) continue;
			int remaining = _extras - alreadyPromised;
			LogisticsPromise promise = new LogisticsPromise();
			promise.item = providedItem;
			promise.numberOfItems = Math.min(remaining, request.notYetAllocated());
			promise.sender = this;
			promise.extra = true;
			request.addPromise(promise);
		}
	}

	@Override
	public void canCraft(LogisticsTransaction transaction) {
		
		if (!isEnabled()){
			return;
		}
		
		BaseLogicCrafting craftingLogic = (BaseLogicCrafting) this.logic;
		ItemStack stack = craftingLogic.getCraftedItem(); 
		if ( stack == null) return;
		
		CraftingTemplate template = new CraftingTemplate(ItemIdentifierStack.GetFromStack(stack), this);

		//Check all materials
		boolean hasSatellite = craftingLogic.isSatelliteConnected(); 
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
		transaction.addCraftingTemplate(template);
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (promise.extra){
			_extras -= promise.numberOfItems;
		}
		_orderManager.addOrder(new ItemIdentifierStack(promise.item, promise.numberOfItems), destination);
	}

	@Override
	public int getAvailableItemCount(ItemIdentifier item) {
		return 0;
	}

//	@Override
//	public Router getRouter() {
//		return router;
//	}

	@Override
	public void registerExtras(int count) {
		_extras += count;
		if(LogisticsPipes.DisplayRequests)System.out.println(count + " extras registered");
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getAllItems() {
		return new HashMap<ItemIdentifier, Integer>();
	}

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
			PacketDispatcher.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,xCoord,yCoord,zCoord,getLogisticsNetworkPacket()).getPacket(), (Player)player);
			final PacketInventoryChange newpacket = new PacketInventoryChange(NetworkConstants.CRAFTING_PIPE_IMPORT_BACK, xCoord, yCoord, zCoord, ((BaseLogicCrafting)logic).getDummyInventory());
			PacketDispatcher.sendPacketToPlayer(newpacket.getPacket(), (Player)player);
		}
	}
	
	public boolean canRegisterSign() {
		return ((BaseLogicCrafting)logic).signEntityX == 0 && ((BaseLogicCrafting)logic).signEntityY == 0 && ((BaseLogicCrafting)logic).signEntityZ == 0;
	}
	
	public void removeRegisteredSign() {
		((BaseLogicCrafting)logic).signEntityX = 0;
		((BaseLogicCrafting)logic).signEntityY = 0;
		((BaseLogicCrafting)logic).signEntityZ = 0;
		if(MainProxy.isServer()) {
			MainProxy.sendToPlayerList(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,xCoord,yCoord,zCoord,this.getLogisticsNetworkPacket()).getPacket(), localModeWatchers);
		}
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
		PacketDispatcher.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, xCoord, yCoord, zCoord, 1).getPacket());
	}

	@Override
	public void stopWaitching() {
		PacketDispatcher.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, xCoord, yCoord, zCoord, 1).getPacket());
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, _orderManager.getContentList()).getPacket(), localModeWatchers);
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
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList();
		if(!oldList.equals(all)) {
			oldList.clear();
			oldList.addAll(all);
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.ORDER_MANAGER_CONTENT, xCoord, yCoord, zCoord, all).getPacket(), localModeWatchers);
		}
	}

	@Override
	public void setOrderManagerContent(LinkedList<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}
	
}