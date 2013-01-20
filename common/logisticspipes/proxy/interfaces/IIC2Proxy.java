package logisticspipes.proxy.interfaces;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IIC2Proxy {

	public abstract boolean isElectricItem(ItemStack stack);

	public abstract int getCharge(ItemStack stack);

	public abstract int getMaxCharge(ItemStack stack);

	public abstract boolean isFullyCharged(ItemStack stack);
	
	public abstract boolean isFullyDischarged(ItemStack stack);
	
	public abstract boolean isPartiallyCharged(ItemStack stack);

	public abstract void addCraftingRecipes();

	public abstract boolean hasIC2();

}
