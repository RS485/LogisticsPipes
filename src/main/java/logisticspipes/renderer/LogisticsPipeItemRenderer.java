/*
package logisticspipes.renderer;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.newpipe.LogisticsNewPipeItemRenderer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

public class LogisticsPipeItemRenderer implements IItemRenderer {

	private ClientConfiguration config = LogisticsPipes.getClientPlayerConfig();
	private LogisticsNewPipeItemRenderer newRenderer;

	private final boolean renderAsBlock;

	private static final float PIPE_MIN_POS = 0.25F;
	private static final float PIPE_MAX_POS = 0.75F;

	public LogisticsPipeItemRenderer(boolean targeted) {
		newRenderer = new LogisticsNewPipeItemRenderer(targeted);
		renderAsBlock = targeted;
	}

	private void renderPipeItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT); //don't break other mods' guis when holding a pipe
		//force transparency
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

		// GL11.glBindTexture(GL11.GL_TEXTURE_2D, 10);
		Tessellator tessellator = Tessellator.instance;

		Block block = LogisticsPipes.LogisticsPipeBlock;
		TextureAtlasSprite icon = item.getItem().getIconFromDamage(0);

		if (icon == null) {
			icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
		}

		block.setBlockBounds(LogisticsPipeItemRenderer.PIPE_MIN_POS, 0.0F, LogisticsPipeItemRenderer.PIPE_MIN_POS, LogisticsPipeItemRenderer.PIPE_MAX_POS, 1.0F, LogisticsPipeItemRenderer.PIPE_MAX_POS);
		//block.setBlockBounds(PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MAX_POS, PIPE_MAX_POS);
		block.setBlockBoundsForItemRender();
		render.setRenderBoundsFromBlock(block);

		GL11.glTranslatef(translateX, translateY, translateZ);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, icon);
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		GL11.glPopAttrib(); // nicely leave the rendering how it was
	}

	private void renderBlockItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		Tessellator tessellator = Tessellator.instance;

		Block block = LogisticsPipes.LogisticsPipeBlock;

		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		block.setBlockBoundsForItemRender();
		render.setRenderBoundsFromBlock(block);

		GL11.glTranslatef(translateX, translateY, translateZ);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(0));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(1));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(2));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(3));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(4));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(5));
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		if (renderAsBlock) {
			renderBlockItem(render, item, translateX, translateY, translateZ);
		} else {
			renderPipeItem(render, item, translateX, translateY, translateZ);
		}
	}

	/** IItemRenderer implementation **
	/

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		if (config.isUseNewRenderer()) {
			return newRenderer.handleRenderType(item, type);
		}
		switch (type) {
			case ENTITY:
				return true;
			case EQUIPPED:
				return true;
			case EQUIPPED_FIRST_PERSON:
				return true;
			case INVENTORY:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return config.isUseNewRenderer() ? newRenderer.shouldUseRenderHelper(type, item, helper) : true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (config.isUseNewRenderer() && SimpleServiceLocator.cclProxy.isActivated()) {
			newRenderer.renderItem(type, item, data);
			return;
		}
		switch (type) {
			case ENTITY:
				renderItem((RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
				break;
			case EQUIPPED:
				renderItem((RenderBlocks) data[0], item, -0.4f, 0.50f, 0.35f);
				break;
			case EQUIPPED_FIRST_PERSON:
				renderItem((RenderBlocks) data[0], item, -0.4f, 0.50f, 0.35f);
				break;
			case INVENTORY:
				renderItem((RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
				break;
			default:
		}
	}
}
*/