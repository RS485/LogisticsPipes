package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.core_LogisticsPipes;

public class PipeLogisticsChassiMk1 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk1(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_CHASSI1_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 1;
	}

}
