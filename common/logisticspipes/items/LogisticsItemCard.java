package logisticspipes.items;

import java.util.List;

import net.minecraft.src.EntityPlayer;
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
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
		super.addInformation(itemStack, player, list, flag);
		if(!itemStack.hasTagCompound()) {
			list.add("This is no valid Card");
		} else {
			if(itemStack.getTagCompound().hasKey("UUID")) {
				list.add("Freq. Card");
				if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					list.add("Id: " + itemStack.getTagCompound().getString("UUID"));
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
