/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import cpw.mods.fml.client.FMLClientHandler;
import logisticspipes.utils.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * Utils class for GUI-related drawing methods.
 */
public final class GuiGraphics {

	public static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("textures/gui/widgets.png");
	public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/slot.png");
	public static final ResourceLocation BIG_SLOT_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/slot-big.png");
	public static final ResourceLocation SMALL_SLOT_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/slot-small.png");
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/GuiBackground.png");
	public static final ResourceLocation LOCK_ICON = new ResourceLocation("logisticspipes", "textures/gui/lock.png");
	public static final ResourceLocation LINES_ICON = new ResourceLocation("logisticspipes", "textures/gui/lines.png");
	public static final ResourceLocation STATS_ICON = new ResourceLocation("logisticspipes", "textures/gui/stats.png");
	public static float zLevel = 0.0F;

	private GuiGraphics() {
	}

	public static void displayItemToolTip(Object[] tooltip, Gui gui, float pzLevel, int guiLeft, int guiTop) {
		displayItemToolTip(tooltip, pzLevel, guiLeft, guiTop, false, false);
	}

	@SuppressWarnings("unchecked")
	public static void displayItemToolTip(Object[] tooltip, float pzLevel, int guiLeft, int guiTop, boolean forceMinecraft, boolean forceAdd) {
		if (tooltip == null) {
			return;
		}

		zLevel = pzLevel;

		Minecraft mc = FMLClientHandler.instance().getClient();
		ItemStack var22 = (ItemStack) tooltip[2];

		List<String> var24 = var22.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

		if (tooltip.length > 4) {
			var24.addAll(1, (List<String>) tooltip[4]);
		}

		if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && (tooltip.length < 4 || (Boolean) tooltip[3])) {
			var24.add(1, "\u00a77" + ((ItemStack) tooltip[2]).stackSize);
		}

		int var11 = (Integer) tooltip[0] - (forceAdd ? 0 : guiLeft) + 12;
		int var12 = (Integer) tooltip[1] - (forceAdd ? 0 : guiTop) - 12;
		drawToolTip(var11, var12, var24, var22.getRarity().rarityColor, forceMinecraft);

