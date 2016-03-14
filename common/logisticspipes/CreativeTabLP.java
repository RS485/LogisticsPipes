package logisticspipes;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabLP extends CreativeTabs {

	public CreativeTabLP() {
		super("Logistics_Pipes");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		return LogisticsPipes.LogisticsBasicPipe;
	}
}
