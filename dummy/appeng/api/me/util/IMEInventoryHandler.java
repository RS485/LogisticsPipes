package appeng.api.me.util;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;

public interface IMEInventoryHandler extends IMEInventory {
	
    public int getPriority();
    public void setPriority( int p );
	
    /**
     * Returns estimated number of total bytes represented by the inventory, used mainly for display.
     */
    public long totalBytes();
    
    /**
     * Returns estimated number of free bytes represented by inventory, used mainly for display.
     */
    public long freeBytes();
    
    /**
     * Returns number of used bytes represented by the inventory, used mainly for display.
     */
    public long usedBytes();
    
    /**
     * The number of items you could add before the freeBytes() decreases.
     */
    public long unusedItemCount();
    
    /**
     * True of False, if you could add a new item type.
     */
    public boolean canHoldNewItem();
    
    /**
     * This tells you where it found your cell, so if your cell changes, you should update this block...
     */
    void setUpdateTarget(TileEntity e);
    
    List<ItemStack> getPreformattedItems();	
	void setPreformattedItems( IItemList in);	
	boolean isPreformatted();
	boolean isFuzzyPreformatted();
	void setFuzzyPreformatted( boolean nf );
	
	public void setName(String name);
	String getName();
	
	void setGrid( IGridInterface grid );
	IGridInterface getGrid();
	
	void setParent( IMEInventoryHandler p );
	IMEInventoryHandler getParent();
	
	void removeGrid(IGridInterface grid, IMEInventoryHandler ignore, List<IMEInventoryHandler> duplicates );
	public void validate( List<IMEInventoryHandler> duplicates );
	public boolean canAccept(IAEItemStack input);
	
}
