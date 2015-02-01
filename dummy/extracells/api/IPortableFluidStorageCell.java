package extracells.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IPortableFluidStorageCell extends IFluidStorageCell {
	
	public boolean usePower(EntityPlayer player, double amount, ItemStack is);

	public boolean hasPower(EntityPlayer player, double amount, ItemStack is);

}
