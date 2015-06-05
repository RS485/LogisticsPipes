package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleCCBasedQuickSort;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.client.FMLClientHandler;

public class HUDCCBasedQuickSort implements IHUDModuleRenderer {

	private final ModuleCCBasedQuickSort module;

	public HUDCCBasedQuickSort(ModuleCCBasedQuickSort module) {
		this.module = module;
	}

	@Override
	public void renderContent(boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		mc.fontRenderer.drawString("Timeout: ", -29, -30, 0);
		mc.fontRenderer.drawString(module.getTimeout() + " ticks", 0 - (mc.fontRenderer.getStringWidth(module.getTimeout() + "ticks") / 2), -20, 0);
		mc.fontRenderer.drawString("Sinks", -29, 0, 0);
		mc.fontRenderer.drawString("pending: ", -19, 10, 0);
		mc.fontRenderer.drawString(Integer.toString(module.getSinkSize()), 0 - (mc.fontRenderer.getStringWidth(Integer.toString(module.getSinkSize())) / 2), 20, 0);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}
}
