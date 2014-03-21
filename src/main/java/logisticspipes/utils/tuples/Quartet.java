package logisticspipes.utils.tuples;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Quartet<T1, T2, T3, T4> extends Triplet<T1, T2, T3> {

	protected T4 value4;

	public Quartet(T1 value1, T2 value2, T3 value3, T4 value4) {
		super(value1, value2, value3);
		this.value4 = value4;
	}

	@Override
	public Quartet<T1, T2, T3, T4> copy() {
		return new Quartet<T1, T2, T3, T4>(value1, value2, value3, value4);
	}
}
