package logisticspipes.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Pair4<T1, T2, T3, T4> extends Pair3<T1, T2, T3> {

	protected T4 value4;

	public Pair4(T1 value1, T2 value2, T3 value3, T4 value4) {
		super(value1, value2, value3);
		this.value4 = value4;
	}

	@Override
	public Pair4<T1, T2, T3, T4> copy() {
		return new Pair4<T1, T2, T3, T4>(value1, value2, value3, value4);
	}
}
