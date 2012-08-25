package logisticspipes.utils;


/*** What kind of language does not have a generic pair class???? ***/
public class Pair<T1, T2> {
	private final T1 _value1;
	private final T2 _value2;
	
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
}
