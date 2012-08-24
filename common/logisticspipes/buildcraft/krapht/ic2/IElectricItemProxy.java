package logisticspipes.buildcraft.krapht.ic2;

import logisticspipes.krapht.ItemIdentifier;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

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
