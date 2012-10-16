package logisticspipes.utils;

public class Pair4<T1, T2, T3, T4> extends Pair3<T1, T2, T3> {

	protected T4 _value4;
	
	public Pair4(T1 value1, T2 value2, T3 value3, T4 value4) {
		super(value1, value2, value3);
		_value4 = value4;
	}
	
	public T4 getValue4(){
		return _value4;
	}

	public void setValue4(T4 value4) {
		_value4 = value4;
	}
	
	public String toString() {
		return new StringBuilder("<").append(_value1.toString()).append(",").append(_value2.toString()).append(",").append(_value3.toString()).append(",").append(_value4.toString()).append(">").toString();
	}
}
