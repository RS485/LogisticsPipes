package net.minecraft.src.buildcraft.krapht.forestry;

import forestry.api.genetics.IAlleleSpecies;
import net.minecraft.src.Item;

public abstract class ForestrySideSide implements IForestryProxy {
	protected Item beeDroneGE;

	@Override
	public int getIconIndexForAlleleId(String uid, int phase) {
		if (!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies))
			return 0;
		IAlleleSpecies species = (IAlleleSpecies) forestry.api.genetics.AlleleManager.alleleRegistry
				.getAllele(uid);
		int indexOffset = 0;
		if (species != null) {
			indexOffset = 16 * species.getBodyType();
		}
		if (phase == 0)
			return indexOffset + 0 + 2;
		if (phase == 1) {
			return indexOffset + 3 + 2;
		}
		return indexOffset + 6 + 2;
	}

	@Override
	public int getColorForAlleleId(String uid, int phase) {
		if (!(forestry.api.genetics.AlleleManager.alleleRegistry.getAllele(uid) instanceof IAlleleSpecies))
			return 0;
		IAlleleSpecies species = (IAlleleSpecies) forestry.api.genetics.AlleleManager.alleleRegistry
				.getAllele(uid);
		if (species != null) {
			if (phase == 0)
				return species.getPrimaryColor();
			if (phase == 1) {
				return species.getSecondaryColor();
			}
			return 16777215;
		}

		return 16777215;
	}

	@Override
	public int getRenderPassesForAlleleId(String uid) {
		return 3;
	}
}
