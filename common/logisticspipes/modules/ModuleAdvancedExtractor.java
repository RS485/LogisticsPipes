package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import logisticspipes.gui.hud.modules.HUDAdvancedExtractor;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.AdvancedExtractorModuleInHand;
import logisticspipes.network.guis.module.inpipe.AdvancedExtractorModuleSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.network.packets.modules.AdvancedExtractorInclude;
import logisticspipes.network.packets.modules.SneakyModuleDirectionUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import network.rs485.logisticspipes.module.SimpleFilter;
import network.rs485.logisticspipes.module.SneakyDirection;

@CCType(name = "Advanced Extractor Module")
public class ModuleAdvancedExtractor extends LogisticsGuiModule implements SimpleFilter, SneakyDirection, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive, ISimpleInventoryEventHandler {

	private final ItemIdentifierInventory _filterInventory = new ItemIdentifierInventory(9, "Item list", 1);
	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	protected int currentTick = 0;
	private boolean _itemsIncluded = true;

	private EnumFacing _sneakyDirection = null;

	private IHUDModuleRenderer HUD = new HUDAdvancedExtractor(this);

	public ModuleAdvancedExtractor() {
		_filterInventory.addListener(this);
	}

	@Override
	@CCCommand(description = "Returns the FilterInventory of this Module")
	@Nonnull
	public ItemIdentifierInventory getFilterInventory() {
		return _filterInventory;
	}

	@Override
	public EnumFacing getSneakyDirection() {
		return _sneakyDirection;
	}

	@Override
	public void setSneakyDirection(EnumFacing sneakyDirection) {
		_sneakyDirection = sneakyDirection;
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound);
		setItemsIncluded(nbttagcompound.getBoolean("itemsIncluded"));
		_sneakyDirection = SneakyDirection.readSneakyDirection(nbttagcompound);
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("itemsIncluded", areItemsIncluded());
		SneakyDirection.writeSneakyDirection(_sneakyDirection, nbttagcompound);
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		return null;
	}

	protected int ticksToAction() {
		return 80 / (int) (Math.pow(2, getUpgradeManager().getActionSpeedUpgrade()));
	}

	protected int neededEnergy() {
		return (int) (5 * Math.pow(1.1, getUpgradeManager().getItemExtractionUpgrade()) * Math.pow(1.2, getUpgradeManager().getItemStackExtractionUpgrade()));
	}

	protected int itemsToExtract() {
		return (int) Math.pow(2, getUpgradeManager().getItemExtractionUpgrade());
	}

	protected ItemSendMode itemSendMode() {
		return 1 + getUpgradeManager().getItemExtractionUpgrade() > 0 ? ItemSendMode.Fast : ItemSendMode.Normal;
	}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction()) {
			return;
		}
		currentTick = 0;

		EnumFacing extractOrientation = _sneakyDirection;
		if (extractOrientation == null) {
			EnumFacing invOrientation = _service.getPointedOrientation();
			if (invOrientation != null) {
				extractOrientation = invOrientation.getOpposite();
			}
		}
		if (extractOrientation == null) return;
		IInventoryUtil inventory = _service.getSneakyInventory(extractOrientation);
		if (inventory == null) {
			return;
		}

		checkExtract(inventory);
	}

	private void checkExtract(IInventoryUtil invUtil) {
		Map<ItemIdentifier, Integer> items = invUtil.getItemsAndCount();
		int itemsleft = itemsToExtract();
		for (Entry<ItemIdentifier, Integer> item : items.entrySet()) {
			if (!CanExtract(item.getKey().makeNormalStack(item.getValue()))) {
				continue;
			}
			List<Integer> jamList = new LinkedList<>();
			Pair<Integer, SinkReply> reply = _service.hasDestination(item.getKey(), true, jamList);
			if (reply == null) {
				continue;
			}

			while (reply != null) {
				int count = Math.min(itemsleft, item.getValue());
				count = Math.min(count, item.getKey().getMaxStackSize());
				if (reply.getValue2().maxNumberOfItems > 0) {
					count = Math.min(count, reply.getValue2().maxNumberOfItems);
				}

				while (!_service.useEnergy(neededEnergy() * count) && count > 0) {
					_service.spawnParticle(Particles.OrangeParticle, 2);
					count--;
				}

				if (count <= 0) {
					break;
				}

				ItemStack stackToSend = invUtil.getMultipleItems(item.getKey(), count);
				if (stackToSend.isEmpty()) {
					break;
				}
				count = stackToSend.getCount();
				_service.sendStack(stackToSend, reply, itemSendMode());
				itemsleft -= count;
				if (itemsleft <= 0) {
					break;
				}

				if (count <= 0) {
					jamList.add(reply.getValue1());
				}

				reply = _service.hasDestination(item.getKey(), true, jamList);
			}
			if (itemsleft <= 0) {
				return;
			}
		}
	}

	public boolean CanExtract(ItemStack item) {
		for (int i = 0; i < _filterInventory.getSizeInventory(); i++) {

			ItemStack stack = _filterInventory.getStackInSlot(i);
			if ((!stack.isEmpty()) && (stack.getItem() == item.getItem())) {
				if (item.getItem().isDamageable()) {
					return areItemsIncluded();
				}
				if (stack.getItemDamage() == item.getItemDamage()) {
					return areItemsIncluded();
				}
			}
		}
		return !areItemsIncluded();
	}

	public boolean areItemsIncluded() {
		return _itemsIncluded;
	}

	public void setItemsIncluded(boolean flag) {
		_itemsIncluded = flag;
		if (!localModeWatchers.isEmpty()) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(AdvancedExtractorInclude.class).setFlag(areItemsIncluded()).setModulePos(this), localModeWatchers);
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<>(5);
		list.add(areItemsIncluded() ? "Included" : "Excluded");
		list.add("Extraction: " + ((_sneakyDirection == null) ? "DEFAULT" : _sneakyDirection.name()));
		list.add("Filter: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		if (MainProxy.isServer(_world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(inventory)).setModulePos(this), localModeWatchers);
		}
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
	}

	@Override
	public void startHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void stopHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(_filterInventory)).setModulePos(this), player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SneakyModuleDirectionUpdate.class).setDirection(_sneakyDirection).setModulePos(this), player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AdvancedExtractorInclude.class).setFlag(areItemsIncluded()).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return false;
	}

	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(AdvancedExtractorModuleSlot.class).setAreItemsIncluded(_itemsIncluded);
	}

	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(AdvancedExtractorModuleInHand.class);
	}
}
