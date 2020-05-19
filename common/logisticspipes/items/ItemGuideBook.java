package logisticspipes.items;

import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import logisticspipes.LPItems;
import logisticspipes.network.PacketHandler;
import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.gui.guidebook.GuiGuideBook;
import network.rs485.logisticspipes.gui.guidebook.SavedPage;
import network.rs485.logisticspipes.network.packets.SetCurrentPagePacket;

public class ItemGuideBook extends LogisticsItem {

	public ItemGuideBook() {
		this.maxStackSize = 1;
	}

	public static void setCurrentPage(ItemStack stack, SavedPage page, ArrayList<SavedPage> tabs, EnumHand hand) {
		if (!stack.isEmpty() && stack.getItem() == LPItems.itemGuideBook) {
			final NBTTagCompound tag = stack.hasTagCompound() ? Objects.requireNonNull(stack.getTagCompound()) : new NBTTagCompound();
			tag.setTag("page", page.toTag());
			NBTTagList bookmarkTagList = new NBTTagList();
			for (SavedPage tab : tabs) bookmarkTagList.appendTag(tab.toTag());
			tag.setTag("bookmarks", bookmarkTagList);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SetCurrentPagePacket.class)
				.setHand(hand)
				.setPage(page)
				.setSavedPages(tabs));
		}
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote) {
			openGuideBook(hand);
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}
		return new ActionResult<>(EnumActionResult.PASS, itemstack);
	}

	public static void openGuideBook(@Nonnull EnumHand hand) {
		Minecraft mc = Minecraft.getMinecraft();
		final GuiGuideBook guideBook = new GuiGuideBook(hand);
		mc.displayGuiScreen(guideBook);
	}
}
