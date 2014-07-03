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

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
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
	protected ArrayList<Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation>> _required = new ArrayList<Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation>>(9);
	protected ArrayList<Triplet<FluidIdentifier, Integer, IRequestFluid>> _requiredFluid = new ArrayList<Triplet<FluidIdentifier, Integer, IRequestFluid>>();
	protected ArrayList<ItemIdentifierStack> _byproduct = new ArrayList<ItemIdentifierStack>(9);
	private final int priority;
	
	public CraftingTemplate(ItemIdentifierStack result, ICraftItems crafter, int priority) {
		_result = result;
		_crafter = crafter;
		this.priority = priority;
	}
	
	public void addRequirement(CraftingRequirement requirement, IRequestItems crafter, IAdditionalTargetInformation info) {
		for(Pair<CraftingRequirement, IRequestItems> i : _required) {
			if(i.getValue1().isSameItemAndFlag(requirement) && i.getValue2() == crafter) {
				i.getValue1().stack.setStackSize(i.getValue1().stack.getStackSize() + requirement.stack.getStackSize());
				return;
			}
		}
		_required.add(new Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation>(requirement, crafter, info));
	}

	public void addRequirement(FluidIdentifier liquid, Integer amount, IRequestFluid crafter) {
		for(Triplet<FluidIdentifier, Integer, IRequestFluid> i : _requiredFluid) {
			if(i.getValue1().equals(liquid) && i.getValue3() == crafter) {
				i.setValue2(i.getValue2() + amount);
				return;
			}
		}
		_requiredFluid.add(new Triplet<FluidIdentifier, Integer, IRequestFluid>(liquid, amount, crafter));
	}
	
	public void addByproduct(ItemIdentifierStack stack) {
		for(ItemIdentifierStack i : _byproduct) {
			if(i.getItem().equals(stack.getItem())) {
				i.setStackSize(i.getStackSize() + stack.getStackSize());
				return;
			}
		}
		_byproduct.add(stack);
	}
	
	public LogisticsPromise generatePromise(int nResultSets) {
		return new LogisticsPromise(_result.getItem(), _result.getStackSize() * nResultSets, _crafter);
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

	protected List<Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation>> getComponentItems(int nCraftingSetsNeeded) {
		List<Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation>> stacks = new ArrayList<Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation>>(_required.size());

		// for each thing needed to satisfy this promise
		for(Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation> stack : _required) {
			Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation> pair = new Triplet<CraftingRequirement, IRequestItems, IAdditionalTargetInformation>(stack.getValue1().clone(), stack.getValue2(), stack.getValue3());
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
