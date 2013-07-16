package logisticspipes.pipes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.HUDSatellite;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.interfaces.routing.IRequireReliableLiquidTransport;
import logisticspipes.logic.BaseLogicLiquidSatellite;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleSatelite;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.hud.ChestContent;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.satpipe.SatPipeSetID;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;
import logisticspipes.utils.PlayerCollectionList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import cpw.mods.fml.common.network.Player;

public class PipeLiquidSatellite extends LiquidRoutedPipe implements IRequestLiquid, IHeadUpDisplayRendererProvider, IChestContentReceiver {

	public final List<EntityPlayer> localModeWatchers = new PlayerCollectionList();
	public final LinkedList<ItemIdentifierStack> itemList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	private final HUDSatellite HUD = new HUDSatellite(this);
	
	public PipeLiquidSatellite(int itemID) {
		super(new BaseLogicLiquidSatellite(), itemID);
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
		if(worldObj.getWorldTime() % 20 == 0 && localModeWatchers.size() > 0) {
			updateInv(false);
		}
	}

	@Override
	public void sendFailed(LiquidIdentifier liquid, Integer amount) {
		if(logic instanceof IRequireReliableLiquidTransport) {
			((IRequireReliableLiquidTransport)logic).liquidLost(liquid, amount);
		}
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
			if(!(pair.getValue1() instanceof ITankContainer)) continue;
			ITankContainer tankContainer = (ITankContainer) pair.getValue1();
			ILiquidTank[] tanks = tankContainer.getTanks(pair.getValue2().getOpposite());
			for(ILiquidTank tank: tanks) {
				LiquidStack liquid = tank.getLiquid();
				if(liquid != null) {
					addToList(LiquidIdentifier.get(liquid).getItemIdentifier().makeStack(liquid.amount));
				}
			}
		}
		if(!itemList.equals(oldList) || force) {
			oldList.clear();
			oldList.addAll(itemList);
//TODO 		MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, getX(), getY(), getZ(), itemList).getPacket(), localModeWatchers);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ChestContent.class).setIdentList(itemList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()).getPacket(), localModeWatchers);
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
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()).getPacket());
	}

	@Override
	public void stopWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, getX(), getY(), getZ(), 1).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()).getPacket());
	}
	
	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(((BaseLogicLiquidSatellite)this.logic).satelliteId).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
//TODO Must be handled manualy
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
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
}
