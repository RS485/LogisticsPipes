package logisticspipes.utils;

import java.util.ArrayList;

import logisticspipes.interfaces.IChainAddList;

public class ChainAddArrayList<T> extends ArrayList<T> implements IChainAddList<T> {

	@Override
	public T addChain(T add) {
		this.add(add);
		return add;
	}
}
