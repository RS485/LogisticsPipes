package logisticspipes.items;

import net.minecraft.src.CreativeTabs;


public class ItemDisk extends ItemDiskProxy {

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
}
