package logisticspipes.proxy.ic2;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.IElectricItem;
import ic2.api.item.Items;
import ic2.api.recipe.Recipes;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemModule;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;


public class IC2Proxy implements IIC2Proxy {

	/**
	 * @return Boolean, true if itemstack is a ic2 electric item.
	 * @param stack The stack to check.
	 */
	@Override
	public boolean isElectricItem(ItemStack stack) {
		return stack != null && (stack.getItem() instanceof IElectricItem);
	}

	/**
	 * @return Boolean, true if stack is the same type of ic2 electric item as template.
	 * @param stack The stack to check
	 * @param template The stack to compare to
	 */
	@Override
	public boolean isSimilarElectricItem(ItemStack stack, ItemStack template) {
		if (stack == null || template == null || !isElectricItem(template)) return false;
		if (((IElectricItem) template.getItem()).getEmptyItemId(stack) == stack.itemID) return true;
		if (((IElectricItem) template.getItem()).getChargedItemId(stack) == stack.itemID) return true;
		return false;
	}

	/**
	 * @return Int value of current charge on electric item.
	 * @param stack The stack to get charge for.
	 */
	private int getCharge(ItemStack stack) {
		if ((stack.getItem() instanceof IElectricItem) && stack.hasTagCompound()) {
			return stack.getTagCompound().getInteger("charge");
		} else {
			return 0;
		}
	}

	/**
	 * @return Int value of maximum charge on electric item.
	 * @param stack The stack to get max charge for.
	 */
	private int getMaxCharge(ItemStack stack) {
		if (!(stack.getItem() instanceof IElectricItem)) return 0;
		return ((IElectricItem) stack.getItem()).getMaxCharge(stack);
	}

	/**
	 * @return Boolean, true if electric item is fully charged.
	 * @param stack The stack to check if its fully charged.
	 */
	@Override
	public boolean isFullyCharged(ItemStack stack) {
		if (!isElectricItem(stack)) return false;
		if (((IElectricItem) stack.getItem()).getChargedItemId(stack) != stack.itemID) return false;
		int charge = getCharge(stack);
		int maxCharge = getMaxCharge(stack);
		return charge == maxCharge;
	}
	
	/**
	 * @return Boolean, true if electric item is fully discharged.
	 * @param stack The stack to check if its fully discharged.
	 */
	@Override
	public boolean isFullyDischarged(ItemStack stack) {
		if (!isElectricItem(stack)) return false;
		if (((IElectricItem) stack.getItem()).getEmptyItemId(stack) != stack.itemID) return false;
		int charge = getCharge(stack);
		return charge == 0;
	}
	
	/**
	 * @return Boolean, true if electric item contains charge but is not full.
	 * @param stack The stack to check if its partially charged.
	 */
	@Override
	public boolean isPartiallyCharged(ItemStack stack) {
		if (!isElectricItem(stack)) return false;
		if (((IElectricItem) stack.getItem()).getChargedItemId(stack) != stack.itemID) return false;
		int charge = getCharge(stack);
		int maxCharge = getMaxCharge(stack);
		return charge != maxCharge;
	}
	
	/**
	 * Adds crafting recipes to "IC2 Crafting"
	 */
	@Override
	public void addCraftingRecipes() {
		Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICBUFFER), new Object[] { 
			"CGC", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), Items.getItem("advancedCircuit"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICBUFFER), new Object[] { 
			" G ", 
			"rBr", 
			"CrC", 
			Character.valueOf('C'), Items.getItem("advancedCircuit"),
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});


		Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			"CGD", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('D'), Items.getItem("reBattery"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			"CGD", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('D'), Items.getItem("chargedReBattery"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			"CGc", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('c'), Items.getItem("reBattery"),
			Character.valueOf('D'), Items.getItem("chargedReBattery"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			"CGc", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('c'), Items.getItem("chargedReBattery"),
			Character.valueOf('D'), Items.getItem("reBattery"),
			Character.valueOf('G'), BuildCraftCore.goldGearItem,
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});

		Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			" G ", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('D'), Items.getItem("reBattery"),
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
		
		Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { 
			" G ", 
			"rBr", 
			"DrC", 
			Character.valueOf('C'), Items.getItem("electronicCircuit"),
			Character.valueOf('D'), Items.getItem("chargedReBattery"),
			Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
			Character.valueOf('r'), Item.redstone,
			Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK)
		});
	}
	
	/**
	 * Registers an TileEntity to the IC2 EnergyNet
	 * @param has to be an instance of IEnergyTile
	*/
	@Override
	public void registerToEneryNet(TileEntity tile) {
		MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile));
	}

	/**
	 * Removes an TileEntity from the IC2 EnergyNet
	 * @param has to be an instance of IEnergyTile
	*/
	@Override
	public void unregisterToEneryNet(TileEntity tile) {
		MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile));
	}

	/**
	 * @return If IC2 is loaded, returns true.
	 */
	@Override
	public boolean hasIC2() {
		return true;
	}}
