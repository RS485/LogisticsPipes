package logisticspipes.proxy.ic2;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.config.Configs;
import logisticspipes.items.ItemModule;
import logisticspipes.items.ItemPipeComponents;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.interfaces.ICraftingParts;
import logisticspipes.proxy.interfaces.IIC2Proxy;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.IC2Items;
import ic2.api.item.IElectricItem;
import ic2.api.recipe.Recipes;

public class IC2Proxy implements IIC2Proxy {

	/**
	 * @return Boolean, true if itemstack is a ic2 electric item.
	 * @param stack
	 *            The stack to check.
	 */
	@Override
	public boolean isElectricItem(ItemStack stack) {
		return stack != null && (stack.getItem() instanceof IElectricItem);
	}

	/**
	 * @return Boolean, true if stack is the same type of ic2 electric item as
	 *         template.
	 * @param stack
	 *            The stack to check
	 * @param template
	 *            The stack to compare to
	 */
	@Override
	public boolean isSimilarElectricItem(ItemStack stack, ItemStack template) {
		if (stack == null || template == null || !isElectricItem(template)) {
			return false;
		}
		if (((IElectricItem) template.getItem()).getEmptyItem(stack) == stack.getItem()) {
			return true;
		}
		if (((IElectricItem) template.getItem()).getChargedItem(stack) == stack.getItem()) {
			return true;
		}
		return false;
	}

	/**
	 * @return Int value of current charge on electric item.
	 * @param stack
	 *            The stack to get charge for.
	 */
	private double getCharge(ItemStack stack) {
		if ((stack.getItem() instanceof IElectricItem) && stack.hasTagCompound()) {
			return stack.getTagCompound().getDouble("charge");
		} else {
			return 0;
		}
	}

	/**
	 * @return Int value of maximum charge on electric item.
	 * @param stack
	 *            The stack to get max charge for.
	 */
	private double getMaxCharge(ItemStack stack) {
		if (!(stack.getItem() instanceof IElectricItem)) {
			return 0;
		}
		return ((IElectricItem) stack.getItem()).getMaxCharge(stack);
	}

	/**
	 * @return Boolean, true if electric item is fully charged.
	 * @param stack
	 *            The stack to check if its fully charged.
	 */
	@Override
	public boolean isFullyCharged(ItemStack stack) {
		if (!isElectricItem(stack)) {
			return false;
		}
		if (((IElectricItem) stack.getItem()).getChargedItem(stack) != stack.getItem()) {
			return false;
		}
		double charge = getCharge(stack);
		double maxCharge = getMaxCharge(stack);
		return charge == maxCharge;
	}

	/**
	 * @return Boolean, true if electric item is fully discharged.
	 * @param stack
	 *            The stack to check if its fully discharged.
	 */
	@Override
	public boolean isFullyDischarged(ItemStack stack) {
		if (!isElectricItem(stack)) {
			return false;
		}
		if (((IElectricItem) stack.getItem()).getEmptyItem(stack) != stack.getItem()) {
			return false;
		}
		double charge = getCharge(stack);
		return charge == 0;
	}

	/**
	 * @return Boolean, true if electric item contains charge but is not full.
	 * @param stack
	 *            The stack to check if its partially charged.
	 */
	@Override
	public boolean isPartiallyCharged(ItemStack stack) {
		if (!isElectricItem(stack)) {
			return false;
		}
		if (((IElectricItem) stack.getItem()).getChargedItem(stack) != stack.getItem()) {
			return false;
		}
		double charge = getCharge(stack);
		double maxCharge = getMaxCharge(stack);
		return charge != maxCharge;
	}

