package logisticspipes.interfaces;

import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.utils.IPositionRotateble;
import logisticspipes.utils.tuples.LPPosition;

public interface ITubeOrientation {

	ITubeRenderOrientation getRenderOrientation();

	void rotatePositions(IPositionRotateble set);

	LPPosition getOffset();

	void setOnPipe(CoreMultiBlockPipe pipe);
}
