package net.minecraft.src.krapht;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;

public class ItemIdentifierProxy {

	public static String getName(int id, ItemStack stack) {
		return Item.itemsList[id].getItemName();
	}

}
