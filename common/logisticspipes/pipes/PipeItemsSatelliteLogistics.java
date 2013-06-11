/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.HUDSatellite;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleSatelite;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.oldpackets.PacketPipeInteger;
import logisticspipes.network.oldpackets.PacketPipeInvContent;
import logisticspipes.network.packets.satpipe.SatPipeSetID;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SidedInventoryForgeAdapter;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class PipeItemsSatelliteLogistics extends CoreRoutedPipe implements IRequestItems, IHeadUpDisplayRendererProvider, IChestContentReceiver {
	
	public final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	public final LinkedList<ItemIdentifierStack> itemList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	private final HUDSatellite HUD = new HUDSatellite(this);
	
	public PipeItemsSatelliteLogistics(int itemID) {
		super(new BaseLogicSatellite(), itemID);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_SATELLITE_TEXTURE;
	}

	@Override
	public void enabledUpdateEntity() {
		if(worldObj.getWorldTime() % 20 == 0 && localModeWatchers.size() > 0) {
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
	public void startWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING, getX(), getY(), getZ(), 1).getPacket());
	}

	@Override
	public void stopWaitching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_STOP_WATCHING, getX(), getY(), getZ(), 1).getPacket());
	}
	
	private IInventory getRawInventory(ForgeDirection ori) {
		Position pos = new Position(this.getX(), this.getY(), this.getZ(), ori);
		pos.moveForwards(1);
		TileEntity tile = this.worldObj.getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);
		if (tile instanceof TileGenericPipe) return null;
		if (!(tile instanceof IInventory)) return null;
		return InventoryHelper.getInventory((IInventory) tile);
	}
	
	private IInventory getInventory(ForgeDirection ori) {
		IInventory rawInventory = getRawInventory(ori);
		if (rawInventory instanceof net.minecraft.inventory.ISidedInventory) return new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) rawInventory, ori.getOpposite(), false);
		if (rawInventory instanceof net.minecraftforge.common.ISidedInventory) return new SidedInventoryForgeAdapter((net.minecraftforge.common.ISidedInventory) rawInventory, ori.getOpposite());
		return rawInventory;
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
		for(ForgeDirection ori:ForgeDirection.values()) {
			IInventory inv = getInventory(ori);
			if(inv != null) {
				for(int i=0;i<inv.getSizeInventory();i++) {
					if(inv.getStackInSlot(i) != null) {
						addToList(ItemIdentifierStack.GetFromStack(inv.getStackInSlot(i)));
					}
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
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(((BaseLogicSatellite)this.logic).satelliteId).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
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

	@Override
	public void setReceivedChestContent(Collection<ItemIdentifierStack> list) {
		itemList.clear();
		itemList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}

}
