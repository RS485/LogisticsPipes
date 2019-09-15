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

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ItemCrafter;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.logisticspipes.routing.request.Resource;
import network.rs485.logisticspipes.util.ItemStackComparator;
import network.rs485.logisticspipes.util.ItemVariant;

public class ItemCraftingTemplate implements ReqCraftingTemplate {

	protected ItemStack _result;
	protected ItemCrafter _crafter;

	protected ArrayList<Tuple2<Resource, IAdditionalTargetInformation>> _required = new ArrayList<>(9);

	protected ArrayList<ItemStack> _byproduct = new ArrayList<>(9);

	private final int priority;

	public ItemCraftingTemplate(ItemStack result, ItemCrafter crafter, int priority) {
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
				i.setCount(i.getCount() + stack.getCount());
				return;
			}
		}
		_byproduct.add(stack);
	}

	@Override
	public LogisticsPromise generatePromise(int nResultSets) {
		ItemStack newStack = _result.copy();
		newStack.setCount(newStack.getCount() * nResultSets);
		return new LogisticsPromise(newStack, _crafter, ResourceType.CRAFTING);
	}

	//TODO: refactor so that other classes don't reach through the template to the crafter.
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
			c = o.compareStack(_result);
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
		return ItemStackComparator.INSTANCE.compare(stack, this._result);
	}

	@Override
	public int compareCrafter(ItemCrafter crafter) {
		return crafter.compareTo(this._crafter);
	}

	@Override
	public boolean canCraft(Resource type) {
		if (type instanceof Resource.Item) {
			return ItemVariant.stacksEqual(((Resource.Item) type).getStack(), _result);
		} else if (type instanceof Resource.Dict) {
			return ((Resource.Dict) type).matches(_result, false);
		} else {
			return false;
		}
	}

	@Override
	public int getResultStackSize() {
		return _result.getCount();
	}

	@Override
	public Resource getResultItem() {
		return new Resource.Item(_result, null);
	}

	@Override
	public List<ExtraPromise> getByproducts(int workSets) {
		return _byproduct.stream()
				.map(ItemStack::copy)
				.peek(stack -> stack.setCount(stack.getCount() * workSets))
				.map(stack -> new LogisticsExtraPromise(stack, getCrafter(), false))
				.collect(Collectors.toList());
	}

	@Override
	public List<Tuple2<Resource, IAdditionalTargetInformation>> getComponents(int nCraftingSetsNeeded) {
		// for each thing needed to satisfy this promise
		return _required.stream()
				.map(stack -> new Tuple2<>(stack.getValue1().copy(), stack.getValue2()))
				.map(tuple -> tuple.with1(stack -> stack.setRequestedAmount(stack.getRequestedAmount() * nCraftingSetsNeeded)))
				.collect(Collectors.toCollection(() -> new ArrayList<>(_required.size())));
	}
}
