package logisticspipes.gui.hud.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.client.FMLClientHandler;

import org.lwjgl.opengl.GL11;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;

public class HUDProviderModule implements IHUDModuleRenderer {

	private List<IHUDButton> buttons = new ArrayList<>();

	private int page = 0;

	private final ModuleProvider module;

	public HUDProviderModule(ModuleProvider moduleProvider) {
		buttons.add(new BasicHUDButton("<", 8, -35, 8, 8) {

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
		buttons.add(new BasicHUDButton(">", 20, -35, 8, 8) {

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
		if (module.displayList.size() % 9 != 0 || ret == 0) {
			ret++;
		}
		return ret;
	}

	@Override
	public void renderContent(boolean shifted) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		GL11.glScalef(1.0F, 1.0F, -0.00001F);
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(module.displayList, null, page, -25, -24, 3, 9, 18, 18, 100.0F, DisplayAmount.ALWAYS, false, shifted);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.00001F);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return buttons;
	}
}
