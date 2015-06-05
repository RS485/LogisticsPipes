package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleExtractor;

import net.minecraft.client.Minecraft;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.client.FMLClientHandler;

public class HUDExtractor implements IHUDModuleRenderer {

	private final ModuleExtractor module;

	public HUDExtractor(ModuleExtractor moduleExtractor) {
		module = moduleExtractor;
	}

	@Override
	public void renderContent(boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();

		ForgeDirection d = module.getSneakyDirection();
		mc.fontRenderer.drawString("Extract", -22, -22, 0);
		mc.fontRenderer.drawString("from:", -22, -9, 0);
		mc.fontRenderer.drawString(((d == ForgeDirection.UNKNOWN) ? "DEFAULT" : d.name()), -22, 18, 0);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}

}
