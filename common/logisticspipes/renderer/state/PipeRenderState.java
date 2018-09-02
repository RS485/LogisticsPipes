package logisticspipes.renderer.state;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import logisticspipes.interfaces.IClientState;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderState;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import logisticspipes.renderer.newpipe.GLRenderList;
import logisticspipes.renderer.newpipe.RenderEntry;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class PipeRenderState implements IClientState {

	public enum LocalCacheType {
		QUADS
	}

	public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
	public final TextureMatrix textureMatrix = new TextureMatrix();
	public final IBCRenderState bcRenderState;

	public List<RenderEntry> cachedRenderer = null;
	public Cache<LocalCacheType, Object> objectCache = CacheBuilder.newBuilder().build();
	public boolean forceRenderOldPipe = false;
	private boolean[] solidSidesCache = new boolean[6];

	public int[] buffer = null;
	public Map<ResourceLocation, GLRenderList> renderLists;

	private boolean dirty = true;

	public PipeRenderState(IBCTilePart tilePart) {
		bcRenderState = tilePart.getBCRenderState();
	}

	public void clean() {
		dirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		bcRenderState.clean();
		clearRenderCaches();
	}

	public boolean isDirty() {
		return dirty || pipeConnectionMatrix.isDirty() || textureMatrix.isDirty() || bcRenderState.isDirty();
	}

	public boolean needsRenderUpdate() {
		return pipeConnectionMatrix.isDirty() || textureMatrix.isDirty() || bcRenderState.needsRenderUpdate();
	}

	public void checkSolidFaces(IBlockAccess worldIn, BlockPos blockPos) {
		boolean[] solidSides = new boolean[6];
		for (EnumFacing dir : EnumFacing.VALUES) {
			DoubleCoordinates pos = CoordinateUtils.add(new DoubleCoordinates(blockPos), dir);
			IBlockState blockSide = pos.getBlockState(worldIn);
			if (blockSide != null && blockSide.isSideSolid(worldIn, pos.getBlockPos(), dir.getOpposite()) && !pipeConnectionMatrix.isConnected(dir)) {
				solidSides[dir.ordinal()] = true;
			}
		}
		if (!Arrays.equals(solidSides, solidSidesCache)) {
			solidSidesCache = solidSides.clone();
			clearRenderCaches();
		}
	}

	public void clearRenderCaches() {
		cachedRenderer = null;
		objectCache.invalidateAll();
		objectCache.cleanUp();
	}

	@Override
	public void writeData(LPDataOutput output) {
		pipeConnectionMatrix.writeData(output);
		textureMatrix.writeData(output);
		bcRenderState.writeData_LP(output); //Always needs to be last. Different length depending on proxy loading state.
	}

	@Override
	public void readData(LPDataInput input) {
		pipeConnectionMatrix.readData(input);
		textureMatrix.readData(input);
		bcRenderState.readData_LP(input); //Always needs to be last. Different length depending on proxy loading state.
	}
}
