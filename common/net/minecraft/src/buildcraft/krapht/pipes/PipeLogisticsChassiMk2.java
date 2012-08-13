package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.mod_LogisticsPipes;

public class PipeLogisticsChassiMk2 extends PipeLogisticsChassi{

	public PipeLogisticsChassiMk2(int itemID) {
		super(itemID);
	}

	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_CHASSI2_TEXTURE;
	}

	@Override
	public int getChassiSize() {
		return 2;
	}
}
