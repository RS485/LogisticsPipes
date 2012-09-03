package logisticspipes.pipes;

import logisticspipes.config.Textures;

public class PipeLogisticsChassiMk3 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk3(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_CHASSI3_TEXTURE;
	}
	
	@Override
	public int getChassiSize() {
		return 3;
	}
}
