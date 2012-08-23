package net.minecraft.src.buildcraft.krapht.forestry;

import net.minecraft.src.Item;

public abstract class ForestrySideSide implements IForestryProxy {

	protected Item beeDroneGE;
	@Override
	public int getIconIndexForAlleleId(int id, int phase) {
		return 0;
	}

	@Override
	public int getColorForAlleleId(int id, int phase) {
		return 0;
	}

	@Override
	public int getRenderPassesForAlleleId(int id) {
		return 0;
	}
}
