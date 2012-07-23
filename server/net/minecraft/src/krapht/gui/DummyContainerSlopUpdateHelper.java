package net.minecraft.src.krapht.gui;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ItemStack;

public class DummyContainerSlopUpdateHelper {

	public static void update(DummyContainer dummyContainer, int slotId, ItemStack stack, EntityPlayer entityplayer) {
		if(entityplayer instanceof EntityPlayerMP) {
			((EntityPlayerMP)entityplayer).updateCraftingInventorySlot(dummyContainer, slotId, stack);
		}
	}

}
