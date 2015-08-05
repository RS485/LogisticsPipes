package logisticspipes.routing.pathfinder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public class PipeInformationManager {

	private Map<Class<?> /*TileEntity*/, Class<? extends IPipeInformationProvider>> infoProvider = new HashMap<Class<?>, Class<? extends IPipeInformationProvider>>();

	public IPipeInformationProvider getInformationProviderFor(TileEntity tile) {
		if (tile == null) {
			return null;
		}
		if (tile instanceof IPipeInformationProvider) {
			return (IPipeInformationProvider) tile;
		} else {
			for (Class<?> type : infoProvider.keySet()) {
				if (type.isAssignableFrom(tile.getClass())) {
					try {
						IPipeInformationProvider provider = infoProvider.get(type).getDeclaredConstructor(type).newInstance(type.cast(tile));
						if (provider.isCorrect()) {
							return provider;
						}
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
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
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		infoProvider.put(source, provider);
	}

	public boolean canConnect(IPipeInformationProvider startPipe, IPipeInformationProvider provider, ForgeDirection direction, boolean flag) {
		return startPipe.canConnect(provider.getTile(), direction, flag) && provider.canConnect(startPipe.getTile(), direction.getOpposite(), flag);
	}

	public boolean isItemPipe(TileEntity tile) {
		if (tile == null) {
			return false;
		}
		if (tile instanceof IPipeInformationProvider) {
			return true;
		} else {
			for (Class<?> type : infoProvider.keySet()) {
				if (type.isAssignableFrom(tile.getClass())) {
					try {
						IPipeInformationProvider provider = infoProvider.get(type).getDeclaredConstructor(type).newInstance(type.cast(tile));
						if (provider.isCorrect() && provider.isItemPipe()) {
							return true;
						}
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	public boolean isPipe(TileEntity tile, boolean check) {
		if (tile == null) {
			return false;
		}
		if (tile instanceof IPipeInformationProvider) {
			return true;
		} else {
			for (Class<?> type : infoProvider.keySet()) {
				if (type.isAssignableFrom(tile.getClass())) {
					try {
						IPipeInformationProvider provider = infoProvider.get(type).getDeclaredConstructor(type).newInstance(type.cast(tile));
						if (!check || provider.isCorrect()) {
							return true;
						}
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
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
