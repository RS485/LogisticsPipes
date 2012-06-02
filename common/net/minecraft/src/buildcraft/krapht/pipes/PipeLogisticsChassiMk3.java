package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.core_LogisticsPipes;

public class PipeLogisticsChassiMk3 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk3(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_CHASSI3_TEXTURE;
	}
	
	@Override
	public int getChassiSize() {
		return 3;
	}
}
