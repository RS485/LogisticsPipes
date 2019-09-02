package logisticspipes.proxy.td;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cofh.thermaldynamics.duct.tiles.TileDuctItem;
import cofh.thermaldynamics.render.RenderDuct;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ITDProxy;
import logisticspipes.proxy.object3d.interfaces.TextureTransformation;
import logisticspipes.proxy.object3d.operation.LPTranslation;
import logisticspipes.proxy.td.subproxies.ITDPart;
import logisticspipes.proxy.td.subproxies.TDPart;
import logisticspipes.renderer.newpipe.RenderEntry;

public class ThermalDynamicsProxy implements ITDProxy {

	private TextureTransformation connectionTextureBasic;
	private TextureTransformation connectionTextureActive;
	private TextureTransformation connectionTextureInactive;

	@Override
	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformationManager.registerProvider(TileDuctItem.class, TDDuctInformationProvider.class);
	}

	@Override
	public ITDPart getTDPart(LogisticsTileGenericPipe pipe) {
		return new TDPart(pipe);
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public boolean isItemDuct(TileEntity tile) {
		return tile instanceof TileDuctItem;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPipeConnections(LogisticsTileGenericPipe pipeTile, List<RenderEntry> renderList) {
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (pipeTile.renderState.pipeConnectionMatrix.isTDConnected(dir)) {
				TextureTransformation texture = connectionTextureBasic;
				if (pipeTile.renderState.textureMatrix.isRouted()) {
					if (pipeTile.renderState.textureMatrix.isRoutedInDir(dir)) {
						texture = connectionTextureActive;
					} else {
						texture = connectionTextureInactive;
					}
				}
				double move = 0.25;
				LPTranslation localTranslation = new LPTranslation(0.5D + dir.getDirectionVec().getX() * move, 0.5D + dir.getDirectionVec().getY() * move, 0.5D + dir.getDirectionVec().getZ() * move);
				renderList.add(new RenderEntry(SimpleServiceLocator.cclProxy.wrapModel(RenderDuct.modelConnection[2][dir.ordinal()]), localTranslation, texture));
			}
		}
	}

	@Override
	public void registerTextures(TextureMap iconRegister) {
		if (connectionTextureBasic == null) {
			connectionTextureBasic = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerSprite(new ResourceLocation("logisticspipes", "blocks/pipes/thermaldynamicsconnection-basic")));
			connectionTextureActive = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerSprite(new ResourceLocation("logisticspipes", "blocks/pipes/thermaldynamicsconnection-active")));
			connectionTextureInactive = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerSprite(new ResourceLocation("logisticspipes", "blocks/pipes/thermaldynamicsconnection-inactive")));
		} else {
			connectionTextureBasic.update(iconRegister.registerSprite(new ResourceLocation("logisticspipes", "blocks/pipes/thermaldynamicsconnection-basic")));
			connectionTextureActive.update(iconRegister.registerSprite(new ResourceLocation("logisticspipes", "blocks/pipes/thermaldynamicsconnection-active")));
			connectionTextureInactive.update(iconRegister.registerSprite(new ResourceLocation("logisticspipes", "blocks/pipes/thermaldynamicsconnection-inactive")));
		}
	}

	@Override
	public boolean isBlockedSide(TileEntity with, EnumFacing opposite) {
		if (!(with instanceof TileDuctItem)) {
			return false;
		}
		return ((TileDuctItem) with).isSideBlocked(opposite.ordinal());
	}
}