package logisticspipes.proxy.cc;

import java.lang.reflect.Field;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.interfaces.ICCProxy;
import net.minecraft.tileentity.TileEntity;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;

public class CCProxy implements ICCProxy {
	
	private Field target;
	
	public CCProxy() {
		ComputerCraftAPI.registerPeripheralProvider(new LPPeripheralProvider());
		try {
			target = Thread.class.getDeclaredField("target");
			target.setAccessible(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isTurtle(TileEntity tile) {
		return tile instanceof TileTurtle;
	}
	
	@Override
	public boolean isComputer(TileEntity tile) {
		return tile instanceof TileComputer;
	}

	@Override
	public boolean isCC() {
		return true;
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
		tile.turtleConnect[tile.connections.get(tile.currentPC).ordinal()] = flag;
		tile.scheduleNeighborChange();
	}

	@Override
	public boolean getTurtleConnect(LogisticsTileGenericPipe tile) {
		return tile.turtleConnect[tile.connections.get(tile.currentPC).ordinal()];
	}

	@Override
	public int getLastCCID(LogisticsTileGenericPipe tile) {
		if(tile.currentPC == null) return -1;
		return tile.currentPC.getID();
	}

	@Override
	public void handleMesssage(int computerId, Object message, LogisticsTileGenericPipe tile, int sourceId) {
		for(IComputerAccess computer: tile.connections.keySet()) {
			if(computer.getID() == computerId) {
				computer.queueEvent(CCConstants.LP_CC_MESSAGE_EVENT, new Object[]{sourceId, message});
			}
		}
	}
}
