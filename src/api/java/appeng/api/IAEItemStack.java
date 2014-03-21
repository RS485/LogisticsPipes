package appeng.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * An alternate version of ItemStack for AE to keep tabs on things easier, and to support larger storage.
 * stackSizes of getItemStack will be capped.
 * 
 * You may hold on to these if you want, just make sure you let go of them when your not using them.
 * 
 * Don't Implement.
 * 
 * Construct with Util.createItemStack( ItemStack )
 * 
 */
public interface IAEItemStack
{
	public int getItemID();
	public int getItemDamage();
	public IAETagCompound getTagCompound();
	
	public ItemStack getItemStack();
	public IAEItemStack copy();
	
	public long getStackSize();
	public void setStackSize( long stackSize );
	
	long getCountRequestable();
	void setCountRequestable(long countRequestable);
	
	boolean isCraftable();
	void setCraftable(boolean isCraftable);
	
	int getDef();
	
	public void reset();
	boolean isMeaninful();
	boolean hasTagCompound();
	
	void add(IAEItemStack option);
	
	void incStackSize(long i);
	void decStackSize(long i);
	
	void incCountRequestable(long i);
	void decCountRequestable(long i);

	Item getItem();
	void writeToNBT(NBTTagCompound i);
	
	@Override
	public boolean equals(Object obj);
	
	
}
