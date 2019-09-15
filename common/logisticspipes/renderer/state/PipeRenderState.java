package logisticspipes.renderer.state;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import logisticspipes.interfaces.IClientState;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
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

	public List<RenderEntry> cachedRenderer = null;
	public Cache<LocalCacheType, Object> objectCache = CacheBuilder.newBuilder().build();
	public int cachedRenderIndex = -1;
	public boolean forceRenderOldPipe = false;
	private boolean[] solidSidesCache = new boolean[6];
	private boolean savedStateHasMCMultiParts = false;

	public int[] buffer = null;
	public Map<Identifier, GLRenderList> renderLists;

	private boolean dirty = true;

	public PipeRenderState() {
	}

	public void clean() {
		dirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		clearRenderCaches();
	}

	public boolean isDirty() {
		return dirty || pipeConnectionMatrix.isDirty() || textureMatrix.isDirty();
	}

	public boolean needsRenderUpdate() {
		return pipeConnectionMatrix.isDirty() || textureMatrix.isDirty();
	}

	public void checkForRenderUpdate(BlockView worldIn, BlockPos blockPos) {
		boolean[] solidSides = new boolean[6];
		for (Direction dir : Direction.values()) {
			DoubleCoordinates pos = CoordinateUtils.add(new DoubleCoordinates(blockPos), dir);
			BlockState blockSide = pos.getBlockState(worldIn);
			if (blockSide != null && blockSide.isSideSolid(worldIn, pos.getBlockPos(), dir.getOpposite()) && !pipeConnectionMatrix.isConnected(dir)) {
				solidSides[dir.ordinal()] = true;
			}
		}
		if (!Arrays.equals(solidSides, solidSidesCache)) {
			solidSidesCache = solidSides.clone();
			clearRenderCaches();
		}
		DoubleCoordinates pos = new DoubleCoordinates(blockPos);
		BlockEntity tile = pos.getBlockEntity(worldIn);
		if (tile instanceof LogisticsTileGenericPipe) {
			boolean hasParts = SimpleServiceLocator.mcmpProxy.hasParts((LogisticsTileGenericPipe) tile);
			if (savedStateHasMCMultiParts != hasParts) {
				savedStateHasMCMultiParts = hasParts;
				clearRenderCaches();
			}
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
	}

	@Override
	public void readData(LPDataInput input) {
		pipeConnectionMatrix.readData(input);
		textureMatrix.readData(input);
	}
}
