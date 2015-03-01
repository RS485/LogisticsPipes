package logisticspipes.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class CraftingRequirement
{
	public ItemIdentifierStack stack;
	//match all items with same oredict name
	public boolean use_od = false;
	//match all items with same id
	public boolean ignore_dmg = false;
	//match all items with same id and damage
	public boolean ignore_nbt = false;
	//match all items with same oredict prefix
	public boolean use_category = false;
	
	public boolean isSameItemAndFlag(CraftingRequirement other) {
		if(!stack.getItem().equals(other.stack.getItem())) return false;
		return use_od == other.use_od && ignore_dmg == other.ignore_dmg && ignore_nbt == other.ignore_nbt && use_category == other.use_category;
	}
	
	@Override
	public CraftingRequirement clone() {
		CraftingRequirement n = new CraftingRequirement();
		n.stack = stack.clone();
		n.use_od = use_od;
		n.ignore_dmg = ignore_dmg;
		n.ignore_nbt = ignore_nbt;
		n.use_category = use_category;
		return n;
	}
	
	public boolean isUnique() {
		return !use_od && !ignore_dmg && !ignore_nbt && !use_category;
	}
	
	public boolean testItem(ItemIdentifierStack other) {
		if(use_od || use_category) {
			if(stack.getItem().getDictIdentifiers().canMatch(other.getItem().getDictIdentifiers(), true, use_category)) { return true; }
		}
		ItemStack stack_n = stack.makeNormalStack();
		ItemStack other_n = other.makeNormalStack();
		if(stack_n.getItem() != other_n.getItem()) return false;
		if(stack_n.getItemDamage() != other_n.getItemDamage()) {
			if(stack_n.getHasSubtypes()) {
				return false;
			}
		} else if(!ignore_dmg) {
			return false;
		}
		if(ignore_nbt) return true;
		if(stack_n.hasTagCompound() ^ other_n.hasTagCompound()) return false;
		if(!stack_n.hasTagCompound() && !other_n.hasTagCompound()) return true;
		if(ItemStack.areItemStackTagsEqual(stack_n, other_n)) return true;
		return false;
	}
	
	public ArrayList<ItemIdentifier> getSubtitutes(IRequestItems target) {
		ArrayList<ItemIdentifier> result = new ArrayList<ItemIdentifier>();
		Map<ItemIdentifier, Integer> avail_items = SimpleServiceLocator.logisticsManager.getAvailableItems(target.getRouter().getIRoutersByCost());
		LinkedList<ItemIdentifier> craft_items = SimpleServiceLocator.logisticsManager.getCraftableItems(target.getRouter().getIRoutersByCost());
		for(Map.Entry<ItemIdentifier, Integer> i: avail_items.entrySet()) {
			if(this.testItem(i.getKey().makeStack(1))) {
				if(!result.contains(i.getKey())) {
					result.add(i.getKey());
				}
			}
		}
		for(ItemIdentifier i: craft_items) {
			if(this.testItem(i.makeStack(1))) {
				if(!result.contains(i)) {
					result.add(i);
				}
			}
		}
		return result;
	}
}
