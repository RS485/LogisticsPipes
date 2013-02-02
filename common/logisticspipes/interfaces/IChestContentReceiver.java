package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.utils.ItemIdentifierStack;

public interface IChestContentReceiver {
	
	public void setReceivedChestContent(List<ItemIdentifierStack> list);
	
}
