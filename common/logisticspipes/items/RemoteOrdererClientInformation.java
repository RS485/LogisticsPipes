package logisticspipes.items;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.main.KeyBoardProxy;
import net.minecraft.src.ItemStack;

public class RemoteOrdererClientInformation extends RemoteOrderer {

	public RemoteOrdererClientInformation(int id) {
		super(id);
	}

	@Override
	public int getIconFromDamage(int par1) {
    	return LogisticsPipes.instance.LOGISTICSREMOTEORDERER_ICONINDEX;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, List list) {
		//Add special tooltip in tribute to DireWolf
		if (itemstack != null && itemstack.itemID == LogisticsPipes.LogisticsRemoteOrderer.shiftedIndex){
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
