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
import logisticspipes.interfaces.routing.ItemCrafter;
import logisticspipes.request.resources.Resource;
import logisticspipes.request.resources.Resource.Dict;
import logisticspipes.routing.LogisticsDictPromise;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.item.ItemStack;
import logisticspipes.utils.tuples.Tuple2;

public class DictCraftingTemplate implements ReqCraftingTemplate {

	protected Resource.Dict _result;
	protected ItemCrafter _crafter;

	protected ArrayList<Tuple2<Resource, IAdditionalTargetInformation>> _required = new ArrayList<>(9);

	protected ArrayList<ItemStack> _byproduct = new ArrayList<>(9);

	private final int priority;

	public DictCraftingTemplate(Resource.Dict result, ItemCrafter crafter, int priority) {
		_result = result;
		_crafter = crafter;
		this.priority = priority;
	}

	public void addRequirement(Resource requirement, IAdditionalTargetInformation info) {
		_required.add(new Tuple2<>(requirement, info));
	}

	public void addByproduct(ItemStack stack) {
		for (ItemStack i : _byproduct) {
			if (i.getItem().equals(stack.getItem())) {
				i.setStackSize(i.getCount() + stack.getCount());
				return;
			}
		}
		_byproduct.add(stack);
	}

	@Override
	public LogisticsPromise generatePromise(int nResultSets) {
		return new LogisticsDictPromise(_result, _result.stack.getCount() * nResultSets, _crafter, ResourceType.CRAFTING);
	}

	// TODO: refactor so that other classes don't reach through the template to the crafter.
	// needed to get the crafter todo, in order to sort
	@Override
	public ItemCrafter getCrafter() {
		return _crafter;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(CraftingTemplate o) {
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
	public int compareStack(ItemStack stack) {
		return stack.compareTo(this._result.stack);
	}

	@Override
	public int compareCrafter(ItemCrafter crafter) {
		return crafter.compareTo(this._crafter);
	}

	@Override
	public boolean canCraft(Resource type) {
		if (type instanceof Resource.Dict) {
			return type.matches(_result.getItem(), Resource.MatchSettings.NORMAL) && _result.matches(((Resource.Dict) type).getItem(), Resource.MatchSettings.NORMAL) && _result.getBitSet().equals(((Resource.Dict) type).getBitSet());
		}
		return false;
	}

	@Override
	public int getResultStackSize() {
		return _result.stack.getCount();
	}

	@Override
	public Resource getResultItem() {
		return _result;
	}

	@Override
	public List<ExtraPromise> getByproducts(int workSets) {
		return _byproduct.stream()
				.map(stack -> new LogisticsExtraPromise(stack.getItem(), stack.getCount() * workSets, getCrafter(), false))
				.collect(Collectors.toList());
	}

	@Override
	public List<Tuple2<Resource, IAdditionalTargetInformation>> getComponents(int nCraftingSetsNeeded) {
		List<Tuple2<Resource, IAdditionalTargetInformation>> stacks = new ArrayList<>(_required.size());

		// for each thing needed to satisfy this promise
		for (Tuple2<Resource, IAdditionalTargetInformation> stack : _required) {
			Tuple2<Resource, IAdditionalTargetInformation> tuple = new Tuple2<>(stack.getValue1()
					.copy(nCraftingSetsNeeded), stack.getValue2());
			stacks.add(tuple);
		}
		return stacks;
	}
}
