package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleTerminus;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class HUDTerminatorModule implements IHUDModuleRenderer {

	private final ModuleTerminus module;
	
	public HUDTerminatorModule(ModuleTerminus module) {
		this.module = module;
	}
	
	@Override
	public void renderContent() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		GL11.glScalef(1.0F, 1.0F, -0.00001F);
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(ItemIdentifierStack.getListFromInventory(module.getFilterInventory()), null, 0, -25, -32, 3, 9, 18, 18, mc, false, false, true, true);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.00001F);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}
}
