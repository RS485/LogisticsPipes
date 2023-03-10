package logisticspipes.pipes;

import net.minecraft.item.Item;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleFluidInsertion;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

public class PipeFluidInsertion extends FluidRoutedPipe {

	private final ModuleFluidInsertion moduleFluidInsertion;

	public PipeFluidInsertion(Item item) {
		super(item);
		moduleFluidInsertion = new ModuleFluidInsertion();
		moduleFluidInsertion.registerHandler(this, this);
		moduleFluidInsertion.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		moduleFluidInsertion.tick();
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_INSERTION;
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
