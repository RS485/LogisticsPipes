package logisticspipes.interfaces;

import java.util.List;

public interface IChainAddList<T> extends List<T> {

	T addChain(T add);
}
