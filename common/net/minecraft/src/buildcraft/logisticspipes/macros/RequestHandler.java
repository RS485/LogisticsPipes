package net.minecraft.src.buildcraft.logisticspipes.macros;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.ItemMessage;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.LogisticsTransaction;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.routing.Router;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.ItemIdentifierStack;

public class RequestHandler {

	public static class RequestReply {
		private RequestReply() {}
		public List<ItemMessage> items;
		public boolean suceed;
	}
	
	public static RequestReply requestMacrolist(NBTTagCompound itemlist, IRequestItems requester, EntityPlayer player) {
		NBTTagList list = itemlist.getTagList("inventar");
		LogisticsTransaction transaction = new LogisticsTransaction();
		List<ItemMessage> items = new ArrayList<ItemMessage>();
		for(int i = 0;i < list.tagCount();i++) {
			NBTTagCompound itemnbt = (NBTTagCompound) list.tagAt(i);
			NBTTagCompound itemNBTContent = itemnbt.getCompoundTag("nbt");
			if(!itemnbt.hasKey("nbt")) {
				itemNBTContent = null;
			}
			ItemIdentifierStack stack = ItemIdentifier.get(itemnbt.getInteger("id"),itemnbt.getInteger("data"),itemNBTContent).makeStack(itemnbt.getInteger("amount"));
			transaction.addRequest(new LogisticsRequest(stack.getItem(), stack.stackSize, requester));
			items.add(new ItemMessage(stack));
		}
		List<ItemMessage> errors = new ArrayList<ItemMessage>();
		RequestReply reply = new RequestReply();
		if(LogisticsManager.Request(transaction, requester.getRouter().getRoutersByCost(), errors, player, false)) {
			LogisticsManager.Request(transaction, requester.getRouter().getRoutersByCost(), errors, player, true);
			reply.items = items;
			reply.suceed = true;
		} else {
			reply.items = errors;
			reply.suceed = false;
		}
		return reply;
	}
	
}
