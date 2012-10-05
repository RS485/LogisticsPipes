package logisticspipes.items;

import java.util.List;

import net.minecraft.src.ItemStack;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

public class LogisticsItemCard extends LogisticsItem {

	public LogisticsItemCard(int i) {
		super(i);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, List par2List) {
		super.addInformation(par1ItemStack, par2List);
		if(!par1ItemStack.hasTagCompound()) {
			par2List.add("This is no valid Card");
		} else {
			if(par1ItemStack.getTagCompound().hasKey("UUID")) {
				par2List.add("Freq. Card");
				if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					par2List.add("Id: " + par1ItemStack.getTagCompound().getString("UUID"));
				}
			}
		}
	}

	@Override
	public boolean getShareTag() {
		return true;
	}

	@Override
	public int getItemStackLimit() {
		return 1;
	}
}
