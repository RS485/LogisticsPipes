package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.asm.IgnoreDisabledProxy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

public interface IBCTilePart {

	boolean hasBlockingPluggable(EnumFacing side);

	@IgnoreDisabledProxy
	void writeToNBT_LP(NBTTagCompound nbt);

	@IgnoreDisabledProxy
	void readFromNBT_LP(NBTTagCompound nbt);

	boolean isSolidOnSide(EnumFacing side);

	void invalidate_LP();

	void validate_LP();

	void updateEntity_LP();

	void scheduleNeighborChange();

	boolean hasGate(EnumFacing orientation);

	IBCRenderState getBCRenderState();

	IBCPipePart getBCPipePart();

	IBCPluggableState getBCPlugableState();

	boolean hasEnabledFacade(EnumFacing dir);

	IBCPipePluggable getBCPipePluggable(EnumFacing sideHit);

	void readOldRedStone(NBTTagCompound nbt);

	void afterStateUpdated();

	Object getOriginal();

	boolean hasPipePluggable(EnumFacing dir);

	void setWorldObj_LP(World world);
}
