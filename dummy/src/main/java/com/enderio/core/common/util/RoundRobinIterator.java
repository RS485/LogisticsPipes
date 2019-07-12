package com.enderio.core.common.util;

import java.util.Iterator;

public class RoundRobinIterator<T> implements Iterable<T>, Iterator<T> {

	@Override
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
