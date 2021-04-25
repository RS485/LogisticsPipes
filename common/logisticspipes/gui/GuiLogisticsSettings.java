package logisticspipes.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.PlayerConfigToServerPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.LogisticsBaseTabGuiScreen;
import network.rs485.logisticspipes.config.ClientConfiguration;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiLogisticsSettings extends LogisticsBaseTabGuiScreen {

	private static final String PREFIX = "gui.settings.";

	public GuiLogisticsSettings(final EntityPlayer player) {
		super(180, 220);
		DummyContainer dummy = new DummyContainer(player, null);
		dummy.addNormalSlotsForPlayerInventory(10, 135);

		addTab(new PipeRenderSettings());

		inventorySlots = dummy;
	}

	private class PipeRenderSettings extends TabSubGui {

		private InputBar renderDistance;
		private InputBar contentRenderDistance;
		private GuiCheckBox useNewRendererButton;
		private GuiCheckBox useFallbackRendererButton;

		private PipeRenderSettings() {}

		@Override
		public void initTab() {
			Keyboard.enableRepeatEvents(true);

			ClientConfiguration config = LogisticsPipes.getClientPlayerConfig();
			if (renderDistance == null) {
				renderDistance = new InputBar(fontRenderer, getBaseScreen(), 15, 75, 30, 15, false, true, InputBar.Align.RIGHT);
				renderDistance.setInteger(config.getRenderPipeDistance());
			}
			renderDistance.reposition(15, 80, 30, 15);
			if (contentRenderDistance == null) {
				contentRenderDistance = new InputBar(fontRenderer, getBaseScreen(), 15, 105, 30, 15, false, true, InputBar.Align.RIGHT);
				contentRenderDistance.setInteger(config.getRenderPipeContentDistance());
			}
			contentRenderDistance.reposition(15, 110, 30, 15);
			//useNewRendererButton = (GuiCheckBox) addButton(new GuiCheckBox(0, guiLeft + 15, guiTop + 30, 16, 16, config.isUseNewRenderer()));
			//useFallbackRendererButton = (GuiCheckBox) addButton(new GuiCheckBox(0, guiLeft + 15, guiTop + 50, 16, 16, config.isUseFallbackRenderer()));
		}

		@Override
		public void renderIcon(int x, int y) {
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableGUIStandardItemLighting();
			ItemStack stack = new ItemStack(LPItems.pipeBasic, 1);
			itemRender.renderItemAndEffectIntoGUI(stack, x, y);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			itemRender.zLevel = 0.0F;
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
			renderDistance.drawTextBox();
			contentRenderDistance.drawTextBox();
			//fontRenderer.drawString(StringUtil.translate(PREFIX + "pipenewrenderer"), 38, 34, 0x404040);
			//fontRenderer.drawString(StringUtil.translate(PREFIX + "pipefallbackrenderer"), 38, 54, 0x404040);
			fontRenderer.drawString(TextUtil.translate(PREFIX + "piperenderdistance"), 10, 70, 0x404040);
			fontRenderer.drawString(TextUtil.translate(PREFIX + "pipecontentrenderdistance"), 10, 100, 0x404040);
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
			ClientConfiguration config = LogisticsPipes.getClientPlayerConfig();
			try {
				config.setRenderPipeDistance(renderDistance.getInteger());
				config.setRenderPipeContentDistance(contentRenderDistance.getInteger());
			} catch (Exception e) {
				e.printStackTrace();
			}
			//config.setUseNewRenderer(useNewRendererButton.getState());
			//config.setUseFallbackRenderer(useFallbackRendererButton.getState());

			MainProxy.sendPacketToServer(
					PacketHandler.getPacket(PlayerConfigToServerPacket.class).setConfig(config));

		}
	}
}
