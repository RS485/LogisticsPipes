package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.utils.ItemIdentifierStack;

public interface IOrderManagerContentReceiver {
	public void setOrderManagerContent(List<ItemIdentifierStack> list);
}
