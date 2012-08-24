package logisticspipes.buildcraft.logisticspipes.items;

import java.util.List;

import logisticspipes.buildcraft.logisticspipes.items.LogisticsNBTTagCompundItem;

import net.minecraft.src.ItemStack;

public class ItemDiskProxy extends LogisticsNBTTagCompundItem {

	public ItemDiskProxy(int i) {
		super(i);
	}

	@Override
	public void addInformation(ItemStack itemStack, List list) {
		if(itemStack.hasTagCompound()) {
			if(itemStack.getTagCompound().hasKey("name")) {
				String name = "\u00a78" + itemStack.getTagCompound().getString("name");
				list.add(name);
			}
		}
	}
}
