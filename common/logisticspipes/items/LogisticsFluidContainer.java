package logisticspipes.items;

import java.util.List;

import javax.annotation.Nullable;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidIdentifierStack;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

public class LogisticsFluidContainer extends LogisticsItem implements IItemAdvancedExistance {

	static int capacity = 8000;

	@Override
	public boolean canExistInNormalInventory(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canExistInWorld(ItemStack stack) {
		return false;
	}

/*	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconReg) {
		itemIcon = iconReg.registerIcon("logisticspipes:liquids/empty");
	}*/

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		FluidIdentifierStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(par1ItemStack));
		if (stack != null) {
			String s = stack.makeFluidStack().getFluid().getUnlocalizedName();
			if (s != null) {
				return s;
			}
		}
		return super.getUnlocalizedName(par1ItemStack);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
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
	@SuppressWarnings("rawtypes")
	public void getSubItems(CreativeTabs ct, NonNullList list) {
		//Don't add to creative in any way
	}
}
