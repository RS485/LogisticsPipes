package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class HUDPassiveSupplier implements IHUDModuleRenderer {
	
	private final ModulePassiveSupplier module;
	
	public HUDPassiveSupplier(ModulePassiveSupplier modulePassiveSupplier) {
		this.module = modulePassiveSupplier;
	}

	@Override
	public void renderContent() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		GL11.glScalef(1.0F, 1.0F, -0.000001F);
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(ItemIdentifierStack.getListFromInventory(module.getFilterInventory()), null, 0, -25, -32, 3, 9, 18, 18, mc, true, true, true, true);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.000001F);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}

}
