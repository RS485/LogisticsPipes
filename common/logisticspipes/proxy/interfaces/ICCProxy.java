package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public interface ICCProxy {
	public boolean isTurtle(TileEntity tile);
	public boolean isComputer(TileEntity tile);
	public boolean isCC();
	public ForgeDirection getOrientation(Object computer, int side, TileEntity pipe);
	public boolean isLuaThread(Thread thread);
}
