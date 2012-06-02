package net.minecraft.src.buildcraft.logisticspipes;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;
import net.minecraft.src.krapht.ItemIdentifier;

public class MessageManager {
	
	public static void overflow(EntityPlayer player, ItemIdentifier item) {
		//TODO Send Client
		//player.addChatMessage("Logistics: Possible crafting loop while trying to craft " + item.getFriendlyName() + " !! ABORTING !!");
	}
	
}
