/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils.gui;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.Color;

/**
 * Utils class for GUI-related drawing methods.
 */
@SideOnly(Side.CLIENT)
public final class GuiGraphics {

	public static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("textures/gui/widgets.png");
	public static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/slot.png");
	public static final ResourceLocation BIG_SLOT_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/slot-big.png");
	public static final ResourceLocation SMALL_SLOT_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/slot-small.png");
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/GuiBackground.png");
	public static final ResourceLocation LOCK_ICON = new ResourceLocation("logisticspipes", "textures/gui/lock.png");
	public static final ResourceLocation LINES_ICON = new ResourceLocation("logisticspipes", "textures/gui/lines.png");
	public static final ResourceLocation STATS_ICON = new ResourceLocation("logisticspipes", "textures/gui/stats.png");
	public static final ResourceLocation SLOT_DISK_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/slot_disk.png");
	public static final ResourceLocation SLOT_PROGRAMMER_TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/slot_programmer.png");
	public static float zLevel = 0.0F;

	private GuiGraphics() {}

	/**
	 * Draws the durability bar for GUI items.
	 *
	 * @param itemstack the itemstack, from which the durability bar should be drawn
	 * @param x         the x-coordinate for the bar
	 * @param y         the y-coordinate for the bar
	 * @param zLevel    the z-level for the bar
	 * TextureManager, ItemStack, int, int, String)
	 */
	public static void drawDurabilityBar(@Nonnull ItemStack itemstack, int x, int y, double zLevel) {
		if (itemstack.getItem().showDurabilityBar(itemstack)) {
			double health = itemstack.getItem().getDurabilityForDisplay(itemstack);
			int j1 = (int) Math.round(13.0D - health * 13.0D);
			int k = (int) Math.round(255.0D - health * 255.0D);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			Tessellator tessellator = Tessellator.getInstance();
			int l = 255 - k << 16 | k << 8;
			int i1 = (255 - k) / 4 << 16 | 16128;
			SimpleGraphics.drawQuad(tessellator, x + 2, y + 13, 13, 2, Color.BLACK, zLevel);
			SimpleGraphics.drawQuad(tessellator, x + 2, y + 13, 12, 1, i1, zLevel + 1.0F);
			SimpleGraphics.drawQuad(tessellator, x + 2, y + 13, j1, 1, l, zLevel + 2.0F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}

	public static void displayItemToolTip(Object[] tooltip, Gui gui, float pzLevel, int guiLeft, int guiTop) {
		GuiGraphics.displayItemToolTip(tooltip, pzLevel, guiLeft, guiTop, false);
	}

	@SuppressWarnings("unchecked")
	public static void displayItemToolTip(Object[] tooltip, float pzLevel, int guiLeft, int guiTop, boolean forceAdd) {
		if (tooltip == null) {
			return;
		}

		GuiGraphics.zLevel = pzLevel;

		Minecraft mc = FMLClientHandler.instance().getClient();
		ItemStack stack = (ItemStack) tooltip[2];
		if (stack == null) stack = ItemStack.EMPTY;

		List<String> tooltipLines;
		if (mc.currentScreen instanceof GuiContainer) {
			tooltipLines = SimpleServiceLocator.neiProxy.getItemToolTip(stack, mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL, (GuiContainer) mc.currentScreen);
		} else {
			tooltipLines = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
		}

		if (tooltip.length > 4) {
			tooltipLines.addAll(1, (List<String>) tooltip[4]);
		}

		if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && (tooltip.length < 4 || (Boolean) tooltip[3])) {
			tooltipLines.add(1, "\u00a77" + ((ItemStack) tooltip[2]).getCount());
		}

		int x = (Integer) tooltip[0] - (forceAdd ? 0 : guiLeft) + 12;
		int y = (Integer) tooltip[1] - (forceAdd ? 0 : guiTop) - 12;
		if (!SimpleServiceLocator.neiProxy.renderItemToolTip(x, y, tooltipLines, stack.getRarity().rarityColor, stack)) {
			GuiGraphics.drawToolTip(x, y, tooltipLines, stack.getRarity().rarityColor);
		}

		GuiGraphics.zLevel = 0;
	}

	public static void drawToolTip(int posX, int posY, List<String> msg, TextFormatting rarityColor) {
		if (msg.isEmpty()) {
			return;
		}

		// use vanilla Minecraft code
		int boxWidth = 0;

		for (String str : msg) {
			int width = FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(str);

			if (width > boxWidth) {
				boxWidth = width;
			}
		}

		int x = posX + 12;
		int y = posY - 12;
		int yHeight = 8;

		if (msg.size() > 1) {
			yHeight += 2 + (msg.size() - 1) * 10;
		}

		GlStateManager.disableDepth();
		GlStateManager.disableLighting();

		GuiGraphics.zLevel = 300.0F;
		int bgColor = 0xf0100010;
		int frameColor1 = 0x505000ff;
		int frameColor2 = (frameColor1 & 0xfefefe) >> 1 | frameColor1 & 0xff000000;

		SimpleGraphics.drawGradientRect(x - 3, y - 4, x + boxWidth + 3, y - 3, bgColor, bgColor, 0.0);
		SimpleGraphics.drawGradientRect(x - 3, y + yHeight + 3, x + boxWidth + 3, y + yHeight + 4, bgColor, bgColor, 0.0);
		SimpleGraphics.drawGradientRect(x - 3, y - 3, x + boxWidth + 3, y + yHeight + 3, bgColor, bgColor, 0.0);
		SimpleGraphics.drawGradientRect(x - 4, y - 3, x - 3, y + yHeight + 3, bgColor, bgColor, 0.0);
		SimpleGraphics.drawGradientRect(x + boxWidth + 3, y - 3, x + boxWidth + 4, y + yHeight + 3, bgColor, bgColor, 0.0);
		SimpleGraphics.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + yHeight + 3 - 1, frameColor1, frameColor2, 0.0);
		SimpleGraphics.drawGradientRect(x + boxWidth + 2, y - 3 + 1, x + boxWidth + 3, y + yHeight + 3 - 1, frameColor1, frameColor2, 0.0);
		SimpleGraphics.drawGradientRect(x - 3, y - 3, x + boxWidth + 3, y - 3 + 1, frameColor1, frameColor1, 0.0);
		SimpleGraphics.drawGradientRect(x - 3, y + yHeight + 2, x + boxWidth + 3, y + yHeight + 3, frameColor2, frameColor2, 0.0);

		for (int i = 0; i < msg.size(); ++i) {
			String line = msg.get(i);

			if (i == 0) {
				line = rarityColor.toString() + line;
			} else {
				line = "\u00a77" + line;
			}

			FMLClientHandler.instance().getClient().fontRenderer.drawStringWithShadow(line, x, y, -1);

			if (i == 0) {
				y += 2;
			}

			y += 10;
		}

		GuiGraphics.zLevel = 0.0F;

		GlStateManager.enableDepth();
	}

