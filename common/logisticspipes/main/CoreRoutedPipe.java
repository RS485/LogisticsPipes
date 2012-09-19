/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logisticspipes.IAdjacentWorldAccess;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.ITrackStatistics;
import logisticspipes.logisticspipes.PipeTransportLayer;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.TilePacketWrapper;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.IRouter;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import logisticspipes.utils.WorldUtil;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransport;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public abstract class CoreRoutedPipe extends Pipe implements IRequestItems, IAdjacentWorldAccess, ITrackStatistics, IWorldProvider, IWatchingHandler {

	public enum ItemSendMode {
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
	
	private final LinkedList<Pair3<IRoutedItem, Orientations, ItemSendMode>> _sendQueue = new LinkedList<Pair3<IRoutedItem, Orientations, ItemSendMode>>(); 
	
	public final List<EntityPlayer> watchers = new ArrayList<EntityPlayer>();
	
	public CoreRoutedPipe(BaseRoutingLogic logic, int itemID) {
		this(new PipeTransportLogistics(), logic, itemID);
	}
	
	public CoreRoutedPipe(PipeTransport transport, BaseRoutingLogic logic, int itemID) {
		super(transport, logic, itemID);
		((PipeTransportItems) transport).allowBouncing = true;
		
		pipecount++;
		//Roughly spread pipe updates throughout the frequency, no need to maintain balance
		_delayOffset = pipecount % Configs.LOGISTICS_DETECTION_FREQUENCY; 
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
	
	public logisticspipes.network.packets.PacketPayload getLogisticsNetworkPacket() {
		logisticspipes.network.packets.PacketPayload payload = new TilePacketWrapper(new Class[] { container.getClass(), transport.getClass(), logic.getClass() }).toPayload(xCoord, yCoord, zCoord, new Object[] { container, transport, logic });

		return payload;
	}
	
	public void queueRoutedItem(IRoutedItem routedItem, Orientations from) {
		_sendQueue.addLast(new Pair3<IRoutedItem, Orientations, ItemSendMode>(routedItem, from, ItemSendMode.Normal));
	}

	public void queueRoutedItem(IRoutedItem routedItem, Orientations from, ItemSendMode mode) {
		_sendQueue.addLast(new Pair3<IRoutedItem, Orientations, ItemSendMode>(routedItem, from, mode));
	}
	
	private void sendRoutedItem(IRoutedItem routedItem, Orientations from){
		Position p = new Position(this.xCoord + 0.5F, this.yCoord + Utils.getPipeFloorOf(routedItem.getItemStack()) + 0.5F, this.zCoord + 0.5F, from);
		p.moveForwards(0.5F);
		routedItem.SetPosition(p.x, p.y, p.z);
		((PipeTransportItems) transport).entityEntering(routedItem.getEntityPassiveItem(), from.reverse());
		
		//router.startTrackingRoutedItem((RoutedEntityItem) routedItem.getEntityPassiveItem());
		
		stat_lifetime_sent++;
		stat_session_sent++;
	}
	
	public abstract ItemSendMode getItemSendMode();
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		getRouter().update(worldObj.getWorldTime() % Configs.LOGISTICS_DETECTION_FREQUENCY == _delayOffset || _initialInit);
		_initialInit = false;
		if (!_sendQueue.isEmpty()){
			if(getItemSendMode() == ItemSendMode.Normal) {
				Pair<IRoutedItem, Orientations> itemToSend = _sendQueue.getFirst();
				sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
				_sendQueue.removeFirst();
				for(int i=0;i < 16 && !_sendQueue.isEmpty() && _sendQueue.getFirst().getValue3() == ItemSendMode.Fast;i++) {
					if (!_sendQueue.isEmpty()){
						itemToSend = _sendQueue.getFirst();
						sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
						_sendQueue.removeFirst();
					}
				}
			} else if(getItemSendMode() == ItemSendMode.Fast) {
				for(int i=0;i < 16;i++) {
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
	
	@Override
	public void invalidate() {
		if(getRouter() != null) {
			getRouter().destroy();
			router = null;
		}
		super.invalidate();
	}
	
	public abstract int getCenterTexture();
	
	@Override
	public String getTextureFile() {
		return Textures.BASE_TEXTURE_FILE;
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
		return Textures.LOGISTICSPIPE_ROUTED_TEXTURE;
	}
	
	public int getNonRoutedTexture(Orientations connection){
		return Textures.LOGISTICSPIPE_NOTROUTED_TEXTURE;
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
			router = SimpleServiceLocator.routerManager.getOrCreateRouter(UUID.fromString(routerId), MainProxy.getDimensionForWorld(worldObj), xCoord, yCoord, zCoord);
		}
		return router;
	}
	
	public void refreshRouterIdFromRouter() {
		if(router != null) {
			routerId = router.getId().toString();
		}
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
				if(MainProxy.isServer(world)) {
					entityplayer.openGui(LogisticsPipes.instance, getLogisticsModule().getGuiHandlerID(), world, xCoord, yCoord, zCoord);
					if(getLogisticsModule() instanceof ModuleExtractor) {
						PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, xCoord, yCoord, zCoord, ((ModuleExtractor)getLogisticsModule()).getSneakyOrientation().ordinal()).getPacket(), (Player)entityplayer);
					}
					return true;
				} else {
					return false;
				}
			}
		}
		return super.blockActivated(world, i, j, k, entityplayer);
	}
	
	public void refreshRender() {
		Field refreshRenderStateFiled;
		try {
			refreshRenderStateFiled = TileGenericPipe.class.getDeclaredField("refreshRenderState");
			refreshRenderStateFiled.setAccessible(true);
			refreshRenderStateFiled.set(this.container, true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
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

	@Override
	public World getWorld() {
		return this.worldObj;
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		watchers.add(player);
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		watchers.remove(player);
	}

	@Override
	public void itemCouldNotBeSend(ItemIdentifierStack item) {
		//Override by subclasses //TODO
	}
}
