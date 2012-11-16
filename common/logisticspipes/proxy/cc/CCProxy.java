package logisticspipes.proxy.cc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.WorldUtil;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import dan200.computer.api.IComputerAccess;

public class CCProxy implements ICCProxy {

	private Field Computer_m_computer;
	private Field Net_m_computer;
	private Field m_apis;
	private Field m_peripherals;
	private Method parseSide;
	private Class<?> computerClass;
	private Class<?> peripheralAPIClass;
	private Field target;
	
	protected boolean valid = false;
	
	public CCProxy() {
		try {
			computerClass = Class.forName("dan200.computer.shared.TileEntityComputer");
			Computer_m_computer = computerClass.getDeclaredField("m_computer");
			Computer_m_computer.setAccessible(true);
			Net_m_computer = Class.forName("dan200.computer.shared.NetworkedComputerHelper").getDeclaredField("m_computer");
			Net_m_computer.setAccessible(true);
			m_apis = Class.forName("dan200.computer.core.Computer").getDeclaredField("m_apis");
			m_apis.setAccessible(true);
			peripheralAPIClass = Class.forName("dan200.computer.core.apis.PeripheralAPI");
			parseSide = peripheralAPIClass.getDeclaredMethod("parseSide", new Class[]{Object[].class});
			parseSide.setAccessible(true);
			m_peripherals = peripheralAPIClass.getDeclaredField("m_peripherals");
			m_peripherals.setAccessible(true);
			target = Thread.class.getDeclaredField("target");
			target.setAccessible(true);
			valid = true;
		} catch(Exception e) {
			if(LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
			valid = false;
		}
	}
	
	@Override
	public boolean isTurtle(TileEntity tile) {
		return false;
	}
	
	@Override
	public boolean isComputer(TileEntity tile) {
		if(!valid) return false;
		return computerClass.isAssignableFrom(tile.getClass());
	}

	@Override
	public boolean isCC() {
		return valid;
	}
	
	protected Object get_local_tile_m_computer(TileEntity tile) throws IllegalArgumentException, IllegalAccessException {
		if(computerClass.isAssignableFrom(tile.getClass())) {
			return Computer_m_computer.get(tile);
		}
		return null;
	}
	
	@Override
	public ForgeDirection getOrientation(Object cObject, String computerSide, TileEntity pipe) {
		if(!valid) return ForgeDirection.UNKNOWN;
		if(!(cObject instanceof IComputerAccess)) return ForgeDirection.UNKNOWN;
		IComputerAccess computer = (IComputerAccess) cObject;
		WorldUtil world = new WorldUtil(pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities();
		for(AdjacentTile aTile: adjacent) {
			try {
				Object local_tile_m_computer = get_local_tile_m_computer(aTile.tile);
				if(local_tile_m_computer != null) {
					Object local_net_m_omputer = Net_m_computer.get(local_tile_m_computer);
					ArrayList local_m_apis = (ArrayList) m_apis.get(local_net_m_omputer);
					for(Object api: local_m_apis) {
						if(peripheralAPIClass.isAssignableFrom(api.getClass())) {
							int side = ((Integer) parseSide.invoke(api, new Object[]{new Object[]{(Object)computerSide}})).intValue();
							Object[] local_m_peripherals = (Object[]) m_peripherals.get(api);
							if(local_m_peripherals[side] == computer) {
								return aTile.orientation;
							}
						}
					}
				}
			} catch (IllegalArgumentException e) {
				if(LogisticsPipes.DEBUG) {
						e.printStackTrace();
				}
			} catch (IllegalAccessException e) {
				if(LogisticsPipes.DEBUG) {
					e.printStackTrace();
				}
			} catch(ClassCastException e) {
				if(LogisticsPipes.DEBUG) {
					e.printStackTrace();
				}
			} catch (InvocationTargetException e) {
				if(LogisticsPipes.DEBUG) {
					e.printStackTrace();
				}
			}
		}
		return ForgeDirection.UNKNOWN;
	}

	private Runnable getTaget(Thread thread) {
		try {
			return (Runnable) target.get(thread);
		} catch (SecurityException e) {
			if(LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			if(LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
		} catch (IllegalAccessException e) {
			if(LogisticsPipes.DEBUG) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public boolean isLuaThread(Thread thread) {
		Runnable tar = getTaget(thread);
		if(tar == null) {
			return false;
		}
		return tar.getClass().getName().contains("org.luaj.vm2.LuaThread");
	}
}
