package logisticspipes.proxy.interfaces;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public interface IForestryProxy {
	
	public abstract boolean forestryEnabled();

	public abstract boolean isBee(ItemStack item);
	
	public abstract boolean isBee(ItemIdentifier item);
	
	public abstract boolean isAnalysedBee(ItemStack item);
	
	public abstract boolean isAnalysedBee(ItemIdentifier item);

	public abstract boolean isKnownAlleleId(String uid, World world);
	
	public abstract String getAlleleName(String uid);
	
	public abstract boolean isTileAnalyser(TileEntity tile);

	public abstract String getFirstAlleleId(ItemStack bee);

	public abstract String getSecondAlleleId(ItemStack bee);
	
	public abstract String getNextAlleleId(String uid);

	public abstract String getPrevAlleleId(String uid);

	public abstract boolean isDrone(ItemStack bee);

	public abstract boolean isFlyer(ItemStack bee);

	public abstract boolean isPrincess(ItemStack bee);

	public abstract boolean isQueen(ItemStack bee);

	public abstract boolean isPurebred(ItemStack bee);

	public abstract boolean isNocturnal(ItemStack bee);

	public abstract boolean isPureNocturnal(ItemStack bee);

	public abstract boolean isPureFlyer(ItemStack bee);

	public abstract boolean isCave(ItemStack bee);

	public abstract boolean isPureCave(ItemStack bee);
	
	public abstract String getForestryTranslation(String input);

	public abstract int getIconIndexForAlleleId(String uid, int phase);

	public abstract int getColorForAlleleId(String uid, int phase);
			
	public abstract int getRenderPassesForAlleleId(String uid);
	
	public abstract void addCraftingRecipes();
}
