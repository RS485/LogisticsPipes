package logisticspipes.items;


public class ItemDisk extends ItemDiskProxy {

	public ItemDisk(int i) {
		super(i);
	}
	
	public int getItemStackLimit()
    {
        return 1;
    }
}
