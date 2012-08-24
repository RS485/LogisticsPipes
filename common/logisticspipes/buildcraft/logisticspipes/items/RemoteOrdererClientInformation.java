package logisticspipes.buildcraft.logisticspipes.items;

import java.util.List;

import logisticspipes.mod_LogisticsPipes;
import logisticspipes.buildcraft.krapht.KeyBoardProxy;
import logisticspipes.buildcraft.logisticspipes.items.RemoteOrderer;


import net.minecraft.src.ItemStack;

public class RemoteOrdererClientInformation extends RemoteOrderer {

	public RemoteOrdererClientInformation(int id) {
		super(id);
	}

	@Override
	public int getIconFromDamage(int par1) {
    	return mod_LogisticsPipes.instance.LOGISTICSREMOTEORDERER_ICONINDEX;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, List list) {
		//Add special tooltip in tribute to DireWolf
		if (itemstack != null && itemstack.itemID == mod_LogisticsPipes.LogisticsRemoteOrderer.shiftedIndex){
			if (KeyBoardProxy.isShiftDown()){
				list.add("a.k.a \"Requesting Tool\" - DW20");
			}
		}
		
		if(itemstack.hasTagCompound() && itemstack.stackTagCompound.hasKey("connectedPipe-x")) {
			list.add("\u00a77Has Remote Pipe");
		}
		
		super.addInformation(itemstack, list);
	}
}
