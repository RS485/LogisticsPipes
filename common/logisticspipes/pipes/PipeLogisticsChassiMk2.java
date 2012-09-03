package logisticspipes.pipes;

import logisticspipes.config.Textures;

public class PipeLogisticsChassiMk2 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk2(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI2_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 2;
	}
}
