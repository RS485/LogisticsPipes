package net.minecraft.src.buildcraft.krapht.ic2;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.krapht.ItemIdentifier;

public interface IElectricItemProxy {

	public abstract boolean isElectricItem(ItemStack stack);

	//public abstract boolean isElectricItem(ItemIdentifier item);

	public abstract int getCharge(ItemStack stack);

	public abstract int getMaxCharge(ItemStack stack);

	public abstract boolean isDischarged(ItemStack stack, boolean partial);

	public abstract boolean isCharged(ItemStack stack, boolean partial);

	public abstract boolean isDischarged(ItemStack stack, boolean partial, Item electricItem);

	public abstract boolean isCharged(ItemStack stack, boolean partial, Item electricItem);

	public abstract void addCraftingRecipes();

}
