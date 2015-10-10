package logisticspipes.utils;

import logisticspipes.interfaces.IChainAddList;

import java.util.ArrayList;

public class ChainAddArrayList<T> extends ArrayList<T> implements IChainAddList<T> {
	@Override
	public T addChain(T add) {
		this.add(add);
		return add;
	}
}
