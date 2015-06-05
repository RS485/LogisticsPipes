package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

public interface IThermalExpansionProxy {

	public boolean isTesseract(TileEntity tile);

	public List<TileEntity> getConnectedTesseracts(TileEntity tile);

	public boolean isTE();

	public ICraftingParts getRecipeParts();
}
