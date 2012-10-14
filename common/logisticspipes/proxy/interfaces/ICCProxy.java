package logisticspipes.proxy.interfaces;

import buildcraft.api.core.Orientations;
import dan200.computer.api.IComputerAccess;
import net.minecraft.src.TileEntity;

public interface ICCProxy {
	public boolean isTurtle(TileEntity tile);
	public boolean isComputer(TileEntity tile);
	public boolean isCC();
	public Orientations getOrientation(IComputerAccess computer, String computerSide, TileEntity pipe);
}
