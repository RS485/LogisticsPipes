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
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.SimpleGraphics;
import logisticspipes.utils.string.StringUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
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
		this.renderBlocks = mcRenderBlocks;
		this.renderItem = mcRenderItem;
		this.texManager = mcTextureManager;
		this.fontRenderer = mcFontRenderer;
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, DisplayAmount displayAmount) {
		renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, zLevel, displayAmount, true, true, false);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, DisplayAmount displayAmount, boolean renderInColor, boolean renderEffect, boolean ignoreDepth) {
		RenderHelper.enableGUIStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);

		// The only thing that ever sets NORMALIZE are slimes. It never gets disabled and it interferes with our lightning in the HUD.
		GL11.glDisable(GL11.GL_NORMALIZE);

		int ppi = 0;
		int column = 0;
		int row = 0;
		ItemStackRenderer itemStackRenderer = new ItemStackRenderer(null, displayAmount, 0, 0, zLevel, renderEffect, ignoreDepth, renderInColor);

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
				itemStackRenderer.setItemstack(itemstack).setPosX(x).setPosY(y);
				itemStackRenderer.render();
			}

			column++;
			if (column >= columns) {
				row++;
				column = 0;
			}
		}

		RenderHelper.disableStandardItemLighting();
	}

	public void render() {
		// Rendering the block/item with lightning and maybe depth, but without text
		GL11.glEnable(GL11.GL_LIGHTING);
		if (ignoreDepth) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		// RenderBlocks is a heavy object, so instantiating it everytime is a bad idea
		if (!ForgeHooksClient.renderInventoryItem(mcRenderBlocks, texManager, itemstack, renderInColor, zLevel, posX, posY)) {
			renderItem.zLevel += zLevel;
			renderItem.renderItemIntoGUI(fontRenderer, texManager, itemstack, posX, posY, renderEffects);
			renderItem.zLevel -= zLevel;
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		renderItem.renderItemOverlayIntoGUI(fontRenderer, texManager, itemstack, posX, posY, "");
		if (ignoreDepth) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		// if we want to render the amount, do that
		if (displayAmount != DisplayAmount.NEVER) {
			FontRenderer specialFontRenderer = itemstack.getItem().getFontRenderer(itemstack);

			if (specialFontRenderer != null) {
				fontRenderer = specialFontRenderer;
			}

			GL11.glDisable(GL11.GL_LIGHTING);
			String amountString = StringUtils.getFormatedStackSize(itemstack.stackSize, displayAmount == DisplayAmount.ALWAYS);

			// 20 should be about the size of a block + 20 for the effect
			GL11.glTranslatef(0.0F, 0.0F, zLevel + 40.0F);
			// using a translated shadow does not hurt and works with the HUD
			SimpleGraphics.drawStringWithTranslatedShadow(fontRenderer, amountString, posX + 17 - fontRenderer.getStringWidth(amountString), posY + 9, Color.getValue(Color.WHITE));
			GL11.glTranslatef(0.0F, 0.0F, -zLevel - 40.0F);
		}
	}

	public enum DisplayAmount {
		HIDE_ONE,
		ALWAYS,
		NEVER,
	}
}
