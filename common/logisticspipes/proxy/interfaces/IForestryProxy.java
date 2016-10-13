package logisticspipes.proxy.interfaces;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.recipes.CraftingParts;
import logisticspipes.utils.item.ItemIdentifier;

public interface IForestryProxy {

	public abstract boolean isBee(ItemStack item);

	public abstract boolean isBee(ItemIdentifier item);

	public abstract boolean isAnalysedBee(ItemStack item);

	public abstract boolean isAnalysedBee(ItemIdentifier item);

	public abstract boolean isKnownAlleleId(String uid, World world);

	public abstract String getAlleleName(String uid);

	public abstract boolean isTileAnalyser(TileEntity tile);

	public abstract String getFirstAlleleId(ItemStack bee);

	public abstract String getSecondAlleleId(ItemStack bee);

	public abstract String getNextAlleleId(String uid, World world);

	public abstract String getPrevAlleleId(String uid, World world);

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

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite getIconIndexForAlleleId(String uid, int phase);

	@SideOnly(Side.CLIENT)
	public abstract int getColorForAlleleId(String uid, int phase);

	@SideOnly(Side.CLIENT)
	public abstract int getRenderPassesForAlleleId(String uid);

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite getIconFromTextureManager(String name);

	public abstract void addCraftingRecipes(CraftingParts parts);

	public abstract void syncTracker(World world, EntityPlayer player);
}
