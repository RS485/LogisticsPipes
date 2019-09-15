package logisticspipes.routing.pathfinder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;

public class PipeInformationManager {

	public static final PipeInformationManager INSTANCE = new PipeInformationManager();

	private Map<Class<?> /*BlockEntity*/, Class<? extends IPipeInformationProvider>> infoProvider = new HashMap<>();

	private PipeInformationManager() {}

	public IPipeInformationProvider getInformationProviderFor(BlockEntity tile) {
		if (tile == null) {
			return null;
		}
		if (tile instanceof IPipeInformationProvider) {
			return (IPipeInformationProvider) tile;
		} else if (tile instanceof ISubMultiBlockPipeInformationProvider) {
			return ((ISubMultiBlockPipeInformationProvider) tile).getMainTile();
		} else {
			for (Class<?> type : infoProvider.keySet()) {
				if (type.isAssignableFrom(tile.getClass())) {
					try {
						IPipeInformationProvider provider = infoProvider.get(type).getDeclaredConstructor(type).newInstance(type.cast(tile));
						if (provider.isCorrect(ConnectionPipeType.UNDEFINED)) {
							return provider;
						}
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException | SecurityException | NoSuchMethodException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public void registerProvider(Class<?> source, Class<? extends IPipeInformationProvider> provider) {
		try {
			provider.getDeclaredConstructor(source);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		infoProvider.put(source, provider);
	}

	public boolean canConnect(IPipeInformationProvider startPipe, IPipeInformationProvider provider, Direction direction, boolean flag) {
		return startPipe.canConnect(provider.getTile(), direction, flag) && provider.canConnect(startPipe.getTile(), direction.getOpposite(), flag);
	}

	public boolean isItemPipe(BlockEntity tile) {
		return isPipe(tile, true, ConnectionPipeType.ITEM);
	}

	public boolean isPipe(BlockEntity tile) {
		return isPipe(tile, true, ConnectionPipeType.UNDEFINED);
	}

	public boolean isPipe(BlockEntity tile, boolean check, ConnectionPipeType pipeType) {
		if (tile == null) {
			return false;
		}
		if (tile instanceof IPipeInformationProvider) {
			return true;
		} else if (tile instanceof ISubMultiBlockPipeInformationProvider) {
			return pipeType == ConnectionPipeType.MULTI;
		} else {
			for (Class<?> type : infoProvider.keySet()) {
				if (type.isAssignableFrom(tile.getClass())) {
					try {
						IPipeInformationProvider provider = infoProvider.get(type).getDeclaredConstructor(type).newInstance(type.cast(tile));
						if (!check || provider.isCorrect(pipeType)) {
							return true;
						}
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	public boolean isNotAPipe(BlockEntity tile) {
		if (tile instanceof IPipeInformationProvider) {
			return false;
		} else if (tile instanceof ISubMultiBlockPipeInformationProvider) {
			return false;
		} else {
			for (Class<?> type : infoProvider.keySet()) {
				if (type.isAssignableFrom(tile.getClass())) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isFluidPipe(BlockEntity tile) {
		IPipeInformationProvider info = getInformationProviderFor(tile);
		if (info == null) {
			return false;
		}
		return info.isFluidPipe();
	}
}