	public static void drawPlayerInventoryBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "backpack"
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				GuiGraphics.drawSlotBackground(mc, xOffset + column * 18 - 1, yOffset + row * 18 - 1);
			}
		}
		//Player "hotbar"
		for (int i1 = 0; i1 < 9; i1++) {
			GuiGraphics.drawSlotBackground(mc, xOffset + i1 * 18 - 1, yOffset + 58 - 1);
		}
	}

	public static void drawPlayerHotbarBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "hotbar"
		for (int i1 = 0; i1 < 9; i1++) {
			GuiGraphics.drawSlotBackground(mc, xOffset + i1 * 18 - 1, yOffset - 1);
		}
	}

	public static void drawPlayerArmorBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "armor"
		for (int i1 = 0; i1 < 4; i1++) {
			GuiGraphics.drawSlotBackground(mc, xOffset - 1, yOffset - 1 - i1 * 18);
		}
	}

	private static void doDrawSlotBackground(Minecraft mc, int x, int y, ResourceLocation slotDiskTexture) {
		GuiGraphics.zLevel = 0;
		mc.renderEngine.bindTexture(slotDiskTexture);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buf.pos(x, y + 18, GuiGraphics.zLevel).tex(0, 1).endVertex();
		buf.pos(x + 18, y + 18, GuiGraphics.zLevel).tex(1, 1).endVertex();
		buf.pos(x + 18, y, GuiGraphics.zLevel).tex(1, 0).endVertex();
		buf.pos(x, y, GuiGraphics.zLevel).tex(0, 0).endVertex();
		tess.draw();
	}

	public static void drawSlotDiskBackground(Minecraft mc, int x, int y) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		doDrawSlotBackground(mc, x, y, GuiGraphics.SLOT_DISK_TEXTURE);
	}

	public static void drawSlotProgrammerBackground(Minecraft mc, int x, int y) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		doDrawSlotBackground(mc, x, y, GuiGraphics.SLOT_PROGRAMMER_TEXTURE);
	}

	public static void drawSlotBackground(Minecraft mc, int x, int y) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		doDrawSlotBackground(mc, x, y, GuiGraphics.SLOT_TEXTURE);
	}

	public static void drawSlotBackground(Minecraft mc, int x, int y, int color) {
		GlStateManager.color(Color.getRed(color), Color.getGreen(color), Color.getBlue(color), Color.getAlpha(color));
		doDrawSlotBackground(mc, x, y, GuiGraphics.SLOT_TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public static void drawBigSlotBackground(Minecraft mc, int x, int y) {
		GuiGraphics.zLevel = 0;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.BIG_SLOT_TEXTURE);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, y + 26, GuiGraphics.zLevel).tex(0, 1).endVertex();
		buf.pos(x + 26, y + 26, GuiGraphics.zLevel).tex(1, 1).endVertex();
		buf.pos(x + 26, y, GuiGraphics.zLevel).tex(1, 0).endVertex();
		buf.pos(x, y, GuiGraphics.zLevel).tex(0, 0).endVertex();
		tess.draw();
	}

	public static void drawSmallSlotBackground(Minecraft mc, int x, int y) {
		GuiGraphics.zLevel = 0;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.SMALL_SLOT_TEXTURE);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, y + 8, GuiGraphics.zLevel).tex(0, 1).endVertex();
		buf.pos(x + 8, y + 8, GuiGraphics.zLevel).tex(1, 1).endVertex();
		buf.pos(x + 8, y, GuiGraphics.zLevel).tex(1, 0).endVertex();
		buf.pos(x, y, GuiGraphics.zLevel).tex(0, 0).endVertex();
		tess.draw();
	}

	public static void renderIconAt(Minecraft mc, int x, int y, float zLevel, TextureAtlasSprite icon) {
		if (icon == null) {
			return;
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(new ResourceLocation(icon.getIconName()));

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, y + 16, GuiGraphics.zLevel).tex(icon.getMinU(), icon.getMaxV()).endVertex();
		buf.pos(x + 16, y + 16, GuiGraphics.zLevel).tex(icon.getMaxU(), icon.getMaxV()).endVertex();
		buf.pos(x + 16, y, GuiGraphics.zLevel).tex(icon.getMaxU(), icon.getMinV()).endVertex();
		buf.pos(x, y, GuiGraphics.zLevel).tex(icon.getMinU(), icon.getMinV()).endVertex();
		tess.draw();
	}

	public static void drawLockBackground(Minecraft mc, int x, int y) {
		GuiGraphics.zLevel = 0;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.LOCK_ICON);
		GlStateManager.enableBlend();

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, y + 15, GuiGraphics.zLevel).tex(0, 1).endVertex();
		buf.pos(x + 14, y + 15, GuiGraphics.zLevel).tex(1, 1).endVertex();
		buf.pos(x + 14, y, GuiGraphics.zLevel).tex(1, 0).endVertex();
		buf.pos(x, y, GuiGraphics.zLevel).tex(0, 0).endVertex();
		tess.draw();

		GlStateManager.disableBlend();
	}

	private static void drawTexture16by16(Minecraft mc, int x, int y, ResourceLocation tex) {
		GuiGraphics.zLevel = 0;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(tex);
		GlStateManager.enableBlend();

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, y + 16, GuiGraphics.zLevel).tex(0, 1).endVertex();
		buf.pos(x + 16, y + 16, GuiGraphics.zLevel).tex(1, 1).endVertex();
		buf.pos(x + 16, y, GuiGraphics.zLevel).tex(1, 0).endVertex();
		buf.pos(x, y, GuiGraphics.zLevel).tex(0, 0).endVertex();
		tess.draw();

		GlStateManager.disableBlend();
	}

	public static void drawLinesBackground(Minecraft mc, int x, int y) {
		drawTexture16by16(mc, x, y, GuiGraphics.LINES_ICON);
	}

	public static void drawStatsBackground(Minecraft mc, int x, int y) {
		drawTexture16by16(mc, x, y, GuiGraphics.STATS_ICON);
	}

	public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel, boolean resetColor) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, resetColor, true, true, true, true);
	}

	public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel, boolean resetColor, boolean displayTop, boolean displayLeft, boolean displayBottom, boolean displayRight) {
		if (resetColor) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		mc.renderEngine.bindTexture(GuiGraphics.BACKGROUND_TEXTURE);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		if (displayTop) {
			//Top Side
			buf.pos(guiLeft + 15, guiTop + 15, zLevel).tex(0.33, 0.33).endVertex();
			buf.pos(right - 15, guiTop + 15, zLevel).tex(0.66, 0.33).endVertex();
			buf.pos(right - 15, guiTop, zLevel).tex(0.66, 0).endVertex();
			buf.pos(guiLeft + 15, guiTop, zLevel).tex(0.33, 0).endVertex();
		}

		if (displayLeft) {
			//Left Side
			buf.pos(guiLeft, bottom - 15, zLevel).tex(0, 0.66).endVertex();
			buf.pos(guiLeft + 15, bottom - 15, zLevel).tex(0.33, 0.66).endVertex();
			buf.pos(guiLeft + 15, guiTop + 15, zLevel).tex(0.33, 0.33).endVertex();
			buf.pos(guiLeft, guiTop + 15, zLevel).tex(0, 0.33).endVertex();
		}

		if (displayBottom) {
			//Bottom Side
			buf.pos(guiLeft + 15, bottom, zLevel).tex(0.33, 1).endVertex();
			buf.pos(right - 15, bottom, zLevel).tex(0.66, 1).endVertex();
			buf.pos(right - 15, bottom - 15, zLevel).tex(0.66, 0.66).endVertex();
			buf.pos(guiLeft + 15, bottom - 15, zLevel).tex(0.33, 0.66).endVertex();
		}

		if (displayRight) {
			//Right Side
			buf.pos(right - 15, bottom - 15, zLevel).tex(0.66, 0.66).endVertex();
			buf.pos(right, bottom - 15, zLevel).tex(1, 0.66).endVertex();
			buf.pos(right, guiTop + 15, zLevel).tex(1, 0.33).endVertex();
			buf.pos(right - 15, guiTop + 15, zLevel).tex(0.66, 0.33).endVertex();
		}

		if (displayTop && displayLeft) {
			//Top Left
			buf.pos(guiLeft, guiTop + 15, zLevel).tex(0, 0.33).endVertex();
			buf.pos(guiLeft + 15, guiTop + 15, zLevel).tex(0.33, 0.33).endVertex();
			buf.pos(guiLeft + 15, guiTop, zLevel).tex(0.33, 0).endVertex();
			buf.pos(guiLeft, guiTop, zLevel).tex(0, 0).endVertex();
		}

		if (displayBottom && displayLeft) {
			//Bottom Left
			buf.pos(guiLeft, bottom, zLevel).tex(0, 1).endVertex();
			buf.pos(guiLeft + 15, bottom, zLevel).tex(0.33, 1).endVertex();
			buf.pos(guiLeft + 15, bottom - 15, zLevel).tex(0.33, 0.66).endVertex();
			buf.pos(guiLeft, bottom - 15, zLevel).tex(0, 0.66).endVertex();
		}

		if (displayBottom && displayRight) {
			//Bottom Right
			buf.pos(right - 15, bottom, zLevel).tex(0.66, 1).endVertex();
			buf.pos(right, bottom, zLevel).tex(1, 1).endVertex();
			buf.pos(right, bottom - 15, zLevel).tex(1, 0.66).endVertex();
			buf.pos(right - 15, bottom - 15, zLevel).tex(0.66, 0.66).endVertex();
		}

		if (displayTop && displayRight) {
			//Top Right
			buf.pos(right - 15, guiTop + 15, zLevel).tex(0.66, 0.33).endVertex();
			buf.pos(right, guiTop + 15, zLevel).tex(1, 0.33).endVertex();
			buf.pos(right, guiTop, zLevel).tex(1, 0).endVertex();
			buf.pos(right - 15, guiTop, zLevel).tex(0.66, 0).endVertex();
		}

		//Center
		buf.pos(guiLeft + 15, bottom - 15, zLevel).tex(0.33, 0.66).endVertex();
		buf.pos(right - 15, bottom - 15, zLevel).tex(0.66, 0.66).endVertex();
		buf.pos(right - 15, guiTop + 15, zLevel).tex(0.66, 0.33).endVertex();
		buf.pos(guiLeft + 15, guiTop + 15, zLevel).tex(0.33, 0.33).endVertex();

		tess.draw();
	}
}
