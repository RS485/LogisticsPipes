package logisticspipes.proxy.interfaces;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public interface ICCProxy {
	public boolean isTurtle(TileEntity tile);
	public boolean isComputer(TileEntity tile);
	public boolean isCC();
	public ForgeDirection getOrientation(Object computer, TileEntity pipe);
	public boolean isLuaThread(Thread thread);
	public void queueEvent(String event, Object[] arguments, LogisticsTileGenericPipe logisticsTileGenericPipe);
	public void setTurtrleConnect(boolean flag, LogisticsTileGenericPipe logisticsTileGenericPipe);
	public boolean getTurtrleConnect(LogisticsTileGenericPipe logisticsTileGenericPipe);
	public int getLastCCID(LogisticsTileGenericPipe logisticsTileGenericPipe);
}
