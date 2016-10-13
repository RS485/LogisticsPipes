package logisticspipes.items;

import java.util.List;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LogisticsBrokenItem extends LogisticsItem implements IItemAdvancedExistance {

	private static final String PREFIX = "tooltip.brokenItem.";

	@Override
	public boolean canExistInNormalInventory(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canExistInWorld(ItemStack stack) {
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		par3List.add(" - " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "1"));
		par3List.add(" - " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "2"));
		par3List.add("    " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "3"));
	}
}
