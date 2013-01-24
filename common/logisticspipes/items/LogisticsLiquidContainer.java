package logisticspipes.items;

import java.util.List;

import logisticspipes.interfaces.IItemAdvancedExistance;
import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsLiquidContainer extends LogisticsItem implements IItemAdvancedExistance {
	public LogisticsLiquidContainer(int i) {
		super(i);
	}

	@Override
	public boolean canExistInNormalInventory() {
		return false;
	}

	@Override
	public boolean canExistInWorld() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("rawtypes")
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {}
	
	@Override
	public int getItemStackLimit() {
		return 1;
	}
}
