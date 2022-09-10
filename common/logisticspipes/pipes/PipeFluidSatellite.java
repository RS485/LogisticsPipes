package logisticspipes.pipes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDSatellite;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
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
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.SatellitePipe;

public class PipeFluidSatellite extends FluidRoutedPipe implements IRequestFluid, IRequireReliableFluidTransport, IHeadUpDisplayRendererProvider, IChestContentReceiver, SatellitePipe {

	// from baseLogicLiquidSatellite
	public static final Set<PipeFluidSatellite> AllSatellites = new HashSet<>();

	// called only on server shutdown
	public static void cleanup() {
		PipeFluidSatellite.AllSatellites.clear();
	}

	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final List<ItemIdentifierStack> itemList = new LinkedList<>();
	private final List<ItemIdentifierStack> oldList = new LinkedList<>();
	private final HUDSatellite HUD = new HUDSatellite(this);
	protected final Map<FluidIdentifier, Integer> _lostItems = new HashMap<>();
	private final ModuleSatellite moduleSatellite;

	@Getter
	private String satellitePipeName = "";

	public PipeFluidSatellite(Item item) {
		super(item);
		throttleTime = 40;
		moduleSatellite = new ModuleSatellite();
		moduleSatellite.registerHandler(this, this);
		moduleSatellite.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return true;
	}

	@Override
	public boolean canInsertToTanks() {
		return true;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_SATELLITE;
	}

	@Override
	public @Nullable LogisticsModule getLogisticsModule() {
		return moduleSatellite;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (isNthTick(20) && localModeWatchers.size() > 0) {
			updateInv(false);
		}
	}

	@Override
	public void sendFailed(FluidIdentifier liquid, Integer amount) {
		liquidLost(liquid, amount);
	}

	private void updateInv(boolean force) {
		itemList.clear();
		itemList.addAll(PipeFluidUtil.INSTANCE.fluidsToItemList(this));

		if (!itemList.equals(oldList) || force) {
			oldList.clear();
			oldList.addAll(itemList);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ChestContent.class).setIdentList(itemList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		}
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
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if (mode == 1) {
			localModeWatchers.add(player);
			final ModernPacket packet = PacketHandler.getPacket(SyncSatelliteNamePacket.class).setString((this).satellitePipeName).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
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
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("satelliteid")) {
			int satelliteId = nbttagcompound.getInteger("satelliteid");
			satellitePipeName = Integer.toString(satelliteId);
		} else {
			satellitePipeName = nbttagcompound.getString("satellitePipeName");
		}
		if (MainProxy.isServer(getWorld())) {
			ensureAllSatelliteStatus();
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setString("satellitePipeName", satellitePipeName);
		super.writeToNBT(nbttagcompound);
	}

	public void ensureAllSatelliteStatus() {
		if (satellitePipeName.isEmpty()) {
			PipeFluidSatellite.AllSatellites.remove(this);
		} else {
			PipeFluidSatellite.AllSatellites.add(this);
		}
	}

	public void updateWatchers() {
		final LogisticsTileGenericPipe container = Objects.requireNonNull(getContainer());
		CoordinatesPacket packet = PacketHandler.getPacket(SyncSatelliteNamePacket.class)
				.setString(satellitePipeName)
				.setTilePos(container);
		MainProxy.sendToPlayerList(packet, localModeWatchers);
		MainProxy.sendPacketToAllWatchingChunk(container, packet);
	}

	@Override
	public void onAllowedRemoval() {
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		PipeFluidSatellite.AllSatellites.remove(this);
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
		final Iterator<Entry<FluidIdentifier, Integer>> iterator = _lostItems.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<FluidIdentifier, Integer> stack = iterator.next();
			int received = RequestTree.requestFluidPartial(stack.getKey(), stack.getValue(), this, null);

			if (received > 0) {
				if (received == stack.getValue()) {
					iterator.remove();
				} else {
					stack.setValue(stack.getValue() - received);
				}
			}
		}
	}

	@Override
	public void liquidLost(FluidIdentifier item, int amount) {
		if (_lostItems.containsKey(item)) {
			_lostItems.put(item, _lostItems.get(item) + amount);
		} else {
			_lostItems.put(item, amount);
		}
	}

	@Override
	public void liquidArrived(FluidIdentifier item, int amount) {}

	@Override
	public void liquidNotInserted(FluidIdentifier item, int amount) {
		liquidLost(item, amount);
	}

	@Override
	public boolean canReceiveFluid() {
		return false;
	}

	@Nonnull
	@Override
	public Set<SatellitePipe> getSatellitesOfType() {
		return Collections.unmodifiableSet(AllSatellites);
	}

	@Override
	public void setSatellitePipeName(@Nonnull String satellitePipeName) {
		this.satellitePipeName = satellitePipeName;
	}

	@Nonnull
	@Override
	public List<ItemIdentifierStack> getItemList() {
		return itemList;
	}
}
