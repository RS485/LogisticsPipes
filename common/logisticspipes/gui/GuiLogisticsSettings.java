package logisticspipes.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.PlayerConfig;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.LogisticsBaseTabGuiScreen;
import logisticspipes.utils.gui.SearchBar;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiLogisticsSettings extends LogisticsBaseTabGuiScreen {

	private final String PREFIX = "gui.settings.";

	public GuiLogisticsSettings(final EntityPlayer player) {
		super(180, 220);
		DummyContainer dummy = new DummyContainer(player, null);
		dummy.addNormalSlotsForPlayerInventory(10, 135);

		addTab(new PipeRenderSettings());

		inventorySlots = dummy;
	}

	private class PipeRenderSettings extends TabSubGui {

		private SearchBar renderDistance;
		private SearchBar contentRenderDistance;
		private GuiCheckBox useNewRendererButton;
		private GuiCheckBox useFallbackRendererButton;

		private PipeRenderSettings() {}

		@Override
		public void initTab() {
			PlayerConfig config = LogisticsPipes.getClientPlayerConfig();
			if (renderDistance == null) {
				renderDistance = new SearchBar(fontRendererObj, getBaseScreen(), 15, 75, 30, 15, false, true, true);
				renderDistance.searchinput1 = config.getRenderPipeDistance() + "";
			}
			renderDistance.reposition(15, 80, 30, 15);
			if (contentRenderDistance == null) {
				contentRenderDistance = new SearchBar(fontRendererObj, getBaseScreen(), 15, 105, 30, 15, false, true, true);
				contentRenderDistance.searchinput1 = config.getRenderPipeContentDistance() + "";
			}
			contentRenderDistance.reposition(15, 110, 30, 15);
			useNewRendererButton = (GuiCheckBox) addButton(new GuiCheckBox(0, guiLeft + 15, guiTop + 30, 16, 16, config.isUseNewRenderer()));
			useFallbackRendererButton = (GuiCheckBox) addButton(new GuiCheckBox(0, guiLeft + 15, guiTop + 50, 16, 16, config.isUseFallbackRenderer()));
		}

		@Override
		public void renderIcon(int x, int y) {
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableGUIStandardItemLighting();
			ItemStack stack = new ItemStack(LogisticsPipes.LogisticsBasicPipe, 1);
			GuiScreen.itemRender.renderItemAndEffectIntoGUI(fontRendererObj, getMC().renderEngine, stack, x, y);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GuiScreen.itemRender.zLevel = 0.0F;
		}

		@Override
		public void renderBackgroundContent() {}

		@Override
		public void buttonClicked(GuiButton button) {
			if (button == useNewRendererButton) {
				useNewRendererButton.change();
			}
			if (button == useFallbackRendererButton) {
				useFallbackRendererButton.change();
			}
		}

		@Override
		public void renderForgroundContent() {
			renderDistance.renderSearchBar();
			contentRenderDistance.renderSearchBar();
			fontRendererObj.drawString(StringUtils.translate(PREFIX + "pipenewrenderer"), 38, 34, 0x404040);
			fontRendererObj.drawString(StringUtils.translate(PREFIX + "pipefallbackrenderer"), 38, 54, 0x404040);
			fontRendererObj.drawString(StringUtils.translate(PREFIX + "piperenderdistance"), 10, 70, 0x404040);
			fontRendererObj.drawString(StringUtils.translate(PREFIX + "pipecontentrenderdistance"), 10, 100, 0x404040);
		}

		@Override
		public boolean handleClick(int x, int y, int type) {
			boolean val1 = renderDistance.handleClick(x - guiLeft, y - guiTop, type);
			boolean val2 = contentRenderDistance.handleClick(x - guiLeft, y - guiTop, type);
			return val1 || val2;
		}

		@Override
		public boolean handleKey(int code, char c) {
			return renderDistance.handleKey(c, code) || contentRenderDistance.handleKey(c, code);
		}

		@Override
		public void guiClose() {
			PlayerConfig config = LogisticsPipes.getClientPlayerConfig();
			try {
				config.setRenderPipeDistance(Integer.valueOf(renderDistance.getContent()));
				config.setRenderPipeContentDistance(Integer.valueOf(contentRenderDistance.getContent()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			config.setUseNewRenderer(useNewRendererButton.getState());
			config.setUseFallbackRenderer(useFallbackRendererButton.getState());
			config.sendUpdate();
		}
	}
}
