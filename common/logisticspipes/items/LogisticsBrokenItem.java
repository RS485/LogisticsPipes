package logisticspipes.items;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.utils.string.StringUtils;

public class LogisticsBrokenItem extends LogisticsItem implements IItemAdvancedExistance {

	private static final String PREFIX = "tooltip.brokenItem.";

	@Override
	public boolean canExistInNormalInventory(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public boolean canExistInWorld(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(" - " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "1"));
		tooltip.add(" - " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "2"));
		tooltip.add("    " + StringUtils.translate(LogisticsBrokenItem.PREFIX + "3"));
	}
}
