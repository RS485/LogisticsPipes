package logisticspipes.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nonnull;

public class OneList<E> implements List<E> {

	private final E content;

	public OneList(E object) {
		if (object == null) {
			throw new NullPointerException("OneList content must not be null");
		}
		this.content = object;
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public boolean addAll(@Nonnull Collection<? extends E> c) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public boolean addAll(int index, @Nonnull Collection<? extends E> c) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public boolean contains(Object o) {
		return content.equals(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return c.size() == 1 && content.equals(c.iterator().next());
	}

	@Override
	public E get(int index) {
		if (!checkRange(index)) {
			throw new IndexOutOfBoundsException("OneList does not have an object at index " + index);
		}
		return content;
	}

	@Override
	public int indexOf(Object o) {
		if (content.equals(o)) {
			return 0;
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Nonnull
	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return indexOf(o);
	}

	@Nonnull
	@Override
	public ListIterator<E> listIterator() {
		return new ListIterator<E>() {

			private boolean handled = false;

			@Override
			public boolean hasNext() {
				return !handled;
			}

			@Override
			public E next() {
				if (handled) {
					return null;
				}
				handled = true;
				return OneList.this.content;
			}

			@Override
			public boolean hasPrevious() {
				return handled;
			}

			@Override
			public E previous() {
				if (!handled) {
					return null;
				}
				handled = false;
				return OneList.this.content;
			}

			@Override
			public int nextIndex() {
				return handled ? 1 : 0;
			}

			@Override
			public int previousIndex() {
				return handled ? 0 : -1;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Cannot modify OneList");
			}

			@Override
			public void set(E e) {
				throw new UnsupportedOperationException("Cannot modify OneList");
			}

			@Override
			public void add(E e) {
				throw new UnsupportedOperationException("Cannot modify OneList");
			}
		};
	}

	@Nonnull
	@Override
	public ListIterator<E> listIterator(int index) {
		if (!checkRange(index)) {
			throw new IndexOutOfBoundsException("OneList does not have an object at index " + index);
		}
		return listIterator();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public boolean removeAll(@Nonnull Collection<?> c) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public boolean retainAll(@Nonnull Collection<?> c) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException("Cannot modify OneList");
	}

	@Override
	public int size() {
		return 1;
	}

	@Nonnull
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Object[] toArray() {
		return new Object[] { content };
	}

	@Nonnull
	@Override
	public <T> T[] toArray(@Nonnull T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof OneList && content.equals(((OneList<?>) obj).content);
	}

	@Override
	public String toString() {
		return "[" + content.toString() + "]";
	}

	private boolean checkRange(int index) {
		return index == 0;
	}
}
