package net.minecraft.src.buildcraft.krapht.forestry;

import net.minecraft.src.Item;

public abstract class ForestrySideSide implements IForestryProxy {
	protected Item beeDroneGE;
	@Override
	public int getIconIndexForAlleleId(int id, int phase) {
		if(forestry.api.genetics.AlleleManager.alleleList[id] == null) return 0;
		return beeDroneGE.getIconFromDamageForRenderPass(forestry.api.genetics.AlleleManager.alleleList[id].getId(), phase);
	}

	@Override
	public int getColorForAlleleId(int id, int phase) {
		if(forestry.api.genetics.AlleleManager.alleleList[id] == null) return 0;
		return beeDroneGE.getColorFromDamage(forestry.api.genetics.AlleleManager.alleleList[id].getId(), phase);
	}

	@Override
	public int getRenderPassesForAlleleId(int id) {
		if(forestry.api.genetics.AlleleManager.alleleList[id] == null) return 0;
		return beeDroneGE.getRenderPasses(forestry.api.genetics.AlleleManager.alleleList[id].getId());
	}
}
