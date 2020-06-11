/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.Getter;

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
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
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
import logisticspipes.utils.EnumFacingUtil;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

@CCType(name = "LogisticsChassiePipe")
public abstract class PipeLogisticsChassi extends CoreRoutedPipe implements ICraftItems, IBufferItems, ISimpleInventoryEventHandler, ISendRoutedItem, IProvideItems, IHeadUpDisplayRendererProvider, ISendQueueContentRecieiver {

	private final ChassiModule _module;
	private final ItemIdentifierInventory _moduleInventory;
	private final ModuleUpgradeManager[] _upgradeManagers;
	private boolean switchOrientationOnTick = true;
	private boolean init = false;

	// HUD
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<>();
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
		_module.registerHandler(this, this);
		hud = new HudChassisPipe(this, _moduleInventory);
		pointedDirection = null;
	}

	@Override
	protected List<TileEntity> getConnectedRawInventories() {
		if (_cachedAdjacentInventories != null) {
			return _cachedAdjacentInventories;
		}
		final NeighborTileEntity<TileEntity> pointedItemHandler = getPointedItemHandler();
		if (pointedItemHandler == null) {
			_cachedAdjacentInventories = Collections.emptyList();
		} else {
			_cachedAdjacentInventories = Collections.singletonList(pointedItemHandler.getTileEntity());
		}
		return _cachedAdjacentInventories;
	}

	public void nextOrientation() {
		boolean found = false;
		EnumFacing oldOrientation = pointedDirection;
		for (int l = 0; l < 6; ++l) {
			pointedDirection = EnumFacing.values()[(pointedDirection == null ? 6 : pointedDirection.ordinal() + 1) % 6];
			if (isValidOrientation(pointedDirection)) {
				found = true;
				break;
			}
		}
		if (!found) {
			pointedDirection = null;
		}
		if (pointedDirection != oldOrientation) {
			clearCache();
			MainProxy.sendPacketToAllWatchingChunk(_module, PacketHandler.getPacket(ChassiOrientationPacket.class).setDir(pointedDirection).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
			refreshRender(true);
		}
	}

	public void setClientOrientation(EnumFacing dir) {
		if (MainProxy.isClient(getWorld())) {
			pointedDirection = dir;
		}
	}

	private boolean isValidOrientation(EnumFacing connection) {
		if (connection == null) {
			return false;
		}
		if (getRouter().isRoutedExit(connection)) {
			return false;
		}
		DoubleCoordinates pos = CoordinateUtils.add(new DoubleCoordinates(this), connection);
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
	public TextureType getRoutedTexture(EnumFacing connection) {
		if (getRouter().isSubPoweredExit(connection)) {
			return Textures.LOGISTICSPIPE_SUBPOWER_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE;
	}

	@Override
	public TextureType getNonRoutedTexture(EnumFacing connection) {
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
	}

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
			int tmp = nbttagcompound.getInteger("Orientation");
			if (tmp == -1) {
				pointedDirection = null;
			} else {
				pointedDirection = EnumFacingUtil.getOrientation(tmp % 6);
			}
			switchOrientationOnTick = (pointedDirection == null);
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
		nbttagcompound.setInteger("Orientation", pointedDirection == null ? -1 : pointedDirection.ordinal());
		for (int i = 0; i < getChassiSize(); i++) {
			_upgradeManagers[i].writeToNBT(nbttagcompound, Integer.toString(i));
		}
	}

	@Override
	public void onAllowedRemoval() {
		_moduleInventory.removeListener(this);
		if (MainProxy.isServer(getWorld())) {
			for (int i = 0; i < getChassiSize(); i++) {
				LogisticsModule x = getSubModule(i);
				if (x instanceof ILegacyActiveModule) {
					ILegacyActiveModule y = (ILegacyActiveModule) x;
					y.onBlockRemoval();
				}
			}
			for (int i = 0; i < _moduleInventory.getSizeInventory(); i++) {
				ItemIdentifierStack ms = _moduleInventory.getIDStackInSlot(i);
				if (ms != null) {
					ItemStack stack = ms.makeNormalStack();
					ItemModuleInformationManager.saveInformation(stack, getSubModule(i));
					_moduleInventory.setInventorySlotContents(i, stack);
				}
			}
			_moduleInventory.dropContents(getWorld(), getX(), getY(), getZ());

			for (int i = 0; i < getChassiSize(); i++) {
				getModuleUpgradeManager(i).dropUpgrades();
			}
		}
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		if (MainProxy.isServer(getWorld())) {
			if (info instanceof ChassiTargetInformation) {
				ChassiTargetInformation target = (ChassiTargetInformation) info;
				LogisticsModule module = getSubModule(target.moduleSlot);
				if (module instanceof IRequireReliableTransport) {
					((IRequireReliableTransport) module).itemArrived(item, info);
				}
			} else {
				if (LogisticsPipes.isDEBUG() && info != null) {
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
				LogisticsModule module = getSubModule(target.moduleSlot);
				if (module instanceof IRequireReliableTransport) {
					((IRequireReliableTransport) module).itemLost(item, info);
				}
			} else {
				if (LogisticsPipes.isDEBUG()) {
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
				LogisticsModule module = getSubModule(target.moduleSlot);
				if (module instanceof IBufferItems) {
					return ((IBufferItems) module).addToBuffer(item, info);
				}
			} else {
				if (LogisticsPipes.isDEBUG()) {
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
			if (stack.isEmpty()) {
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
					if (!MainProxy.isClient(getWorld())) {
						ItemModuleInformationManager.readInformation(stack, next);
					}
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
		if (MainProxy.isServer(getWorld())) {
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

	@Nonnull
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
			if (item.isEmpty()) {
				_moduleInventory.setInventorySlotContents(i, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).splitStack(1));
				InventoryChanged(_moduleInventory);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		if (entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty()) {
			return false;
		}

		if (entityplayer.isSneaking() && SimpleServiceLocator.configToolHandler.canWrench(entityplayer, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), container)) {
			if (MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openGui) {
					((PipeLogisticsChassi) container.pipe).nextOrientation();
				} else {
					entityplayer.sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			SimpleServiceLocator.configToolHandler.wrenchUsed(entityplayer, entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), container);
			return true;
		}

		if (!entityplayer.isSneaking() && entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemModule) {
			if (MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openGui) {
					return tryInsertingModule(entityplayer);
				} else {
					entityplayer.sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
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
			LogisticsModule x = getSubModule(i);
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
			LogisticsModule x = getSubModule(i);
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
			LogisticsModule x = getSubModule(i);
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
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
		// if we don't have a pointed inventory we can't be interested in anything
		if (getPointedItemHandler() == null) {
			return;
		}

		for (int moduleIndex = 0; moduleIndex < getChassiSize(); moduleIndex++) {
			LogisticsModule module = getSubModule(moduleIndex);
			if (module != null && module.interestedInAttachedInventory()) {
				IInventoryUtil inv = getSneakyInventory(module.getSlot(), module.getPositionInt());
				if (inv == null) {
					continue;
				}
				Set<ItemIdentifier> items = inv.getItems();
				itemidCollection.addAll(items);

				//also add tag-less variants ... we should probably add a module.interestedIgnoringNBT at some point
				items.stream().map(ItemIdentifier::getIgnoringNBT).forEach(itemidCollection::add);

				boolean modulesInterestedInUndamged = false;
				for (int i = 0; i < getChassiSize(); i++) {
					if (getSubModule(moduleIndex).interestedInUndamagedID()) {
						modulesInterestedInUndamged = true;
						break;
					}
				}
				if (modulesInterestedInUndamged) {
					items.stream().map(ItemIdentifier::getUndamaged).forEach(itemidCollection::add);
				}
				break; // no need to check other modules for interest in the inventory, when we know that 1 already is.
			}
		}
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule module = getSubModule(i);
			if (module != null) {
				module.collectSpecificInterests(itemidCollection);
			}
		}
	}

	@Override
	public boolean hasGenericInterests() {
		if (getPointedItemHandler() == null) {
			return false;
		}
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = getSubModule(i);

			if (x != null && x.hasGenericInterests()) {
				return true;
			}
		}
		return false;
	}

	@CCCommand(description = "Returns the LogisticsModule for the given slot number starting by 1")
	public LogisticsModule getModuleInSlot(Double i) {
		return getSubModule((int) (i - 1));
	}

	@CCCommand(description = "Returns the size of this Chassie pipe")
	public Integer getChassieSize() {
		return getChassiSize();
	}

	/** ICraftItems */
	public final LinkedList<LogisticsOrder> _extras = new LinkedList<>();

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
			LogisticsModule x = getSubModule(i);

			if (x instanceof ICraftItems) {
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
			LogisticsModule x = getSubModule(i);

			if (x instanceof ICraftItems) {
				if (craftables == null) {
					craftables = new LinkedList<>();
				}
				craftables.addAll(((ICraftItems) x).getCraftedItems());
			}
		}
		return craftables;
	}

	@Override
	public boolean canCraft(IResource toCraft) {
		for (int i = 0; i < getChassiSize(); i++) {
			LogisticsModule x = getSubModule(i);

			if (x instanceof ICraftItems) {
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
			if (LogisticsPipes.isDEBUG()) {
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

	@Nullable
	public LogisticsModule getSubModule(int slot) {
		return _module.getModule(slot);
	}

	public static class ChassiTargetInformation implements IAdditionalTargetInformation {

		@Getter
		private final int moduleSlot;

		public ChassiTargetInformation(int slot) {
			moduleSlot = slot;
		}
	}
}
