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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.gui.GuiChassiPipe;
import logisticspipes.gui.hud.HUDChassiePipe;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.ILegacyActiveModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendQueueContentRecieiver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.items.ItemModule;
import logisticspipes.logic.BaseChassiLogic;
import logisticspipes.logisticspipes.ChassiModule;
import logisticspipes.logisticspipes.ChassiTransportLayer;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.network.packets.PacketPipeUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.ticks.HudUpdateTick;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Position;
import buildcraft.core.DefaultProps;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;

public abstract class PipeLogisticsChassi extends CoreRoutedPipe implements ISimpleInventoryEventHandler, IInventoryProvider, ISendRoutedItem, IProvideItems, IWorldProvider, IHeadUpDisplayRendererProvider, ISendQueueContentRecieiver {

	private final ChassiModule _module;
	private final SimpleInventory _moduleInventory;
	private boolean switchOrientationOnTick = true;
	private boolean init = false;
	BaseChassiLogic ChassiLogic;
	private boolean convertFromMeta = false;

	//HUD
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	private HUDChassiePipe HUD;

	public PipeLogisticsChassi(int itemID) {
		super(new BaseChassiLogic(), itemID);
		ChassiLogic = (BaseChassiLogic) logic;
		_moduleInventory = new SimpleInventory(getChassiSize(), "Chassi pipe", 1);
		_moduleInventory.addListener(this);
		_module = new ChassiModule(getChassiSize(), this);
		HUD = new HUDChassiePipe(this, _module, _moduleInventory);
	}

	@Override
	protected List<IInventory> getConnectedRawInventories()	{
		if(_cachedAdjacentInventories != null) {
			return _cachedAdjacentInventories;
		}
		List<IInventory> adjacent = new ArrayList<IInventory>(1);
		IInventory adjinv = getRawInventory();
		if(adjinv != null) {
			adjacent.add(adjinv);
		}
		_cachedAdjacentInventories = adjacent;
		return _cachedAdjacentInventories;
	}	
	
	public ForgeDirection getPointedOrientation(){
		return ChassiLogic.orientation;
	}

	public TileEntity getPointedTileEntity(){
		if(ChassiLogic.orientation == ForgeDirection.UNKNOWN) return null;
		if(this.container.tileBuffer == null) {
			return null;
		}
		return this.container.tileBuffer[ChassiLogic.orientation.ordinal()].getTile();
	}

