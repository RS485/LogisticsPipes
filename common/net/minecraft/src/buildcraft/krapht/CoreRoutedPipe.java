/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import buildcraft.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.api.APIProxy;
import buildcraft.core.EntityPassiveItem;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.core.CoreProxy;
import buildcraft.core.Utils;
import buildcraft.core.network.PacketPayload;
import net.minecraft.src.buildcraft.krapht.logic.BaseRoutingLogic;
import net.minecraft.src.buildcraft.krapht.logic.LogicProvider;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.krapht.network.TilePacketWrapper;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsProviderLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsSupplierLogistics;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.buildcraft.krapht.routing.RoutedEntityItem;
import net.minecraft.src.buildcraft.logisticspipes.IAdjacentWorldAccess;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.ITrackStatistics;
import net.minecraft.src.buildcraft.logisticspipes.PipeTransportLayer;
import net.minecraft.src.buildcraft.logisticspipes.RouteLayer;
import net.minecraft.src.buildcraft.logisticspipes.TransportLayer;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import net.minecraft.src.krapht.AdjacentTile;
import net.minecraft.src.krapht.Pair;
import net.minecraft.src.krapht.WorldUtil;

public abstract class CoreRoutedPipe extends Pipe implements IRequestItems, IAdjacentWorldAccess, ITrackStatistics {

	protected enum ItemSendMode {
		Normal,
		Fast
	}
	
	private IRouter router;
	private String routerId;
	private static int pipecount = 0;
	private int _delayOffset = 0;
	protected int _nextTexture = getCenterTexture();
	
	private boolean _initialInit = true;
	
	private boolean enabled = true;
	
	private RouteLayer _routeLayer;
	protected TransportLayer _transportLayer;
	
	public int stat_session_sent;
	public int stat_session_recieved;
	public int stat_session_relayed;
	
	public long stat_lifetime_sent;
	public long stat_lifetime_recieved;
	public long stat_lifetime_relayed;
	
	private final LinkedList<Pair<IRoutedItem, Orientations>> _sendQueue = new LinkedList<Pair<IRoutedItem, Orientations>>(); 
	
	
	public CoreRoutedPipe(BaseRoutingLogic logic, int itemID) {
		super(new PipeTransportLogistics(), logic, itemID);
		((PipeTransportItems) transport).allowBouncing = true;
		
		pipecount++;
		//Roughly spread pipe updates throughout the frequency, no need to maintain balance
		_delayOffset = pipecount % core_LogisticsPipes.LOGISTICS_DETECTION_FREQUENCY; 
	}
	
	public RouteLayer getRouteLayer(){
		if (_routeLayer == null){
			_routeLayer = new RouteLayer(getRouter(), getTransportLayer());
		}
		return _routeLayer;
	}
	
	public TransportLayer getTransportLayer()
	{
		if (_transportLayer == null) {
			_transportLayer = new PipeTransportLayer(this, this, getRouter());
		}
		return _transportLayer;
	}
	
	public net.minecraft.src.buildcraft.krapht.network.PacketPayload getLogisticsNetworkPacket() {
		net.minecraft.src.buildcraft.krapht.network.PacketPayload payload = new TilePacketWrapper(new Class[] { container.getClass(), transport.getClass(), logic.getClass() }).toPayload(xCoord, yCoord, zCoord, new Object[] { container, transport, logic });

		return payload;
	}
	
//	@Deprecated
//	public void sendRoutedItem(ItemStack item, UUID destination, Position origin){
//		Position entityPos = new Position(origin.x + 0.5, origin.y + Utils.getPipeFloorOf(item), origin.z + 0.5, origin.orientation.reverse());
//		entityPos.moveForwards(0.5);
//		
//		RoutedEntityItem routedItem = new RoutedEntityItem(worldObj, new EntityPassiveItem(worldObj, entityPos.x, entityPos.y, entityPos.z, item));
//		routedItem.sourceUUID = this.getRouter().getId();
//		router.startTrackingRoutedItem(routedItem);
//		routedItem.destinationUUID = destination;
//		if (destination != null && SimpleServiceLocator.routerManager.isRouter(destination) ){
//			SimpleServiceLocator.routerManager.getRouter(destination).startTrackingInboundItem(routedItem);
//		}
//		
//		routedItem.speed = Utils.pipeNormalSpeed * core_LogisticsPipes.LOGISTICS_ROUTED_SPEED_MULTIPLIER;
//		((PipeTransportItems) transport).entityEntering(routedItem, entityPos.orientation);
//		stat_lifetime_sent++;
//		stat_session_sent++;
//	}
	
