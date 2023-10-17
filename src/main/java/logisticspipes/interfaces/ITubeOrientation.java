package logisticspipes.interfaces;

import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.utils.IPositionRotateble;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface ITubeOrientation {

	ITubeRenderOrientation getRenderOrientation();

	void rotatePositions(IPositionRotateble set);

	DoubleCoordinates getOffset();

	void setOnPipe(CoreMultiBlockPipe pipe);
}
