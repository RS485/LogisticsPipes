package logisticspipes.utils;

import lombok.Data;

@Data
public class Pair<T1, T2> {
	protected T1 value1;
	protected T2 value2;

	public Pair(T1 value1, T2 value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public Pair<T1, T2> copy() {
		return new Pair<T1, T2>(value1, value2);
	}
}
