package net.minecraft.src.buildcraft.logisticspipes;

import java.util.LinkedList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.ItemMessage;
import net.minecraft.src.buildcraft.krapht.network.PacketCraftingLoop;
import net.minecraft.src.buildcraft.krapht.network.PacketItems;
import net.minecraft.src.krapht.ItemIdentifier;

public class MessageManager {
	
	public static void overflow(EntityPlayer player, ItemIdentifier item) {
		LinkedList<ItemMessage> error = new LinkedList<ItemMessage>();
		error.add(new ItemMessage(item.itemID, item.itemDamage, 1, item.tag));
		CoreProxy.sendToPlayer(player, new PacketCraftingLoop(error));
	}

	public static void errors(EntityPlayer player, LinkedList<ItemMessage> errors) {
		CoreProxy.sendToPlayer(player, new PacketItems(errors,true));
	}

	public static void requested(EntityPlayer player, LinkedList<ItemMessage> items) {
		CoreProxy.sendToPlayer(player, new PacketItems(items,false));
	}
	
}
