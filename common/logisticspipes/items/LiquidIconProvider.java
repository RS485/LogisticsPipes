package logisticspipes.items;

import logisticspipes.LogisticsPipes;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.liquids.LiquidContainerData;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

public enum LiquidIconProvider {
	EMPTY("empty"),

	WATER("water"), 
	LAVA("lava"),

	BIOMASS("biomass"), 
	BIOFUEL("biofuel"),

	OIL("Oil"), 
	FUEL("Fuel"),

	SEEDOIL("seedoil"), 
	HONEY("honey"), 
	JUICE("juice"), 
	CRUSHEDICE("ice"), 
	MILK("milk"),

	ACID("acid"), 
	POISON("poison"), 
	LIQUIDNITROGEN("liquidNitrogen"), 
	DNA("liquidDNA"),

	CREOSOTEOIL("Creosote Oil"), 
	STEAM("Steam");

	public String liquidID;
	public int iconIdx;
	public boolean available = false;
	public Icon liquidIcon;

	private LiquidIconProvider(String id) {
		this.liquidID = id;
	}
	
	public static void registerLiquids(Item container) {
		ItemStack empty = new ItemStack(container, 1, 0);
		LiquidStack liquid = null;
		
		for (LiquidIconProvider liquidType : LiquidIconProvider.values()) {
			if (liquidType.ordinal() == 0) {
				liquid = null; 
			} else if (liquidType.ordinal() == 1) {
				liquid = new LiquidStack(Block.waterStill, LogisticsLiquidContainer.capacity);
			} else if (liquidType.ordinal() == 2) {
				liquid = new LiquidStack(Block.lavaStill, LogisticsLiquidContainer.capacity);
			} else {
				liquid = LiquidDictionary.getLiquid(liquidType.liquidID, LogisticsLiquidContainer.capacity);
			}
			
			if (liquid != null) {
				ItemStack filled = new ItemStack(container, 1, liquidType.ordinal());
				LiquidContainerRegistry.registerLiquid(new LiquidContainerData(liquid, filled, empty));
				liquidType.available = true;
			}
		}
		LiquidIconProvider.EMPTY.available = true;
	}
	
	public static ItemStack getFilledContainer(LiquidStack stack) {
		int ordinal = 0;
		String liqName = LiquidDictionary.findLiquidName(stack);
		System.out.println(liqName);
		if (liqName.equalsIgnoreCase("water")) ordinal = 1;
		if (liqName.equalsIgnoreCase("lava")) ordinal = 2;
    	if (stack != null) {
    		for(LiquidIconProvider prov : LiquidIconProvider.values()) {
    			if (prov.liquidID.equalsIgnoreCase(liqName)) ordinal = prov.ordinal();
    		}
    	}
		ItemStack item = new ItemStack(LogisticsPipes.LogisticsLiquidContainer, 1, ordinal);
		NBTTagCompound nbt = new NBTTagCompound("tag");
		stack.writeToNBT(nbt);
		item.setTagCompound(nbt);
		return item;
	}
	
	public static LiquidStack getLiquidFromContainer(ItemStack stack) {
		if(stack.getItem() instanceof LogisticsLiquidContainer && stack.hasTagCompound()) {
			return LiquidStack.loadLiquidStackFromNBT(stack.getTagCompound());
		}
		return null;
	}


}
