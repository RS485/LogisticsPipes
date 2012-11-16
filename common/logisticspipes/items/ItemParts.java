package logisticspipes.items;

import java.util.List;

import logisticspipes.config.Textures;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class ItemParts extends LogisticsItem {
	
	public ItemParts(int par1) {
		super(par1);
		this.setHasSubtypes(true);
	}
	
    public int getIconFromDamage(int par1) {
    	switch(par1) {
    	case 0: //bow
    		return Textures.LOGISTICSITEMHUD_PART1_ICONINDEX;
    	case 1: //glass
    		return Textures.LOGISTICSITEMHUD_PART2_ICONINDEX;
    	case 2: //nose bridge
    		return Textures.LOGISTICSITEMHUD_PART3_ICONINDEX;
    	case 3: //nano hopper
    		return Textures.LOGISTICSITEM_NANOHOPPER_ICONINDEX;
    		default: return super.getIconFromDamage(par1);
    	}
    }

	@Override
	public String getItemNameIS(ItemStack par1ItemStack) {
    	switch(par1ItemStack.getItemDamage()) {
    	case 0: //bow
    		return "item.HUDbow";
    	case 1: //glass
    		return "item.HUDglass";
    	case 2: //nose bridge
    		return "item.HUDnosebridge";
    	case 3: 
    		return "item.NanoHopper";
    	}
		return super.getItemNameIS(par1ItemStack);
	}

	@Override
	public CreativeTabs getCreativeTab() {
        return CreativeTabs.tabRedstone;
    }
	
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		par3List.add(new ItemStack(this, 1, 0));
		par3List.add(new ItemStack(this, 1, 1));
		par3List.add(new ItemStack(this, 1, 2));
		par3List.add(new ItemStack(this, 1, 3));
    }
	
}
