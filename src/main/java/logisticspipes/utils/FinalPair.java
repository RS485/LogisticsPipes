package logisticspipes.utils;

/**
 * A final pair that caches hashcode and implements equals, mainly for use as
 * hashmap key
 */
public class FinalPair<T1, T2> {

	private final T1 _value1;
	private final T2 _value2;
	private final int _hashcode;

	public FinalPair(T1 value1, T2 value2) {
		_value1 = value1;
		_value2 = value2;
		_hashcode = _value1.hashCode() ^ _value2.hashCode();
	}

	public T1 getValue1() {
		return _value1;
	}

	public T2 getValue2() {
		return _value2;
	}

	@Override
	public String toString() {
		return String.format("<%s,%s>", _value1, _value2);
	}

	@Override
	public int hashCode() {
		return _hashcode;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FinalPair)) {
			return false;
		}
		FinalPair<?, ?> p = (FinalPair<?, ?>) o;
		return _value1.equals(p._value1) && _value2.equals(p._value2);
	}
}
