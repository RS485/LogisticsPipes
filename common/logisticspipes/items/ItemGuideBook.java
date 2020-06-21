package logisticspipes.items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import logisticspipes.gui.GuiGuideBook;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.SetCurrentPagePacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.GuideBookContents;

public class ItemGuideBook extends LogisticsItem {

	public ItemGuideBook() {
		this.setMaxStackSize(1);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("page", 1);
		nbt.setFloat("sliderProgress", 0.0F);
	}

	public static void setCurrentPage(int page, float sliderProgress, EnumHand hand) {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(SetCurrentPagePacket.class).setHand(hand).setSliderProgress(sliderProgress).setPage(page));
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote) {
			if (openGuideBook(hand) != null) return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}
		return new ActionResult<>(EnumActionResult.PASS, itemstack);
	}

	@Nullable
	public static GuideBookContents openGuideBook(@Nonnull EnumHand hand) {
		Minecraft mc = Minecraft.getMinecraft();
		GuideBookContents gbc = GuideBookContents.load();
		if (gbc != null) {
			mc.displayGuiScreen(new GuiGuideBook(hand, gbc));
		}
		return gbc;
	}
}
