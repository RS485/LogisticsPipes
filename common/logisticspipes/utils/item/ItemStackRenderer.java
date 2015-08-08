/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils.item;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.SimpleGraphics;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.client.ForgeHooksClient;

import lombok.Data;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL11;

@Data
@Accessors(chain = true)
public class ItemStackRenderer {

	private RenderManager renderManager;
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
	private EntityItem entityitem;
	private World worldObj;
	private float partialTickTime;

	public ItemStackRenderer(int posX, int posY, float zLevel, boolean renderEffects, boolean ignoreDepth, boolean renderInColor) {
		this.posX = posX;
		this.posY = posY;
		this.zLevel = zLevel;
		this.renderEffects = renderEffects;
		this.ignoreDepth = ignoreDepth;
		this.renderInColor = renderInColor;
		renderManager = RenderManager.instance;
		fontRenderer = renderManager.getFontRenderer();
		if (fontRenderer == null) {
			fontRenderer = Minecraft.getMinecraft().fontRenderer;
		}
		worldObj = renderManager.worldObj;
		texManager = renderManager.renderEngine;
		if (texManager == null) {
			texManager = Minecraft.getMinecraft().getTextureManager();
		}
		renderBlocks = RenderBlocks.getInstance();
		renderItem = RenderItem.getInstance();
		scaleX = 1.0F;
		scaleY = 1.0F;
		scaleZ = 1.0F;
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, DisplayAmount displayAmount) {
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, zLevel, displayAmount, true, true, false);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, DisplayAmount displayAmount, boolean renderInColor, boolean renderEffect, boolean ignoreDepth) {
		ItemStackRenderer itemStackRenderer = new ItemStackRenderer(0, 0, zLevel, renderEffect, ignoreDepth, renderInColor);
		itemStackRenderer.setDisplayAmount(displayAmount);
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
				itemStackRenderer.renderInGui();
			}

			column++;
			if (column >= columns) {
				row++;
				column = 0;
			}
		}
	}

	public void renderInGui() {
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

	public void renderInWorld() {
		assert renderManager != null;
		assert renderItem != null;
		assert scaleX != 0.0F;
		assert scaleY != 0.0F;
		assert scaleZ != 0.0F;

		if (entityitem == null || !ItemStack.areItemStacksEqual(entityitem.getEntityItem(), itemstack)) {
			if (itemstack == null) {
				throw new RuntimeException("No EntityItem and no ItemStack, I do not know what to render!");
			} else {
				if (worldObj == null) {
					throw new NullPointerException("World object is null");
				}
				entityitem = new EntityItem(worldObj, 0.0D, 0.0D, 0.0D, itemstack);
				entityitem.getEntityItem().stackSize = 1;
				entityitem.hoverStart = 0.0F;
			}
		}

		boolean changeColor = renderItem.renderWithColor != renderInColor;
		if (changeColor) {
			renderItem.renderWithColor = renderInColor;
		}

		Item item = itemstack.getItem();
		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).field_150939_a;
			if (block instanceof BlockPane) {
				GL11.glScalef(0.5F, 0.5F, 0.5F);
			}
		} else if (item == LogisticsPipes.logisticsRequestTable) {
			GL11.glScalef(0.5F, 0.5F, 0.5F);
		}

		renderManager.renderEntityWithPosYaw(entityitem, posX, posY, zLevel, 0.0F, partialTickTime);

		if (changeColor) {
			renderItem.renderWithColor = !renderInColor;
		}
	}

	public enum DisplayAmount {
		HIDE_ONE,
		ALWAYS,
		NEVER,
	}
}
