package logisticspipes.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.ListSyncPacket;
import logisticspipes.proxy.MainProxy;

public class SyncList<E> implements List<E> {

	private final List<E> list;
	private ListSyncPacket<E> packetType;
	private PlayerCollectionList watcherList = null;
	private boolean dirty = false;
	private int dim, x, z;

	public SyncList() {
		this(null, new ArrayList<>());
	}

	public SyncList(ListSyncPacket<E> type) {
		this(type, new ArrayList<>());
	}

	public SyncList(ListSyncPacket<E> type, List<E> list) {
		this.packetType = type;
		this.list = list;
	}

	/**
	 * Can be used to trigger update manualy
	 */
	public void markDirty() {
		if (packetType == null) {
			return;
		}
		dirty = true;
	}

	public void sendUpdateToWaters() {
		if (packetType == null) {
			return;
		}
		if (dirty) {
			dirty = false;
			if (watcherList != null) {
				MainProxy.sendToPlayerList(packetType.template().setList(list), watcherList);
			} else {
				MainProxy.sendPacketToAllWatchingChunk(x, z, dim, packetType.template().setList(list));
			}
		}
	}

	public void setPacketType(ListSyncPacket<E> type, int dim, int x, int z) {
		packetType = type;
		this.dim = dim;
		this.x = x;
		this.z = z;
		if (watcherList != null) {
			MainProxy.sendToPlayerList(packetType.template().setList(list), watcherList);
		} else {
			MainProxy.sendPacketToAllWatchingChunk(x, z, dim, packetType.template().setList(list));
		}
	}

	public void addWatcher(EntityPlayer player) {
		if (watcherList == null) {
			watcherList = new PlayerCollectionList();
		}
		if (packetType != null) {
			MainProxy.sendPacketToPlayer(packetType.template().setList(list), player);
		}
		watcherList.add(player);
	}

	public boolean removeWatcher(EntityPlayer player) {
		if (watcherList == null) {
			watcherList = new PlayerCollectionList();
		}
		return watcherList.remove(player);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object paramObject) {
		return list.contains(paramObject);
	}

	@Nonnull
	@Override
	public Iterator<E> iterator() {
		return new SyncIter(list.iterator());
	}

	@Nonnull
	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Nonnull
	@Override
	public <T> T[] toArray(@Nonnull T[] paramArrayOfT) {
		return list.toArray(paramArrayOfT);
	}

	@Override
	public boolean add(E paramE) {
		boolean flag = list.add(paramE);
		markDirty();
		return flag;
	}

	@Override
	public boolean remove(Object paramObject) {
		boolean flag = list.remove(paramObject);
		markDirty();
		return flag;
	}

	@Override
	public boolean containsAll(@Nonnull Collection<?> paramCollection) {
		return list.containsAll(paramCollection);
	}

	@Override
	public boolean addAll(@Nonnull Collection<? extends E> paramCollection) {
		boolean flag = list.addAll(paramCollection);
		markDirty();
		return flag;
	}

	@Override
	public boolean addAll(int paramInt, @Nonnull Collection<? extends E> paramCollection) {
		boolean flag = list.addAll(paramInt, paramCollection);
		markDirty();
		return flag;
	}

	@Override
	public boolean removeAll(@Nonnull Collection<?> paramCollection) {
		boolean flag = list.removeAll(paramCollection);
		markDirty();
		return flag;
	}

	@Override
	public boolean retainAll(@Nonnull Collection<?> paramCollection) {
		boolean flag = list.retainAll(paramCollection);
		markDirty();
		return flag;
	}

	@Override
	public void clear() {
		list.clear();
		markDirty();
	}

	@Override
	public E get(int paramInt) {
		return list.get(paramInt);
	}

	@Override
	public E set(int paramInt, E paramE) {
		E object = list.set(paramInt, paramE);
		markDirty();
		return object;
	}

	@Override
	public void add(int paramInt, E paramE) {
		list.add(paramInt, paramE);
		markDirty();
	}

	@Override
	public E remove(int paramInt) {
		E object = list.remove(paramInt);
		markDirty();
		return object;
	}

	@Override
	public int indexOf(Object paramObject) {
		return list.indexOf(paramObject);
	}

	@Override
	public int lastIndexOf(Object paramObject) {
		int index = list.lastIndexOf(paramObject);
		markDirty();
		return index;
	}

	@Nonnull
	@Override
	public ListIterator<E> listIterator() {
		return new SyncListIter(list.listIterator());
	}

	@Nonnull
	@Override
	public ListIterator<E> listIterator(int paramInt) {
		return new SyncListIter(list.listIterator(paramInt));
	}

	@Nonnull
	@Override
	public List<E> subList(int paramInt1, int paramInt2) {
		throw new UnsupportedOperationException();
	}

	private class SyncIter implements Iterator<E> {

		private final Iterator<E> iter;

		protected SyncIter(Iterator<E> iter) {
			this.iter = iter;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public E next() {
			return iter.next();
		}

		@Override
		public void remove() {
			iter.remove();
			markDirty();
		}
	}

	private class SyncListIter extends SyncIter implements ListIterator<E> {

		private final ListIterator<E> iter;

		protected SyncListIter(ListIterator<E> iter) {
			super(iter);
			this.iter = iter;
		}

		@Override
		public void add(E paramE) {
			iter.add(paramE);
			markDirty();
		}

		@Override
		public boolean hasPrevious() {
			return iter.hasPrevious();
		}

		@Override
		public int nextIndex() {
			return iter.nextIndex();
		}

		@Override
		public E previous() {
			return iter.previous();
		}

		@Override
		public int previousIndex() {
			return iter.previousIndex();
		}

		@Override
		public void set(E paramE) {
			iter.set(paramE);
			markDirty();
		}
	}
}
