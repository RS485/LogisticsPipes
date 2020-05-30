package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import kotlin.Pair;

import logisticspipes.gui.hud.modules.HUDExtractor;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.SneakyModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inpipe.SneakyModuleInSlotGuiProvider;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.modules.SneakyModuleDirectionUpdate;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.logistics.LogisticsManager;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.SneakyDirection;

public class ModuleExtractor extends LogisticsModule implements SneakyDirection, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, Gui {

	//protected final int ticksToAction = 100;
	private int currentTick = 0;

	private EnumFacing _sneakyDirection = null;

	private IHUDModuleRenderer HUD = new HUDExtractor(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	public ModuleExtractor() {}

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
		return getUpgradeManager().getItemExtractionUpgrade() > 0 ? ItemSendMode.Fast : ItemSendMode.Normal;
	}

	@Override
	public EnumFacing getSneakyDirection() {
		return _sneakyDirection;
	}

	@Override
	public void setSneakyDirection(EnumFacing sneakyDirection) {
		_sneakyDirection = sneakyDirection;
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SneakyModuleDirectionUpdate.class).setDirection(_sneakyDirection).setModulePos(this), localModeWatchers);
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		_sneakyDirection = SneakyDirection.readSneakyDirection(nbttagcompound);
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		SneakyDirection.writeSneakyDirection(_sneakyDirection, nbttagcompound);
	}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction()) {
			return;
		}
		currentTick = 0;

		//Extract Item
		final NeighborTileEntity<TileEntity> pointedItemHandler = _service.getPointedItemHandler();
		if (pointedItemHandler == null) {
			return;
		}
		EnumFacing extractOrientation = _sneakyDirection;
		if (extractOrientation == null) {
			final EnumFacing pointedOrientation = _service.getPointedOrientation();
			if (pointedOrientation != null) {
				extractOrientation = pointedOrientation.getOpposite();
			}
		}

		if (extractOrientation == null) return;
		IInventoryUtil targetUtil = _service.getSneakyInventory(extractOrientation);
		if (targetUtil == null) return;

		int itemsleft = itemsToExtract();
		for (int i = 0; i < targetUtil.getSizeInventory(); i++) {

			ItemStack slot = targetUtil.getStackInSlot(i);
			if (slot.isEmpty()) {
				continue;
			}
			ItemIdentifier slotitem = ItemIdentifier.get(slot);
			List<Integer> jamList = new LinkedList<>();
			Pair<Integer, SinkReply> reply = LogisticsManager.INSTANCE.getDestination(slot, slotitem, true, (ServerRouter) _service.getRouter(), jamList);
			if (reply == null) {
				continue;
			}

			while (reply != null) {
				int count = Math.min(itemsleft, slot.getCount());
				count = Math.min(count, slotitem.getMaxStackSize());
				if (reply.getSecond().maxNumberOfItems > 0) {
					count = Math.min(count, reply.getSecond().maxNumberOfItems);
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
				_service.sendStack(stackToSend, reply.getFirst(), reply.getSecond(), itemSendMode());
				itemsleft -= count;
				if (itemsleft <= 0) {
					break;
				}
				slot = targetUtil.getStackInSlot(i);
				if (slot.isEmpty()) {
					break;
				}
				jamList.add(reply.getFirst());
				reply = LogisticsManager.INSTANCE.getDestination(slot, ItemIdentifier.get(slot), true, (ServerRouter) _service.getRouter(), jamList);
			}
			if (itemsleft <= 0) {
				break;
			}
		}
	}

	@Override
	public @Nonnull List<String> getClientInformation() {
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
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SneakyModuleDirectionUpdate.class).setDirection(_sneakyDirection).setModulePos(this), localModeWatchers);
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

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(SneakyModuleInSlotGuiProvider.class).setSneakyOrientation(getSneakyDirection());
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(SneakyModuleInHandGuiProvider.class);
	}

}
