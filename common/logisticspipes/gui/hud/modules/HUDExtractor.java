package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleExtractor;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.client.FMLClientHandler;

public class HUDExtractor implements IHUDModuleRenderer {
	
	private final ModuleExtractor module;
	
	public HUDExtractor(ModuleExtractor moduleExtractor) {
		this.module = moduleExtractor;
	}

	@Override
	public void renderContent() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		mc.fontRenderer.drawString("Default" , -12, -22, 0);
		mc.fontRenderer.drawString("Top" , -12, -9, 0);
		mc.fontRenderer.drawString("Side" , -12, 5, 0);
		mc.fontRenderer.drawString("Bottom" , -12, 18, 0);
		
		switch(module.getSneakyOrientation()) {
		case Default:
			mc.fontRenderer.drawString("X" , -22, -22, 0);
			break;
		case Top:
			mc.fontRenderer.drawString("X" , -22, -9, 0);
			break;
		case Side:
			mc.fontRenderer.drawString("X" , -22, 5, 0);
			break;
		case Bottom:
			mc.fontRenderer.drawString("X" , -22, 18, 0);
			break;
		default:
		}
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}

}
