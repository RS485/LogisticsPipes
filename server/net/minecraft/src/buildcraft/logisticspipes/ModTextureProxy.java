package net.minecraft.src.buildcraft.logisticspipes;

import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.GuiHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import buildcraft.mod_BuildCraftTransport;
import buildcraft.core.CoreProxy;
import buildcraft.core.utils.Localization;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import cpw.mods.fml.client.SpriteHelper;


public abstract class ModTextureProxy {
	
	protected int index = 0;
	
	protected Item createPipe (int defaultID, Class <? extends Pipe> clas, String descr) {
		Item res =  BlockGenericPipe.registerPipe (defaultID, clas);
		res.setItemName(clas.getSimpleName());
		CoreProxy.addName(res, descr);
		MinecraftForgeClient.registerItemRenderer(res.shiftedIndex, mod_BuildCraftTransport.pipeItemRenderer);
		if(defaultID != mod_LogisticsPipes.LOGISTICSPIPE_BASIC_ID) {
			registerShapelessResetRecipe(res,0,mod_LogisticsPipes.LogisticsBasicPipe,0);
		}
		return res;
	}
	
	protected abstract void registerShapelessResetRecipe(Item res, int i, Item logisticsBasicPipe, int j);

	public int registerTexture(String fileName) {
		ModLoader.addOverride(mod_LogisticsPipes.BASE_TEXTURE_FILE, fileName, index);
		return index++;
	}
	
	public void initTextures() {
		String spirt = 	"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111" + 
		"1111111111111111";
		SpriteHelper.registerSpriteMapForFile(mod_LogisticsPipes.BASE_TEXTURE_FILE, spirt);
	}

	public void load() {
		MinecraftForge.setGuiHandler(this,new GuiHandler());
		
		MinecraftForgeClient.preloadTexture(mod_LogisticsPipes.LOGISTICSITEMS_TEXTURE_FILE);
		MinecraftForgeClient.preloadTexture(mod_LogisticsPipes.LOGISTICSACTIONTRIGGERS_TEXTURE_FILE);
	}
	
	public void modsLoaded() {
		Localization.addLocalization("/lang/logisticspipes/", "en_US");
	}
}
