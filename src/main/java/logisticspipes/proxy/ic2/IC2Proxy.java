package logisticspipes.proxy.ic2;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.MinecraftForge;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;

import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.interfaces.IIC2Proxy;
import logisticspipes.recipes.CraftingParts;

public class IC2Proxy implements IIC2Proxy {

	/**
	 * Adds crafting recipes to "IC2 Crafting"
	 */
	@Override
	public void addCraftingRecipes(CraftingParts parts) {
		/*
		CraftingRecipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_LV_SUPPLIER),
				new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_TRANSPORTATION), Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'), IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'),
						IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });

		CraftingRecipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_MV_SUPPLIER),
				new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_LV_SUPPLIER), Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'), IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'),
						IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });

		CraftingRecipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_HV_SUPPLIER),
				new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_MV_SUPPLIER), Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'), IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'),
						IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });

		CraftingRecipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_EV_SUPPLIER),
				new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_IC2_HV_SUPPLIER), Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'), IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'),
						IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });

		CraftingRecipes.advRecipes.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_IC2_POWERPROVIDER), new Object[] { "PSP", "OBO", "PTP", Character.valueOf('B'), Blocks.redstone_block, Character.valueOf('S'), IC2Items.getItem("energyStorageUpgrade"), Character.valueOf('O'),
				IC2Items.getItem("overclockerUpgrade"), Character.valueOf('T'), IC2Items.getItem("transformerUpgrade"), Character.valueOf('P'), Items.paper });
		*/
	}

	/**
	 * Registers an TileEntity to the IC2 EnergyNet
	 *
	 * @param tile to be an instance of IEnergyTile
	 */
	@Override
	public void registerToEneryNet(TileEntity tile) {
		if (MainProxy.isServer(tile.getWorld())) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile));
		}
	}

	/**
	 * Removes an TileEntity from the IC2 EnergyNet
	 *
	 * @param tile to be an instance of IEnergyTile
	 */
	@Override
	public void unregisterToEneryNet(TileEntity tile) {
		if (MainProxy.isServer(tile.getWorld())) {
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
	public boolean acceptsEnergyFrom(TileEntity energy, TileEntity tile, EnumFacing opposite) {
		return tile instanceof IEnergyEmitter && energy instanceof IEnergySink && ((IEnergySink) energy).acceptsEnergyFrom((IEnergyEmitter) tile, opposite);
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
	public double injectEnergyUnits(TileEntity tile, EnumFacing opposite, double d) {
		return ((IEnergySink) tile).injectEnergy(opposite, d, 1); //TODO check the voltage
	}
}
