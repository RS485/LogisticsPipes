/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.routing.LogisticsDictPromise;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

public class DictCraftingTemplate implements IReqCraftingTemplate {

	protected DictResource _result;
	protected ICraftItems _crafter;

	protected ArrayList<Pair<IResource, IAdditionalTargetInformation>> _required = new ArrayList<>(9);

	protected ArrayList<ItemIdentifierStack> _byproduct = new ArrayList<>(9);

	private final int priority;

	public DictCraftingTemplate(DictResource result, ICraftItems crafter, int priority) {
		_result = result;
		_crafter = crafter;
		this.priority = priority;
	}

	public void addRequirement(IResource requirement, IAdditionalTargetInformation info) {
		_required.add(new Pair<>(requirement, info));
	}

	public void addByproduct(ItemIdentifierStack stack) {
		for (ItemIdentifierStack i : _byproduct) {
			if (i.getItem().equals(stack.getItem())) {
				i.setStackSize(i.getStackSize() + stack.getStackSize());
				return;
			}
		}
		_byproduct.add(stack);
	}

	@Override
	public LogisticsPromise generatePromise(int nResultSets) {
		return new LogisticsDictPromise(_result, _result.stack.getStackSize() * nResultSets, _crafter, ResourceType.CRAFTING);
	}

	//TODO: refactor so that other classes don't reach through the template to the crafter.
	// needed to get the crafter todo, in order to sort
	@Override
	public ICraftItems getCrafter() {
		return _crafter;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(ICraftingTemplate o) {
		int c = o.comparePriority(priority);
		if (c == 0) {
			c = o.compareStack(_result.stack);
		}
		if (c == 0) {
			c = o.compareCrafter(_crafter);
		}
		return c;
	}

	@Override
	public int comparePriority(int priority) {
		return priority - this.priority;
	}

	@Override
	public int compareStack(ItemIdentifierStack stack) {
		return stack.compareTo(this._result.stack);
	}

	@Override
	public int compareCrafter(ICraftItems crafter) {
		return crafter.compareTo(this._crafter);
	}

	@Override
	public boolean canCraft(IResource type) {
		if (type instanceof DictResource) {
			return type.matches(_result.getItem(), IResource.MatchSettings.NORMAL) && _result.matches(((DictResource) type).getItem(), IResource.MatchSettings.NORMAL) && _result.getBitSet().equals(((DictResource) type).getBitSet());
		}
		return false;
	}

	@Override
	public int getResultStackSize() {
		return _result.stack.getStackSize();
	}

	@Override
	public IResource getResultItem() {
		return _result;
	}

	@Override
	public List<IExtraPromise> getByproducts(int workSets) {
		return _byproduct.stream()
				.map(stack -> new LogisticsExtraPromise(stack.getItem(), stack.getStackSize() * workSets, getCrafter(), false))
				.collect(Collectors.toList());
	}

	@Override
	public List<Pair<IResource, IAdditionalTargetInformation>> getComponents(int nCraftingSetsNeeded) {
		List<Pair<IResource, IAdditionalTargetInformation>> stacks = new ArrayList<>(_required.size());

		// for each thing needed to satisfy this promise
		for (Pair<IResource, IAdditionalTargetInformation> stack : _required) {
			Pair<IResource, IAdditionalTargetInformation> pair = new Pair<>(stack.getValue1()
					.clone(nCraftingSetsNeeded), stack.getValue2());
			stacks.add(pair);
		}
		return stacks;
	}
}
