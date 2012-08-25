package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;

public class PipeLogisticsChassiMk2 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk2(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return LogisticsPipes.LOGISTICSPIPE_CHASSI2_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 2;
	}
}
