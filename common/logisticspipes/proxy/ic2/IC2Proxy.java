package logisticspipes.proxy.ic2;

import ic2.api.IElectricItem;
import ic2.api.Ic2Recipes;
import ic2.api.Items;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemModule;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;


public class IC2Proxy implements IIC2Proxy {

	/**
	 * @return Boolean, true if itemstack is a ic2 electric item.
	 * @param stack The stack to check.
	 */
	@Override
	public boolean isElectricItem(ItemStack stack) {
		return stack != null && stack.getItem() instanceof IElectricItem;
	}

	/**
	 * @return Int value of current charge on electric item.
	 * @param stack The stack to get charge for.
	 */
	@Override
	public int getCharge(ItemStack stack) {
		if (stack.getItem() instanceof IElectricItem && stack.hasTagCompound()) {
			return stack.getTagCompound().getInteger("charge");
		} else {
			return 0;
		}
	}

	/**
	 * @return Int value of maximum charge on electric item.
	 * @param stack The stack to get max charge for.
	 */
	@Override
	public int getMaxCharge(ItemStack stack) {
		if (!(stack.getItem() instanceof IElectricItem)) return 0;
		return ((IElectricItem) stack.getItem()).getMaxCharge();
	}

	/**
	 * @return Boolean, true if electric item is fully charged.
	 * @param stack The stack to check if its fully charged.
	 */
	@Override
	public boolean isFullyCharged(ItemStack stack) {
		if (!isElectricItem(stack)) return false;
		int charge = getCharge(stack);
		int maxCharge = getMaxCharge(stack);
		return charge == maxCharge;
	}
	
	/**
	 * @return Boolean, true if electric item is fully discharged.
	 * @param stack The stack to check if its fully discharged.
	 */
	@Override
	public boolean isFullyDischarged(ItemStack stack) {
		if (!isElectricItem(stack)) return false;
		int charge = getCharge(stack);
		return charge == 0;
	}
	
	/**
	 * @return Boolean, true if electric item contains charge but is not full.
	 * @param stack The stack to check if its partially chraged.
	 */
	@Override
	public boolean isPartiallyCharged(ItemStack stack) {
		return (!isFullyCharged(stack) && !isFullyDischarged(stack));
	}
	
	/**
	 * Adds crafting recipes to "IC2 Crafting"
	 */
	@Override
	public void addCraftingRecipes() {
		Ic2Recipes.addCraftingRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICBUFFER), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), Items.getItem("advancedCircuit"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Ic2Recipes.addCraftingRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICBUFFER), new Object[] { 
			" G ", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), Items.getItem("advancedCircuit"),
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});


		Ic2Recipes.addCraftingRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			"CGD", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('D'), Items.getItem("reBattery"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Ic2Recipes.addCraftingRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			"CGD", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('D'), Items.getItem("chargedReBattery"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Ic2Recipes.addCraftingRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			"CGc", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('c'), Items.getItem("reBattery"),
			Character.valueOf('D'), Items.getItem("chargedReBattery"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Ic2Recipes.addCraftingRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			"CGc", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('c'), Items.getItem("chargedReBattery"),
			Character.valueOf('D'), Items.getItem("reBattery"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});

		Ic2Recipes.addCraftingRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			" G ", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('D'), Items.getItem("reBattery"),
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Ic2Recipes.addCraftingRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			" G ", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('D'), Items.getItem("chargedReBattery"),
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
	}

	/**
	 * @return If IC2 is loaded, returns true.
	 */
	@Override
	public boolean hasIC2() {
		return true;
	}


}
