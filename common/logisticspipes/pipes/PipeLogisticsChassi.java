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

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.gui.GuiChassiPipe;
import logisticspipes.gui.hud.HudChassisPipe;
import logisticspipes.interfaces.IBufferItems;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILegacyActiveModule;
import logisticspipes.interfaces.ISendQueueContentRecieiver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ChassiTransportLayer;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.ChassiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.pipe.ChassiOrientationPacket;
import logisticspipes.network.packets.pipe.ChassiePipeModuleContent;
import logisticspipes.network.packets.pipe.RequestChassiOrientationPacket;
import logisticspipes.network.packets.pipe.SendQueueContent;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.upgrades.ModuleUpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.request.ICraftingTemplate;
import logisticspipes.request.IPromise;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.routing.order.LogisticsItemOrder;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.ticks.HudUpdateTick;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.client.FMLClientHandler;

import lombok.Getter;

@CCType(name = "LogisticsChassiePipe")
public abstract class PipeLogisticsChassi extends CoreRoutedPipe implements ICraftItems, IBufferItems, ISimpleInventoryEventHandler, ISendRoutedItem, IProvideItems, IHeadUpDisplayRendererProvider, ISendQueueContentRecieiver {

	private final ChassiModule _module;
	private final ItemIdentifierInventory _moduleInventory;
	private final ModuleUpgradeManager[] _upgradeManagers;
	private boolean switchOrientationOnTick = true;
	private boolean init = false;

	private boolean convertFromMeta = false;

	// HUD
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private HudChassisPipe hud;

	public PipeLogisticsChassi(Item item) {
		super(item);
		_moduleInventory = new ItemIdentifierInventory(getChassiSize(), "Chassi pipe", 1);
		_moduleInventory.addListener(this);
		_upgradeManagers = new ModuleUpgradeManager[getChassiSize()];
		for (int i = 0; i < getChassiSize(); i++) {
			_upgradeManagers[i] = new ModuleUpgradeManager(this, upgradeManager);
		}
		_module = new ChassiModule(getChassiSize(), this);
		hud = new HudChassisPipe(this, _module, _moduleInventory);
		pointedDirection = ForgeDirection.UNKNOWN;
	}

	@Override
	protected List<IInventory> getConnectedRawInventories() {
		if (_cachedAdjacentInventories != null) {
			return _cachedAdjacentInventories;
		}
		List<IInventory> adjacent = new ArrayList<IInventory>(1);
		IInventory adjinv = getRealInventory();
		if (adjinv != null) {
			adjacent.add(adjinv);
		}
		_cachedAdjacentInventories = adjacent;
		return _cachedAdjacentInventories;
	}

	public void nextOrientation() {
		boolean found = false;
		ForgeDirection oldOrientation = pointedDirection;
		for (int l = 0; l < 6; ++l) {
			pointedDirection = ForgeDirection.values()[(pointedDirection.ordinal() + 1) % 6];
			if (isValidOrientation(pointedDirection)) {
				found = true;
				break;
			}
		}
		if (!found) {
			pointedDirection = ForgeDirection.UNKNOWN;
		}
		if (pointedDirection != oldOrientation) {
			clearCache();
			MainProxy.sendPacketToAllWatchingChunk(getX(), getZ(), MainProxy.getDimensionForWorld(getWorld()), PacketHandler.getPacket(ChassiOrientationPacket.class).setDir(pointedDirection).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
			refreshRender(true);
		}
	}

	public void setClientOrientation(ForgeDirection dir) {
		if (MainProxy.isClient(getWorld())) {
			pointedDirection = dir;
		}
	}

	private boolean isValidOrientation(ForgeDirection connection) {
		if (connection == ForgeDirection.UNKNOWN) {
			return false;
		}
		if (getRouter().isRoutedExit(connection)) {
			return false;
		}
		LPPosition pos = new LPPosition(getX(), getY(), getZ());
		pos.moveForward(connection);
		TileEntity tile = pos.getTileEntity(getWorld());

		if (tile == null) {
			return false;
		}
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			return false;
		}
		return MainProxy.checkPipesConnections(container, tile, connection);
	}

