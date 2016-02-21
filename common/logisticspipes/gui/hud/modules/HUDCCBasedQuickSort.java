package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleCCBasedQuickSort;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.client.FMLClientHandler;

public class HUDCCBasedQuickSort implements IHUDModuleRenderer {

	private final ModuleCCBasedQuickSort module;

	public HUDCCBasedQuickSort(ModuleCCBasedQuickSort module) {
		this.module = module;
	}

	@Override
	public void renderContent(boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		mc.fontRendererObj.drawString("Timeout: ", -29, -30, 0);
		mc.fontRendererObj.drawString(module.getTimeout() + " ticks", 0 - (mc.fontRendererObj.getStringWidth(module.getTimeout() + "ticks") / 2), -20, 0);
		mc.fontRendererObj.drawString("Sinks", -29, 0, 0);
		mc.fontRendererObj.drawString("pending: ", -19, 10, 0);
		mc.fontRendererObj.drawString(Integer.toString(module.getSinkSize()), 0 - (mc.fontRendererObj.getStringWidth(Integer.toString(module.getSinkSize())) / 2), 20, 0);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}
}
