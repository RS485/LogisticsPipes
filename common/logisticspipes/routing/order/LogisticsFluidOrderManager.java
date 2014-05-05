package logisticspipes.routing.order;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.tuples.Triplet;

public class LogisticsFluidOrderManager {
	
	private LinkedList<Triplet<FluidIdentifier, Integer, IRequestFluid>> queue = new LinkedList<Triplet<FluidIdentifier, Integer, IRequestFluid>>();
	
	public void add(FluidLogisticsPromise promise, IRequestFluid destination) {
		if(promise.amount < 0) throw new RuntimeException("The amount can't be less than zero");
		queue.addLast(new Triplet<FluidIdentifier, Integer, IRequestFluid>(promise.liquid, promise.amount, destination));
	}

	public boolean hasOrders() {
		return !queue.isEmpty();
	}

	public Triplet<FluidIdentifier, Integer, IRequestFluid> getFirst() {
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

	public List<Triplet<FluidIdentifier, Integer, IRequestFluid>> getAll() {
		return Collections.unmodifiableList(queue);
	}
}
