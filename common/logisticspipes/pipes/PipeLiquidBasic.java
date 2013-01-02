package logisticspipes.pipes;

import net.minecraftforge.liquids.LiquidStack;
import logisticspipes.interfaces.routing.ILiquidSink;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.LiquidIdentifier;

public class PipeLiquidBasic extends LiquidRoutedPipe implements ILiquidSink {
	public PipeLiquidBasic(int itemID) {
		super(itemID);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_BASIC;
	}

	@Override
	public int sinkAmount(LiquidStack stack) {
		LiquidIdentifier ident = LiquidIdentifier.get(stack);
		String name = ident.getName();
		if(name.equals("water")) {
			return stack.amount;
		}
		return 0;
	}
}
