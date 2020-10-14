package logisticspipes.utils.tuples;

import lombok.Data;

import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;

@Data
public class Pair<T1, T2> implements ILPCCTypeHolder {

	private final Object[] ccTypeHolder = new Object[1];
	protected T1 value1;
	protected T2 value2;

	public Pair(kotlin.Pair<T1, T2> kotlinPair) {
		this(kotlinPair.component1(), kotlinPair.component2());
	}

	public Pair(T1 value1, T2 value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public Pair<T1, T2> copy() {
		return new Pair<>(value1, value2);
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

}
