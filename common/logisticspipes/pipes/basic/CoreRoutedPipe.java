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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.gates.ActionDisableLogistics;
import logisticspipes.interfaces.ISecurityProvider;
import logisticspipes.interfaces.IWatchingHandler;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logisticspipes.IAdjacentWorldAccess;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.ITrackStatistics;
import logisticspipes.logisticspipes.PipeTransportLayer;
import logisticspipes.logisticspipes.RouteLayer;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.LogisticsGuiModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.PacketPayload;
import logisticspipes.network.TilePacketWrapper;
import logisticspipes.network.packets.pipe.RequestRoutingLasersPacket;
import logisticspipes.network.packets.pipe.StatUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.renderer.LogisticsHUDRenderer;
import logisticspipes.request.RequestTree.workWeightedSorter;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.routing.ServerRouter;
import logisticspipes.security.PermissionException;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.ticks.WorldTickHandler;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.WorldUtil;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.core.utils.Utils;
import buildcraft.transport.PipeTransport;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@CCType(name = "LogisticsPipes:Normal")
public abstract class CoreRoutedPipe extends Pipe<PipeTransportLogistics> implements IRequestItems, IAdjacentWorldAccess, ITrackStatistics, IWorldProvider, IWatchingHandler, IRoutedPowerProvider {

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
	private Field itemIDAccess;
	private int cachedItemID = -1;
	private boolean blockRemove = false;
	private boolean destroyByPlayer = false;
	
	public long delayTo = 0;
	public int repeatFor = 0;
	
	protected RouteLayer _routeLayer;
	protected TransportLayer _transportLayer;
	protected final PriorityBlockingQueue<IRoutedItem> _inTransitToMe = new PriorityBlockingQueue<IRoutedItem>(10,new IRoutedItem.DelayComparator());
	
	private UpgradeManager upgradeManager = new UpgradeManager(this);
	
	public int stat_session_sent;
	public int stat_session_recieved;
	public int stat_session_relayed;
	
	public long stat_lifetime_sent;
	public long stat_lifetime_recieved;
	public long stat_lifetime_relayed;
	
	public int server_routing_table_size = 0;
	
	protected final LinkedList<Pair3<IRoutedItem, ForgeDirection, ItemSendMode>> _sendQueue = new LinkedList<Pair3<IRoutedItem, ForgeDirection, ItemSendMode>>();
	
	protected final ArrayList<TravelingItem> queuedDataForUnroutedItems = new ArrayList<TravelingItem>();
	
	public final PlayerCollectionList watchers = new PlayerCollectionList();

	protected List<IInventory> _cachedAdjacentInventories;

	//public BaseRoutingLogic logic;
	// from BaseRoutingLogic
	protected int throttleTime = 20;
	private int throttleTimeLeft = 20 + new Random().nextInt(Configs.LOGISTICS_DETECTION_FREQUENCY);
	
	public CoreRoutedPipe(int itemID) {
		this(new PipeTransportLogistics(), itemID);
	}

	public CoreRoutedPipe(PipeTransportLogistics transport, int itemID) {
		super(transport, itemID);
		//this.logic = logic;
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
	
	public PacketPayload getLogisticsNetworkPacket() {
		PacketPayload payload = new TilePacketWrapper(new Class[] { container.getClass(), transport.getClass()}).toPayload(getX(), getY(), getZ(), new Object[] { container, transport });

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
		Position p = new Position(this.getX() + 0.5F, this.getY() + Utils.getPipeFloorOf(routedItem.getItemStack()), this.getZ() + 0.5F, from);
		if(from == ForgeDirection.DOWN) {
			p.moveForwards(0.24F);
		} else if(from == ForgeDirection.UP) {
			p.moveForwards(0.74F);
		} else {
			p.moveForwards(0.49F);
		}
		routedItem.SetPosition(p.x, p.y, p.z);
		((PipeTransportItems) transport).injectItem(routedItem.getTravelingItem(), from.getOpposite());
		
		IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(routedItem.getDestination(),false);
		if(r != null) {
			CoreRoutedPipe pipe = r.getCachedPipe();
			if(pipe !=null) // pipes can unload at inconvenient times ...
				pipe.notifyOfSend(routedItem);
			else {
				//TODO: handle sending items to known chunk-unloaded destination?
			}
		} // should not be able to send to a non-existing router
		//router.startTrackingRoutedItem((RoutedEntityItem) routedItem.getTravelingItem());
		MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, this.getX(), this.getY(), this.getZ(), this.getWorld(), 2);
		stat_lifetime_sent++;
		stat_session_sent++;
		updateStats();
	}
	
