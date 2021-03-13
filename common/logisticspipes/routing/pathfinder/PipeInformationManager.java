package logisticspipes.routing.pathfinder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import network.rs485.logisticspipes.connection.ConnectionType;

public class PipeInformationManager {

	private Map<Class<?> /*TileEntity*/, Class<? extends IPipeInformationProvider>> infoProvider = new HashMap<>();

	public IPipeInformationProvider getInformationProviderFor(TileEntity tile) {
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
						if (provider.isCorrect(ConnectionType.UNDEFINED)) {
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

	public boolean canConnect(IPipeInformationProvider startPipe, IPipeInformationProvider provider, EnumFacing direction, boolean flag) {
		return startPipe.canConnect(provider.getTile(), direction, flag) && provider.canConnect(startPipe.getTile(), direction.getOpposite(), flag);
	}

	public boolean isItemPipe(TileEntity tile) {
		return isPipe(tile, true, ConnectionType.ITEM);
	}

	public boolean isPipe(TileEntity tile) {
		return isPipe(tile, true, ConnectionType.UNDEFINED);
	}

	public boolean isPipe(TileEntity tile, boolean check, ConnectionType pipeType) {
		if (tile == null) {
			return false;
		}
		if (tile instanceof IPipeInformationProvider) {
			return true;
		} else if (tile instanceof ISubMultiBlockPipeInformationProvider) {
			return pipeType == ConnectionType.MULTIBLOCK;
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

	public boolean isNotAPipe(TileEntity tile) {
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

	public boolean isFluidPipe(TileEntity tile) {
		IPipeInformationProvider info = getInformationProviderFor(tile);
		if (info == null) {
			return false;
		}
		return info.isFluidPipe();
	}
}
