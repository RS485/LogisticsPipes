package logisticspipes.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemDisk extends LogisticsItem {

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.tabRedstone;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
		if (itemStack.hasTagCompound()) {
			if (itemStack.getTagCompound().hasKey("name")) {
				String name = "\u00a78" + itemStack.getTagCompound().getString("name");
				list.add(name);
			}
		}
	}
}
