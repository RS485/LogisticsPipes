package logisticspipes.items;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifierStack;

public class LogisticsFluidContainer extends LogisticsItem implements IItemAdvancedExistance {

	static int capacity = 8000;

	public LogisticsFluidContainer() {
		setMaxStackSize(1);
	}

	@Override
	public boolean canExistInNormalInventory(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public boolean canExistInWorld(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	@Nonnull
	public String getUnlocalizedName(@Nonnull ItemStack stack) {
		FluidIdentifierStack fluidStack = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(stack));
		if (fluidStack != null) {
			String s = fluidStack.makeFluidStack().getFluid().getUnlocalizedName();
			if (s != null) {
				return s;
			}
		}
		return super.getUnlocalizedName(stack);
	}

	@Override
	@Nonnull
	public String getItemStackDisplayName(@Nonnull ItemStack itemstack) {
		String unlocalizedName = getUnlocalizedName(itemstack);
		String unlocalizedNameInefficiently = getUnlocalizedNameInefficiently(itemstack); // Fix for Logistics fluid container naming
		return I18n.translateToLocal(unlocalizedName + (unlocalizedName.equals(unlocalizedNameInefficiently) ? ".name" : "")).trim();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			FluidIdentifierStack fluidStack = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(stack));
			if (fluidStack != null) {
				tooltip.add("Type:  " + fluidStack.makeFluidStack().getFluid().getLocalizedName(fluidStack.makeFluidStack()));
				tooltip.add("Value: " + fluidStack.getAmount() + "mB");
			}
		}
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		// don't add to creative tabs in any way
	}
}
