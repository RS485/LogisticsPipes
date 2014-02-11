/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.request;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.CraftingRequirement;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;


public class CraftingTemplate implements Comparable<CraftingTemplate>{
	
	protected ItemIdentifierStack _result;
	protected ICraftItems _crafter;
	protected ArrayList<Pair<CraftingRequirement, IRequestItems>> _required = new ArrayList<Pair<CraftingRequirement, IRequestItems>>(9);
	protected ArrayList<Triplet<FluidIdentifier, Integer, IRequestFluid>> _requiredFluid = new ArrayList<Triplet<FluidIdentifier, Integer, IRequestFluid>>();
	protected ArrayList<ItemIdentifierStack> _byproduct = new ArrayList<ItemIdentifierStack>(9);
	private final int priority;
	
	public CraftingTemplate(ItemIdentifierStack result, ICraftItems crafter, int priority) {
		_result = result;
		_crafter = crafter;
		this.priority = priority;
	}
	
	public void addRequirement(CraftingRequirement requirement, IRequestItems crafter) {
		for(Pair<CraftingRequirement, IRequestItems> i : _required) {
			if(i.getValue1().isSameItemAndFlag(requirement) && i.getValue2() == crafter) {
				i.getValue1().stack.setStackSize(i.getValue1().stack.getStackSize() + requirement.stack.getStackSize());
				return;
			}
		}
		_required.add(new Pair<CraftingRequirement, IRequestItems>(requirement, crafter));
	}

	public void addRequirement(FluidIdentifier liquid, Integer amount, IRequestFluid crafter) {
		for(Triplet<FluidIdentifier, Integer, IRequestFluid> i : _requiredFluid) {
			if(i.getValue1() == liquid && i.getValue3() == crafter) {
				i.setValue2(i.getValue2() + amount);
				return;
			}
		}
		_requiredFluid.add(new Triplet<FluidIdentifier, Integer, IRequestFluid>(liquid, amount, crafter));
	}
	
	public void addByproduct(ItemIdentifierStack stack) {
		for(ItemIdentifierStack i : _byproduct) {
			if(i.getItem() == stack.getItem()) {
				i.setStackSize(i.getStackSize() + stack.getStackSize());
				return;
			}
		}
		_byproduct.add(stack);
	}
	
	public LogisticsPromise generatePromise(int nResultSets) {
		LogisticsPromise promise = new LogisticsPromise();
		promise.item = _result.getItem();
		promise.numberOfItems = _result.getStackSize() * nResultSets;
		promise.sender = _crafter;
		return promise;
	}
	
	//TODO: refactor so that other classes don't reach through the template to the crafter.
	// needed to get the crafter todo, in order to sort
	public ICraftItems getCrafter(){
		return _crafter;
	}
	
	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(CraftingTemplate o) {
		int c = this.priority-o.priority;
		if(c==0)
			c= _result.compareTo(o._result);
		if(c==0)
			c=_crafter.compareTo(o._crafter);
		return c;
	}

	public boolean canCraft(ItemIdentifier item) {
		return item.equals(_result.getItem());
	}

	public int getResultStackSize() {
		return _result.getStackSize();
	}
	
	ItemIdentifier getResultItem() {
		return _result.getItem();
	}
	
	public List<ItemIdentifierStack> getByproduct() {
		return _byproduct;
	}

	protected List<Pair<CraftingRequirement, IRequestItems>> getComponentItems(int nCraftingSetsNeeded) {
		List<Pair<CraftingRequirement, IRequestItems>> stacks = new ArrayList<Pair<CraftingRequirement, IRequestItems>>(_required.size());

		// for each thing needed to satisfy this promise
		for(Pair<CraftingRequirement, IRequestItems> stack : _required) {
			Pair<CraftingRequirement, IRequestItems> pair = new Pair<CraftingRequirement, IRequestItems>(stack.getValue1().clone(),stack.getValue2());
			pair.getValue1().stack.setStackSize(pair.getValue1().stack.getStackSize() * nCraftingSetsNeeded);
			stacks.add(pair);
		}
		return stacks;
	}

	protected List<Triplet<FluidIdentifier, Integer, IRequestFluid>> getComponentFluid(int nCraftingSetsNeeded) {
		List<Triplet<FluidIdentifier, Integer, IRequestFluid>> stacks = new ArrayList<Triplet<FluidIdentifier, Integer, IRequestFluid>>(_requiredFluid.size());
		
		// for each thing needed to satisfy this promise
		for(Triplet<FluidIdentifier, Integer, IRequestFluid> stack : _requiredFluid) {
			stacks.add(new Triplet<FluidIdentifier, Integer, IRequestFluid>(stack.getValue1(),stack.getValue2()*nCraftingSetsNeeded,stack.getValue3()));
		}
		return stacks;
	}

}
