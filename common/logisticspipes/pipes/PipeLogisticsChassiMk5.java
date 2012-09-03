package logisticspipes.pipes;

import logisticspipes.config.Textures;

public class PipeLogisticsChassiMk5 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk5(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI5_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 8;
	}


}
