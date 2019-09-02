package logisticspipes.request;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IProvide;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.request.resources.FluidResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.FinalPair;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class RequestTree extends RequestTreeNode {

	public enum ActiveRequestType {
		Provide,
		Craft,
		AcceptPartial,
		SimulateOnly,
		LogMissing,
		LogUsed
	}

	public static final EnumSet<ActiveRequestType> defaultRequestFlags = EnumSet.of(ActiveRequestType.Provide, ActiveRequestType.Craft);
	private HashMap<FinalPair<IProvide, ItemIdentifier>, Integer> _promisetotals;

	public RequestTree(IResource requestType, RequestTree parent, EnumSet<ActiveRequestType> requestFlags, IAdditionalTargetInformation info) {
		super(requestType, parent, requestFlags, info);
	}

	private int getExistingPromisesFor(FinalPair<IProvide, ItemIdentifier> key) {
		if (_promisetotals == null) {
			_promisetotals = new HashMap<>();
		}
		Integer n = _promisetotals.get(key);
		if (n == null) {
			return 0;
		}
		return n;
	}

	public int getAllPromissesFor(IProvide provider, ItemIdentifier item) {
		FinalPair<IProvide, ItemIdentifier> key = new FinalPair<>(provider, item);
		return getExistingPromisesFor(key);
	}

	public LinkedList<IExtraPromise> getExtrasFor(IResource item) {
		HashMap<IProvide, List<IExtraPromise>> extraMap = new HashMap<>();
		checkForExtras(item, extraMap);
		removeUsedExtras(item, extraMap);
		LinkedList<IExtraPromise> extras = new LinkedList<>();
		extraMap.values().forEach(extras::addAll);
		return extras;
	}

	protected LinkedLogisticsOrderList fullFillAll() {
		return fullFill();
	}

	public void sendMissingMessage(RequestLog log) {
		Map<IResource, Integer> missing = new HashMap<>();
		buildMissingMap(missing);
		log.handleMissingItems(RequestTreeNode.shrinkToList(missing));
	}

	public void sendUsedMessage(RequestLog log) {
		Map<IResource, Integer> used = new HashMap<>();
		Map<IResource, Integer> missing = new HashMap<>();
		buildUsedMap(used, missing);
		log.handleSucessfullRequestOfList(RequestTreeNode.shrinkToList(used), new LinkedLogisticsOrderList());
		log.handleMissingItems(RequestTreeNode.shrinkToList(missing));
	}

	protected void promiseAdded(IPromise promise) {
		FinalPair<IProvide, ItemIdentifier> key = new FinalPair<>(promise.getProvider(), promise.getItemType());
		if (_promisetotals == null) {
			_promisetotals = new HashMap<>();
		}
		_promisetotals.put(key, getExistingPromisesFor(key) + promise.getAmount());
	}

	protected void promiseRemoved(IPromise promise) {
		FinalPair<IProvide, ItemIdentifier> key = new FinalPair<>(promise.getProvider(), promise.getItemType());
		int r = getExistingPromisesFor(key) - promise.getAmount();
		if (r == 0) {
			_promisetotals.remove(key);
		} else {
			_promisetotals.put(key, r);
		}
	}

	public static class workWeightedSorter implements Comparator<ExitRoute> {

		public final double distanceWeight;

		public workWeightedSorter(double distanceWeight) {
			this.distanceWeight = distanceWeight;
		}

		@Override
		public int compare(ExitRoute o1, ExitRoute o2) {
			int c;
			if (o1.destination.getPipe() instanceof IHavePriority) {
				if (o2.destination.getPipe() instanceof IHavePriority) {
					c = ((IHavePriority) o2.destination.getCachedPipe()).getPriority() - ((IHavePriority) o1.destination.getCachedPipe()).getPriority();
					if (c != 0) {
						return c;
					}
				} else {
					return -1;
				}
			} else {
				if (o2.destination.getPipe() instanceof IHavePriority) {
					return 1;
				}
			}

			//GetLoadFactor*64 should be an integer anyway.
			c = (int) Math.floor(o1.destination.getCachedPipe().getLoadFactor() * 64) - (int) Math.floor(o2.destination.getCachedPipe().getLoadFactor() * 64);
			if (distanceWeight != 0) {
				c += (int) (Math.floor(o1.distanceToDestination * 64) - (int) Math.floor(o2.distanceToDestination * 64)) * distanceWeight;
			}
			return c;
		}

	}

	public static boolean request(List<ItemIdentifierStack> items, IRequestItems requester, RequestLog log, EnumSet<ActiveRequestType> requestFlags, IAdditionalTargetInformation info) {
		Map<IResource, Integer> messages = new HashMap<>();
		RequestTree tree = new RequestTree(new ItemResource(new ItemIdentifierStack(ItemIdentifier.get(Item.getItemFromBlock(Blocks.STONE), 0, null), 0), requester), null, requestFlags, info);
		boolean isDone = true;
		for (ItemIdentifierStack stack : items) {
			ItemIdentifier item = stack.getItem();
			Integer count = messages.get(item);
			if (count == null) {
				count = 0;
			}
			count += stack.getStackSize();
			ItemResource req = new ItemResource(stack, requester);
			messages.put(req, count);
			RequestTree node = new RequestTree(req, tree, requestFlags, info);
			isDone = isDone && node.isDone();
		}
		if (isDone) {
			LinkedLogisticsOrderList list = tree.fullFillAll();
			if (log != null) {
				log.handleSucessfullRequestOfList(RequestTreeNode.shrinkToList(messages), list);
			}
			return true;
		} else {
			if (log != null) {
				tree.logFailedRequestTree(log);
			}
			return false;
		}
	}

	public static int request(ItemIdentifierStack item, IRequestItems requester, RequestLog log, boolean acceptPartial, boolean simulateOnly, boolean logMissing, boolean logUsed, EnumSet<ActiveRequestType> requestFlags, IAdditionalTargetInformation info) {
		ItemResource req = new ItemResource(item, requester);
		RequestTree tree = new RequestTree(req, null, requestFlags, info);
		if (!simulateOnly && (tree.isDone() || ((tree.getPromiseAmount() > 0) && acceptPartial))) {
			LinkedLogisticsOrderList list = tree.fullFillAll();
			if (log != null) {
				log.handleSucessfullRequestOf(req.copyForDisplayWith(item.getStackSize()), list);
			}
			return tree.getPromiseAmount();
		} else {
			if (log != null) {
				if (!tree.isDone()) {
					tree.recurseFailedRequestTree();
				}
				if (logMissing) {
					tree.sendMissingMessage(log);
				}
				if (logUsed) {
					tree.sendUsedMessage(log);
				}

			}
			return tree.getPromiseAmount();
		}
	}

	public static boolean request(ItemIdentifierStack item, IRequestItems requester, RequestLog log, IAdditionalTargetInformation info) {
		return RequestTree.request(item, requester, log, false, false, true, false, RequestTree.defaultRequestFlags, info) == item.getStackSize();
	}

	public static int requestPartial(ItemIdentifierStack item, IRequestItems requester, IAdditionalTargetInformation info) {
		return RequestTree.request(item, requester, null, true, false, true, false, RequestTree.defaultRequestFlags, info);
	}

	public static int simulate(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		return RequestTree.request(item, requester, log, true, true, false, true, RequestTree.defaultRequestFlags, null);
	}

	public static int requestFluidPartial(FluidIdentifier liquid, int amount, IRequestFluid pipe, RequestLog log) {
		return RequestTree.requestFluid(liquid, amount, pipe, log, true);
	}

	public static boolean requestFluid(FluidIdentifier liquid, int amount, IRequestFluid pipe, RequestLog log) {
		return RequestTree.requestFluid(liquid, amount, pipe, log, false) == amount;
	}

	private static int requestFluid(FluidIdentifier liquid, int amount, IRequestFluid pipe, RequestLog log, boolean acceptPartial) {
		FluidResource req = new FluidResource(liquid, amount, pipe);
		RequestTree request = new RequestTree(req, null, RequestTree.defaultRequestFlags, null);
		if (request.isDone() || acceptPartial) {
			request.fullFill();
			if (log != null) {
				log.handleSucessfullRequestOf(req.copyForDisplayWith(req.getRequestedAmount()), null);
			}
			return request.getPromiseAmount();
		} else {
			if (log != null) {
				request.sendMissingMessage(log);
			}
			return request.getPromiseAmount();
		}
	}
}
