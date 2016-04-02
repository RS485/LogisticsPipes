package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDInvSysConnector;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IDirectRoutingConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.orderer.OrdererManagerContent;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.TransportInvConnection;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public class PipeItemsInvSysConnector extends CoreRoutedPipe implements IDirectRoutingConnection, IHeadUpDisplayRendererProvider, IOrderManagerContentReceiver {

	private boolean init = false;
	private HashMap<ItemIdentifier, List<ItemRoutingInformation>> itemsOnRoute = new HashMap<ItemIdentifier, List<ItemRoutingInformation>>();
	public ItemIdentifierInventory inv = new ItemIdentifierInventory(1, "Freq. card", 1);
	public int resistance;
	public Set<ItemIdentifierStack> oldList = new TreeSet<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private HUDInvSysConnector HUD = new HUDInvSysConnector(this);
	private UUID idbuffer = UUID.randomUUID();

	public PipeItemsInvSysConnector(Item item) {
		super(new TransportInvConnection(), item);
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (!init) {
			if (hasConnectionUUID()) {
				if (!SimpleServiceLocator.connectionManager.addDirectConnection(getConnectionUUID(), getRouter())) {
					dropFreqCard();
				}
				CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
				if (CRP != null) {
					CRP.refreshRender(true);
				}
				getRouter().update(true, this);
				refreshRender(true);
				init = true;
				idbuffer = getConnectionUUID();
			}
		}
		if (init && !hasConnectionUUID()) {
			init = false;
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if (CRP != null) {
				CRP.refreshRender(true);
			}
		}
		if (init && idbuffer != null && !idbuffer.equals(getConnectionUUID())) {
			init = false;
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if (CRP != null) {
				CRP.refreshRender(true);
			}
		}
		if (itemsOnRoute.size() > 0) {
			checkConnectedInvs();
		}
	}

	private void checkConnectedInvs() {
		if (!itemsOnRoute.isEmpty()) { // don't check the inventory if you don't want anything

			WorldUtil wUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
			for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)) {
				if(!isConnectedInv(tile.tile)) continue;
				if (tile.tile instanceof IInventory) {
					IInventory inv = InventoryHelper.getInventory((IInventory) tile.tile);
					if (inv instanceof net.minecraft.inventory.ISidedInventory) {
						inv = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) inv, tile.orientation.getOpposite(), false);
					}
					IInventoryUtil access = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv, tile.orientation.getOpposite());
					if (checkOneConnectedInv(access, tile.orientation)) {
						updateContentListener();
						break;
					}
				}
			}
		}
	}

	private boolean checkOneConnectedInv(IInventoryUtil inv, ForgeDirection dir) {
		boolean contentchanged = false;
		if (!itemsOnRoute.isEmpty()) { // don't check the inventory if you don't want anything
			List<ItemIdentifier> items = new ArrayList<ItemIdentifier>(itemsOnRoute.keySet());
			items.retainAll(inv.getItems());
			Map<ItemIdentifier, Integer> amounts = null;
			if (!items.isEmpty()) {
				amounts = inv.getItemsAndCount();
			}
			for (ItemIdentifier ident : items) {
				if (!amounts.containsKey(ident)) {
					continue;
				}
				int itemAmount = amounts.get(ident);
				List<ItemRoutingInformation> needs = itemsOnRoute.get(ident);
				for (Iterator<ItemRoutingInformation> iterator = needs.iterator(); iterator.hasNext();) {
					ItemRoutingInformation need = iterator.next();
					if (need.getItem().getStackSize() <= itemAmount) {
						if (!useEnergy(6)) {
							return contentchanged;
						}
						ItemStack toSend = inv.getMultipleItems(ident, need.getItem().getStackSize());
						if (toSend == null) {
							return contentchanged;
						}
						if (toSend.stackSize != need.getItem().getStackSize()) {
							if (inv instanceof ITransactor) {
								((ITransactor) inv).add(toSend, dir.getOpposite(), true);
							} else {
								container.getWorldObj().spawnEntityInWorld(ItemIdentifierStack.getFromStack(toSend).makeEntityItem(getWorld(), container.xCoord, container.yCoord, container.zCoord));
							}
							new UnsupportedOperationException("The extracted amount didn't match the requested one. (" + inv + ")").printStackTrace();
							return contentchanged;
						}
						sendStack(need, dir);

						iterator.remove(); // finished with this need, we sent part of a stack, lets see if anyone where needs the current item type.
						contentchanged = true;
						if (needs.isEmpty()) {
							itemsOnRoute.remove(ident);
						}

						//Refresh Available Items
						amounts = inv.getItemsAndCount();
						if (amounts.containsKey(ident)) {
							itemAmount = amounts.get(ident);
						} else {
							itemAmount = 0;
							break;
						}
					}
				}
			}
		}
		return contentchanged;
	}

	public void sendStack(ItemRoutingInformation info, ForgeDirection dir) {
		IRoutedItem itemToSend = SimpleServiceLocator.routedItemHelper.createNewTravelItem(info);
		super.queueRoutedItem(itemToSend, dir);
		spawnParticle(Particles.OrangeParticle, 4);
	}

	@SuppressWarnings("deprecation")
	private UUID getConnectionUUID() {
		if (inv != null) {
			if (inv.getStackInSlot(0) != null) {
				if (inv.getStackInSlot(0).hasTagCompound()) {
					if (inv.getStackInSlot(0).getTagCompound().hasKey("UUID")) {
						return UUID.fromString(inv.getStackInSlot(0).getTagCompound().getString("UUID"));
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private boolean hasConnectionUUID() {
		if (inv != null) {
			if (inv.getStackInSlot(0) != null) {
				if (inv.getStackInSlot(0).hasTagCompound()) {
					if (inv.getStackInSlot(0).getTagCompound().hasKey("UUID")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void dropFreqCard() {
		if (inv.getStackInSlot(0) == null) {
			return;
		}
		EntityItem item = new EntityItem(getWorld(), getX(), getY(), getZ(), inv.getStackInSlot(0));
		getWorld().spawnEntityInWorld(item);
		inv.clearInventorySlotContents(0);
	}

	public Set<ItemIdentifierStack> getExpectedItems() {
		// got to be a TreeMap, because a TreeSet doesn't have the ability to retrieve the key.
		Set<ItemIdentifierStack> list = new TreeSet<ItemIdentifierStack>();
		for (Entry<ItemIdentifier, List<ItemRoutingInformation>> entry : itemsOnRoute.entrySet()) {
			if (entry.getValue().isEmpty()) {
				continue;
			}
			ItemIdentifierStack currentStack = new ItemIdentifierStack(entry.getKey(), 0);
			for (ItemRoutingInformation e : entry.getValue()) {
				currentStack.setStackSize(currentStack.getStackSize() + e.getItem().getStackSize());
			}
			list.add(currentStack);
		}
		return list;
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Inv_Sys_Connector_ID, getWorld(), getX(), getY(), getZ());
	}

	@Override
	public void onAllowedRemoval() {
		if (!stillNeedReplace) {
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if (CRP != null) {
				CRP.refreshRender(true);
			}
		}
		dropFreqCard();
	}

	@Override
	public void invalidate() {
		if (!stillNeedReplace) {
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if (CRP != null) {
				CRP.refreshRender(true);
			}
		}
		init = false;
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		if (!stillNeedReplace) {
			CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
			SimpleServiceLocator.connectionManager.removeDirectConnection(getRouter());
			if (CRP != null) {
				CRP.refreshRender(true);
			}
		}
		init = false;
		super.onChunkUnload();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		inv.writeToNBT(nbttagcompound, "");
		nbttagcompound.setInteger("resistance", resistance);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		inv.readFromNBT(nbttagcompound, "");
		resistance = nbttagcompound.getInteger("resistance");
	}

	private boolean hasRemoteConnection() {
		return hasConnectionUUID() && getWorld() != null && SimpleServiceLocator.connectionManager.hasDirectConnection(getRouter());
	}

	private boolean inventoryConnected() {
		for (int i = 0; i < 6; i++) {
			LPPosition p = new LPPosition(getX(), getY(), getZ());
			ForgeDirection dir = ForgeDirection.values()[i];
			p.moveForward(dir);
			TileEntity tile = p.getTileEntity(getWorld());
			if (tile instanceof IInventory && this.container.canPipeConnect(tile, dir)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TextureType getCenterTexture() {
		if (!stillNeedReplace && hasRemoteConnection()) {
			if (inventoryConnected()) {
				return Textures.LOGISTICSPIPE_INVSYSCON_CON_TEXTURE;
			} else {
				return Textures.LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE;
			}
		}
		return Textures.LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}

	@Override
	public int getConnectionResistance() {
		return resistance;
	}

	@Override
	public void addItem(ItemRoutingInformation info) {
		if (info.getItem() != null && info.getItem().getStackSize() > 0 && info.destinationint >= 0) {
			ItemIdentifier insertedType = info.getItem().getItem();
			List<ItemRoutingInformation> entry = itemsOnRoute.get(insertedType);
			if (entry == null) {
				entry = new LinkedList<ItemRoutingInformation>(); // linked list as this is almost always very small, but experiences random removal
				itemsOnRoute.put(insertedType, entry);
			}
			entry.add(info);
			updateContentListener();
		}
	}

	public boolean isConnectedInv(TileEntity tile) {
		for (int i = 0; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.values()[i];
			LPPosition p = new LPPosition(getX(), getY(), getZ());
			p.moveForward(dir);
			TileEntity lTile = p.getTileEntity(getWorld());
			if (lTile instanceof IInventory) {
				if (lTile == tile) {
					return this.container.canPipeConnect(lTile, dir);
				}
			}
		}
		return false;
	}

	public void handleItemEnterInv(ItemRoutingInformation info, TileEntity tile) {
		if (info.getItem().getStackSize() == 0) {
			return; // system.throw("why you try to insert empty stack?");
		}
		if (isConnectedInv(tile)) {
			if (hasRemoteConnection()) {
				CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(getRouter());
				if (CRP instanceof IDirectRoutingConnection) {
					IDirectRoutingConnection pipe = (IDirectRoutingConnection) CRP;
					pipe.addItem(info);
					spawnParticle(Particles.OrangeParticle, 4);
				}
			}
		}
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}

	private void updateContentListener() {
		if (!localModeWatchers.isEmpty()) {
			Set<ItemIdentifierStack> newList = getExpectedItems();
			if (!newList.equals(oldList)) {
				oldList = newList;
				MainProxy.sendToPlayerList(PacketHandler.getPacket(OrdererManagerContent.class).setIdentSet(newList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
			}
		}
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if (mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererManagerContent.class).setIdentSet(getExpectedItems()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), player);
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
	public void setOrderManagerContent(Collection<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}

}
