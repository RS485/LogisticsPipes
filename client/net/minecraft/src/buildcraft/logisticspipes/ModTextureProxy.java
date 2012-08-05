package net.minecraft.src.buildcraft.logisticspipes;

import cpw.mods.fml.client.SpriteHelper;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.core_LogisticsPipes;
import buildcraft.mod_BuildCraftTransport;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.core.CoreProxy;
import buildcraft.core.utils.Localization;
import net.minecraft.src.buildcraft.krapht.GuiHandler;
import net.minecraft.src.buildcraft.krapht.network.ConnectionHandler;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.MinecraftForgeClient;


public abstract class ModTextureProxy extends core_LogisticsPipes {
	
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
	
	public int registerTexture(String fileName) {
		ModLoader.addOverride(BASE_TEXTURE_FILE, fileName, index);
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
		SpriteHelper.registerSpriteMapForFile(BASE_TEXTURE_FILE, spirt);
	}
	
	@Override
	public void load() {
		super.load();
		MinecraftForge.registerConnectionHandler(new ConnectionHandler());
		
		MinecraftForge.setGuiHandler(this,new GuiHandler());
		
		MinecraftForgeClient.preloadTexture(LOGISTICSITEMS_TEXTURE_FILE);
		MinecraftForgeClient.preloadTexture(LOGISTICSACTIONTRIGGERS_TEXTURE_FILE);
	}
	
	@Override
	public void modsLoaded() {
		Localization.addLocalization("/lang/logisticspipes/", "en_US");
		
		super.modsLoaded();
	}
}
