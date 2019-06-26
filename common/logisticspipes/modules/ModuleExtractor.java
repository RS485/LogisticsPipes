package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.gui.hud.modules.HUDExtractor;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsSneakyDirectionModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ExtractorModuleInHand;
import logisticspipes.network.guis.module.inpipe.ExtractorModuleSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;

public class ModuleExtractor extends LogisticsSneakyDirectionModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {

	//protected final int ticksToAction = 100;
	private int currentTick = 0;

	private EnumFacing _sneakyDirection = null;

	private IHUDModuleRenderer HUD = new HUDExtractor(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	public ModuleExtractor() {

	}

	protected int ticksToAction() {
		return 80 / (int) (Math.pow(2, getUpgradeManager().getActionSpeedUpgrade()));
	}

	protected int neededEnergy() {
		return (int) (5 * Math.pow(1.1, getUpgradeManager().getItemExtractionUpgrade()) * Math.pow(1.2, getUpgradeManager().getItemStackExtractionUpgrade()))	;
	}

	protected int itemsToExtract() {
		return (int) Math.pow(2, getUpgradeManager().getItemExtractionUpgrade());
	}

	protected ItemSendMode itemSendMode() {
		return getUpgradeManager().getItemExtractionUpgrade() > 0 ? ItemSendMode.Fast : ItemSendMode.Normal;
	}

	@Override
	public EnumFacing getSneakyDirection() {
		return _sneakyDirection;
	}

	@Override
	public void setSneakyDirection(EnumFacing sneakyDirection) {
		_sneakyDirection = sneakyDirection;
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ExtractorModuleMode.class).setDirection(_sneakyDirection).setModulePos(this), localModeWatchers);
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		return null;
	}

	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ExtractorModuleSlot.class).setSneakyOrientation(getSneakyDirection());
	}

	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ExtractorModuleInHand.class);
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		if (nbttagcompound.hasKey("sneakydirection")) {
			int sneak = nbttagcompound.getInteger("sneakydirection");
			if(sneak == 6) {
				_sneakyDirection = null;
			} else {
				_sneakyDirection = EnumFacing.values()[sneak];
			}
		} else if (nbttagcompound.hasKey("sneakyorientation")) {
			//convert sneakyorientation to sneakydirection
			int t = nbttagcompound.getInteger("sneakyorientation");
			switch (t) {
				default:
				case 0:
					_sneakyDirection = null;
					break;
				case 1:
					_sneakyDirection = EnumFacing.UP;
					break;
				case 2:
					_sneakyDirection = EnumFacing.SOUTH;
					break;
				case 3:
					_sneakyDirection = EnumFacing.DOWN;
					break;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("sneakydirection", _sneakyDirection == null ? 6: _sneakyDirection.ordinal());
	}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction()) {
			return;
		}
		currentTick = 0;

		//Extract Item
		TileEntity realInventory = _service.getRealInventory();
		if (realInventory == null) {
			return;
		}
		EnumFacing extractOrientation = _sneakyDirection;
		if (extractOrientation == null) {
			extractOrientation = _service.inventoryOrientation().getOpposite();
		}

		IInventoryUtil targetUtil = _service.getSneakyInventory(extractOrientation);

		int itemsleft = itemsToExtract();
		for (int i = 0; i < targetUtil.getSizeInventory(); i++) {

			ItemStack slot = targetUtil.getStackInSlot(i);
			if (slot.isEmpty()) {
				continue;
			}
			ItemIdentifier slotitem = ItemIdentifier.get(slot);
			List<Integer> jamList = new LinkedList<>();
			Pair<Integer, SinkReply> reply = _service.hasDestination(slotitem, true, jamList);
			if (reply == null) {
				continue;
			}

			while (reply != null) {
				int count = Math.min(itemsleft, slot.getCount());
				count = Math.min(count, slotitem.getMaxStackSize());
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

				ItemStack stackToSend = targetUtil.decrStackSize(i, count);
				if (stackToSend.isEmpty()) {
					break;
				}
				count = stackToSend.getCount();
				_service.sendStack(stackToSend, reply, itemSendMode());
				itemsleft -= count;
				if (itemsleft <= 0) {
					break;
				}
				slot = targetUtil.getStackInSlot(i);
				if (slot.isEmpty()) {
					break;
				}
				jamList.add(reply.getValue1());
				reply = _service.hasDestination(ItemIdentifier.get(slot), true, jamList);
			}
			if (itemsleft <= 0) {
				break;
			}
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<>(1);
		list.add("Extraction: " + ((_sneakyDirection == null) ? "DEFAULT" : _sneakyDirection.name()));
		return list;
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
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ExtractorModuleMode.class).setDirection(_sneakyDirection).setModulePos(this), localModeWatchers);
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
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
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

}
