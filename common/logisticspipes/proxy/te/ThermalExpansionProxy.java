package logisticspipes.proxy.te;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.proxy.interfaces.ICraftingParts;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import logisticspipes.recipes.CraftingDependency;
import logisticspipes.recipes.RecipeManager;
import logisticspipes.recipes.RecipeManager.LocalCraftingManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thermalexpansion.block.TEBlocks;
import thermalexpansion.block.ender.TileTesseract;
import thermalexpansion.item.TEItems;
import cofh.api.energy.IEnergyHandler;
import cofh.api.transport.IEnderItemHandler;
import cofh.api.transport.RegistryEnderAttuned;

public class ThermalExpansionProxy implements IThermalExpansionProxy {
	
	@Override
	public boolean isTesseract(TileEntity tile) {
		return tile instanceof TileTesseract;
	}

	@Override
	public List<TileEntity> getConnectedTesseracts(TileEntity tile) {
		List<IEnderItemHandler> interfaces = RegistryEnderAttuned.getLinkedItemOutputs((TileTesseract)tile);
	    List<TileEntity> validOutputs = new LinkedList<TileEntity>();
	    if(interfaces == null) return validOutputs;
	    for (IEnderItemHandler object: interfaces) {
	    	if(object.canReceiveItems() && object.canSendItems() && object instanceof TileEntity) {
	    		validOutputs.add((TileEntity) object);
	    	}
	    }
	    return validOutputs;
	}

	@Override
	public boolean isTE() {
		return true;
	}

	@Override
	public int getMaxEnergyStored(TileEntity tile, ForgeDirection opposite) {
		return ((IEnergyHandler)tile).getMaxEnergyStored(opposite);
	}

	@Override
	public boolean isEnergyHandler(TileEntity tile) {
		return tile instanceof IEnergyHandler;
	}

	@Override
	public int getEnergyStored(TileEntity tile, ForgeDirection opposite) {
		return ((IEnergyHandler)tile).getEnergyStored(opposite);
	}

	@Override
	public boolean canConnectEnergy(TileEntity tile, ForgeDirection opposite) {
		return ((IEnergyHandler)tile).canConnectEnergy(opposite);
	}

	@Override
	public int receiveEnergy(TileEntity tile, ForgeDirection opposite, int i, boolean b) {
		return ((IEnergyHandler)tile).receiveEnergy(opposite, i, b);
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
