package logisticspipes.logisticspipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiHandler;
import net.minecraft.src.GuiRenameWorld;
import net.minecraft.src.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import buildcraft.mod_BuildCraftTransport;
import buildcraft.core.ProxyCore;
import buildcraft.core.utils.Localization;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import cpw.mods.fml.client.SpriteHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;


public abstract class ModTextureProxy {
	
	protected int index = 0;
	
	protected Item createPipe (int defaultID, Class <? extends Pipe> clas, String descr) {
		Item res =  BlockGenericPipe.registerPipe (defaultID, clas);
		res.setItemName(clas.getSimpleName());
		ProxyCore.proxy.addName(res, descr);
		MinecraftForgeClient.registerItemRenderer(res.shiftedIndex, mod_BuildCraftTransport.pipeItemRenderer);
		if(defaultID != LogisticsPipes.LOGISTICSPIPE_BASIC_ID) {
			registerShapelessResetRecipe(res,0,LogisticsPipes.LogisticsBasicPipe,0);
		}
		return res;
	}
	
	protected abstract void registerShapelessResetRecipe(Item res, int i, Item logisticsBasicPipe, int j);

	public int registerTexture(String fileName) {
		return RenderingRegistry.addTextureOverride(LogisticsPipes.BASE_TEXTURE_FILE, fileName/*, index*/);
		//return index++;
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
		SpriteHelper.registerSpriteMapForFile(LogisticsPipes.BASE_TEXTURE_FILE, spirt);
	}

	public void load() {
		NetworkRegistry.instance().registerGuiHandler(LogisticsPipes.instance, new GuiHandler());
		
		MinecraftForgeClient.preloadTexture(LogisticsPipes.LOGISTICSITEMS_TEXTURE_FILE);
		MinecraftForgeClient.preloadTexture(LogisticsPipes.LOGISTICSACTIONTRIGGERS_TEXTURE_FILE);
		Localization.addLocalization("/lang/logisticspipes/", "en_US");
	}
}
