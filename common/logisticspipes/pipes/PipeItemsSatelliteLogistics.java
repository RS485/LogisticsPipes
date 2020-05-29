/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDSatellite;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleSatellite;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.hud.ChestContent;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.satpipe.SyncSatelliteNamePacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifierStack;

public class PipeItemsSatelliteLogistics extends CoreRoutedPipe implements IRequestItems, IRequireReliableTransport, IHeadUpDisplayRendererProvider, IChestContentReceiver {

	public static Set<PipeItemsSatelliteLogistics> AllSatellites = Collections.newSetFromMap(new WeakHashMap<>());

	// called only on server shutdown
	public static void cleanup() {
		PipeItemsSatelliteLogistics.AllSatellites.clear();
	}

	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	public final LinkedList<ItemIdentifierStack> itemList = new LinkedList<>();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<>();
	private final HUDSatellite HUD = new HUDSatellite(this);
	protected final LinkedList<ItemIdentifierStack> _lostItems = new LinkedList<>();

	@Getter
	public String satellitePipeName = "";

	public PipeItemsSatelliteLogistics(Item item) {
		super(item);
		throttleTime = 40;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_SATELLITE_TEXTURE;
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (isNthTick(20) && localModeWatchers.size() > 0) {
			updateInv(false);
		}
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return new ModuleSatellite(this);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	private void addToList(ItemIdentifierStack stack) {
		for (ItemIdentifierStack ident : itemList) {
			if (ident.getItem().equals(stack.getItem())) {
				ident.setStackSize(ident.getStackSize() + stack.getStackSize());
				return;
			}
		}
		itemList.addLast(stack);
	}

	private void updateInv(boolean force) {
		itemList.clear();
		IInventoryUtil inv = this.getPointedInventory();
		if (inv != null) {
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stackInSlot = inv.getStackInSlot(i);
				if (!stackInSlot.isEmpty()) {
					addToList(ItemIdentifierStack.getFromStack(stackInSlot));
				}
			}
		}
		if (!itemList.equals(oldList) || force) {
			oldList.clear();
			oldList.addAll(itemList);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ChestContent.class).setIdentList(itemList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		}
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if (mode == 1) {
			localModeWatchers.add(player);
			final ModernPacket packet = PacketHandler.getPacket(SyncSatelliteNamePacket.class).setString(satellitePipeName).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToPlayer(packet, player);
			updateInv(true);
		} else {
			super.playerStartWatching(player, mode);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		super.playerStopWatching(player, mode);
		localModeWatchers.remove(player);
	}

	@Override
	public void setReceivedChestContent(Collection<ItemIdentifierStack> list) {
		itemList.clear();
		itemList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("satelliteid")) {
			int satelliteId = nbttagcompound.getInteger("satelliteid");
			satellitePipeName = Integer.toString(satelliteId);
		} else {
			satellitePipeName = nbttagcompound.getString("satellitePipeName");
		}
		ensureAllSatelliteStatus();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setString("satellitePipeName", satellitePipeName);
		super.writeToNBT(nbttagcompound);
	}

	protected void ensureAllSatelliteStatus() {
		if (MainProxy.isClient()) {
			return;
		}
		if (satellitePipeName.isEmpty()) {
			PipeItemsSatelliteLogistics.AllSatellites.remove(this);
		}
		if (!satellitePipeName.isEmpty()) {
			PipeItemsSatelliteLogistics.AllSatellites.add(this);
		}
	}

	private void updateWatchers() {
		CoordinatesPacket packet = PacketHandler.getPacket(SyncSatelliteNamePacket.class).setString(satellitePipeName).setTilePos(this.getContainer());
		MainProxy.sendToPlayerList(packet, localModeWatchers);
		MainProxy.sendPacketToAllWatchingChunk(this.getContainer(), packet);
	}

	@Override
	public void onAllowedRemoval() {
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		PipeItemsSatelliteLogistics.AllSatellites.remove(this);
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		// Send the satellite id when opening gui
		final ModernPacket packet = PacketHandler.getPacket(SyncSatelliteNamePacket.class).setString(satellitePipeName).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
		MainProxy.sendPacketToPlayer(packet, entityplayer);
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SatellitePipe_ID, getWorld(), getX(), getY(), getZ());
	}

	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		if (_lostItems.isEmpty()) {
			return;
		}
		final Iterator<ItemIdentifierStack> iterator = _lostItems.iterator();
		while (iterator.hasNext()) {
			ItemIdentifierStack stack = iterator.next();
			int received = RequestTree.requestPartial(stack, (CoreRoutedPipe) container.pipe, null);
			if (received > 0) {
				if (received == stack.getStackSize()) {
					iterator.remove();
				} else {
					stack.setStackSize(stack.getStackSize() - received);
				}
			}
		}
	}

	@Override
	public void itemLost(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		_lostItems.add(item);
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {}

	public void setSatelliteName(String name) {
		satellitePipeName = name;
		if (MainProxy.isServer(this.getWorld())) {
			updateWatchers();
		}
		ensureAllSatelliteStatus();
	}
}
