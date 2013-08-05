package logisticspipes.proxy.cc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.interfaces.ICCProxy;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.WorldUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import dan200.computer.api.IComputerAccess;

public class CCProxy implements ICCProxy {

	private Field Computer_m_computer;
	private Field Net_m_computer;
	private Field m_apis;
	private Field m_peripherals;
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
			m_peripherals = peripheralAPIClass.getDeclaredField("m_peripherals");
			m_peripherals.setAccessible(true);
			target = Thread.class.getDeclaredField("target");
			target.setAccessible(true);
			valid = true;
		} catch(Exception e) {
			e.printStackTrace();
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
	
	@SuppressWarnings("rawtypes")
	@Override
	public ForgeDirection getOrientation(Object cObject, TileEntity pipe) {
		if(!valid) return ForgeDirection.UNKNOWN;
		if(!(cObject instanceof IComputerAccess)) return ForgeDirection.UNKNOWN;
		IComputerAccess computer = (IComputerAccess) cObject;
		WorldUtil world = new WorldUtil(pipe.getWorldObj(), pipe.xCoord, pipe.yCoord, pipe.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities(false);
		for(AdjacentTile aTile: adjacent) {
			try {
				Object local_tile_m_computer = get_local_tile_m_computer(aTile.tile);
				if(local_tile_m_computer != null) {
					Object local_net_m_omputer = Net_m_computer.get(local_tile_m_computer);
					ArrayList local_m_apis = (ArrayList) m_apis.get(local_net_m_omputer);
					for(Object api: local_m_apis) {
						if(peripheralAPIClass.isAssignableFrom(api.getClass())) {
							Object[] local_m_peripherals = (Object[]) m_peripherals.get(api);
							for(Object computeraccess : local_m_peripherals) {
								if(computeraccess == computer) {
									return aTile.orientation;
								}
							}
						}
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch(ClassCastException e) {
				e.printStackTrace();
			}
		}
		return ForgeDirection.UNKNOWN;
	}

	private Runnable getTaget(Thread thread) {
		try {
			return (Runnable) target.get(thread);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
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

	@Override
	public void queueEvent(String event, Object[] arguments, LogisticsTileGenericPipe tile) {
		for(IComputerAccess computer: tile.connections.keySet()) {
			computer.queueEvent(event, arguments);
		}
	}

	@Override
	public void setTurtleConnect(boolean flag, LogisticsTileGenericPipe tile) {
		tile.turtleConnect[tile.connections.get(tile.lastPC).ordinal()] = flag;
		tile.scheduleNeighborChange();
	}

	@Override
	public boolean getTurtleConnect(LogisticsTileGenericPipe tile) {
		return tile.turtleConnect[tile.connections.get(tile.lastPC).ordinal()];
	}

	@Override
	public int getLastCCID(LogisticsTileGenericPipe tile) {
		if(tile.lastPC == null) return -1;
		return tile.lastPC.getID();
	}
}
