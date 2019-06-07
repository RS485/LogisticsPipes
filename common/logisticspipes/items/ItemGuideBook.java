package logisticspipes.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;

public class ItemGuideBook extends LogisticsItem{
	public ItemGuideBook(){
		this.setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if(!world.isRemote){return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);}
		player.openGui(LogisticsPipes.instance, GuiIDs.GUI_Guide_Book_ID, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}
}
