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

import logisticspipes.recipes.CraftingParts;
import logisticspipes.utils.item.ItemIdentifier;

public interface IForestryProxy {

	boolean isBee(ItemStack item);

	boolean isBee(ItemIdentifier item);

	boolean isAnalysedBee(ItemStack item);

	boolean isAnalysedBee(ItemIdentifier item);

	boolean isKnownAlleleId(String uid, World world);

	String getAlleleName(String uid);

	boolean isTileAnalyser(TileEntity tile);

	String getFirstAlleleId(ItemStack bee);

	String getSecondAlleleId(ItemStack bee);

	String getNextAlleleId(String uid, World world);

	String getPrevAlleleId(String uid, World world);

	boolean isDrone(ItemStack bee);

	boolean isFlyer(ItemStack bee);

	boolean isPrincess(ItemStack bee);

	boolean isQueen(ItemStack bee);

	boolean isPurebred(ItemStack bee);

	boolean isNocturnal(ItemStack bee);

	boolean isPureNocturnal(ItemStack bee);

	boolean isPureFlyer(ItemStack bee);

	boolean isCave(ItemStack bee);

	boolean isPureCave(ItemStack bee);

	String getForestryTranslation(String input);

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite getIconIndexForAlleleId(String uid, int phase);

	@SideOnly(Side.CLIENT)
	int getColorForAlleleId(String uid, int phase);

	@SideOnly(Side.CLIENT)
	int getRenderPassesForAlleleId(String uid);

	@SideOnly(Side.CLIENT)
	public abstract TextureAtlasSprite getIconFromTextureManager(String name);

	void addCraftingRecipes(CraftingParts parts);

	void syncTracker(World world, EntityPlayer player);
}
