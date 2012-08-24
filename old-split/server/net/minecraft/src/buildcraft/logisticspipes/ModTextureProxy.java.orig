package net.minecraft.src.buildcraft.logisticspipes;

import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;
import net.minecraft.src.core_LogisticsPipes;
import buildcraft.mod_BuildCraftTransport;
import buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.GuiHandler;
import net.minecraft.src.buildcraft.krapht.network.ConnectionHandler;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import net.minecraft.src.forge.MinecraftForge;


public abstract class ModTextureProxy extends core_LogisticsPipes {
	
	public int index = 0;
	
	protected Item createPipe (int defaultID, Class <? extends Pipe> clas, String descr) {
//		String name = Character.toLowerCase(clas.getSimpleName().charAt(0))
//				+ clas.getSimpleName().substring(1);
		
		Item res =  BlockGenericPipe.registerPipe (defaultID, clas);
		res.setItemName(clas.getSimpleName());
		CoreProxy.addName(res, descr);
		
		return res;
	}
	
	public int registerTexture(String fileName) {
		return index++;
	}

	public void initTextures() {
		
	}
	
	@Override
	public void load() {
		super.load();
		MinecraftForge.registerConnectionHandler(new ConnectionHandler());
		MinecraftForge.setGuiHandler(this,new GuiHandler());
	}
}
