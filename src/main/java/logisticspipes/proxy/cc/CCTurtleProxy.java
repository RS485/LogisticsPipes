package logisticspipes.proxy.cc;

import java.lang.reflect.Field;

import net.minecraft.tileentity.TileEntity;

public class CCTurtleProxy extends CCProxy {
	
	private Field Turtle_m_computer;
	private Class<?> turtleClass;
	
	public CCTurtleProxy() {
		super();
		if(!valid) return;
		try {
			turtleClass = Class.forName("dan200.turtle.shared.TileEntityTurtle");
			Turtle_m_computer = turtleClass.getDeclaredField("m_computer");
			Turtle_m_computer.setAccessible(true);
			
		} catch(Exception e) {
			e.printStackTrace();
			valid = false;
		}
	}
	
	@Override
	public boolean isTurtle(TileEntity tile) {
		if(!valid) return false;
		return turtleClass.isInstance(tile);
	}
	
	@Override
	protected Object get_local_tile_m_computer(TileEntity tile) throws IllegalArgumentException, IllegalAccessException {
		if(turtleClass.isInstance(tile)) {
			return Turtle_m_computer.get(tile);
		}
		return super.get_local_tile_m_computer(tile);
	}
}
