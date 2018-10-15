package logisticspipes.interfaces;

import net.minecraft.util.EnumFacing;

public interface IRotationProvider {

	@Deprecated
	public int getRotation();

	default EnumFacing getFacing() {
		switch(getRotation()) {
			case 0:
				return EnumFacing.WEST;
			case 1:
				return EnumFacing.EAST;
			case 2:
				return EnumFacing.NORTH;
			case 3:
			default:
				return EnumFacing.SOUTH;
		}
	}

	@Deprecated
	public void setRotation(int rotation);

	default void setFacing(EnumFacing facing) {
		switch(facing) {
			case NORTH:
				setRotation(2);
				break;
			case DOWN:
			case UP:
			case SOUTH:
				setRotation(3);
				break;
			case WEST:
				setRotation(0);
				break;
			case EAST:
				setRotation(1);
				break;
		}
	}

	public int getFrontTexture();
}
