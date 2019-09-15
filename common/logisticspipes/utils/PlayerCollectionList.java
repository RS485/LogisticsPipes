package logisticspipes.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerCollectionList {

	private List<EqualWeakReference<PlayerEntity>> players = new ArrayList<>();
	private boolean checkingPlayers = false;

	public void checkPlayers() {
		checkingPlayers = true;
		Iterator<EqualWeakReference<PlayerEntity>> iPlayers = players.iterator();
		while (iPlayers.hasNext()) {
			EqualWeakReference<PlayerEntity> playerReference = iPlayers.next();
			boolean remove = false;
			if (playerReference.get() == null) {
				remove = true;
			} else if (playerReference.get().removed) {
				remove = true;
			} else if (playerReference.get() instanceof ServerPlayerEntity) {
				if (!((ServerPlayerEntity) playerReference.get()).networkHandler.getConnection().isOpen()) {
					remove = true;
				}
			}
			if (remove) {
				iPlayers.remove();
			}
		}
		checkingPlayers = false;
	}

	public Iterable<PlayerEntity> players() {
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

	public void add(PlayerEntity player) {
		players.add(new EqualWeakReference<>(player));
	}

	public boolean remove(PlayerEntity player) {
		if (contains(player) && players.size() > 0) {
			return players.remove(new EqualWeakReference<>(player));
		} else {
			return false;
		}
	}

	public boolean contains(PlayerEntity player) {
		checkPlayers();
		return players.contains(new EqualWeakReference<>(player));
	}

	private static class Itr implements Iterator<PlayerEntity> {

		private final Iterator<EqualWeakReference<PlayerEntity>> iterator;

		private Itr(Iterator<EqualWeakReference<PlayerEntity>> source) {
			iterator = source;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public PlayerEntity next() {
			EqualWeakReference<PlayerEntity> reference = iterator.next();
			return reference.get();
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}
}
