package logisticspipes.utils;

import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

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
	
	public boolean isSameItemAndFlag(CraftingRequirement other)
	{
		if(!stack.getItem().equals(other.stack.getItem()))
			return false;
		return use_od == other.use_od && ignore_dmg == other.ignore_dmg && ignore_nbt == other.ignore_nbt && use_category == other.use_category;
	}
	
	@Override
	public CraftingRequirement clone()
	{
		CraftingRequirement n = new CraftingRequirement();
		n.stack = stack.clone();
		n.use_od = use_od;
		n.ignore_dmg = ignore_dmg;
		n.ignore_nbt = ignore_nbt;
		n.use_category = use_category;
		return n;
	}
	
	public boolean isUnique()
	{
		return !use_od && !ignore_dmg && !ignore_nbt && !use_category;
	}
	
	public boolean testItem(ItemIdentifierStack other)
	{
		ItemStack stack_n = stack.makeNormalStack();
		ItemStack other_n = other.makeNormalStack();
		if(use_od || use_category)
		{
			int id1 = OreDictionary.getOreID(stack_n);
			int id2 = OreDictionary.getOreID(other_n);
			if(id1 == id2)
				return true;
			String nam1 = OreDictionary.getOreName(id1);
			String nam2 = OreDictionary.getOreName(id2);
			if(nam1.equals(nam2))
				return true;
			if(use_category)
			{
				nam1 = Pattern.compile("[A-Z].*").matcher(nam1).replaceFirst("");
				nam2 = Pattern.compile("[A-Z].*").matcher(nam2).replaceFirst("");
				if(nam1.equals(nam2))
					return true;
			}
		}
		if(stack_n.itemID != other_n.itemID)
			return false;
		if(stack_n.getItemDamage() != other_n.getItemDamage())
			if(stack_n.getHasSubtypes())
				return false;
			else if(!ignore_dmg)
				return false;
		if(ignore_nbt)
			return true;
		if(stack_n.hasTagCompound() ^ other_n.hasTagCompound())
			return false;
		if(!stack_n.hasTagCompound() && !other_n.hasTagCompound())
			return true;
		if(ItemStack.areItemStackTagsEqual(stack_n, other_n))
			return true;
		return false;
	}
}
