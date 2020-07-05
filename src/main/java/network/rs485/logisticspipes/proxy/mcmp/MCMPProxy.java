/*
 * Copyright (c) 2015  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the MIT license:
 *
 * Copyright (c) 2015  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular MIT license in your project, replace this copyright notice (this line and any lines below and NOT the copyright line above) with the lines from the original MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this file and associated documentation files (the "Source Code"), to deal in the Source Code without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Source Code, and to permit persons to whom the Source Code is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Source Code, which also can be distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package network.rs485.logisticspipes.proxy.mcmp;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.MCMultiPart;
import mcmultipart.api.event.DrawMultipartHighlightEvent;
import mcmultipart.api.multipart.MultipartOcclusionHelper;
import mcmultipart.block.BlockMultipartContainer;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.client.ModelMultipartContainer;
import mcmultipart.multipart.PartInfo;

import logisticspipes.LPBlocks;
import logisticspipes.LPConstants;
import logisticspipes.asm.mcmp.MCMPHooks;
import logisticspipes.modplugins.mcmp.LPPipeMultipart;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.IMCMPBlockAccess;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.IMCMPLTGPCompanion;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.LPTileMultipartContainer;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.MCMPBlockAccess;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.MCMPLTGPCompanion;

public class MCMPProxy implements IMCMPProxy {

	@SideOnly(Side.CLIENT)
	private ModelMultipartContainer modelMultipartContainer;

	public MCMPProxy() {
		MCMPHooks.mcmpGetTEHook = (world, pos) -> {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe lpTile = (LogisticsTileGenericPipe) tile;
				TileEntity nTile = lpTile.imcmpltgpCompanion.getMCMPTileEntity();
				if (nTile != null) {
					return nTile;
				}
			}
			return tile;
		};
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public IMCMPLTGPCompanion createMCMPCompanionFor(LogisticsTileGenericPipe pipe) {
		return new MCMPLTGPCompanion(pipe);
	}

	@Override
	public IMCMPBlockAccess createMCMPBlockAccess() {
		return new MCMPBlockAccess();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addQuads(@Nonnull List<BakedQuad> list, IBlockState state, EnumFacing side, long rand) {
		if (modelMultipartContainer == null) modelMultipartContainer = new ModelMultipartContainer();
		list.addAll(modelMultipartContainer.getQuads(state, side, rand));
	}

	@Override
	public void registerTileEntities() {
		GameRegistry.registerTileEntity(LPTileMultipartContainer.class, new ResourceLocation(LPConstants.LP_MOD_ID, "mcmp_lp_tile_multipart_container"));
	}

	@Override
	public boolean checkIntersectionWith(LogisticsTileGenericPipe logisticsTileGenericPipe, AxisAlignedBB aabb) {
		return ((TileMultipartContainer) logisticsTileGenericPipe.imcmpltgpCompanion.getMCMPTileEntity()).getParts().values().stream().filter(i -> !(i.getPart() instanceof LPPipeMultipart)).anyMatch(i -> MultipartOcclusionHelper
				.testBoxIntersection(i.getPart().getOcclusionBoxes(i), Collections.singletonList(aabb)));
	}

	@Override
	public boolean hasParts(LogisticsTileGenericPipe pipeTile) {
		return ((TileMultipartContainer) pipeTile.imcmpltgpCompanion.getMCMPTileEntity()).getParts().values().stream().anyMatch(i -> !(i.getPart() instanceof LPPipeMultipart));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderTileEntitySpecialRenderer(LogisticsTileGenericPipe tileentity, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance
				.getRenderer(TileMultipartContainer.class);
		if (destroyStage < 0 || (tileentity.imcmpltgpCompanion.getMCMPTileEntity() != null && Minecraft.getMinecraft().objectMouseOver != null)) {
			renderer.render(tileentity.imcmpltgpCompanion.getMCMPTileEntity(), x, y, z, partialTicks, destroyStage, alpha);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onDrawHighlight(DrawBlockHighlightEvent event) {
		if (event instanceof DrawMultipartHighlightEvent) {
			return;
		}
		RayTraceResult hit = event.getTarget();
		if (hit.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		BlockPos pos = hit.getBlockPos();
		EntityPlayer player = event.getPlayer();
		if (player == null) {
			return;
		}
		World world = player.world;
		if (world == null) {
			return;
		}

		if (world.getBlockState(pos).getBlock() == LPBlocks.pipe) {
			Optional<TileMultipartContainer> tile = BlockMultipartContainer.getTile(world, pos);
			if (!tile.isPresent()) {
				return;
			}

			int slotID = hit.subHit;
			if (slotID < 0) {
				return;
			}
			PartInfo info = tile.get().getParts().get(MCMultiPart.slotRegistry.getValue(slotID));
			if (info == null || !(hit.hitInfo instanceof RayTraceResult)) {
				return;
			}
			hit = (RayTraceResult) hit.hitInfo;

			float partialTicks = event.getPartialTicks();
			if (!MinecraftForge.EVENT_BUS.post(new DrawMultipartHighlightEvent(event.getContext(), player, hit, slotID, partialTicks, info))) {
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
						GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.glLineWidth(2.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.depthMask(false);
				IBlockState state = info.getState();

				if (state.getMaterial() != Material.AIR && world.getWorldBorder().contains(pos)) {
					double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
					double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
					double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
					RenderGlobal.drawSelectionBoundingBox(info.getPart().getSelectedBoundingBox(info).grow(0.002).offset(-x, -y, -z),
							0.0F, 0.0F, 0.0F, 0.4F);
				}

				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
			}

			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
		TileEntity tile = event.getWorld().getTileEntity(event.getPos());
		if (tile instanceof LogisticsTileGenericPipe) {
			((LogisticsTileGenericPipe) tile).scheduleNeighborChange();
		}
	}
}
