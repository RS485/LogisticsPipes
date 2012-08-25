package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;

public class PipeLogisticsChassiMk5 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk5(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return LogisticsPipes.LOGISTICSPIPE_CHASSI5_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 8;
	}


}
