/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.*;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDSatellite;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.modules.ModuleSatelite;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.hud.ChestContent;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.satpipe.SatPipeNext;
import logisticspipes.network.packets.satpipe.SatPipePrev;
import logisticspipes.network.packets.satpipe.SatPipeSetID;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public class PipeItemsSatelliteLogistics extends CoreRoutedPipe implements IRequestItems, IRequireReliableTransport, IHeadUpDisplayRendererProvider, IChestContentReceiver {

	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	public final LinkedList<ItemIdentifierStack> itemList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	private final HUDSatellite HUD = new HUDSatellite(this);

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
		return new ModuleSatelite(this);
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

	private IInventory getRawInventory(ForgeDirection ori) {
		LPPosition pos = new LPPosition(getX(), getY(), getZ());
		pos.moveForward(ori);
		TileEntity tile = pos.getTileEntity(getWorld());
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			return null;
		}
		if (!(tile instanceof IInventory)) {
			return null;
		}
		return InventoryHelper.getInventory((IInventory) tile);
	}

	private IInventory getInventory(ForgeDirection ori) {
		IInventory rawInventory = getRawInventory(ori);
		if (rawInventory instanceof net.minecraft.inventory.ISidedInventory) {
			return new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) rawInventory, ori.getOpposite(), false);
		}
		return rawInventory;
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
		for (ForgeDirection ori : ForgeDirection.VALID_DIRECTIONS) {
			if(!this.container.isPipeConnected(ori)) continue;
			IInventory inv = getInventory(ori);
			if (inv != null) {
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					if (inv.getStackInSlot(i) != null) {
						addToList(ItemIdentifierStack.getFromStack(inv.getStackInSlot(i)));
					}
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
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
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

	public static Set<PipeItemsSatelliteLogistics> AllSatellites = Collections.newSetFromMap(new WeakHashMap<PipeItemsSatelliteLogistics, Boolean>());;

	// called only on server shutdown
	public static void cleanup() {
		PipeItemsSatelliteLogistics.AllSatellites.clear();
	}

	protected final LinkedList<ItemIdentifierStack> _lostItems = new LinkedList<ItemIdentifierStack>();

	public int satelliteId;

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		satelliteId = nbttagcompound.getInteger("satelliteid");
		ensureAllSatelliteStatus();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("satelliteid", satelliteId);
		super.writeToNBT(nbttagcompound);
	}

	protected int findId(int increment) {
		if (MainProxy.isClient(getWorld())) {
			return satelliteId;
		}
		int potentialId = satelliteId;
		boolean conflict = true;
		while (conflict) {
			potentialId += increment;
			if (potentialId < 0) {
				return 0;
			}
			conflict = false;
			for (final PipeItemsSatelliteLogistics sat : PipeItemsSatelliteLogistics.AllSatellites) {
				if (sat.satelliteId == potentialId) {
					conflict = true;
					break;
				}
			}
		}
		return potentialId;
	}

	protected void ensureAllSatelliteStatus() {
		if (MainProxy.isClient()) {
			return;
		}
		if (satelliteId == 0 && PipeItemsSatelliteLogistics.AllSatellites.contains(this)) {
			PipeItemsSatelliteLogistics.AllSatellites.remove(this);
		}
		if (satelliteId != 0 && !PipeItemsSatelliteLogistics.AllSatellites.contains(this)) {
			PipeItemsSatelliteLogistics.AllSatellites.add(this);
		}
	}

	public void setNextId(EntityPlayer player) {
		satelliteId = findId(1);
		ensureAllSatelliteStatus();
		if (MainProxy.isClient(player.worldObj)) {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeNext.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToServer(packet);
		} else {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToPlayer(packet, player);
		}
		updateWatchers();
	}

	public void setPrevId(EntityPlayer player) {
		satelliteId = findId(-1);
		ensureAllSatelliteStatus();
		if (MainProxy.isClient(player.worldObj)) {
			final ModernPacket packet = PacketHandler.getPacket(SatPipePrev.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToServer(packet);
		} else {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
			MainProxy.sendPacketToPlayer(packet, player);
		}
		updateWatchers();
	}

	private void updateWatchers() {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), ((PipeItemsSatelliteLogistics) container.pipe).localModeWatchers);
	}

	@Override
	public void onAllowedRemoval() {
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		if (PipeItemsSatelliteLogistics.AllSatellites.contains(this)) {
			PipeItemsSatelliteLogistics.AllSatellites.remove(this);
		}
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		// Send the satellite id when opening gui
		final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
		MainProxy.sendPacketToPlayer(packet, entityplayer);
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SatelitePipe_ID, getWorld(), getX(), getY(), getZ());
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

	public void setSatelliteId(int integer) {
		satelliteId = integer;
	}

}
