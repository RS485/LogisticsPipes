package logisticspipes.items;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.SetCurrentPagePacket;
import logisticspipes.proxy.MainProxy;

public class ItemGuideBook extends LogisticsItem {

	public ItemGuideBook() {
		this.setMaxStackSize(1);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("page", 1);
		nbt.setFloat("sliderProgress", 0.0F);
	}

<<<<<<< feature/custom-guide-book
	public static void setCurrentPage(int slot, EntityPlayer player, NBTTagCompound nbt){
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SetCurrentPagePacket.class).setNbt(nbt).setSlot(slot), player);
=======
	public static void setCurrentPage(int page, float sliderProgress, EnumHand hand) {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(SetCurrentPagePacket.class).setHand(hand).setSliderProgress(sliderProgress).setPage(page));
>>>>>>> Remade some key parts
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
<<<<<<< feature/custom-guide-book
<<<<<<< feature/custom-guide-book
		if (world.isRemote) MainProxy.proxy.openGuideBookGui(hand);
=======
		if (world.isRemote) MainProxy.proxy.openGuiFromItem(GuiIDs.GUI_Guide_Book_ID, hand);
>>>>>>> Remade some key parts
=======
		if (world.isRemote) MainProxy.proxy.openGuideBookGui(hand);
>>>>>>> Ready for Review
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}
}
