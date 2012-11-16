package logisticspipes.main;

import logisticspipes.LogisticsPipes;
import net.minecraft.src.CreativeTabs;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class CreativeTabLP extends CreativeTabs {

	public CreativeTabLP() {
		super("Logistics_Pipes");
	}

    @SideOnly(Side.CLIENT)
    public int getTabIconItemIndex() {
        return LogisticsPipes.LogisticsBasicPipe.shiftedIndex;
    }
}
