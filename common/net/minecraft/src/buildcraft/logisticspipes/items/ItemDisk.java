package net.minecraft.src.buildcraft.logisticspipes.items;

import net.minecraft.src.buildcraft.krapht.LogisticsItem;
import net.minecraft.src.buildcraft.krapht.network.LogisticsPipesPacket;

public class ItemDisk extends LogisticsNBTTagCompundItem {

	public ItemDisk(int i) {
		super(i);
	}
	
	public int getItemStackLimit()
    {
        return 1;
    }
}
