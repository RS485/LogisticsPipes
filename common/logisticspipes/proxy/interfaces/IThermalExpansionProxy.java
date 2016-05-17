package logisticspipes.proxy.interfaces;

import java.util.List;

import logisticspipes.recipes.CraftingParts;
import net.minecraft.tileentity.TileEntity;

public interface IThermalExpansionProxy {

	public boolean isTesseract(TileEntity tile);

	public List<TileEntity> getConnectedTesseracts(TileEntity tile);

	public boolean isTE();

	public CraftingParts getRecipeParts();
}
