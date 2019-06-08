package logisticspipes.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.PlayerConfigToClientPacket;
import logisticspipes.network.packets.SetCurrentPagePacket;
import logisticspipes.proxy.MainProxy;

public class ItemGuideBook extends LogisticsItem {

	public ItemGuideBook() {
		this.setMaxStackSize(1);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("page", 1);
		nbt.setFloat("sliderProgress", 0.0F);
	}

	public static void setCurrentPage(int slot, EntityPlayer player, NBTTagCompound nbt){
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SetCurrentPagePacket.class).setNbt(nbt).setSlot(slot), player);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote) {return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);}
		player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Guide_Book_ID, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}
}
