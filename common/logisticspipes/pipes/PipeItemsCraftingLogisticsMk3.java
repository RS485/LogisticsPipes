package logisticspipes.pipes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.HUDCraftingMK3;
import logisticspipes.interfaces.IChestContentReceiver;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.ModuleCrafterMK3;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.ChestContent;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.order.IOrderInfoProvider.RequestType;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.CraftingPipeMk3Transport;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.Player;

public class PipeItemsCraftingLogisticsMk3 extends PipeItemsCraftingLogisticsMk2 implements ISimpleInventoryEventHandler, IChestContentReceiver {
	
	private HUDCraftingMK3 HUD = new HUDCraftingMK3(this);
	
	public PipeItemsCraftingLogisticsMk3(int itemID) {
		super(new CraftingPipeMk3Transport(), itemID);
		craftingModule=new ModuleCrafterMK3(this);
		((CraftingPipeMk3Transport)transport).pipe = this;
	}
	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
	}

	@Override
	public void onAllowedRemoval() {
		super.onAllowedRemoval();
		craftingModule.onAllowedRemoval();
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CRAFTERMK3_TEXTURE;
	}

	/*
	@Override
	public void InventoryChanged(IInventory inventory) {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ChestContent.class).setIdentList(ItemIdentifierStack.getListFromInventory(inv, true)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
	}
	
	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		super.playerStartWatching(player, mode);
		if(mode == 1) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ChestContent.class).setIdentList(ItemIdentifierStack.getListFromInventory(inv, true)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
		}
	}

	@Override
	public void setReceivedChestContent(Collection<ItemIdentifierStack> list) {
		bufferList.clear();
		bufferList.addAll(list);
	}
*/
	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}

	@Override
	public ModuleCrafter getLogisticsModule() {
		return this.craftingModule;
	}
	@Override
	public void setReceivedChestContent(
			Collection<ItemIdentifierStack> _allItems) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void InventoryChanged(IInventory inventory) {
		// TODO Auto-generated method stub
		
	}
	public ModuleCrafterMK3 getMk3Module() {
		return (ModuleCrafterMK3)craftingModule;
	}
}
