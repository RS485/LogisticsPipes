package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.core_LogisticsPipes;

public class PipeLogisticsChassiMk4 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk4(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_CHASSI4_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 4;
	}


}
