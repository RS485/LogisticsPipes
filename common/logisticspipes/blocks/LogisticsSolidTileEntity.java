package logisticspipes.blocks;

import logisticspipes.interfaces.IRotationProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class LogisticsSolidTileEntity extends TileEntity implements IRotationProvider {

	public int rotation = 0;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		rotation = nbt.getInteger("rotation");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("rotation", rotation);
	}
	
	@Override
	public int getRotation() {
		return rotation;
	}

	@Override
	public int getFrontTexture() {
		return 0;
	}

	@Override
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	
	public void notifyOfBlockChange() {}
}
