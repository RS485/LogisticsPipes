package logisticspipes.utils;

import java.util.BitSet;

public class SlidingWindowBitSet {

	private final BitSet set = new BitSet();
	private final int window;
	private int currentEx = -1;
	private int cleared;

	public SlidingWindowBitSet(int window) {
		this.window = window;
	}

	public void set(long i, boolean b) {
		int mod = (int) i & ((1 << window) - 1);
		checkEx(i);
		set.set(mod, b);
	}

	public void set(long i) {
		int mod = (int) i & ((1 << window) - 1);
		checkEx(i);
		set.set(mod);
	}

	public boolean get(long i) {
		int mod = (int) i & ((1 << window) - 1);
		checkEx(i);
		return set.get(mod);
	}

	private void checkEx(long i) {
		int bit = (1 << window);
		int mod = (int) i & bit - 1;
		int ex = (int) i >> window;
		if (currentEx < ex) {
			if (cleared < bit) {
				set.clear(cleared, bit);
			}
			currentEx = ex;
			cleared = 0;
		}
		if (cleared <= mod && currentEx == ex) {
			int clear_till = ((mod >> 6) + 1) << 6;
			set.clear(cleared, clear_till);
			cleared = clear_till;
		}
	}
}
