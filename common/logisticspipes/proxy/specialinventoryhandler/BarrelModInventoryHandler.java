package logisticspipes.proxy.specialinventoryhandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.Loader;

/*
 * This class was last edited on Feb-05-2014 by need4speed402 the creator of 'The Barrels Mod'
 */
public class BarrelModInventoryHandler extends SpecialInventoryHandler {
	private static final Class tileEntityClass;
	private static final Method getStackLimit;
	
	private static final Method getItem, setItem;
	
	private static final Method getModeForSide;

	static{
		Class XtileEntityClass = null;
		Method XgetStackLimit = null, XgetItem = null, XsetItem = null, XgetModeForSide = null;
		
		if (Loader.isModLoaded("barrels")){
			try {
				XtileEntityClass = Class.forName("need4speed402.mods.barrels.TileEntityBarrel");
				
				XgetStackLimit = XtileEntityClass.getMethod("getStackLimit");
				
				XgetItem = XtileEntityClass.getMethod("getItem");
				XsetItem = XtileEntityClass.getMethod("setItem", ItemStack.class);
				
				XgetModeForSide = XtileEntityClass.getMethod("getModeForSide", int.class);
			} catch (Exception e) {
				XtileEntityClass = null;
			}
		}
		
		tileEntityClass = XtileEntityClass;
		getStackLimit = XgetStackLimit;
		getItem = XgetItem;
		setItem = XsetItem;
		getModeForSide = XgetModeForSide;
	}
	
	private final TileEntity tile;
	private final int hide;

	private BarrelModInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		this.tile = tile;
		this.hide = hideOnePerStack || hideOne ? 1 : 0;
	}

	public BarrelModInventoryHandler() {
		this.tile = null;
		this.hide = 0;
	}
	
	@Override
	public boolean init() {
		return tileEntityClass != null;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tileEntityClass.isAssignableFrom(tile.getClass());
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new BarrelModInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	private ItemStack match (ItemIdentifier iden, ItemStack stack){
		if (stack == null){
			return null;
		}
		
		return ItemIdentifier.get(stack) == iden ? stack : null;
	}
	
	@Override
	public int itemCount(ItemIdentifier itemIdent) {
		try {
			ItemStack stack = (ItemStack) getItem.invoke(this.tile);
			
			if (this.match(itemIdent, stack) != null) {
				return stack.stackSize - this.hide;
			}
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return 0;
	}

	@Override
	public ItemStack getSingleItem(ItemIdentifier itemIdent) {
		return this.getMultipleItems(itemIdent, 1);
	}
	
	@Override
	public ItemStack getMultipleItems(ItemIdentifier itemIdent, int count) {
		try {
			ItemStack stack = (ItemStack) getItem.invoke(this.tile);
			ItemStack match = this.match(itemIdent, stack);
			
			if (stack != null && match != null) {
				if (stack.stackSize - this.hide < count){
					return null;
				}
				
				stack.stackSize -= count;
				
				setItem.invoke(this.tile, stack);
				
				ItemStack ret = match.copy();
				ret.stackSize = count;
				return ret;
			}
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return null;
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<ItemIdentifier>();
		
		try {
			ItemStack stack = (ItemStack) getItem.invoke(this.tile);
			
			if (stack != null){
				result.add(ItemIdentifier.get(stack));
			}
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return result;
	}
	
	@Override
	public HashMap<ItemIdentifier, Integer> getItemsAndCount() {
		HashMap<ItemIdentifier, Integer> result = new HashMap<ItemIdentifier, Integer>();
		
		try {
			ItemStack stack = (ItemStack) getItem.invoke(this.tile);
			
			if (stack != null){
				result.put(ItemIdentifier.get(stack), stack.stackSize - this.hide);
			}
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return result;
	}

	@Override
	public boolean containsItem(ItemIdentifier itemIdent) {
		try {
			ItemStack stack = (ItemStack) getItem.invoke(this.tile);
			
			return this.match(itemIdent, stack) != null;
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return false;
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		try{
			ItemStack stack = (ItemStack) getItem.invoke(this.tile);
			
			if (stack != null){
				return ItemIdentifier.getUndamaged(stack) == itemIdent;
			}
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}
	
	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		try {
			ItemStack stack = (ItemStack) getItem.invoke(this.tile);
			int max = (Integer) getStackLimit.invoke(this.tile);
			
			if (stack != null) {
				if (this.match(itemIdent, stack) == null){
					return 0;
				}
				
				return max * stack.getMaxStackSize() - stack.stackSize;
			}else{
				return max * itemIdent.makeNormalStack(1).getMaxStackSize();
			}
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return 0;
	}

	@Override
	public ItemStack add(ItemStack stack, ForgeDirection from, boolean doAdd) {
		ItemStack st = stack.copy();
		st.stackSize = 0;
		if (from == ForgeDirection.UNKNOWN){
			return st;
		}
		
		try {
			Enum mode = (Enum) getModeForSide.invoke(this.tile, from.ordinal());
			if (mode.name().equals("UNUSED") || mode.name().equals("OUT")){
				return st;
			}
			
			ItemStack inBarrel = (ItemStack) getItem.invoke(this.tile);
			int max = (Integer) getStackLimit.invoke(this.tile);
			
			if (inBarrel == null) {
				st.stackSize = Math.min(max * stack.getMaxStackSize(), stack.stackSize);
				
				if (doAdd) {
					setItem.invoke(this.tile, st.copy());
				}
			}else if (this.match(ItemIdentifier.get(stack), inBarrel) != null){
				int room = max * inBarrel.getMaxStackSize() - inBarrel.stackSize;
				
				st.stackSize = Math.min(room, stack.stackSize);
				
				if (doAdd) {
					inBarrel.stackSize += st.stackSize;
					
					setItem.invoke(this.tile, inBarrel);
				}
			}
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return st;
	}

	@Override
	public boolean isSpecialInventory() {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		try {
			//as said in the spec, you cannot use this method to get the direct instance of the item in the barrel, it is a re-calculated copy.
			return (ItemStack) getItem.invoke(this.tile);
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		try {
			ItemStack stack = (ItemStack) getItem.invoke(this.tile);
			
			if(stack.stackSize > this.hide) {
				stack.stackSize -= 1;
				
				setItem.invoke(this.tile, stack);
				
				ItemStack ret = stack.copy();
				ret.stackSize = 1;
				return ret;
			}
		} catch (IllegalAccessException e){}
		catch (IllegalArgumentException e){}
		catch (InvocationTargetException e){}
		
		return null;
	}
}
