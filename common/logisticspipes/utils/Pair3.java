package logisticspipes.utils;

public class Pair3<T1, T2, T3> extends Pair<T1, T2> {

	protected T3 _value3;
	
	public Pair3(T1 value1, T2 value2, T3 value3) {
		super(value1, value2);
		_value3 = value3;
	}
	
	public T3 getValue3(){
		return _value3;
	}

	public void setValue3(T3 value3) {
		_value3 = value3;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("<").append(_value1.toString()).append(",").append(_value2.toString()).append(",").append(_value3.toString()).append(">").toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Pair3)) return false;
		return  ((_value1 == null && ((Pair3<?, ?, ?>)o)._value1 == null) || (_value1 != null && _value1.equals(((Pair3<?, ?, ?>)o)._value1))) && 
				((_value2 == null && ((Pair3<?, ?, ?>)o)._value2 == null) || (_value2 != null && _value2.equals(((Pair3<?, ?, ?>)o)._value2))) && 
				((_value3 == null && ((Pair3<?, ?, ?>)o)._value3 == null) || (_value3 != null && _value3.equals(((Pair3<?, ?, ?>)o)._value3)));
	}

	public Pair3<T1, T2, T3> copy() {
		return new Pair3<T1, T2, T3>(_value1, _value2, _value3);
	}
}
