package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.asm.IgnoreDisabledProxy;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public interface IBCTilePart {
	void refreshRenderState();
	boolean hasFacade(ForgeDirection direction);
	ItemStack getFacade(ForgeDirection direction);
	boolean hasPlug(ForgeDirection side);
	boolean hasRobotStation(ForgeDirection side);
	boolean addPlug(ForgeDirection forgeDirection);
	boolean addRobotStation(ForgeDirection forgeDirection);
	@IgnoreDisabledProxy
	void writeToNBT(NBTTagCompound nbt);
	@IgnoreDisabledProxy
	void readFromNBT(NBTTagCompound nbt);
	void invalidate();
	void validate();
	Object getPluggables(int i);
	void updateEntity();
	boolean hasGate(ForgeDirection side);
	void setGate(Object makeGate, int i);
	boolean hasEnabledFacade(ForgeDirection dir);
	boolean dropSideItems(ForgeDirection sideHit);
	boolean hasBlockingPluggable(ForgeDirection side);
	Object getStation(ForgeDirection sideHit);
	boolean addGate(ForgeDirection side, Object makeGate);
	boolean addFacade(ForgeDirection direction, Object states);
}
