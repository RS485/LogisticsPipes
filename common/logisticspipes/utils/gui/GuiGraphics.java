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
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
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
	public static final RenderBlocks mcRenderBlocks = new RenderBlocks();
	public static float zLevel = 0.0F;

	private GuiGraphics() {
	}

	public enum DisplayAmount {
		HIDE_ONE,
		ALWAYS,
		NEVER,
	}

	public static void renderItemStack(ItemStack itemstack, int posX, int posY, float zLevel, TextureManager texManager, RenderItem itemRenderer, FontRenderer fontRenderer, DisplayAmount displayAmount, boolean disableEffects, boolean depthTest) {

		// Rendering the block/item with lightning and maybe depth, but without text
		GL11.glEnable(GL11.GL_LIGHTING);
		if (depthTest) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}

		// RenderBlocks is a heavy object, so instantiating it everytime is a bad idea
		if (!ForgeHooksClient.renderInventoryItem(mcRenderBlocks, texManager, itemstack, true, zLevel, posX, posY)) {
			itemRenderer.zLevel += zLevel;
			itemRenderer.renderItemIntoGUI(fontRenderer, texManager, itemstack, posX, posY, false);

			if (!disableEffects && itemstack.hasEffect(0)) {
				// 20 should be about the size of a block, when rendered this way
				GL11.glTranslatef(0.0F, 0.0F, 20.0F);
				itemRenderer.renderEffect(texManager, posX, posY);
				GL11.glTranslatef(0.0F, 0.0F, -20.0F);
			}

			itemRenderer.zLevel -= zLevel;
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		itemRenderer.renderItemOverlayIntoGUI(fontRenderer, texManager, itemstack, posX, posY, "");
		if (depthTest) {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		}

		// if we want to render the amount, do that
		if (displayAmount != DisplayAmount.NEVER) {
			FontRenderer specialFontRenderer = itemstack.getItem().getFontRenderer(itemstack);

			if (specialFontRenderer != null) {
				fontRenderer = specialFontRenderer;
			}

			GL11.glDisable(GL11.GL_LIGHTING);
			String amountString = StringUtils.getFormatedStackSize(itemstack.stackSize, displayAmount == DisplayAmount.ALWAYS);

			// 20 should be about the size of a block + 20 for the overlay
			GL11.glTranslatef(0.0F, 0.0F, zLevel + 40.0F);
			// using a translated shadow does not hurt and works with the HUD
			SimpleGraphics.drawStringWithTranslatedShadow(fontRenderer, amountString, posX + 17 - fontRenderer.getStringWidth(amountString), posY + 9, Color.getValue(Color.WHITE));
			GL11.glTranslatef(0.0F, 0.0F, -zLevel - 40.0F);
		}
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, Minecraft mc, DisplayAmount displayAmount) {
		renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, zLevel, mc, displayAmount, true);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, Minecraft mc, DisplayAmount displayAmount, boolean color) {
		renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, zLevel, mc, displayAmount, color, false);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, Minecraft mc, DisplayAmount displayAmount, boolean color, boolean disableEffect) {
		renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, zLevel, mc, displayAmount, color, disableEffect, true);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, Minecraft mc, DisplayAmount displayAmount, boolean color, boolean disableEffect, boolean depthTest) {
		RenderHelper.enableGUIStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);

		// The only thing that ever sets NORMALIZE are slimes. It never gets disabled and it interferes with our lightning in the HUD.
		GL11.glDisable(GL11.GL_NORMALIZE);

		int ppi = 0;
		int column = 0;
		int row = 0;
		FontRenderer fontRenderer = mc.fontRenderer;
		RenderItem itemRenderer = new RenderItem();
		itemRenderer.renderWithColor = color;
		for (ItemIdentifierStack identifierStack : _allItems) {
			if (identifierStack == null) {
				column++;
				if (column >= columns) {
					row++;
					column = 0;
				}
				ppi++;
				continue;
			}
			ItemIdentifier item = identifierStack.getItem();
			if (IItemSearch != null && !IItemSearch.itemSearched(item)) continue;
			ppi++;

			if (ppi <= items * page) continue;
			if (ppi > items * (page + 1)) continue;
			ItemStack itemstack = identifierStack.unsafeMakeNormalStack();
			int x = left + xSize * column;
			int y = top + ySize * row + 1;

			if (itemstack != null) {
				renderItemStack(itemstack, x, y, zLevel, mc.renderEngine, itemRenderer, fontRenderer, displayAmount, disableEffect, depthTest);
			}

			column++;
			if (column >= columns) {
				row++;
				column = 0;
			}
		}

		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.disableStandardItemLighting();
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
