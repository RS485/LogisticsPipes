/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.gui.GuiChassisPipe;
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
import logisticspipes.logisticspipes.ChassisTransportLayer;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.ChassisModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.pipe.ChassisOrientationPacket;
import logisticspipes.network.packets.pipe.ChassisPipeModuleContent;
import logisticspipes.network.packets.pipe.RequestChassisOrientationPacket;
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
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.connection.Adjacent;
import network.rs485.logisticspipes.connection.ConnectionType;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.connection.SingleAdjacent;
import network.rs485.logisticspipes.module.PipeServiceProviderUtilKt;
import network.rs485.logisticspipes.pipes.IChassisPipe;

@CCType(name = "LogisticsChassiePipe")
public abstract class PipeLogisticsChassis extends CoreRoutedPipe
		implements ICraftItems, IBufferItems, ISimpleInventoryEventHandler, ISendRoutedItem, IProvideItems,
		IHeadUpDisplayRendererProvider, ISendQueueContentRecieiver, IChassisPipe {

	private final ChassisModule _module;
	private final ItemIdentifierInventory _moduleInventory;
	private final NonNullList<ModuleUpgradeManager> slotUpgradeManagers = NonNullList.create();
	private boolean init = false;

	// HUD
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<>();
	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final HudChassisPipe hud;

	public PipeLogisticsChassis(Item item) {
		super(item);
		_moduleInventory = new ItemIdentifierInventory(getChassisSize(), "Chassi pipe", 1);
		_moduleInventory.addListener(this);
		assert slotUpgradeManagers.size() == 0; // starting at index 0
		for (int i = 0; i < getChassisSize(); i++) {
			addModuleUpgradeManager();
		}
		_module = new ChassisModule(getChassisSize(), this);
		_module.registerHandler(this, this);
		hud = new HudChassisPipe(this, _moduleInventory);
	}

	/**
	 * Returns just the adjacent this chassis points at or no adjacent.
	 */
	@Nonnull
	@Override
	public Adjacent getAvailableAdjacent() {
		return getPointedAdjacentOrNoAdjacent();
	}

	/**
	 * Updates pointedAdjacent on {@link CoreRoutedPipe}.
	 */
	@Override
	protected void updateAdjacentCache() {
		super.updateAdjacentCache();
		final Adjacent adjacent = getAdjacent();
		if (adjacent instanceof SingleAdjacent) {
			setPointedAdjacent(((SingleAdjacent) adjacent));
		} else {
			final SingleAdjacent oldPointedAdjacent = getPointedAdjacent();
			SingleAdjacent newPointedAdjacent = null;
			if (oldPointedAdjacent != null) {
				// update pointed adjacent with connection type or reset it
				newPointedAdjacent = adjacent.optionalGet(oldPointedAdjacent.getDir()).map(connectionType -> new SingleAdjacent(this, oldPointedAdjacent.getDir(), connectionType)).orElse(null);
			}
			if (newPointedAdjacent == null) {
				newPointedAdjacent = adjacent.neighbors().entrySet().stream().findAny().map(connectedNeighbor -> new SingleAdjacent(this, connectedNeighbor.getKey().getDirection(), connectedNeighbor.getValue())).orElse(null);
			}
			setPointedAdjacent(newPointedAdjacent);
		}
	}

	@Override
	public void nextOrientation() {
		final SingleAdjacent pointedAdjacent = getPointedAdjacent();
		Pair<NeighborTileEntity<TileEntity>, ConnectionType> newNeighbor;
		if (pointedAdjacent == null) {
			newNeighbor = nextPointedOrientation(null);
		} else {
			newNeighbor = nextPointedOrientation(pointedAdjacent.getDir());
		}
		final ChassisOrientationPacket packet = PacketHandler.getPacket(ChassisOrientationPacket.class);
		if (newNeighbor == null) {
			setPointedAdjacent(null);
			packet.setDir(null);
		} else {
			setPointedAdjacent(new SingleAdjacent(this, newNeighbor.getValue1().getDirection(), newNeighbor.getValue2()));
			packet.setDir(newNeighbor.getValue1().getDirection());
		}
		MainProxy.sendPacketToAllWatchingChunk(_module, packet.setTilePos(container));
		refreshRender(true);
	}

	@Override
	public void setPointedOrientation(@Nullable EnumFacing dir) {
		if (dir == null) {
			setPointedAdjacent(null);
		} else {
			setPointedAdjacent(new SingleAdjacent(this, dir, ConnectionType.UNDEFINED));
		}
	}

	@Override
	@Nonnull
	public IInventory getModuleInventory() {
		return _moduleInventory;
	}

	@Nonnull
	public ModuleUpgradeManager getModuleUpgradeManager(int slot) {
		return slotUpgradeManagers.get(slot);
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
		if (getPointedAdjacent() != null && connection.equals(getPointedAdjacent().getDir())) {
			return Textures.LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE;
		}
		if (isPowerProvider(connection)) {
			return Textures.LOGISTICSPIPE_POWERED_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		try {
			super.readFromNBT(nbttagcompound);
			_moduleInventory.readFromNBT(nbttagcompound, "chassi");
			InventoryChanged(_moduleInventory);
			_module.readFromNBT(nbttagcompound);
			int tmp = nbttagcompound.getInteger("Orientation");
			if (tmp != -1) {
				setPointedOrientation(EnumFacingUtil.getOrientation(tmp % 6));
			}
			for (int i = 0; i < getChassisSize(); i++) {
				if (i >= slotUpgradeManagers.size()) {
					addModuleUpgradeManager();
				}
				slotUpgradeManagers.get(i).readFromNBT(nbttagcompound, Integer.toString(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addModuleUpgradeManager() {
		slotUpgradeManagers.add(new ModuleUpgradeManager(this, upgradeManager));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		_moduleInventory.writeToNBT(nbttagcompound, "chassi");
		_module.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("Orientation", getPointedAdjacent() == null ? -1 : getPointedAdjacent().getDir().ordinal());
		for (int i = 0; i < getChassisSize(); i++) {
			slotUpgradeManagers.get(i).writeToNBT(nbttagcompound, Integer.toString(i));
		}
	}

	@Override
	public void onAllowedRemoval() {
		_moduleInventory.removeListener(this);
		if (MainProxy.isServer(getWorld())) {
			for (int i = 0; i < getChassisSize(); i++) {
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

			for (int i = 0; i < getChassisSize(); i++) {
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
				Objects.requireNonNull(next, "getModuleForItem returned null for " + stack.toString());
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
				if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiChassisPipe) {
					FMLClientHandler.instance().getClient().currentScreen.initGui();
				}
			}
		}
		if (MainProxy.isServer(getWorld())) {
			if (!localModeWatchers.isEmpty()) {
				MainProxy.sendToPlayerList(PacketHandler.getPacket(ChassisPipeModuleContent.class).setIdentList(ItemIdentifierStack.getListFromInventory(_moduleInventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
			}
		}
	}

	@Override
	public void ignoreDisableUpdateEntity() {
		if (!init) {
			init = true;
			if (MainProxy.isClient(getWorld())) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestChassisOrientationPacket.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
			}
		}
	}

	@Override
	public final LogisticsModule getLogisticsModule() {
		return _module;
	}

	@Nonnull
	@Override
	public TransportLayer getTransportLayer() {
		if (_transportLayer == null) {
			_transportLayer = new ChassisTransportLayer(this);
		}
		return _transportLayer;
	}

	private boolean tryInsertingModule(EntityPlayer entityplayer) {
		for (int i = 0; i < _moduleInventory.getSizeInventory(); i++) {
			if (_moduleInventory.getIDStackInSlot(i) == null) {
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
					((PipeLogisticsChassis) container.pipe).nextOrientation();
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
		for (int i = 0; i < getChassisSize(); i++) {
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
		for (int i = 0; i < getChassisSize(); i++) {
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
		for (int i = 0; i < getChassisSize(); i++) {
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
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ChassisPipeModuleContent.class).setIdentList(ItemIdentifierStack.getListFromInventory(_moduleInventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), player);
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

	@Override
	public int sendQueueChanged(boolean force) {
		if (MainProxy.isServer(getWorld())) {
			if (Configs.MULTI_THREAD_NUMBER > 0 && !force) {
				HudUpdateTick.add(getRouter());
			} else {
				if (localModeWatchers.size() > 0) {
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

	public ChassisModule getModules() {
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
		if (getPointedAdjacentOrNoAdjacent().inventories().isEmpty()) {
			return;
		}

		for (int moduleIndex = 0; moduleIndex < getChassisSize(); moduleIndex++) {
			LogisticsModule module = getSubModule(moduleIndex);
			if (module != null && module.interestedInAttachedInventory()) {
				final ISlotUpgradeManager upgradeManager = getUpgradeManager(module.getSlot(), module.getPositionInt());
				IInventoryUtil inv = PipeServiceProviderUtilKt.availableSneakyInventories(this, upgradeManager).stream().findFirst().orElse(null);
				if (inv == null) {
					continue;
				}
				Set<ItemIdentifier> items = inv.getItems();
				itemidCollection.addAll(items);

				//also add tag-less variants ... we should probably add a module.interestedIgnoringNBT at some point
				items.stream().map(ItemIdentifier::getIgnoringNBT).forEach(itemidCollection::add);

				boolean modulesInterestedInUndamged = false;
				for (int i = 0; i < getChassisSize(); i++) {
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
		for (int i = 0; i < getChassisSize(); i++) {
			LogisticsModule module = getSubModule(i);
			if (module != null) {
				module.collectSpecificInterests(itemidCollection);
			}
		}
	}

	@Override
	public boolean hasGenericInterests() {
		if (getPointedAdjacentOrNoAdjacent().inventories().isEmpty()) {
			return false;
		}
		for (int i = 0; i < getChassisSize(); i++) {
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
		return getChassisSize();
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
		for (int i = 0; i < getChassisSize(); i++) {
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
		for (int i = 0; i < getChassisSize(); i++) {
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
	public void firstInitialiseTick() {
		super.firstInitialiseTick();
		updateAdjacentCache();
	}

	@Override
	public boolean canCraft(IResource toCraft) {
		for (int i = 0; i < getChassisSize(); i++) {
			LogisticsModule x = getSubModule(i);

			if (x instanceof ICraftItems) {
				if (((ICraftItems) x).canCraft(toCraft)) {
					return true;
				}
			}
		}
		return false;
	}

	@Nonnull
	@Override
	public ISlotUpgradeManager getUpgradeManager(ModulePositionType slot, int positionInt) {
		if (slot != ModulePositionType.SLOT || positionInt >= slotUpgradeManagers.size()) {
			if (LogisticsPipes.isDEBUG()) {
				new UnsupportedOperationException("Position info aren't for a chassis pipe. (" + slot + "/" + positionInt + ")").printStackTrace();
			}
			return super.getUpgradeManager(slot, positionInt);
		}
		return slotUpgradeManagers.get(positionInt);
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
