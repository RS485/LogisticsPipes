package logisticspipes.items;

import java.util.List;

import javax.annotation.Nullable;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

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
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(" - " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "1"));
		tooltip.add(" - " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "2"));
		tooltip.add("    " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "3"));
	}
}
