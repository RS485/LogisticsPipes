package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;

public class PipeLogisticsChassiMk1 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk1(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return LogisticsPipes.LOGISTICSPIPE_CHASSI1_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 1;
	}

}
