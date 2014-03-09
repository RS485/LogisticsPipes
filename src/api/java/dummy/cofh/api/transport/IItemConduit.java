package cofh.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public abstract interface IItemConduit {
	public abstract ItemStack insertItem(ForgeDirection paramForgeDirection, ItemStack paramItemStack);

	@Deprecated
	public abstract ItemStack insertItem(ForgeDirection paramForgeDirection, ItemStack paramItemStack, boolean paramBoolean);

	@Deprecated
	public abstract ItemStack sendItems(ItemStack stack, ForgeDirection dir);
}
