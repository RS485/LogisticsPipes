package net.minecraft.src.buildcraft.krapht.ic2;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.buildcraft.logisticspipes.items.ItemModule;
import net.minecraft.src.core_LogisticsPipes;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftSilicon;

import net.minecraft.src.ic2.api.IElectricItem;
import net.minecraft.src.ic2.api.Items;
import net.minecraft.src.ic2.api.Ic2Recipes;


public class ElectricItemProxy implements IElectricItemProxy {

	public boolean isElectricItem(ItemStack stack)
	{
		return stack != null && stack.getItem() instanceof IElectricItem;
	}

	public int getCharge(ItemStack stack)
	{
		if (stack.getItem() instanceof IElectricItem && stack.hasTagCompound())
			return stack.getTagCompound().getInteger("charge");
		else
			return 0;
	}

	public int getMaxCharge(ItemStack stack)
	{
		if (stack.getItem() instanceof IElectricItem)
			return ((IElectricItem) stack.getItem()).getMaxCharge();
		else
			return 0;
	}

	public boolean isDischarged(ItemStack stack, boolean partial)
	{
		return isDischarged(stack, partial, stack.getItem());
	}

	public boolean isCharged(ItemStack stack, boolean partial)
	{
		return isCharged(stack, partial, stack.getItem());
	}

	public boolean isDischarged(ItemStack stack, boolean partial, Item electricItem)
	{
		if (electricItem instanceof IElectricItem && (((IElectricItem)electricItem).getChargedItemId() == stack.itemID || ((IElectricItem)electricItem).getEmptyItemId() == stack.itemID))
		{
			if (partial)
				return getCharge(stack) < getMaxCharge(stack);
			else
				return getCharge(stack) == 0;
		}
		return false;
	}

	public boolean isCharged(ItemStack stack, boolean partial, Item electricItem)
	{
		if (electricItem instanceof IElectricItem && ((IElectricItem)electricItem).getChargedItemId() == stack.itemID)
		{
			if (partial)
				return getCharge(stack) > 0;
			else
				return getCharge(stack) == getMaxCharge(stack);
		}
		return false;
	}

	public void addCraftingRecipes()
	{
		Ic2Recipes.addCraftingRecipe(new ItemStack(core_LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { "CGD", "rBr", "DrC", 
										Character.valueOf('C'), Items.getItem("electronicCircuit"),
										Character.valueOf('D'), Items.getItem("reBattery"),
										Character.valueOf('G'), BuildCraftCore.goldGearItem,
										Character.valueOf('r'), Item.redstone,
										Character.valueOf('B'), new ItemStack(core_LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)});

		Ic2Recipes.addCraftingRecipe(new ItemStack(core_LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { " G ", "rBr", "DrC", 
										Character.valueOf('C'), Items.getItem("electronicCircuit"),
										Character.valueOf('D'), Items.getItem("reBattery"),
										Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
										Character.valueOf('r'), Item.redstone,
										Character.valueOf('B'), new ItemStack(core_LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)});
	}

}
