/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.DelayQueue;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.gates.ActionDisableLogistics;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISecurityProvider;
import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logisticspipes.IAdjacentWorldAccess;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.ITrackStatistics;
import logisticspipes.logisticspipes.PipeTransportLayer;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.TilePacketWrapper;
import logisticspipes.network.packets.PacketRoutingStats;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.security.PermissionException;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.ticks.WorldTickHandler;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.utils.Utils;
import buildcraft.transport.EntityData;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

@CCType(name = "LogisticsPipes:Normal")
public abstract class CoreRoutedPipe extends Pipe implements IRequestItems, IAdjacentWorldAccess, ITrackStatistics, IWorldProvider, IWatchingHandler, IRoutedPowerProvider {

	public enum ItemSendMode {
		Normal,
		Fast
	}

	protected boolean stillNeedReplace = true;
	
	protected IRouter router;
	protected String routerId;
	protected Object routerIdLock = new Object();
	private static int pipecount = 0;
	protected int _delayOffset = 0;
	
	private boolean _textureBufferPowered;
	
	protected boolean _initialInit = true;
	
	private boolean enabled = true;
	
	protected RouteLayer _routeLayer;
	protected TransportLayer _transportLayer;
	private DelayQueue<IRoutedItem> _inTransitToMe = new DelayQueue<IRoutedItem>();
	
	private UpgradeManager upgradeManager = new UpgradeManager(this);
	
	public int stat_session_sent;
	public int stat_session_recieved;
	public int stat_session_relayed;
	
	public long stat_lifetime_sent;
	public long stat_lifetime_recieved;
	public long stat_lifetime_relayed;
	
	public int server_routing_table_size = 0;
	
	protected final LinkedList<Pair3<IRoutedItem, ForgeDirection, ItemSendMode>> _sendQueue = new LinkedList<Pair3<IRoutedItem, ForgeDirection, ItemSendMode>>();
	
	protected final ArrayList<IPipedItem> queuedDataForUnroutedItems = new ArrayList<IPipedItem>();
	
	public final List<EntityPlayer> watchers = new ArrayList<EntityPlayer>();

	protected List<IInventory> _cachedAdjacentInventories;
	
	public CoreRoutedPipe(BaseRoutingLogic logic, int itemID) {
		this(new PipeTransportLogistics(), logic, itemID);
	}

	public CoreRoutedPipe(PipeTransportLogistics transport, BaseRoutingLogic logic, int itemID) {
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

	public UpgradeManager getUpgradeManager() {
		return upgradeManager;
	}
	
	public logisticspipes.network.packets.PacketPayload getLogisticsNetworkPacket() {
		logisticspipes.network.packets.PacketPayload payload = new TilePacketWrapper(new Class[] { container.getClass(), transport.getClass(), logic.getClass() }).toPayload(xCoord, yCoord, zCoord, new Object[] { container, transport, logic });

		return payload;
	}
	
	public void queueRoutedItem(IRoutedItem routedItem, ForgeDirection from) {
		_sendQueue.addLast(new Pair3<IRoutedItem, ForgeDirection, ItemSendMode>(routedItem, from, ItemSendMode.Normal));
		sendQueueChanged(false);
	}

	public void queueRoutedItem(IRoutedItem routedItem, ForgeDirection from, ItemSendMode mode) {
		_sendQueue.addLast(new Pair3<IRoutedItem, ForgeDirection, ItemSendMode>(routedItem, from, mode));
		sendQueueChanged(false);
	}
	/** 
	 * @param force  == true never delegates to a thread
	 * @return number of things sent.
	 */
	public int sendQueueChanged(boolean force) {return 0;}
	
	private void sendRoutedItem(IRoutedItem routedItem, ForgeDirection from){
		Position p = new Position(this.xCoord + 0.5F, this.yCoord + Utils.getPipeFloorOf(routedItem.getItemStack()), this.zCoord + 0.5F, from);
		if(from == ForgeDirection.DOWN) {
			p.moveForwards(0.24F);
		} else if(from == ForgeDirection.UP) {
			p.moveForwards(0.74F);
		} else {
			p.moveForwards(0.49F);
		}
		routedItem.SetPosition(p.x, p.y, p.z);
		((PipeTransportItems) transport).entityEntering(routedItem.getEntityPassiveItem(), from.getOpposite());
		
		IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(routedItem.getDestination(),false);
		if(r != null) {
			CoreRoutedPipe pipe = r.getCachedPipe();
			if(pipe !=null) // pipes can unload at inconvenient times ...
				pipe.notifyOfSend(routedItem);
			else {
				//TODO: handle sending items to known chunk-unloaded destination?
			}
		} // should not be able to send to a non-existing router
		//router.startTrackingRoutedItem((RoutedEntityItem) routedItem.getEntityPassiveItem());
		MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, this.xCoord, this.yCoord, this.zCoord, this.worldObj, 2);
		stat_lifetime_sent++;
		stat_session_sent++;
		updateStats();
	}
	
