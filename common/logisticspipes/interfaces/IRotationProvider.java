package logisticspipes.interfaces;

import net.minecraft.util.math.Direction;

public interface IRotationProvider {

	@Deprecated
	default int getRotation() {
		switch (getFacing()) {
			case WEST:
				return 0;
			case EAST:
				return 1;
			case NORTH:
				return 2;
			case SOUTH:
				return 3;
		}
		return 0;
	}

	Direction getFacing();

	@Deprecated
	default void setRotation(int rotation) {
		switch (rotation) {
			case 0:
				setFacing(Direction.WEST);
				break;
			case 1:
				setFacing(Direction.EAST);
				break;
			case 2:
				setFacing(Direction.NORTH);
				break;
			case 3:
				setFacing(Direction.SOUTH);
				break;
		}
	}

	void setFacing(Direction facing);

}
