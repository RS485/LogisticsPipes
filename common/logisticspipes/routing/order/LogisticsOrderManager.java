/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing.order;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.ILPPositionProvider;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.PipeManagerContentPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifierStack;

public abstract class LogisticsOrderManager<T extends LogisticsOrder, I> implements Iterable<T> {

	protected final LogisticsOrderLinkedList<T, I> _orders;
	protected IChangeListener listener = null;
	protected PlayerCollectionList watchingPlayers = new PlayerCollectionList();
	private ILPPositionProvider pos;

	public LogisticsOrderManager(LogisticsOrderLinkedList<T, I> orders, ILPPositionProvider pos) {
		_orders = orders;
		this.pos = pos;
	}

	public LogisticsOrderManager(IChangeListener listener, ILPPositionProvider pos, LogisticsOrderLinkedList<T, I> orders) {
		this(orders, pos);
		this.listener = listener;
	}

	private static void addToList(ItemIdentifierStack stack, LinkedList<ItemIdentifierStack> list) {
		for (ItemIdentifierStack ident : list) {
			if (ident.getItem().equals(stack.getItem())) {
				ident.setStackSize(ident.getStackSize() + stack.getStackSize());
				return;
			}
		}
		list.addLast(stack.clone());
	}

	protected void listen() {
		changed();
		if (listener != null) {
			listener.listenedChanged();
		}
	}

	public void dump() {
		StringBuilder sb = new StringBuilder(" ############################################# ").append(System.getProperty("line.separator"));
		for (T s : _orders) {
			sb.append(s.getAsDisplayItem()).append(" / ").append(s.getAmount()).append(" / ").append(s.getType().name()).append(System.getProperty("line.separator"));
		}
		System.out.print(sb.append(" ############################################# ").toString());
		System.out.println();
	}

	public LinkedList<ItemIdentifierStack> getContentList(World world) {
		if (MainProxy.isClient(world) || _orders.size() == 0) {
			return new LinkedList<>();
		}
		LinkedList<ItemIdentifierStack> list = new LinkedList<>();
		for (LogisticsOrder request : _orders) {
			LogisticsOrderManager.addToList(request.getAsDisplayItem(), list);
		}
		return list;
	}

	public boolean hasOrders(ResourceType... type) {
		return peekAtTopRequest(type) != null;
	}

	/* only multi-access SAFE when type is null; all other access patterns may change the state of the stack so the returned element is on top*/
	@SuppressWarnings("unchecked")
	public T peekAtTopRequest(ResourceType... type) {
		List<ResourceType> typeList = Arrays.asList(type);
		if (_orders.size() == 0) {
			return null;
		}
		T top = (T) _orders.getFirst().setInProgress(true);
		int loopCount = 0;
		while (!typeList.contains(top.getType())) {
			loopCount++;
			if (loopCount > _orders.size()) {
				return null;
			}
			deferSend(); // sets the new top to InProgress
			top = _orders.getFirst();
		}
		return top;
	}

	@SuppressWarnings("unchecked")
	public void sendSuccessfull(int number, boolean defersend, IRoutedItem item) {
		_orders.getFirst().reduceAmountBy(number);
		if (_orders.getFirst().isWatched() && item != null) {
			IDistanceTracker tracker = new DistanceTracker();
			item.setDistanceTracker(tracker);
			_orders.getFirst().addDistanceTracker(tracker);
		}
		int destination = _orders.getFirst().getRouterId();
		if (_orders.getFirst().getAmount() <= 0) {
			LogisticsOrder order = _orders.removeFirst();
			order.setFinished(true);
			order.setInProgress(false);
		}
		if (!_orders.isEmpty()) {
			LogisticsOrder start = _orders.getFirst();
			if (defersend && destination == start.getRouterId()) {
				_orders.addLast((T) _orders.removeFirst().setInProgress(false));
				while (start != _orders.getFirst() && destination == _orders.getFirst().getRouterId()) {
					_orders.addLast(_orders.removeFirst());
				}
				if (start == _orders.getFirst()) {
					_orders.addLast(_orders.removeFirst());
				}
				_orders.getFirst().setInProgress(true);
			}
		}
		listen();
	}

	public void sendFailed() {
		if (!_orders.isEmpty()) {
			LogisticsOrder order = _orders.removeFirst();
			order.setFinished(true);
			order.setInProgress(false);
		}
		if (!_orders.isEmpty()) {
			_orders.getFirst().setInProgress(true);
		}
		listen();
	}

	@SuppressWarnings("unchecked")
	public void deferSend() {
		_orders.addLast((T) _orders.removeFirst().setInProgress(false));
		_orders.getFirst().setInProgress(true);
		listen();
	}

	public int totalAmountCountInAllOrders() {
		int amount = 0;
		for (LogisticsOrder request : _orders) {
			amount += request.getAmount();
		}
		return amount;
	}

	public void setMachineProgress(byte progress) {
		if (_orders.isEmpty()) {
			return;
		}
		_orders.getFirst().setMachineProgress(progress);
		changed();
	}

	public boolean isFirstOrderWatched() {
		if (_orders.isEmpty()) {
			return false;
		}
		return _orders.getFirst().isWatched();
	}

	public void startWatching(EntityPlayer player) {
		watchingPlayers.add(player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeManagerContentPacket.class).setManager(this).setLPPos(pos.getLPPosition()), player);
	}

	public void stopWatching(EntityPlayer player) {
		watchingPlayers.remove(player);
	}

	public boolean hasExtras() {
		return _orders.hasExtras();
	}

	private void changed() {
		if (watchingPlayers.isEmpty()) {
			return;
		}
		//if(!oldOrders.equals(_orders)) {
		//	oldOrders.clear();
		//	oldOrders.addAll(_orders);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(PipeManagerContentPacket.class).setManager(this).setLPPos(pos.getLPPosition()), watchingPlayers);
		//}
	}

	/**
	 * DON'T MODIFY TROUGH THIS ONLY READ THE VALUES
	 */
	@Nonnull
	@Override
	public Iterator<T> iterator() {
		return this._orders.iterator();
	}

	public int size() {
		return _orders.size();
	}
}
