package cofh.api.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IToolHammer {
	public abstract boolean isUsable(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase, BlockPos paramBlockPos);

	public abstract boolean isUsable(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase, Entity paramEntity);

	public abstract void toolUsed(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase, BlockPos paramBlockPos);

	public abstract void toolUsed(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase, Entity paramEntity);
}
