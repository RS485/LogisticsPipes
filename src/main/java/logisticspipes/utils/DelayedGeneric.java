package logisticspipes.utils;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class DelayedGeneric<T> implements Delayed {

	private final long origin;
	private final long delay;
	private final T workItem;

	public DelayedGeneric(final T workItem, final long delay) {
		this.origin = System.nanoTime();
		this.workItem = workItem;
		this.delay = delay * 1000;
	}

	public T get() {
		return workItem;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(delay - (System.nanoTime() - origin), TimeUnit.NANOSECONDS);
	}

	@Override
	public int compareTo(@Nonnull Delayed delayed) {
		if (delayed == this) {
			return 0;
		}

		if (delayed instanceof DelayedGeneric) {
			long diff = (delay + origin) - (((DelayedGeneric<?>) delayed).delay + ((DelayedGeneric<?>) delayed).origin);
			return ((diff == 0) ? 0 : ((diff < 0) ? -1 : 1));
		}

		long d = (getDelay(TimeUnit.NANOSECONDS) - delayed.getDelay(TimeUnit.NANOSECONDS));
		return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
	}

	@Override
	public String toString() {
		return "DelayedGeneric[" + workItem.toString() + ", (" + delay + ", " + getDelay(TimeUnit.MILLISECONDS) + ")]";
	}
}
