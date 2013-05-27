package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.HUDSatellite;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.interfaces.routing.IRequireReliableLiquidTransport;
import logisticspipes.logic.BaseLogicLiquidSatellite;
import logisticspipes.modules.ModuleSatelite;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.network.packets.PacketPipeInvContent;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import cpw.mods.fml.common.network.Player;

public class PipeLiquidSatelliteLogistics extends LiquidRoutedPipe implements IRequestLiquid, IHeadUpDisplayRendererProvider, IChestContentReceiver {

	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	public final LinkedList<ItemIdentifierStack> itemList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	private final HUDSatellite HUD = new HUDSatellite(this);
	
	public PipeLiquidSatelliteLogistics(int itemID) {
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
	public ILogisticsModule getLogisticsModule() {
		return new ModuleSatelite(this);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void enabledUpdateEntity() {
		if(worldObj.getWorldTime() % 20 == 0 && localModeWatchers.size() > 0) {
			updateInv(false);
		}
	}

	@Override
	public void sendFailed(LiquidIdentifier liquid, Integer amount) {
		if(logic instanceof IRequireReliableLiquidTransport) {
			((IRequireReliableLiquidTransport)logic).itemLost(liquid, amount);
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
			MainProxy.sendToPlayerList(new PacketPipeInvContent(NetworkConstants.PIPE_CHEST_CONTENT, getX(), getY(), getZ(), itemList).getPacket(), localModeWatchers);
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
	public void startWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, getX(), getY(), getZ(), 1).getPacket());
	}

	@Override
	public void stopWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, getX(), getY(), getZ(), 1).getPacket());
	}
	
	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, getX(), getY(), getZ(), ((BaseLogicLiquidSatellite)this.logic).satelliteId).getPacket(), (Player)player);
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
