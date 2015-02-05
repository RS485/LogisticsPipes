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
import thermalexpansion.block.TEBlocks;
import thermalexpansion.block.ender.TileTesseract;
import thermalexpansion.item.TEItems;
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
}
