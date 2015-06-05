package logisticspipes.items;

import java.util.List;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconReg) {
		itemIcon = iconReg.registerIcon("logisticspipes:liquids/empty");
	}

	@Override
	public int getItemStackLimit() {
		return 1;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		FluidStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(par1ItemStack));
		if (stack != null) {
			String s = stack.getFluid().getUnlocalizedName();
			if (s != null) {
				return s;
			}
		}
		return super.getUnlocalizedName(par1ItemStack);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			FluidStack stack = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(par1ItemStack));
			if (stack != null) {
				par3List.add("Type:  " + stack.getFluid().getLocalizedName(stack));
				par3List.add("Value: " + stack.amount + "mB");
			}
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void getSubItems(Item par1, CreativeTabs ct, List list) {
		//Don't add to creative in any way
	}
}
