package logisticspipes.utils;

import java.lang.ref.WeakReference;

public class EqualWeakReference<T> extends WeakReference<T> {

	public EqualWeakReference(T referent) {
		super(referent);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EqualWeakReference) {
			Object o = ((EqualWeakReference<?>)obj).get();
			if(o == null) {
				return this.get() == null;
			} else {
				return o.equals(this.get());
			}
		}
		return false;
	}
}
