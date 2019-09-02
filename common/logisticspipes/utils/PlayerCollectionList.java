package logisticspipes.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerCollectionList {

	private List<EqualWeakReference<EntityPlayer>> players = new ArrayList<>();
	private boolean checkingPlayers = false;

	public void checkPlayers() {
		checkingPlayers = true;
		Iterator<EqualWeakReference<EntityPlayer>> iPlayers = players.iterator();
		while (iPlayers.hasNext()) {
			EqualWeakReference<EntityPlayer> playerReference = iPlayers.next();
			boolean remove = false;
			if (playerReference.get() == null) {
				remove = true;
			} else if (playerReference.get().isDead) {
				remove = true;
			} else if (playerReference.get() instanceof EntityPlayerMP) {
				if (!((EntityPlayerMP) playerReference.get()).connection.netManager.isChannelOpen()) {
					remove = true;
				}
			}
			if (remove) {
				iPlayers.remove();
			}
		}
		checkingPlayers = false;
	}

	public Iterable<EntityPlayer> players() {
		checkPlayers();
		return () -> new Itr(players.iterator());
	}

	public int size() {
		if (!checkingPlayers) {
			checkPlayers();
		}
		return players.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean isEmptyWithoutCheck() {
		return players.size() == 0;
	}

	public void add(EntityPlayer player) {
		players.add(new EqualWeakReference<>(player));
	}

	public boolean remove(EntityPlayer player) {
		if (contains(player) && players.size() > 0) {
			return players.remove(new EqualWeakReference<>(player));
		} else {
			return false;
		}
	}

	public boolean contains(EntityPlayer player) {
		checkPlayers();
		return players.contains(new EqualWeakReference<>(player));
	}

	private static class Itr implements Iterator<EntityPlayer> {

		private final Iterator<EqualWeakReference<EntityPlayer>> iterator;

		private Itr(Iterator<EqualWeakReference<EntityPlayer>> source) {
			iterator = source;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public EntityPlayer next() {
			EqualWeakReference<EntityPlayer> reference = iterator.next();
			return reference.get();
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}
}
