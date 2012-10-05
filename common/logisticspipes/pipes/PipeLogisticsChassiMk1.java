package logisticspipes.pipes;

import logisticspipes.config.Textures;

public class PipeLogisticsChassiMk1 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk1(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI1_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 1;
	}
}
