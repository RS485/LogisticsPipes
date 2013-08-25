package logisticspipes.pipes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import buildcraft.core.network.TileNetworkData;
import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDSatellite;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequireReliableFluidTransport;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleSatelite;
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
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.Pair;
import logisticspipes.utils.PlayerCollectionList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.common.network.Player;

public class PipeFluidSatellite extends FluidRoutedPipe implements IRequestFluid, IRequireReliableFluidTransport, IHeadUpDisplayRendererProvider, IChestContentReceiver {

	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	public final LinkedList<ItemIdentifierStack> itemList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	private final HUDSatellite HUD = new HUDSatellite(this);
	
	public PipeFluidSatellite(int itemID) {
		super(itemID);
		throttleTime = 40;
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
	public LogisticsModule getLogisticsModule() {
		return new ModuleSatelite(this);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if(getWorld().getWorldTime() % 20 == 0 && localModeWatchers.size() > 0) {
			updateInv(false);
		}
	}

	@Override
	public void sendFailed(FluidIdentifier liquid, Integer amount) {
		liquidLost(liquid, amount);
	}
	
	private void addToList(ItemIdentifierStack stack) {
		for(ItemIdentifierStack ident:itemList) {
			if(ident.getItem().equals(stack.getItem())) {
				ident.stackSize += stack.stackSize;
				return;
			}
		}
		itemList.addLast(stack);
	}

	private void updateInv(boolean force) {
		itemList.clear();
		for(Pair<TileEntity, ForgeDirection> pair:getAdjacentTanks(false)) {
			if(!(pair.getValue1() instanceof IFluidHandler)) continue;
			IFluidHandler tankContainer = (IFluidHandler) pair.getValue1();
			FluidTankInfo[] tanks = tankContainer.getTankInfo(pair.getValue2().getOpposite());
			for(FluidTankInfo tank: tanks) {
				FluidStack liquid = tank.fluid;
				if(liquid != null) {
					addToList(FluidIdentifier.get(liquid).getItemIdentifier().makeStack(liquid.amount));
				}
			}
		}
		if(!itemList.equals(oldList) || force) {
			oldList.clear();
			oldList.addAll(itemList);
//TODO 		MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, getX(), getY(), getZ(), itemList).getPacket(), localModeWatchers);
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
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, getX(), getY(), getZ(), 1).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, getX(), getY(), getZ(), 1).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}
	
	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID((this).satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToPlayer(packet, (Player)player);
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

// from baseLogicLiquidSatellite
	public static HashSet<PipeFluidSatellite> AllSatellites = new HashSet<PipeFluidSatellite>();

	// called only on server shutdown
	public static void cleanup() {
		AllSatellites.clear();
	}
	
	protected final Map<FluidIdentifier, Integer> _lostItems = new HashMap<FluidIdentifier, Integer>();

	@TileNetworkData
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
		if(MainProxy.isClient(this.getWorld())) return satelliteId;
		int potentialId = satelliteId;
		boolean conflict = true;
		while (conflict) {
			potentialId += increment;
			if (potentialId < 0) {
				return 0;
			}
			conflict = false;
			for (final PipeFluidSatellite sat : AllSatellites) {
				if (sat.satelliteId == potentialId) {
					conflict = true;
					break;
				}
			}
		}
		return potentialId;
	}

	protected void ensureAllSatelliteStatus() {
		if(MainProxy.isClient()) return;
		if (satelliteId == 0 && AllSatellites.contains(this)) {
			AllSatellites.remove(this);
		}
		if (satelliteId != 0 && !AllSatellites.contains(this)) {
			AllSatellites.add(this);
		}
	}

	public void setNextId(EntityPlayer player) {
		satelliteId = findId(1);
		ensureAllSatelliteStatus();
		if (MainProxy.isClient(player.worldObj)) {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeNext.class).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToServer(packet);
		} else {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToPlayer(packet, (Player)player);
		}
		updateWatchers();
	}

	public void setPrevId(EntityPlayer player) {
		satelliteId = findId(-1);
		ensureAllSatelliteStatus();
		if (MainProxy.isClient(player.worldObj)) {
			final ModernPacket packet = PacketHandler
					.getPacket(SatPipePrev.class).setPosX(getX())
					.setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToServer(packet);
		} else {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToPlayer(packet,(Player) player);
		}
		updateWatchers();
	}

	
	private void updateWatchers() {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), ((PipeFluidSatellite)this.container.pipe).localModeWatchers);
	}
	

	@Override
	public void onAllowedRemoval() {
		if(MainProxy.isClient(this.getWorld())) return;
		if (AllSatellites.contains(this)) {
			AllSatellites.remove(this);
		}
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if (MainProxy.isServer(entityplayer.worldObj)) {
			// Send the satellite id when opening gui
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
//TODO Must be handled manualy
			MainProxy.sendPacketToPlayer(packet, (Player)entityplayer);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SatelitePipe_ID, getWorld(), getX(), getY(), getZ());
		}
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
			
			if(received > 0) {
				if(received == stack.getValue()) {
					iterator.remove();
				} else {
					stack.setValue(stack.getValue() - received);
				}
			}
		}
	}

	public void setSatelliteId(int integer) {
		satelliteId = integer;
	}

	@Override
	public void liquidLost(FluidIdentifier item, int amount) {
		if(_lostItems.containsKey(item)) {
			_lostItems.put(item, _lostItems.get(item) + amount);
		} else {
			_lostItems.put(item, amount);
		}
	}

	@Override
	public void liquidArrived(FluidIdentifier item, int amount) {}

	@Override
	public void liquidNotInserted(FluidIdentifier item, int amount) {
		this.liquidLost(item, amount);
	}
}
