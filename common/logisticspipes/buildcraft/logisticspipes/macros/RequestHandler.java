package logisticspipes.buildcraft.logisticspipes.macros;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.mod_LogisticsPipes;
import logisticspipes.buildcraft.krapht.IRequestItems;
import logisticspipes.buildcraft.krapht.ItemMessage;
import logisticspipes.buildcraft.krapht.LogisticsManager;
import logisticspipes.buildcraft.krapht.LogisticsRequest;
import logisticspipes.buildcraft.krapht.LogisticsTransaction;
import logisticspipes.buildcraft.krapht.RoutedPipe;
import logisticspipes.buildcraft.krapht.routing.Router;
import logisticspipes.krapht.ItemIdentifier;
import logisticspipes.krapht.ItemIdentifierStack;


import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

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
