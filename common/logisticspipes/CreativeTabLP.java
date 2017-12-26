package logisticspipes;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabLP extends CreativeTabs {

	public CreativeTabLP() {
		super("Logistics_Pipes");
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(LogisticsPipes.LogisticsBasicPipe);
	}
}
