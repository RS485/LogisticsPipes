package logisticspipes.items;

import java.util.List;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;


public class ItemDisk extends LogisticsItem {

	public ItemDisk(int i) {
		super(i);
	}
	
	public int getItemStackLimit()
    {
        return 1;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public CreativeTabs getCreativeTab()
    {
        return CreativeTabs.tabRedstone;
    }

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
		if(itemStack.hasTagCompound()) {
			if(itemStack.getTagCompound().hasKey("name")) {
				String name = "\u00a78" + itemStack.getTagCompound().getString("name");
				list.add(name);
			}
		}
	}
}
