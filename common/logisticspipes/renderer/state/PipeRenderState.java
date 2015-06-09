package logisticspipes.renderer.state;

import java.io.IOException;
import java.util.List;

import logisticspipes.interfaces.IClientState;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderState;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.renderer.newpipe.GLRenderList;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeRenderState implements IClientState {

	public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
	public final TextureMatrix textureMatrix = new TextureMatrix();
	public final IBCRenderState bcRenderState;

	public List<Pair<IModel3D, I3DOperation[]>> cachedRenderer = null;
	public boolean forceRenderOldPipe = false;
	public boolean solidSidesCache[] = new boolean[6];

	public int[] buffer = null;
	public GLRenderList renderList;
	/*
	 * This is a placeholder for the pipe renderer to set to a value that the BlockGenericPipe->TileGenericPipe will then return the the WorldRenderer
	 */
	@SideOnly(Side.CLIENT)
	public IIcon currentTexture;
	@SideOnly(Side.CLIENT)
	public IIcon[] textureArray;

	private boolean dirty = true;

	public PipeRenderState(IBCTilePart tilePart) {
		bcRenderState = tilePart.getBCRenderState();
	}

	public void clean() {
		dirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		bcRenderState.clean();
		cachedRenderer = null;
	}

	public boolean isDirty() {
		return dirty || pipeConnectionMatrix.isDirty() || textureMatrix.isDirty() || bcRenderState.isDirty();
	}

	public boolean needsRenderUpdate() {
		return pipeConnectionMatrix.isDirty() || textureMatrix.isDirty() || bcRenderState.needsRenderUpdate();
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		pipeConnectionMatrix.writeData(data);
		textureMatrix.writeData(data);
		bcRenderState.writeData_LP(data); //Always needs to be last. Different length depending on proxy loading state.
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		pipeConnectionMatrix.readData(data);
		textureMatrix.readData(data);
		bcRenderState.readData_LP(data); //Always needs to be last. Different length depending on proxy loading state.
	}
}
