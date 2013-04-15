package logisticspipes.items;

import java.util.List;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.textures.Textures;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsLiquidContainer extends LogisticsItem implements IItemAdvancedExistance {
	public LogisticsLiquidContainer(int i) {
		super(i);
	}

	@Override
	public boolean canExistInNormalInventory(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canExistInWorld(ItemStack stack) {
		return false;
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
		//this.iconIndex = Textures.LOGISTICSITEM_LIQUIDCONTAINER_ICONINDEX;
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
