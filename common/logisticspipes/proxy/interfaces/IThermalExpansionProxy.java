package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.recipes.CraftingParts;

public interface IThermalExpansionProxy {

	boolean isTE();

	CraftingParts getRecipeParts();
}