		zLevel = 0;
	}

	public static void drawToolTip(int posX, int posY, List<String> msg, EnumChatFormatting rarityColor, boolean forceMinecraft) {
		if (!forceMinecraft) {
			// try NEI methods
			try {
				Class<?> LayoutManager = Class.forName("codechicken.nei.LayoutManager");
				Field GuiManagerField = LayoutManager.getDeclaredField("gui");
				GuiManagerField.setAccessible(true);
				Object GuiManagerObject = GuiManagerField.get(null);
				Class<?> GuiManager = Class.forName("codechicken.nei.GuiManager");
				Method drawMultilineTip = GuiManager.getDeclaredMethod("drawMultilineTip", int.class, int.class, List.class, int.class);

				drawMultilineTip.invoke(GuiManagerObject, posX, posY, msg, rarityColor);
			} catch (ReflectiveOperationException e) {
				forceMinecraft = true;
			}
		}

		if (forceMinecraft) {
			if (msg.isEmpty()) {
				return;
			}

			// use vanilla Minecraft code
			int var10 = 0;
			int var11;
			int var12;

			for (var11 = 0; var11 < msg.size(); ++var11) {
				var12 = FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(msg.get(var11));

				if (var12 > var10) {
					var10 = var12;
				}
			}

			var11 = posX + 12;
			var12 = posY - 12;
			int var14 = 8;

			if (msg.size() > 1) {
				var14 += 2 + (msg.size() - 1) * 10;
			}

			GL11.glDisable(2896 /*GL_LIGHTING*/);
			GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
			zLevel = 300.0F;
			int var15 = -267386864;

			SimpleGraphics.drawGradientRect(var11 - 3, var12 - 4, var11 + var10 + 3, var12 - 3, var15, var15, 0.0);
			SimpleGraphics.drawGradientRect(var11 - 3, var12 + var14 + 3, var11 + var10 + 3, var12 + var14 + 4, var15, var15, 0.0);
			SimpleGraphics.drawGradientRect(var11 - 3, var12 - 3, var11 + var10 + 3, var12 + var14 + 3, var15, var15, 0.0);
			SimpleGraphics.drawGradientRect(var11 - 4, var12 - 3, var11 - 3, var12 + var14 + 3, var15, var15, 0.0);
			SimpleGraphics.drawGradientRect(var11 + var10 + 3, var12 - 3, var11 + var10 + 4, var12 + var14 + 3, var15, var15, 0.0);
			int var16 = 1347420415;
			int var17 = (var16 & 16711422) >> 1 | var16 & -16777216;
			SimpleGraphics.drawGradientRect(var11 - 3, var12 - 3 + 1, var11 - 3 + 1, var12 + var14 + 3 - 1, var16, var17, 0.0);
			SimpleGraphics.drawGradientRect(var11 + var10 + 2, var12 - 3 + 1, var11 + var10 + 3, var12 + var14 + 3 - 1, var16, var17, 0.0);
			SimpleGraphics.drawGradientRect(var11 - 3, var12 - 3, var11 + var10 + 3, var12 - 3 + 1, var16, var16, 0.0);
			SimpleGraphics.drawGradientRect(var11 - 3, var12 + var14 + 2, var11 + var10 + 3, var12 + var14 + 3, var17, var17, 0.0);

			for (int var18 = 0; var18 < msg.size(); ++var18) {
				String var19 = msg.get(var18);

				if (var18 == 0) {
					var19 = "\u00a7" + rarityColor.getFormattingCode() + var19;
				} else {
					var19 = "\u00a77" + var19;
				}

				FMLClientHandler.instance().getClient().fontRenderer.drawStringWithShadow(var19, var11, var12, -1);

				if (var18 == 0) {
					var12 += 2;
				}

				var12 += 10;
			}

			zLevel = 0.0F;

			GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
			GL11.glEnable(2896 /*GL_LIGHTING*/);
		}
	}

	public static void drawPlayerInventoryBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "backpack"
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				drawSlotBackground(mc, xOffset + column * 18 - 1, yOffset + row * 18 - 1);
			}
		}
		//Player "hotbar"
		for (int i1 = 0; i1 < 9; i1++) {
			drawSlotBackground(mc, xOffset + i1 * 18 - 1, yOffset + 58 - 1);
		}
	}

	public static void drawPlayerHotbarBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "hotbar"
		for (int i1 = 0; i1 < 9; i1++) {
			drawSlotBackground(mc, xOffset + i1 * 18 - 1, yOffset - 1);
		}
	}

	public static void drawPlayerArmorBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "armor"
		for (int i1 = 0; i1 < 4; i1++) {
			drawSlotBackground(mc, xOffset - 1, yOffset - 1 - i1 * 18);
		}
	}

	public static void drawSlotBackground(Minecraft mc, int x, int y) {
		zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(SLOT_TEXTURE);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x, y + 18, zLevel, 0, 1);
		var9.addVertexWithUV(x + 18, y + 18, zLevel, 1, 1);
		var9.addVertexWithUV(x + 18, y, zLevel, 1, 0);
		var9.addVertexWithUV(x, y, zLevel, 0, 0);
		var9.draw();
	}

	public static void drawSlotBackground(Minecraft mc, int x, int y, int color) {
		zLevel = 0;
		GL11.glColor4f(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color));
		mc.renderEngine.bindTexture(SLOT_TEXTURE);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x, y + 18, zLevel, 0, 1);
		var9.addVertexWithUV(x + 18, y + 18, zLevel, 1, 1);
		var9.addVertexWithUV(x + 18, y, zLevel, 1, 0);
		var9.addVertexWithUV(x, y, zLevel, 0, 0);
		var9.draw();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void drawBigSlotBackground(Minecraft mc, int x, int y) {
		zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(BIG_SLOT_TEXTURE);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x, y + 26, zLevel, 0, 1);
		var9.addVertexWithUV(x + 26, y + 26, zLevel, 1, 1);
		var9.addVertexWithUV(x + 26, y, zLevel, 1, 0);
		var9.addVertexWithUV(x, y, zLevel, 0, 0);
		var9.draw();
	}

	public static void drawSmallSlotBackground(Minecraft mc, int x, int y) {
		zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(SMALL_SLOT_TEXTURE);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x, y + 8, zLevel, 0, 1);
		var9.addVertexWithUV(x + 8, y + 8, zLevel, 1, 1);
		var9.addVertexWithUV(x + 8, y, zLevel, 1, 0);
		var9.addVertexWithUV(x, y, zLevel, 0, 0);
		var9.draw();
	}

	public static void renderIconAt(Minecraft mc, int x, int y, float zLevel, IIcon icon) {
		if (icon == null) return;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x, y + 16, zLevel, icon.getMinU(), icon.getMaxV());
		var9.addVertexWithUV(x + 16, y + 16, zLevel, icon.getMaxU(), icon.getMaxV());
		var9.addVertexWithUV(x + 16, y, zLevel, icon.getMaxU(), icon.getMinV());
		var9.addVertexWithUV(x, y, zLevel, icon.getMinU(), icon.getMinV());
		var9.draw();
	}

	public static void drawLockBackground(Minecraft mc, int x, int y) {
		zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(LOCK_ICON);
		GL11.glEnable(GL11.GL_BLEND);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x, y + 15, zLevel, 0, 1);
		var9.addVertexWithUV(x + 14, y + 15, zLevel, 1, 1);
		var9.addVertexWithUV(x + 14, y, zLevel, 1, 0);
		var9.addVertexWithUV(x, y, zLevel, 0, 0);
		var9.draw();
	}

	public static void drawLinesBackground(Minecraft mc, int x, int y) {
		zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(LINES_ICON);
		GL11.glEnable(GL11.GL_BLEND);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x, y + 16, zLevel, 0, 1);
		var9.addVertexWithUV(x + 16, y + 16, zLevel, 1, 1);
		var9.addVertexWithUV(x + 16, y, zLevel, 1, 0);
		var9.addVertexWithUV(x, y, zLevel, 0, 0);
		var9.draw();
	}

	public static void drawStatsBackground(Minecraft mc, int x, int y) {
		zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(STATS_ICON);
		GL11.glEnable(GL11.GL_BLEND);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x, y + 16, zLevel, 0, 1);
		var9.addVertexWithUV(x + 16, y + 16, zLevel, 1, 1);
		var9.addVertexWithUV(x + 16, y, zLevel, 1, 0);
		var9.addVertexWithUV(x, y, zLevel, 0, 0);
		var9.draw();
	}

	public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel, boolean resetColor) {
		drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, resetColor, true, true, true, true);
	}

	public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel, boolean resetColor, boolean displayTop, boolean displayLeft, boolean displayBottom, boolean displayRight) {
		if (resetColor) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		mc.renderEngine.bindTexture(BACKGROUND_TEXTURE);

		if (displayTop) {
			//Top Side
			Tessellator var9 = Tessellator.instance;
			var9.startDrawingQuads();
			var9.addVertexWithUV(guiLeft + 15, guiTop + 15, zLevel, 0.33, 0.33);
			var9.addVertexWithUV(right - 15, guiTop + 15, zLevel, 0.66, 0.33);
			var9.addVertexWithUV(right - 15, guiTop, zLevel, 0.66, 0);
			var9.addVertexWithUV(guiLeft + 15, guiTop, zLevel, 0.33, 0);
			var9.draw();
		}

		if (displayLeft) {
			//Left Side
			Tessellator var9 = Tessellator.instance;
			var9.startDrawingQuads();
			var9.addVertexWithUV(guiLeft, bottom - 15, zLevel, 0, 0.66);
			var9.addVertexWithUV(guiLeft + 15, bottom - 15, zLevel, 0.33, 0.66);
			var9.addVertexWithUV(guiLeft + 15, guiTop + 15, zLevel, 0.33, 0.33);
			var9.addVertexWithUV(guiLeft, guiTop + 15, zLevel, 0, 0.33);
			var9.draw();
		}

		if (displayBottom) {
			//Bottom Side
			Tessellator var9 = Tessellator.instance;
			var9.startDrawingQuads();
			var9.addVertexWithUV(guiLeft + 15, bottom, zLevel, 0.33, 1);
			var9.addVertexWithUV(right - 15, bottom, zLevel, 0.66, 1);
			var9.addVertexWithUV(right - 15, bottom - 15, zLevel, 0.66, 0.66);
			var9.addVertexWithUV(guiLeft + 15, bottom - 15, zLevel, 0.33, 0.66);
			var9.draw();
		}

		if (displayRight) {
			//Right Side
			Tessellator var9 = Tessellator.instance;
			var9.startDrawingQuads();
			var9.addVertexWithUV(right - 15, bottom - 15, zLevel, 0.66, 0.66);
			var9.addVertexWithUV(right, bottom - 15, zLevel, 1, 0.66);
			var9.addVertexWithUV(right, guiTop + 15, zLevel, 1, 0.33);
			var9.addVertexWithUV(right - 15, guiTop + 15, zLevel, 0.66, 0.33);
			var9.draw();
		}

		if (displayTop && displayLeft) {
			//Top Left
			Tessellator var9 = Tessellator.instance;
			var9.startDrawingQuads();
			var9.addVertexWithUV(guiLeft, guiTop + 15, zLevel, 0, 0.33);
			var9.addVertexWithUV(guiLeft + 15, guiTop + 15, zLevel, 0.33, 0.33);
			var9.addVertexWithUV(guiLeft + 15, guiTop, zLevel, 0.33, 0);
			var9.addVertexWithUV(guiLeft, guiTop, zLevel, 0, 0);
			var9.draw();
		}

		if (displayBottom && displayLeft) {
			//Bottom Left
			Tessellator var9 = Tessellator.instance;
			var9.startDrawingQuads();
			var9.addVertexWithUV(guiLeft, bottom, zLevel, 0, 1);
			var9.addVertexWithUV(guiLeft + 15, bottom, zLevel, 0.33, 1);
			var9.addVertexWithUV(guiLeft + 15, bottom - 15, zLevel, 0.33, 0.66);
			var9.addVertexWithUV(guiLeft, bottom - 15, zLevel, 0, 0.66);
			var9.draw();
		}

		if (displayBottom && displayRight) {
			//Bottom Right
			Tessellator var9 = Tessellator.instance;
			var9.startDrawingQuads();
			var9.addVertexWithUV(right - 15, bottom, zLevel, 0.66, 1);
			var9.addVertexWithUV(right, bottom, zLevel, 1, 1);
			var9.addVertexWithUV(right, bottom - 15, zLevel, 1, 0.66);
			var9.addVertexWithUV(right - 15, bottom - 15, zLevel, 0.66, 0.66);
			var9.draw();
		}

		if (displayTop && displayRight) {
			//Top Right
			Tessellator var9 = Tessellator.instance;
			var9.startDrawingQuads();
			var9.addVertexWithUV(right - 15, guiTop + 15, zLevel, 0.66, 0.33);
			var9.addVertexWithUV(right, guiTop + 15, zLevel, 1, 0.33);
			var9.addVertexWithUV(right, guiTop, zLevel, 1, 0);
			var9.addVertexWithUV(right - 15, guiTop, zLevel, 0.66, 0);
			var9.draw();
		}

		//Center
		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(guiLeft + 15, bottom - 15, zLevel, 0.33, 0.66);
		var9.addVertexWithUV(right - 15, bottom - 15, zLevel, 0.66, 0.66);
		var9.addVertexWithUV(right - 15, guiTop + 15, zLevel, 0.66, 0.33);
		var9.addVertexWithUV(guiLeft + 15, guiTop + 15, zLevel, 0.33, 0.33);
		var9.draw();
	}
}
