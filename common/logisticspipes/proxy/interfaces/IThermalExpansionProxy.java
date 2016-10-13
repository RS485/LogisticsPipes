package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.recipes.CraftingParts;

public interface IThermalExpansionProxy {

	boolean isTesseract(TileEntity tile);

	List<TileEntity> getConnectedTesseracts(TileEntity tile);

	boolean isTE();

	CraftingParts getRecipeParts();
}
