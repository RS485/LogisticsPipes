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
import buildcraft.api.core.Orientations;
import dan200.computer.api.IComputerAccess;

public class CCProxy implements ICCProxy {

	private Field Turtle_m_computer;
	private Field Computer_m_computer;
	private Field Net_m_computer;
	private Field m_apis;
	private Field m_peripherals;
	private Method parseSide;
	private Class<?> turtleClass;
	private Class<?> computerClass;
	private Class<?> peripheralAPIClass;
	
	private boolean valid = false;
	
	public CCProxy() {
		try {
			turtleClass = Class.forName("dan200.turtle.shared.TileEntityTurtle");
			computerClass = Class.forName("dan200.computer.shared.TileEntityComputer");
			Turtle_m_computer = turtleClass.getDeclaredField("m_computer");
			Turtle_m_computer.setAccessible(true);
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
		if(!valid) return false;
		return turtleClass.isAssignableFrom(tile.getClass());
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

	@Override
	public Orientations getOrientation(IComputerAccess computer, String computerSide, TileEntity pipe) {
		if(!valid) return Orientations.Unknown;
		WorldUtil world = new WorldUtil(pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities();
		for(AdjacentTile aTile: adjacent) {
			try {
				Object local_tile_m_computer = null;
				if(turtleClass.isAssignableFrom(aTile.tile.getClass())) {
					local_tile_m_computer = Turtle_m_computer.get(aTile.tile);
				} else if(computerClass.isAssignableFrom(aTile.tile.getClass())) {
					local_tile_m_computer = Computer_m_computer.get(aTile.tile);
				}
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
		return Orientations.Unknown;
	}
}
