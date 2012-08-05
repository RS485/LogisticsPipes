package net.minecraft.src.buildcraft.logisticspipes;

import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_BuildCraftTransport;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.utils.Localization;
import net.minecraft.src.buildcraft.krapht.GuiHandler;
import net.minecraft.src.buildcraft.krapht.network.ConnectionHandler;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.MinecraftForgeClient;


public abstract class ModTextureProxy extends core_LogisticsPipes {

	protected Item createPipe (int defaultID, Class <? extends Pipe> clas, String descr) {
		Item res =  BlockGenericPipe.registerPipe (defaultID, clas);
		res.setItemName(clas.getSimpleName());
		CoreProxy.addName(res, descr);
		MinecraftForgeClient.registerItemRenderer(res.shiftedIndex, mod_BuildCraftTransport.instance);
		if(defaultID != mod_LogisticsPipes.LOGISTICSPIPE_BASIC_ID) {
			registerShapelessResetRecipe(res,0,mod_LogisticsPipes.LogisticsBasicPipe,0);
		}
		return res;
	}

	@Override
	public void load() {
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
