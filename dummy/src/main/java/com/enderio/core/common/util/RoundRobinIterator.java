package com.enderio.core.common.util;

import java.util.Iterator;
import javax.annotation.Nonnull;

public class RoundRobinIterator<T> implements Iterable<T>, Iterator<T> {

	@Override
	@Nonnull
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		return null;
	}

	@Override
	public void remove() {
	}
}
