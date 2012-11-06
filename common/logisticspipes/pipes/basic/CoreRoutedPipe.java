/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes.basic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.ILogisticsPowerProvider;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logisticspipes.IAdjacentWorldAccess;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.ITrackStatistics;
import logisticspipes.logisticspipes.PipeTransportLayer;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.TilePacketWrapper;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ServerRouter;
import logisticspipes.ticks.WorldTickHandler;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.WorldUtil;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
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

@CCType(name = "LogisticsPipes:Normal")
public abstract class CoreRoutedPipe extends Pipe implements IRequestItems, IAdjacentWorldAccess, ITrackStatistics, IWorldProvider, IWatchingHandler, IChassiePowerProvider {

	public enum ItemSendMode {
		Normal,
		Fast
	}
	
	protected boolean stillNeedReplace = true;
	
	private IRouter router;
	private String routerId;
	private Object routerIdLock = new Object();
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
	
	public int server_routing_table_size = 0;
	
	protected final LinkedList<Pair3<IRoutedItem, Orientations, ItemSendMode>> _sendQueue = new LinkedList<Pair3<IRoutedItem, Orientations, ItemSendMode>>(); 
	
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
		sendQueueChanged();
	}

	public void queueRoutedItem(IRoutedItem routedItem, Orientations from, ItemSendMode mode) {
		_sendQueue.addLast(new Pair3<IRoutedItem, Orientations, ItemSendMode>(routedItem, from, mode));
		sendQueueChanged();
	}
	
	protected void sendQueueChanged() {}
	
	private void sendRoutedItem(IRoutedItem routedItem, Orientations from){
		Position p = new Position(this.xCoord + 0.5F, this.yCoord + Utils.getPipeFloorOf(routedItem.getItemStack()) + 0.5F, this.zCoord + 0.5F, from);
		p.moveForwards(0.5F);
		routedItem.SetPosition(p.x, p.y, p.z);
		((PipeTransportItems) transport).entityEntering(routedItem.getEntityPassiveItem(), from.reverse());
		
		//router.startTrackingRoutedItem((RoutedEntityItem) routedItem.getEntityPassiveItem());
		
		stat_lifetime_sent++;
		stat_session_sent++;
		updateStats();
	}
	
	public abstract ItemSendMode getItemSendMode();
	
	private boolean checkTileEntity(boolean force) {
		if(worldObj.getWorldTime() % 10 == 0 || force) {
			if(this.container.getClass() != LogisticsPipes.logisticsTileGenericPipe) {
				TileEntity tile = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord);
				if(tile != this.container) {
					System.out.println("LocalCodeError");
				}
				if(MainProxy.isClient()) {
					WorldTickHandler.clientPipesToReplace.add(this.container);
				} else {
					WorldTickHandler.serverPipesToReplace.add(this.container);
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(checkTileEntity(_initialInit)) {
			stillNeedReplace = true;
			return;
		} else {
			stillNeedReplace = false;
		}
		getRouter().update(worldObj.getWorldTime() % Configs.LOGISTICS_DETECTION_FREQUENCY == _delayOffset || _initialInit);
		_initialInit = false;
		if (!_sendQueue.isEmpty()){
			if(getItemSendMode() == ItemSendMode.Normal || !SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
				Pair<IRoutedItem, Orientations> itemToSend = _sendQueue.getFirst();
				sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
				_sendQueue.removeFirst();
				if(SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
					for(int i=0;i < 16 && !_sendQueue.isEmpty() && _sendQueue.getFirst().getValue3() == ItemSendMode.Fast;i++) {
						if (!_sendQueue.isEmpty()){
							itemToSend = _sendQueue.getFirst();
							sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
							_sendQueue.removeFirst();
						}
					}
				}
				sendQueueChanged();
			} else if(getItemSendMode() == ItemSendMode.Fast) {
				for(int i=0;i < 16;i++) {
					if (!_sendQueue.isEmpty()){
						Pair<IRoutedItem, Orientations> itemToSend = _sendQueue.getFirst();
						sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
						_sendQueue.removeFirst();
					}
				}
				sendQueueChanged();
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
		
		synchronized (routerIdLock) {
			if (routerId == null || routerId == ""){
				routerId = UUID.randomUUID().toString();
			}
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
		
		synchronized (routerIdLock) {
			routerId = nbttagcompound.getString("routerId");
		}
		
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
			synchronized (routerIdLock) {
				if (routerId == null || routerId == ""){
					routerId = UUID.randomUUID().toString();
				}
				router = SimpleServiceLocator.routerManager.getOrCreateRouter(UUID.fromString(routerId), MainProxy.getDimensionForWorld(worldObj), xCoord, yCoord, zCoord);
			}
		}
		return router;
	}
	
	public void refreshRouterIdFromRouter() {
		if(router != null) {
			synchronized (routerIdLock) {
				routerId = router.getId().toString();
			}
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
		if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			if (getLogisticsModule() != null && getLogisticsModule().getGuiHandlerID() != -1){
				if(MainProxy.isServer(world)) {
					entityplayer.openGui(LogisticsPipes.instance, getLogisticsModule().getGuiHandlerID(), world, xCoord, yCoord, zCoord);
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
		updateStats();
	}
	
	@Override
	public void relayedItem(int count) {
		stat_session_relayed += count;
		stat_lifetime_relayed += count;
		updateStats();
	}

	@Override
	public World getWorld() {
		return this.worldObj;
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 0) {
			watchers.add(player);
			PacketDispatcher.sendPacketToPlayer(new PacketRoutingStats(NetworkConstants.STAT_UPDATE, this).getPacket(), (Player)player);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		if(mode == 0) {
			watchers.remove(player);
		}
	}
	
	public void updateStats() {
		if(watchers.size() > 0) {
			MainProxy.sendToPlayerList(new PacketRoutingStats(NetworkConstants.STAT_UPDATE, this).getPacket(), watchers);
		}
	}
	
	@Override
	public void itemCouldNotBeSend(ItemIdentifierStack item) {
		//Override by subclasses //TODO
	}

	public boolean isLockedExit(Orientations orientation) {
		return false;
	}
	
	/* Power System */
	
	public List<ILogisticsPowerProvider> getRoutedPowerProviders() {
		if(MainProxy.isServer()) {
			return ((ServerRouter)this.getRouter()).getPowerProvider();
		} else {
			return null;
		}
	}
	
	public boolean useEnergy(int amount) {
		if(Configs.LOGISTICS_POWER_USAGE_DISABLED) return true;
		if(MainProxy.isClient()) return false;
		List<ILogisticsPowerProvider> list = getRoutedPowerProviders();
		for(ILogisticsPowerProvider provider: list) {
			if(provider.canUseEnergy(amount)) {
				provider.useEnergy(amount);
				return true;
			}
		}
		return false;
	}
	
	public void queueEvent(String event, Object[] arguments) {
		if(this.container instanceof LogisticsTileGenericPipe) {
			((LogisticsTileGenericPipe)this.container).queueEvent(event, arguments);
		}
	}
	
	public boolean stillNeedReplace() {
		return stillNeedReplace;
	}
	
	/* --- CCCommands --- */
	@CCCommand
	public String getRouterId() {
		return getRouter().getId().toString();
	}

	@CCCommand
	public void setTurtleConnect(Boolean flag) {
		if(this.container instanceof LogisticsTileGenericPipe) {
			((LogisticsTileGenericPipe)this.container).setTurtrleConnect(flag);
		}
	}

	@CCCommand
	public boolean getTurtleConnect() {
		if(this.container instanceof LogisticsTileGenericPipe) {
			return ((LogisticsTileGenericPipe)this.container).getTurtrleConnect();
		}
		return false;
	}

	@CCCommand
	public int getItemID(Double itemId) throws Exception {
		ItemIdentifier item = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(item == null) throw new Exception("Invalid ItemIdentifierID");
		return item.itemID;
	}

	@CCCommand
	public int getItemDamage(Double itemId) throws Exception {
		ItemIdentifier itemd = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(itemd == null) throw new Exception("Invalid ItemIdentifierID");
		return itemd.itemDamage;
	}

	@CCCommand
	public Map<Object,Object> getNBTTagCompound(Double itemId) throws Exception {
		ItemIdentifier itemn = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(itemn == null) throw new Exception("Invalid ItemIdentifierID");
		return itemn.getNBTTagCompoundAsMap();
	}

	@CCCommand
	public int getItemIdentifierIDFor(Double itemID, Double itemDamage) {
		return ItemIdentifier.get((int)Math.floor(itemID), (int)Math.floor(itemDamage), null).getId();
	}

	@CCCommand
	public String getItemName(Double itemId) throws Exception {
		ItemIdentifier itemd = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(itemd == null) throw new Exception("Invalid ItemIdentifierID");
		return itemd.getFriendlyName();
	}
}
