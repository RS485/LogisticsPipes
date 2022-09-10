package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.gui.hud.HUDInvSysConnector;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IChannelManager;
import logisticspipes.interfaces.routing.IChannelRoutingConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.guis.pipe.InvSysConGuiProvider;
import logisticspipes.network.packets.gui.ChannelInformationPacket;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.orderer.OrdererManagerContent;
import logisticspipes.network.packets.pipe.InvSysConResistance;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.TransportInvConnection;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import network.rs485.logisticspipes.connection.LPNeighborTileEntityKt;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

public class PipeItemsInvSysConnector extends CoreRoutedPipe implements IChannelRoutingConnection, IHeadUpDisplayRendererProvider, IOrderManagerContentReceiver,
		IGuiOpenControler {

	private boolean init = false;
	private HashMap<ItemIdentifier, List<ItemRoutingInformation>> itemsOnRoute = new HashMap<>();
	public int resistance;
	public Set<ItemIdentifierStack> oldList = new TreeSet<>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<>();
	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	public final PlayerCollectionList localGuiWatchers = new PlayerCollectionList();
	private HUDInvSysConnector HUD = new HUDInvSysConnector(this);
	private UUID idbuffer = UUID.randomUUID();

	private UUID connectedChannel;

	public PipeItemsInvSysConnector(Item item) {
		super(new TransportInvConnection(), item);
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (!init) {
			if (hasConnectionUUID()) {
				if (!SimpleServiceLocator.connectionManager.addChannelConnection(getConnectionUUID(), getRouter())) {
					connectedChannel = null;
					sendChannelInformationToPlayers();
				}
				List<CoreRoutedPipe> connectedPipes = SimpleServiceLocator.connectionManager.getConnectedPipes(getRouter());
				if (connectedPipes != null) {
					connectedPipes.forEach(c -> {
						c.getRouter().update(true, c);
						c.refreshRender(true);
					});
				}
				getRouter().update(true, this);
				refreshRender(true);
				init = true;
				idbuffer = getConnectionUUID();
			}
		}
		if (init && !hasConnectionUUID()) {
			init = false;
			List<CoreRoutedPipe> connectedPipes = SimpleServiceLocator.connectionManager.getConnectedPipes(getRouter());
			SimpleServiceLocator.connectionManager.removeChannelConnection(getRouter());
			if (connectedPipes != null) {
				connectedPipes.forEach(c -> {
					c.getRouter().update(true, c);
					c.refreshRender(true);
				});
			}
		}
		if (init && idbuffer != null && !idbuffer.equals(getConnectionUUID())) {
			init = false;
			List<CoreRoutedPipe> connectedPipes = SimpleServiceLocator.connectionManager.getConnectedPipes(getRouter());
			SimpleServiceLocator.connectionManager.removeChannelConnection(getRouter());
			if (connectedPipes != null) {
				connectedPipes.forEach(c -> {
					c.getRouter().update(true, c);
					c.refreshRender(true);
				});
			}
		}
		if (itemsOnRoute.size() > 0) {
			checkConnectedInvs();
		}
	}

	private void checkConnectedInvs() {
		if (!itemsOnRoute.isEmpty()) { // don't check the inventory if you don't want anything
			final boolean shouldUpdate = getAvailableAdjacent().inventories().stream()
					.anyMatch(neighbor -> {
						final IInventoryUtil invUtil = LPNeighborTileEntityKt.getInventoryUtil(neighbor);
						return invUtil != null &&
								container.canPipeConnect(neighbor.getTileEntity(), neighbor.getDirection()) &&
								checkOneConnectedInv(invUtil, neighbor.getDirection());
					});

			if (shouldUpdate) updateContentListener();
		}
	}

	private boolean checkOneConnectedInv(@Nonnull IInventoryUtil inv, EnumFacing dir) {
		boolean contentchanged = false;
		if (!itemsOnRoute.isEmpty()) { // don't check the inventory if you don't want anything
			List<ItemIdentifier> items = new ArrayList<>(itemsOnRoute.keySet());
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
				for (Iterator<ItemRoutingInformation> iterator = needs.iterator(); iterator.hasNext(); ) {
					ItemRoutingInformation need = iterator.next();
					if (need.getItem().getStackSize() <= itemAmount) {
						if (!useEnergy(6)) {
							return contentchanged;
						}
						ItemStack toSend = inv.getMultipleItems(ident, need.getItem().getStackSize());
						if (toSend.isEmpty()) {
							return contentchanged;
						}
						if (toSend.getCount() != need.getItem().getStackSize()) {
							if (inv instanceof ITransactor) {
								((ITransactor) inv).add(toSend, dir.getOpposite(), true);
							} else {
								container.getWorld().spawnEntity(ItemIdentifierStack.getFromStack(toSend).makeEntityItem(getWorld(), container.getX(), container.getY(), container.getZ()));
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
							break;
						}
					}
				}
			}
		}
		return contentchanged;
	}

	public void sendStack(ItemRoutingInformation info, EnumFacing dir) {
		IRoutedItem itemToSend = SimpleServiceLocator.routedItemHelper.createNewTravelItem(info);
		super.queueRoutedItem(itemToSend, dir);
		spawnParticle(Particles.OrangeParticle, 4);
	}

	private static UUID testUUID = UUID.randomUUID();

	private UUID getConnectionUUID() {
		return connectedChannel;
	}

	private boolean hasConnectionUUID() {
		return connectedChannel != null;
	}

	public Set<ItemIdentifierStack> getExpectedItems() {
		// got to be a TreeMap, because a TreeSet doesn't have the ability to retrieve the key.
		Set<ItemIdentifierStack> list = new TreeSet<>();
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
		NewGuiHandler.getGui(InvSysConGuiProvider.class).setTilePos(this.container).open(entityplayer);
	}

	@Override
	public void onAllowedRemoval() {
		removePipeFromChannel();
	}

	private void removePipeFromChannel() {
		if (!stillNeedReplace) {
			List<CoreRoutedPipe> connectedPipes = SimpleServiceLocator.connectionManager.getConnectedPipes(getRouter());
			SimpleServiceLocator.connectionManager.removeChannelConnection(getRouter());
			if (connectedPipes != null) {
				connectedPipes.forEach(c -> c.refreshRender(true));
			}
		}
	}

	@Override
	public void invalidate() {
		removePipeFromChannel();
		init = false;
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		removePipeFromChannel();
		init = false;
		super.onChunkUnload();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("resistance", resistance);
		if (connectedChannel != null) {
			nbttagcompound.setString("connectedChannel", connectedChannel.toString());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		resistance = nbttagcompound.getInteger("resistance");
		if (nbttagcompound.hasKey("connectedChannel")) {
			connectedChannel = UUID.fromString(nbttagcompound.getString("connectedChannel"));
		} else {
			connectedChannel = null;
		}
	}

	private boolean hasRemoteConnection() {
		return hasConnectionUUID() && getWorld() != null && SimpleServiceLocator.connectionManager.hasChannelConnection(getRouter());
	}

	private boolean isInventoryConnected(@Nullable TileEntity tileEntityFilter) {
		return new WorldCoordinatesWrapper(this.container)
				.allNeighborTileEntities().stream()
				.anyMatch(neighbor -> (tileEntityFilter == null || neighbor.getTileEntity() == tileEntityFilter) &&
						neighbor.canHandleItems() &&
						this.container.canPipeConnect(neighbor.getTileEntity(), neighbor.getDirection()));
	}

	@Override
	public TextureType getCenterTexture() {
		if (!stillNeedReplace && hasRemoteConnection()) {
			if (isInventoryConnected(null)) {
				return Textures.LOGISTICSPIPE_INVSYSCON_CON_TEXTURE;
			} else {
				return Textures.LOGISTICSPIPE_INVSYSCON_MIS_TEXTURE;
			}
		}
		return Textures.LOGISTICSPIPE_INVSYSCON_DIS_TEXTURE;
	}

	@Override
	public @Nullable LogisticsModule getLogisticsModule() {
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
			List<ItemRoutingInformation> entry = itemsOnRoute.computeIfAbsent(insertedType, k -> new LinkedList<>());
			// linked list as this is almost always very small, but experiences random removal
			entry.add(info);
			updateContentListener();
		}
	}

	public void handleItemEnterInv(ItemRoutingInformation info, TileEntity tile) {
		if (info.getItem().getStackSize() == 0) {
			return; // system.throw("why you try to insert empty stack?");
		}
		if (info.destinationint < 0) {
			return; // The item does not have a destination anymore, maybe the target pipe has been removed... We cannot do anything anymore so just let it be.
		}
		if (isInventoryConnected(tile)) {
			if (hasRemoteConnection()) {
				List<CoreRoutedPipe> connectedPipes = SimpleServiceLocator.connectionManager.getConnectedPipes(getRouter());
				Optional<CoreRoutedPipe> bestConnection = connectedPipes.stream()
						.map(con -> new Triplet<>(
								con,
								con.getRouter().getExitFor(info.destinationint, info._transportMode == IRoutedItem.TransportMode.Active, info.getItem().getItem()),
								con.getRouter().getExitFor(getRouterId(), info._transportMode == IRoutedItem.TransportMode.Active, info.getItem().getItem())
						))
						.filter(triplet -> triplet.getValue2() != null && triplet.getValue3() != null)
						.filter(triplet -> triplet.getValue2().exitOrientation != triplet.getValue3().exitOrientation)
						.min(Comparator.comparing(trip -> trip.getValue2().blockDistance)).map(Pair::getValue1);
				if (!bestConnection.isPresent()) {
					bestConnection = connectedPipes.stream()
							.map(con -> new Pair<>(
									con,
									con.getRouter().getExitFor(info.destinationint, info._transportMode == IRoutedItem.TransportMode.Active, info.getItem().getItem())
							))
							.filter(triplet -> triplet.getValue2() != null)
							.min(Comparator.comparing(trip -> trip.getValue2().blockDistance)).map(Pair::getValue1);
				}
				if (bestConnection.isPresent() && bestConnection.get() instanceof IChannelRoutingConnection) {
					IChannelRoutingConnection pipe = (IChannelRoutingConnection) bestConnection.get();
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

	public void setChannelFromClient(UUID fromString) {
		this.connectedChannel = fromString;
		sendChannelInformationToPlayers();
	}

	@Override
	public void guiOpenedByPlayer(EntityPlayer player) {
		localGuiWatchers.add(player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(InvSysConResistance.class).setInteger(this.resistance).setBlockPos(this.getPos()), player);

		IChannelManager manager = SimpleServiceLocator.channelManagerProvider.getChannelManager(this.getWorld());
		Optional<ChannelInformation> channel = manager.getChannels().stream()
				.filter(chan -> chan.getChannelIdentifier().equals(getConnectionUUID()))
				.findFirst();
		channel.ifPresent(chan -> MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ChannelInformationPacket.class).setInformation(chan).setTargeted(true), player));
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		localGuiWatchers.remove(player);
	}

	private void sendChannelInformationToPlayers() {
		IChannelManager manager = SimpleServiceLocator.channelManagerProvider.getChannelManager(this.getWorld());
		Optional<ChannelInformation> channel = manager.getChannels().stream()
				.filter(chan -> chan.getChannelIdentifier().equals(getConnectionUUID()))
				.findFirst();
		channel.ifPresent(chan -> MainProxy.sendToPlayerList(PacketHandler.getPacket(ChannelInformationPacket.class).setInformation(chan).setTargeted(true), localGuiWatchers));
	}
}
