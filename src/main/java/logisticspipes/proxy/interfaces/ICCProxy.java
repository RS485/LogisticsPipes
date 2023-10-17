package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.recipes.CraftingParts;

public interface ICCProxy {

	boolean isTurtle(TileEntity tile);

	boolean isComputer(TileEntity tile);

	boolean isCC();

	boolean isLuaThread(Thread thread);

	void queueEvent(String event, Object[] arguments, LogisticsTileGenericPipe logisticsTileGenericPipe);

	void setTurtleConnect(boolean flag, LogisticsTileGenericPipe logisticsTileGenericPipe);

	boolean getTurtleConnect(LogisticsTileGenericPipe logisticsTileGenericPipe);

	int getLastCCID(LogisticsTileGenericPipe logisticsTileGenericPipe);

	void handleMesssage(int computerId, Object message, LogisticsTileGenericPipe tile, int sourceId);

	void addCraftingRecipes(CraftingParts parts);

	Object getAnswer(Object object);
}
