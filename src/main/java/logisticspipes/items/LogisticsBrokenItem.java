package logisticspipes.items;

import java.util.List;

import logisticspipes.interfaces.IItemAdvancedExistance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsBrokenItem extends LogisticsItem implements IItemAdvancedExistance {

	public LogisticsBrokenItem(int i) {
		super(i);
	}

	@Override
	public boolean canExistInNormalInventory(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canExistInWorld(ItemStack stack) {
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		par3List.add(" - This is an useless item");
		par3List.add(" - You get this by trying to");
		par3List.add("    break a protected pipe");
	}
}
