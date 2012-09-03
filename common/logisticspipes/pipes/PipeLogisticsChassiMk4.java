package logisticspipes.pipes;

import logisticspipes.config.Textures;

public class PipeLogisticsChassiMk4 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk4(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI4_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 4;
	}


}
