package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.client.FMLClientHandler;

import org.lwjgl.opengl.GL11;

public class HUDElectricManager implements IHUDModuleRenderer {

	private final ModuleElectricManager module;

	public HUDElectricManager(ModuleElectricManager moduleElectricManager) {
		module = moduleElectricManager;
	}

	@Override
	public void renderContent(boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		GL11.glScalef(1.0F, 1.0F, -0.000001F);
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(ItemIdentifierStack.getListFromInventory(module.getFilterInventory()), null, 0, -25, -32, 3, 9, 18, 18, 100.0F, DisplayAmount.NEVER, true, false, shifted);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.000001F);
		mc.fontRenderer.drawString("Charge:", -29, 25, 0);
		if (module.isDischargeMode()) {
			mc.fontRenderer.drawString("No", 15, 25, 0);
		} else {
			mc.fontRenderer.drawString("Yes", 11, 25, 0);
		}
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}
}