	public IInventory getModuleInventory() {
		return _moduleInventory;
	}

	public ModuleUpgradeManager getModuleUpgradeManager(int slot) {
		return _upgradeManagers[slot];
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_TEXTURE;
	}

	@Override
	public TextureType getRoutedTexture(ForgeDirection connection) {
		if (getRouter().isSubPoweredExit(connection)) {
			return Textures.LOGISTICSPIPE_SUBPOWER_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE;
	}

	@Override
	public TextureType getNonRoutedTexture(ForgeDirection connection) {
		if (connection.equals(pointedDirection)) {
			return Textures.LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE;
		}
		if (isPowerProvider(connection)) {
			return Textures.LOGISTICSPIPE_POWERED_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE;
	}

	@Override
	public void onNeighborBlockChange_Logistics() {
		if (!isValidOrientation(pointedDirection)) {
			if (MainProxy.isServer(getWorld())) {
				nextOrientation();
			}
		}
	};

	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();
		switchOrientationOnTick = true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		try {
			super.readFromNBT(nbttagcompound);
			_moduleInventory.readFromNBT(nbttagcompound, "chassi");
			InventoryChanged(_moduleInventory);
			_module.readFromNBT(nbttagcompound);
			pointedDirection = ForgeDirection.values()[nbttagcompound.getInteger("Orientation") % 7];
			if (nbttagcompound.getInteger("Orientation") == 0) {
				convertFromMeta = true;
			}
			switchOrientationOnTick = (pointedDirection == ForgeDirection.UNKNOWN);
			for (int i = 0; i < getChassiSize(); i++) {
				_upgradeManagers[i].readFromNBT(nbttagcompound, Integer.toString(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		_moduleInventory.writeToNBT(nbttagcompound, "chassi");
		_module.writeToNBT(nbttagcompound);
		if (pointedDirection == null) {
			pointedDirection = ForgeDirection.UNKNOWN;
		}
		nbttagcompound.setInteger("Orientation", pointedDirection.ordinal());
		for (int i = 0; i < getChassiSize(); i++) {
			_upgradeManagers[i].writeToNBT(nbttagcompound, Integer.toString(i));
		}
	}

	@Override
	public void onAllowedRemoval() {
		_moduleInventory.removeListener(this);
		if (MainProxy.isServer(getWorld())) {
			for (int i = 0; i < getChassiSize(); i++) {
				LogisticsModule x = _module.getSubModule(i);
				if (x instanceof ILegacyActiveModule) {
					ILegacyActiveModule y = (ILegacyActiveModule) x;
					y.onBlockRemoval();
				}
			}
			for (int i = 0; i < _moduleInventory.getSizeInventory(); i++) {
				ItemIdentifierStack ms = _moduleInventory.getIDStackInSlot(i);
				if (ms != null) {
					ItemStack s = ms.makeNormalStack();
					ItemModuleInformationManager.saveInfotmation(s, getLogisticsModule().getSubModule(i));
					_moduleInventory.setInventorySlotContents(i, s);
				}
			}
			_moduleInventory.dropContents(getWorld(), getX(), getY(), getZ());
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		if (MainProxy.isServer(getWorld())) {
			if (info instanceof ChassiTargetInformation) {
				ChassiTargetInformation target = (ChassiTargetInformation) info;
				LogisticsModule module = _module.getSubModule(target.moduleSlot);
				if (module instanceof IRequireReliableTransport) {
					((IRequireReliableTransport) module).itemArrived(item, info);
				}
			} else {
				if (LPConstants.DEBUG && info != null) {
					System.out.println(item);
					new RuntimeException("[ItemArrived] Information weren't ment for a chassi pipe").printStackTrace();
				}
			}
		}
	}

	@Override
	public void itemLost(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		if (MainProxy.isServer(getWorld())) {
			if (info instanceof ChassiTargetInformation) {
				ChassiTargetInformation target = (ChassiTargetInformation) info;
				LogisticsModule module = _module.getSubModule(target.moduleSlot);
				if (module instanceof IRequireReliableTransport) {
					((IRequireReliableTransport) module).itemLost(item, info);
				}
			} else {
				if (LPConstants.DEBUG) {
					System.out.println(item);
					new RuntimeException("[ItemLost] Information weren't ment for a chassi pipe").printStackTrace();
				}
			}
		}
	}

	@Override
	public int addToBuffer(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		if (MainProxy.isServer(getWorld())) {
			if (info instanceof ChassiTargetInformation) {
				ChassiTargetInformation target = (ChassiTargetInformation) info;
				LogisticsModule module = _module.getSubModule(target.moduleSlot);
				if (module instanceof IBufferItems) {
					return ((IBufferItems) module).addToBuffer(item, info);
				}
			} else {
				if (LPConstants.DEBUG) {
					System.out.println(item);
					new RuntimeException("[AddToBuffer] Information weren't ment for a chassi pipe").printStackTrace();
				}
			}
		}
		return item.getStackSize();
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		boolean reInitGui = false;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null) {
				if (_module.hasModule(i)) {
					_module.removeModule(i);
					reInitGui = true;
				}
				continue;
			}

			if (stack.getItem() instanceof ItemModule) {
				LogisticsModule current = _module.getModule(i);
				LogisticsModule next = ((ItemModule) stack.getItem()).getModuleForItem(stack, _module.getModule(i), this, this);
				next.registerPosition(ModulePositionType.SLOT, i);
				next.registerCCEventQueuer(this);
				if (current != next) {
					_module.installModule(i, next);
					if (!MainProxy.isClient()) {
						ItemModuleInformationManager.readInformation(stack, next);
					}
					ItemModuleInformationManager.removeInformation(stack);
				}
				inventory.setInventorySlotContents(i, stack);
			}
		}
		if (reInitGui) {
			if (MainProxy.isClient(getWorld())) {
				if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiChassiPipe) {
					FMLClientHandler.instance().getClient().currentScreen.initGui();
				}
			}
		}
		if (MainProxy.isServer()) {
			if (!localModeWatchers.isEmpty()) {
				MainProxy.sendToPlayerList(PacketHandler.getPacket(ChassiePipeModuleContent.class).setIdentList(ItemIdentifierStack.getListFromInventory(_moduleInventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
			}
		}
	}

	@Override
	public void ignoreDisableUpdateEntity() {
		if (switchOrientationOnTick) {
			switchOrientationOnTick = false;
			if (MainProxy.isServer(getWorld())) {
				nextOrientation();
			}
		}
		if (convertFromMeta && getWorld().getBlockMetadata(getX(), getY(), getZ()) != 0) {
			pointedDirection = ForgeDirection.values()[getWorld().getBlockMetadata(getX(), getY(), getZ()) % 6];
			getWorld().setBlockMetadataWithNotify(getX(), getY(), getZ(), 0, 0);
			convertFromMeta = false;
		}
		if (!init) {
			init = true;
			if (MainProxy.isClient(getWorld())) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestChassiOrientationPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
			}
		}
	}

	public abstract int getChassiSize();

	@Override
	public final LogisticsModule getLogisticsModule() {
		return _module;
	}

	@Override
	public TransportLayer getTransportLayer() {
		if (_transportLayer == null) {
			_transportLayer = new ChassiTransportLayer(this);
		}
		return _transportLayer;
	}

	private boolean tryInsertingModule(EntityPlayer entityplayer) {
		for (int i = 0; i < _moduleInventory.getSizeInventory(); i++) {
			ItemStack item = _moduleInventory.getStackInSlot(i);
			if (item == null) {
				_moduleInventory.setInventorySlotContents(i, entityplayer.getCurrentEquippedItem().splitStack(1));
				InventoryChanged(_moduleInventory);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		if (entityplayer.getCurrentEquippedItem() == null) {
			return false;
		}

		if (SimpleServiceLocator.toolWrenchHandler.isWrenchEquipped(entityplayer) && entityplayer.isSneaking() && SimpleServiceLocator.toolWrenchHandler.canWrench(entityplayer, getX(), getY(), getZ())) {
			if (MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openGui) {
					((PipeLogisticsChassi) container.pipe).nextOrientation();
				} else {
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			SimpleServiceLocator.toolWrenchHandler.wrenchUsed(entityplayer, getX(), getY(), getZ());
			return true;
		}

		if (!entityplayer.isSneaking() && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.ModuleItem && entityplayer.getCurrentEquippedItem().getItemDamage() != ItemModule.BLANK) {
			if (MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openGui) {
					return tryInsertingModule(entityplayer);
				} else {
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			return true;
		}

		return false;
	}

	/*** IProvideItems ***/
	@Override
	public void canProvide(RequestTreeNode tree, RequestTree root, List<IFilter> filters) {
		if (!isEnabled()) {
			return;
		}
		for (IFilter filter : filters) {
			if (filter.isBlocked() == filter.isFilteredItem(tree.getRequestType()) || filter.blockProvider()) {
				return;
			}
		}
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule) {
				ILegacyActiveModule y = (ILegacyActiveModule) x;
				y.canProvide(tree, root, filters);
			}
		}
	}

	@Override
	public LogisticsOrder fullFill(LogisticsPromise promise, IRequestItems destination, IAdditionalTargetInformation info) {
		if (!isEnabled()) {
			return null;
		}
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule) {
				ILegacyActiveModule y = (ILegacyActiveModule) x;
				LogisticsOrder result = y.fullFill(promise, destination, info);
				if (result != null) {
					spawnParticle(Particles.WhiteParticle, 2);
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list, List<IFilter> filter) {
		if (!isEnabled()) {
			return;
		}
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule) {
				ILegacyActiveModule y = (ILegacyActiveModule) x;
				y.getAllItems(list, filter);
			}
		}
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return hud;
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		hud.stopWatching();
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if (mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ChassiePipeModuleContent.class).setIdentList(ItemIdentifierStack.getListFromInventory(_moduleInventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SendQueueContent.class).setIdentList(ItemIdentifierStack.getListSendQueue(_sendQueue)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), player);
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
		if (MainProxy.isServer(getWorld())) {
			if (Configs.MULTI_THREAD_NUMBER > 0 && !force) {
				HudUpdateTick.add(getRouter());
			} else {
				if (localModeWatchers != null && localModeWatchers.size() > 0) {
					LinkedList<ItemIdentifierStack> items = ItemIdentifierStack.getListSendQueue(_sendQueue);
					MainProxy.sendToPlayerList(PacketHandler.getPacket(SendQueueContent.class).setIdentList(items).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
					return items.size();
				}
			}
		}
		return 0;
	}

	@Override
	public void handleSendQueueItemIdentifierList(Collection<ItemIdentifierStack> _allItems) {
		displayList.clear();
		displayList.addAll(_allItems);
	}

	public ChassiModule getModules() {
		return _module;
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		for (int i = 0; i < _moduleInventory.getSizeInventory(); i++) {
			LogisticsModule current = _module.getModule(i);
			if (current != null) {
				current.registerPosition(ModulePositionType.SLOT, i);
			}
		}
	}

	@Override
	public int getSourceID() {
		return getRouterId();
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		//if we don't have a pointed inventory we can't be interested in anything
		if (getRealInventory() == null) {
			return l1;
		}
		for (int moduleIndex = 0; moduleIndex < getChassiSize(); moduleIndex++) {
			LogisticsModule module = _module.getSubModule(moduleIndex);
			if (module != null && module.interestedInAttachedInventory()) {
				IInventoryUtil inv = getSneakyInventory(false, module.getSlot(), module.getPositionInt());
				if (inv == null) {
					continue;
				}
				Set<ItemIdentifier> items = inv.getItems();
				l1.addAll(items);

				//also add tag-less variants ... we should probably add a module.interestedIgnoringNBT at some point
				for (ItemIdentifier id : items) {
					l1.add(id.getIgnoringNBT());
				}

				boolean modulesInterestedInUndamged = false;
				for (int i = 0; i < getChassiSize(); i++) {
					if (_module.getSubModule(moduleIndex).interestedInUndamagedID()) {
						modulesInterestedInUndamged = true;
						break;
					}
				}
				if (modulesInterestedInUndamged) {
					for (ItemIdentifier id : items) {
						l1.add(id.getUndamaged());
					}
				}
				break; // no need to check other modules for interest in the inventory, when we know that 1 already is.
			}
		}
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule module = _module.getSubModule(i);
			if (module != null) {
				Collection<ItemIdentifier> current = module.getSpecificInterests();
				if (current != null) {
					l1.addAll(current);
				}
			}
		}
		return l1;
	}

	@Override
	public boolean hasGenericInterests() {
		if (getRealInventory() == null) {
			return false;
		}
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = _module.getSubModule(i);

			if (x != null && x.hasGenericInterests()) {
				return true;
			}
		}
		return false;
	}

	@CCCommand(description = "Returns the LogisticsModule for the given slot number starting by 1")
	public LogisticsModule getModuleInSlot(Double i) {
		return _module.getSubModule((int) (i - 1));
	}

	@CCCommand(description = "Returns the size of this Chassie pipe")
	public Integer getChassieSize() {
		return getChassiSize();
	}

	public abstract ResourceLocation getChassiGUITexture();

	/** ICraftItems */
	public final LinkedList<LogisticsOrder> _extras = new LinkedList<LogisticsOrder>();

	@Override
	public void registerExtras(IPromise promise) {
		if (!(promise instanceof LogisticsPromise)) {
			throw new UnsupportedOperationException("Extra has to be an item for a chassis pipe");
		}
		ItemIdentifierStack stack = new ItemIdentifierStack(((LogisticsPromise) promise).item, ((LogisticsPromise) promise).numberOfItems);
		_extras.add(new LogisticsItemOrder(new DictResource(stack, null), null, ResourceType.EXTRA, null));
	}

	@Override
	public ICraftingTemplate addCrafting(IResource toCraft) {
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = _module.getSubModule(i);

			if (x != null && x instanceof ICraftItems) {
				if (((ICraftItems) x).canCraft(toCraft)) {
					return ((ICraftItems) x).addCrafting(toCraft);
				}
			}
		}
		return null;

		// trixy code goes here to ensure the right crafter answers the right request
	}

	@Override
	public List<ItemIdentifierStack> getCraftedItems() {
		List<ItemIdentifierStack> craftables = null;
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = _module.getSubModule(i);

			if (x != null && x instanceof ICraftItems) {
				if (craftables == null) {
					craftables = new LinkedList<ItemIdentifierStack>();
				}
				craftables.addAll(((ICraftItems) x).getCraftedItems());
			}
		}
		return craftables;
	}

	@Override
	public boolean canCraft(IResource toCraft) {
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = _module.getSubModule(i);

			if (x != null && x instanceof ICraftItems) {
				if (((ICraftItems) x).canCraft(toCraft)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ISlotUpgradeManager getUpgradeManager(ModulePositionType slot, int positionInt) {
		if (slot != ModulePositionType.SLOT || positionInt >= _upgradeManagers.length) {
			if (LPConstants.DEBUG) {
				new UnsupportedOperationException("Position info arn't for a chassi pipe. (" + slot + "/" + positionInt + ")").printStackTrace();
			}
			return super.getUpgradeManager(slot, positionInt);
		}
		return _upgradeManagers[positionInt];
	}

	@Override
	public int getTodo() {
		// TODO Auto-generated method stub
		// probably not needed, the chasi order manager handles the count, would need to store origin to specifically know this.
		return 0;
	}

	public static class ChassiTargetInformation implements IAdditionalTargetInformation {

		@Getter
		private final int moduleSlot;

		public ChassiTargetInformation(int slot) {
			moduleSlot = slot;
		}
	}
}
