package logisticspipes.proxy.interfaces;

import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public interface ICCProxy {
	public boolean isTurtle(TileEntity tile);
	public boolean isComputer(TileEntity tile);
	public boolean isCC();
	public ForgeDirection getOrientation(Object computer, String computerSide, TileEntity pipe);
	public boolean isLuaThread(Thread thread);
}
