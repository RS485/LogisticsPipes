package logisticspipes.items;

import logisticspipes.items.ItemDiskProxy;
import logisticspipes.main.LogisticsItem;
import logisticspipes.network.LogisticsPipesPacket;

public class ItemDisk extends ItemDiskProxy {

	public ItemDisk(int i) {
		super(i);
	}
	
	public int getItemStackLimit()
    {
        return 1;
    }
}
