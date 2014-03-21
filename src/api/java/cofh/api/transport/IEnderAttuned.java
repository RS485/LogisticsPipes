package cofh.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IEnderAttuned {
	  public abstract String getOwnerString();

	  public abstract int getFrequency();

	  public abstract boolean setFrequency(int paramInt);

	  public abstract boolean clearFrequency();

	  public abstract boolean canSendItems();

	  public abstract boolean canSendFluid();

	  public abstract boolean canSendEnergy();

	  public abstract boolean canReceiveItems();

	  public abstract boolean canReceiveFluid();

	  public abstract boolean canReceiveEnergy();

	  public abstract boolean currentlyValidToReceiveItems(IEnderAttuned paramIEnderAttuned);

	  public abstract boolean currentlyValidToReceiveFluid(IEnderAttuned paramIEnderAttuned);

	  public abstract boolean currentlyValidToReceiveEnergy(IEnderAttuned paramIEnderAttuned);

	  public abstract boolean currentlyValidToSendItems(IEnderAttuned paramIEnderAttuned);

	  public abstract boolean currentlyValidToSendFluid(IEnderAttuned paramIEnderAttuned);

	  public abstract boolean currentlyValidToSendEnergy(IEnderAttuned paramIEnderAttuned);

	  public abstract ItemStack receiveItem(ItemStack paramItemStack);

	  public abstract FluidStack receiveFluid(FluidStack paramFluidStack, boolean paramBoolean);

	  public abstract int receiveEnergy(int paramInt, boolean paramBoolean);

	  public static enum EnderTypes
	  {
	    ITEM, FLUID, REDSTONE_FLUX;
	  }
}
