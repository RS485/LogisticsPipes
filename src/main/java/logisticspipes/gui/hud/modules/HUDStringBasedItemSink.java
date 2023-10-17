package logisticspipes.gui.hud.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.client.FMLClientHandler;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IStringBasedModule;
import logisticspipes.utils.gui.hud.BasicHUDButton;

public class HUDStringBasedItemSink implements IHUDModuleRenderer {

	private final IStringBasedModule itemSink;
	private final List<IHUDButton> list;
	private int page = 0;

	public HUDStringBasedItemSink(IStringBasedModule module) {
		itemSink = module;
		list = new ArrayList<>();
		list.add(new BasicHUDButton("<", 10, -35, 8, 8) {

			@Override
			public boolean shouldRenderButton() {
				return true;
			}

			@Override
			public void clicked() {
				page--;
			}

			@Override
			public boolean buttonEnabled() {
				return page > 0;
			}
		});
		list.add(new BasicHUDButton(">", 20, -35, 8, 8) {

			@Override
			public boolean shouldRenderButton() {
				return true;
			}

			@Override
			public void clicked() {
				page++;
			}

			@Override
			public boolean buttonEnabled() {
				return (page + 1) * 6 < itemSink.stringListProperty().size();
			}
		});
	}

	@Override
	public void renderContent(boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		for (int i = page * 6; i < itemSink.stringListProperty().size() && i < 6 + (page * 6); i++) {
			String mod = itemSink.stringListProperty().get(i);
			mc.fontRenderer.drawString(mod.substring(0, Math.min(12, mod.length())), -28, -25 + ((i - (page * 6)) * 10),
					0x404040);
			//mc.fontRenderer.drawSplitString(mod, -28, -25 + ((i - (page * 6)) * 10), 50, 0x404040);
		}
	}

	@Override
	public List<IHUDButton> getButtons() {

		return list;
	}
}
