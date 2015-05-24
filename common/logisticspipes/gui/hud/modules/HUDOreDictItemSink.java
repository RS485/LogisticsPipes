package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.GuiGraphics.DisplayAmount;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class HUDOreDictItemSink implements IHUDModuleRenderer {

	private final ModuleOreDictItemSink itemSink;

	public HUDOreDictItemSink(ModuleOreDictItemSink module) {
		itemSink = module;
	}

	@Override
	public void renderContent(boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		GL11.glScalef(1.0F, 1.0F, -0.00001F);
		GuiGraphics.renderItemIdentifierStackListIntoGui(itemSink.getHudItemList(), null, 0, -25, -32, 3, 9, 18, 18, 100.0F, mc, DisplayAmount.NEVER, true, true, !shifted);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.00001F);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}
}
