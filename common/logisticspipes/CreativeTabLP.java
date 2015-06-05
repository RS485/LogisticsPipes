package logisticspipes;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
