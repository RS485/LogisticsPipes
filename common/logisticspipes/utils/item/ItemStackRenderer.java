/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/mc16/LICENSE.md
 */

package logisticspipes.utils.item;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.Data;
import lombok.experimental.Accessors;

import logisticspipes.LPItems;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.SimpleGraphics;
import network.rs485.logisticspipes.util.TextUtil;

@Data
@Accessors(chain = true)
@SideOnly(Side.CLIENT)
public class ItemStackRenderer {

	private RenderManager renderManager;
	private RenderItem renderItem;
	private TextureManager texManager;
	private FontRenderer fontRenderer;
	private RenderEntityItem itemEntityRenderer;

	@Nonnull
	private ItemStack itemstack = ItemStack.EMPTY;
	private ItemIdentifierStack itemIdentStack;
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
	private World world;
	private float partialTickTime;

	public ItemStackRenderer(int posX, int posY, float zLevel, boolean renderEffects, boolean ignoreDepth) {
		this.posX = posX;
		this.posY = posY;
		this.zLevel = zLevel;
		this.renderEffects = renderEffects;
		this.ignoreDepth = ignoreDepth;
		renderManager = Minecraft.getMinecraft().getRenderManager();
		fontRenderer = renderManager.getFontRenderer();
		world = renderManager.world;
		texManager = renderManager.renderEngine;
		if (texManager == null) texManager = Minecraft.getMinecraft().getTextureManager();
		renderItem = Minecraft.getMinecraft().getRenderItem();
		itemEntityRenderer = new RenderEntityItem(renderManager, renderItem);
		scaleX = 1.0F;
		scaleY = 1.0F;
		scaleZ = 1.0F;
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, DisplayAmount displayAmount) {
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, zLevel, displayAmount, true, false);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, float zLevel, DisplayAmount displayAmount, boolean renderEffect, boolean ignoreDepth) {
		ItemStackRenderer itemStackRenderer = new ItemStackRenderer(0, 0, zLevel, renderEffect, ignoreDepth);
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

			if (!itemstack.isEmpty()) {
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
		assert displayAmount != null;
		assert renderItem != null;
		assert texManager != null;
		assert fontRenderer != null;
		assert scaleX != 0.0F;
		assert scaleY != 0.0F;
		assert scaleZ != 0.0F;

		GlStateManager.pushMatrix();

		// The only thing that ever sets NORMALIZE are slimes. It never gets disabled and it interferes with our lightning in the HUD.
		GlStateManager.disableNormalize();

		// set up lightning
		GlStateManager.scale(1.0F / scaleX, 1.0F / scaleY, 1.0F / scaleZ);
		RenderHelper.enableGUIStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		GlStateManager.scale(scaleX, scaleY, scaleZ);

		if (ignoreDepth) {
			GlStateManager.disableDepth();
		} else {
			GlStateManager.enableDepth();
		}

		renderItem.zLevel += zLevel;

		if (itemIdentStack != null) {
			if (itemIdentStack.getStackSize() < 1) {
				itemstack = itemIdentStack.getItem().unsafeMakeNormalStack(1);
			} else {
				itemstack = itemIdentStack.unsafeMakeNormalStack();
			}
		}

		IBakedModel bakedmodel = renderItem.getItemModelWithOverrides(itemstack, null, (renderEffects ? Minecraft.getMinecraft().player : null));

		GlStateManager.pushMatrix();
		this.texManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		this.texManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.setupGuiTransform(posX, posY, bakedmodel.isGui3d());
		bakedmodel = ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
		renderItem.renderItem(itemstack, bakedmodel);
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();

		this.texManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		this.texManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

		renderItem.zLevel -= zLevel;

		// disable lightning
		RenderHelper.disableStandardItemLighting();

		if (ignoreDepth) {
			GlStateManager.disableDepth();
		} else {
			GlStateManager.enableDepth();
		}
		// 20 should be about the size of a block
		GuiGraphics.drawDurabilityBar(itemstack, posX, posY, zLevel + 20.0F);

		// if we want to render the amount, do that
		if (displayAmount != DisplayAmount.NEVER) {
			if (ignoreDepth) {
				GlStateManager.disableDepth();
			} else {
				GlStateManager.enableDepth();
			}

			FontRenderer specialFontRenderer = itemstack.getItem().getFontRenderer(itemstack);

			if (specialFontRenderer != null) {
				fontRenderer = specialFontRenderer;
			}

			GlStateManager.disableLighting();
			String amountString = TextUtil.getThreeDigitFormattedNumber(itemIdentStack != null ? itemIdentStack.getStackSize() : itemstack.getCount(), displayAmount == DisplayAmount.ALWAYS);
			GlStateManager.translate(0.0F, 0.0F, zLevel + 130.0F);

			// using a translated shadow does not hurt and works with the HUD
			SimpleGraphics.drawStringWithTranslatedShadow(fontRenderer, amountString, posX + 17 - fontRenderer.getStringWidth(amountString), posY + 9, Color.getValue(Color.WHITE));

			GlStateManager.translate(0.0F, 0.0F, -(zLevel + 130.0F));
		}

		GlStateManager.popMatrix();
	}

	private void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d) {
		GlStateManager.translate((float) xPosition, (float) yPosition, 100.0F + renderItem.zLevel);
		GlStateManager.translate(8.0F, 8.0F, 0.0F);
		GlStateManager.scale(1.0F, -1.0F, 1.0F);
		GlStateManager.scale(16.0F, 16.0F, 16.0F);

		if (isGui3d) {
			GlStateManager.enableLighting();
		} else {
			GlStateManager.disableLighting();
		}
	}

	public void renderInWorld() {
		assert renderManager != null;
		assert renderItem != null;
		assert scaleX != 0.0F;
		assert scaleY != 0.0F;
		assert scaleZ != 0.0F;

		if (entityitem == null || !ItemStack.areItemStacksEqual(entityitem.getItem(), itemstack)) {
			Objects.requireNonNull(world, "World is needed for EntityItem creation");
			if (itemstack.isEmpty()) {
				// :itemcard: 🤷
				itemstack = new ItemStack(LPItems.itemCard);
			}
			entityitem = new EntityItem(world, 0.0D, 0.0D, 0.0D, itemstack);
			entityitem.getItem().setCount(1);
			entityitem.hoverStart = 0.0F;
		}

		Item item = itemstack.getItem();
		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			if (block instanceof BlockPane) {
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
			}
		} else if (item == LPItems.requestTable) {
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
		}

		itemEntityRenderer.doRender(entityitem, posX, posY, zLevel, 0.0F, partialTickTime);
	}

	public void renderItemInGui(float x, float y, Item item, float zLevel, float scale) {
		// TODO check if I can set position and the scale here
		this.setPosX(0);
		this.setPosY(0);
		this.setScaleX(1f);
		this.setScaleY(1f);
		this.setScaleZ(1f);
		this.itemstack = new ItemStack(item);
		this.displayAmount = DisplayAmount.NEVER;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, -100.0);
		GlStateManager.scale(scale, scale, 1f);
		GlStateManager.disableDepth();
		float previousZ = renderItem.zLevel;
		renderItem.zLevel = zLevel;
		this.renderInGui();
		renderItem.zLevel = previousZ;
		GlStateManager.enableDepth();
		GlStateManager.scale(1 / scale, 1 / scale, 1f);
		GlStateManager.translate(-x, -y, 100.0);
		GlStateManager.popMatrix();
	}

	public enum DisplayAmount {
		HIDE_ONE,
		ALWAYS,
		NEVER,
	}

}
