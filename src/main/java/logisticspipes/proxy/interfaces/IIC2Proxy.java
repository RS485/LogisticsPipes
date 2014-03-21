package logisticspipes.proxy.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface IIC2Proxy {

	public abstract boolean isElectricItem(ItemStack stack);

	public abstract boolean isSimilarElectricItem(ItemStack stack, ItemStack template);

	public abstract boolean isFullyCharged(ItemStack stack);
	
	public abstract boolean isFullyDischarged(ItemStack stack);
	
	public abstract boolean isPartiallyCharged(ItemStack stack);

	public abstract void addCraftingRecipes();

	public abstract boolean hasIC2();

	public abstract void registerToEneryNet(TileEntity tile);

	public abstract void unregisterToEneryNet(TileEntity tile);

}
