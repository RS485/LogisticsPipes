package cofh.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public abstract interface IItemConduit {
	public abstract ItemStack sendItems(ItemStack stack, ForgeDirection dir);
}
