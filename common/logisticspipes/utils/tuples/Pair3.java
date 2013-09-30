package logisticspipes.utils.tuples;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Pair3<T1, T2, T3> extends Pair<T1, T2> {

	protected T3 value3;

	public Pair3(T1 value1, T2 value2, T3 value3) {
		super(value1, value2);
		this.value3 = value3;
	}

	@Override
	public Pair3<T1, T2, T3> copy() {
		return new Pair3<T1, T2, T3>(value1, value2, value3);
	}
}
