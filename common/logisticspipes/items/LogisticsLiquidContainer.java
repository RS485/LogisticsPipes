package logisticspipes.items;

import java.util.List;

import logisticspipes.interfaces.IItemAdvancedExistance;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsLiquidContainer extends LogisticsItem implements IItemAdvancedExistance {
	static int capacity = 8000;
	
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
	public void registerIcons(IconRegister iconReg) {
		for (LiquidIconProvider liquids : LiquidIconProvider.values()) {
			if ((liquids.liquidIcon == null)) {
				liquids.liquidIcon = iconReg.registerIcon("logisticspipes:" + "liquids/" + liquids.liquidID);
			}
		}
		this.itemIcon = LiquidIconProvider.EMPTY.liquidIcon;
	}
	
    /**
     * Gets an icon index based on an item's damage value and the given render pass
     */
	@Override
    public Icon getIconFromDamageForRenderPass(int damage, int pass) {
    	Icon icon = this.itemIcon;
    	if (damage != 0 && pass == 0) {
    		icon = LiquidIconProvider.values()[java.lang.Math.max(0, java.lang.Math.min(damage, LiquidIconProvider.values().length - 1))].liquidIcon;
    	}
    	return icon;
    }
    	
	@Override
	public int getItemStackLimit() {
		return 1;
	}
	
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
	public void getSubItems(int par1, CreativeTabs ct, List list) {
		for (LiquidIconProvider liquids : LiquidIconProvider.values()) {
			if (liquids.available) {
				list.add(new ItemStack(this, 1, liquids.ordinal()));
			}
		}
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }
	
	@Override
    public int getRenderPasses(int metadata) {
        return 2;
    }
}