	/**
	 * Adds crafting recipes to "IC2 Crafting"
	 */
	@Override
	public void addCraftingRecipes(ICraftingParts parts) {
		if (!Configs.ENABLE_BETA_RECIPES) {
			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICBUFFER), new Object[] { "CGC", "rBr", "CrC", Character.valueOf('C'), IC2Items.getItem("advancedCircuit"), Character.valueOf('G'), parts.getGearTear2(), Character.valueOf('r'), Items.redstone, Character.valueOf('B'),
					new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICBUFFER), new Object[] { " G ", "rBr", "CrC", Character.valueOf('C'), IC2Items.getItem("advancedCircuit"), Character.valueOf('G'), parts.getChipTear2(), Character.valueOf('r'), Items.redstone, Character.valueOf('B'),
					new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { "CGD", "rBr", "DrC", Character.valueOf('C'), IC2Items.getItem("electronicCircuit"), Character.valueOf('D'), IC2Items.getItem("reBattery"), Character.valueOf('G'), parts.getGearTear2(),
					Character.valueOf('r'), Items.redstone, Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { "CGD", "rBr", "DrC", Character.valueOf('C'), IC2Items.getItem("electronicCircuit"), Character.valueOf('D'), IC2Items.getItem("chargedReBattery"), Character.valueOf('G'),
					parts.getGearTear2(), Character.valueOf('r'), Items.redstone, Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER),
					new Object[] { "CGc", "rBr", "DrC", Character.valueOf('C'), IC2Items.getItem("electronicCircuit"), Character.valueOf('c'), IC2Items.getItem("reBattery"), Character.valueOf('D'), IC2Items.getItem("chargedReBattery"), Character.valueOf('G'), parts.getGearTear2(), Character.valueOf('r'),
							Items.redstone, Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER),
					new Object[] { "CGc", "rBr", "DrC", Character.valueOf('C'), IC2Items.getItem("electronicCircuit"), Character.valueOf('c'), IC2Items.getItem("chargedReBattery"), Character.valueOf('D'), IC2Items.getItem("reBattery"), Character.valueOf('G'), parts.getGearTear2(), Character.valueOf('r'),
							Items.redstone, Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { " G ", "rBr", "DrC", Character.valueOf('C'), IC2Items.getItem("electronicCircuit"), Character.valueOf('D'), IC2Items.getItem("reBattery"), Character.valueOf('G'), parts.getChipTear2(),
					Character.valueOf('r'), Items.redstone, Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER), new Object[] { " G ", "rBr", "DrC", Character.valueOf('C'), IC2Items.getItem("electronicCircuit"), Character.valueOf('D'), IC2Items.getItem("chargedReBattery"), Character.valueOf('G'),
					parts.getChipTear2(), Character.valueOf('r'), Items.redstone, Character.valueOf('B'), new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_LV_SUPPLIER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_TRANSPORTATION), Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'), IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'),
							IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_MV_SUPPLIER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_LV_SUPPLIER), Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'), IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'),
							IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_HV_SUPPLIER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_MV_SUPPLIER), Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'), IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'),
							IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_EV_SUPPLIER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_HV_SUPPLIER), Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'), IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'),
							IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_IC2_POWERPROVIDER), new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), Blocks.redstone_block, Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'),
					IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'), IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });
		}
		if (Configs.ENABLE_BETA_RECIPES) {
			ItemStack packager = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_MICROPACKAGER);
			ItemStack expand = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_LOGICEXPANDER);
			ItemStack lense = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_FOCUSLENSE);
			ItemStack accept = new ItemStack(LogisticsPipes.LogisticsPipeComponents, 1, ItemPipeComponents.ITEM_POWERACCEPT);

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICBUFFER), new Object[] { "CGC", "rBr", "CrC", Character.valueOf('C'), IC2Items.getItem("advancedCircuit"), Character.valueOf('G'), packager, Character.valueOf('r'), Items.redstone, Character.valueOf('B'),
					new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.ELECTRICMANAGER),
					new Object[] { "CGD", "rBr", "DrC", Character.valueOf('C'), IC2Items.getItem("electronicCircuit"), Character.valueOf('D'), IC2Items.getItem("reBattery"), Character.valueOf('G'), packager, Character.valueOf('r'), Items.redstone, Character.valueOf('B'),
							new ItemStack(LogisticsPipes.ModuleItem, 1, ItemModule.BLANK) });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_LV_SUPPLIER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), expand, Character.valueOf('S'), accept, Character.valueOf('O'), IC2Items.getItem("coil"), Character.valueOf('T'), IC2Items.getItem("reBattery"), Character.valueOf('P'), Items.paper });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_MV_SUPPLIER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), expand, Character.valueOf('S'), accept, Character.valueOf('O'), IC2Items.getItem("coil"), Character.valueOf('T'), IC2Items.getItem("advBattery"), Character.valueOf('P'), Items.paper });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_HV_SUPPLIER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), expand, Character.valueOf('S'), accept, Character.valueOf('O'), IC2Items.getItem("coil"), Character.valueOf('T'), IC2Items.getItem("energyCrystal"), Character.valueOf('P'), Items.paper });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_EV_SUPPLIER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), expand, Character.valueOf('S'), accept, Character.valueOf('O'), IC2Items.getItem("coil"), Character.valueOf('T'), IC2Items.getItem("lapotronCrystal"), Character.valueOf('P'), Items.paper });

			Recipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_IC2_POWERPROVIDER),
					new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), Blocks.glowstone, Character.valueOf('S'), lense, Character.valueOf('O'), IC2Items.getItem("coil"), Character.valueOf('T'), IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.iron_ingot });
		}
	}

	/**
	 * Registers an TileEntity to the IC2 EnergyNet
	 * 
	 * @param has
	 *            to be an instance of IEnergyTile
	 */
	@Override
	public void registerToEneryNet(TileEntity tile) {
		if (MainProxy.isServer(tile.getWorldObj())) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile));
		}
	}

	/**
	 * Removes an TileEntity from the IC2 EnergyNet
	 * 
	 * @param has
	 *            to be an instance of IEnergyTile
	 */
	@Override
	public void unregisterToEneryNet(TileEntity tile) {
		if (MainProxy.isServer(tile.getWorldObj())) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile));
		}
	}

	/**
	 * @return If IC2 is loaded, returns true.
	 */
	@Override
	public boolean hasIC2() {
		return true;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity energy, TileEntity tile, ForgeDirection opposite) {
		return ((IEnergySink) energy).acceptsEnergyFrom(tile, opposite);
	}

	@Override
	public boolean isEnergySink(TileEntity tile) {
		return tile instanceof IEnergySink;
	}

	@Override
	public double demandedEnergyUnits(TileEntity tile) {
		return ((IEnergySink) tile).getDemandedEnergy();
	}

	@Override
	public double injectEnergyUnits(TileEntity tile, ForgeDirection opposite, double d) {
		return ((IEnergySink) tile).injectEnergy(opposite, d, 1); //TODO check the voltage
	}
}
