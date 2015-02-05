package logisticspipes.proxy.cofh;

import thermalexpansion.block.TEBlocks;
import thermalexpansion.item.TEItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.proxy.cofh.subproxies.ICoFHEnergyReceiver;
import logisticspipes.proxy.interfaces.ICoFHPowerProxy;
import logisticspipes.proxy.interfaces.ICraftingParts;
import logisticspipes.recipes.CraftingDependency;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.RecipeManager.LocalCraftingManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyReceiver;

public class CoFHPowerProxy implements ICoFHPowerProxy {
	
	@Override
	public boolean isEnergyReceiver(TileEntity tile) {
		return tile instanceof IEnergyReceiver;
	}
	
	@Override
	public ICoFHEnergyReceiver getEnergyReceiver(TileEntity tile) {
		final IEnergyReceiver handler = (IEnergyReceiver) tile;
		return new ICoFHEnergyReceiver() {
			@Override
			public int getMaxEnergyStored(ForgeDirection opposite) {
				return handler.getMaxEnergyStored(opposite);
			}

			@Override
			public int getEnergyStored(ForgeDirection opposite) {
				return handler.getEnergyStored(opposite);
			}

			@Override
			public boolean canConnectEnergy(ForgeDirection opposite) {
				return handler.canConnectEnergy(opposite);
			}

			@Override
			public int receiveEnergy(ForgeDirection opposite, int i, boolean b) {
				return handler.receiveEnergy(opposite, i, b);
			}
		};
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void addCraftingRecipes(ICraftingParts parts) {
		LocalCraftingManager craftingManager = RecipeManager.craftingManager;
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_RF_SUPPLIER), CraftingDependency.Power_Distribution, new Object[] { 
			false, 
			"PEP", 
			"RBR", 
			"PTP", 
			Character.valueOf('B'), new ItemStack(LogisticsPipes.UpgradeItem, 1, ItemUpgrade.POWER_TRANSPORTATION),
			Character.valueOf('P'), Items.paper,
			Character.valueOf('E'), new ItemStack(TEBlocks.blockDynamo, 1, 0), 
			Character.valueOf('T'), TEItems.powerCoilSilver, 
			Character.valueOf('R'), TEItems.powerCoilGold
		});
		craftingManager.addRecipe(new ItemStack(LogisticsPipes.LogisticsSolidBlock, 1, LogisticsSolidBlock.LOGISTICS_RF_POWERPROVIDER), CraftingDependency.Power_Distribution, new Object[] { 
			false, 
			"PEP", 
			"RBR", 
			"PTP", 
			Character.valueOf('B'), Blocks.redstone_block,
			Character.valueOf('P'), Items.paper,
			Character.valueOf('E'), new ItemStack(TEBlocks.blockDynamo, 1, 0), 
			Character.valueOf('T'), TEItems.powerCoilSilver, 
			Character.valueOf('R'), TEItems.powerCoilGold
		});
	}
}
