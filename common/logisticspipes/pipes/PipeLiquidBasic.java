package logisticspipes.pipes;

import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

public class PipeLiquidBasic extends LiquidRoutedPipe {

	public PipeLiquidBasic(int itemID) {
		super(itemID);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_BASIC;
	}

}
