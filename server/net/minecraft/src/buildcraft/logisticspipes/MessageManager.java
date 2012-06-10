package net.minecraft.src.buildcraft.logisticspipes;

import java.util.LinkedList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.ErrorMessage;
import net.minecraft.src.buildcraft.krapht.network.PacketCraftingLoop;
import net.minecraft.src.buildcraft.krapht.network.PacketMissingItems;
import net.minecraft.src.krapht.ItemIdentifier;

public class MessageManager {
	
	public static void overflow(EntityPlayer player, ItemIdentifier item) {
		LinkedList<ErrorMessage> error = new LinkedList<ErrorMessage>();
		error.add(new ErrorMessage(item.itemID, item.itemDamage, 1));
		CoreProxy.sendToPlayer(player, new PacketCraftingLoop(error));
	}

	public static void errors(EntityPlayer player, LinkedList<ErrorMessage> errors) {
		CoreProxy.sendToPlayer(player, new PacketMissingItems(errors));
	}
	
}
