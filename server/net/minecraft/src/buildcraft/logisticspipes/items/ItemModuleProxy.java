package net.minecraft.src.buildcraft.logisticspipes.items;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.krapht.LogisticsItem;

public abstract class ItemModuleProxy extends LogisticsItem {

	public ItemModuleProxy(int i) {
		super(i);
	}
	
	@Override
    public boolean func_46003_i()
    {
        return false;
    }

	public abstract String getModuleDisplayName(ItemStack itemstack);
	
	public abstract int getModuleIconFromDamage(int i);
	
	public abstract String getTextureMap();
	
	public void loadModules() {}
}
