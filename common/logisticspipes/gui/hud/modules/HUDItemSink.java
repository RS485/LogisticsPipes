package logisticspipes.gui.hud.modules;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.BasicGuiHelper;

public class HUDItemSink implements IHUDModuleRenderer {
	
	private final ModuleItemSink module;
	
	public HUDItemSink(ModuleItemSink module) {
		this.module = module;
	}
	
	@Override
	public void renderContent() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		GL11.glScalef(1.0F, 1.0F, -0.000001F);
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(ItemIdentifierStack.getListFromInventory(module.getFilterInventory()), null, 0, -25, -32, 3, 9, 18, 18, mc, false, false);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.000001F);
		mc.fontRenderer.drawString("Default:" , -29, 25, 0);
		if(module.isDefaultRoute()) {
			mc.fontRenderer.drawString("Yes" , 11, 25, 0);
		} else {
			mc.fontRenderer.drawString("No" , 15, 25, 0);
		}
	}
}
