package logisticspipes.proxy.td;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.ITDProxy;
import logisticspipes.proxy.td.subproxies.ITDPart;
import logisticspipes.proxy.td.subproxies.TDPart;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.repack.codechicken.lib.render.CCRenderState;
import cofh.repack.codechicken.lib.render.uv.IconTransformation;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ducts.item.TileItemDuct;
import cofh.thermaldynamics.ducts.item.TravelingItem;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.render.RenderDuct;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ThermalDynamicsProxy implements ITDProxy {

	private IconTransformation connectionTextureBasic;
	private IconTransformation connectionTextureActive;
	private IconTransformation connectionTextureInactive;
	
	@Override
	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformaitonManager.registerProvider(TileItemDuct.class, TDDuctInformationProvider.class);
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
		return tile instanceof TileItemDuct;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderPipeConnections(LogisticsTileGenericPipe pipeTile, RenderBlocks renderer) {
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			if(pipeTile.renderState.pipeConnectionMatrix.isTDConnected(dir)) {
				IconTransformation texture = connectionTextureBasic;
				if(pipeTile.renderState.textureMatrix.isRouted()) {
					if(pipeTile.renderState.textureMatrix.isRoutedInDir(dir)) {
						texture = connectionTextureActive;
					} else {
						texture = connectionTextureInactive;
					}
				}
				double move = 0.25;
				Translation localTranslation = new Translation(pipeTile.xCoord + 0.5D + dir.offsetX * move, pipeTile.yCoord + 0.5D + dir.offsetY * move, pipeTile.zCoord + 0.5D + dir.offsetZ * move);
				RenderDuct.modelConnection[2][dir.ordinal()].render(new CCRenderState.IVertexOperation[] { localTranslation, texture});
			}
		}
	}
	
	@Override
	public void registerTextures(IIconRegister iconRegister) {
		if(connectionTextureBasic == null) {
			connectionTextureBasic = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/ThermalDynamicsConnection-Basic"));
			connectionTextureActive = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/ThermalDynamicsConnection-Active"));
			connectionTextureInactive = new IconTransformation(iconRegister.registerIcon("logisticspipes:" + "pipes/ThermalDynamicsConnection-Inactive"));
		} else {
			connectionTextureBasic.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/ThermalDynamicsConnection-Basic");
			connectionTextureActive.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/ThermalDynamicsConnection-Active");
			connectionTextureInactive.icon = iconRegister.registerIcon("logisticspipes:" + "pipes/ThermalDynamicsConnection-Inactive");
		}
	}

	@Override
	public boolean isBlockedSide(TileEntity with, ForgeDirection opposite) {
		if(!(with instanceof TileItemDuct)) return false;
		return ((TileItemDuct)with).isBlockedSide(opposite.ordinal());
	}
}
