package logisticspipes.request;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.routing.IFluidProvider;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.FluidIdentifier;

public class FluidRequestTreeNode {
	
	protected final FluidIdentifier liquid;
	protected final int amount;
	protected List<FluidLogisticsPromise> promises = new ArrayList<FluidLogisticsPromise>();
	
	private int promiseFluidAmount = 0;
	
	protected final RequestTree root;
	protected final IRequestFluid target;
	private final RequestTreeNode parentNode;
	
	public FluidRequestTreeNode(FluidIdentifier liquid, int amount, IRequestFluid target, RequestTreeNode parentNode) {
		this.liquid = liquid;
		this.amount = amount;
		this.target = target;
		this.parentNode=parentNode;
		if(parentNode != null) {
			parentNode.liquidSubRequests.add(this);
			this.root = parentNode.root;
		} else {
			this.root = null;
		}
		
		checkFluidProvider();
	}

	public void addPromise(FluidLogisticsPromise promise) {
		promises.add(promise);
		promiseFluidAmount += promise.amount;
		if(root != null) {
			root.promiseAdded(promise);
		}
	}

	protected void buildMissingMap(Map<ItemIdentifier,Integer> missing) {
		if(amountLeft() != 0) {
			ItemIdentifier item = liquid.getItemIdentifier();
			Integer count = missing.get(item);
			if(count == null)
				count = 0;
			count += amountLeft();
			missing.put(item, count);
		}
	}

	protected void buildUsedMap(Map<ItemIdentifier,Integer> used, Map<ItemIdentifier,Integer> missing) {
		int usedcount = promiseFluidAmount;
		if(usedcount != 0) {
			ItemIdentifier item = liquid.getItemIdentifier();
			Integer count = used.get(item);
			if(count == null)
				count = 0;
			count += usedcount;
			used.put(item, count);
		}
		if(amountLeft() != 0) {
			ItemIdentifier item = liquid.getItemIdentifier();
			Integer count = missing.get(item);
			if(count == null)
				count = 0;
			count += amountLeft();
			missing.put(item, count);
		}
	}

	public void fullFill() {
		for(FluidLogisticsPromise promise: promises){
			promise.sender.fullFill(promise, target);
		}
	}

	public IRequestFluid getTarget() {
		return target;
	}
	
	void destroy() {
		if(parentNode != null) {
			parentNode.remove(this);
		}
	}
	
	protected void removeSubPromisses() {
		for(FluidLogisticsPromise promise:promises) {
			if(root != null) {
				root.promiseRemoved(promise);
			}
		}
	}
	
	private boolean checkFluidProvider() {
		boolean done = true;
		FluidRoutedPipe thisPipe = (FluidRoutedPipe) this.target;
		List<IFluidProvider> providers = getFluidProviders();
		for(IFluidProvider provider:providers) {
			if(!thisPipe.sharesTankWith((FluidRoutedPipe) provider)) {
				int alreadyRequested = 0;
				if(root != null) {
					alreadyRequested = root.getAllPromissesFor(provider, getFluid());
				}
				provider.canProvide(this, alreadyRequested);
			}
		}
		if(!isDone()) {
			done = false;
		}
		return done;
	}

	private List<IFluidProvider> getFluidProviders() {
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(liquid.getItemIdentifier());
		List<IFluidProvider> providers = new LinkedList<IFluidProvider>();
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i+1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i,false);
			if(r.getPipe() instanceof IFluidProvider){
				ExitRoute e = target.getRouter().getDistanceTo(r);
				if (e!=null) {
					CoreRoutedPipe pipe = e.destination.getPipe();
					if (pipe instanceof IFluidProvider){
						providers.add((IFluidProvider)pipe);
					}
				}
			}
		}
		return providers;
	}
	
	public int getAmount() {
		return amount;
	}

	public FluidIdentifier getFluid() {
		return liquid;
	}

	public ItemIdentifierStack getStack() {
		return liquid.getItemIdentifier().makeStack(amount);
	}
	
	public int amountLeft() {
		return amount - promiseFluidAmount;
	}

	public boolean isDone() {
		return amountLeft() <= 0;
	}

	public void sendMissingMessage(RequestLog log) {
		Map<ItemIdentifier,Integer> missing = new HashMap<ItemIdentifier,Integer>();
		missing.put(liquid.getItemIdentifier(), amountLeft());
		log.handleMissingItems(missing);
	}
	
	public int getPromiseFluidAmount() {
		return promiseFluidAmount;
	}
}
