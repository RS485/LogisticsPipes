package logisticspipes.routing;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair3;

public class LogisticsLiquidOrderManager {
	
	private LinkedList<Pair3<LiquidIdentifier, Integer, IRequestLiquid>> queue = new LinkedList<Pair3<LiquidIdentifier, Integer, IRequestLiquid>>();
	
	public void add(LiquidLogisticsPromise promise, IRequestLiquid destination) {
		if(promise.amount < 0) throw new RuntimeException("The amount can't be less than zero");
		queue.addLast(new Pair3<LiquidIdentifier, Integer, IRequestLiquid>(promise.liquid, promise.amount, destination));
	}

	public boolean hasOrders() {
		return !queue.isEmpty();
	}

	public Pair3<LiquidIdentifier, Integer, IRequestLiquid> getFirst() {
		return queue.getFirst();
	}

	public void sendAmount(int amount) {
		if(!hasOrders()) return;
		int result = queue.getFirst().getValue2() - amount;
		if(result <= 0) {
			queue.removeFirst();
		} else {
			queue.getFirst().setValue2(queue.getFirst().getValue2() - amount);
		}
	}
	
	public void sendFailed() {
		if(!hasOrders()) return;
		queue.getFirst().getValue3().sendFailed(queue.getFirst().getValue1(), queue.getFirst().getValue2());
		queue.removeFirst();
	}

	public List<Pair3<LiquidIdentifier, Integer, IRequestLiquid>> getAll() {
		return Collections.unmodifiableList(queue);
	}
}
