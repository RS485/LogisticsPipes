package logisticspipes.main;

import logisticspipes.LogisticsPipes;
import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreativeTabLP extends CreativeTabs {

	public CreativeTabLP() {
		super("Logistics_Pipes");
	}

    @Override
	@SideOnly(Side.CLIENT)
    public int getTabIconItemIndex() {
        return LogisticsPipes.LogisticsBasicPipe.itemID;
    }
}
