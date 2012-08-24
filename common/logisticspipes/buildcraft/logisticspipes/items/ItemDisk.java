package logisticspipes.buildcraft.logisticspipes.items;

import logisticspipes.buildcraft.krapht.LogisticsItem;
import logisticspipes.buildcraft.krapht.network.LogisticsPipesPacket;
import logisticspipes.buildcraft.logisticspipes.items.ItemDiskProxy;

public class ItemDisk extends ItemDiskProxy {

	public ItemDisk(int i) {
		super(i);
	}
	
	public int getItemStackLimit()
    {
        return 1;
    }
}
