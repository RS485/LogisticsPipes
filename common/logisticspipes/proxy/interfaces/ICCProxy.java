package logisticspipes.proxy.interfaces;

import net.minecraft.src.TileEntity;
import buildcraft.api.core.Orientations;

public interface ICCProxy {
	public boolean isTurtle(TileEntity tile);
	public boolean isComputer(TileEntity tile);
	public boolean isCC();
	public Orientations getOrientation(Object computer, String computerSide, TileEntity pipe);
	public boolean isLuaThread(Thread thread);
}
