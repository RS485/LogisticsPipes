package logisticspipes.interfaces;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILogisticsItem {

	@SideOnly(Side.CLIENT)
	void registerModels();
}