	/**
	 * Readjusts the routed item's coordinates and adds it to BC
	 * @param routedItem - The item to send, nothing will be done regarding source and destinations
	 * @param from - The orientation relative to this pipe where the item is coming from.
	 */
	
	public void queueRoutedItem(IRoutedItem routedItem, Orientations from){

		_sendQueue.addLast(new Pair<IRoutedItem, Orientations>(routedItem, from));
	}
	
	private void sendRoutedItem(IRoutedItem routedItem, Orientations from){
		Position p = new Position(this.xCoord + 0.5F, this.yCoord + Utils.getPipeFloorOf(routedItem.getItemStack()) + 0.5F, this.zCoord + 0.5F, from);
		p.moveForwards(0.5F);
		routedItem.SetPosition(p.x, p.y, p.z);
		((PipeTransportItems) transport).entityEntering(routedItem.getEntityPassiveItem(), from.reverse());
		
		router.startTrackingRoutedItem((RoutedEntityItem) routedItem.getEntityPassiveItem());
		
		stat_lifetime_sent++;
		stat_session_sent++;
	}
	
	public abstract ItemSendMode getItemSendMode();
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		getRouter().update(worldObj.getWorldTime() % core_LogisticsPipes.LOGISTICS_DETECTION_FREQUENCY == _delayOffset || _initialInit);
		_initialInit = false;
		if (!_sendQueue.isEmpty()){
			if(getItemSendMode() == ItemSendMode.Normal) {
				Pair<IRoutedItem, Orientations> itemToSend = _sendQueue.getFirst();
				sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
				_sendQueue.removeFirst();
			} else if(getItemSendMode() == ItemSendMode.Fast) {
				for(int i=0;i<64;i++) {
					if (!_sendQueue.isEmpty()){
						Pair<IRoutedItem, Orientations> itemToSend = _sendQueue.getFirst();
						sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
						_sendQueue.removeFirst();
					}
				}
			} else if(getItemSendMode() == null) {
				throw new UnsupportedOperationException("getItemSendMode() can't return null. "+this.getClass().getName());
			} else {
				throw new UnsupportedOperationException("getItemSendMode() returned unhandled value. " + getItemSendMode().name() + " in "+this.getClass().getName());
			}
		}
		if (getLogisticsModule() == null) return;
		if (!isEnabled()) return;
		getLogisticsModule().tick();
	}	
	
	@Override
	public void onBlockRemoval() {
		try {
			super.onBlockRemoval();
			if(getRouter() != null) {
				getRouter().destroy();
			}
			if (logic instanceof BaseRoutingLogic){
				((BaseRoutingLogic)logic).destroy();
			}
			//Just in case
			pipecount = Math.max(pipecount - 1, 0);
			
			if (transport != null && transport instanceof PipeTransportLogistics){
				((PipeTransportLogistics)transport).dropBuffer();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract int getCenterTexture();
	
	@Override
	public String getTextureFile() {
		return mod_LogisticsPipes.BASE_TEXTURE_FILE;
	}

	@Override
	public final int getTextureIndex(Orientations connection) {

		if (connection == Orientations.Unknown){
			return getCenterTexture();
		}
		
		if (getRouter().isRoutedExit(connection)) {
			return getRoutedTexture(connection);
			
		}
		else {
			return getNonRoutedTexture(connection);
		}
	}
	
	public int getRoutedTexture(Orientations connection){
		return core_LogisticsPipes.LOGISTICSPIPE_ROUTED_TEXTURE;
	}
	
	public int getNonRoutedTexture(Orientations connection){
		return core_LogisticsPipes.LOGISTICSPIPE_NOTROUTED_TEXTURE;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (routerId == null || routerId == ""){
			routerId = UUID.randomUUID().toString();
		}
		nbttagcompound.setString("routerId", routerId);
		nbttagcompound.setLong("stat_lifetime_sent", stat_lifetime_sent);
		nbttagcompound.setLong("stat_lifetime_recieved", stat_lifetime_recieved);
		nbttagcompound.setLong("stat_lifetime_relayed", stat_lifetime_relayed);
		if (getLogisticsModule() != null){
			getLogisticsModule().writeToNBT(nbttagcompound, "");
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		routerId = nbttagcompound.getString("routerId");
		
		stat_lifetime_sent = nbttagcompound.getLong("stat_lifetime_sent");
		stat_lifetime_recieved = nbttagcompound.getLong("stat_lifetime_recieved");
		stat_lifetime_relayed = nbttagcompound.getLong("stat_lifetime_relayed");
		if (getLogisticsModule() != null){
			getLogisticsModule().readFromNBT(nbttagcompound, "");
		}
	}
	
	@Override
	public IRouter getRouter() {
		if (router == null){
			if (routerId == null || routerId == ""){
				routerId = UUID.randomUUID().toString();
			}
			router = SimpleServiceLocator.routerManager.getOrCreateRouter(UUID.fromString(routerId), worldObj.getWorldInfo().getDimension(), xCoord, yCoord, zCoord);
		}
		return router;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled; 
	}
	

	public void onNeighborBlockChange_Logistics(){}
	
	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();
	}
	
	public abstract ILogisticsModule getLogisticsModule();
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem && !(entityplayer.isSneaking())){
			if (getLogisticsModule() != null && getLogisticsModule().getGuiHandlerID() != -1){
				if(!APIProxy.isClient(entityplayer.worldObj)) {
					entityplayer.openGui(mod_LogisticsPipes.instance, getLogisticsModule().getGuiHandlerID(), world, xCoord, yCoord, zCoord);
					if(APIProxy.isServerSide() && getLogisticsModule() instanceof ModuleItemSink) {
						CoreProxy.sendToPlayer(entityplayer, new PacketPipeInteger(NetworkConstants.ITEM_SINK_STATUS, xCoord, yCoord, zCoord, ((ModuleItemSink)getLogisticsModule()).isDefaultRoute() ? 1 : 0));
					}
					if(APIProxy.isServerSide() && getLogisticsModule() instanceof ModuleExtractor) {
						CoreProxy.sendToPlayer(entityplayer, new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, xCoord, yCoord, zCoord, ((ModuleExtractor)getLogisticsModule()).getSneakyOrientation().ordinal()));
					}
					return true;
				} else {
					return false;
				}
			}
		}
		return super.blockActivated(world, i, j, k, entityplayer);
	}
	
	
	
	/***  --  IAdjacentWorldAccess  --  ***/
	
	@Override
	public LinkedList<AdjacentTile> getConnectedEntities() {
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities();
		
		Iterator<AdjacentTile> iterator = adjacent.iterator();
		while (iterator.hasNext()){
			AdjacentTile tile = iterator.next();
			if (!SimpleServiceLocator.buildCraftProxy.checkPipesConnections(this.container, tile.tile)){
				iterator.remove();
			}
		}
		
		return adjacent;
	}
	
	@Override
	public int getRandomInt(int maxSize) {
		return worldObj.rand.nextInt(maxSize);
	}
	
	/***  --  ITrackStatistics  --  ***/
	
	@Override
	public void recievedItem(int count) {
		stat_session_recieved += count;
		stat_lifetime_recieved += count;
	}
	
}
