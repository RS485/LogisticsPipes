package logisticspipes.proxy.interfaces;

import net.minecraft.src.TileEntity;
import buildcraft.api.core.Orientations;
import dan200.computer.api.IComputerAccess;

public interface ICCProxy {
	public boolean isTurtle(TileEntity tile);
	public boolean isComputer(TileEntity tile);
	public boolean isCC();
	public Orientations getOrientation(IComputerAccess computer, String computerSide, TileEntity pipe);
	public boolean isLuaThread(Thread thread);
}
