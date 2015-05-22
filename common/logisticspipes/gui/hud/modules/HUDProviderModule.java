package logisticspipes.gui.hud.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class HUDProviderModule implements IHUDModuleRenderer {

	private List<IHUDButton> buttons = new ArrayList<IHUDButton>();
	
	private int page = 0;
	
	private final ModuleProvider module;

	public HUDProviderModule(ModuleProvider moduleProvider) {
		buttons.add(new BasicHUDButton("<" , 8, -35, 8, 8) {
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
		buttons.add(new BasicHUDButton(">" , 20, -35, 8, 8) {
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
				return page + 1 < getMaxPage();
			}
		});
		module = moduleProvider;
	}
	
	public int getMaxPage() {
		int ret = module.displayList.size() / 9;
		if(module.displayList.size() % 9 != 0 || ret == 0) {
			ret++;
		}
		return ret;
	}
	
	@Override
	public void renderContent() {
		Minecraft mc = FMLClientHandler.instance().getClient();
		GL11.glScalef(1.0F, 1.0F, -0.00001F);
		GuiGraphics.renderItemIdentifierStackListIntoGui(module.displayList, null, page, -25, -24, 3, 9, 18, 18, mc, true, true, true, true);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.00001F);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return buttons;
	}
}
