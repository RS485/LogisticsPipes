package logisticspipes.request;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LiquidLogisticsPromise;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.LiquidIdentifier;

public class LiquidRequestTreeNode {
	
	protected final LiquidIdentifier liquid;
	protected final int amount;
	protected List<LiquidLogisticsPromise> promises = new ArrayList<LiquidLogisticsPromise>();
	
	private int promiseLiquidAmount = 0;
	
	protected final RequestTree root;
	protected final IRequestLiquid target;
	private final RequestTreeNode parentNode;
	
	public LiquidRequestTreeNode(LiquidIdentifier liquid, int amount, IRequestLiquid target, RequestTreeNode parentNode) {
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
		
		checkLiquidProvider();
	}

	public void addPromise(LiquidLogisticsPromise promise) {
		promises.add(promise);
		promiseLiquidAmount += promise.amount;
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
		int usedcount = promiseLiquidAmount;
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
		for(LiquidLogisticsPromise promise: promises){
			promise.sender.fullFill(promise, target);
		}
	}

	public IRequestLiquid getTarget() {
		return target;
	}
	
	void destroy() {
		if(parentNode != null) {
			parentNode.remove(this);
		}
	}
	
	protected void removeSubPromisses() {
		for(LiquidLogisticsPromise promise:promises) {
			if(root != null) {
				root.promiseRemoved(promise);
			}
		}
	}
	
	private boolean checkLiquidProvider() {
		boolean done = true;
		LiquidRoutedPipe thisPipe = (LiquidRoutedPipe) this.target;
		List<ILiquidProvider> providers = getLiquidProviders();
		for(ILiquidProvider provider:providers) {
			if(!thisPipe.sharesTankWith((LiquidRoutedPipe) provider)) {
				int alreadyRequested = 0;
				if(root != null) {
					alreadyRequested = root.getAllPromissesFor(provider, getLiquid());
				}
				provider.canProvide(this, alreadyRequested);
			}
		}
		if(!isDone()) {
			done = false;
		}
		return done;
	}

	private List<ILiquidProvider> getLiquidProviders() {
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(liquid.getItemIdentifier());
		List<ILiquidProvider> providers = new LinkedList<ILiquidProvider>();
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i+1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i,false);
			if(r.getPipe() instanceof ILiquidProvider){
				ExitRoute e = target.getRouter().getDistanceTo(r);
				if (e!=null) {
					CoreRoutedPipe pipe = e.destination.getPipe();
					if (pipe instanceof ILiquidProvider){
						providers.add((ILiquidProvider)pipe);
					}
				}
			}
		}
		return providers;
	}
	
	public int getAmount() {
		return amount;
	}

	public LiquidIdentifier getLiquid() {
		return liquid;
	}

	public ItemIdentifierStack getStack() {
		return liquid.getItemIdentifier().makeStack(amount);
	}
	
	public int amountLeft() {
		return amount - promiseLiquidAmount;
	}

	public boolean isDone() {
		return amountLeft() <= 0;
	}

	public void sendMissingMessage(RequestLog log) {
		Map<ItemIdentifier,Integer> missing = new HashMap<ItemIdentifier,Integer>();
		missing.put(liquid.getItemIdentifier(), amountLeft());
		log.handleMissingItems(missing);
	}
	
	public int getPromiseLiquidAmount() {
		return promiseLiquidAmount;
	}
}
