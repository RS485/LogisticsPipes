package net.minecraft.src.buildcraft.krapht.forestry;

import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.krapht.ItemIdentifier;

public interface IForestryProxy {
	
	public abstract boolean forestryEnabled();

	public abstract boolean isBee(ItemStack item);
	
	public abstract boolean isBee(ItemIdentifier item);
	
	public abstract boolean isAnalysedBee(ItemStack item);
	
	public abstract boolean isAnalysedBee(ItemIdentifier item);

	public abstract boolean isVaildAlleleId(int id);

	public abstract boolean isKnownAlleleId(int id, World world);
	
	public abstract String getAlleleName(int id);
	
	public abstract boolean isTileAnalyser(TileEntity tile);

	public abstract int getFirstAlleleId(ItemStack bee);

	public abstract int getSecondAlleleId(ItemStack bee);

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

	public abstract int getIconIndexForAlleleId(int id, int phase);

	public abstract int getColorForAlleleId(int id, int phase);
			
	public abstract int getRenderPassesForAlleleId(int id);
	
	public abstract void addCraftingRecipes();
}