	private void notifyOfSend(IRoutedItem routedItem) {
		this._inTransitToMe.add(routedItem);
		//LogisticsPipes.log.info("Sending: "+routedItem.getIDStack().getItem().getFriendlyName());
	}

	public abstract ItemSendMode getItemSendMode();
	
	private boolean checkTileEntity(boolean force) {
		if(getWorld().getWorldTime() % 10 == 0 || force) {
			if(!(this.container instanceof LogisticsTileGenericPipe)) {
				TileEntity tile = getWorld().getBlockTileEntity(getX(), getY(), getZ());
				if(tile != this.container) {
					LogisticsPipes.log.severe("LocalCodeError");
				}
				if(MainProxy.isClient(getWorld())) {
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
		WorldUtil worldUtil = new WorldUtil(this.getWorld(), this.getX(), this.getY(), this.getZ());
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
	 * first tick just create a router and do nothing.
	 */
	public void firstInitialiseTick() {
		getRouter();
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
				getWorld().notifyBlockChange(getX(), getY(), getZ(), getWorld().getBlockId(getX(), getY(), getZ()));
				/* TravelingItems are just held by a pipe, they don't need to know their world
				 * for(Pair3<IRoutedItem, ForgeDirection, ItemSendMode> item : _sendQueue) {
					//assign world to any entityitem we created in readfromnbt
					item.getValue1().getTravelingItem().setWorld(getWorld());
				}*/
				//first tick just create a router and do nothing.
				firstInitialiseTick();
				return;
			}
		}
		if(repeatFor > 0) {
			if(delayTo < System.currentTimeMillis()) {
				delayTo = System.currentTimeMillis() + 200;
				repeatFor--;
				getWorld().markBlockForUpdate(this.getX(), this.getY(), this.getZ());
			}
		}

		// remove old items _inTransit -- these should have arrived, but have probably been lost instead. In either case, it will allow a re-send so that another attempt to re-fill the inventory can be made.		
		while(this._inTransitToMe.peek()!=null && this._inTransitToMe.peek().getTickToTimeOut()<=0){
			final IRoutedItem p=_inTransitToMe.poll();
			if (LogisticsPipes.DEBUG) {
					LogisticsPipes.log.info("Timed Out: "+p.getIDStack().getItem().getFriendlyName());
			}
		}
		//update router before ticking logic/transport
		getRouter().update(getWorld().getWorldTime() % Configs.LOGISTICS_DETECTION_FREQUENCY == _delayOffset || _initialInit);
		getUpgradeManager().securityTick();
		super.updateEntity();
		
		// from BaseRoutingLogic
		if (--throttleTimeLeft <= 0) {
			throttledUpdateEntity();
			throttleTimeLeft = throttleTime;
		}
		
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
		if(MainProxy.isClient(getWorld())) return;
		checkTexturePowered();
		if (!isEnabled()) return;
		enabledUpdateEntity();
		if (getLogisticsModule() == null) return;
		getLogisticsModule().tick();
	}	

	protected void onAllowedRemoval() {}

// From BaseRoutingLogic
	public void throttledUpdateEntity(){}
	
	protected void delayThrottle() {
		//delay 6(+1) ticks to prevent suppliers from ticking between a item arriving at them and the item hitting their adj. inv
		if(throttleTimeLeft < 7)
			throttleTimeLeft = 7;
	}
	
	private void doDebugStuff(EntityPlayer entityplayer) {
		//entityplayer.worldObj.setWorldTime(4951);
		IRouter r = getRouter();
		if(!(r instanceof ServerRouter)) return;
		System.out.println("***");
		System.out.println("---------Interests---------------");
		for(Entry<ItemIdentifier, Set<IRouter>> i: ServerRouter.getInterestedInSpecifics().entrySet()){
			System.out.print(i.getKey().getFriendlyName()+":");
			for(IRouter j:i.getValue())
				System.out.print(j.getSimpleID()+",");
			System.out.println();
		}
		
		System.out.print("ALL ITEMS:");
		for(IRouter j:ServerRouter.getInterestedInGeneral())
			System.out.print(j.getSimpleID()+",");
		System.out.println();
			
		
		
		
		ServerRouter sr = (ServerRouter) r;
		
		System.out.println(r.toString());
		System.out.println("---------CONNECTED TO---------------");
		for (CoreRoutedPipe adj : sr._adjacent.keySet()) {
			System.out.println(adj.getRouter().getSimpleID());
		}
		System.out.println();
		System.out.println("========DISTANCE TABLE==============");
		for(ExitRoute n : r.getIRoutersByCost()) {
			System.out.println(n.destination.getSimpleID()+ " @ " + n.distanceToDestination + " -> "+ n.connectionDetails +"("+n.destination.getId() +")");
		}
		System.out.println();
		System.out.println("*******EXIT ROUTE TABLE*************");
		List<ExitRoute> table = r.getRouteTable();
		for (int i=0; i < table.size(); i++){			
			if(table.get(i)!=null)
			System.out.println(i + " -> " + r.getSimpleID() + " via " + table.get(i).exitOrientation + "(" + table.get(i) + " distance)");
		}
		System.out.println();
		System.out.println("++++++++++CONNECTIONS+++++++++++++++");
		System.out.println(Arrays.toString(ForgeDirection.VALID_DIRECTIONS));
		System.out.println(Arrays.toString(sr.sideDisconnected));
		System.out.println(Arrays.toString(container.pipeConnectionsBuffer));
		System.out.println();
		System.out.println("~~~~~~~~~~~~~~~POWER~~~~~~~~~~~~~~~~");
		System.out.println(r.getPipe().getRoutedPowerProviders());
		System.out.println(r.getPowerProvider());
		refreshConnectionAndRender(true);
	}
// end FromBaseRoutingLogic
	
	@Override
	public final void onBlockRemoval() {
		revertItemID();
		if(canBeDestroyed() || destroyByPlayer) {
			try {
				onAllowedRemoval();
				super.onBlockRemoval();
				//invalidate() removes the router
//				if (logic instanceof BaseRoutingLogic){
//					((BaseRoutingLogic)logic).destroy();
//				}
				//Just in case
				pipecount = Math.max(pipecount - 1, 0);
				
				if (transport != null && transport instanceof PipeTransportLogistics){
					transport.dropBuffer();
				}
				getUpgradeManager().dropUpgrades();
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else if(!blockRemove) {
			final World worldCache = getWorld();
			final int xCache = getX();
			final int yCache = getY();
			final int zCache = getZ();
			final TileEntity tileCache = this.container;
			blockRemove = true;
			QueuedTasks.queueTask(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					tileCache.validate();
					worldCache.setBlock(xCache, yCache, zCache, BuildCraftTransport.genericPipeBlock.blockID);
					worldCache.setBlockTileEntity(xCache, yCache, zCache, tileCache);
					worldCache.notifyBlockChange(xCache, yCache, zCache, BuildCraftTransport.genericPipeBlock.blockID);
					blockRemove = false;
					return null;
				}
			});
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
		if(router != null) {
			router.clearPipeCache();
			router.clearInterests();
		}
	}
	
	@Override
	public void dropContents() {
		if(MainProxy.isClient(getWorld())) return;
		if(canBeDestroyed() || destroyByPlayer) {
			super.dropContents();
		} else {
			if(itemIDAccess == null) {
				try {
					itemIDAccess = Pipe.class.getDeclaredField("itemID");
					itemIDAccess.setAccessible(true);
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
			cachedItemID = itemID;
			try {
				itemIDAccess.setInt(this, LogisticsPipes.LogisticsBrokenItem.itemID);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			final World worldCache = getWorld();
			final int xCache = getX();
			final int yCache = getY();
			final int zCache = getZ();
			final TileEntity tileCache = this.container;
			blockRemove = true;
			QueuedTasks.queueTask(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					revertItemID();
					worldCache.setBlock(xCache, yCache, zCache, BuildCraftTransport.genericPipeBlock.blockID);
					worldCache.setBlockTileEntity(xCache, yCache, zCache, tileCache);
					worldCache.notifyBlockChange(xCache, yCache, zCache, BuildCraftTransport.genericPipeBlock.blockID);
					blockRemove = false;
					return null;
				}
			});
		}
	}

	private void revertItemID() {
		if(cachedItemID != -1) {
			try {
				itemIDAccess.setInt(this, cachedItemID);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			cachedItemID = -1;
		}
	}

	public void checkTexturePowered() {
		if(Configs.LOGISTICS_POWER_USAGE_DISABLED) return;
		if(getWorld().getWorldTime() % 10 != 0) return;
		if(stillNeedReplace || _initialInit || router == null) return;
		boolean flag;
		if((flag = canUseEnergy(1)) != _textureBufferPowered) {
			_textureBufferPowered = flag;
			refreshRender(false);
			MainProxy.sendSpawnParticlePacket(Particles.RedParticle, this.getX(), this.getY(), this.getZ(), this.getWorld(), 3);
		}
	}
	
	
	public abstract TextureType getCenterTexture();
	
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
			p.getValue1().getTravelingItem().writeToNBT(tagentityitem);
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
			TravelingItem entity = new TravelingItem();
			entity.readFromNBT(tagentityitem);
			IRoutedItem routeditem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(entity);
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
				router = SimpleServiceLocator.routerManager.getOrCreateRouter(routerIntId, MainProxy.getDimensionForWorld(getWorld()), getX(), getY(), getZ(), false);
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
		if(!stillNeedReplace && MainProxy.isServer(getWorld())) {
			onNeighborBlockChange_Logistics();
		}
	}

	public void onNeighborBlockChange_Logistics(){}
	
	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();
	}
	
	public abstract LogisticsModule getLogisticsModule();
	
	@Override
	public final boolean blockActivated(EntityPlayer entityplayer) {
		
		
		SecuritySettings settings = null;
		if(MainProxy.isServer(entityplayer.worldObj)) {
			LogisticsSecurityTileEntity station = SimpleServiceLocator.securityStationManager.getStation(getUpgradeManager().getSecurityID());
			// Logic had false
			if(station != null) {
				settings = station.getSecuritySettingsForPlayer(entityplayer, true);
			}
		}
		
				if(handleClick(entityplayer, settings)) return true;
		if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			if(wrenchClicked(entityplayer, settings)) {
				return true;
			}
		}
		if(SimpleServiceLocator.buildCraftProxy.isUpgradeManagerEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			if(MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openUpgrades) {
					getUpgradeManager().openGui(entityplayer, this);
				} else {
					entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
				}
			}
			return true;
		}
		if(!(entityplayer.isSneaking()) && getUpgradeManager().tryIserting(getWorld(), entityplayer)) {
			return true;
		}
		//TODO: simplify any duplicate logic from above
		// from logic
		if (entityplayer.getCurrentEquippedItem() == null) {
			if (!entityplayer.isSneaking()) return false;
			if(MainProxy.isClient(entityplayer.worldObj)) {
				if(!LogisticsHUDRenderer.instance().hasLasers()) { //TODO remove old Lasers
					MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestRoutingLasersPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
				} else {
					LogisticsHUDRenderer.instance().resetLasers();
				}
			}
			if (LogisticsPipes.DEBUG) {
				doDebugStuff(entityplayer);
			}
			return true;
		} else if (entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsNetworkMonitior && (settings == null || settings.openNetworkMonitor)) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_RoutingStats_ID, getWorld(), getX(), getY(), getZ());
			}
			return true;
		} else if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer) && (settings == null || settings.openGui)) {
			onWrenchClicked(entityplayer);
			return true;
		} else if (entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer && (settings == null || settings.openRequest)) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, getWorld(), getX(), getY(), getZ());
			}
			return true;
		} else if(entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
			}
			return true;
		} else if(entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsNetworkMonitior) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
			}
			return true;
		}
		return super.blockActivated(entityplayer);
	}
	
	protected boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		return false;
	}
	
	protected boolean wrenchClicked(EntityPlayer entityplayer, SecuritySettings settings) {
		if (getLogisticsModule() != null && getLogisticsModule() instanceof LogisticsGuiModule) {
			if(MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openGui) {
					entityplayer.openGui(LogisticsPipes.instance, ((LogisticsGuiModule)getLogisticsModule()).getGuiHandlerID(), getWorld(), getX(), getY(), getZ());
				} else {
					entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
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
			MainProxy.sendSpawnParticlePacket(Particles.GreenParticle, this.getX(), this.getY(), this.getZ(), this.getWorld(), 3);
		}
	}
	
	public void refreshConnectionAndRender(boolean spawnPart) {
		clearCache();
		this.container.scheduleNeighborChange();
		if (spawnPart) {
			MainProxy.sendSpawnParticlePacket(Particles.GreenParticle, this.getX(), this.getY(), this.getZ(), this.getWorld(), 3);
		}
	}
	
	/***  --  IAdjacentWorldAccess  --  ***/
	
	@Override
	public LinkedList<AdjacentTile> getConnectedEntities() {
		WorldUtil world = new WorldUtil(this.getWorld(), this.getX(), this.getY(), this.getZ());
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
		return getWorld().rand.nextInt(maxSize);
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
		return container.getWorld();
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 0) {
			watchers.add(player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(StatUpdate.class).setPipe(this), (Player)player);
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
			MainProxy.sendToPlayerList(PacketHandler.getPacket(StatUpdate.class).setPipe(this), watchers);
		}
	}
	
	@Override
	public void itemCouldNotBeSend(ItemIdentifierStack item) {
		if(this instanceof IRequireReliableTransport) {
			((IRequireReliableTransport)this).itemLost(item);
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
	public final boolean canPipeConnect(TileEntity tile, ForgeDirection dir) {
		return canPipeConnect(tile, dir, false);
	}
	
	public boolean globalIgnoreConnectionDisconnection = false;
	
	public final boolean canPipeConnect(TileEntity tile, ForgeDirection dir, boolean ignoreSystemDisconnection) {
		ForgeDirection side = OrientationsUtil.getOrientationOfTilewithPipe(this.transport, tile);
		if(getUpgradeManager().isSideDisconnected(side)) {
			return false;
		}
		if(!stillNeedReplace) {
			if(getRouter().isSideDisconneceted(side) && !ignoreSystemDisconnection && !globalIgnoreConnectionDisconnection) {
				return false;
			}
		}
		return (super.canPipeConnect(tile, dir) || logisitcsIsPipeConnected(tile)) && !disconnectPipe(tile, dir);
	}
	
	public void connectionUpdate() {
		if(container != null && !stillNeedReplace) {
			container.scheduleNeighborChange();
			getWorld().notifyBlockChange(getX(), getY(), getZ(), getWorld().getBlockId(getX(), getY(), getZ()));
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
		if(MainProxy.isClient(getWorld())) {
			return null;
		}
		if(stillNeedReplace) {
			return null;
		}
		List<ILogisticsPowerProvider> list = new ArrayList<ILogisticsPowerProvider>(this.getRouter().getPowerProvider());
		addAll(list, getRoutedPowerProviders(this.getRouter(), new BitSet(ServerRouter.getBiggestSimpleID()+1), new ArrayList<IFilter>()));
		return list;
	}
	
	private List<ILogisticsPowerProvider> getRoutedPowerProviders(IRouter router, BitSet layer, List<IFilter> filters) {
		List<ILogisticsPowerProvider> power = new LinkedList<ILogisticsPowerProvider>();
		List<IRouter> routersIndex = router.getFilteringRouter();
		List<ExitRoute> validSources = new ArrayList<ExitRoute>(); // get the routing table 
		for (IRouter r:routersIndex) {
			ExitRoute e = router.getDistanceTo(r);
			if (e!=null) {
				validSources.add(e);
			}
		}
		Collections.sort(validSources, new workWeightedSorter(1.0));
		List<ExitRoute> firewalls = new LinkedList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
		for(ExitRoute r : validSources) {
			if(r.containsFlag(PipeRoutingConnectionType.canPowerFrom) && !used.get(r.destination.getSimpleID())) {
				if(r.destination instanceof IFilteringRouter) {
					firewalls.add(r);
					used.set(r.destination.getSimpleID());
				}
			}
		}
		for(ExitRoute r:firewalls) {
			IFilter filter = ((IFilteringRouter)r.destination).getFilter();
			filters.add(filter);
			boolean canPassPower = true;
			for(IFilter f:filters) {
				if(f.blockPower()) canPassPower = false;
			}
			IRouter center = r.destination.getRouter(ForgeDirection.UNKNOWN);
			if(center != null) {
				if(canPassPower) {
					addAll(power, center.getPowerProvider());
				}
				List<ILogisticsPowerProvider> list = getRoutedPowerProviders(center, used, filters);
				addAll(power, list);
			}
			filters.remove(filter);
		}
		return power;
	}
	
	private <T> void addAll(List<T> list, List<T> add) {
		for(T o:add) {
			if(!list.contains(o)) {
				list.add(o);
			}
		}
	}
	
	@Override
	public boolean useEnergy(int amount){
		return useEnergy(amount, null, true);
	}
	@Override
	public boolean canUseEnergy(int amount){
		return canUseEnergy(amount,null);
	}

	@Override
	public boolean canUseEnergy(int amount, List<Object> providersToIgnore) {
		if(MainProxy.isClient(getWorld())) return false;
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
	
	@Override
	public boolean useEnergy(int amount, List<Object> providersToIgnore) {
		return useEnergy(amount, providersToIgnore, false);
	}

	private boolean useEnergy(int amount, List<Object> providersToIgnore, boolean sparkles) {
		if(MainProxy.isClient(getWorld())) return false;
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
				if(sparkles){
					int particlecount = amount;
					if (particlecount > 10) {
						particlecount = 10;
					}
					MainProxy.sendSpawnParticlePacket(Particles.GoldParticle, this.getX(), this.getY(), this.getZ(), this.getWorld(), particlecount);
				}
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
	
	public boolean canBeDestroyedByPlayer(EntityPlayer entityPlayer) {
		LogisticsSecurityTileEntity station = SimpleServiceLocator.securityStationManager.getStation(getUpgradeManager().getSecurityID());
		if(station != null) {
			return station.getSecuritySettingsForPlayer(entityPlayer, true).removePipes;
		}
		return true;
	}
	
	public boolean canBeDestroyed() {
		ISecurityProvider sec = getSecurityProvider();
		if(sec != null) {
			if(!sec.canAutomatedDestroy()) {
				return false;
			}
		}
		return true;
	}

	public void setDestroyByPlayer() {
		destroyByPlayer = true;
	}
	
	public boolean blockRemove() {
		return blockRemove;
	}
	
	public void checkCCAccess() throws PermissionException {
		ISecurityProvider sec = getSecurityProvider();
		if(sec != null) {
			int id = -1;
			if(this.container instanceof LogisticsTileGenericPipe) {
				id = ((LogisticsTileGenericPipe)this.container).getLastCCID();
			}
			if(!sec.getAllowCC(id)) {
				throw new PermissionException();
			}
		}
	}

	public void queueUnroutedItemInformation(TravelingItem data) {
		if(data != null && data.getItemStack() != null) {
			data.setItemStack(data.getItemStack().copy());
			queuedDataForUnroutedItems.add(data);
		}
	}
	
	public TravelingItem getQueuedForItemStack(ItemStack stack) {
		for(TravelingItem item:queuedDataForUnroutedItems) {
			if(ItemIdentifierStack.GetFromStack(item.getItemStack()).equals(ItemIdentifierStack.GetFromStack(stack))) {
				queuedDataForUnroutedItems.remove(item);
				return item;
			}
		}
		return null;
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
		//LogisticsPipes.log.info("Ariving: "+routedEntityItem.getIDStack().getItem().getFriendlyName());
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return Textures.LPpipeIconProvider;
	}
	
	@Override
	public final int getIconIndex(ForgeDirection connection) {
		TextureType texture = getTextureType(connection);
		if(_textureBufferPowered) {
			return texture.powered;
		} else if(Configs.LOGISTICS_POWER_USAGE_DISABLED) {
			return texture.normal;
		} else {
			return texture.unpowered;
		}
	}

	@Override
	public final int getX() {
		//TODO: what if container is null; pipes don't have a coord any more.
/*		if(this.container == null) {
			return getX();
		}*/
		return this.container.xCoord;
	}

	@Override
	public final int getY() {
		/*
		if(this.container == null) {
			return getY();
		}*/
		return this.container.yCoord;
	}

	@Override
	public final int getZ() {
		/*
		if(this.container == null) {
			return getZ();
		}
		*/
		return this.container.zCoord;
	}

	public void addCrashReport(CrashReportCategory crashReportCategory) {
		addRouterCrashReport(crashReportCategory);
		crashReportCategory.addCrashSection("stillNeedReplace", stillNeedReplace);
	}
	
	protected void addRouterCrashReport(CrashReportCategory crashReportCategory) {
		crashReportCategory.addCrashSection("Router", this.getRouter().toString());
	}
	
	public boolean isFluidPipe() {
		return false;
	}
	
	/* --- Trigger --- */
	@Override
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> actions = super.getActions();
		actions.add(BuildCraftProxy.LogisticsDisableAction);
		return actions;
	}
	
	@Override
	protected void actionsActivated(Map<IAction, Boolean> actions) {
		super.actionsActivated(actions);

		setEnabled(true);
		// Activate the actions
		for (Entry<IAction, Boolean> i : actions.entrySet()) {
			if (i.getValue()) {
				if (i.getKey() instanceof ActionDisableLogistics){
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
			((LogisticsTileGenericPipe)this.container).setTurtleConnect(flag);
		}
	}

	@CCCommand(description="Returns the TurtleConnect flag for this Turtle on this LogisticsPipe")
	public boolean getTurtleConnect() {
		if(this.container instanceof LogisticsTileGenericPipe) {
			return ((LogisticsTileGenericPipe)this.container).getTurtleConnect();
		}
		return false;
	}

	@CCCommand(description="Returns the Item Id for given ItemIdentifier Id.")
	public int getItemID(Double itemId) throws Exception {
		if(itemId == null) throw new Exception("Invalid ItemIdentifierID");
		ItemIdentifier item = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(item == null) throw new Exception("Invalid ItemIdentifierID");
		return item.itemID;
	}

	@CCCommand(description="Returns the Item damage for the given ItemIdentifier Id.")
	public int getItemDamage(Double itemId) throws Exception {
		if(itemId == null) throw new Exception("Invalid ItemIdentifierID");
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
	public String getUnlocalizedName(Double itemId) throws Exception {
		if(itemId == null) throw new Exception("Invalid ItemIdentifierID");
		ItemIdentifier itemd = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(itemd == null) throw new Exception("Invalid ItemIdentifierID");
		return itemd.getFriendlyNameCC();
	}

	@CCCommand(description="Returns true if the computer is allowed to interact with the connected pipe.", needPermission=false)
	public boolean canAccess() {
		ISecurityProvider sec = getSecurityProvider();
		if(sec != null) {
			int id = -1;
			if(this.container instanceof LogisticsTileGenericPipe) {
				id = ((LogisticsTileGenericPipe)this.container).getLastCCID();
			}
			return sec.getAllowCC(id);
		}
		return true;
	}
	
	// from logic
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if (MainProxy.isServer(entityplayer.worldObj)) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Freq_Card_ID, getWorld(), getX(), getY(), getZ());
		}
	}
	
	final void destroy(){ // no overide, put code in OnBlockRemoval
	
	}
	
}
