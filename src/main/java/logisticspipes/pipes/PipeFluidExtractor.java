package logisticspipes.pipes;

import net.minecraft.item.Item;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleFluidExtractor;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

public class PipeFluidExtractor extends FluidRoutedPipe {

	private final ModuleFluidExtractor moduleFluidExtractor;

	public PipeFluidExtractor(Item item) {
		super(item);
		moduleFluidExtractor = new ModuleFluidExtractor();
		moduleFluidExtractor.registerHandler(this, this);
		moduleFluidExtractor.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return this.moduleFluidExtractor;
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (!isNthTick(10)) {
			return;
		}
		moduleFluidExtractor.tick();
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_EXTRACTOR;
	}

	@Override
	public boolean canInsertToTanks() {
		return false;
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return false;
	}

	@Override
	public boolean canReceiveFluid() {
		return true;
	}
}
