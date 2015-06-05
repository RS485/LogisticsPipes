/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils.item;

import java.util.List;

import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.SimpleGraphics;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.ForgeHooksClient;

import lombok.Data;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL11;

@Data
@Accessors(chain = true)
public class ItemStackRenderer {

	public static final RenderBlocks mcRenderBlocks = new RenderBlocks();
	public static final RenderItem mcRenderItem = new RenderItem();
	public static final TextureManager mcTextureManager = Minecraft.getMinecraft().getTextureManager();
	public static final FontRenderer mcFontRenderer = Minecraft.getMinecraft().fontRenderer;

	private RenderBlocks renderBlocks;
	private RenderItem renderItem;
	private TextureManager texManager;
	private FontRenderer fontRenderer;

	private ItemStack itemstack;
	private int posX;
	private int posY;
	private float zLevel;
	private float scaleX;
	private float scaleY;
	private float scaleZ;
	private DisplayAmount displayAmount;
	private boolean renderEffects;
	private boolean ignoreDepth;
	private boolean renderInColor;

	public ItemStackRenderer(ItemStack itemstack, DisplayAmount displayAmount, int posX, int posY, float zLevel, boolean renderEffects, boolean ignoreDepth, boolean renderInColor) {
		this.itemstack = itemstack;
		this.displayAmount = displayAmount;
		this.posX = posX;
		this.posY = posY;
		this.zLevel = zLevel;
		this.renderEffects = renderEffects;
		this.ignoreDepth = ignoreDepth;
		this.renderInColor = renderInColor;
		renderBlocks = ItemStackRenderer.mcRenderBlocks;
		renderItem = ItemStackRenderer.mcRenderItem;
		texManager = ItemStackRenderer.mcTextureManager;
		fontRenderer = ItemStackRenderer.mcFontRenderer;
		scaleX = 1.0F;
		scaleY = 1.0F;
		scaleZ = 1.0F;
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, DisplayAmount displayAmount) {
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, zLevel, displayAmount, true, true, false);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, DisplayAmount displayAmount, boolean renderInColor, boolean renderEffect, boolean ignoreDepth) {
		ItemStackRenderer itemStackRenderer = new ItemStackRenderer(null, displayAmount, 0, 0, zLevel, renderEffect, ignoreDepth, renderInColor);
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, itemStackRenderer);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, ItemStackRenderer itemStackRenderer) {
		int ppi = 0;
		int column = 0;
		int row = 0;

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
			if (IItemSearch != null && !IItemSearch.itemSearched(item)) {
				continue;
			}
			ppi++;

			if (ppi <= items * page) {
				continue;
			}
			if (ppi > items * (page + 1)) {
				continue;
			}
			ItemStack itemstack = identifierStack.unsafeMakeNormalStack();
			int x = left + xSize * column;
			int y = top + ySize * row + 1;

			if (itemstack != null) {
				itemStackRenderer.setItemstack(itemstack).setPosX(x).setPosY(y);
				itemStackRenderer.render();
			}

			column++;
			if (column >= columns) {
				row++;
				column = 0;
			}
		}
	}

	public void render() {
		assert itemstack != null;
		assert displayAmount != null;
		assert renderBlocks != null;
		assert renderItem != null;
		assert texManager != null;
		assert fontRenderer != null;
		assert scaleX != 0.0F;
		assert scaleY != 0.0F;
		assert scaleZ != 0.0F;

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

		// The only thing that ever sets NORMALIZE are slimes. It never gets disabled and it interferes with our lightning in the HUD.
		GL11.glDisable(GL11.GL_NORMALIZE);

		// set up lightning
		GL11.glScalef(1.0F / scaleX, 1.0F / scaleY, 1.0F / scaleZ);
		RenderHelper.enableGUIStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		GL11.glScalef(scaleX, scaleY, scaleZ);

		if (ignoreDepth) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		if (!ForgeHooksClient.renderInventoryItem(renderBlocks, texManager, itemstack, renderInColor, zLevel, posX, posY)) {
			renderItem.zLevel += zLevel;
			renderItem.renderItemIntoGUI(fontRenderer, texManager, itemstack, posX, posY, renderEffects);
			renderItem.zLevel -= zLevel;
		}

		// disable lightning
		RenderHelper.disableStandardItemLighting();

		if (ignoreDepth) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		// 20 should be about the size of a block
		GuiGraphics.drawDurabilityBar(itemstack, posX, posY, zLevel + 20.0F);

		// if we want to render the amount, do that
		if (displayAmount != DisplayAmount.NEVER) {
			if (ignoreDepth) {
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			} else {
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}

			FontRenderer specialFontRenderer = itemstack.getItem().getFontRenderer(itemstack);

			if (specialFontRenderer != null) {
				fontRenderer = specialFontRenderer;
			}

			GL11.glDisable(GL11.GL_LIGHTING);
			String amountString = StringUtils.getFormatedStackSize(itemstack.stackSize, displayAmount == DisplayAmount.ALWAYS);

			// 20 should be about the size of a block + 20 for the effect and overlay
			GL11.glTranslatef(0.0F, 0.0F, zLevel + 40.0F);

			// using a translated shadow does not hurt and works with the HUD
			SimpleGraphics.drawStringWithTranslatedShadow(fontRenderer, amountString, posX + 17 - fontRenderer.getStringWidth(amountString), posY + 9, Color.getValue(Color.WHITE));

			GL11.glTranslatef(0.0F, 0.0F, -(zLevel + 40.0F));
		}

		GL11.glPopAttrib();
	}

	public enum DisplayAmount {
		HIDE_ONE,
		ALWAYS,
		NEVER,
	}
}
