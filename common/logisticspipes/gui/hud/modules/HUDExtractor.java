package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleExtractor;

import net.minecraft.client.Minecraft;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.client.FMLClientHandler;

public class HUDExtractor implements IHUDModuleRenderer {

	private final ModuleExtractor module;

	public HUDExtractor(ModuleExtractor moduleExtractor) {
		module = moduleExtractor;
	}

	@Override
	public void renderContent(boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();

		EnumFacing d = module.getSneakyDirection();
		mc.fontRendererObj.drawString("Extract", -22, -22, 0);
		mc.fontRendererObj.drawString("from:", -22, -9, 0);
		mc.fontRendererObj.drawString(((d == null) ? "DEFAULT" : d.name()), -22, 18, 0);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}

}