	public void nextOrientation() {
		boolean found = false;
		ForgeDirection oldOrientation = ChassiLogic.orientation;
		for (int l = 0; l < 6; ++l) {
			ChassiLogic.orientation = ForgeDirection.values()[(ChassiLogic.orientation.ordinal() + 1) % 6];
			if(isValidOrientation(ChassiLogic.orientation)) {
				found = true;
				break;
			}
		}
		if (!found) {
			ChassiLogic.orientation = ForgeDirection.UNKNOWN;
		}
		if(ChassiLogic.orientation != oldOrientation) {
			clearCache();
			MainProxy.sendPacketToAllAround(xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE, MainProxy.getDimensionForWorld(worldObj), new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE,xCoord,yCoord,zCoord,getLogisticsNetworkPacket()).getPacket());
			refreshRender(true);
		}
	}

	private boolean isValidOrientation(ForgeDirection connection){
		if (connection == ForgeDirection.UNKNOWN) return false;
		if (getRouter().isRoutedExit(connection)) return false;
		Position pos = new Position(xCoord, yCoord, zCoord, connection);
		pos.moveForwards(1.0);
		TileEntity tile = worldObj.getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);

		if (tile == null) return false;
		if (tile instanceof TileGenericPipe) return false;
		return SimpleServiceLocator.buildCraftProxy.checkPipesConnections(this.container, tile, connection);
	}

	public IInventory getModuleInventory(){
		return this._moduleInventory;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_TEXTURE;
	}

	@Override
	public TextureType getRoutedTexture(ForgeDirection connection) {
		return Textures.LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE;
	}

	@Override
	public TextureType getNonRoutedTexture(ForgeDirection connection) {
		if (connection.equals(ChassiLogic.orientation)){
			return Textures.LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE;
	}

	@Override
	public void onNeighborBlockChange_Logistics() {
		if (!isValidOrientation(ChassiLogic.orientation)){
			if(MainProxy.isServer(this.worldObj)) {
				nextOrientation();
			}
		}
	};

	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();
		switchOrientationOnTick = true;
	}


	/*** IInventoryProvider ***/

	@Override
	public IInventory getRawInventory() {
		TileEntity tile = getPointedTileEntity();
		if (tile == null ) return null;
		if (tile instanceof TileGenericPipe) return null;
		if (!(tile instanceof IInventory)) return null;
		return InventoryHelper.getInventory((IInventory) tile);
	}

	@Override
	public IInventory getPointedInventory() {
		IInventory rawInventory = getRawInventory();
		if (rawInventory instanceof ISidedInventory) return new SidedInventoryAdapter((ISidedInventory) rawInventory, this.getPointedOrientation().getOpposite());
		return rawInventory;
	}

	@Override
	public IInventory getSneakyInventory() {
		UpgradeManager manager = getUpgradeManager();
		ForgeDirection insertion = this.getPointedOrientation().getOpposite();
		if(manager.hasSneakyUpgrade()) {
			insertion = manager.getSneakyOrientation();
		}
		return getSneakyInventory(insertion);
	}
	
	@Override
	public IInventory getSneakyInventory(ForgeDirection insertion) {
		IInventory rawInventory = getRawInventory();
		if (rawInventory instanceof ISidedInventory) return new SidedInventoryAdapter((ISidedInventory) rawInventory, insertion);
		return rawInventory;
	}

	@Override
	public ForgeDirection inventoryOrientation() {
		return getPointedOrientation();
	}

	/*** ISendRoutedItem ***/

	public int getSourceint() {
		return this.getRouter().getSimpleID();
	};

	@Override
	public Pair3<Integer, SinkReply, List<IFilter>> hasDestination(ItemIdentifier stack, boolean allowDefault, List<Integer> routerIDsToExclude) {
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, allowDefault, getRouter().getSimpleID(), routerIDsToExclude);
	}

	@Override
	public void sendStack(ItemStack stack, Pair3<Integer, SinkReply, List<IFilter>> reply, ItemSendMode mode) {
		IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stack, this.worldObj);
		itemToSend.setDestination(reply.getValue1());
		if (reply.getValue2().isPassive){
			if (reply.getValue2().isDefault){
				itemToSend.setTransportMode(TransportMode.Default);
			} else {
				itemToSend.setTransportMode(TransportMode.Passive);
			}
		}
		List<IRelayItem> list = new LinkedList<IRelayItem>();
		if(reply.getValue3() != null) {
			for(IFilter filter:reply.getValue3()) {
				list.add(filter);
			}
		}
		itemToSend.addRelayPoints(list);
		super.queueRoutedItem(itemToSend, getPointedOrientation(), mode);
	}

	@Override
	public void sendStack(ItemStack stack, int destination, ItemSendMode mode, List<IRelayItem> relays) {
		IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stack, this.worldObj);
		itemToSend.setDestination(destination);
		itemToSend.setTransportMode(TransportMode.Active);
		itemToSend.addRelayPoints(relays);
		super.queueRoutedItem(itemToSend, getPointedOrientation(), mode);
	}


	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		try {
			super.readFromNBT(nbttagcompound);
			_moduleInventory.readFromNBT(nbttagcompound, "chassi");
			InventoryChanged(_moduleInventory);
			_module.readFromNBT(nbttagcompound);
			ChassiLogic.orientation = ForgeDirection.values()[nbttagcompound.getInteger("Orientation") % 7];
			if(nbttagcompound.getInteger("Orientation") == 0) {
				convertFromMeta = true;
			}
			switchOrientationOnTick = (ChassiLogic.orientation == ForgeDirection.UNKNOWN);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		_moduleInventory.writeToNBT(nbttagcompound, "chassi");
		_module.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("Orientation", ChassiLogic.orientation.ordinal());
	}

	@Override
	public void onBlockRemoval() {
		super.onBlockRemoval();
		_moduleInventory.removeListener(this);
		if(MainProxy.isServer(this.worldObj)) {
			for (int i = 0; i < this.getChassiSize(); i++){
				ILogisticsModule x = _module.getSubModule(i);
				if (x instanceof ILegacyActiveModule) {
					ILegacyActiveModule y = (ILegacyActiveModule)x;
					y.onBlockRemoval();
				}
			}
			for(int i=0;i<_moduleInventory.getSizeInventory();i++) {
				if(_moduleInventory.getStackInSlot(i) != null) {
					ItemModuleInformationManager.saveInfotmation(_moduleInventory.getStackInSlot(i), this.getLogisticsModule().getSubModule(i));
				}
			}
			_moduleInventory.dropContents(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		}
	}

	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		boolean reInitGui = false;
		for (int i = 0; i < inventory.getSizeInventory(); i++){
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null){
				if (_module.hasModule(i)){
					_module.removeModule(i);
					reInitGui = true;
				}
				continue;
			}

			if (stack.getItem() instanceof ItemModule){
				ILogisticsModule current = _module.getModule(i);
				ILogisticsModule next = ((ItemModule)stack.getItem()).getModuleForItem(stack, _module.getModule(i), this, this, this, this);
				next.registerPosition(xCoord, yCoord, zCoord, i);
				if (current != next){
					_module.installModule(i, next);
					if(!MainProxy.isClient()) {
						ItemModuleInformationManager.readInformation(stack, next);
					}
					ItemModuleInformationManager.removeInformation(stack);
				}
			}
		}
		if (reInitGui) {
			if(MainProxy.isClient(this.worldObj)) {
				if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiChassiPipe){
					FMLClientHandler.instance().getClient().currentScreen.initGui();
				}
			}
		}
		if(MainProxy.isServer()) {
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.CHASSIE_PIPE_MODULE_CONTENT, xCoord, yCoord, zCoord, ItemIdentifierStack.getListFromInventory(_moduleInventory)).getPacket(), localModeWatchers);
			//register earlier provider modules with later ones, needed for the "who is the first whose filter allows that item" check
			List<ILegacyActiveModule> prevModules = new LinkedList<ILegacyActiveModule>();
			for (int i = 0; i < this.getChassiSize(); i++){
				ILogisticsModule x = _module.getSubModule(i);
				if (x instanceof ILegacyActiveModule) {
					ILegacyActiveModule y = (ILegacyActiveModule)x;
					y.registerPreviousLegacyModules(new ArrayList(prevModules));
					prevModules.add(y);
				}
			}
		}
	}

	@Override
	public void ignoreDisableUpdateEntity() {
		if (switchOrientationOnTick){
			switchOrientationOnTick = false;
			if(MainProxy.isServer(this.worldObj)) {
				nextOrientation();
			}
		}
		if(convertFromMeta && worldObj.getBlockMetadata(xCoord, yCoord, zCoord) != 0) {
			ChassiLogic.orientation = ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord) % 6];
			worldObj.setBlockMetadata(xCoord, yCoord, zCoord, 0);
			convertFromMeta=false;
		}
		if(!init) {
			init = true;
			if(MainProxy.isClient(this.worldObj)) {
				MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.REQUEST_PIPE_UPDATE, xCoord, yCoord, zCoord).getPacket());
			}
		}
	}

	public abstract int getChassiSize();

	@Override
	public final ILogisticsModule getLogisticsModule() {
		return _module;
	}

	@Override
	public TransportLayer getTransportLayer() {
		if (this._transportLayer == null){
			_transportLayer = new ChassiTransportLayer(this);
		}
		return _transportLayer;
	}

	private boolean tryInsertingModule(EntityPlayer entityplayer) {
		for(int i=0;i<_moduleInventory.getSizeInventory();i++) {
			ItemStack item = _moduleInventory.getStackInSlot(i);
			if(item == null) {
				_moduleInventory.setInventorySlotContents(i, entityplayer.getCurrentEquippedItem().splitStack(1));
				InventoryChanged(_moduleInventory);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean handleClick(World world, int x, int y, int z, EntityPlayer entityplayer, SecuritySettings settings) {
		if (entityplayer.getCurrentEquippedItem() == null) return false;

		if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer) && entityplayer.isSneaking()) {
			if(MainProxy.isServer(world)) {
				if (settings == null || settings.openGui) {
					((PipeLogisticsChassi)this.container.pipe).nextOrientation();
				} else {
					entityplayer.sendChatToPlayer("Permission denied");
				}
			}
			return true;
		}
		
		if(!entityplayer.isSneaking() && entityplayer.getCurrentEquippedItem().itemID == LogisticsPipes.ModuleItem.itemID && entityplayer.getCurrentEquippedItem().getItemDamage() != ItemModule.BLANK) {
			if(MainProxy.isServer(world)) {
				if (settings == null || settings.openGui) {
					return tryInsertingModule(entityplayer);
				} else {
					entityplayer.sendChatToPlayer("Permission denied");
				}
			}
			return true;
		}

		return false;
	}

	/*** IProvideItems ***/
	@Override
	public void canProvide(RequestTreeNode tree, int donePromisses, List<IFilter> filters) {
		if (!isEnabled()){
			return;
		}
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.isFilteredItem(tree.getStackItem().getUndamaged()) || filter.blockProvider()) return;
		}
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule){
				ILegacyActiveModule y = (ILegacyActiveModule)x;
				if(y.filterAllowsItem(tree.getStackItem())) {
					y.canProvide(tree, donePromisses, filters);
					return;
				}
			}
		}
	}

	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (!isEnabled()){
			return;
		}
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule){
				ILegacyActiveModule y = (ILegacyActiveModule)x;
				if(y.filterAllowsItem(promise.item)) {
					y.fullFill(promise, destination);
					MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, xCoord, yCoord, zCoord, this.worldObj, 2);
					return;
				}
			}
		}
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list, List<IFilter> filter) {
		if (!isEnabled()){
			return;
		}
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule) {
				ILegacyActiveModule y = (ILegacyActiveModule)x;
				y.getAllItems(list, filter);
			}
		}
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public World getWorld() {
		return this.worldObj;
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
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
		HUD.stopWatching();
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.CHASSIE_PIPE_MODULE_CONTENT, xCoord, yCoord, zCoord, ItemIdentifierStack.getListFromInventory(_moduleInventory)).getPacket(), (Player)player);
			MainProxy.sendPacketToPlayer(new PacketPipeInvContent(NetworkConstants.SEND_QUEUE_CONTENT, xCoord, yCoord, zCoord, ItemIdentifierStack.getListSendQueue(_sendQueue)).getPacket(), (Player)player);
		} else {
			super.playerStartWatching(player, mode);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		super.playerStopWatching(player, mode);
		localModeWatchers.remove(player);
	}

	public void handleModuleItemIdentifierList(Collection<ItemIdentifierStack> _allItems) {
		_moduleInventory.handleItemIdentifierList(_allItems);
	}

	public void handleContentItemIdentifierList(Collection<ItemIdentifierStack> _allItems) {
		_moduleInventory.handleItemIdentifierList(_allItems);
	}

	@Override
	public int sendQueueChanged(boolean force) {
		if(MainProxy.isServer(this.worldObj)) {
			if(Configs.MULTI_THREAD_NUMBER > 0 && !force) {
				HudUpdateTick.add(getRouter());
			} else {
				if(localModeWatchers != null && localModeWatchers.size()>0) {
					LinkedList<ItemIdentifierStack> items = ItemIdentifierStack.getListSendQueue(_sendQueue);				
					MainProxy.sendCompressedToPlayerList(new PacketPipeInvContent(NetworkConstants.SEND_QUEUE_CONTENT, xCoord, yCoord, zCoord, items).getPacket(), localModeWatchers);
					return items.size();
				}
			}
		}
		return 0;
	}

	@Override
	public void handleSendQueueItemIdentifierList(Collection<ItemIdentifierStack> _allItems){
		displayList.clear();
		displayList.addAll(_allItems);
	}

	public ChassiModule getModules() {
		return _module;
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		for (int i = 0; i < _moduleInventory.getSizeInventory(); i++){
			ILogisticsModule current = _module.getModule(i);
			if(current != null) {
				current.registerPosition(xCoord, yCoord, zCoord, i);
			}
		}
	}

	@Override
	public int getSourceID() {
		return this.getRouterId();
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		//if we don't have a pointed inventory we can't be interested in anything
		if(getRawInventory() == null) return l1;
		for (int moduleIndex = 0; moduleIndex < this.getChassiSize(); moduleIndex++){
			ILogisticsModule module = _module.getSubModule(moduleIndex);
			if(module!=null && module.interestedInAttachedInventory()) {
				IInventory inv = getRawInventory();
				if (inv instanceof ISidedInventory) {
					inv = new SidedInventoryAdapter((ISidedInventory) inv, ForgeDirection.UNKNOWN);
				}
				Set<ItemIdentifier> items = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv).getItems();
				l1.addAll(items);

				//also add tag-less variants ... we should probably add a module.interestedIgnoringNBT at some point
				for(ItemIdentifier id:items) {
					l1.add(id.getIgnoringNBT());
				}

				boolean modulesInterestedInUndamged=false;
				for (int i = 0; i < this.getChassiSize(); i++) {
					if( _module.getSubModule(moduleIndex).interestedInUndamagedID()){
						modulesInterestedInUndamged=true;
						break;
					}
				}
				if(modulesInterestedInUndamged) {
					for(ItemIdentifier id:items){	
						l1.add(id.getUndamaged());
					}
				}
				break; // no need to check other modules for interest in the inventory, when we know that 1 already is.
			} 
		}
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule module = _module.getSubModule(i);
			if(module!=null) {
				Collection<ItemIdentifier> current = module.getSpecificInterests();
				if(current!=null)
					l1.addAll(current);
			}
		}
		return l1;
	}

	@Override
	public boolean hasGenericInterests() {
		if(getRawInventory() == null) return false;
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule x = _module.getSubModule(i);
			
			if(x!=null && x.hasGenericInterests())
				return true;			
		}
		return false;
	}
}