	private void notifyOfSend(IRoutedItem routedItem) {
		this._inTransitToMe.add(routedItem);
		
	}

	public abstract ItemSendMode getItemSendMode();
	
	private boolean checkTileEntity(boolean force) {
		if(worldObj.getWorldTime() % 10 == 0 || force) {
			if(this.container.getClass() != BuildCraftProxy.logisticsTileGenericPipe) {
				TileEntity tile = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord);
				if(tile != this.container) {
					LogisticsPipes.log.severe("LocalCodeError");
				}
				if(MainProxy.isClient(worldObj)) {
					WorldTickHandler.clientPipesToReplace.add(this.container);
				} else {
					WorldTickHandler.serverPipesToReplace.add(this.container);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Designed to help protect against routing loops - if both pipes are on the same block, and of ISided overlapps, return true
	 * @param other
	 * @return boolean indicating if both pull from the same inventory.
	 */
	public boolean sharesInventoryWith(CoreRoutedPipe other){
		List<IInventory> others = other.getConnectedRawInventories();
		if(others==null || others.size()==0)
			return false;
		for(IInventory i : getConnectedRawInventories()) {
			if(others.contains(i)) {
				return true;
			}
		}
		return false;
	}
	
	protected List<IInventory> getConnectedRawInventories()	{
		if(_cachedAdjacentInventories != null) {
			return _cachedAdjacentInventories;
		}
		WorldUtil worldUtil = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<IInventory> adjacent = new LinkedList<IInventory>();
		for (AdjacentTile tile : worldUtil.getAdjacentTileEntities(true)){
			if (tile.tile instanceof TileGenericPipe) continue;
			if (!(tile.tile instanceof IInventory)) continue;
			adjacent.add(InventoryHelper.getInventory((IInventory)tile.tile));
		}
		_cachedAdjacentInventories=adjacent;
		return _cachedAdjacentInventories;
	}
	
	/*** 
	 * Only Called Server Side
	 * Only Called when the pipe is enabled
	 */
	public void enabledUpdateEntity() {}
	
	/***
	 * Called Server and Client Side
	 * Called every tick
	 */
	public void ignoreDisableUpdateEntity() {}
	
	@Override
	public final void updateEntity() {
		if(checkTileEntity(_initialInit)) {
			stillNeedReplace = true;
			return;
		} else {
			if(stillNeedReplace) {
				stillNeedReplace = false;
				worldObj.notifyBlockChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
				for(Pair3<IRoutedItem, ForgeDirection, ItemSendMode> item : _sendQueue) {
					//assign world to any entityitem we created in readfromnbt
					item.getValue1().getEntityPassiveItem().setWorld(worldObj);
				}
				//first tick just create a router and do nothing.
				getRouter();
				return;
			}
		}
		// remove old items _inTransit -- these should have arived, but have probably been lost instead. In either case, it will allow a re-send so that another attempt to re-fill the inventory can be made.
		while(this._inTransitToMe.poll()!=null){}
		//update router before ticking logic/transport
		getRouter().update(worldObj.getWorldTime() % Configs.LOGISTICS_DETECTION_FREQUENCY == _delayOffset || _initialInit);
		getUpgradeManager().securityTick();
		super.updateEntity();
		ignoreDisableUpdateEntity();
		_initialInit = false;
		if (!_sendQueue.isEmpty()){
			if(getItemSendMode() == ItemSendMode.Normal || !SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
				Pair3<IRoutedItem, ForgeDirection, ItemSendMode> itemToSend = _sendQueue.getFirst();
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
				sendQueueChanged(false);
			} else if(getItemSendMode() == ItemSendMode.Fast) {
				for(int i=0;i < 16;i++) {
					if (!_sendQueue.isEmpty()){
						Pair3<IRoutedItem, ForgeDirection, ItemSendMode> itemToSend = _sendQueue.getFirst();
						sendRoutedItem(itemToSend.getValue1(), itemToSend.getValue2());
						_sendQueue.removeFirst();
					}
				}
				sendQueueChanged(false);
			} else if(getItemSendMode() == null) {
				throw new UnsupportedOperationException("getItemSendMode() can't return null. "+this.getClass().getName());
			} else {
				throw new UnsupportedOperationException("getItemSendMode() returned unhandled value. " + getItemSendMode().name() + " in "+this.getClass().getName());
			}
		}
		if(MainProxy.isClient(worldObj)) return;
		checkTexturePowered();
		if (!isEnabled()) return;
		enabledUpdateEntity();
		if (getLogisticsModule() == null) return;
		getLogisticsModule().tick();
	}	
	
	@Override
	public void onBlockRemoval() {
		try {
			super.onBlockRemoval();
			//invalidate() removes the router
			if (logic instanceof BaseRoutingLogic){
				((BaseRoutingLogic)logic).destroy();
			}
			//Just in case
			pipecount = Math.max(pipecount - 1, 0);
			
			if (transport != null && transport instanceof PipeTransportLogistics){
				((PipeTransportLogistics)transport).dropBuffer();
			}
			getUpgradeManager().dropUpgrades(worldObj, xCoord, yCoord, zCoord);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if(router != null) {
			router.destroy();
			router = null;
		}
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(router != null){
			router.clearPipeCache();
			router.clearInterests();
		}
	}
	
	public void checkTexturePowered() {
		if(Configs.LOGISTICS_POWER_USAGE_DISABLED) return;
		if(worldObj.getWorldTime() % 10 != 0) return;
		if(stillNeedReplace || _initialInit || router == null) return;
		boolean flag;
		if((flag = canUseEnergy(1)) != _textureBufferPowered) {
			_textureBufferPowered = flag;
			refreshRender(false);
			MainProxy.sendSpawnParticlePacket(Particles.RedParticle, this.xCoord, this.yCoord, this.zCoord, this.worldObj, 3);
		}
	}
	
	
	public abstract TextureType getCenterTexture();
	
	@Override
	public String getTextureFile() {
		return Textures.BASE_TEXTURE_FILE;
	}
	
	@Override
	public final int getTextureIndex(ForgeDirection connection) {
		TextureType texture = getTextureType(connection);
		if(_textureBufferPowered) {
			return texture.powered;
		} else if(Configs.LOGISTICS_POWER_USAGE_DISABLED) {
			return texture.normal;
		} else {
			return texture.unpowered;
		}
	}
	
	public TextureType getTextureType(ForgeDirection connection) {
		if(stillNeedReplace || _initialInit)
			return getCenterTexture();

		if (connection == ForgeDirection.UNKNOWN){
			return getCenterTexture();
		} else if ((router != null) && getRouter(connection).isRoutedExit(connection)) {
			return getRoutedTexture(connection);
			
		} else {
			return getNonRoutedTexture(connection);
		}
	}
	
	public TextureType getRoutedTexture(ForgeDirection connection){
		return Textures.LOGISTICSPIPE_ROUTED_TEXTURE;
	}
	
	public TextureType getNonRoutedTexture(ForgeDirection connection){
		return Textures.LOGISTICSPIPE_NOTROUTED_TEXTURE;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		synchronized (routerIdLock) {
			if (routerId == null || routerId == ""){
				if(router != null)
					routerId = router.getId().toString();
				else
					routerId = UUID.randomUUID().toString();
			}
		}
		nbttagcompound.setString("routerId", routerId);
		nbttagcompound.setLong("stat_lifetime_sent", stat_lifetime_sent);
		nbttagcompound.setLong("stat_lifetime_recieved", stat_lifetime_recieved);
		nbttagcompound.setLong("stat_lifetime_relayed", stat_lifetime_relayed);
		if (getLogisticsModule() != null){
			getLogisticsModule().writeToNBT(nbttagcompound);
		}
		NBTTagCompound upgradeNBT = new NBTTagCompound();
		upgradeManager.writeToNBT(upgradeNBT);
		nbttagcompound.setCompoundTag("upgradeManager", upgradeNBT);

		NBTTagList sendqueue = new NBTTagList();
		for(Pair3<IRoutedItem, ForgeDirection, ItemSendMode> p : _sendQueue) {
			NBTTagCompound tagentry = new NBTTagCompound();
			NBTTagCompound tagentityitem = new NBTTagCompound();
			p.getValue1().getEntityPassiveItem().writeToNBT(tagentityitem);
			tagentry.setCompoundTag("entityitem", tagentityitem);
			tagentry.setByte("from", (byte)(p.getValue2().ordinal()));
			tagentry.setByte("mode", (byte)(p.getValue3().ordinal()));
			sendqueue.appendTag(tagentry);
		}
		nbttagcompound.setTag("sendqueue", sendqueue);
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
			getLogisticsModule().readFromNBT(nbttagcompound);
		}
		upgradeManager.readFromNBT(nbttagcompound.getCompoundTag("upgradeManager"));

		_sendQueue.clear();
		NBTTagList sendqueue = nbttagcompound.getTagList("sendqueue");
		for(int i = 0; i < sendqueue.tagCount(); i++) {
			NBTTagCompound tagentry = (NBTTagCompound)sendqueue.tagAt(i);
			NBTTagCompound tagentityitem = tagentry.getCompoundTag("entityitem");
			IPipedItem entity = new EntityPassiveItem(null);
			entity.readFromNBT(tagentityitem);
			IRoutedItem routeditem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(null, entity);
			ForgeDirection from = ForgeDirection.values()[tagentry.getByte("from")];
			ItemSendMode mode = ItemSendMode.values()[tagentry.getByte("mode")];
			_sendQueue.add(new Pair3<IRoutedItem, ForgeDirection, ItemSendMode>(routeditem, from, mode));
		}
	}
	
	@Override
	public IRouter getRouter() {
		if(stillNeedReplace) {
			System.out.println("Hey, don't get routers for pipes that aren't ready");
			new Throwable().printStackTrace();
		}
		if (router == null){
			synchronized (routerIdLock) {
				
				UUID routerIntId = null;
				if(routerId!=null && !routerId.isEmpty())
					routerIntId = UUID.fromString(routerId);
				router = SimpleServiceLocator.routerManager.getOrCreateRouter(routerIntId, MainProxy.getDimensionForWorld(worldObj), xCoord, yCoord, zCoord, false);
			}
		}
		return router;
	}
	
	public IRouter getRouter(ForgeDirection dir) {
		return getRouter();
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled; 
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);
		clearCache();
		if(!stillNeedReplace && MainProxy.isServer(worldObj)) {
			onNeighborBlockChange_Logistics();
		}
	}

	public void onNeighborBlockChange_Logistics(){}
	
	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();
	}
	
	public abstract ILogisticsModule getLogisticsModule();
	
	@Override
	public final boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		SecuritySettings settings = null;
		if(MainProxy.isServer(world)) {
			LogisticsSecurityTileEntity station = SimpleServiceLocator.securityStationManager.getStation(getUpgradeManager().getSecurityID());
			if(station != null) {
				settings = station.getSecuritySettingsForPlayer(entityplayer, true);
			}
		}
		if(handleClick(world, i, j, k, entityplayer, settings)) return true;
		if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			if(wrenchClicked(world, i, j, k, entityplayer, settings)) {
				return true;
			}
		}
		if(SimpleServiceLocator.buildCraftProxy.isUpgradeManagerEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			if(MainProxy.isServer(world)) {
				if (settings == null || settings.openUpgrades) {
					getUpgradeManager().openGui(entityplayer, this);
				} else {
					entityplayer.sendChatToPlayer("Permission denied");
				}
			}
			return true;
		}
		if(!(entityplayer.isSneaking()) && getUpgradeManager().tryIserting(world, entityplayer)) {
			return true;
		}
		return super.blockActivated(world, i, j, k, entityplayer);
	}
	
	protected boolean handleClick(World world, int i, int j, int k, EntityPlayer entityplayer, SecuritySettings settings) {
		return false;
	}
	
	protected boolean wrenchClicked(World world, int i, int j, int k, EntityPlayer entityplayer, SecuritySettings settings) {
		if (getLogisticsModule() != null && getLogisticsModule() instanceof ILogisticsGuiModule) {
			if(MainProxy.isServer(world)) {
				if (settings == null || settings.openGui) {
					entityplayer.openGui(LogisticsPipes.instance, ((ILogisticsGuiModule)getLogisticsModule()).getGuiHandlerID(), world, xCoord, yCoord, zCoord);
				} else {
					entityplayer.sendChatToPlayer("Permission denied");
				}
			}
			return true;
		}
		return false;
	}
	
	protected void clearCache() {
		_cachedAdjacentInventories=null;
	}
	
	public void refreshRender(boolean spawnPart) {
		
		this.container.scheduleRenderUpdate();
		if (spawnPart) {
			MainProxy.sendSpawnParticlePacket(Particles.GreenParticle, this.xCoord, this.yCoord, this.zCoord, this.worldObj, 3);
		}
	}
	
	public void refreshConnectionAndRender(boolean spawnPart) {
		clearCache();
		this.container.scheduleNeighborChange();
		if (spawnPart) {
			MainProxy.sendSpawnParticlePacket(Particles.GreenParticle, this.xCoord, this.yCoord, this.zCoord, this.worldObj, 3);
		}
	}
	
	/***  --  IAdjacentWorldAccess  --  ***/
	
	@Override
	public LinkedList<AdjacentTile> getConnectedEntities() {
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities(true);
		
		Iterator<AdjacentTile> iterator = adjacent.iterator();
		while (iterator.hasNext()){
			AdjacentTile tile = iterator.next();
			if (!SimpleServiceLocator.buildCraftProxy.checkPipesConnections(this.container, tile.tile, tile.orientation)){
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
			MainProxy.sendPacketToPlayer(new PacketRoutingStats(NetworkConstants.STAT_UPDATE, this).getPacket(), (Player)player);
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
		if(logic instanceof IRequireReliableTransport) {
			((IRequireReliableTransport)logic).itemLost(item);
		}
		//Override by subclasses //TODO
	}

	public boolean isLockedExit(ForgeDirection orientation) {
		return false;
	}
	
	public boolean logisitcsIsPipeConnected(TileEntity tile) {
		return false;
	}
	
	public boolean disconnectPipe(TileEntity tile, ForgeDirection dir) {
		return false;
	}
	
	@Override
	public final boolean isPipeConnected(TileEntity tile, ForgeDirection dir) {
		return isPipeConnected(tile, dir, false);
	}
	
	public boolean globalIgnoreConnectionDisconnection = false;
	
	public final boolean isPipeConnected(TileEntity tile, ForgeDirection dir, boolean ignoreSystemDisconnection) {
		ForgeDirection side = OrientationsUtil.getOrientationOfTilewithPipe((PipeTransportItems) this.transport, tile);
		if(getUpgradeManager().isSideDisconnected(side)) {
			return false;
		}
		if(!stillNeedReplace) {
			if(getRouter().isSideDisconneceted(side) && !ignoreSystemDisconnection && !globalIgnoreConnectionDisconnection) {
				return false;
			}
		}
		return (super.isPipeConnected(tile, dir) || logisitcsIsPipeConnected(tile)) && !disconnectPipe(tile, dir);
	}
	
	public void connectionUpdate() {
		if(container != null && !stillNeedReplace) {
			container.scheduleNeighborChange();
			worldObj.notifyBlockChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
		}
	}
	
	public UUID getSecurityID() {
		return getUpgradeManager().getSecurityID();
	}

	public void insetSecurityID(UUID id) {
		getUpgradeManager().insetSecurityID(id);
	}
	
	/* Power System */

	public List<ILogisticsPowerProvider> getRoutedPowerProviders() {
		if(MainProxy.isClient(worldObj)) {
			return null;
		}
		if(stillNeedReplace) {
			return null;
		}
		return this.getRouter().getPowerProvider();
	}
	
	public boolean useEnergy(int amount){
		return useEnergy(amount, null);
	}
	public boolean canUseEnergy(int amount){
		return canUseEnergy(amount,null);
	}

	public boolean canUseEnergy(int amount, List<Object> providersToIgnore) {
		if(MainProxy.isClient(worldObj)) return false;
		if(Configs.LOGISTICS_POWER_USAGE_DISABLED) return true;
		if(amount == 0) return true;
		if(providersToIgnore !=null && providersToIgnore.contains(this))
			return false;
		List<ILogisticsPowerProvider> list = getRoutedPowerProviders();
		if(list == null) return false;
		for(ILogisticsPowerProvider provider: list) {
			if(provider.canUseEnergy(amount, providersToIgnore)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean useEnergy(int amount, List<Object> providersToIgnore) {
		if(MainProxy.isClient(worldObj)) return false;
		if(Configs.LOGISTICS_POWER_USAGE_DISABLED) return true;
		if(amount == 0) return true;
		if(providersToIgnore==null)
			providersToIgnore = new ArrayList<Object>();
		if(providersToIgnore.contains(this))
			return false;
		providersToIgnore.add(this);
		List<ILogisticsPowerProvider> list = getRoutedPowerProviders();
		if(list == null) return false;
		for(ILogisticsPowerProvider provider: list) {
			if(provider.canUseEnergy(amount, providersToIgnore)) {
				provider.useEnergy(amount, providersToIgnore);
				int particlecount = amount;
				if (particlecount > 10) {
					particlecount = 10;
				}
				MainProxy.sendSpawnParticlePacket(Particles.GoldParticle, this.xCoord, this.yCoord, this.zCoord, this.worldObj, particlecount);
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
	
	@Override
	public int compareTo(IRequestItems other){
		return this.getID()-other.getID();
	}
	
	@Override
	public int getID(){
		return this.itemID;
	}

	public Set<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	public boolean hasGenericInterests() {
		return false;
	}
	
	public ISecurityProvider getSecurityProvider() {
		return SimpleServiceLocator.securityStationManager.getStation(getUpgradeManager().getSecurityID());
	}
	
	public void checkCCAccess() throws PermissionException {
		ISecurityProvider sec = getSecurityProvider();
		if(sec != null) {
			if(!sec.getAllowCC()) {
				throw new PermissionException();
			}
		}
	}

	public void queueUnroutedItemInformation(EntityData data) {
		if(data.item != null && data.item.getItemStack() != null) {
			data.item.setItemStack(data.item.getItemStack().copy());
			queuedDataForUnroutedItems.add(data.item);
		}
	}
	
	public IPipedItem getQueuedForItemStack(ItemStack stack) {
		for(IPipedItem item:queuedDataForUnroutedItems) {
			if(ItemIdentifierStack.GetFromStack(item.getItemStack()).equals(ItemIdentifierStack.GetFromStack(stack))) {
				queuedDataForUnroutedItems.remove(item);
				return item;
			}
		}
		return null;
	}
	
	/* --- Trigger --- */
	@Override
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> actions = super.getActions();
		actions.add(BuildCraftProxy.LogisticsDisableAction);
		return actions;
	}
	
	@Override
	protected void actionsActivated(HashMap<Integer, Boolean> actions) {
		super.actionsActivated(actions);

		setEnabled(true);
		// Activate the actions
		for (Entry<Integer, Boolean> i : actions.entrySet()) {
			if (i.getValue()) {
				if (ActionManager.actions[i.getKey()] instanceof ActionDisableLogistics){
					setEnabled(false);
				}
			}
		}
	}
	
	/* --- CCCommands --- */
	@CCCommand(description="Returns the Router UUID as an integer; all pipes have a unique ID")
	public int getRouterId() {
		return getRouter().getSimpleID();
	}

	@CCCommand(description="Sets the TurtleConnect flag for this Turtle on this LogisticsPipe")
	public void setTurtleConnect(Boolean flag) {
		if(this.container instanceof LogisticsTileGenericPipe) {
			((LogisticsTileGenericPipe)this.container).setTurtrleConnect(flag);
		}
	}

	@CCCommand(description="Returns the TurtleConnect flag for this Turtle on this LogisticsPipe")
	public boolean getTurtleConnect() {
		if(this.container instanceof LogisticsTileGenericPipe) {
			return ((LogisticsTileGenericPipe)this.container).getTurtrleConnect();
		}
		return false;
	}

	@CCCommand(description="Returns the Item Id for given ItemIdentifier Id.")
	public int getItemID(Double itemId) throws Exception {
		ItemIdentifier item = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(item == null) throw new Exception("Invalid ItemIdentifierID");
		return item.itemID;
	}

	@CCCommand(description="Returns the Item damage for the given ItemIdentifier Id.")
	public int getItemDamage(Double itemId) throws Exception {
		ItemIdentifier itemd = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(itemd == null) throw new Exception("Invalid ItemIdentifierID");
		return itemd.itemDamage;
	}

	@CCCommand(description="Returns the NBTTagCompound for the given ItemIdentifier Id.")
	public Map<Object,Object> getNBTTagCompound(Double itemId) throws Exception {
		ItemIdentifier itemn = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(itemn == null) throw new Exception("Invalid ItemIdentifierID");
		return itemn.getNBTTagCompoundAsMap();
	}

	@CCCommand(description="Returns the ItemIdentifier Id for the given Item id and damage.")
	public int getItemIdentifierIDFor(Double itemID, Double itemDamage) {
		return ItemIdentifier.get((int)Math.floor(itemID), (int)Math.floor(itemDamage), null).getId();
	}

	@CCCommand(description="Returns the name of the item for the given ItemIdentifier Id.")
	public String getItemName(Double itemId) throws Exception {
		ItemIdentifier itemd = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(itemd == null) throw new Exception("Invalid ItemIdentifierID");
		return itemd.getFriendlyNameCC();
	}

	/** used as a distance offset when deciding which pipe to use
	 * NOTE: called very regularly, returning a pre-calculated int is probably appropriate.
	 * @return
	 */
	public double getLoadFactor() {
		return 0.0;
	}

	public void notifyOfItemArival(RoutedEntityItem routedEntityItem) {
		this._inTransitToMe.remove(routedEntityItem);		
	}

	public int countOnRoute(ItemIdentifier it) {
		int count = 0;
		for(Iterator<IRoutedItem> iter = _inTransitToMe.iterator();iter.hasNext();) {
			IRoutedItem next = iter.next();
			if(next.getIDStack().getItem() == it)
				count += next.getIDStack().stackSize;
		}
		return count;
	}
}
