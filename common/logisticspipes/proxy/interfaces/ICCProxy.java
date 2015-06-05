package logisticspipes.proxy.interfaces;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.tileentity.TileEntity;

public interface ICCProxy {

	public boolean isTurtle(TileEntity tile);

	public boolean isComputer(TileEntity tile);

	public boolean isCC();

	public boolean isLuaThread(Thread thread);

	public void queueEvent(String event, Object[] arguments, LogisticsTileGenericPipe logisticsTileGenericPipe);

	public void setTurtleConnect(boolean flag, LogisticsTileGenericPipe logisticsTileGenericPipe);

	public boolean getTurtleConnect(LogisticsTileGenericPipe logisticsTileGenericPipe);

	public int getLastCCID(LogisticsTileGenericPipe logisticsTileGenericPipe);

	public void handleMesssage(int computerId, Object message, LogisticsTileGenericPipe tile, int sourceId);

	public void addCraftingRecipes(ICraftingParts parts);

	public Object getAnswer(Object object);
}
