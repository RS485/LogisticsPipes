package logisticspipes.gui.hud.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.client.FMLClientHandler;

public class HUDModBasedItemSink implements IHUDModuleRenderer {
	
	private final ModuleModBasedItemSink itemSink;
	private int page = 0;
	private final List<IHUDButton> list;
	
	public HUDModBasedItemSink(ModuleModBasedItemSink module) {
		itemSink = module;
		list = new ArrayList<IHUDButton>();
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
				return (page + 1) * 6 < itemSink.modList.size();
			}
		});
	}
	
	@Override
	public void renderContent() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		for(int i = page * 6; i < itemSink.modList.size() && i < 6 + (page * 6); i++) {
			String mod = itemSink.modList.get(i);
			mc.fontRenderer.drawString(mod.substring(0, Math.min(12, mod.length())), -28, -25 + ((i - (page * 6)) * 10), 0x404040);
			//mc.fontRenderer.drawSplitString(mod, -28, -25 + ((i - (page * 6)) * 10), 50, 0x404040);
		}
	}

	@Override
	public List<IHUDButton> getButtons() {
		
		return list;
	}
}
