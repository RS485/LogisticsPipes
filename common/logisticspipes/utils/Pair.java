package logisticspipes.utils;



/*** What kind of language does not have a generic pair class???? ***/
public class Pair<T1, T2> {
	protected T1 _value1;
	protected T2 _value2;
	
	public Pair(T1 value1, T2 value2){
		_value1 = value1;
		_value2 = value2;
	}
	
	public T1 getValue1(){
		return _value1;
	}
	
	public T2 getValue2(){
		return _value2;
	}
	
	@Override
	public String toString() {
		return new StringBuilder("<").append(_value1.toString()).append(",").append(_value2.toString()).append(">").toString();
	}

	public void setValue1(T1 value1) {
		_value1 = value1;
	}

	public void setValue2(T2 value2) {
		_value2 = value2;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Pair)) return false;
		return  ((_value1 == null && ((Pair<?, ?>)o)._value1 == null) || (_value1 != null && _value1.equals(((Pair<?, ?>)o)._value1))) && 
				((_value2 == null && ((Pair<?, ?>)o)._value2 == null) || (_value2 != null && _value2.equals(((Pair<?, ?>)o)._value2)));
	}

	public Pair<T1, T2> copy() {
		return new Pair<T1, T2>(_value1, _value2);
	}
}
